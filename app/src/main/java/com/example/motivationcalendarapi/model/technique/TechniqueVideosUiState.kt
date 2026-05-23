package com.example.motivationcalendarapi.model.technique

sealed interface TechniqueVideosUiState {
    data object Idle : TechniqueVideosUiState
    data object Loading : TechniqueVideosUiState

    data class Success(
        val videos: List<ExerciseTechniqueVideo>
    ) : TechniqueVideosUiState

    data class Error(
        val message: String
    ) : TechniqueVideosUiState
}
