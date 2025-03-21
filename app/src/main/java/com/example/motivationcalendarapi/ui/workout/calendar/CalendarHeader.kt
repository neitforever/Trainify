package com.example.motivationcalendarapi.ui.workout.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.utils.CalendarState
import java.time.format.DateTimeFormatter

@Composable
fun CalendarHeader(
    calendarState: CalendarState,
    modifier: Modifier = Modifier
) {
    val monthYearFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    val currentMonth = calendarState.currentMonth.value

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { calendarState.prevMonth() }) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = "Previous month"
            )
        }

        Text(
            text = currentMonth.format(monthYearFormatter),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        IconButton(onClick = { calendarState.nextMonth() }) {
            Icon(
                painter = painterResource(R.drawable.ic_play_arrow),
                contentDescription = "Next month"
            )
        }
    }
}
