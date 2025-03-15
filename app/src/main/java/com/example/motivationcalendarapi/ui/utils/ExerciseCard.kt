package com.motivationcalendar.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.ui.utils.NoteBottomSheet
import com.example.motivationcalendarapi.ui.utils.StatusIcon
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
    onStatusClick: (Int, Int) -> Unit,
    workoutViewModel: WorkoutViewModel,
    navController: NavController
) {
    val isExpanded = remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }


    val notesMap by workoutViewModel.notesUpdates.collectAsState()
    val currentNote = notesMap[exercise.exercise.id] ?: exercise.exercise.note
    var localNote by remember { mutableStateOf(currentNote) }

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
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row {
                    Text(
                        text = "${index + 1}.",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    Text(
                        text = exercise.exercise.name,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .clickable { isExpanded.value = !isExpanded.value },
                        maxLines = if (isExpanded.value) 1 else Int.MAX_VALUE,
                        overflow = if (isExpanded.value) TextOverflow.Ellipsis else TextOverflow.Clip,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true }, modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dots),
                            contentDescription = "Menu",
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
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete Exercise",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }, onClick = {
                            workoutViewModel.removeExercise(index)
                            showMenu = false
                        })
                        DropdownMenuItem(text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "Info",
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Exercise Info",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }, onClick = {
                            navController.navigate("exercise_detail/${exercise.exercise.id}")
                            showMenu = false
                        })

                    }
                }
            }

            if (exerciseSets.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Set",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Rep",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Weight",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Status",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }

                    exerciseSets.forEachIndexed { setIndex, set ->
                        var showStatusMenu by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = if (setIndex == exerciseSets.lastIndex) 12.dp else 8.dp)
                                .height(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .fillMaxHeight()
                                    .wrapContentWidth()
                            ) {
                                Text(
                                    text = "${setIndex + 1}",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(horizontal = 8.dp)
                                    .fillMaxHeight()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable { onRepClick(index, setIndex) }
                            ) {
                                Text(
                                    text = "${set.rep}",
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(0.5f)
                                    .padding(horizontal = 8.dp)
                                    .fillMaxHeight()
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .clickable { onWeightClick(index, setIndex) }
                            ) {
                                Text(
                                    text = "%.1f".format(set.weight),
                                    modifier = Modifier.align(Alignment.Center),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp, end = 8.dp)
                                    .weight(0.5f)
                                    .fillMaxHeight()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { showStatusMenu = true }
                                        .align(Alignment.Center)
                                ) {
                                    StatusIcon(
                                        status = set.status,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showStatusMenu,
                                    onDismissRequest = { showStatusMenu = false },
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
                                                painter = painterResource(R.drawable.ic_warm_up),
                                                contentDescription = "Warm-up",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Warm-up",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }, onClick = {
                                        workoutViewModel.updateSetStatus(
                                            index, setIndex, SetStatus.WARMUP
                                        )
                                        showStatusMenu = false
                                    })
                                    DropdownMenuItem(text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_close),
                                                contentDescription = "Failed",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Failed",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }, onClick = {
                                        workoutViewModel.updateSetStatus(
                                            index, setIndex, SetStatus.FAILED
                                        )
                                        showStatusMenu = false
                                    })
                                    DropdownMenuItem(text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_complete),
                                                contentDescription = "Completed",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Completed",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }, onClick = {
                                        workoutViewModel.updateSetStatus(
                                            index, setIndex, SetStatus.COMPLETED
                                        )
                                        showStatusMenu = false
                                    })
                                    DropdownMenuItem(text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete),
                                                contentDescription = "Delete set",
                                                modifier = Modifier.size(24.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Delete Set",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }, onClick = {
                                        workoutViewModel.removeExerciseSet(index, setIndex)
                                        showStatusMenu = false
                                    })
                                }
                            }
                        }
                    }
                }
            }

            Text(text = "Add Set",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddSetClick(index) }
                    .padding(8.dp),
                textAlign = TextAlign.Center)
            Text(text = "Note",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showNoteDialog = true }
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center)
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
        }
    )

}

