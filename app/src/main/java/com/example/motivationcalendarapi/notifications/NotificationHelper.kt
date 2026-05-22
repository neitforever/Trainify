package com.example.motivationcalendarapi.notifications

import Screen
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.motivationcalendarapi.MainActivity
import com.example.motivationcalendarapi.R
import java.util.Locale

object NotificationHelper {
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channels = listOf(
            NotificationChannel(
                NotificationConstants.CHANNEL_WORKOUT_ACTIVE,
                context.getString(R.string.notification_channel_workout_active),
                NotificationManager.IMPORTANCE_LOW
            ),
            NotificationChannel(
                NotificationConstants.CHANNEL_WORKOUT_REMINDER,
                context.getString(R.string.notification_channel_workout_reminder),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationConstants.CHANNEL_AI_GENERATION,
                context.getString(R.string.notification_channel_ai_generation),
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                NotificationConstants.CHANNEL_PROGRESS,
                context.getString(R.string.notification_channel_progress),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        context.getSystemService(NotificationManager::class.java).createNotificationChannels(channels)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    fun show(
        context: Context,
        type: NotificationType,
        destination: String? = null,
        textOverride: String? = null,
        ongoing: Boolean = false
    ) {
        if (!hasNotificationPermission(context)) return
        val notification = buildNotification(context, type, destination, textOverride, ongoing)
        NotificationManagerCompat.from(context).notify(type.id, notification)
    }

    fun cancel(type: NotificationType, context: Context) {
        NotificationManagerCompat.from(context).cancel(type.id)
    }

    fun buildNotification(
        context: Context,
        type: NotificationType,
        destination: String? = null,
        textOverride: String? = null,
        ongoing: Boolean = false
    ) = NotificationCompat.Builder(context, type.channelId)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(context.getString(type.titleRes))
        .setContentText(textOverride ?: context.getString(type.textRes))
        .setStyle(NotificationCompat.BigTextStyle().bigText(textOverride ?: context.getString(type.textRes)))
        .setColor(ContextCompat.getColor(context, R.color.primary))
        .setContentIntent(createPendingIntent(context, destination))
        .setAutoCancel(!ongoing)
        .setOngoing(ongoing)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()

    fun buildWorkoutActiveNotification(context: Context, elapsedSeconds: Int = 0) = buildNotification(
        context = context,
        type = NotificationType.WORKOUT_ACTIVE,
        destination = Screen.AddWorkout.route,
        textOverride = context.getString(R.string.notification_workout_active_text, formatDuration(elapsedSeconds)),
        ongoing = true
    )

    fun showWorkoutActive(context: Context, elapsedSeconds: Int = 0) {
        if (!hasNotificationPermission(context)) return
        NotificationManagerCompat.from(context).notify(
            NotificationType.WORKOUT_ACTIVE.id,
            buildWorkoutActiveNotification(context, elapsedSeconds)
        )
    }

    fun cancelWorkoutActive(context: Context) = cancel(NotificationType.WORKOUT_ACTIVE, context)

    fun showWorkoutReminder(context: Context, days: Int) = show(
        context = context,
        type = NotificationType.WORKOUT_REMINDER,
        destination = Screen.AddWorkout.route,
        textOverride = context.getString(R.string.notification_workout_reminder_text, days)
    )

    fun showAiExerciseCreated(context: Context, exerciseName: String) = show(
        context = context,
        type = NotificationType.AI_EXERCISE_CREATED,
        destination = Screen.AiExerciseGeneration.route,
        textOverride = context.getString(R.string.notification_ai_exercise_text, exerciseName)
    )

    fun showAiTemplateCreated(context: Context, templateName: String) = show(
        context = context,
        type = NotificationType.AI_TEMPLATE_CREATED,
        destination = Screen.AiTemplateGeneration.route,
        textOverride = context.getString(R.string.notification_ai_template_text, templateName)
    )

    fun showAiExerciseGenerationFailed(context: Context, reason: String) = show(
        context = context,
        type = NotificationType.AI_EXERCISE_FAILED,
        destination = Screen.AiExerciseGeneration.route,
        textOverride = context.getString(R.string.notification_ai_exercise_failed_text, reason)
    )

    fun showAiTemplateGenerationFailed(context: Context, reason: String) = show(
        context = context,
        type = NotificationType.AI_TEMPLATE_FAILED,
        destination = Screen.AiTemplateGeneration.route,
        textOverride = context.getString(R.string.notification_ai_template_failed_text, reason)
    )

    fun showWeightProgress(context: Context, weight: Double) = show(
        context = context,
        type = NotificationType.WEIGHT_PROGRESS,
        destination = Screen.BodyProgress.route,
        textOverride = context.getString(R.string.notification_weight_progress_text, formatWeight(weight))
    )

    fun showWeightProgressReminder(context: Context, days: Int) = show(
        context = context,
        type = NotificationType.WEIGHT_PROGRESS_REMINDER,
        destination = Screen.BodyProgress.route,
        textOverride = context.getString(R.string.notification_weight_progress_reminder_text, days)
    )

    private fun createPendingIntent(context: Context, destination: String?): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            destination?.let { putExtra(NotificationConstants.EXTRA_DESTINATION, it) }
        }
        return PendingIntent.getActivity(
            context,
            destination?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatDuration(seconds: Int): String {
        val safeSeconds = seconds.coerceAtLeast(0)
        return String.format(Locale.getDefault(), "%02d:%02d", safeSeconds / 60, safeSeconds % 60)
    }

    private fun formatWeight(weight: Double): String {
        return String.format(Locale.getDefault(), "%.1f kg", weight)
    }
}
