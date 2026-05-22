package com.example.motivationcalendarapi.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object NotificationScheduler {
    fun scheduleWorkoutReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationConstants.WORKOUT_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelWorkoutReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationConstants.WORKOUT_REMINDER_WORK_NAME)
    }

    fun scheduleWeightProgressReminder(context: Context) {
        val request = PeriodicWorkRequestBuilder<WeightProgressReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            NotificationConstants.WEIGHT_PROGRESS_REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancelWeightProgressReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(NotificationConstants.WEIGHT_PROGRESS_REMINDER_WORK_NAME)
    }
}
