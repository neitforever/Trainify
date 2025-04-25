package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.repositories.MainRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutSettingsViewModel(private val repository: MainRepository) : ViewModel() {
    val minRep: StateFlow<Int> = repository.minRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val maxRep: StateFlow<Int> = repository.maxRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val stepRep: StateFlow<Int> = repository.stepRepFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val minWeight: StateFlow<Float> = repository.minWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxWeight: StateFlow<Float> = repository.maxWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val stepWeight: StateFlow<Float> = repository.stepWeightFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    fun saveRepSettings(min: Int, max: Int, step: Int) {
        viewModelScope.launch {
            repository.saveRepSettings(min, max, step)
        }
    }

    fun saveWeightSettings(min: Float, max: Float, step: Float) {
        viewModelScope.launch {
            repository.saveWeightSettings(min, max, step)
        }
    }
}

class WorkoutSettingsViewModelFactory(
    private val repository: MainRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WorkoutSettingsViewModel(repository) as T
    }
}