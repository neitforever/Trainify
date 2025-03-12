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
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import formatTime

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel, navController: NavController, paddingValues: Dp
) {
    val groupedWorkouts by viewModel.workoutsByMonthAndWeek().collectAsState(initial = emptyMap())

    var expandedMonths by remember { mutableStateOf(setOf<String>()) }
    var expandedWeeks by remember { mutableStateOf(setOf<Pair<String, Int>>()) }



        if (groupedWorkouts.isEmpty()) {
            Text("Empty history", style = MaterialTheme.typography.headlineMedium)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.padding(top = paddingValues)
            ) {
                groupedWorkouts.forEach { (month, weeks) ->
                    val isMonthExpanded = expandedMonths.contains(month)
                    item {
                        Text(text = month,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    expandedMonths = if (isMonthExpanded) {
                                        expandedMonths - month
                                    } else {
                                        expandedMonths + month
                                    }
                                }
                                .padding(vertical = 8.dp))
                    }

                    if (isMonthExpanded) {
                        weeks.forEach { (week, workouts) ->
                            val weekKey = month to week
                            val isWeekExpanded = expandedWeeks.contains(weekKey)

                            item {
                                HorizontalDivider(
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(text = "Week $week",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .clickable {
                                            expandedWeeks = if (isWeekExpanded) {
                                                expandedWeeks - weekKey
                                            } else {
                                                expandedWeeks + weekKey
                                            }
                                        })
                                HorizontalDivider(
                                    color = Color.White,
                                    modifier = Modifier.fillMaxWidth()
                                )

                            }

                            if (isWeekExpanded) {
                                items(workouts) { workout ->
                                    WorkoutItem(
                                        workout = workout,
                                        workoutIndex = workouts.indexOf(workout) + 1,
                                        onClick = {
                                            navController.navigate(Screen.WorkoutDetail.route + "/${workout.id}")
                                        },
                                        onDelete = { viewModel.deleteWorkout(workout) },

                                        )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }


@Composable
fun WorkoutItem(
    workout: Workout, workoutIndex: Int, onClick: () -> Unit, onDelete: () -> Unit
) {

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
            Text(
                text = "$workoutIndex.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f).align(Alignment.CenterVertically)) {
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



