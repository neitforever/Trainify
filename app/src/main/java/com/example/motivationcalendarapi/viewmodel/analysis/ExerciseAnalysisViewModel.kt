package com.example.motivationcalendarapi.viewmodel.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisMetric
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisPeriod
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisUiState
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisViewMode
import com.example.motivationcalendarapi.repositories.analysis.ExerciseAnalysisRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExerciseAnalysisViewModel(
    private val repository: ExerciseAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseAnalysisUiState())
    val uiState: StateFlow<ExerciseAnalysisUiState> = _uiState.asStateFlow()

    private var analysisJob: Job? = null
    private var currentExerciseId: String? = null

    fun observeExercise(exerciseId: String, cardType: ExerciseCardType) {
        if (currentExerciseId == exerciseId && analysisJob != null) return
        currentExerciseId = exerciseId
        val defaultMetric = defaultMetricFor(cardType)
        _uiState.update { it.copy(selectedMetric = defaultMetric, isLoading = true) }
        subscribe(exerciseId)
    }

    fun changePeriod(period: ExerciseAnalysisPeriod) {
        if (_uiState.value.selectedPeriod == period) return
        _uiState.update { it.copy(selectedPeriod = period) }
        currentExerciseId?.let(::subscribe)
    }

    fun changeMetric(metric: ExerciseAnalysisMetric) {
        _uiState.update { it.copy(selectedMetric = metric) }
        currentExerciseId?.let(::subscribe)
    }

    fun toggleProjectionExpanded() {
        _uiState.update { it.copy(isProjectionExpanded = !it.isProjectionExpanded) }
    }

    fun toggleOneRepMaxInfoExpanded() {
        _uiState.update { it.copy(isOneRepMaxInfoExpanded = !it.isOneRepMaxInfoExpanded) }
    }

    fun toggleWeeklyProgressionExpanded() {
        _uiState.update { it.copy(isWeeklyProgressionExpanded = !it.isWeeklyProgressionExpanded) }
    }

    fun toggleShowAllRecords() {
        _uiState.update { it.copy(showAllRecords = !it.showAllRecords) }
    }

    fun changeViewMode(mode: ExerciseAnalysisViewMode) {
        _uiState.update { it.copy(selectedViewMode = mode) }
    }

    private fun subscribe(exerciseId: String) {
        analysisJob?.cancel()
        val state = _uiState.value
        analysisJob = viewModelScope.launch {
            repository.observeExerciseAnalysis(
                exerciseId = exerciseId,
                period = state.selectedPeriod,
                metric = state.selectedMetric
            )
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Analysis loading error"
                        )
                    }
                }
                .collect { result ->
                    val safeMetric = if (
                        state.selectedMetric == ExerciseAnalysisMetric.RELATIVE_STRENGTH &&
                        !result.hasActualBodyWeight
                    ) {
                        ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX
                    } else {
                        state.selectedMetric
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            selectedMetric = safeMetric,
                            result = result,
                            errorMessage = null
                        )
                    }
                    if (safeMetric != state.selectedMetric) {
                        changeMetric(safeMetric)
                    }
                }
        }
    }

    private fun defaultMetricFor(cardType: ExerciseCardType): ExerciseAnalysisMetric {
        return when (cardType) {
            ExerciseCardType.STRENGTH -> ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX
            ExerciseCardType.BIKE -> ExerciseAnalysisMetric.TIME
            ExerciseCardType.TREADMILL -> ExerciseAnalysisMetric.TIME
        }
    }
}

class ExerciseAnalysisViewModelFactory(
    private val repository: ExerciseAnalysisRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExerciseAnalysisViewModel(repository) as T
    }
}
