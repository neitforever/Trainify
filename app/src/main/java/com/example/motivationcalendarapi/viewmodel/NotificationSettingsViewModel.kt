package com.example.motivationcalendarapi.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.motivationcalendarapi.notifications.NotificationScheduler
import com.example.motivationcalendarapi.notifications.NotificationSettings
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    private val repository: NotificationSettingsDataStore,
    private val appContext: Context
) : ViewModel() {
    val settings: StateFlow<NotificationSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationSettings())

    fun setWorkoutActiveEnabled(enabled: Boolean) = viewModelScope.launch { repository.setWorkoutActiveEnabled(enabled) }

    fun setWorkoutReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setWorkoutReminderEnabled(enabled)
        if (enabled) NotificationScheduler.scheduleWorkoutReminder(appContext) else NotificationScheduler.cancelWorkoutReminder(appContext)
    }

    fun setAiExerciseCreatedEnabled(enabled: Boolean) = viewModelScope.launch { repository.setAiExerciseCreatedEnabled(enabled) }
    fun setAiTemplateCreatedEnabled(enabled: Boolean) = viewModelScope.launch { repository.setAiTemplateCreatedEnabled(enabled) }
    fun setWeightProgressEnabled(enabled: Boolean) = viewModelScope.launch { repository.setWeightProgressEnabled(enabled) }

    fun setWeightProgressReminderEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setWeightProgressReminderEnabled(enabled)
        if (enabled) NotificationScheduler.scheduleWeightProgressReminder(appContext) else NotificationScheduler.cancelWeightProgressReminder(appContext)
    }

    fun setWorkoutReminderDays(days: Int) = viewModelScope.launch {
        repository.setWorkoutReminderDays(days)
        NotificationScheduler.scheduleWorkoutReminder(appContext)
    }

    fun setWeightProgressReminderDays(days: Int) = viewModelScope.launch {
        repository.setWeightProgressReminderDays(days)
        NotificationScheduler.scheduleWeightProgressReminder(appContext)
    }
}

class NotificationSettingsViewModelFactory(
    private val repository: NotificationSettingsDataStore,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationSettingsViewModel(repository, appContext) as T
    }
}
