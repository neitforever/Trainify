package com.example.motivationcalendarapi.ui.workout.planning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.composed
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.utils.resolvedDifficulty
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.localizedName
import com.example.motivationcalendarapi.model.planning.PlannedWorkout
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutSourceType
import com.example.motivationcalendarapi.model.planning.PlannedWorkoutStatus
import com.example.motivationcalendarapi.model.planning.localizedName
import com.example.motivationcalendarapi.utils.formatDate
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private fun Modifier.planningNoRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDayPlanningBottomSheet(
    date: LocalDate,
    completedWorkouts: List<Workout>,
    plannedWorkouts: List<PlannedWorkout>,
    templates: List<Template>,
    lang: String,
    onDismiss: () -> Unit,
    onOpenWorkout: (String) -> Unit,
    onCreateManualPlan: () -> Unit,
    onCreateAiPlanForDay: () -> Unit,
    onCreatePlanFromTemplate: (Template) -> Unit,
    onStartPlannedWorkout: (PlannedWorkout) -> Unit,
    onSkipPlannedWorkout: (String) -> Unit,
    onDeletePlannedWorkout: (String) -> Unit,
    onRestoreSkippedPlannedWorkout: (String) -> Unit,
    onMovePlannedWorkout: (String, LocalDate) -> Unit,
    onEditPlannedWorkout: (PlannedWorkout) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val text = remember(lang) { TrainingDayPlanningText(lang) }
    val titleMillis = remember(date) { date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() }
    val canPlan = !date.isBefore(LocalDate.now())
    val plannedSort = compareBy<PlannedWorkout> { it.date }.thenBy { it.createdAt }.thenBy { it.id }
    val activePlans = plannedWorkouts.filter { it.status == PlannedWorkoutStatus.PLANNED }.sortedWith(plannedSort)
    val skippedPlans = plannedWorkouts.filter { it.status == PlannedWorkoutStatus.SKIPPED }.sortedWith(plannedSort)
    val completedForUi = completedWorkouts.sortedWith(compareBy<Workout> { it.timestamp }.thenBy { it.id })
    val isEmptyDay = activePlans.isEmpty() && skippedPlans.isEmpty() && completedForUi.isEmpty()
    var moveTargetPlan by remember { mutableStateOf<PlannedWorkout?>(null) }

    moveTargetPlan?.let { plan ->
        MovePlannedWorkoutDialog(
            initialMonth = YearMonth.from(date),
            selectedDate = date,
            text = text,
            onDismiss = { moveTargetPlan = null },
            onDateSelected = { newDate ->
                if (newDate != date) {
                    onMovePlannedWorkout(plan.id, newDate)
                    moveTargetPlan = null
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                DayHeaderCard(
                    title = formatDate(context, titleMillis),
                    subtitle = when {
                        isEmptyDay && canPlan -> text.emptyFutureSubtitle
                        isEmptyDay -> text.emptyPastSubtitle
                        activePlans.isNotEmpty() && completedForUi.isNotEmpty() -> text.mixedSubtitle
                        activePlans.isNotEmpty() -> text.plannedSubtitle
                        else -> text.completedSubtitle
                    }
                )
            }

            if (isEmptyDay) {
                item {
                    EmptyDayCard(
                        title = if (canPlan) text.emptyFutureTitle else text.emptyPastTitle,
                        body = if (canPlan) text.emptyFutureBody else text.emptyPastBody,
                        icon = if (canPlan) R.drawable.ic_time else R.drawable.ic_history
                    )
                }
            }

            if (activePlans.isNotEmpty()) {
                item { SectionTitle(text = text.plannedTitle) }
                items(activePlans, key = { it.id }) { planned ->
                    PlannedWorkoutItem(
                        plannedWorkout = planned,
                        lang = lang,
                        text = text,
                        onStart = { onStartPlannedWorkout(planned) },
                        onView = { onEditPlannedWorkout(planned) },
                        onSkip = { onSkipPlannedWorkout(planned.id) },
                        onDelete = { onDeletePlannedWorkout(planned.id) }
                    )
                }
            }

            if (completedForUi.isNotEmpty()) {
                item { SectionTitle(text = text.completedTitle) }
                items(completedForUi, key = { it.id }) { workout ->
                    CompletedWorkoutItem(workout = workout, lang = lang, text = text, onClick = { onOpenWorkout(workout.id) })
                }
            }

            if (skippedPlans.isNotEmpty()) {
                item { SectionTitle(text = text.skippedTitle) }
                items(skippedPlans, key = { it.id }) { planned ->
                    SkippedPlanItem(
                        plannedWorkout = planned,
                        lang = lang,
                        text = text,
                        onView = { onEditPlannedWorkout(planned) },
                        onRestore = { moveTargetPlan = planned },
                        onDelete = { onDeletePlannedWorkout(planned.id) }
                    )
                }
            }

            if (canPlan) {
                item { SectionTitle(text = text.planThisDay) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        PlanningSquareActionButton(
                            title = text.aiShort,
                            body = text.aiPickWorkoutForDay,
                            icon = R.drawable.ic_reward_fg_ai_template,
                            onClick = onCreateAiPlanForDay,
                            modifier = Modifier.weight(1f)
                        )
                        PlanningSquareActionButton(
                            title = text.manualShort,
                            body = text.createManualPlan,
                            icon = R.drawable.ic_add,
                            onClick = onCreateManualPlan,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                if (templates.isNotEmpty()) {
                    item { SectionTitle(text = text.mostUsedTemplates) }
                    items(templates.take(3), key = { it.id }) { template ->
                        TemplatePlanningItem(template = template, lang = lang, text = text, onClick = { onCreatePlanFromTemplate(template) })
                    }
                }
            }

            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}

@Composable
private fun DayHeaderCard(title: String, subtitle: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun EmptyDayCard(title: String, body: String, icon: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun PlanningItemCard(
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}

@Composable
private fun PlanningIconBadge(
    icon: Int,
    tint: androidx.compose.ui.graphics.Color,
    containerColor: androidx.compose.ui.graphics.Color,
    borderColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DeleteCircleButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.size(38.dp).planningNoRippleClickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.72f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun PlannedWorkoutItem(
    plannedWorkout: PlannedWorkout,
    lang: String,
    text: TrainingDayPlanningText,
    onStart: () -> Unit,
    onView: () -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit
) {
    val title = plannedWorkout.localizedName(lang).takeIf { it.isNotBlank() } ?: text.plannedWorkout
    val isAi = plannedWorkout.sourceType == PlannedWorkoutSourceType.AI_GENERATED ||
        plannedWorkout.sourceType == PlannedWorkoutSourceType.AI_RECOMMENDED
    PlanningItemCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlanningIconBadge(
                    icon = if (isAi) R.drawable.ic_reward_fg_ai_template else R.drawable.ic_template,
                    tint = if (isAi) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    containerColor = if (isAi) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.36f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = text.exercises(plannedWorkout.exercises.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DeleteCircleButton(onClick = onDelete)
            }
            plannedWorkout.aiReason?.takeIf { it.isNotBlank() }?.let { reason ->
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactPlanActionButton(
                    title = text.view,
                    icon = R.drawable.ic_list,
                    onClick = onView,
                    modifier = Modifier.weight(1f)
                )
                CompactPlanActionButton(
                    title = text.start,
                    icon = R.drawable.ic_play_arrow,
                    onClick = onStart,
                    modifier = Modifier.weight(1f)
                )
                CompactPlanActionButton(
                    title = text.skip,
                    icon = R.drawable.ic_close,
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    isError = true
                )
            }
    }
}

@Composable
private fun CompletedWorkoutItem(workout: Workout, lang: String, text: TrainingDayPlanningText, onClick: () -> Unit) {
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
    Card(
        modifier = Modifier.fillMaxWidth().planningNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                border = BorderStroke(1.dp, difficultyColor.copy(alpha = 0.26f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(difficultyIcon),
                        contentDescription = null,
                        tint = difficultyColor,
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = workout.localizedName(lang).ifBlank { text.workout },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = text.completedWorkoutMeta(workout.exercises.size, text.difficultyName(difficulty)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun SkippedPlanItem(
    plannedWorkout: PlannedWorkout,
    lang: String,
    text: TrainingDayPlanningText,
    onView: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    PlanningItemCard(borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.16f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlanningIconBadge(
                    icon = R.drawable.ic_close,
                    tint = MaterialTheme.colorScheme.error,
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.34f),
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                )
                Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                    Text(
                        text = plannedWorkout.localizedName(lang).ifBlank { text.skippedPlan },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = text.skippedPlanBody,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                DeleteCircleButton(onClick = onDelete)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                CompactPlanActionButton(text.view, R.drawable.ic_list, onView, Modifier.weight(1f))
                CompactPlanActionButton(text.move, R.drawable.ic_time, onRestore, Modifier.weight(1f))
            }
    }
}

@Composable
private fun CompactPlanActionButton(
    title: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false
) {
    Surface(
        modifier = modifier.height(70.dp).planningNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
        border = BorderStroke(1.dp, if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


@Composable
private fun MovePlannedWorkoutDialog(
    initialMonth: YearMonth,
    selectedDate: LocalDate,
    text: TrainingDayPlanningText,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val anchorMonth = remember(initialMonth) { initialMonth }
    val initialPage = 12_000
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { initialPage * 2 })
    var visibleMonth by remember(initialMonth) { mutableStateOf(initialMonth) }

    LaunchedEffect(pagerState.currentPage) {
        val monthFromPager = anchorMonth.plusMonths((pagerState.currentPage - initialPage).toLong())
        if (visibleMonth != monthFromPager) visibleMonth = monthFromPager
    }

    LaunchedEffect(visibleMonth) {
        val targetPage = initialPage + java.time.temporal.ChronoUnit.MONTHS.between(anchorMonth, visibleMonth).toInt()
        if (pagerState.currentPage != targetPage) pagerState.animateScrollToPage(targetPage)
    }

    val monthTitle = remember(visibleMonth) {
        visibleMonth.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + visibleMonth.year
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = text.movePlanTitle,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp).planningNoRippleClickable { visibleMonth = visibleMonth.minusMonths(1) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_back),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )

                            Surface(
                                modifier = Modifier.size(40.dp).planningNoRippleClickable { visibleMonth = visibleMonth.plusMonths(1) },
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_arrow_forward),
                                        contentDescription = null,
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = true,
                            pageSpacing = 16.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(156.dp)
                        ) { page ->
                            val monthForGrid = anchorMonth.plusMonths((page - initialPage).toLong())
                            MoveWorkoutMonthGrid(
                                month = monthForGrid,
                                selectedDate = selectedDate,
                                onDateSelected = onDateSelected
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = text.cancel,
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun MoveWorkoutMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDay = remember(month) { month.atDay(1) }
    val days = remember(month) {
        List(month.lengthOfMonth()) { index -> firstDay.plusDays(index.toLong()) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        gridItems(days, key = { day: LocalDate -> day.toString() }) { day ->
            MoveWorkoutCalendarDay(
                day = day.dayOfMonth,
                isSelected = day == selectedDate,
                isToday = day == LocalDate.now(),
                onClick = { onDateSelected(day) }
            )
        }
    }
}

@Composable
private fun MoveWorkoutCalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when {
        isSelected -> colorScheme.primaryContainer.copy(alpha = 0.86f)
        isToday -> colorScheme.primary.copy(alpha = 0.10f)
        else -> colorScheme.surface.copy(alpha = 0.76f)
    }
    val borderColor = when {
        isSelected -> colorScheme.primary
        isToday -> colorScheme.primary.copy(alpha = 0.62f)
        else -> colorScheme.outline.copy(alpha = 0.16f)
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .planningNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isSelected || isToday) 1.4.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) colorScheme.primary else colorScheme.onSurface,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TemplatePlanningItem(template: Template, lang: String, text: TrainingDayPlanningText, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().planningNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_template),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Column(modifier = Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    text = template.localizedName(lang),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = text.exercises(template.exercises.size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlanningSquareActionButton(
    title: String,
    body: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.aspectRatio(1.18f).planningNoRippleClickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PlanningIconBadge(
                icon = icon,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = body,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
private fun AiWorkoutPromptCard(
    value: String,
    onValueChange: (String) -> Unit,
    text: TrainingDayPlanningText,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.20f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(text = text.aiPromptTitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text(text = text.aiPromptPlaceholder) }
            )
            FilledTonalButton(onClick = onGenerate, modifier = Modifier.fillMaxWidth()) {
                Text(text = text.generate)
            }
        }
    }
}

private data class TrainingDayPlanningText(val lang: String) {
    val emptyFutureSubtitle = when (lang) { "ru" -> "На этот день ещё ничего не запланировано"; "be" -> "На гэты дзень яшчэ нічога не запланавана"; else -> "Nothing is planned for this day yet" }
    val emptyPastSubtitle = when (lang) { "ru" -> "В этот день тренировок не было"; "be" -> "У гэты дзень трэніровак не было"; else -> "No workouts were completed on this day" }
    val mixedSubtitle = when (lang) { "ru" -> "Есть выполненные и запланированные тренировки"; "be" -> "Ёсць выкананыя і запланаваныя трэніроўкі"; else -> "Completed and planned workouts are available" }
    val plannedSubtitle = when (lang) { "ru" -> "На этот день есть план тренировки"; "be" -> "На гэты дзень ёсць план трэніроўкі"; else -> "This day has a workout plan" }
    val completedSubtitle = when (lang) { "ru" -> "Тренировки, выполненные в этот день"; "be" -> "Трэніроўкі, выкананыя ў гэты дзень"; else -> "Workouts completed on this day" }
    val emptyFutureTitle = when (lang) { "ru" -> "День свободен"; "be" -> "Дзень вольны"; else -> "Free day" }
    val emptyFutureBody = when (lang) { "ru" -> "Можно создать ручной план, выбрать шаблон или попросить AI подобрать тренировку."; "be" -> "Можна стварыць ручны план, выбраць шаблон або папрасіць AI падабраць трэніроўку."; else -> "Create a manual plan, choose a template, or ask AI to pick a workout." }
    val emptyPastTitle = when (lang) { "ru" -> "Тренировок не было"; "be" -> "Трэніровак не было"; else -> "No workouts" }
    val emptyPastBody = when (lang) { "ru" -> "В этот день нет завершённых тренировок и активных планов."; "be" -> "У гэты дзень няма завершаных трэніровак і актыўных планаў."; else -> "This day has no completed workouts or active plans." }
    val plannedTitle = when (lang) { "ru" -> "Запланировано"; "be" -> "Запланавана"; else -> "Planned" }
    val completedTitle = when (lang) { "ru" -> "Выполнено"; "be" -> "Выканана"; else -> "Completed" }
    val skippedTitle = when (lang) { "ru" -> "Пропущено"; "be" -> "Прапушчана"; else -> "Skipped" }
    val planThisDay = when (lang) { "ru" -> "Запланировать на этот день"; "be" -> "Запланаваць на гэты дзень"; else -> "Plan this day" }
    val aiPickWorkoutForDay = when (lang) { "ru" -> "AI подберёт"; "be" -> "AI падбярэ"; else -> "AI pick" }
    val aiShort = when (lang) { "ru" -> "AI"; "be" -> "AI"; else -> "AI" }
    val manualShort = when (lang) { "ru" -> "Вручную"; "be" -> "Уручную"; else -> "Manual" }
    val createManualPlan = when (lang) { "ru" -> "Создать ручной план"; "be" -> "Стварыць ручны план"; else -> "Create manual plan" }
    val chooseTemplate = when (lang) { "ru" -> "Выбрать шаблон"; "be" -> "Выбраць шаблон"; else -> "Choose template" }
    val mostUsedTemplates = when (lang) { "ru" -> "Часто используемые шаблоны"; "be" -> "Часта выкарыстоўваныя шаблоны"; else -> "Most used templates" }
    val plannedWorkout = when (lang) { "ru" -> "Запланированная тренировка"; "be" -> "Запланаваная трэніроўка"; else -> "Planned workout" }
    val workout = when (lang) { "ru" -> "Тренировка"; "be" -> "Трэніроўка"; else -> "Workout" }
    val view = when (lang) { "ru" -> "Смотреть"; "be" -> "Глядзець"; else -> "View" }
    val hide = when (lang) { "ru" -> "Скрыть"; "be" -> "Схаваць"; else -> "Hide" }
    val start = when (lang) { "ru" -> "Начать"; "be" -> "Пачаць"; else -> "Start" }
    val edit = when (lang) { "ru" -> "Редактировать"; "be" -> "Рэдагаваць"; else -> "Edit" }
    val skip = when (lang) { "ru" -> "Пропустить"; "be" -> "Прапусціць"; else -> "Skip" }
    val delete = when (lang) { "ru" -> "Удалить"; "be" -> "Выдаліць"; else -> "Delete" }
    val move = when (lang) { "ru" -> "Перенести"; "be" -> "Перанесці"; else -> "Move" }
    val skippedPlan = when (lang) { "ru" -> "Пропущенная тренировка"; "be" -> "Прапушчаная трэніроўка"; else -> "Skipped workout" }
    val skippedPlanBody = when (lang) { "ru" -> "План пропущен"; "be" -> "План прапушчаны"; else -> "Skipped plan" }
    val movePlanTitle = when (lang) { "ru" -> "Перенести тренировку"; "be" -> "Перанесці трэніроўку"; else -> "Move workout" }
    val cancel = when (lang) { "ru" -> "Отмена"; "be" -> "Адмена"; else -> "Cancel" }
    val aiPromptTitle = when (lang) { "ru" -> "Что нужно от тренировки?"; "be" -> "Што трэба ад трэніроўкі?"; else -> "What do you want from this workout?" }
    val aiPromptPlaceholder = when (lang) { "ru" -> "Например: лёгкая тренировка на верх тела, 45 минут, без ног"; "be" -> "Напрыклад: лёгкая трэніроўка на верх цела, 45 хвілін, без ног"; else -> "Example: light upper-body workout, 45 minutes, no legs" }
    val generate = when (lang) { "ru" -> "Сгенерировать"; "be" -> "Згенераваць"; else -> "Generate" }
    fun exercises(count: Int) = when (lang) { "ru" -> "$count упр."; "be" -> "$count практ."; else -> "$count exercises" }
    fun difficultyName(difficulty: DifficultyLevel) = when (difficulty) {
        DifficultyLevel.EASY -> when (lang) { "ru" -> "лёгкая"; "be" -> "лёгкая"; else -> "easy" }
        DifficultyLevel.NORMAL -> when (lang) { "ru" -> "средняя"; "be" -> "сярэдняя"; else -> "normal" }
        DifficultyLevel.HARD -> when (lang) { "ru" -> "сложная"; "be" -> "складаная"; else -> "hard" }
    }
    fun completedWorkoutMeta(count: Int, difficulty: String) = when (lang) {
        "ru" -> "$count упр. • $difficulty"
        "be" -> "$count практ. • $difficulty"
        else -> "$count exercises • $difficulty"
    }
}
