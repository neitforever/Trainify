package com.example.motivationcalendarapi.ui.template.fragments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.ui.fragments.StatusIcon
import com.example.motivationcalendarapi.ui.workout.fragments.NoteBottomSheet
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

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
    onStatusClick: (Int, Int, SetStatus) -> Unit,
    navController: NavController,
    templateId: String,
    onDeleteSet: (String, Int, Int) -> Unit,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel
) {
    var showMenu by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    val isExpanded = remember { mutableStateOf(false) }


    val notesMap by workoutViewModel.notesUpdates.collectAsState()
    val currentNote = notesMap[exercise.exercise.id] ?: exercise.exercise.note
    var localNote by remember { mutableStateOf(currentNote) }

    LaunchedEffect(currentNote) {
        localNote = currentNote
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

            // верхняя
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, start = 12.dp, end = 8.dp)
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
                        text = exercise.exercise.name.replaceFirstChar { it.uppercase() },
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
                        .padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterEnd)
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
                            onDelete()
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

            //подходы
            Column(verticalArrangement = Arrangement.Center) {
                if (exerciseSets.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Set",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            exerciseSets.forEachIndexed { setIndex, set ->
                                Box(
                                    modifier = Modifier
                                        .padding(bottom = 8.dp)
                                        .size(60.dp, 40.dp)
                                        .fillMaxHeight()
                                ) {
                                    Text(
                                        text = "${setIndex + 1}",
                                        modifier = Modifier.align(Alignment.Center),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Rep",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            exerciseSets.forEachIndexed { setIndex, set ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp)
                                        .size(60.dp, 40.dp)
                                        .fillMaxHeight()
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .clickable {}
                                        .clickable { onRepClick(index, setIndex) }
                                ) {
                                    Text(
                                        text = "${set.rep}",
                                        modifier = Modifier.align(Alignment.Center),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Weight",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            exerciseSets.forEachIndexed { setIndex, set ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp)
                                        .size(60.dp, 40.dp)
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
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Status",
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
                                            .fillMaxHeight()
                                            .clickable { showStatusMenu = true },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        StatusIcon(status = set.status)
                                    }
                                    DropdownMenu(
                                        expanded = showStatusMenu,
                                        onDismissRequest = { showStatusMenu = false },
                                        modifier = Modifier.background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                        )
                                    ) {
                                        DropdownMenuItem(
                                            text = {
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
                                            },
                                            onClick = {
                                                onStatusClick(index, setIndex, SetStatus.WARMUP)
                                                showStatusMenu = false
                                            }
                                        )
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
                                            onStatusClick(index, setIndex, SetStatus.FAILED)
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
                                            onStatusClick(index, setIndex, SetStatus.COMPLETED)
                                            showStatusMenu = false
                                        })
                                        if (exerciseSets.size > 1) {
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(R.drawable.ic_delete),
                                                            contentDescription = "Delete Set",
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
                                                },
                                                onClick = {
                                                    onDeleteSet(templateId, index, setIndex)
                                                    showStatusMenu = false
                                                }
                                            )
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }




        }


        // нижняя
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .padding(horizontal = 8.dp,vertical = 8.dp)
                .fillMaxWidth(),
        ) {
            IconButton(
                onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_up),
                    contentDescription = "Move Up",
                    tint = if (canMoveUp) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Add Set",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.clickable { onAddSetClick(index) },
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Note",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable { showNoteDialog = true }
                        .fillMaxWidth(0.5f),
                    textAlign = TextAlign.Center
                )
            }


            IconButton(
                onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_down),
                    contentDescription = "Move Down",
                    tint = if (canMoveDown) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }

    }

    NoteBottomSheet(
        showBottomSheet = showNoteDialog,
        exercise = exercise.copy(exercise = exercise.exercise.copy(note = localNote)),
        onDismiss = {
            showNoteDialog = false
            workoutViewModel.updateExerciseNote(exercise.exercise.id, localNote)
        },
        onSaveNote = { newNote ->
            localNote = newNote
            workoutViewModel.updateExerciseNote(exercise.exercise.id, newNote)
        }
    )

}

