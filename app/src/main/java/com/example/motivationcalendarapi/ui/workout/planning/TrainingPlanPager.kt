package com.example.motivationcalendarapi.ui.workout.planning

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutStatus
import com.example.motivationcalendarapi.model.planning.localizedName
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt

@Composable
fun TrainingPlanPager(
    workouts: List<Workout>,
    plannedWorkouts: List<PlannedWorkout>,
    lang: String,
    onCreateAiPlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val today = remember { LocalDate.now() }
    val weekStart = remember(today) { today.with(java.time.DayOfWeek.MONDAY) }
    val weekEnd = remember(weekStart) { weekStart.plusDays(6) }
    val plannedThisWeek = plannedWorkouts
        .filter { it.localDate() in weekStart..weekEnd }
        .sortedWith(compareBy<PlannedWorkout> { it.date }.thenBy { it.createdAt }.thenBy { it.id })
    val completedThisWeek = plannedThisWeek.count { it.status == PlannedWorkoutStatus.COMPLETED }
    val completionPercent = if (plannedThisWeek.isEmpty()) 0 else ((completedThisWeek.toFloat() / plannedThisWeek.size) * 100).roundToInt()
    val nextWorkout = plannedWorkouts
        .filter { it.status == PlannedWorkoutStatus.PLANNED && !it.localDate().isBefore(today) }
        .minWithOrNull(compareBy<PlannedWorkout> { it.date }.thenBy { it.createdAt }.thenBy { it.id })

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        pageSpacing = 10.dp
    ) { page ->
        when (page) {
            0 -> PlanStatsCard(
                completed = completedThisWeek,
                total = plannedThisWeek.size,
                percent = completionPercent,
                onCreateAiPlan = onCreateAiPlan
            )
            1 -> NextWorkoutCard(nextWorkout = nextWorkout, lang = lang)
            else -> AiRecommendationCard(
                workouts = workouts,
                plannedWorkouts = plannedWorkouts,
                onCreateAiPlan = onCreateAiPlan
            )
        }
    }
}

@Composable
private fun PlanStatsCard(
    completed: Int,
    total: Int,
    percent: Int,
    onCreateAiPlan: () -> Unit
) {
    PlanCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(62.dp)) {
                CircularProgressIndicator(
                    progress = { if (total == 0) 0f else completed.toFloat() / total },
                    modifier = Modifier.size(62.dp),
                    strokeWidth = 6.dp
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.training_plan_week),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.training_plan_completed_format, completed, total),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCreateAiPlan, modifier = Modifier.fillMaxWidth()) {
            Icon(
                painter = painterResource(R.drawable.ic_reward_fg_ai_template),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(R.string.create_ai_training_plan),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun NextWorkoutCard(nextWorkout: PlannedWorkout?, lang: String) {
    val fallbackName = stringResource(R.string.planned_workout)
    val noPlanText = stringResource(R.string.no_planned_workouts)
    val workoutTitle = nextWorkout?.localizedName(lang)?.takeIf { it.isNotBlank() } ?: fallbackName
    PlanCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = R.drawable.ic_time)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = stringResource(R.string.next_planned_workout),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (nextWorkout != null) workoutTitle else noPlanText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (nextWorkout != null) {
                    Text(
                        text = stringResource(R.string.exercises_count, nextWorkout.exercises.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiRecommendationCard(
    workouts: List<Workout>,
    plannedWorkouts: List<PlannedWorkout>,
    onCreateAiPlan: () -> Unit
) {
    val recommendation = when {
        plannedWorkouts.none { it.status == PlannedWorkoutStatus.PLANNED } -> stringResource(R.string.ai_recommendation_no_plan)
        workouts.isEmpty() -> stringResource(R.string.ai_recommendation_start_history)
        else -> stringResource(R.string.ai_recommendation_based_on_history)
    }
    PlanCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconBadge(icon = R.drawable.ic_reward_fg_ai_exercise)
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = stringResource(R.string.ai_recommendation),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onCreateAiPlan, modifier = Modifier.fillMaxWidth()) {
            Text(text = stringResource(R.string.plan_with_ai))
        }
    }
}

@Composable
private fun PlanCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun IconBadge(icon: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
    }
}

private fun PlannedWorkout.localDate(): LocalDate =
    Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate()
