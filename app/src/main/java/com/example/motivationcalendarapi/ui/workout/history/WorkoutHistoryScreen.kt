package com.motivationcalendar.ui

import Screen
import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.ui.workout.history.fragments.EmptyHistoryView
import com.example.motivationcalendarapi.ui.workout.history.fragments.MonthHeader
import com.example.motivationcalendarapi.ui.workout.history.fragments.WeekHeader
import com.example.motivationcalendarapi.ui.workout.history.fragments.WorkoutItem
import com.example.motivationcalendarapi.ui.workout.history.fragments.YearHeader
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    viewModel: WorkoutViewModel,
    navController: NavController,
    paddingValues: Dp
) {
    val groupedWorkouts by viewModel.workoutsByYearMonthAndWeek()
        .collectAsState(initial = emptyMap())

    var expandedYears by remember { mutableStateOf(setOf<Int>()) }
    var expandedMonths by remember { mutableStateOf(setOf<Pair<Int, Month>>()) }
    var expandedWeeks by remember { mutableStateOf(setOf<Triple<Int, Month, Int>>()) }

    val currentYear = LocalDate.now().year
    val currentMonth = LocalDate.now().month
    val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH)

    LaunchedEffect(groupedWorkouts) {
        if (expandedYears.isEmpty() && groupedWorkouts.isNotEmpty()) {
            expandedYears = setOf(currentYear)
            expandedMonths = setOf(currentYear to currentMonth)
            expandedWeeks = setOf(Triple(currentYear, currentMonth, currentWeek))
        }
    }

    if (groupedWorkouts.isEmpty()) {
        EmptyHistoryView()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues)
                .padding(horizontal = 12.dp),
        ) {
            groupedWorkouts.forEach { (year, monthsMap) ->
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

                item {
                    AnimatedVisibility(
                        visible = expandedYears.contains(year),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            monthsMap.forEach { (month, weeksMap) ->
                                val monthName =
                                    month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                                        .replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
                                            else it.toString()
                                        }

                                MonthHeader(
                                    monthName = monthName,
                                    isExpanded = expandedMonths.contains(year to month),
                                    onToggle = {
                                        val key = year to month
                                        expandedMonths = if (expandedMonths.contains(key)) {
                                            expandedMonths - key
                                        } else {
                                            expandedMonths + key
                                        }
                                    }
                                )

                                AnimatedVisibility(
                                    visible = expandedMonths.contains(year to month),
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    Column {
                                        weeksMap.forEach { (weekNumber, workouts) ->
                                            WeekHeader(
                                                weekNumber = weekNumber,
                                                isExpanded = expandedWeeks.contains(
                                                    Triple(year, month, weekNumber)
                                                ),
                                                onToggle = {
                                                    val key = Triple(year, month, weekNumber)
                                                    expandedWeeks =
                                                        if (expandedWeeks.contains(key)) {
                                                            expandedWeeks - key
                                                        } else {
                                                            expandedWeeks + key
                                                        }
                                                }
                                            )

                                            AnimatedVisibility(
                                                visible = expandedWeeks.contains(
                                                    Triple(year, month, weekNumber)
                                                ),
                                                enter = fadeIn() + expandVertically(),
                                                exit = fadeOut() + shrinkVertically()
                                            ) {
                                                Column {
                                                    workouts.forEach { workout ->
                                                        WorkoutItem(
                                                            workout = workout,
                                                            onClick = {
                                                                navController.navigate(Screen.WorkoutDetail.route + "/${workout.id}")
                                                            },
                                                            viewModel = viewModel
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
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
            }
        }
    }
}





