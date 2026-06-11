package com.example.motivationcalendarapi.viewmodel.technique_analysis

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.technique_analysis.ExerciseTechniqueAnalysisUiState
import com.example.motivationcalendarapi.repositories.technique_analysis.ExerciseTechniqueAnalysisRepository
import com.example.motivationcalendarapi.repositories.technique_analysis.TechniqueAnalysisStage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseTechniqueAnalysisViewModel(
    private val repository: ExerciseTechniqueAnalysisRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExerciseTechniqueAnalysisUiState())
    val uiState: StateFlow<ExerciseTechniqueAnalysisUiState> = _uiState.asStateFlow()

    private val exerciseStates = mutableMapOf<String, ExerciseTechniqueAnalysisUiState>()
    private var dotsJob: Job? = null

    fun openForExercise(exerciseId: String) {
        val current = _uiState.value
        if (current.activeExerciseId == exerciseId) return

        current.activeExerciseId?.let { previousExerciseId ->
            exerciseStates[previousExerciseId] = current
        }

        _uiState.value = exerciseStates[exerciseId]
            ?.copy(activeExerciseId = exerciseId)
            ?: ExerciseTechniqueAnalysisUiState(activeExerciseId = exerciseId)

        syncDotsAnimation()
    }

    fun selectVideo(uri: Uri) {
        _uiState.update {
            it.copy(
                selectedVideoUri = uri,
                result = null,
                matchedExercise = null,
                errorMessage = null
            )
        }
        saveCurrentExerciseState()
    }

    fun clearCurrentExerciseState() {
        _uiState.value.activeExerciseId?.let { exerciseStates.remove(it) }
        _uiState.value = ExerciseTechniqueAnalysisUiState(activeExerciseId = _uiState.value.activeExerciseId)
        stopDotsAnimation()
    }

    fun analyze(
        currentExercise: Exercise,
        allExercises: List<Exercise>,
        lang: String
    ) {
        val uri = _uiState.value.selectedVideoUri ?: return
        if (_uiState.value.isPreparingVideo || _uiState.value.isAnalyzing) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeExerciseId = currentExercise.id,
                    isPreparingVideo = true,
                    isAnalyzing = false,
                    errorMessage = null,
                    loadingDots = "."
                )
            }
            syncDotsAnimation()
            saveCurrentExerciseState()

            runCatching {
                repository.analyzeVideo(
                    videoUri = uri,
                    currentExercise = currentExercise,
                    allExercises = allExercises,
                    lang = lang,
                    onStageChange = { stage ->
                        _uiState.update { current ->
                            when (stage) {
                                TechniqueAnalysisStage.PREPARING_VIDEO -> current.copy(isPreparingVideo = true, isAnalyzing = false)
                                TechniqueAnalysisStage.UPLOADING_AND_ANALYZING -> current.copy(isPreparingVideo = false, isAnalyzing = true)
                            }
                        }
                        syncDotsAnimation()
                        saveCurrentExerciseState()
                    }
                )
            }.onSuccess { result ->
                val matchedExercise = result.matchedExerciseId?.let { id -> allExercises.firstOrNull { it.id == id } }
                _uiState.update {
                    it.copy(
                        isPreparingVideo = false,
                        isAnalyzing = false,
                        result = result,
                        matchedExercise = matchedExercise?.takeIf { exercise -> exercise.id != currentExercise.id },
                        errorMessage = null
                    )
                }
                stopDotsAnimation()
                saveCurrentExerciseState()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isPreparingVideo = false,
                        isAnalyzing = false,
                        errorMessage = error.message ?: "Technique analysis failed."
                    )
                }
                stopDotsAnimation()
                saveCurrentExerciseState()
            }
        }
    }

    private fun syncDotsAnimation() {
        val loading = _uiState.value.isPreparingVideo || _uiState.value.isAnalyzing
        if (!loading) {
            stopDotsAnimation()
            return
        }
        if (dotsJob?.isActive == true) return
        dotsJob = viewModelScope.launch {
            val values = listOf(".", "..", "...")
            var index = 0
            while (_uiState.value.isPreparingVideo || _uiState.value.isAnalyzing) {
                _uiState.update { it.copy(loadingDots = values[index % values.size]) }
                saveCurrentExerciseState()
                index++
                delay(420L)
            }
        }
    }

    private fun stopDotsAnimation() {
        dotsJob?.cancel()
        dotsJob = null
    }

    private fun saveCurrentExerciseState() {
        val state = _uiState.value
        state.activeExerciseId?.let { exerciseStates[it] = state }
    }
}

class ExerciseTechniqueAnalysisViewModelFactory(
    private val repository: ExerciseTechniqueAnalysisRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExerciseTechniqueAnalysisViewModel(repository) as T
    }
}
