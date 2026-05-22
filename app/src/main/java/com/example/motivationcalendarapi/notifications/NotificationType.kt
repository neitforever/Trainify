package com.example.motivationcalendarapi.notifications

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.motivationcalendarapi.R

enum class NotificationType(
    val id: Int,
    val channelId: String,
    @StringRes val titleRes: Int,
    @StringRes val textRes: Int,
    @DrawableRes val iconRes: Int
) {
    WORKOUT_ACTIVE(
        id = 1001,
        channelId = NotificationConstants.CHANNEL_WORKOUT_ACTIVE,
        titleRes = R.string.notification_workout_active_title,
        textRes = R.string.notification_workout_active_text,
        iconRes = R.drawable.ic_dumbbell
    ),
    WORKOUT_REMINDER(
        id = 1002,
        channelId = NotificationConstants.CHANNEL_WORKOUT_REMINDER,
        titleRes = R.string.notification_workout_reminder_title,
        textRes = R.string.notification_workout_reminder_text,
        iconRes = R.drawable.ic_time
    ),
    AI_EXERCISE_CREATED(
        id = 1003,
        channelId = NotificationConstants.CHANNEL_AI_GENERATION,
        titleRes = R.string.notification_ai_exercise_title,
        textRes = R.string.notification_ai_exercise_text,
        iconRes = R.drawable.ic_reward_fg_ai_exercise
    ),
    AI_TEMPLATE_CREATED(
        id = 1004,
        channelId = NotificationConstants.CHANNEL_AI_GENERATION,
        titleRes = R.string.notification_ai_template_title,
        textRes = R.string.notification_ai_template_text,
        iconRes = R.drawable.ic_reward_fg_ai_template
    ),
    AI_EXERCISE_FAILED(
        id = 1007,
        channelId = NotificationConstants.CHANNEL_AI_GENERATION,
        titleRes = R.string.notification_ai_exercise_failed_title,
        textRes = R.string.notification_ai_exercise_failed_text,
        iconRes = R.drawable.ic_reward_fg_ai_exercise
    ),
    AI_TEMPLATE_FAILED(
        id = 1008,
        channelId = NotificationConstants.CHANNEL_AI_GENERATION,
        titleRes = R.string.notification_ai_template_failed_title,
        textRes = R.string.notification_ai_template_failed_text,
        iconRes = R.drawable.ic_reward_fg_ai_template
    ),
    WEIGHT_PROGRESS(
        id = 1005,
        channelId = NotificationConstants.CHANNEL_PROGRESS,
        titleRes = R.string.notification_weight_progress_title,
        textRes = R.string.notification_weight_progress_text,
        iconRes = R.drawable.ic_progress
    ),
    WEIGHT_PROGRESS_REMINDER(
        id = 1006,
        channelId = NotificationConstants.CHANNEL_PROGRESS,
        titleRes = R.string.notification_weight_progress_reminder_title,
        textRes = R.string.notification_weight_progress_reminder_text,
        iconRes = R.drawable.ic_progress
    )
}
