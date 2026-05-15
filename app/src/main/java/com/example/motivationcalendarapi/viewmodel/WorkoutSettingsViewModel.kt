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

    val minCardioTime: StateFlow<Float> = repository.minCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxCardioTime: StateFlow<Float> = repository.maxCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 120f)

    val stepCardioTime: StateFlow<Float> = repository.stepCardioTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 5f)

    val minResistance: StateFlow<Float> = repository.minResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxResistance: StateFlow<Float> = repository.maxResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 20f)

    val stepResistance: StateFlow<Float> = repository.stepResistanceFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 1f)

    val minIncline: StateFlow<Float> = repository.minInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val maxIncline: StateFlow<Float> = repository.maxInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 15f)

    val stepIncline: StateFlow<Float> = repository.stepInclineFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.5f)

    fun saveCardioTimeSettings(min: Float, max: Float, step: Float) {
        viewModelScope.launch {
            repository.saveCardioTimeSettings(min, max, step)
        }
    }

    fun saveResistanceSettings(min: Float, max: Float, step: Float) {
        viewModelScope.launch {
            repository.saveResistanceSettings(min, max, step)
        }
    }

    fun saveInclineSettings(min: Float, max: Float, step: Float) {
        viewModelScope.launch {
            repository.saveInclineSettings(min, max, step)
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