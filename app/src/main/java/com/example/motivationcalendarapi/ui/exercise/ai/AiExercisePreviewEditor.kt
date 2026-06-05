package com.example.motivationcalendarapi.ui.exercise.ai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.getIconForEquipment
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseCatalog
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.repositories.ai.GeminiAiGenerationApi
import com.example.motivationcalendarapi.repositories.ai.GeneratedTemplateDraft
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.ui.template.fragments.ExerciseTemplateItem
import com.motivationcalendar.ui.RepsDialog
import com.example.motivationcalendarapi.ui.dialogs.WeightDialog
import com.example.motivationcalendarapi.ui.dialogs.FloatMetricDialog
import com.example.motivationcalendarapi.viewmodel.AiExerciseGenerationViewModel
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.Collections

@Composable
internal fun ExercisePreviewEditor(
    exercise: Exercise,
    lang: String,
    allExercises: List<Exercise>,
    selectedBodyPart: String,
    selectedEquipment: String,
    onChange: (Exercise) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.preview_and_edit),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.name),
            lang = lang,
            values = exercise.nameLocalized
        ) { onChange(exercise.copy(nameLocalized = it)) }

        PreviewBodyEquipmentCard(
            title = stringResource(R.string.body_part),
            selected = exercise.getBodyPart(lang).ifBlank { selectedBodyPart },
            iconRes = getIconForBodyPart(exercise.getBodyPart(lang).ifBlank { selectedBodyPart }),
            optionGroups = ExerciseCatalog.groupedBodyPartLabels(lang),
            optionIcon = { getIconForBodyPart(it) },
            onSelected = { selected ->
                val bodyMap = ExerciseCatalog.bodyParts.firstOrNull { it.getLabel(lang) == selected }?.localized
                    ?: allExercises.firstOrNull { it.getBodyPart(lang) == selected }?.bodyPartLocalized
                    ?: mapOf("en" to selected, "ru" to selected, "be" to selected)
                onChange(exercise.copy(bodyPartLocalized = bodyMap))
            }
        )

        PreviewBodyEquipmentCard(
            title = stringResource(R.string.equipment),
            selected = exercise.getEquipment(lang).ifBlank { selectedEquipment },
            iconRes = getIconForEquipment(exercise.equipmentLocalized).takeIf { exercise.equipmentLocalized.isNotEmpty() } ?: safeEquipmentIcon(selectedEquipment),
            optionGroups = ExerciseCatalog.groupedEquipmentLabels(lang),
            optionIcon = { safeEquipmentIcon(it) },
            onSelected = { selected ->
                val equipmentMap = ExerciseCatalog.equipment.firstOrNull { it.getLabel(lang) == selected }?.localized
                    ?: allExercises.firstOrNull { it.getEquipment(lang) == selected }?.equipmentLocalized
                    ?: mapOf("en" to selected, "ru" to selected, "be" to selected)
                onChange(exercise.copy(equipmentLocalized = equipmentMap))
            }
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.target),
            lang = lang,
            values = exercise.targetLocalized
        ) { onChange(exercise.copy(targetLocalized = it)) }

        PreviewCardTypeCard(
            selected = exercise.cardType,
            onSelected = { onChange(exercise.copy(cardType = it)) }
        )

        PreviewLocalizedListCard(
            title = stringResource(R.string.instructions),
            lang = lang,
            values = exercise.instructionsLocalized
        ) { onChange(exercise.copy(instructionsLocalized = it)) }
    }
}

@Composable
internal fun PreviewLocalizedStringCard(
    title: String,
    lang: String,
    values: Map<String, String>,
    onChange: (Map<String, String>) -> Unit
) {
    CardBlock(title = title) {
        AiTextField(
            value = values[lang].orEmpty(),
            onValueChange = { value ->
                onChange(values.toMutableMap().apply { this[lang] = value })
            },
            label = title
        )
    }
}

@Composable
internal fun PreviewLocalizedListCard(
    title: String,
    lang: String,
    values: Map<String, List<String>>,
    onChange: (Map<String, List<String>>) -> Unit
) {
    CardBlock(title = title) {
        val joined = values[lang].orEmpty().joinToString("\n")
        AiTextField(
            value = joined,
            onValueChange = { value ->
                onChange(values.toMutableMap().apply {
                    this[lang] = value.lines().map { it.trim() }.filter { it.isNotBlank() }
                })
            },
            label = title,
            minLines = 4
        )
    }
}

@Composable
internal fun PreviewBodyEquipmentCard(
    title: String,
    selected: String,
    iconRes: Int,
    optionGroups: List<Pair<String, List<String>>>,
    optionIcon: (String) -> Int,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 210),
        label = "$title preview arrow rotation"
    )

    CardBlock(title = title) {
        SelectableHeaderCard(
            value = selected.ifBlank { stringResource(R.string.not_set) },
            iconRes = iconRes,
            isFilled = selected.isNotBlank(),
            rotation = rotation,
            onClick = { expanded = !expanded }
        )

        AnimatedVisibility(visible = expanded) {
            GroupedInlineOptionsList(
                optionGroups = optionGroups,
                optionIcon = optionIcon,
                optionContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                selectedOptions = listOf(selected).filter { it.isNotBlank() },
                onSelected = { option ->
                    onSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
internal fun PreviewCardTypeCard(selected: String, onSelected: (String) -> Unit) {
    val options = ExerciseCardType.entries.map { it.name }
    PreviewBodyEquipmentCard(
        title = stringResource(R.string.exercise_type),
        selected = selected,
        iconRes = cardTypeIcon(selected),
        optionGroups = listOf("" to options),
        optionIcon = { cardTypeIcon(it) },
        onSelected = onSelected
    )
}

internal fun cardTypeIcon(cardType: String): Int = when (cardType) {
    ExerciseCardType.BIKE.name -> R.drawable.ic_card_bike
    ExerciseCardType.TREADMILL.name -> R.drawable.ic_card_treadmill
    else -> R.drawable.ic_dumbbell
}


@Composable
internal fun TemplatePreviewEditor(
    draft: GeneratedTemplateDraft,
    lang: String,
    navController: NavController,
    workoutViewModel: WorkoutViewModel,
    onChange: (GeneratedTemplateDraft) -> Unit
) {
    val minRep by workoutViewModel.minRep.collectAsState()
    val maxRep by workoutViewModel.maxRep.collectAsState()
    val stepRep by workoutViewModel.stepRep.collectAsState()
    val minWeight by workoutViewModel.minWeight.collectAsState()
    val maxWeight by workoutViewModel.maxWeight.collectAsState()
    val stepWeight by workoutViewModel.stepWeight.collectAsState()
    val minCardioTime by workoutViewModel.minCardioTime.collectAsState()
    val maxCardioTime by workoutViewModel.maxCardioTime.collectAsState()
    val stepCardioTime by workoutViewModel.stepCardioTime.collectAsState()
    val minResistance by workoutViewModel.minResistance.collectAsState()
    val maxResistance by workoutViewModel.maxResistance.collectAsState()
    val stepResistance by workoutViewModel.stepResistance.collectAsState()
    val minIncline by workoutViewModel.minIncline.collectAsState()
    val maxIncline by workoutViewModel.maxIncline.collectAsState()
    val stepIncline by workoutViewModel.stepIncline.collectAsState()

    var currentExerciseIndex by remember { mutableStateOf(0) }
    var currentSetIndex by remember { mutableStateOf(0) }
    var showRepDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showResistanceDialog by remember { mutableStateOf(false) }
    var showInclineDialog by remember { mutableStateOf(false) }

    fun updateDraftSet(exerciseIndex: Int, setIndex: Int, transform: (ExerciseSet) -> ExerciseSet) {
        val updatedExercises = draft.exercises.toMutableList()
        val currentExercise = updatedExercises.getOrNull(exerciseIndex) ?: return
        val updatedSets = currentExercise.sets.mapIndexed { currentSetIndex, set ->
            if (currentSetIndex == setIndex) transform(set) else set
        }
        updatedExercises[exerciseIndex] = currentExercise.copy(sets = updatedSets)
        onChange(draft.copy(exercises = updatedExercises))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.preview_and_edit),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        PreviewLocalizedStringCard(
            title = stringResource(R.string.template_name),
            lang = lang,
            values = draft.nameLocalized
        ) { updatedName ->
            onChange(draft.copy(nameLocalized = updatedName))
        }

        draft.exercises.forEachIndexed { index, extended ->
            ExerciseTemplateItem(
                index = index,
                exercise = extended,
                templateId = "ai_template_preview",
                onDelete = {
                    val updated = draft.exercises.toMutableList().apply { removeAt(index) }
                    onChange(draft.copy(exercises = updated))
                },
                onMoveUp = {
                    if (index > 0) {
                        val updated = draft.exercises.toMutableList().apply {
                            Collections.swap(this, index, index - 1)
                        }
                        onChange(draft.copy(exercises = updated))
                    }
                },
                onMoveDown = {
                    if (index < draft.exercises.lastIndex) {
                        val updated = draft.exercises.toMutableList().apply {
                            Collections.swap(this, index, index + 1)
                        }
                        onChange(draft.copy(exercises = updated))
                    }
                },
                canMoveUp = index > 0,
                canMoveDown = index < draft.exercises.lastIndex,
                onAddSetClick = { exerciseIndex ->
                    val updated = draft.exercises.toMutableList()
                    val current = updated.getOrNull(exerciseIndex)
                    if (current != null) {
                        val cardType = current.exercise.getCardType(lang)
                        val nextSet = current.sets.lastOrNull() ?: when (cardType) {
                            ExerciseCardType.STRENGTH -> ExerciseSet(rep = minRep, weight = minWeight, status = SetStatus.NONE)
                            ExerciseCardType.BIKE -> ExerciseSet(time = minCardioTime, resistance = minResistance, status = SetStatus.NONE)
                            ExerciseCardType.TREADMILL -> ExerciseSet(time = minCardioTime, resistance = minResistance, incline = minIncline, status = SetStatus.NONE)
                        }
                        updated[exerciseIndex] = current.copy(sets = current.sets + nextSet.copy(status = SetStatus.NONE))
                        onChange(draft.copy(exercises = updated))
                    }
                },
                onRepClick = { exerciseIndex, setIndex ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                    showRepDialog = true
                },
                onWeightClick = { exerciseIndex, setIndex ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                    showWeightDialog = true
                },
                onTimeClick = { exerciseIndex, setIndex ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                    showTimeDialog = true
                },
                onResistanceClick = { exerciseIndex, setIndex ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                    showResistanceDialog = true
                },
                onInclineClick = { exerciseIndex, setIndex ->
                    currentExerciseIndex = exerciseIndex
                    currentSetIndex = setIndex
                    showInclineDialog = true
                },
                onStatusClick = { exerciseIndex, setIndex, status ->
                    updateDraftSet(exerciseIndex, setIndex) { it.copy(status = status) }
                },
                navController = navController,
                onDeleteSet = { _, exerciseIndex, setIndex ->
                    val updated = draft.exercises.toMutableList()
                    val current = updated.getOrNull(exerciseIndex)
                    if (current != null && current.sets.size > 1) {
                        updated[exerciseIndex] = current.copy(
                            sets = current.sets.toMutableList().apply { removeAt(setIndex) }
                        )
                        onChange(draft.copy(exercises = updated))
                    }
                },
                exerciseSets = extended.sets,
                workoutViewModel = workoutViewModel,
                lang = lang
            )
        }
    }

    val selectedSet = draft.exercises
        .getOrNull(currentExerciseIndex)
        ?.sets
        ?.getOrNull(currentSetIndex)

    RepsDialog(
        showDialog = showRepDialog,
        initialRep = selectedSet?.rep ?: minRep,
        minRep = minRep,
        maxRep = maxRep,
        stepRep = stepRep,
        onDismiss = { showRepDialog = false },
        onSave = { newRep ->
            updateDraftSet(currentExerciseIndex, currentSetIndex) { it.copy(rep = newRep) }
            showRepDialog = false
        }
    )

    WeightDialog(
        showDialog = showWeightDialog,
        initialWeight = selectedSet?.weight ?: minWeight,
        minWeight = minWeight,
        maxWeight = maxWeight,
        stepWeight = stepWeight,
        onDismiss = { showWeightDialog = false },
        onSave = { newWeight ->
            updateDraftSet(currentExerciseIndex, currentSetIndex) { it.copy(weight = newWeight) }
            showWeightDialog = false
        }
    )

    FloatMetricDialog(
        showDialog = showTimeDialog,
        title = stringResource(R.string.edit_time),
        label = stringResource(R.string.time_minutes),
        initialValue = selectedSet?.time ?: minCardioTime,
        minValue = minCardioTime,
        maxValue = maxCardioTime,
        stepValue = stepCardioTime,
        onDismiss = { showTimeDialog = false },
        onSave = { newTime ->
            updateDraftSet(currentExerciseIndex, currentSetIndex) { it.copy(time = newTime) }
            showTimeDialog = false
        }
    )

    FloatMetricDialog(
        showDialog = showResistanceDialog,
        title = stringResource(R.string.edit_resistance),
        label = stringResource(R.string.resistance_level),
        initialValue = selectedSet?.resistance ?: minResistance,
        minValue = minResistance,
        maxValue = maxResistance,
        stepValue = stepResistance,
        onDismiss = { showResistanceDialog = false },
        onSave = { newResistance ->
            updateDraftSet(currentExerciseIndex, currentSetIndex) { it.copy(resistance = newResistance) }
            showResistanceDialog = false
        }
    )

    FloatMetricDialog(
        showDialog = showInclineDialog,
        title = stringResource(R.string.edit_incline),
        label = stringResource(R.string.incline_percent),
        initialValue = selectedSet?.incline ?: minIncline,
        minValue = minIncline,
        maxValue = maxIncline,
        stepValue = stepIncline,
        onDismiss = { showInclineDialog = false },
        onSave = { newIncline ->
            updateDraftSet(currentExerciseIndex, currentSetIndex) { it.copy(incline = newIncline) }
            showInclineDialog = false
        }
    )
}

