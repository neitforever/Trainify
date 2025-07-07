package com.example.motivationcalendarapi.utils

import android.content.Context
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun formatTime(context: Context, seconds: Int?): String {
    val safeSeconds = seconds ?: 0
    return String.format(
        context.resources.configuration.locales[0] ?: Locale.getDefault(),
        "%02d:%02d",
        safeSeconds / 60,
        safeSeconds % 60
    )
}

fun formatDate(context: Context, timestamp: Long): String {
    val locale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale)
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .format(formatter)
}

fun getWeekOfMonth(timestamp: Long?): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp ?: System.currentTimeMillis()
    calendar.firstDayOfWeek = Calendar.MONDAY
    return calendar.get(Calendar.WEEK_OF_MONTH).toString()
}