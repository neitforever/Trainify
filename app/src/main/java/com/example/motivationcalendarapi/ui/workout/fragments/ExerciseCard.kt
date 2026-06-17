package com.motivationcalendar.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ClusterSetData
import com.example.motivationcalendarapi.model.ClusterSetPart
import com.example.motivationcalendarapi.model.DropSetPart
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExerciseSetType
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.toDefaultClusterSet
import com.example.motivationcalendarapi.model.toDefaultDropSet
import com.example.motivationcalendarapi.model.toNormalSet
import com.example.motivationcalendarapi.ui.dialogs.TimeMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.WeightDialog
import com.example.motivationcalendarapi.ui.fragments.StatusIcon
import com.example.motivationcalendarapi.ui.workout.fragments.NoteBottomSheet
import com.example.motivationcalendarapi.utils.formatCompactDecimal
import com.example.motivationcalendarapi.utils.formatExerciseMinutes
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    index: Int,
    exercise: ExtendedExercise,
    exerciseSets: List<ExerciseSet>,
    onAddSetClick: (Int) -> Unit,
    onRepClick: (Int, Int) -> Unit,
    onWeightClick: (Int, Int) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    onInclineClick: (Int, Int) -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canCreateSupersetWithPrevious: Boolean = canMoveUp,
    canCreateSupersetWithNext: Boolean = canMoveDown,
    workoutViewModel: WorkoutViewModel,
    navController: NavController,
    lang: String,
    onDeleteExercise: (() -> Unit)? = null,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)? = null,
    onDeleteSetClick: ((Int, Int) -> Unit)? = null,
    showMaxSetMenu: Boolean = true,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)? = null,
    onCreateSupersetWithNext: ((Int) -> Unit)? = null,
    onCreateSupersetWithPrevious: ((Int) -> Unit)? = null,
    onRemoveFromSuperset: ((Int) -> Unit)? = null,
    supersetLabel: String? = null,
    isSupersetFirst: Boolean = true,
    isSupersetLast: Boolean = true,
    supersetBlockStartIndex: Int = index,
    supersetBlockEndIndex: Int = index,
    modifier: Modifier = Modifier,
    isExerciseDragging: Boolean = false,
    isExerciseMergeTarget: Boolean = false,
    exerciseDragOffsetY: Float = 0f,
    onExerciseDragStart: ((Int, Offset) -> Unit)? = null,
    onExerciseDrag: ((Offset) -> Unit)? = null,
    onExerciseDragEnd: (() -> Unit)? = null
) {
    val isExpanded = remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }


    val notesMap by workoutViewModel.notesUpdates.collectAsState()
    val currentNote = notesMap[exercise.exercise.id] ?: exercise.exercise.note
    var localNote by remember { mutableStateOf(currentNote) }

    val cardType = exercise.exercise.getCardType(lang)

    val maxSet = remember(exercise.exercise.id, showMaxSetMenu) {
        if (showMaxSetMenu) workoutViewModel.findMaxSetForExercise(exercise.exercise.id) else null
    }

    LaunchedEffect(currentNote) {
        localNote = currentNote
    }

    val updatedExercise = remember(exercise, localNote) {
        exercise.copy(exercise = exercise.exercise.copy(note = localNote))
    }

    val isSupersetCard = exercise.supersetGroupId != null
    val cardShape = if (isSupersetCard) {
        RoundedCornerShape(
            topStart = if (isSupersetFirst) 28.dp else 6.dp,
            topEnd = if (isSupersetFirst) 28.dp else 6.dp,
            bottomStart = if (isSupersetLast) 28.dp else 6.dp,
            bottomEnd = if (isSupersetLast) 28.dp else 6.dp
        )
    } else {
        MaterialTheme.shapes.medium
    }

    val dragContainerColor by animateColorAsState(
        targetValue = when {
            isExerciseDragging -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            isExerciseMergeTarget -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "exerciseDragContainerColor"
    )
    val cardColors = if (isExerciseDragging || isExerciseMergeTarget) {
        CardDefaults.cardColors(containerColor = dragContainerColor)
    } else {
        CardDefaults.cardColors()
    }

    Card(
        modifier = modifier
            .zIndex(if (isExerciseDragging) 1f else 0f)
            .graphicsLayer {
                translationY = exerciseDragOffsetY
                alpha = if (isExerciseDragging) 0.96f else 1f
                shadowElevation = if (isExerciseDragging) 18f else 0f
            }
            .pointerInput(index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset -> onExerciseDragStart?.invoke(index, offset) },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onExerciseDrag?.invoke(dragAmount)
                    },
                    onDragEnd = { onExerciseDragEnd?.invoke() },
                    onDragCancel = { onExerciseDragEnd?.invoke() }
                )
            }
            .padding(
                top = if (isSupersetCard && !isSupersetFirst) 1.dp else 8.dp,
                bottom = if (isSupersetCard && !isSupersetLast) 1.dp else 8.dp
            ),
        shape = cardShape,
        border = null,
        colors = cardColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isSupersetCard && isSupersetFirst && supersetLabel != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TechniqueBadge(text = supersetLabel)
                }
            }
            // Верхняя
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    Text(
                        text = exercise.exercise.getName(lang).replaceFirstChar { it.uppercase() },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clickable { isExpanded.value = !isExpanded.value },
                        maxLines = if (isExpanded.value) 1 else Int.MAX_VALUE,
                        overflow = if (isExpanded.value) TextOverflow.Ellipsis else TextOverflow.Clip,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .align(Alignment.CenterVertically)

                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dots),
                            contentDescription = stringResource(R.string.menu),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        )

                    ) {
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_delete),
                                    contentDescription = stringResource(R.string.delete),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.delete_exercise),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }, onClick = {
                            onDeleteExercise?.invoke() ?: workoutViewModel.removeExercise(index)
                            showMenu = false
                        })
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = stringResource(R.string.info),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.exercise_info),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }, onClick = {
                            navController.navigate("exercise_detail/${exercise.exercise.id}")
                            showMenu = false
                        })

                        if (canCreateSupersetWithPrevious) {
                            DropdownMenuItem(
                                text = { StatusMenuRow(iconRes = R.drawable.ic_add_circle, text = stringResource(R.string.create_superset_with_previous)) },
                                onClick = {
                                    onCreateSupersetWithPrevious?.invoke(supersetBlockStartIndex) ?: workoutViewModel.createSupersetWithPrevious(supersetBlockStartIndex)
                                    showMenu = false
                                }
                            )
                        }

                        if (canCreateSupersetWithNext) {
                            DropdownMenuItem(
                                text = { StatusMenuRow(iconRes = R.drawable.ic_add_circle, text = stringResource(R.string.create_superset_with_next)) },
                                onClick = {
                                    onCreateSupersetWithNext?.invoke(supersetBlockEndIndex) ?: workoutViewModel.createSupersetWithNext(supersetBlockEndIndex)
                                    showMenu = false
                                }
                            )
                        }

                        if (exercise.supersetGroupId != null) {
                            DropdownMenuItem(
                                text = { StatusMenuRow(iconRes = R.drawable.ic_close, text = stringResource(R.string.remove_from_superset)) },
                                onClick = {
                                    onRemoveFromSuperset?.invoke(index) ?: workoutViewModel.removeExerciseFromSuperset(index)
                                    showMenu = false
                                }
                            )
                        }

                        if (maxSet != null) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_equipment_body_weight),
                                            contentDescription = stringResource(R.string.max_set),
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(
                                                R.string.max_set_format,
                                                maxSet.weight,
                                                maxSet.rep
                                            ),
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                onClick = { showMenu = false }
                            )
                        }

                    }
                }
            }

            when (cardType) {
            ExerciseCardType.STRENGTH -> {
                StrengthSetsTable(
                    index = index,
                    exerciseSets = exerciseSets,
                    onRepClick = onRepClick,
                    onWeightClick = onWeightClick,
                    workoutViewModel = workoutViewModel,
                    onSetStatusClick = onSetStatusClick,
                    onDeleteSetClick = onDeleteSetClick,
                    onSetTechniqueClick = onSetTechniqueClick
                )
            }

            ExerciseCardType.BIKE -> {
                BikeSetsTable(
                    index = index,
                    exerciseSets = exerciseSets,
                    onTimeClick = onTimeClick,
                    onResistanceClick = onResistanceClick,
                    workoutViewModel = workoutViewModel,
                    onSetStatusClick = onSetStatusClick,
                    onDeleteSetClick = onDeleteSetClick,
                    onSetTechniqueClick = onSetTechniqueClick
                )
            }

            ExerciseCardType.TREADMILL -> {
                TreadmillSetsTable(
                    index = index,
                    exerciseSets = exerciseSets,
                    onTimeClick = onTimeClick,
                    onResistanceClick = onResistanceClick,
                    onInclineClick = onInclineClick,
                    workoutViewModel = workoutViewModel,
                    onSetStatusClick = onSetStatusClick,
                    onDeleteSetClick = onDeleteSetClick,
                    onSetTechniqueClick = onSetTechniqueClick
                )
            }
        }
        }

        // Нижняя
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_up),
                    contentDescription = stringResource(R.string.move_up),
                    tint = if (canMoveUp) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.add_set),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onAddSetClick(index) },
                    textAlign = TextAlign.Center
                )
                Text(
                    text = stringResource(R.string.note),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable { showNoteDialog = true }
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center)
            }
            IconButton(
                onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = stringResource(R.string.move_down),
                    tint = if (canMoveDown) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

    }


    NoteBottomSheet(
        showBottomSheet = showNoteDialog, exercise = updatedExercise, onDismiss = {
        showNoteDialog = false
        workoutViewModel.updateExerciseNote(updatedExercise.exercise.id, localNote)
    }, onSaveNote = { newNote ->
        localNote = newNote
        workoutViewModel.updateExerciseNote(updatedExercise.exercise.id, newNote)
    },
        lang = lang
    )


}

@Composable
private fun StrengthSetsTable(
    index: Int,
    exerciseSets: List<ExerciseSet>,
    onRepClick: (Int, Int) -> Unit,
    onWeightClick: (Int, Int) -> Unit,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?
) {
    SetsTable(
        index = index,
        exerciseSets = exerciseSets,
        columns = listOf(
            TableColumn(
                title = stringResource(R.string.rep),
                value = { it.rep.toString() },
                onClick = onRepClick
            ),
            TableColumn(
                title = stringResource(R.string.Weight),
                value = { "%.1f".format(it.weight) },
                onClick = onWeightClick
            )
        ),
        workoutViewModel = workoutViewModel,
        onSetStatusClick = onSetStatusClick,
        onDeleteSetClick = onDeleteSetClick,
        onSetTechniqueClick = onSetTechniqueClick,
        allowAdvancedSetTechnique = true
    )
}

@Composable
private fun BikeSetsTable(
    index: Int,
    exerciseSets: List<ExerciseSet>,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?
) {
    SetsTable(
        index = index,
        exerciseSets = exerciseSets,
        columns = listOf(
            TableColumn(
                title = stringResource(R.string.time_minutes),
                value = { formatExerciseMinutes(it.time) },
                onClick = onTimeClick
            ),
            TableColumn(
                title = stringResource(R.string.resistance_level),
                value = { it.resistance.toString() },
                onClick = onResistanceClick
            )
        ),
        workoutViewModel = workoutViewModel,
        onSetStatusClick = onSetStatusClick,
        onDeleteSetClick = onDeleteSetClick,
        onSetTechniqueClick = onSetTechniqueClick,
        allowAdvancedSetTechnique = false
    )
}

@Composable
private fun TreadmillSetsTable(
    index: Int,
    exerciseSets: List<ExerciseSet>,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    onInclineClick: (Int, Int) -> Unit,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?
) {
    SetsTable(
        index = index,
        exerciseSets = exerciseSets,
        columns = listOf(
            TableColumn(
                title = stringResource(R.string.time_minutes),
                value = { formatExerciseMinutes(it.time) },
                onClick = onTimeClick
            ),
            TableColumn(
                title = stringResource(R.string.resistance_level),
                value = { it.resistance.toString() },
                onClick = onResistanceClick
            ),
            TableColumn(
                title = stringResource(R.string.incline_percent),
                value = { formatCompactDecimal(it.incline) },
                onClick = onInclineClick
            )
        ),
        workoutViewModel = workoutViewModel,
        onSetStatusClick = onSetStatusClick,
        onDeleteSetClick = onDeleteSetClick,
        onSetTechniqueClick = onSetTechniqueClick,
        allowAdvancedSetTechnique = false
    )
}

private data class TableColumn(
    val title: String,
    val value: (ExerciseSet) -> String,
    val onClick: (Int, Int) -> Unit
)

@Composable
private fun SetsTable(
    index: Int,
    exerciseSets: List<ExerciseSet>,
    columns: List<TableColumn>,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?,
    allowAdvancedSetTechnique: Boolean
) {
    if (exerciseSets.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExerciseSetHeaderCell(text = stringResource(R.string.set), modifier = Modifier.width(60.dp))
            columns.forEach { column ->
                ExerciseSetHeaderCell(
                    text = column.title,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .width(60.dp)
                )
            }
            ExerciseSetHeaderCell(text = stringResource(R.string.status), modifier = Modifier.width(60.dp))
        }

        exerciseSets.forEachIndexed { setIndex, set ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(60.dp, 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${setIndex + 1}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }

                columns.forEach { column ->
                    val cellValue = column.value(set)
                    ExerciseSetValueCell(
                        value = cellValue,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .padding(bottom = 8.dp)
                            .width(60.dp),
                        onClick = { column.onClick(index, setIndex) }
                    )
                }

                SetStatusActionCell(
                    exerciseIndex = index,
                    setIndex = setIndex,
                    set = set,
                    exerciseSets = exerciseSets,
                    workoutViewModel = workoutViewModel,
                    onSetStatusClick = onSetStatusClick,
                    onDeleteSetClick = onDeleteSetClick,
                    onSetTechniqueClick = onSetTechniqueClick,
                    allowAdvancedSetTechnique = allowAdvancedSetTechnique,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .width(60.dp)
                        .height(40.dp)
                )
            }

            if (allowAdvancedSetTechnique &&
                (set.type == ExerciseSetType.DROP_SET || set.type == ExerciseSetType.CLUSTER_SET)
            ) {
                AdvancedSetsInlineEditor(
                    exerciseIndex = index,
                    exerciseSets = exerciseSets,
                    workoutViewModel = workoutViewModel,
                    onSetTechniqueClick = onSetTechniqueClick,
                    targetSetIndex = setIndex
                )
            }
        }
    }
}

@Composable
private fun ExerciseSetHeaderCell(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(bottom = 12.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun ExerciseSetValueCell(
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .clickable { onClick() }
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = if (value.isLongTimerCell()) {
                MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SetStatusActionCell(
    exerciseIndex: Int,
    setIndex: Int,
    set: ExerciseSet,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?,
    allowAdvancedSetTechnique: Boolean,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable { showStatusMenu = true },
            contentAlignment = Alignment.Center
        ) {
            StatusIcon(status = set.status)
        }

        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_warm_up, text = stringResource(R.string.warm_up)) },
                onClick = {
                    onSetStatusClick?.invoke(exerciseIndex, setIndex, SetStatus.WARMUP)
                        ?: workoutViewModel.updateSetStatus(exerciseIndex, setIndex, SetStatus.WARMUP)
                    showStatusMenu = false
                }
            )

            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_close, text = stringResource(R.string.failed)) },
                onClick = {
                    onSetStatusClick?.invoke(exerciseIndex, setIndex, SetStatus.FAILED)
                        ?: workoutViewModel.updateSetStatus(exerciseIndex, setIndex, SetStatus.FAILED)
                    showStatusMenu = false
                }
            )

            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_complete, text = stringResource(R.string.completed)) },
                onClick = {
                    onSetStatusClick?.invoke(exerciseIndex, setIndex, SetStatus.COMPLETED)
                        ?: workoutViewModel.updateSetStatus(exerciseIndex, setIndex, SetStatus.COMPLETED)
                    showStatusMenu = false
                }
            )

            if (allowAdvancedSetTechnique) {
                DropdownMenuItem(
                    text = { StatusMenuRow(iconRes = R.drawable.ic_restart, text = stringResource(R.string.normal_set)) },
                    onClick = {
                        val newSet = set.toNormalSet()
                        onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet)
                            ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                        showStatusMenu = false
                    }
                )

                DropdownMenuItem(
                    text = { StatusMenuRow(iconRes = R.drawable.ic_drop_set, text = stringResource(R.string.drop_set)) },
                    onClick = {
                        val newSet = set.toDefaultDropSet()
                        onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet)
                            ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                        showStatusMenu = false
                    }
                )

                DropdownMenuItem(
                    text = { StatusMenuRow(iconRes = R.drawable.ic_cluster_set, text = stringResource(R.string.cluster_set)) },
                    onClick = {
                        val newSet = set.toDefaultClusterSet()
                        onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet)
                            ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                        showStatusMenu = false
                    }
                )
            }

            if (exerciseSets.size > 1) {
                DropdownMenuItem(
                    text = { StatusMenuRow(iconRes = R.drawable.ic_delete, text = stringResource(R.string.delete_set)) },
                    onClick = {
                        onDeleteSetClick?.invoke(exerciseIndex, setIndex)
                            ?: workoutViewModel.removeExerciseSet(exerciseIndex, setIndex)
                        showStatusMenu = false
                    }
                )
            }
        }
    }
}


@Composable
private fun StatusColumn(
    exerciseIndex: Int,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel,
    onSetStatusClick: ((Int, Int, SetStatus) -> Unit)?,
    onDeleteSetClick: ((Int, Int) -> Unit)?,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?,
    allowAdvancedSetTechnique: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(R.string.status),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        exerciseSets.forEachIndexed { setIndex, set ->
            var showStatusMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp, 40.dp)
                        .clickable { showStatusMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    StatusIcon(status = set.status)
                }

                DropdownMenu(
                    expanded = showStatusMenu,
                    onDismissRequest = { showStatusMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = {
                            StatusMenuRow(
                                iconRes = R.drawable.ic_warm_up,
                                text = stringResource(R.string.warm_up)
                            )
                        },
                        onClick = {
                            onSetStatusClick?.invoke(
                                exerciseIndex,
                                setIndex,
                                SetStatus.WARMUP
                            ) ?: workoutViewModel.updateSetStatus(
                                exerciseIndex,
                                setIndex,
                                SetStatus.WARMUP
                            )
                            showStatusMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            StatusMenuRow(
                                iconRes = R.drawable.ic_close,
                                text = stringResource(R.string.failed)
                            )
                        },
                        onClick = {
                            onSetStatusClick?.invoke(
                                exerciseIndex,
                                setIndex,
                                SetStatus.FAILED
                            ) ?: workoutViewModel.updateSetStatus(
                                exerciseIndex,
                                setIndex,
                                SetStatus.FAILED
                            )
                            showStatusMenu = false
                        }
                    )

                    DropdownMenuItem(
                        text = {
                            StatusMenuRow(
                                iconRes = R.drawable.ic_complete,
                                text = stringResource(R.string.completed)
                            )
                        },
                        onClick = {
                            onSetStatusClick?.invoke(
                                exerciseIndex,
                                setIndex,
                                SetStatus.COMPLETED
                            ) ?: workoutViewModel.updateSetStatus(
                                exerciseIndex,
                                setIndex,
                                SetStatus.COMPLETED
                            )
                            showStatusMenu = false
                        }
                    )

                    if (allowAdvancedSetTechnique) {
                    DropdownMenuItem(
                            text = { StatusMenuRow(iconRes = R.drawable.ic_restart, text = stringResource(R.string.normal_set)) },
                            onClick = {
                                val newSet = set.toNormalSet()
                                onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet) ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                                showStatusMenu = false
                            }
                        )
    
                        DropdownMenuItem(
                            text = { StatusMenuRow(iconRes = R.drawable.ic_down, text = stringResource(R.string.drop_set)) },
                            onClick = {
                                val newSet = set.toDefaultDropSet()
                                onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet) ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                                showStatusMenu = false
                            }
                        )
    
                        DropdownMenuItem(
                            text = { StatusMenuRow(iconRes = R.drawable.ic_time, text = stringResource(R.string.cluster_set)) },
                            onClick = {
                                val newSet = set.toDefaultClusterSet()
                                onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet) ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
                                showStatusMenu = false
                            }
                        )
                        }

                    if (exerciseSets.size > 1) {
                        DropdownMenuItem(
                            text = {
                                StatusMenuRow(
                                    iconRes = R.drawable.ic_delete,
                                    text = stringResource(R.string.delete_set)
                                )
                            },
                            onClick = {
                                onDeleteSetClick?.invoke(
                                    exerciseIndex,
                                    setIndex
                                ) ?: workoutViewModel.removeExerciseSet(
                                    exerciseIndex,
                                    setIndex
                                )
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

private sealed class AdvancedSetEditTarget {
    data class DropWeight(val setIndex: Int, val partIndex: Int, val value: Float) : AdvancedSetEditTarget()
    data class DropReps(val setIndex: Int, val partIndex: Int, val value: Int) : AdvancedSetEditTarget()
    data class ClusterWeight(val setIndex: Int, val clusterIndex: Int, val value: Float) : AdvancedSetEditTarget()
    data class ClusterReps(val setIndex: Int, val clusterIndex: Int, val value: Int) : AdvancedSetEditTarget()
    data class ClusterRest(val setIndex: Int, val valueSeconds: Int) : AdvancedSetEditTarget()
}

@Composable
private fun AdvancedSetsInlineEditor(
    exerciseIndex: Int,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel,
    onSetTechniqueClick: ((Int, Int, ExerciseSet) -> Unit)?,
    targetSetIndex: Int? = null
) {
    val advancedSets = exerciseSets.mapIndexedNotNull { index, set ->
        val isAdvanced = set.type == ExerciseSetType.DROP_SET || set.type == ExerciseSetType.CLUSTER_SET
        val isTarget = targetSetIndex == null || targetSetIndex == index
        if (isAdvanced && isTarget) index to set else null
    }
    if (advancedSets.isEmpty()) return

    val minRep by workoutViewModel.minRep.collectAsState()
    val maxRep by workoutViewModel.maxRep.collectAsState()
    val stepRep by workoutViewModel.stepRep.collectAsState()
    val minWeight by workoutViewModel.minWeight.collectAsState()
    val maxWeight by workoutViewModel.maxWeight.collectAsState()
    val stepWeight by workoutViewModel.stepWeight.collectAsState()

    var editTarget by remember { mutableStateOf<AdvancedSetEditTarget?>(null) }

    fun updateSet(setIndex: Int, newSet: ExerciseSet) {
        onSetTechniqueClick?.invoke(exerciseIndex, setIndex, newSet)
            ?: workoutViewModel.updateActiveWorkoutSet(exerciseIndex, setIndex, newSet)
    }

    fun normalizeDropSet(set: ExerciseSet, parts: List<DropSetPart>): ExerciseSet {
        val first = parts.firstOrNull()
        return set.copy(
            rep = parts.sumOf { it.rep },
            weight = first?.weight ?: set.weight,
            dropSetParts = parts
        )
    }

    fun clusterPartsFromSet(set: ExerciseSet): List<ClusterSetPart> {
        if (set.clusterSetParts.isNotEmpty()) return set.clusterSetParts
        val cluster = set.clusterSetData ?: ClusterSetData(
            weight = set.weight,
            clusterCount = 1,
            repsPerCluster = set.rep.coerceAtLeast(1),
            restBetweenClustersSec = 20
        )
        return List(cluster.clusterCount.coerceAtLeast(1)) {
            ClusterSetPart(
                weight = cluster.weight,
                rep = cluster.repsPerCluster.coerceAtLeast(1),
                status = SetStatus.NONE
            )
        }
    }

    fun normalizeClusterSet(set: ExerciseSet, parts: List<ClusterSetPart>): ExerciseSet {
        val safeParts = parts.ifEmpty {
            listOf(ClusterSetPart(weight = set.weight, rep = set.rep.coerceAtLeast(1), status = SetStatus.NONE))
        }
        val currentRest = set.clusterSetData?.restBetweenClustersSec ?: 20
        val first = safeParts.first()
        return set.copy(
            rep = safeParts.sumOf { it.rep },
            weight = first.weight,
            clusterSetParts = safeParts,
            clusterSetData = ClusterSetData(
                weight = first.weight,
                clusterCount = safeParts.size,
                repsPerCluster = first.rep.coerceAtLeast(1),
                restBetweenClustersSec = currentRest
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        advancedSets.forEach { (setIndex, set) ->
            when (set.type) {
                ExerciseSetType.DROP_SET -> DropSetInlineEditor(
                    set = set,
                    onEditWeight = { partIndex, value ->
                        editTarget = AdvancedSetEditTarget.DropWeight(setIndex, partIndex, value)
                    },
                    onEditReps = { partIndex, value ->
                        editTarget = AdvancedSetEditTarget.DropReps(setIndex, partIndex, value)
                    },
                    onStatusChange = { partIndex, status ->
                        val parts = set.dropSetParts.toMutableList()
                        if (partIndex in parts.indices) {
                            parts[partIndex] = parts[partIndex].copy(status = status)
                            updateSet(setIndex, normalizeDropSet(set, parts))
                        }
                    },
                    onAddPart = {
                        val last = set.dropSetParts.lastOrNull() ?: DropSetPart(weight = set.weight, rep = set.rep.coerceAtLeast(1), status = SetStatus.NONE)
                        val next = DropSetPart(
                            weight = (last.weight * 0.8f).coerceAtLeast(minWeight),
                            rep = last.rep.coerceAtLeast(minRep.coerceAtLeast(1)),
                            status = SetStatus.NONE
                        )
                        updateSet(setIndex, normalizeDropSet(set, set.dropSetParts + next))
                    },
                    onRemovePart = { partIndex ->
                        if (set.dropSetParts.size > 1) {
                            val updated = set.dropSetParts.toMutableList().apply { removeAt(partIndex) }
                            updateSet(setIndex, normalizeDropSet(set, updated))
                        }
                    }
                )

                ExerciseSetType.CLUSTER_SET -> {
                    val clusterParts = clusterPartsFromSet(set)
                    ClusterSetInlineEditor(
                        set = set,
                        clusterParts = clusterParts,
                        onEditWeight = { clusterIndex, value -> editTarget = AdvancedSetEditTarget.ClusterWeight(setIndex, clusterIndex, value) },
                        onEditReps = { clusterIndex, value -> editTarget = AdvancedSetEditTarget.ClusterReps(setIndex, clusterIndex, value) },
                        onEditRest = { seconds -> editTarget = AdvancedSetEditTarget.ClusterRest(setIndex, seconds) },
                        onStatusChange = { clusterIndex, status ->
                            val parts = clusterParts.toMutableList()
                            if (clusterIndex in parts.indices) {
                                parts[clusterIndex] = parts[clusterIndex].copy(status = status)
                                updateSet(setIndex, normalizeClusterSet(set, parts))
                            }
                        },
                        onAddCluster = {
                            val last = clusterParts.lastOrNull() ?: ClusterSetPart(weight = set.weight, rep = set.rep.coerceAtLeast(1), status = SetStatus.NONE)
                            val next = last.copy(status = SetStatus.NONE)
                            updateSet(setIndex, normalizeClusterSet(set, clusterParts + next))
                        },
                        onRemoveCluster = { clusterIndex ->
                            if (clusterParts.size > 1) {
                                val updated = clusterParts.toMutableList().apply { removeAt(clusterIndex) }
                                updateSet(setIndex, normalizeClusterSet(set, updated))
                            }
                        }
                    )
                }

                ExerciseSetType.NORMAL -> Unit
            }
        }
    }

    when (val target = editTarget) {
        is AdvancedSetEditTarget.DropWeight -> WeightDialog(
            showDialog = true,
            initialWeight = target.value,
            minWeight = minWeight,
            maxWeight = maxWeight,
            stepWeight = stepWeight,
            onDismiss = { editTarget = null },
            onSave = { newWeight ->
                val set = exerciseSets.getOrNull(target.setIndex)
                if (set != null) {
                    val parts = set.dropSetParts.toMutableList()
                    if (target.partIndex in parts.indices) {
                        parts[target.partIndex] = parts[target.partIndex].copy(weight = newWeight)
                        updateSet(target.setIndex, normalizeDropSet(set, parts))
                    }
                }
                editTarget = null
            }
        )

        is AdvancedSetEditTarget.DropReps -> RepsDialog(
            showDialog = true,
            initialRep = target.value,
            minRep = minRep,
            maxRep = maxRep,
            stepRep = stepRep,
            onDismiss = { editTarget = null },
            onSave = { newRep ->
                val set = exerciseSets.getOrNull(target.setIndex)
                if (set != null) {
                    val parts = set.dropSetParts.toMutableList()
                    if (target.partIndex in parts.indices) {
                        parts[target.partIndex] = parts[target.partIndex].copy(rep = newRep)
                        updateSet(target.setIndex, normalizeDropSet(set, parts))
                    }
                }
                editTarget = null
            }
        )


        is AdvancedSetEditTarget.ClusterWeight -> WeightDialog(
            showDialog = true,
            initialWeight = target.value,
            minWeight = minWeight,
            maxWeight = maxWeight,
            stepWeight = stepWeight,
            onDismiss = { editTarget = null },
            onSave = { newWeight ->
                val set = exerciseSets.getOrNull(target.setIndex)
                if (set != null) {
                    val parts = clusterPartsFromSet(set).toMutableList()
                    if (target.clusterIndex in parts.indices) {
                        parts[target.clusterIndex] = parts[target.clusterIndex].copy(weight = newWeight)
                        updateSet(target.setIndex, normalizeClusterSet(set, parts))
                    }
                }
                editTarget = null
            }
        )

        is AdvancedSetEditTarget.ClusterReps -> RepsDialog(
            showDialog = true,
            initialRep = target.value,
            minRep = minRep,
            maxRep = maxRep,
            stepRep = stepRep,
            onDismiss = { editTarget = null },
            onSave = { newReps ->
                val set = exerciseSets.getOrNull(target.setIndex)
                if (set != null) {
                    val parts = clusterPartsFromSet(set).toMutableList()
                    if (target.clusterIndex in parts.indices) {
                        parts[target.clusterIndex] = parts[target.clusterIndex].copy(rep = newReps)
                        updateSet(target.setIndex, normalizeClusterSet(set, parts))
                    }
                }
                editTarget = null
            }
        )


        is AdvancedSetEditTarget.ClusterRest -> TimeMetricDialog(
            showDialog = true,
            title = stringResource(R.string.edit_cluster_rest),
            initialValueMinutes = target.valueSeconds / 60f,
            minValueMinutes = 0f,
            maxValueMinutes = 5f,
            stepValueMinutes = 1f / 60f,
            onDismiss = { editTarget = null },
            onSave = { newMinutes ->
                val set = exerciseSets.getOrNull(target.setIndex)
                if (set != null) {
                    val cluster = set.clusterSetData ?: ClusterSetData()
                    val seconds = (newMinutes * 60f).toInt().coerceAtLeast(0)
                    updateSet(target.setIndex, set.copy(clusterSetData = cluster.copy(restBetweenClustersSec = seconds)))
                }
                editTarget = null
            }
        )

        null -> Unit
    }
}

@Composable
private fun DropSetInlineEditor(
    set: ExerciseSet,
    onEditWeight: (Int, Float) -> Unit,
    onEditReps: (Int, Int) -> Unit,
    onStatusChange: (Int, SetStatus) -> Unit,
    onAddPart: () -> Unit,
    onRemovePart: (Int) -> Unit
) {
    AdvancedSetContainer(
        title = stringResource(R.string.drop_set),
        iconRes = R.drawable.ic_drop_set
    ) {
        AdvancedSetHeader(
            first = stringResource(R.string.part),
            second = stringResource(R.string.rep),
            third = stringResource(R.string.Weight),
            fourth = stringResource(R.string.status)
        )

        set.dropSetParts.forEachIndexed { partIndex, part ->
            AdvancedSetRow(
                firstValue = (partIndex + 1).toString(),
                repValue = part.rep.toString(),
                weightValue = "%.1f".format(part.weight),
                status = part.status,
                onRepClick = { onEditReps(partIndex, part.rep) },
                onWeightClick = { onEditWeight(partIndex, part.weight) },
                onStatusChange = { onStatusChange(partIndex, it) },
                onDelete = { onRemovePart(partIndex) },
                deleteEnabled = set.dropSetParts.size > 1
            )
        }

        AdvancedAddButton(
            text = stringResource(R.string.add_drop_set_part),
            onClick = onAddPart
        )
    }
}

@Composable
private fun ClusterSetInlineEditor(
    set: ExerciseSet,
    clusterParts: List<ClusterSetPart>,
    onEditWeight: (Int, Float) -> Unit,
    onEditReps: (Int, Int) -> Unit,
    onEditRest: (Int) -> Unit,
    onStatusChange: (Int, SetStatus) -> Unit,
    onAddCluster: () -> Unit,
    onRemoveCluster: (Int) -> Unit
) {
    val rest = set.clusterSetData?.restBetweenClustersSec ?: 20
    AdvancedSetContainer(
        title = stringResource(R.string.cluster_set),
        iconRes = R.drawable.ic_cluster_set
    ) {
        AdvancedSetCell(
            title = stringResource(R.string.cluster_rest),
            value = stringResource(R.string.seconds_short_value, rest),
            modifier = Modifier.fillMaxWidth(),
            onClick = { onEditRest(rest) }
        )

        AdvancedSetHeader(
            first = stringResource(R.string.part),
            second = stringResource(R.string.rep),
            third = stringResource(R.string.Weight),
            fourth = stringResource(R.string.status)
        )

        clusterParts.forEachIndexed { clusterIndex, cluster ->
            AdvancedSetRow(
                firstValue = (clusterIndex + 1).toString(),
                repValue = cluster.rep.toString(),
                weightValue = "%.1f".format(cluster.weight),
                status = cluster.status,
                onRepClick = { onEditReps(clusterIndex, cluster.rep) },
                onWeightClick = { onEditWeight(clusterIndex, cluster.weight) },
                onStatusChange = { onStatusChange(clusterIndex, it) },
                onDelete = { onRemoveCluster(clusterIndex) },
                deleteEnabled = clusterParts.size > 1
            )
        }

        AdvancedAddButton(
            text = stringResource(R.string.add_cluster),
            onClick = onAddCluster
        )
    }
}

@Composable
private fun AdvancedSetHeader(
    first: String,
    second: String,
    third: String,
    fourth: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdvancedHeaderText(text = first, modifier = Modifier.width(54.dp))
        AdvancedHeaderText(text = second, modifier = Modifier.weight(1f))
        AdvancedHeaderText(text = third, modifier = Modifier.weight(1f))
        AdvancedHeaderText(text = fourth, modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun AdvancedHeaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AdvancedSetRow(
    firstValue: String,
    repValue: String,
    weightValue: String,
    status: SetStatus,
    onRepClick: () -> Unit,
    onWeightClick: () -> Unit,
    onStatusChange: (SetStatus) -> Unit,
    onDelete: () -> Unit,
    deleteEnabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdvancedSetPlainCell(
            value = firstValue,
            modifier = Modifier.width(54.dp)
        )
        AdvancedSetValueCell(
            value = repValue,
            modifier = Modifier.weight(1f),
            onClick = onRepClick
        )
        AdvancedSetValueCell(
            value = weightValue,
            modifier = Modifier.weight(1f),
            onClick = onWeightClick
        )
        AdvancedSetStatusCell(
            status = status,
            onStatusChange = onStatusChange,
            onDelete = onDelete,
            deleteEnabled = deleteEnabled,
            modifier = Modifier.width(64.dp)
        )
    }
}

@Composable
private fun AdvancedSetPlainCell(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedSetValueCell(
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)?
) {
    val clickableModifier = if (onClick != null) Modifier.clickable { onClick() } else Modifier
    Box(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .then(clickableModifier)
            .padding(horizontal = 0.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = if (value.isLongTimerCell()) {
                MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedSetStatusCell(
    status: SetStatus,
    onStatusChange: (SetStatus) -> Unit,
    onDelete: () -> Unit,
    deleteEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(40.dp)
                .fillMaxWidth()
                .clickable { showStatusMenu = true },
            contentAlignment = Alignment.Center
        ) {
            StatusIcon(status = status)
        }

        DropdownMenu(
            expanded = showStatusMenu,
            onDismissRequest = { showStatusMenu = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_warm_up, text = stringResource(R.string.warm_up)) },
                onClick = {
                    onStatusChange(SetStatus.WARMUP)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_close, text = stringResource(R.string.failed)) },
                onClick = {
                    onStatusChange(SetStatus.FAILED)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                text = { StatusMenuRow(iconRes = R.drawable.ic_complete, text = stringResource(R.string.completed)) },
                onClick = {
                    onStatusChange(SetStatus.COMPLETED)
                    showStatusMenu = false
                }
            )
            DropdownMenuItem(
                enabled = deleteEnabled,
                text = { StatusMenuRow(iconRes = R.drawable.ic_delete, text = stringResource(R.string.delete_part)) },
                onClick = {
                    if (deleteEnabled) onDelete()
                    showStatusMenu = false
                }
            )
        }
    }
}

@Composable
private fun AdvancedAddButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.clickable { onClick() }
        )
    }
}

@Composable
private fun AdvancedSetContainer(
    title: String,
    iconRes: Int,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TechniqueBadge(text = title, iconRes = iconRes)
            content()
        }
    }
}

@Composable
private fun AdvancedSetCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TechniqueBadge(
    text: String,
    iconRes: Int? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = text,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusMenuRow(
    iconRes: Int,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}


private fun String.isLongTimerCell(): Boolean {
    return contains(":") && length >= 6
}
