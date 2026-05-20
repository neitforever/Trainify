package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.repositories.ai.AiGenerationHighDemandException
import com.example.motivationcalendarapi.repositories.ai.AiGenerationNetworkException
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AiExerciseGenerationUiState(
    val prompt: String = "",
    val selectedBodyPart: String = "",
    val selectedEquipment: String = "",
    val difficulty: String = "",
    val draft: Exercise? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNetworkError: Boolean = false,
    val isHighDemandError: Boolean = false
)

class AiExerciseGenerationViewModel : ViewModel() {
    private val api = GeminiAiGenerationApi()

    private val _uiState = MutableStateFlow(AiExerciseGenerationUiState())
    val uiState: StateFlow<AiExerciseGenerationUiState> = _uiState.asStateFlow()

    fun setPrompt(value: String) {
        _uiState.update { it.copy(prompt = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setBodyPart(value: String) {
        _uiState.update { it.copy(selectedBodyPart = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setEquipment(value: String) {
        _uiState.update { it.copy(selectedEquipment = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun setDifficulty(value: String) {
        _uiState.update { it.copy(difficulty = value, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun updateDraft(exercise: Exercise) {
        _uiState.update { it.copy(draft = exercise, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun clearDraft() {
        _uiState.update { it.copy(draft = null, errorMessage = null, isNetworkError = false, isHighDemandError = false) }
    }

    fun generate(
        lang: String,
        localExercises: List<Exercise>,
        requiredFieldsMessage: String,
        highDemandMessage: String
    ) {
        val state = _uiState.value
        if (state.prompt.isBlank() || state.selectedBodyPart.isBlank() || state.selectedEquipment.isBlank() || state.difficulty.isBlank()) {
            _uiState.update { it.copy(errorMessage = requiredFieldsMessage, isNetworkError = false, isHighDemandError = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, isNetworkError = false, isHighDemandError = false, draft = null) }
            runCatching {
                api.generateExercise(
                    prompt = state.prompt,
                    selectedBodyPart = state.selectedBodyPart,
                    selectedEquipment = state.selectedEquipment,
                    levelRange = state.difficulty,
                    lang = lang,
                    localExercises = localExercises
                )
            }.onSuccess { exercise ->
                _uiState.update { it.copy(draft = exercise, isLoading = false, isNetworkError = false, isHighDemandError = false) }
            }.onFailure { error ->
                val isNetworkError = error is AiGenerationNetworkException
                val isHighDemandError = error is AiGenerationHighDemandException
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = when {
                            isHighDemandError -> highDemandMessage
                            else -> error.message ?: "Generation error"
                        },
                        isNetworkError = isNetworkError,
                        isHighDemandError = isHighDemandError
                    )
                }
            }
        }
    }
}
