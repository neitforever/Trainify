package com.motivationcalendar.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.utils.DifficultyLevel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import formatTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel, navController: NavController, paddingValues: Dp
) {
    val groupedWorkouts by viewModel.workoutsByYearMonthAndWeek()
        .collectAsState(initial = emptyMap())

    var expandedYears by remember { mutableStateOf(setOf<Int>()) }
    var expandedMonths by remember { mutableStateOf(setOf<Pair<Int, String>>()) }
    var expandedWeeks by remember { mutableStateOf(setOf<Triple<Int, String, Int>>()) }

    LaunchedEffect(groupedWorkouts) {
        if (expandedYears.isEmpty() && groupedWorkouts.isNotEmpty()) {
            val initialYears = groupedWorkouts.keys.toSet()
            expandedYears = initialYears

            val initialMonths = mutableSetOf<Pair<Int, String>>()
            groupedWorkouts.forEach { (year, months) ->
                months.keys.forEach { monthName ->
                    initialMonths.add(year to monthName)
                }
            }
            expandedMonths = initialMonths

            val initialWeeks = mutableSetOf<Triple<Int, String, Int>>()
            groupedWorkouts.forEach { (year, months) ->
                months.forEach { (monthName, weeks) ->
                    weeks.keys.forEach { weekNumber ->
                        initialWeeks.add(Triple(year, monthName, weekNumber))
                    }
                }
            }
            expandedWeeks = initialWeeks
        }
    }

    LazyColumn(
        modifier = Modifier.padding(top = paddingValues),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (groupedWorkouts.isEmpty()) {
            item {
                Text("Empty history", style = MaterialTheme.typography.headlineMedium)
            }
        } else {
            groupedWorkouts.forEach { (year, months) ->
                item {
                    YearHeader(
                        year = year,
                        isExpanded = expandedYears.contains(year),
                        onToggle = {
                            expandedYears = if (expandedYears.contains(year)) {
                                expandedYears - year
                            } else {
                                expandedYears + year
                            }
                        }
                    )
                }

                if (expandedYears.contains(year)) {
                    months.forEach { (monthName, weeks) ->
                        item {
                            MonthHeader(
                                monthName = monthName,
                                isExpanded = expandedMonths.contains(year to monthName),
                                onToggle = {
                                    val key = year to monthName
                                    expandedMonths = if (expandedMonths.contains(key)) {
                                        expandedMonths - key
                                    } else {
                                        expandedMonths + key
                                    }
                                }
                            )
                        }

                        weeks.forEach { (weekNumber, workouts) ->
                            item {
                                WeekHeader(
                                    weekNumber = weekNumber,
                                    isExpanded = expandedWeeks.contains(
                                        Triple(year, monthName, weekNumber)
                                    ),
                                    onToggle = {
                                        val key = Triple(year, monthName, weekNumber)
                                        expandedWeeks = if (expandedWeeks.contains(key)) {
                                            expandedWeeks - key
                                        } else {
                                            expandedWeeks + key
                                        }
                                    }
                                )
                            }

                            if (expandedWeeks.contains(Triple(year, monthName, weekNumber))) {
                                items(workouts) { workout ->
                                    WorkoutItem(
                                        workout = workout,
                                        workoutIndex = workouts.indexOf(workout) + 1,
                                        onClick = {
                                            navController.navigate(Screen.WorkoutDetail.route + "/${workout.id}")
                                        },
                                        onDelete = { viewModel.deleteWorkout(workout) },
                                        viewModel = viewModel
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(
                    modifier = Modifier
                        .absolutePadding(bottom = 200.dp)
                )
            }
        }
    }
}
@Composable
private fun YearHeader(
    year: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (isExpanded) R.drawable.ic_add
                else R.drawable.ic_minus
            ),
            contentDescription = null
        )
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.displaySmall
        )
    }
}

@Composable
private fun MonthHeader(
    monthName: String,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 16.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (isExpanded) R.drawable.ic_minus
                else R.drawable.ic_add
            ),
            contentDescription = null
        )
        Text(
            text = monthName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun WeekHeader(
    weekNumber: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(start = 32.dp)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(
                id = if (isExpanded) R.drawable.ic_minus
                else R.drawable.ic_add
            ),
            contentDescription = null
        )
        Text(
            text = "Week $weekNumber",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun WorkoutItem(
    workout: Workout,
    workoutIndex: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    viewModel: WorkoutViewModel
) {
    val difficulty by remember(workout) {
        derivedStateOf { viewModel.calculateWorkoutDifficulty(workout) }
    }

    ElevatedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.elevatedButtonColors(Color.Transparent)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = when (difficulty) {
                        DifficultyLevel.EASY -> R.drawable.ic_smile_easy
                        DifficultyLevel.NORMAL -> R.drawable.ic_smile_normal
                        DifficultyLevel.HARD -> R.drawable.ic_smile_hard
                    }
                ),
                contentDescription = "Difficulty Level",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.CenterVertically)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = if (workout.name.isBlank()) "Blank" else workout.name,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Duration: ${formatTime(workout.duration)}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}



