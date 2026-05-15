package com.example.motivationcalendarapi.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.repositories.equipment.EquipmentAlternative
import com.example.motivationcalendarapi.repositories.equipment.EquipmentCandidate
import com.example.motivationcalendarapi.repositories.equipment.EquipmentRecognitionDataStore
import com.example.motivationcalendarapi.repositories.equipment.GeminiEquipmentRecognitionApi
import com.example.motivationcalendarapi.repositories.equipment.EquipmentRecognitionHighDemandException
import com.example.motivationcalendarapi.repositories.equipment.EquipmentRecognitionNetworkException
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

sealed class EquipmentMatchType {
    object Exact : EquipmentMatchType()
    object Similar : EquipmentMatchType()
}

data class SelectedEquipmentImage(val uri: Uri)

data class RecognizedEquipment(
    val equipmentKey: String,
    val nameLocalized: Map<String, String>,
    val descriptionLocalized: Map<String, String>,
    val confidence: Float,
    val alternatives: List<EquipmentAlternative> = emptyList()
) {
    fun getName(lang: String): String = nameLocalized[lang] ?: nameLocalized["en"] ?: equipmentKey
    fun getDescription(lang: String): String = descriptionLocalized[lang] ?: descriptionLocalized["en"] ?: ""
}

data class MatchedExercise(
    val exercise: Exercise,
    val matchType: EquipmentMatchType
)

data class EquipmentRecognitionUiState(
    val selectedImage: SelectedEquipmentImage? = null,
    val recognizedEquipment: RecognizedEquipment? = null,
    val matchedExercises: List<MatchedExercise> = emptyList(),
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null,
    val isVpnRequiredError: Boolean = false,
    val isHighDemandError: Boolean = false
)

private data class CachedEquipmentRecognitionState(
    val imageUri: String?,
    val recognizedEquipment: RecognizedEquipment?,
    val matchedExerciseIds: List<String>
)

class EquipmentRecognitionViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val gson = Gson()
    private val api = GeminiEquipmentRecognitionApi(context)
    private val cache = EquipmentRecognitionDataStore(context)

    private val _uiState = MutableStateFlow(EquipmentRecognitionUiState())
    val uiState: StateFlow<EquipmentRecognitionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val cached = cache.lastStateJsonFlow.first()?.let { json ->
                runCatching { gson.fromJson(json, CachedEquipmentRecognitionState::class.java) }.getOrNull()
            }
            if (cached != null) {
                _uiState.value = _uiState.value.copy(
                    selectedImage = cached.imageUri?.let { SelectedEquipmentImage(it.toUri()) },
                    recognizedEquipment = cached.recognizedEquipment
                )
            }
        }
    }

    fun restoreMatchedExercises(allExercises: List<Exercise>) {
        val recognized = _uiState.value.recognizedEquipment ?: return
        if (_uiState.value.matchedExercises.isNotEmpty()) return
        _uiState.value = _uiState.value.copy(
            matchedExercises = findMatchedExercises(recognized.equipmentKey, allExercises)
        )
    }

    fun setGalleryImage(uri: Uri) {
        viewModelScope.launch {
            runCatching { copyImageToInternalCache(uri) }
                .onSuccess { cachedUri ->
                    _uiState.value = EquipmentRecognitionUiState(
                        selectedImage = SelectedEquipmentImage(cachedUri)
                    )
                    saveCache()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun setCameraImage(bitmap: Bitmap) {
        viewModelScope.launch {
            runCatching { saveBitmapToInternalCache(bitmap) }
                .onSuccess { cachedUri ->
                    _uiState.value = EquipmentRecognitionUiState(
                        selectedImage = SelectedEquipmentImage(cachedUri)
                    )
                    saveCache()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun clear() {
        viewModelScope.launch {
            _uiState.value = EquipmentRecognitionUiState()
            cache.clearLastState()
        }
    }

    fun analyzeSelectedImage(
        allExercises: List<Exercise>,
        fallbackError: String
    ) {
        val image = _uiState.value.selectedImage ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = fallbackError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                recognizedEquipment = null,
                matchedExercises = emptyList(),
                isAnalyzing = true,
                errorMessage = null,
                isVpnRequiredError = false,
                isHighDemandError = false
            )

            runCatching {
                val candidates = buildEquipmentCandidates(allExercises)
                val result = api.recognize(image.uri, candidates)
                val selectedCandidate = candidates.firstOrNull { it.key == result.equipmentKey }
                    ?: candidates.first()

                RecognizedEquipment(
                    equipmentKey = selectedCandidate.key,
                    nameLocalized = mapOf(
                        "en" to selectedCandidate.nameEn,
                        "ru" to selectedCandidate.nameRu,
                        "be" to selectedCandidate.nameBe
                    ),
                    descriptionLocalized = mapOf(
                        "en" to result.descriptionEn.ifBlank { selectedCandidate.nameEn },
                        "ru" to result.descriptionRu.ifBlank { result.descriptionEn.ifBlank { selectedCandidate.nameRu } },
                        "be" to result.descriptionBe.ifBlank { result.descriptionEn.ifBlank { selectedCandidate.nameBe } }
                    ),
                    confidence = result.confidence.coerceIn(0f, 1f),
                    alternatives = result.alternatives
                )
            }.onSuccess { equipment ->
                _uiState.value = _uiState.value.copy(
                    recognizedEquipment = equipment,
                    matchedExercises = findMatchedExercises(equipment.equipmentKey, allExercises),
                    isAnalyzing = false
                )
                saveCache()
            }.onFailure { error ->
                val isNetworkError = error is EquipmentRecognitionNetworkException
                val isHighDemandError = error is EquipmentRecognitionHighDemandException
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    errorMessage = when {
                        isNetworkError -> context.getString(R.string.vpn_required_message)
                        isHighDemandError -> context.getString(R.string.gemini_high_demand_message)
                        else -> error.message ?: context.getString(R.string.recognition_error_message)
                    },
                    isVpnRequiredError = isNetworkError,
                    isHighDemandError = isHighDemandError
                )
                saveCache()
            }
        }
    }

    private fun buildEquipmentCandidates(exercises: List<Exercise>): List<EquipmentCandidate> {
        return exercises
            .mapNotNull { exercise ->
                val en = exercise.equipmentLocalized["en"].orEmpty().trim()
                if (en.isBlank()) return@mapNotNull null
                EquipmentCandidate(
                    key = en.toEquipmentKey(),
                    nameEn = en,
                    nameRu = exercise.equipmentLocalized["ru"] ?: en,
                    nameBe = exercise.equipmentLocalized["be"] ?: en
                )
            }
            .distinctBy { it.key }
            .sortedBy { it.nameEn.lowercase(Locale.ROOT) }
    }

    private fun findMatchedExercises(equipmentKey: String, exercises: List<Exercise>): List<MatchedExercise> {
        val exact = exercises.filter { exercise ->
            exercise.equipmentLocalized["en"].orEmpty().toEquipmentKey() == equipmentKey
        }.map { MatchedExercise(it, EquipmentMatchType.Exact) }

        if (exact.isNotEmpty()) return exact.sortedForUi("en")

        val normalizedKeyWords = equipmentKey.split("_").filter { it.length > 2 }
        val similar = exercises.filter { exercise ->
            val source = listOf(
                exercise.equipmentLocalized["en"].orEmpty(),
                exercise.bodyPartLocalized["en"].orEmpty(),
                exercise.targetLocalized["en"].orEmpty(),
                exercise.nameLocalized["en"].orEmpty()
            ).joinToString(" ").lowercase(Locale.ROOT)

            normalizedKeyWords.any { word -> source.contains(word) }
        }.map { MatchedExercise(it, EquipmentMatchType.Similar) }

        return similar.sortedForUi("en")
    }

    private fun List<MatchedExercise>.sortedForUi(lang: String): List<MatchedExercise> =
        sortedWith(
            compareByDescending<MatchedExercise> { it.exercise.favorite }
                .thenBy { it.exercise.getName(lang).lowercase(Locale.getDefault()) }
        )

    private suspend fun saveCache() {
        val state = _uiState.value
        val cacheState = CachedEquipmentRecognitionState(
            imageUri = state.selectedImage?.uri?.toString(),
            recognizedEquipment = state.recognizedEquipment,
            matchedExerciseIds = state.matchedExercises.map { it.exercise.id }
        )
        cache.saveLastState(gson.toJson(cacheState))
    }

    private fun copyImageToInternalCache(sourceUri: Uri): Uri {
        val file = File(context.filesDir, "equipment_recognition_image_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(file).use { output -> input.copyTo(output) }
        } ?: error("Cannot open selected image.")
        return Uri.fromFile(file)
    }

    private fun saveBitmapToInternalCache(bitmap: Bitmap): Uri {
        val file = File(context.filesDir, "equipment_recognition_camera_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { output -> bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output) }
        return Uri.fromFile(file)
    }

    private fun String.toEquipmentKey(): String = trim()
        .lowercase(Locale.ROOT)
        .replace(Regex("[^a-z0-9а-яёіўўґєії]+"), "_")
        .trim('_')
}
