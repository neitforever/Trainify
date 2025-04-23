package com.example.motivationcalendarapi.ui.profile.profile_calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.ui.profile.profile_calendar.fragments.ProfileMonthCalendar
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun ProfileCalendarView(
    workouts: List<Workout>,
    workoutViewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    val months = listOf(
        currentDate.minusMonths(2).withDayOfMonth(1),
        currentDate.minusMonths(1).withDayOfMonth(1),
        currentDate.withDayOfMonth(1)
    )

    val workoutDifficulties = remember(workouts) {
        workouts.associate { workout ->
            Instant.ofEpochMilli(workout.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() to workoutViewModel.calculateWorkoutDifficulty(workout)
        }
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Training Activity",
                style = MaterialTheme.typography.headlineSmall,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            months.forEach { monthStart ->
                ProfileMonthCalendar(
                    month = YearMonth.from(monthStart),
                    workoutDifficulties = workoutDifficulties,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
