package com.example.motivationcalendarapi.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WorkoutReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val settings = NotificationSettingsDataStore(context).settingsFlow.first()
        if (!settings.workoutReminderEnabled) return Result.success()

        val lastWorkout = WorkoutDatabase.getDatabase(context).workoutDao().getLastWorkout()
        val daysWithoutWorkout = if (lastWorkout == null) {
            settings.workoutReminderDays
        } else {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastWorkout.timestamp).toInt()
        }

        if (daysWithoutWorkout >= settings.workoutReminderDays) {
            NotificationHelper.showWorkoutReminder(context, daysWithoutWorkout)
        }
        return Result.success()
    }
}
