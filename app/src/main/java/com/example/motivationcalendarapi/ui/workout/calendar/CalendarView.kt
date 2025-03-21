package com.example.motivationcalendarapi.ui.workout.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.utils.CalendarState
import java.time.Instant
import java.time.ZoneId

@Composable
fun CustomCalendarView(
    workouts: List<Workout>,
    calendarState: CalendarState,
    onWorkoutClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearMonth = calendarState.currentMonth.value
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)

    val workoutsByDate = workouts.associate { workout ->
        Instant.ofEpochMilli(workout.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() to workout
    }

    val days = (1..daysInMonth).map { day ->
        firstDayOfMonth.withDayOfMonth(day)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(days.size) { index ->
            val date = days[index]
            val hasWorkout = workoutsByDate.containsKey(date)

            CalendarDay(
                day = date.dayOfMonth,
                hasWorkout = hasWorkout,
                onClick = {
                    workoutsByDate[date]?.id?.let { workoutId ->
                        onWorkoutClick(workoutId)
                    }
                }
            )
        }
    }
}