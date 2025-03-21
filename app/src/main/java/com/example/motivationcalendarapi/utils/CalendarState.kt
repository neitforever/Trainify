package com.example.motivationcalendarapi.utils

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate
import java.time.YearMonth

class CalendarState(initialDate: LocalDate = LocalDate.now()) {
    val currentMonth: MutableState<YearMonth> = mutableStateOf(YearMonth.from(initialDate))

    fun nextMonth() {
        currentMonth.value = currentMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        currentMonth.value = currentMonth.value.minusMonths(1)
    }
}