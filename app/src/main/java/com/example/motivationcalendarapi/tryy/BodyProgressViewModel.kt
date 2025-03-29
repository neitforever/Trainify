package com.example.motivationcalendarapi.tryy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BodyProgressViewModel(
    private val repository: BodyProgressRepository
) : ViewModel() {
    val allProgress = repository.getAllProgress()

    fun addProgress(progress: BodyProgress) = viewModelScope.launch {
        repository.insert(progress)
    }

    fun deleteProgress(progress: BodyProgress) = viewModelScope.launch {
        repository.delete(progress)
    }
}

class BodyProgressViewModelFactory(
    private val repository: BodyProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BodyProgressViewModel(repository) as T
    }
}