package com.example.motivationcalendarapi.ui.workout.detail.fragments

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.ui.fragments.StatusIcon
import com.example.motivationcalendarapi.ui.workout.fragments.NoteBottomSheet
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel

@Composable
fun ExerciseCardHistory(
    index: Int,
    exercise: ExtendedExercise,
    exerciseSets: List<ExerciseSet>,
    workoutViewModel: WorkoutViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isExpanded = remember { mutableStateOf(false) }
    val notesMap by workoutViewModel.notesUpdates.collectAsState()
    val currentNote = notesMap[exercise.exercise.id] ?: exercise.exercise.note
    var localNote by remember { mutableStateOf(currentNote) }
    val updatedExercise = remember(exercise, localNote) {
        exercise.copy(exercise = exercise.exercise.copy(note = localNote))
    }
    var showNoteDialog by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    val maxSet = remember(exercise.exercise.id) {
        workoutViewModel.findMaxSetForExercise(exercise.exercise.id)
    }

    Card(
        modifier = modifier
            .padding(vertical = 8.dp)
//            .border(
//                width = 1.dp,
//                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
//                shape = MaterialTheme.shapes.medium
//            ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        )
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
                Box(modifier = Modifier.fillMaxHeight()) {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_dots),
                            contentDescription = stringResource(R.string.info),
                            modifier = Modifier.size(24.dp),
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
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_info),
                                        contentDescription = stringResource(R.string.exercise_info),
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
                            },
                            onClick = {
                                navController.navigate("exercise_detail/${exercise.exercise.id}")
                                showMenu = false
                            }
                        )

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
                                            text = stringResource(R.string.max_set_format, maxSet.weight, maxSet.rep),
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

            Column(verticalArrangement = Arrangement.Center) {
                if (exerciseSets.isNotEmpty()) {
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

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.rep),
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
                                text = stringResource(R.string.Weight),
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
                                text = stringResource(R.string.status),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            exerciseSets.forEach { set ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .padding(bottom = 8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(60.dp, 40.dp)
                                            .fillMaxHeight(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        StatusIcon(status = set.status)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.note),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .clickable { showNoteDialog = true },
                    textAlign = TextAlign.Center
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
        }
    )
}
