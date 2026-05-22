package com.example.motivationcalendarapi.repositories

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.motivationcalendarapi.notifications.NotificationSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationSettingsDataStore(private val context: Context) {
    private object Keys {
        val WORKOUT_ACTIVE_ENABLED = booleanPreferencesKey("notification_workout_active_enabled")
        val WORKOUT_REMINDER_ENABLED = booleanPreferencesKey("notification_workout_reminder_enabled")
        val AI_EXERCISE_CREATED_ENABLED = booleanPreferencesKey("notification_ai_exercise_created_enabled")
        val AI_TEMPLATE_CREATED_ENABLED = booleanPreferencesKey("notification_ai_template_created_enabled")
        val WEIGHT_PROGRESS_ENABLED = booleanPreferencesKey("notification_weight_progress_enabled")
        val WEIGHT_PROGRESS_REMINDER_ENABLED = booleanPreferencesKey("notification_weight_progress_reminder_enabled")
        val WORKOUT_REMINDER_DAYS = intPreferencesKey("notification_workout_reminder_days")
        val WEIGHT_PROGRESS_REMINDER_DAYS = intPreferencesKey("notification_weight_progress_reminder_days")
    }

    val settingsFlow: Flow<NotificationSettings> = context.dataStore.data.map { preferences ->
        NotificationSettings(
            workoutActiveEnabled = preferences[Keys.WORKOUT_ACTIVE_ENABLED] ?: true,
            workoutReminderEnabled = preferences[Keys.WORKOUT_REMINDER_ENABLED] ?: true,
            aiExerciseCreatedEnabled = preferences[Keys.AI_EXERCISE_CREATED_ENABLED] ?: true,
            aiTemplateCreatedEnabled = preferences[Keys.AI_TEMPLATE_CREATED_ENABLED] ?: true,
            weightProgressEnabled = preferences[Keys.WEIGHT_PROGRESS_ENABLED] ?: true,
            weightProgressReminderEnabled = preferences[Keys.WEIGHT_PROGRESS_REMINDER_ENABLED] ?: true,
            workoutReminderDays = preferences[Keys.WORKOUT_REMINDER_DAYS] ?: 4,
            weightProgressReminderDays = preferences[Keys.WEIGHT_PROGRESS_REMINDER_DAYS] ?: 7
        )
    }

    suspend fun setWorkoutActiveEnabled(enabled: Boolean) = saveBoolean(Keys.WORKOUT_ACTIVE_ENABLED, enabled)
    suspend fun setWorkoutReminderEnabled(enabled: Boolean) = saveBoolean(Keys.WORKOUT_REMINDER_ENABLED, enabled)
    suspend fun setAiExerciseCreatedEnabled(enabled: Boolean) = saveBoolean(Keys.AI_EXERCISE_CREATED_ENABLED, enabled)
    suspend fun setAiTemplateCreatedEnabled(enabled: Boolean) = saveBoolean(Keys.AI_TEMPLATE_CREATED_ENABLED, enabled)
    suspend fun setWeightProgressEnabled(enabled: Boolean) = saveBoolean(Keys.WEIGHT_PROGRESS_ENABLED, enabled)
    suspend fun setWeightProgressReminderEnabled(enabled: Boolean) = saveBoolean(Keys.WEIGHT_PROGRESS_REMINDER_ENABLED, enabled)

    suspend fun setWorkoutReminderDays(days: Int) {
        context.dataStore.edit { it[Keys.WORKOUT_REMINDER_DAYS] = days.coerceIn(1, 30) }
    }

    suspend fun setWeightProgressReminderDays(days: Int) {
        context.dataStore.edit { it[Keys.WEIGHT_PROGRESS_REMINDER_DAYS] = days.coerceIn(1, 30) }
    }

    private suspend fun saveBoolean(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, enabled: Boolean) {
        context.dataStore.edit { it[key] = enabled }
    }
}
