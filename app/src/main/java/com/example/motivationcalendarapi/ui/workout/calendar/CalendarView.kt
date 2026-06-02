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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.utils.CalendarState
import com.example.motivationcalendarapi.utils.formatDate
import com.example.motivationcalendarapi.utils.groupByLocalDate
import com.example.motivationcalendarapi.utils.resolvedDifficulty
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CustomCalendarView(
    workouts: List<Workout>,
    calendarState: CalendarState,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = calendarState.currentMonth.value
    var selectedDayWorkouts by remember { mutableStateOf<List<Workout>>(emptyList()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    if (selectedDayWorkouts.size > 1 && selectedDate != null) {
        WorkoutDaySelectionDialog(
            date = selectedDate!!,
            workouts = selectedDayWorkouts,
            onDismiss = {
                selectedDayWorkouts = emptyList()
                selectedDate = null
            },
            onWorkoutClick = { workoutId ->
                selectedDayWorkouts = emptyList()
                selectedDate = null
                onWorkoutClick(workoutId)
            }
        )
    }

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

        val workoutsByDate = remember(workouts) { workouts.groupByLocalDate() }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(days.size, key = { days[it].toString() }) { index ->
                val date = days[index]
                val dayWorkouts = workoutsByDate[date].orEmpty()

                CalendarDay(
                    day = date.dayOfMonth,
                    hasWorkout = dayWorkouts.isNotEmpty(),
                    workoutCount = dayWorkouts.size,
                    onClick = {
                        when (dayWorkouts.size) {
                            0 -> Unit
                            1 -> onWorkoutClick(dayWorkouts.first().id)
                            else -> {
                                selectedDate = date
                                selectedDayWorkouts = dayWorkouts
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDaySelectionDialog(
    date: LocalDate,
    workouts: List<Workout>,
    onDismiss: () -> Unit,
    onWorkoutClick: (String) -> Unit
) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val titleDate = remember(date, locale) {
        date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    val timeFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("HH:mm", locale)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WorkoutDaySheetHeader(
                title = stringResource(R.string.workouts_for_date, formatDate(context, titleDate)),
                subtitle = stringResource(R.string.select_workout_to_view),
                onDismiss = onDismiss
            )

            Spacer(modifier = Modifier.heightIn(min = 10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                items(workouts, key = { it.id }) { workout ->
                    WorkoutSelectionItem(
                        workout = workout,
                        timeFormatter = timeFormatter,
                        onClick = { onWorkoutClick(workout.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.heightIn(min = 18.dp))
        }
    }
}

@Composable
private fun WorkoutDaySheetHeader(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

//        IconButton(
//            onClick = onDismiss
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.ic_close),
//                contentDescription = stringResource(R.string.close),
//                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f),
//                modifier = Modifier.size(22.dp)
//            )
//        }
    }
}

@Composable
private fun WorkoutSelectionItem(
    workout: Workout,
    timeFormatter: DateTimeFormatter,
    onClick: () -> Unit
) {
    val time = remember(workout.timestamp, timeFormatter) {
        Instant.ofEpochMilli(workout.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .format(timeFormatter)
    }
    val difficulty = workout.resolvedDifficulty()
    val difficultyColor = when (difficulty) {
        DifficultyLevel.EASY -> EASY_COLOR
        DifficultyLevel.NORMAL -> NORMAL_COLOR
        DifficultyLevel.HARD -> HARD_COLOR
    }
    val difficultyIcon = when (difficulty) {
        DifficultyLevel.EASY -> R.drawable.ic_smile_easy
        DifficultyLevel.NORMAL -> R.drawable.ic_smile_normal
        DifficultyLevel.HARD -> R.drawable.ic_smile_hard
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(difficultyIcon),
                contentDescription = stringResource(R.string.difficulty_level),
                tint = difficultyColor,
                modifier = Modifier.size(34.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = stringResource(R.string.exercises_count, workout.exercises.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .padding(start = 8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_arrow_right),
                    contentDescription = stringResource(R.string.navigate),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
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
