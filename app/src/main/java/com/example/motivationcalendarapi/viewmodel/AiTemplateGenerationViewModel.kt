package com.example.motivationcalendarapi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.notifications.AiGenerationBackgroundEvent
import com.example.motivationcalendarapi.notifications.AiGenerationBackgroundState
import com.example.motivationcalendarapi.notifications.AiGenerationForegroundService
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class AiTemplateGenerationUiState(
    val prompt: String = "",
    val selectedBodyParts: List<String> = emptyList(),
    val selectedEquipment: List<String> = emptyList(),
    val difficulty: String = "",
    val minExercises: Int = 4,
    val maxExercises: Int = 6,
    val draft: GeneratedTemplateDraft? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val isHighDemandError: Boolean = false
)

class AiTemplateGenerationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AiTemplateGenerationUiState())
    val uiState: StateFlow<AiTemplateGenerationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            AiGenerationBackgroundState.events.collect { event ->
                when (event) {
                    is AiGenerationBackgroundEvent.TemplateStarted -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false, isHighDemandError = false, draft = null) }
                    }
                    is AiGenerationBackgroundEvent.TemplateSuccess -> {
                        _uiState.update { it.copy(draft = event.draft, isLoading = false, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
                    }
                    is AiGenerationBackgroundEvent.TemplateFailure -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = event.message,
                                isNetworkError = event.isNetworkError,
                                isHighDemandError = event.isHighDemandError
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }

    fun setPrompt(value: String) {
        _uiState.update { it.copy(prompt = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setDifficulty(value: String) {
        _uiState.update { it.copy(difficulty = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setDefaultDifficultyIfBlank(value: String) {
        _uiState.update { if (it.difficulty.isBlank()) it.copy(difficulty = value) else it }
    }

    fun toggleBodyPart(value: String) {
        _uiState.update { state ->
            val next = if (state.selectedBodyParts.contains(value)) {
                state.selectedBodyParts - value
            } else {
                state.selectedBodyParts + value
            }
            state.copy(selectedBodyParts = next, errorMessage = null, isNetworkError = false, isHighDemandError = false)
        }
    }

    fun toggleEquipment(value: String) {
        _uiState.update { state ->
            val next = if (state.selectedEquipment.contains(value)) {
                state.selectedEquipment - value
            } else {
                state.selectedEquipment + value
            }
            state.copy(selectedEquipment = next, errorMessage = null, isNetworkError = false, isHighDemandError = false)
        }
    }

    fun clearEquipment() {
        _uiState.update { it.copy(selectedEquipment = emptyList(), errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setExerciseRange(min: Int, max: Int) {
        val safeMin = min.coerceIn(1, 15)
        val safeMax = max.coerceIn(safeMin, 15)
        _uiState.update { it.copy(minExercises = safeMin, maxExercises = safeMax) }
    }

    fun updateDraft(draft: GeneratedTemplateDraft) {
        _uiState.update { it.copy(draft = draft, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun clearDraft() {
        _uiState.update { it.copy(draft = null, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setError(message: String?) {
        _uiState.update { it.copy(errorMessage = message, isNetworkError = false, isHighDemandError = false) }
    }

    fun resetAfterSave() {
        _uiState.value = AiTemplateGenerationUiState()
    }

    fun addExercises(exercises: List<Exercise>, lang: String) {
        val current = _uiState.value.draft ?: return
        val existingIds = current.exercises.map { it.exercise.id }.toSet()
        val newExtended = exercises
            .filterNot { it.id in existingIds }
            .map { exercise ->
                ExtendedExercise(
                    exercise = exercise,
                    sets = defaultSetsFor(exercise.getCardType(lang))
                )
            }
        if (newExtended.isNotEmpty()) {
            updateDraft(current.copy(exercises = current.exercises + newExtended))
        }
    }

    private fun defaultSetsFor(cardType: ExerciseCardType): List<ExerciseSet> = when (cardType) {
        ExerciseCardType.STRENGTH -> List(3) { ExerciseSet(rep = 12, weight = 30f, status = SetStatus.NONE) }
        ExerciseCardType.BIKE -> listOf(ExerciseSet(time = 20f, resistance = 5f, status = SetStatus.NONE))
        ExerciseCardType.TREADMILL -> listOf(ExerciseSet(time = 20f, resistance = 6f, incline = 3f, status = SetStatus.NONE))
    }

    fun generate(
        lang: String,
        localExercises: List<Exercise>,
        requiredFieldsMessage: String,
        highDemandMessage: String,
        context: Context? = null
    ) {
        val state = _uiState.value
        if (state.prompt.isBlank() || state.selectedBodyParts.isEmpty() || state.difficulty.isBlank()) {
            _uiState.update { it.copy(errorMessage = requiredFieldsMessage, isNetworkError = false, isHighDemandError = false) }
            return
        }

        val appContext = context?.applicationContext
        if (appContext == null) {
            _uiState.update { it.copy(errorMessage = highDemandMessage, isNetworkError = true, isHighDemandError = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false, isHighDemandError = false, draft = null) }
        AiGenerationForegroundService.startTemplate(
            context = appContext,
            prompt = state.prompt,
            bodyParts = state.selectedBodyParts,
            equipment = state.selectedEquipment,
            difficulty = state.difficulty,
            minExercises = state.minExercises,
            maxExercises = state.maxExercises,
            lang = lang,
            localExercises = localExercises
        )
    }
}
