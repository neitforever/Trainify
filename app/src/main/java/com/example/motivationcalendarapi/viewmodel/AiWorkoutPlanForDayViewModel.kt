package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AiWorkoutPlanForDayUiState(
    val prompt: String = "",
    val customHours: Int = 0,
    val customMinutes: Int = 45,
    val intensity: String = "",
    val focus: String = "",
    val minExercises: Int = 4,
    val maxExercises: Int = 6,
    val isGenerating: Boolean = false,
    val generationError: String? = null
)

class AiWorkoutPlanForDayViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(AiWorkoutPlanForDayUiState())
    val uiState: StateFlow<AiWorkoutPlanForDayUiState> = _uiState.asStateFlow()

    fun ensureDefaults(defaultFocus: String, defaultIntensity: String) {
        _uiState.update { state ->
            state.copy(
                focus = state.focus.ifBlank { defaultFocus },
                intensity = state.intensity.ifBlank { defaultIntensity }
            )
        }
    }

    fun setPrompt(value: String) {
        _uiState.update { it.copy(prompt = value, generationError = null) }
    }

    fun setDuration(totalMinutes: Int) {
        val safe = totalMinutes.coerceIn(1, 300)
        _uiState.update { it.copy(customHours = safe / 60, customMinutes = safe % 60, generationError = null) }
    }

    fun setFocus(value: String) {
        _uiState.update { it.copy(focus = value, generationError = null) }
    }

    fun setIntensity(value: String) {
        _uiState.update { it.copy(intensity = value, generationError = null) }
    }

    fun setExerciseRange(min: Int, max: Int) {
        val safeMin = min.coerceIn(1, 15)
        val safeMax = max.coerceIn(safeMin, 15)
        _uiState.update { it.copy(minExercises = safeMin, maxExercises = safeMax, generationError = null) }
    }

    fun setGenerating(value: Boolean) {
        _uiState.update { it.copy(isGenerating = value, generationError = if (value) null else it.generationError) }
    }

    fun setGenerationError(message: String) {
        _uiState.update { it.copy(isGenerating = false, generationError = message) }
    }

    fun resetForm(defaultFocus: String = "", defaultIntensity: String = "") {
        _uiState.value = AiWorkoutPlanForDayUiState(focus = defaultFocus, intensity = defaultIntensity)
    }
}
