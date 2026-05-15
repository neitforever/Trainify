package com.example.motivationcalendarapi.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.motivationcalendarapi.model.Exercise
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed class SelectedEquipmentImage {
    data class Gallery(val uri: Uri) : SelectedEquipmentImage()
    data class Camera(val bitmap: Bitmap) : SelectedEquipmentImage()
}

data class RecognizedEquipment(
    val nameLocalized: Map<String, String>,
    val descriptionLocalized: Map<String, String>,
    val confidence: Float? = null
) {
    fun getName(lang: String): String = nameLocalized[lang] ?: nameLocalized["en"] ?: ""
    fun getDescription(lang: String): String = descriptionLocalized[lang] ?: descriptionLocalized["en"] ?: ""
}

data class EquipmentRecognitionUiState(
    val selectedImage: SelectedEquipmentImage? = null,
    val recognizedEquipment: RecognizedEquipment? = null,
    val suitableExercises: List<Exercise> = emptyList(),
    val isAnalyzing: Boolean = false,
    val errorMessage: String? = null
)

class EquipmentRecognitionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EquipmentRecognitionUiState())
    val uiState: StateFlow<EquipmentRecognitionUiState> = _uiState.asStateFlow()

    fun setGalleryImage(uri: Uri) {
        _uiState.value = _uiState.value.copy(
            selectedImage = SelectedEquipmentImage.Gallery(uri),
            errorMessage = null
        )
    }

    fun setCameraImage(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            selectedImage = SelectedEquipmentImage.Camera(bitmap),
            errorMessage = null
        )
    }

    fun clear() {
        _uiState.value = EquipmentRecognitionUiState()
    }

    fun analyzeSelectedImage(
        allExercises: List<Exercise>,
        lang: String,
        fallbackError: String
    ) {
        val image = _uiState.value.selectedImage ?: run {
            _uiState.value = _uiState.value.copy(errorMessage = fallbackError)
            return
        }

        _uiState.value = _uiState.value.copy(isAnalyzing = true, errorMessage = null)

        val equipment = recognizeEquipmentLocally(image, allExercises, lang)
        val matchedExercises = allExercises
            .filter { exercise ->
                val exerciseEquipment = exercise.getEquipment(lang).lowercase(Locale.getDefault())
                val equipmentName = equipment.getName(lang).lowercase(Locale.getDefault())
                exerciseEquipment == equipmentName || exerciseEquipment.contains(equipmentName) || equipmentName.contains(exerciseEquipment)
            }
            .sortedWith(compareByDescending<Exercise> { it.favorite }.thenBy { it.getName(lang).lowercase(Locale.getDefault()) })

        _uiState.value = _uiState.value.copy(
            recognizedEquipment = equipment,
            suitableExercises = matchedExercises,
            isAnalyzing = false
        )
    }

    private fun recognizeEquipmentLocally(
        image: SelectedEquipmentImage,
        exercises: List<Exercise>,
        lang: String
    ): RecognizedEquipment {
        val availableEquipment = exercises
            .map { it.getEquipment(lang).trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }

        val galleryName = when (image) {
            is SelectedEquipmentImage.Gallery -> image.uri.lastPathSegment.orEmpty().lowercase(Locale.getDefault())
            is SelectedEquipmentImage.Camera -> ""
        }

        val detected = availableEquipment.firstOrNull { equipment ->
            galleryName.contains(equipment.lowercase(Locale.getDefault()).replace(" ", "")) ||
                    galleryName.contains(equipment.lowercase(Locale.getDefault()))
        } ?: availableEquipment.firstOrNull().orEmpty()

        return RecognizedEquipment(
            nameLocalized = mapOf(
                "en" to detected,
                "ru" to detected,
                "be" to detected
            ),
            descriptionLocalized = mapOf(
                "en" to "The selected image was processed locally. To recognize equipment by real visual features, connect a cloud vision API or add a trained on-device model.",
                "ru" to "Выбранное изображение обработано локально. Для распознавания тренажёра по реальным визуальным признакам нужно подключить cloud vision API или добавить обученную on-device модель.",
                "be" to "Выбраная выява апрацавана лакальна. Для распазнавання трэнажора па рэальных візуальных прыкметах трэба падключыць cloud vision API або дадаць навучаную on-device мадэль."
            ),
            confidence = null
        )
    }
}
