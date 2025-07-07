package com.example.motivationcalendarapi.ui.workout.fragments

import Screen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.ui.workout.calendar.CalendarHeader
import com.example.motivationcalendarapi.ui.workout.calendar.CustomCalendarView
import com.example.motivationcalendarapi.utils.CalendarState
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InActiveWorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    navController: NavController,
    paddingTop: Dp
){
    @Composable
    fun rememberCalendarState(initialDate: LocalDate = LocalDate.now()) = remember {
        CalendarState(initialDate)
    }
    val workouts by workoutViewModel.allWorkouts.collectAsState()
    val calendarState = rememberCalendarState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingTop + 8.dp)
    ) {
        CalendarHeader(
            calendarState = calendarState,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .padding(horizontal = 4.dp)
        )

        CustomCalendarView(
            workouts = workouts,
            calendarState = calendarState,
            onWorkoutClick = { workoutId ->
                navController.navigate("${Screen.WorkoutDetail.route}/$workoutId")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
}

