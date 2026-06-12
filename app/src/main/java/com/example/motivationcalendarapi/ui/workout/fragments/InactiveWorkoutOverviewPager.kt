package com.example.motivationcalendarapi.ui.workout.fragments

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.localizedName
import com.example.motivationcalendarapi.ui.exercise.detail.SectionIcon
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.utils.formatCompactDecimal
import com.example.motivationcalendarapi.utils.formatDate
import com.example.motivationcalendarapi.utils.formatTime
import com.example.motivationcalendarapi.utils.getStartAndEndOfCurrentWeek
import com.example.motivationcalendarapi.utils.resolvedDifficulty
import kotlinx.coroutines.delay

@Composable
fun InactiveWorkoutOverviewPager(
    workouts: List<Workout>,
    lang: String,
    showWeeklyStartupLoading: Boolean,
    onWeeklyStartupLoadingShown: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val context = LocalContext.current
    val text = remember(lang) { InactiveWorkoutOverviewText(lang) }
    val weekStats = remember(workouts) { workouts.toWeekStats() }
    val lastWorkout = remember(workouts) { workouts.maxByOrNull { it.timestamp } }
    LaunchedEffect(showWeeklyStartupLoading) {
        if (showWeeklyStartupLoading) {
            delay(1_000)
            onWeeklyStartupLoadingShown()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            pageSpacing = 12.dp
        ) { page ->
            when (page) {
                0 -> WeekSummaryCard(
                    stats = weekStats,
                    text = text,
                    context = context,
                    showStartupLoading = showWeeklyStartupLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(256.dp)
                )

                1 -> LastWorkoutCard(
                    workout = lastWorkout,
                    text = text,
                    context = context,
                    lang = lang,
                    onWorkoutClick = onWorkoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(256.dp)
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                val selected = pagerState.currentPage == index
                Surface(
                    modifier = Modifier.size(width = if (selected) 18.dp else 7.dp, height = 7.dp),
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.78f)
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                    },
                    content = {}
                )
            }
        }
    }
}

@Composable
private fun WeekSummaryCard(
    stats: WeekWorkoutStats,
    text: InactiveWorkoutOverviewText,
    context: Context,
    showStartupLoading: Boolean,
    modifier: Modifier = Modifier
) {
    OverviewCard(modifier = modifier) {
        OverviewHeader(
            iconResId = R.drawable.ic_reward_fg_consistency,
            title = text.weekTitle,
            subtitle = when {
                showStartupLoading -> text.weekLoadingBody
                stats.workoutCount == 0 -> text.weekEmptySubtitle
                else -> text.weekSubtitle
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showStartupLoading) {
            Spacer(modifier = Modifier.weight(1f))

            OverviewLoadingState(
                title = text.weekLoadingTitle,
                body = text.weekLoadingBody
            )

            Spacer(modifier = Modifier.weight(1f))
        } else if (stats.workoutCount == 0) {
            Spacer(modifier = Modifier.weight(1f))

            EmptyOverviewPrompt(
                iconResId = R.drawable.ic_dumbbell,
                title = text.weekEmptyTitle,
                body = text.weekEmptyBody,
                showHints = false
            )

            Spacer(modifier = Modifier.weight(1f))
        } else {
            CompactAverageWorkoutBlock(
                title = text.weekAverageTitle,
                value = "${formatTime(context, stats.averageDurationSeconds)} • ${stats.averageSetsPerWorkout} ${text.setUnit}"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewMetricChip(
                    label = text.workouts,
                    value = stats.workoutCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricChip(
                    label = text.exercises,
                    value = stats.exerciseCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewMetricChip(
                    label = text.sets,
                    value = stats.setCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricChip(
                    label = text.time,
                    value = formatTime(context, stats.totalDurationSeconds),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun LastWorkoutCard(
    workout: Workout?,
    text: InactiveWorkoutOverviewText,
    context: Context,
    lang: String,
    onWorkoutClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OverviewCard(
        modifier = modifier.then(
            if (workout != null) Modifier.clickable { onWorkoutClick(workout.id) } else Modifier
        )
    ) {
        OverviewHeader(
            iconResId = R.drawable.ic_history,
            title = text.recentWorkout,
            subtitle = when {
                workout != null -> formatDate(context, workout.timestamp)
                else -> text.lastWorkoutEmptySubtitle
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (workout == null) {
            Spacer(modifier = Modifier.weight(1f))

            EmptyOverviewPrompt(
                iconResId = R.drawable.ic_history,
                title = text.lastWorkoutEmptyTitle,
                body = text.lastWorkoutEmptyBody,
                showHints = false
            )

            Spacer(modifier = Modifier.weight(1f))
        } else {
            val difficulty = workout.resolvedDifficulty()
            val difficultyIcon = when (difficulty) {
                DifficultyLevel.EASY -> R.drawable.ic_smile_easy
                DifficultyLevel.NORMAL -> R.drawable.ic_smile_normal
                DifficultyLevel.HARD -> R.drawable.ic_smile_hard
            }
            val difficultyColor = when (difficulty) {
                DifficultyLevel.EASY -> EASY_COLOR
                DifficultyLevel.NORMAL -> NORMAL_COLOR
                DifficultyLevel.HARD -> HARD_COLOR
            }
            val setCount = workout.completedSetCount()
            val liftedWeight = workout.totalLiftedWeightKg()
            val workoutName = workout.localizedName(lang)
                .ifBlank { text.defaultWorkoutName }
                .replaceFirstChar { it.uppercase() }

            CompactRecentWorkoutTitleBlock(
                workoutName = workoutName,
                workoutDate = formatDate(context, workout.timestamp),
                difficultyIconResId = difficultyIcon,
                difficultyColor = difficultyColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewMetricChip(
                    label = text.exercises,
                    value = workout.exercises.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricChip(
                    label = text.sets,
                    value = setCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OverviewMetricChip(
                    label = text.time,
                    value = formatTime(context, workout.duration),
                    modifier = Modifier.weight(1f)
                )
                OverviewMetricChip(
                    label = text.liftedWeight,
                    value = "${formatCompactDecimal(liftedWeight)} ${text.kg}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactRecentWorkoutTitleBlock(
    workoutName: String,
    workoutDate: String,
    difficultyIconResId: Int,
    difficultyColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(difficultyIconResId),
                        contentDescription = null,
                        tint = difficultyColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = workoutDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
private fun OverviewHeader(
    iconResId: Int,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionIcon(iconResId = iconResId, size = 40, iconSize = 22)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun OverviewMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CompactAverageWorkoutBlock(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.68f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(R.drawable.ic_dumbbell),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = value,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun OverviewLoadingState(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.86f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun EmptyOverviewPrompt(
    iconResId: Int,
    title: String,
    body: String,
    firstHint: String = "",
    secondHint: String = "",
    showHints: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(iconResId),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.82f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
            )

            if (showHints) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OverviewHintChip(
                        text = firstHint,
                        modifier = Modifier.weight(1f)
                    )
                    OverviewHintChip(
                        text = secondHint,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewHintChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp)
        )
    }
}

private data class WeekWorkoutStats(
    val workoutCount: Int,
    val exerciseCount: Int,
    val setCount: Int,
    val totalDurationSeconds: Int,
    val averageDurationSeconds: Int,
    val averageSetsPerWorkout: Int
)

private fun List<Workout>.toWeekStats(): WeekWorkoutStats {
    val (startOfWeek, endOfWeek) = getStartAndEndOfCurrentWeek()
    val weekWorkouts = filter { it.timestamp in startOfWeek..endOfWeek }
    val setCount = weekWorkouts.sumOf { it.completedSetCount() }
    val totalDurationSeconds = weekWorkouts.sumOf { it.duration }
    val workoutCount = weekWorkouts.size
    return WeekWorkoutStats(
        workoutCount = workoutCount,
        exerciseCount = weekWorkouts.sumOf { it.exercises.size },
        setCount = setCount,
        totalDurationSeconds = totalDurationSeconds,
        averageDurationSeconds = if (workoutCount == 0) 0 else totalDurationSeconds / workoutCount,
        averageSetsPerWorkout = if (workoutCount == 0) 0 else setCount / workoutCount
    )
}

private fun Workout.completedSetCount(): Int {
    return exercises.sumOf { extendedExercise ->
        extendedExercise.sets.count { it.status != SetStatus.FAILED }
    }
}

private fun Workout.totalLiftedWeightKg(): Float {
    return exercises.sumOf { extendedExercise ->
        extendedExercise.sets
            .filter { it.status != SetStatus.FAILED }
            .sumOf { (it.weight * it.rep).toDouble() }
    }.toFloat()
}



private data class InactiveWorkoutOverviewText(
    val weekTitle: String,
    val weekSubtitle: String,
    val weekEmptySubtitle: String,
    val weekEmptyTitle: String,
    val weekEmptyBody: String,
    val weekEmptyFirstHint: String,
    val weekEmptySecondHint: String,
    val weekAverageTitle: String,
    val weekAverageEmpty: String,
    val weekLoadingTitle: String,
    val weekLoadingBody: String,
    val workouts: String,
    val exercises: String,
    val sets: String,
    val time: String,
    val recentWorkout: String,
    val lastWorkoutEmptySubtitle: String,
    val lastWorkoutLoadingTitle: String,
    val lastWorkoutLoadingBody: String,
    val lastWorkoutEmptyTitle: String,
    val lastWorkoutEmptyBody: String,
    val lastWorkoutEmptyFirstHint: String,
    val lastWorkoutEmptySecondHint: String,
    val defaultWorkoutName: String,
    val exerciseUnit: String,
    val setUnit: String,
    val liftedWeight: String,
    val kg: String,
    val easy: String,
    val normal: String,
    val hard: String
) {
    constructor(lang: String) : this(
        weekTitle = when (lang) {
            "ru" -> "Итоги недели"
            "be" -> "Вынікі тыдня"
            else -> "Weekly recap"
        },
        weekSubtitle = when (lang) {
            "ru" -> "Коротко о твоей активности за неделю"
            "be" -> "Каротка пра тваю актыўнасць за тыдзень"
            else -> "Your current week activity at a glance"
        },
        weekEmptySubtitle = when (lang) {
            "ru" -> "Неделя начнётся с первой завершённой тренировки"
            "be" -> "Тыдзень пачнецца з першай завершанай трэніроўкі"
            else -> "Your week starts with the first completed workout"
        },
        weekEmptyTitle = when (lang) {
            "ru" -> "Статистика ещё не собрана"
            "be" -> "Статыстыка яшчэ не сабрана"
            else -> "No stats collected yet"
        },
        weekEmptyBody = when (lang) {
            "ru" -> "Заверши тренировку, и здесь появятся итоги недели."
            "be" -> "Завяршы трэніроўку, і тут з'явяцца вынікі тыдня."
            else -> "Finish a workout to fill this weekly recap."
        },
        weekEmptyFirstHint = when (lang) {
            "ru" -> "Тренировки"
            "be" -> "Трэніроўкі"
            else -> "Workouts"
        },
        weekEmptySecondHint = when (lang) {
            "ru" -> "Подходы"
            "be" -> "Падыходы"
            else -> "Sets"
        },
        weekAverageTitle = when (lang) {
            "ru" -> "Средняя тренировка"
            "be" -> "Сярэдняя трэніроўка"
            else -> "Average workout"
        },
        weekAverageEmpty = when (lang) {
            "ru" -> "Появится после первой тренировки недели"
            "be" -> "З'явіцца пасля першай трэніроўкі тыдня"
            else -> "Available after the first workout this week"
        },
        weekLoadingTitle = when (lang) {
            "ru" -> "Проверяю неделю"
            "be" -> "Правяраю тыдзень"
            else -> "Checking this week"
        },
        weekLoadingBody = when (lang) {
            "ru" -> "Ищу завершённые тренировки"
            "be" -> "Шукаю завершаныя трэніроўкі"
            else -> "Looking for completed workouts"
        },
        workouts = when (lang) {
            "ru" -> "Тренировки"
            "be" -> "Трэніроўкі"
            else -> "Workouts"
        },
        exercises = when (lang) {
            "ru" -> "Упражнения"
            "be" -> "Практыкаванні"
            else -> "Exercises"
        },
        sets = when (lang) {
            "ru" -> "Подходы"
            "be" -> "Падыходы"
            else -> "Sets"
        },
        time = when (lang) {
            "ru" -> "Время"
            "be" -> "Час"
            else -> "Time"
        },
        recentWorkout = when (lang) {
            "ru" -> "Недавняя тренировка"
            "be" -> "Нядаўняя трэніроўка"
            else -> "Recent workout"
        },
        lastWorkoutEmptySubtitle = when (lang) {
            "ru" -> "Быстрый возврат появится после первой тренировки"
            "be" -> "Хуткае вяртанне з'явіцца пасля першай трэніроўкі"
            else -> "Quick return appears after the first workout"
        },
        lastWorkoutLoadingTitle = when (lang) {
            "ru" -> "Проверяю историю"
            "be" -> "Правяраю гісторыю"
            else -> "Checking history"
        },
        lastWorkoutLoadingBody = when (lang) {
            "ru" -> "Ищу последнюю тренировку"
            "be" -> "Шукаю апошнюю трэніроўку"
            else -> "Looking for the latest workout"
        },
        lastWorkoutEmptyTitle = when (lang) {
            "ru" -> "История тренировок пуста"
            "be" -> "Гісторыя трэніровак пустая"
            else -> "Workout history is empty"
        },
        lastWorkoutEmptyBody = when (lang) {
            "ru" -> "После завершения тренировки здесь будет карточка для быстрого открытия."
            "be" -> "Пасля завяршэння трэніроўкі тут будзе картка для хуткага адкрыцця."
            else -> "After finishing a workout, this card will open it quickly."
        },
        lastWorkoutEmptyFirstHint = when (lang) {
            "ru" -> "Длительность"
            "be" -> "Працягласць"
            else -> "Duration"
        },
        lastWorkoutEmptySecondHint = when (lang) {
            "ru" -> "Вес"
            "be" -> "Вага"
            else -> "Weight"
        },
        defaultWorkoutName = when (lang) {
            "ru" -> "Тренировка"
            "be" -> "Трэніроўка"
            else -> "Workout"
        },
        exerciseUnit = when (lang) {
            "ru" -> "упр."
            "be" -> "практ."
            else -> "ex."
        },
        setUnit = when (lang) {
            "ru" -> "подх."
            "be" -> "падых."
            else -> "sets"
        },
        liftedWeight = when (lang) {
            "ru" -> "Вес"
            "be" -> "Вага"
            else -> "Weight"
        },
        kg = when (lang) {
            "ru" -> "кг"
            "be" -> "кг"
            else -> "kg"
        },
        easy = when (lang) {
            "ru" -> "Лёгкая"
            "be" -> "Лёгкая"
            else -> "Easy"
        },
        normal = when (lang) {
            "ru" -> "Средняя"
            "be" -> "Сярэдняя"
            else -> "Normal"
        },
        hard = when (lang) {
            "ru" -> "Сложная"
            "be" -> "Складаная"
            else -> "Hard"
        }
    )
}
