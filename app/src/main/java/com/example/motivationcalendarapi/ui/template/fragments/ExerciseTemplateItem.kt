package com.example.motivationcalendarapi.ui.template.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.ui.fragments.StatusIcon
import com.example.motivationcalendarapi.ui.workout.fragments.NoteBottomSheet
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseTemplateItem(
    index: Int,
    exercise: ExtendedExercise,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onAddSetClick: (Int) -> Unit,
    onRepClick: (Int, Int) -> Unit,
    onWeightClick: (Int, Int) -> Unit,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    onInclineClick: (Int, Int) -> Unit,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    navController: NavController,
    templateId: String,
    onDeleteSet: (String, Int, Int) -> Unit,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel,
    lang: String
) {
    val isExpanded = remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }

    val notesMap by workoutViewModel.notesUpdates.collectAsState()
    val currentNote = notesMap[exercise.exercise.id] ?: exercise.exercise.note
    var localNote by remember { mutableStateOf(currentNote) }

    val cardType = exercise.exercise.getCardType(lang)

    LaunchedEffect(currentNote) {
        localNote = currentNote
    }

    val updatedExercise = remember(exercise, localNote) {
        exercise.copy(exercise = exercise.exercise.copy(note = localNote))
    }

    Card(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .padding(horizontal = 16.dp)
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
                        color = MaterialTheme.colorScheme.onSurface
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
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        DropdownMenuItem(
                            text = {
                                TemplateMenuRow(
                                    iconRes = R.drawable.ic_delete,
                                    text = stringResource(R.string.delete_exercise)
                                )
                            },
                            onClick = {
                                onDelete()
                                showMenu = false
                            }
                        )

                        DropdownMenuItem(
                            text = {
                                TemplateMenuRow(
                                    iconRes = R.drawable.ic_info,
                                    text = stringResource(R.string.exercise_info)
                                )
                            },
                            onClick = {
                                navController.navigate("exercise_detail/${exercise.exercise.id}")
                                showMenu = false
                            }
                        )
                    }
                }
            }

            when (cardType) {
                ExerciseCardType.STRENGTH -> {
                    StrengthSetsTable(
                        index = index,
                        templateId = templateId,
                        exerciseSets = exerciseSets,
                        onRepClick = onRepClick,
                        onWeightClick = onWeightClick,
                        onStatusClick = onStatusClick,
                        onDeleteSet = onDeleteSet
                    )
                }

                ExerciseCardType.BIKE -> {
                    BikeSetsTable(
                        index = index,
                        templateId = templateId,
                        exerciseSets = exerciseSets,
                        onTimeClick = onTimeClick,
                        onResistanceClick = onResistanceClick,
                        onStatusClick = onStatusClick,
                        onDeleteSet = onDeleteSet
                    )
                }

                ExerciseCardType.TREADMILL -> {
                    TreadmillSetsTable(
                        index = index,
                        templateId = templateId,
                        exerciseSets = exerciseSets,
                        onTimeClick = onTimeClick,
                        onResistanceClick = onResistanceClick,
                        onInclineClick = onInclineClick,
                        onStatusClick = onStatusClick,
                        onDeleteSet = onDeleteSet
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_up),
                    contentDescription = stringResource(R.string.move_up),
                    tint = if (canMoveUp) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
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
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = stringResource(R.string.move_down),
                    tint = if (canMoveDown) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    }
                )
            }
        }
    }

    NoteBottomSheet(
        showBottomSheet = showNoteDialog,
        exercise = updatedExercise,
        onDismiss = {
            showNoteDialog = false
            workoutViewModel.updateExerciseNote(updatedExercise.exercise.id, localNote)
        },
        onSaveNote = { newNote ->
            localNote = newNote
            workoutViewModel.updateExerciseNote(updatedExercise.exercise.id, newNote)
        },
        lang = lang
    )
}

@Composable
private fun TemplateMenuRow(
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

@Composable
private fun StrengthSetsTable(
    index: Int,
    templateId: String,
    exerciseSets: List<ExerciseSet>,
    onRepClick: (Int, Int) -> Unit,
    onWeightClick: (Int, Int) -> Unit,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    onDeleteSet: (String, Int, Int) -> Unit
) {
    SetsTable(
        index = index,
        templateId = templateId,
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
        onStatusClick = onStatusClick,
        onDeleteSet = onDeleteSet
    )
}

@Composable
private fun BikeSetsTable(
    index: Int,
    templateId: String,
    exerciseSets: List<ExerciseSet>,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    onDeleteSet: (String, Int, Int) -> Unit
) {
    SetsTable(
        index = index,
        templateId = templateId,
        exerciseSets = exerciseSets,
        columns = listOf(
            TableColumn(
                title = stringResource(R.string.time_minutes),
                value = { "%.1f".format(it.time) },
                onClick = onTimeClick
            ),
            TableColumn(
                title = stringResource(R.string.resistance_level),
                value = { "%.1f".format(it.resistance) },
                onClick = onResistanceClick
            )
        ),
        onStatusClick = onStatusClick,
        onDeleteSet = onDeleteSet
    )
}

@Composable
private fun TreadmillSetsTable(
    index: Int,
    templateId: String,
    exerciseSets: List<ExerciseSet>,
    onTimeClick: (Int, Int) -> Unit,
    onResistanceClick: (Int, Int) -> Unit,
    onInclineClick: (Int, Int) -> Unit,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    onDeleteSet: (String, Int, Int) -> Unit
) {
    SetsTable(
        index = index,
        templateId = templateId,
        exerciseSets = exerciseSets,
        columns = listOf(
            TableColumn(
                title = stringResource(R.string.time_minutes),
                value = { "%.1f".format(it.time) },
                onClick = onTimeClick
            ),
            TableColumn(
                title = stringResource(R.string.resistance_level),
                value = { "%.1f".format(it.resistance) },
                onClick = onResistanceClick
            ),
            TableColumn(
                title = stringResource(R.string.incline_percent),
                value = { "%.1f".format(it.incline) },
                onClick = onInclineClick
            )
        ),
        onStatusClick = onStatusClick,
        onDeleteSet = onDeleteSet
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
    templateId: String,
    exerciseSets: List<ExerciseSet>,
    columns: List<TableColumn>,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    onDeleteSet: (String, Int, Int) -> Unit
) {
    if (exerciseSets.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.set),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            exerciseSets.forEachIndexed { setIndex, _ ->
                Box(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .size(60.dp, 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${setIndex + 1}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        columns.forEach { column ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = column.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                exerciseSets.forEachIndexed { setIndex, set ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .padding(bottom = 8.dp)
                            .size(60.dp, 40.dp)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                shape = MaterialTheme.shapes.small
                            )
                            .clickable {
                                column.onClick(index, setIndex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = column.value(set),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        StatusColumn(
            exerciseIndex = index,
            templateId = templateId,
            exerciseSets = exerciseSets,
            onStatusClick = onStatusClick,
            onDeleteSet = onDeleteSet
        )
    }
}

@Composable
private fun StatusColumn(
    exerciseIndex: Int,
    templateId: String,
    exerciseSets: List<ExerciseSet>,
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    onDeleteSet: (String, Int, Int) -> Unit
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
                            onStatusClick(
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
                            onStatusClick(
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
                            onStatusClick(
                                exerciseIndex,
                                setIndex,
                                SetStatus.COMPLETED
                            )
                            showStatusMenu = false
                        }
                    )

                    if (exerciseSets.size > 1) {
                        DropdownMenuItem(
                            text = {
                                StatusMenuRow(
                                    iconRes = R.drawable.ic_delete,
                                    text = stringResource(R.string.delete_set)
                                )
                            },
                            onClick = {
                                onDeleteSet(
                                    templateId,
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