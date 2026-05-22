package com.example.motivationcalendarapi.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.repositories.NotificationSettingsDataStore
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class WeightProgressReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val context = applicationContext
        val settings = NotificationSettingsDataStore(context).settingsFlow.first()
        if (!settings.weightProgressReminderEnabled) return Result.success()

        val lastProgress = WorkoutDatabase.getDatabase(context).bodyProgressDao().getLastProgress()
        val daysWithoutProgress = if (lastProgress == null) {
            settings.weightProgressReminderDays
        } else {
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastProgress.timestamp).toInt()
        }

        if (daysWithoutProgress >= settings.weightProgressReminderDays) {
            NotificationHelper.showWeightProgressReminder(context, daysWithoutProgress)
        }
        return Result.success()
    }
}
