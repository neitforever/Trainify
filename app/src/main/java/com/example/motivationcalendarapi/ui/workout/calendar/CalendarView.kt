package com.example.motivationcalendarapi.ui.workout.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.utils.CalendarState
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth

@Composable
fun CustomCalendarView(
    workouts: List<Workout>,
    calendarState: CalendarState,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = calendarState.currentMonth.value

    AnimatedContent(
        targetState = currentMonth,
        transitionSpec = { calculateTransitionSpec(initialState, targetState) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp)
            .pointerInput(calendarState) {
                detectSwipeGestures(calendarState)
            }
    ) { targetYearMonth ->
        val daysInMonth = targetYearMonth.lengthOfMonth()
        val firstDayOfMonth = targetYearMonth.atDay(1)
        val days = List(daysInMonth) { dayOffset ->
            firstDayOfMonth.plusDays(dayOffset.toLong())
        }

        val workoutsByDate = workouts.associate { workout ->
            Instant.ofEpochMilli(workout.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() to workout
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),

        ) {
            items(days.size, key = { days[it].toString() }) { index ->
                val date = days[index]
                val hasWorkout = workoutsByDate.containsKey(date)

                CalendarDay(
                    day = date.dayOfMonth,
                    hasWorkout = hasWorkout,
                    onClick = {
                        workoutsByDate[date]?.id?.let(onWorkoutClick)
                    }
                )
            }
        }
    }
}

private fun AnimatedContentTransitionScope<YearMonth>.calculateTransitionSpec(
    initial: YearMonth,
    target: YearMonth
): ContentTransform {
    return if (target.isAfter(initial)) {
        slideInHorizontally(
            animationSpec = tween(300),
            initialOffsetX = { fullWidth -> fullWidth }
        ) + fadeIn() togetherWith
                slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> -fullWidth }
                ) + fadeOut()
    } else {
        slideInHorizontally(
            animationSpec = tween(300),
            initialOffsetX = { fullWidth -> -fullWidth }
        ) + fadeIn() togetherWith
                slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> fullWidth }
                ) + fadeOut()
    }.using(SizeTransform(clip = false))
}

private suspend fun PointerInputScope.detectSwipeGestures(
    calendarState: CalendarState
) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragEnd = {
            val swipeThreshold = 100f
            when {
                totalDrag > swipeThreshold -> calendarState.prevMonth()
                totalDrag < -swipeThreshold -> calendarState.nextMonth()
            }
        },
        onHorizontalDrag = { _, dragAmount ->
            totalDrag += dragAmount
        }
    )
}