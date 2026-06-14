package com.example.motivationcalendarapi.ui.template

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.localizedName
import com.example.motivationcalendarapi.utils.formatCompactDecimal
import com.example.motivationcalendarapi.utils.formatExerciseMinutes
import com.example.motivationcalendarapi.ui.dialogs.FloatMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.TimeMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.WeightDialog
import com.motivationcalendar.ui.RepsDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class TemplateWorkoutDiff(
    val matched: List<Pair<ExtendedExercise, ExtendedExercise>>,
    val onlyTemplate: List<ExtendedExercise>,
    val onlyWorkout: List<ExtendedExercise>
)

private data class TemplateUpdateOptions(
    val updateSetsCount: Boolean = true,
    val updateReps: Boolean = true,
    val updateWeight: Boolean = true,
    val updateTime: Boolean = true,
    val updateResistance: Boolean = true,
    val updateIncline: Boolean = true
)

private enum class PreviewSection { Summary, Updated, Added, Unchanged }

private data class SetEditTarget(
    val exerciseId: String,
    val setIndex: Int,
    val field: SetEditField,
    val currentValue: String,
    val title: String
)

private enum class SetEditField { Reps, Weight, Time, Resistance, Incline }

@Composable
fun TemplateUpdateFromWorkoutSheet(
    template: Template,
    workouts: List<Workout>,
    lang: String,
    minRep: Int,
    maxRep: Int,
    stepRep: Int,
    minWeight: Float,
    maxWeight: Float,
    stepWeight: Float,
    minCardioTime: Float,
    maxCardioTime: Float,
    stepCardioTime: Float,
    minResistance: Float,
    maxResistance: Float,
    stepResistance: Float,
    minIncline: Float,
    maxIncline: Float,
    stepIncline: Float,
    onDismiss: () -> Unit,
    onApply: (List<ExtendedExercise>) -> Unit
) {
    var selectedWorkout by remember { mutableStateOf<Workout?>(null) }
    var updateOptions by remember { mutableStateOf(TemplateUpdateOptions()) }
    var openSection by remember { mutableStateOf(PreviewSection.Summary) }
    val selectedNewExerciseIds = remember { mutableStateMapOf<String, Boolean>() }
    val removedTemplateExerciseIds = remember { mutableStateMapOf<String, Boolean>() }
    val updatedExerciseDrafts = remember { mutableStateMapOf<String, ExtendedExercise>() }
    val addedExerciseDrafts = remember { mutableStateMapOf<String, ExtendedExercise>() }
    val unchangedExerciseDrafts = remember { mutableStateMapOf<String, ExtendedExercise>() }
    var setEditTarget by remember { mutableStateOf<SetEditTarget?>(null) }

    fun goBackOrClose() {
        if (selectedWorkout != null) selectedWorkout = null else onDismiss()
    }

    BackHandler(enabled = true) { goBackOrClose() }

    Dialog(
        onDismissRequest = { goBackOrClose() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxSize()
        ) {
            val sourceWorkout = selectedWorkout
            if (sourceWorkout == null) {
                TemplateUpdateWorkoutPicker(
                    template = template,
                    workouts = workouts,
                    lang = lang,
                    onWorkoutSelected = { workout ->
                        selectedWorkout = workout
                        openSection = PreviewSection.Summary
                        val diff = buildTemplateWorkoutDiff(template, workout)
                        selectedNewExerciseIds.clear()
                        removedTemplateExerciseIds.clear()
                        updatedExerciseDrafts.clear()
                        addedExerciseDrafts.clear()
                        unchangedExerciseDrafts.clear()
                        diff.onlyTemplate.forEach { exercise ->
                            unchangedExerciseDrafts[exercise.exercise.id] = exercise.copy(sets = exercise.sets.map { set -> set.copy(status = SetStatus.NONE) })
                        }
                        diff.onlyWorkout.forEach { exercise ->
                            selectedNewExerciseIds[exercise.exercise.id] = false
                            addedExerciseDrafts[exercise.exercise.id] = normalizeSource(exercise)
                        }
                        diff.matched.forEach { pair ->
                            updatedExerciseDrafts[pair.first.exercise.id] = normalizeSource(pair.second)
                        }
                    },
                    onClose = onDismiss
                )
            } else {
                val diff = remember(template, sourceWorkout) { buildTemplateWorkoutDiff(template, sourceWorkout) }
                LaunchedEffect(sourceWorkout.id) {
                    diff.onlyWorkout.forEach { exercise ->
                        if (!addedExerciseDrafts.containsKey(exercise.exercise.id)) {
                            addedExerciseDrafts[exercise.exercise.id] = normalizeSource(exercise)
                        }
                    }
                    diff.matched.forEach { pair ->
                        if (!updatedExerciseDrafts.containsKey(pair.first.exercise.id)) {
                            updatedExerciseDrafts[pair.first.exercise.id] = normalizeSource(pair.second)
                        }
                    }
                    diff.onlyTemplate.forEach { exercise ->
                        if (!unchangedExerciseDrafts.containsKey(exercise.exercise.id)) {
                            unchangedExerciseDrafts[exercise.exercise.id] = exercise.copy(sets = exercise.sets.map { set -> set.copy(status = SetStatus.NONE) })
                        }
                    }
                }
                TemplateUpdatePreview(
                    workout = sourceWorkout,
                    diff = diff,
                    lang = lang,
                    selectedNewExerciseIds = selectedNewExerciseIds,
                    removedTemplateExerciseIds = removedTemplateExerciseIds,
                    updatedExerciseDrafts = updatedExerciseDrafts,
                    addedExerciseDrafts = addedExerciseDrafts,
                    unchangedExerciseDrafts = unchangedExerciseDrafts,
                    updateOptions = updateOptions,
                    openSection = openSection,
                    onOpenSectionChange = { openSection = it },
                    onOptionsChange = { updateOptions = it },
                    onBack = { selectedWorkout = null },
                    onApply = {
                        val updatedExercises = buildUpdatedTemplateExercises(
                            templateExercises = template.exercises,
                            workoutExercises = sourceWorkout.exercises,
                            selectedNewExerciseIds = selectedNewExerciseIds.filterValues { it }.keys,
                            removedTemplateExerciseIds = removedTemplateExerciseIds.filterValues { it }.keys,
                            updatedExerciseDrafts = updatedExerciseDrafts,
                            addedExerciseDrafts = addedExerciseDrafts,
                            unchangedExerciseDrafts = unchangedExerciseDrafts,
                            options = updateOptions
                        )
                        onApply(updatedExercises)
                        onDismiss()
                    },
                    onEditSetRequested = { exerciseId, setIndex, field, currentValue, label ->
                        setEditTarget = SetEditTarget(exerciseId, setIndex, field, currentValue, label)
                    }
                )
                fun saveEditedSetValue(target: SetEditTarget, value: String) {
                    val map = when {
                        addedExerciseDrafts.containsKey(target.exerciseId) -> addedExerciseDrafts
                        updatedExerciseDrafts.containsKey(target.exerciseId) -> updatedExerciseDrafts
                        else -> unchangedExerciseDrafts
                    }
                    val exercise = map[target.exerciseId]
                    if (exercise != null) {
                        map[target.exerciseId] = exercise.copy(
                            sets = updateSetValue(exercise.sets, target.setIndex, target.field, value)
                        )
                    }
                    setEditTarget = null
                }

                setEditTarget?.let { target ->
                    val currentSet = when {
                        addedExerciseDrafts.containsKey(target.exerciseId) -> addedExerciseDrafts[target.exerciseId]
                        updatedExerciseDrafts.containsKey(target.exerciseId) -> updatedExerciseDrafts[target.exerciseId]
                        else -> unchangedExerciseDrafts[target.exerciseId]
                    }?.sets?.getOrNull(target.setIndex)

                    when (target.field) {
                        SetEditField.Reps -> RepsDialog(
                            showDialog = true,
                            initialRep = currentSet?.rep ?: minRep,
                            minRep = minRep,
                            maxRep = maxRep,
                            stepRep = stepRep,
                            onDismiss = { setEditTarget = null },
                            onSave = { saveEditedSetValue(target, it.toString()) }
                        )
                        SetEditField.Weight -> WeightDialog(
                            showDialog = true,
                            initialWeight = currentSet?.weight ?: minWeight,
                            minWeight = minWeight,
                            maxWeight = maxWeight,
                            stepWeight = stepWeight,
                            onDismiss = { setEditTarget = null },
                            onSave = { saveEditedSetValue(target, it.toString()) }
                        )
                        SetEditField.Time -> TimeMetricDialog(
                            showDialog = true,
                            title = stringResource(R.string.edit_time),
                            initialValueMinutes = currentSet?.time ?: minCardioTime,
                            minValueMinutes = minCardioTime,
                            maxValueMinutes = maxCardioTime,
                            stepValueMinutes = stepCardioTime,
                            onDismiss = { setEditTarget = null },
                            onSave = { saveEditedSetValue(target, it.toString()) }
                        )
                        SetEditField.Resistance -> FloatMetricDialog(
                            showDialog = true,
                            title = stringResource(R.string.edit_resistance),
                            label = stringResource(R.string.resistance_level),
                            initialValue = currentSet?.resistance ?: minResistance,
                            minValue = minResistance,
                            maxValue = maxResistance,
                            stepValue = stepResistance,
                            onDismiss = { setEditTarget = null },
                            onSave = { saveEditedSetValue(target, it.toString()) }
                        )
                        SetEditField.Incline -> FloatMetricDialog(
                            showDialog = true,
                            title = stringResource(R.string.edit_incline),
                            label = stringResource(R.string.incline_percent),
                            initialValue = currentSet?.incline ?: minIncline,
                            minValue = minIncline,
                            maxValue = maxIncline,
                            stepValue = stepIncline,
                            onDismiss = { setEditTarget = null },
                            onSave = { saveEditedSetValue(target, it.toString()) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateUpdateWorkoutPicker(
    template: Template,
    workouts: List<Workout>,
    lang: String,
    onWorkoutSelected: (Workout) -> Unit,
    onClose: () -> Unit
) {
    val sortedWorkouts = remember(workouts, template) {
        workouts.filter { it.exercises.isNotEmpty() }
            .sortedWith(compareByDescending<Workout> { countMatchedExercises(template.exercises, it.exercises) }.thenByDescending { it.timestamp })
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
                SheetHeader(
                    iconResId = R.drawable.ic_progress,
                    title = stringResource(R.string.template_update_from_workout_title),
                    subtitle = stringResource(R.string.template_update_from_workout_subtitle),
                    navigationIconResId = R.drawable.ic_close,
                    navigationContentDescription = stringResource(R.string.close),
                    onNavigationClick = onClose
                )
            }
            if (sortedWorkouts.isEmpty()) {
                item { EmptyUpdateState() }
            } else {
                item {
                    InfoCard(
                        iconResId = R.drawable.ic_info,
                        title = stringResource(R.string.template_update_choose_title),
                        text = stringResource(R.string.template_update_choose_hint)
                    )
                }
                item {
                    ChooseOverviewCard(
                        workoutsCount = sortedWorkouts.size,
                        templateExerciseCount = template.exercises.size,
                        bestMatchCount = sortedWorkouts.maxOfOrNull { countMatchedExercises(template.exercises, it.exercises) } ?: 0,
                        bestNewCount = sortedWorkouts.minOfOrNull { countNewWorkoutExercises(template.exercises, it.exercises) } ?: 0
                    )
                }
                items(sortedWorkouts, key = { it.id }) { workout ->
                    WorkoutSourceCard(
                        workout = workout,
                        lang = lang,
                        matchedCount = countMatchedExercises(template.exercises, workout.exercises),
                        templateExerciseCount = template.exercises.size,
                        newExerciseCount = countNewWorkoutExercises(template.exercises, workout.exercises),
                        onClick = { onWorkoutSelected(workout) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
        StickyFooterCard(
            description = stringResource(R.string.template_update_step_choose_workout),
            activeStep = 0,
            primaryText = if (sortedWorkouts.isEmpty()) stringResource(R.string.close) else null,
            onPrimaryClick = if (sortedWorkouts.isEmpty()) onClose else null
        )
    }
}

@Composable
private fun TemplateUpdatePreview(
    workout: Workout,
    diff: TemplateWorkoutDiff,
    lang: String,
    selectedNewExerciseIds: MutableMap<String, Boolean>,
    removedTemplateExerciseIds: MutableMap<String, Boolean>,
    updatedExerciseDrafts: MutableMap<String, ExtendedExercise>,
    addedExerciseDrafts: MutableMap<String, ExtendedExercise>,
    unchangedExerciseDrafts: MutableMap<String, ExtendedExercise>,
    updateOptions: TemplateUpdateOptions,
    openSection: PreviewSection,
    onOpenSectionChange: (PreviewSection) -> Unit,
    onOptionsChange: (TemplateUpdateOptions) -> Unit,
    onBack: () -> Unit,
    onApply: () -> Unit,
    onEditSetRequested: (String, Int, SetEditField, String, String) -> Unit
) {
    val willAddCount = selectedNewExerciseIds.count { it.value }
    val removedCount = removedTemplateExerciseIds.count { it.value }
    val canApply = diff.matched.isNotEmpty() || willAddCount > 0 || removedCount > 0 || unchangedExerciseDrafts.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
            item { Spacer(modifier = Modifier.height(32.dp)) }
            item {
                SheetHeader(
                    iconResId = R.drawable.ic_template,
                    title = stringResource(R.string.template_update_preview_title),
                    subtitle = stringResource(
                        R.string.template_update_preview_subtitle,
                        workout.localizedName(lang).ifBlank { stringResource(R.string.workout) }.replaceFirstChar { it.uppercase() },
                        formatWorkoutDate(workout.timestamp)
                    ),
                    navigationIconResId = R.drawable.ic_arrow_back,
                    navigationContentDescription = stringResource(R.string.back),
                    onNavigationClick = onBack
                )
            }
            item {
                SectionNavigatorCard(
                    openSection = openSection,
                    onOpenSectionChange = onOpenSectionChange
                )
            }
            if (openSection == PreviewSection.Summary) {
                item { SummaryCard(updated = diff.matched.size, added = willAddCount, unchanged = diff.onlyTemplate.size - removedCount) }
                item {
                    InfoCard(
                        iconResId = R.drawable.ic_info,
                        title = stringResource(R.string.template_update_rule_title),
                        text = stringResource(R.string.template_update_rule_text)
                    )
                }
                item {
                    InfoCard(
                        iconResId = R.drawable.ic_progress,
                        title = stringResource(R.string.template_update_choose_next_title),
                        text = stringResource(R.string.template_update_choose_next_text)
                    )
                }
            }
            if (openSection == PreviewSection.Updated) {
                item {
                    SectionGuidanceCard(
                        iconResId = R.drawable.ic_progress,
                        title = stringResource(R.string.template_update_section_updated),
                        text = stringResource(R.string.template_update_section_updated_hint)
                    )
                }
                if (diff.matched.isEmpty()) {
                    item { EmptyPreviewSectionCard() }
                }
                items(diff.matched, key = { it.first.exercise.id }) { pair ->
                    val id = pair.first.exercise.id
                    val draft = updatedExerciseDrafts[id] ?: normalizeSource(pair.second)
                    ExercisePreviewCard(
                        index = diff.matched.indexOf(pair),
                        exercise = draft,
                        lang = lang,
                        title = pair.first.exercise.getName(lang).ifBlank { stringResource(R.string.exercise) },
                        beforeSets = pair.first.sets,
                        afterSets = draft.sets,
                        options = updateOptions,
                        state = ExercisePreviewState.Updated,
                        checked = true,
                        onCheckedChange = {},
                        onSetDraftChange = { updatedExerciseDrafts[id] = draft.copy(sets = it) },
                        onKeepCurrent = { updatedExerciseDrafts[id] = pair.first.copy(sets = pair.first.sets.map { set -> set.copy(status = SetStatus.NONE) }) },
                        onUseSource = { updatedExerciseDrafts[id] = normalizeSource(pair.second) },
                        onRemoveFromTemplate = { removedTemplateExerciseIds[id] = true },
                        onRestoreInTemplate = { removedTemplateExerciseIds[id] = false },
                        isRemoved = removedTemplateExerciseIds[id] == true,
                        onEditSet = { setIndex, field, currentValue, label -> onEditSetRequested(id, setIndex, field, currentValue, label) }
                    )
                }
            }
            if (openSection == PreviewSection.Added) {
                item {
                    SectionGuidanceCard(
                        iconResId = R.drawable.ic_add,
                        title = stringResource(R.string.template_update_section_new),
                        text = stringResource(R.string.template_update_section_new_hint)
                    )
                }
                if (diff.onlyWorkout.isEmpty()) {
                    item { EmptyPreviewSectionCard() }
                }
                items(diff.onlyWorkout, key = { it.exercise.id }) { exercise ->
                    val id = exercise.exercise.id
                    val checked = selectedNewExerciseIds[id] == true
                    val draft = addedExerciseDrafts[id] ?: normalizeSource(exercise)
                    ExercisePreviewCard(
                        index = diff.onlyWorkout.indexOf(exercise),
                        exercise = draft,
                        lang = lang,
                        title = exercise.exercise.getName(lang).ifBlank { stringResource(R.string.exercise) },
                        beforeSets = null,
                        afterSets = draft.sets,
                        options = updateOptions,
                        state = ExercisePreviewState.Added,
                        checked = checked,
                        onCheckedChange = { selectedNewExerciseIds[id] = it },
                        onSetDraftChange = { addedExerciseDrafts[id] = draft.copy(sets = it) },
                        onKeepCurrent = {},
                        onUseSource = { addedExerciseDrafts[id] = normalizeSource(exercise) },
                        onRemoveFromTemplate = {},
                        onRestoreInTemplate = {},
                        isRemoved = false,
                        onEditSet = { setIndex, field, currentValue, label -> onEditSetRequested(id, setIndex, field, currentValue, label) }
                    )
                }
            }
            if (openSection == PreviewSection.Unchanged) {
                item {
                    SectionGuidanceCard(
                        iconResId = R.drawable.ic_complete,
                        title = stringResource(R.string.template_update_section_unchanged),
                        text = stringResource(R.string.template_update_section_unchanged_hint)
                    )
                }
                if (diff.onlyTemplate.isEmpty()) {
                    item { EmptyPreviewSectionCard() }
                }
                items(diff.onlyTemplate, key = { it.exercise.id }) { exercise ->
                    val id = exercise.exercise.id
                    val draft = unchangedExerciseDrafts[id] ?: exercise.copy(sets = exercise.sets.map { set -> set.copy(status = SetStatus.NONE) })
                    ExercisePreviewCard(
                        index = diff.onlyTemplate.indexOf(exercise),
                        exercise = draft,
                        lang = lang,
                        title = exercise.exercise.getName(lang).ifBlank { stringResource(R.string.exercise) },
                        beforeSets = null,
                        afterSets = draft.sets,
                        options = updateOptions,
                        state = ExercisePreviewState.Unchanged,
                        checked = false,
                        onCheckedChange = {},
                        onSetDraftChange = { unchangedExerciseDrafts[id] = draft.copy(sets = it) },
                        onKeepCurrent = { unchangedExerciseDrafts[id] = exercise.copy(sets = exercise.sets.map { set -> set.copy(status = SetStatus.NONE) }) },
                        onUseSource = { unchangedExerciseDrafts[id] = exercise.copy(sets = exercise.sets.map { set -> set.copy(status = SetStatus.NONE) }) },
                        onRemoveFromTemplate = { removedTemplateExerciseIds[id] = true },
                        onRestoreInTemplate = { removedTemplateExerciseIds[id] = false },
                        isRemoved = removedTemplateExerciseIds[id] == true,
                        onEditSet = { setIndex, field, currentValue, label -> onEditSetRequested(id, setIndex, field, currentValue, label) }
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(148.dp)) }
        }
            StickyFooterCard(
                description = stringResource(R.string.template_update_step_preview),
                activeStep = 1
            )
        }
        TemplateUpdateApplyFab(
            enabled = canApply,
            onClick = onApply,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 16.dp, bottom = 80.dp)
        )
    }
}


@Composable
private fun TemplateUpdateApplyFab(enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 6.dp,
        shadowElevation = 6.dp,
        modifier = modifier
            .size(56.dp)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(R.drawable.ic_complete),
                contentDescription = stringResource(R.string.template_update_apply),
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SheetHeader(
    iconResId: Int,
    title: String,
    subtitle: String,
    navigationIconResId: Int,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 42.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallIconBox(iconResId = iconResId)
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        IconButton(onClick = onNavigationClick, modifier = Modifier.align(Alignment.TopEnd)) {
            Icon(
                painter = painterResource(navigationIconResId),
                contentDescription = navigationContentDescription,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyUpdateState() {
    InfoCard(
        iconResId = R.drawable.ic_minus,
        title = stringResource(R.string.template_update_no_workouts_title),
        text = stringResource(R.string.template_update_no_workouts_text)
    )
}

@Composable
private fun ChooseOverviewCard(workoutsCount: Int, templateExerciseCount: Int, bestMatchCount: Int, bestNewCount: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallIconBox(iconResId = R.drawable.ic_template)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.template_update_choose_overview_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.template_update_choose_overview_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetaCell(stringResource(R.string.template_update_choose_available), workoutsCount.toString(), Modifier.weight(1f))
                MiniMetaCell(stringResource(R.string.template_update_choose_template), templateExerciseCount.toString(), Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMetaCell(stringResource(R.string.template_update_choose_best_match), bestMatchCount.toString(), Modifier.weight(1f))
                MiniMetaCell(stringResource(R.string.template_update_choose_min_new), bestNewCount.toString(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WorkoutSourceCard(workout: Workout, lang: String, matchedCount: Int, templateExerciseCount: Int, newExerciseCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallIconBox(iconResId = R.drawable.ic_history)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.localizedName(lang).ifBlank { stringResource(R.string.workout) }.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Clip
                    )
                    Text(
                        text = stringResource(R.string.template_update_workout_card_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            WorkoutMetaGrid(
                date = formatWorkoutDate(workout.timestamp),
                exercises = workout.exercises.size,
                matchedCount = matchedCount,
                templateExerciseCount = templateExerciseCount,
                newExerciseCount = newExerciseCount
            )
        }
    }
}

@Composable
private fun WorkoutMetaGrid(date: String, exercises: Int, matchedCount: Int, templateExerciseCount: Int, newExerciseCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetaCell(stringResource(R.string.template_update_meta_date), date, Modifier.weight(1f))
            MiniMetaCell(stringResource(R.string.template_update_meta_exercises), exercises.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            MiniMetaCell(
                label = stringResource(R.string.template_update_meta_matches),
                value = stringResource(R.string.template_update_matches_value, matchedCount, templateExerciseCount),
                modifier = Modifier.weight(1f)
            )
            MiniMetaCell(
                label = stringResource(R.string.template_update_meta_new),
                value = newExerciseCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniMetaCell(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Clip)
    }
}

@Composable
private fun InfoCard(iconResId: Int, title: String, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            SmallIconBox(iconResId = iconResId)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

@Composable
private fun EmptyPreviewSectionCard() {
    InfoCard(
        iconResId = R.drawable.ic_minus,
        title = stringResource(R.string.template_update_empty_section_title),
        text = stringResource(R.string.template_update_empty_section_text)
    )
}

@Composable
private fun SmallIconBox(iconResId: Int) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painter = painterResource(iconResId), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun SectionNavigatorCard(openSection: PreviewSection, onOpenSectionChange: (PreviewSection) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = stringResource(R.string.template_update_navigation_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SectionChip(stringResource(R.string.template_update_section_summary), openSection == PreviewSection.Summary, Modifier.weight(1f)) { onOpenSectionChange(PreviewSection.Summary) }
                SectionChip(stringResource(R.string.template_update_summary_updated), openSection == PreviewSection.Updated, Modifier.weight(1f)) { onOpenSectionChange(PreviewSection.Updated) }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SectionChip(stringResource(R.string.template_update_summary_added), openSection == PreviewSection.Added, Modifier.weight(1f)) { onOpenSectionChange(PreviewSection.Added) }
                SectionChip(stringResource(R.string.template_update_summary_unchanged), openSection == PreviewSection.Unchanged, Modifier.weight(1f)) { onOpenSectionChange(PreviewSection.Unchanged) }
            }
        }
    }
}

@Composable
private fun SectionChip(text: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.background.copy(alpha = 0.55f))
            .border(1.dp, if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Clip)
    }
}

@Composable
private fun SummaryCard(updated: Int, added: Int, unchanged: Int) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryItem(stringResource(R.string.template_update_summary_updated), updated.toString())
            SummaryItem(stringResource(R.string.template_update_summary_added), added.toString())
            SummaryItem(stringResource(R.string.template_update_summary_unchanged), unchanged.toString())
        }
    }
}

@Composable
private fun RowScope.SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 2)
    }
}

@Composable
private fun UpdateOptionsCard(options: TemplateUpdateOptions, onOptionsChange: (TemplateUpdateOptions) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SmallIconBox(iconResId = R.drawable.ic_settings)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = stringResource(R.string.template_update_parameters_title), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(text = stringResource(R.string.template_update_parameters_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OptionGridRow(
                first = { OptionChip(stringResource(R.string.template_update_option_sets_count), options.updateSetsCount, Modifier.weight(1f)) { onOptionsChange(options.copy(updateSetsCount = it)) } },
                second = { OptionChip(stringResource(R.string.rep), options.updateReps, Modifier.weight(1f)) { onOptionsChange(options.copy(updateReps = it)) } }
            )
            OptionGridRow(
                first = { OptionChip(stringResource(R.string.Weight), options.updateWeight, Modifier.weight(1f)) { onOptionsChange(options.copy(updateWeight = it)) } },
                second = { OptionChip(stringResource(R.string.time_minutes), options.updateTime, Modifier.weight(1f)) { onOptionsChange(options.copy(updateTime = it)) } }
            )
            OptionGridRow(
                first = { OptionChip(stringResource(R.string.resistance_level), options.updateResistance, Modifier.weight(1f)) { onOptionsChange(options.copy(updateResistance = it)) } },
                second = { OptionChip(stringResource(R.string.incline_percent), options.updateIncline, Modifier.weight(1f)) { onOptionsChange(options.copy(updateIncline = it)) } }
            )
        }
    }
}

@Composable
private fun OptionGridRow(first: @Composable RowScope.() -> Unit, second: @Composable RowScope.() -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        first()
        second()
    }
}

@Composable
private fun OptionChip(text: String, checked: Boolean, modifier: Modifier, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.background.copy(alpha = 0.56f))
            .border(1.dp, if (checked) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange, modifier = Modifier.size(32.dp))
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SectionGuidanceCard(iconResId: Int, title: String, text: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallIconBox(iconResId = iconResId)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

private enum class ExercisePreviewState { Updated, Added, Unchanged }

@Composable
private fun ExercisePreviewCard(
    index: Int,
    exercise: ExtendedExercise,
    lang: String,
    title: String,
    beforeSets: List<ExerciseSet>?,
    afterSets: List<ExerciseSet>?,
    options: TemplateUpdateOptions,
    state: ExercisePreviewState,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onSetDraftChange: (List<ExerciseSet>) -> Unit,
    onKeepCurrent: () -> Unit,
    onUseSource: () -> Unit,
    onRemoveFromTemplate: () -> Unit,
    onRestoreInTemplate: () -> Unit,
    isRemoved: Boolean,
    onEditSet: (Int, SetEditField, String, String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${index + 1}.", maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(end = 4.dp))
                    Text(text = title.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 3, overflow = TextOverflow.Clip, modifier = Modifier.weight(1f))
                }
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(34.dp)) {
                        Icon(painter = painterResource(R.drawable.ic_dots), contentDescription = stringResource(R.string.menu), tint = MaterialTheme.colorScheme.onSurface)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
                        when (state) {
                            ExercisePreviewState.Updated -> {
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_progress, stringResource(R.string.template_update_menu_use_workout_values)) }, onClick = { onUseSource(); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_complete, stringResource(R.string.template_update_menu_use_template_values)) }, onClick = { onKeepCurrent(); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_add, stringResource(R.string.add_set)) }, onClick = { onSetDraftChange(addSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_delete, stringResource(R.string.delete_set)) }, onClick = { onSetDraftChange(removeLastSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(if (isRemoved) R.drawable.ic_complete else R.drawable.ic_delete, if (isRemoved) stringResource(R.string.template_update_menu_restore_in_template) else stringResource(R.string.template_update_menu_remove_from_template)) }, onClick = { if (isRemoved) onRestoreInTemplate() else onRemoveFromTemplate(); showMenu = false })
                            }
                            ExercisePreviewState.Added -> {
                                DropdownMenuItem(text = { MenuItemContent(if (checked) R.drawable.ic_close else R.drawable.ic_add, if (checked) stringResource(R.string.template_update_menu_do_not_add) else stringResource(R.string.template_update_menu_add)) }, onClick = { onCheckedChange(!checked); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_add, stringResource(R.string.add_set)) }, onClick = { onSetDraftChange(addSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_delete, stringResource(R.string.delete_set)) }, onClick = { onSetDraftChange(removeLastSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_progress, stringResource(R.string.template_update_menu_reset_source)) }, onClick = { onUseSource(); showMenu = false })
                            }
                            ExercisePreviewState.Unchanged -> {
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_complete, stringResource(R.string.template_update_menu_use_template_values)) }, onClick = { onKeepCurrent(); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_add, stringResource(R.string.add_set)) }, onClick = { onSetDraftChange(addSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(R.drawable.ic_delete, stringResource(R.string.delete_set)) }, onClick = { onSetDraftChange(removeLastSet(afterSets ?: exercise.sets)); showMenu = false })
                                DropdownMenuItem(text = { MenuItemContent(if (isRemoved) R.drawable.ic_complete else R.drawable.ic_delete, if (isRemoved) stringResource(R.string.template_update_menu_restore_in_template) else stringResource(R.string.template_update_menu_remove_from_template)) }, onClick = { if (isRemoved) onRestoreInTemplate() else onRemoveFromTemplate(); showMenu = false })
                            }
                        }
                    }
                }
            }
            if (state == ExercisePreviewState.Added) {
                Row(modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                    Text(text = stringResource(R.string.template_update_new_exercise_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (isRemoved) {
                Text(text = stringResource(R.string.template_update_removed_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
            }
            val cardType = exercise.exercise.getCardType(lang)
            if (beforeSets != null) {
                SetBlockTitle(stringResource(R.string.template_update_current_template), false)
                PreviewSetsTable(cardType = cardType, exerciseSets = beforeSets, options = options, editable = false, onEditSet = onEditSet)
            }
            if (afterSets != null) {
                SetBlockTitle(stringResource(R.string.template_update_selected_workout), true)
                PreviewSetsTable(cardType = cardType, exerciseSets = afterSets, options = options, editable = true, onEditSet = onEditSet)
            }
        }
    }
}

@Composable
private fun MenuItemContent(iconResId: Int, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Icon(painter = painterResource(iconResId), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SetBlockTitle(text: String, accent: Boolean) {
    Text(text = text, style = MaterialTheme.typography.labelLarge, color = if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))
}

private data class PreviewColumn(val title: String, val field: SetEditField, val value: (ExerciseSet) -> String)

@Composable
private fun PreviewSetsTable(cardType: ExerciseCardType, exerciseSets: List<ExerciseSet>, options: TemplateUpdateOptions, editable: Boolean, onEditSet: (Int, SetEditField, String, String) -> Unit) {
    if (exerciseSets.isEmpty()) return
    val columns = when (cardType) {
        ExerciseCardType.STRENGTH -> listOf(PreviewColumn(stringResource(R.string.rep), SetEditField.Reps) { it.rep.toString() }, PreviewColumn(stringResource(R.string.Weight), SetEditField.Weight) { formatCompactDecimal(it.weight) })
        ExerciseCardType.BIKE -> listOf(PreviewColumn(stringResource(R.string.time_minutes), SetEditField.Time) { formatExerciseMinutes(it.time) }, PreviewColumn(stringResource(R.string.resistance_level), SetEditField.Resistance) { formatCompactDecimal(it.resistance) })
        ExerciseCardType.TREADMILL -> listOf(PreviewColumn(stringResource(R.string.time_minutes), SetEditField.Time) { formatExerciseMinutes(it.time) }, PreviewColumn(stringResource(R.string.resistance_level), SetEditField.Resistance) { formatCompactDecimal(it.resistance) }, PreviewColumn(stringResource(R.string.incline_percent), SetEditField.Incline) { formatCompactDecimal(it.incline) })
    }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        PreviewTableColumn(stringResource(R.string.set), exerciseSets.mapIndexed { index, _ -> (index + 1).toString() }, false, false) { _, _ -> }
        columns.forEach { column ->
            val dimmed = when (column.title) {
                stringResource(R.string.rep) -> !options.updateReps
                stringResource(R.string.Weight) -> !options.updateWeight
                stringResource(R.string.time_minutes) -> !options.updateTime
                stringResource(R.string.resistance_level) -> !options.updateResistance
                stringResource(R.string.incline_percent) -> !options.updateIncline
                else -> false
            }
            PreviewTableColumn(column.title, exerciseSets.map { column.value(it) }, dimmed, editable) { setIndex, value -> onEditSet(setIndex, column.field, value, column.title) }
        }
    }
}

@Composable
private fun PreviewTableColumn(title: String, values: List<String>, dimmed: Boolean, editable: Boolean, onClick: (Int, String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = if (dimmed) 0.38f else 1f), modifier = Modifier.padding(bottom = 8.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        values.forEachIndexed { index, value ->
            Box(modifier = Modifier.padding(horizontal = 3.dp).padding(bottom = 8.dp).width(60.dp).height(40.dp).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = if (dimmed) 0.18f else 0.5f), MaterialTheme.shapes.small).clickable(enabled = editable && !dimmed) { onClick(index, value) }, contentAlignment = Alignment.Center) {
                Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (dimmed) 0.38f else 1f), maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 3.dp))
            }
        }
    }
}

@Composable
private fun StickyFooterCard(
    description: String,
    activeStep: Int,
    secondaryText: String? = null,
    onSecondaryClick: (() -> Unit)? = null,
    primaryText: String? = null,
    onPrimaryClick: (() -> Unit)? = null,
    primaryEnabled: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.background, tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
        Card(shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    repeat(2) { index ->
                        Box(modifier = Modifier.padding(horizontal = 4.dp).size(width = if (index == activeStep) 22.dp else 8.dp, height = 8.dp).clip(CircleShape).background(if (index == activeStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)))
                    }
                }
                Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                if (primaryText != null || secondaryText != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (secondaryText != null && onSecondaryClick != null) {
                            OutlinedButton(onClick = onSecondaryClick, modifier = Modifier.weight(1f)) { Text(secondaryText) }
                        }
                        if (primaryText != null && onPrimaryClick != null) {
                            Button(onClick = onPrimaryClick, enabled = primaryEnabled, modifier = Modifier.weight(1f)) { Text(primaryText) }
                        }
                    }
                }
            }
        }
    }
}


private fun countNewWorkoutExercises(templateExercises: List<ExtendedExercise>, workoutExercises: List<ExtendedExercise>): Int {
    val templateIds = templateExercises.map { it.exercise.id }.toSet()
    return workoutExercises.count { it.exercise.id !in templateIds }
}

private fun buildTemplateWorkoutDiff(template: Template, workout: Workout): TemplateWorkoutDiff {
    val workoutByExerciseId = workout.exercises.associateBy { it.exercise.id }
    val templateIds = template.exercises.map { it.exercise.id }.toSet()
    return TemplateWorkoutDiff(
        matched = template.exercises.mapNotNull { templateExercise -> workoutByExerciseId[templateExercise.exercise.id]?.let { templateExercise to it } },
        onlyTemplate = template.exercises.filter { it.exercise.id !in workoutByExerciseId.keys },
        onlyWorkout = workout.exercises.filter { it.exercise.id !in templateIds }
    )
}

private fun buildUpdatedTemplateExercises(
    templateExercises: List<ExtendedExercise>,
    workoutExercises: List<ExtendedExercise>,
    selectedNewExerciseIds: Set<String>,
    removedTemplateExerciseIds: Set<String>,
    updatedExerciseDrafts: Map<String, ExtendedExercise>,
    addedExerciseDrafts: Map<String, ExtendedExercise>,
    unchangedExerciseDrafts: Map<String, ExtendedExercise>,
    options: TemplateUpdateOptions
): List<ExtendedExercise> {
    val workoutByExerciseId = workoutExercises.associateBy { it.exercise.id }
    val updatedExisting = templateExercises
        .filter { it.exercise.id !in removedTemplateExerciseIds }
        .map { templateExercise ->
            val source = updatedExerciseDrafts[templateExercise.exercise.id] ?: workoutByExerciseId[templateExercise.exercise.id]
            val unchangedDraft = unchangedExerciseDrafts[templateExercise.exercise.id]
            when {
                source != null -> templateExercise.copy(sets = mergeSets(templateExercise.sets, normalizeSource(source).sets, options))
                unchangedDraft != null -> templateExercise.copy(sets = unchangedDraft.sets.map { set -> set.copy(status = SetStatus.NONE) })
                else -> templateExercise.copy(sets = templateExercise.sets.map { set -> set.copy(status = SetStatus.NONE) })
            }
        }
    return updatedExisting + workoutExercises.filter { it.exercise.id in selectedNewExerciseIds }.map { exercise ->
        addedExerciseDrafts[exercise.exercise.id] ?: normalizeSource(exercise)
    }
}

private fun normalizeSource(source: ExtendedExercise): ExtendedExercise = source.copy(sets = source.sets.map { set -> set.copy(status = SetStatus.NONE) })

private fun addSet(sets: List<ExerciseSet>): List<ExerciseSet> {
    val base = sets.lastOrNull() ?: ExerciseSet(status = SetStatus.NONE)
    return sets + base.copy(status = SetStatus.NONE)
}

private fun removeLastSet(sets: List<ExerciseSet>): List<ExerciseSet> {
    return if (sets.size <= 1) sets else sets.dropLast(1)
}


private fun updateSetValue(sets: List<ExerciseSet>, setIndex: Int, field: SetEditField, rawValue: String): List<ExerciseSet> {
    val normalized = rawValue.replace(',', '.').trim()
    return sets.mapIndexed { index, set ->
        if (index != setIndex) set else when (field) {
            SetEditField.Reps -> set.copy(rep = normalized.toIntOrNull() ?: set.rep, status = SetStatus.NONE)
            SetEditField.Weight -> set.copy(weight = normalized.toFloatOrNull() ?: set.weight, status = SetStatus.NONE)
            SetEditField.Time -> set.copy(time = normalized.toFloatOrNull() ?: set.time, status = SetStatus.NONE)
            SetEditField.Resistance -> set.copy(resistance = normalized.toFloatOrNull() ?: set.resistance, status = SetStatus.NONE)
            SetEditField.Incline -> set.copy(incline = normalized.toFloatOrNull() ?: set.incline, status = SetStatus.NONE)
        }
    }
}

private fun mergeSets(current: List<ExerciseSet>, source: List<ExerciseSet>, options: TemplateUpdateOptions): List<ExerciseSet> {
    val base = if (options.updateSetsCount) source else current
    return base.mapIndexed { index, baseSet ->
        val sourceSet = source.getOrNull(index)
        val currentSet = current.getOrNull(index)
        if (sourceSet == null || currentSet == null) baseSet.copy(status = SetStatus.NONE) else currentSet.copy(
            rep = if (options.updateReps) sourceSet.rep else currentSet.rep,
            weight = if (options.updateWeight) sourceSet.weight else currentSet.weight,
            time = if (options.updateTime) sourceSet.time else currentSet.time,
            resistance = if (options.updateResistance) sourceSet.resistance else currentSet.resistance,
            incline = if (options.updateIncline) sourceSet.incline else currentSet.incline,
            status = SetStatus.NONE
        )
    }
}

private fun countMatchedExercises(templateExercises: List<ExtendedExercise>, workoutExercises: List<ExtendedExercise>): Int {
    val workoutIds = workoutExercises.map { it.exercise.id }.toSet()
    return templateExercises.count { it.exercise.id in workoutIds }
}

private fun formatWorkoutDate(timestamp: Long): String {
    if (timestamp <= 0L) return "—"
    return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(timestamp))
}
