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

fun getStartAndEndOfCurrentWeek(): Pair<Long, Long> {
    val calendar = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val startOfWeek = calendar.timeInMillis

    calendar.add(Calendar.DATE, 6)
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)
    val endOfWeek = calendar.timeInMillis

    return startOfWeek to endOfWeek
}

//fun getWeekOfMonth(timestamp: Long?): String {
//    val calendar = Calendar.getInstance()
//    calendar.timeInMillis = timestamp ?: System.currentTimeMillis()
//    calendar.firstDayOfWeek = Calendar.MONDAY
//    return calendar.get(Calendar.WEEK_OF_MONTH).toString()
//}