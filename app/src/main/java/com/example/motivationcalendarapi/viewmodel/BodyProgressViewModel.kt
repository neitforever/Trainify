package com.example.motivationcalendarapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.model.BodyProgress
import com.example.motivationcalendarapi.repositories.BodyProgressRepository
import kotlinx.coroutines.launch

class BodyProgressViewModel(
    private val repository: BodyProgressRepository
) : ViewModel() {

    val allProgress = repository.getAllProgress()

//    init {
//        viewModelScope.launch {
//            val currentProgress = repository.getAllProgress().first()
//            if (!currentProgress.isEmpty()) {
//                insertSampleData()
//            }
//        }
//    }
//
//    private fun insertSampleData() {
//        val samples = listOf(
//            createProgressEntry(60.0, 2024, Calendar.MARCH, 30),
//            createProgressEntry(75.0, 2024, Calendar.APRIL, 2),
//            createProgressEntry(80.0, 2024, Calendar.APRIL, 6)
//        )
//
//        viewModelScope.launch {
//            samples.forEach { repository.insert(it) }
//        }
//    }
//
//    private fun createProgressEntry(weight: Double, year: Int, month: Int, day: Int): BodyProgress {
//        val calendar = Calendar.getInstance().apply {
//            set(year, month, day, 12, 0, 0)
//            set(Calendar.SECOND, 0)
//            set(Calendar.MILLISECOND, 0)
//        }
//        return BodyProgress(
//            weight = weight,
//            timestamp = calendar.timeInMillis
//        )
//    }

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
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BodyProgressViewModel(repository) as T
    }
}