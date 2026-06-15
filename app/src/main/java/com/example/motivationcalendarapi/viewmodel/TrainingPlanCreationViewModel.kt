package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.DayOfWeek

data class TrainingPlanCreationUiState(
    val prompt: String = "",
    val weekCount: Int = 4,
    val selectedDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
    val selectedBodyParts: List<String> = emptyList(),
    val selectedEquipment: List<String> = emptyList(),
    val bodyPartSectionExpanded: Boolean = false,
    val equipmentSectionExpanded: Boolean = false,
    val durationMinutes: Int = 45,
    val exerciseCount: Int = 6,
    val intensity: String = "",
    val isGenerating: Boolean = false,
    val errorMessage: String? = null
)

class TrainingPlanCreationViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingPlanCreationUiState())
    val uiState: StateFlow<TrainingPlanCreationUiState> = _uiState.asStateFlow()

    fun ensureDefaults(defaultIntensity: String) {
        _uiState.update { state ->
            state.copy(intensity = state.intensity.ifBlank { defaultIntensity })
        }
    }

    fun setPrompt(value: String) = _uiState.update { it.copy(prompt = value, errorMessage = null) }
    fun setWeekCount(value: Int) = _uiState.update { it.copy(weekCount = value.coerceIn(1, 12), errorMessage = null) }
    fun setSelectedDays(value: Set<DayOfWeek>) = _uiState.update { it.copy(selectedDays = value, errorMessage = null) }
    fun setSelectedBodyParts(value: List<String>) = _uiState.update { it.copy(selectedBodyParts = value, errorMessage = null) }
    fun setSelectedEquipment(value: List<String>) = _uiState.update { it.copy(selectedEquipment = value, errorMessage = null) }
    fun setBodyPartSectionExpanded(value: Boolean) = _uiState.update { it.copy(bodyPartSectionExpanded = value) }
    fun setEquipmentSectionExpanded(value: Boolean) = _uiState.update { it.copy(equipmentSectionExpanded = value) }
    fun setDurationMinutes(value: Int) = _uiState.update { it.copy(durationMinutes = value.coerceIn(1, 300), errorMessage = null) }
    fun setExerciseCount(value: Int) = _uiState.update { it.copy(exerciseCount = value.coerceIn(1, 15), errorMessage = null) }
    fun setIntensity(value: String) = _uiState.update { it.copy(intensity = value, errorMessage = null) }
    fun setGenerating(value: Boolean) = _uiState.update { it.copy(isGenerating = value, errorMessage = if (value) null else it.errorMessage) }
    fun setError(message: String) = _uiState.update { it.copy(isGenerating = false, errorMessage = message) }

    fun reset(defaultIntensity: String = "") {
        _uiState.value = TrainingPlanCreationUiState(intensity = defaultIntensity)
    }
}
