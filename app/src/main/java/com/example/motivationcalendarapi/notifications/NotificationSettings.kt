package com.example.motivationcalendarapi.notifications

data class NotificationSettings(
    val workoutActiveEnabled: Boolean = true,
    val workoutReminderEnabled: Boolean = true,
    val aiExerciseCreatedEnabled: Boolean = true,
    val aiTemplateCreatedEnabled: Boolean = true,
    val weightProgressEnabled: Boolean = true,
    val weightProgressReminderEnabled: Boolean = true,
    val workoutReminderDays: Int = 4,
    val weightProgressReminderDays: Int = 7
)
