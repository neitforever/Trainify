package com.motivationcalendar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import getWeekOfMonth
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.utils.AddExerciseAndTimeRow
import com.example.motivationcalendarapi.ui.utils.dialogs.EndWorkoutDialog
import com.example.motivationcalendarapi.ui.utils.ExerciseSelectionBottomSheet
import com.example.motivationcalendarapi.ui.utils.WorkoutNameTextField
import com.example.motivationcalendarapi.ui.utils.dialogs.ExistWorkoutDialog
import com.example.motivationcalendarapi.ui.utils.dialogs.WeightDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    exersiceViewModel: ExerciseViewModel,
    navController: NavController,
    drawerState: DrawerState
) {
    val workouts by workoutViewModel.allWorkouts.collectAsState()
    val timerValue by workoutViewModel.timerValue.collectAsState()
    val timerRunning by workoutViewModel.timerRunning.collectAsState()
    val isWorkoutStarted by workoutViewModel.isWorkoutStarted.collectAsState()
    val workoutName by workoutViewModel.workoutName.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var showRepDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var currentExerciseIndex by remember { mutableStateOf(0) }
    var currentSetIndex by remember { mutableStateOf(0) }

    val currentWeek = getWeekOfMonth(System.currentTimeMillis())
    val workoutsThisWeek = workouts.count { workout ->
        getWeekOfMonth(workout.timestamp) == currentWeek
    }
    val workoutNumberInWeek = workoutsThisWeek + 1

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSheetOpen = remember { mutableStateOf(false) }


    val selectedExercises by workoutViewModel.selectedExercises.collectAsState()
    val exerciseSetsMap by workoutViewModel.exerciseSetsMap.collectAsState()

    val showSetDialog = remember { mutableStateOf(false) }
    val newSet = remember { mutableStateOf(ExerciseSet(rep = 0, weigth = 0f)) }

    val showOverwriteDialog by workoutViewModel.showOverwriteDialog.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val showEndWorkoutDialog = remember { mutableStateOf(false) }
    val showPauseDialog = remember { mutableStateOf(false) }
    val isWorkoutPaused = remember { derivedStateOf { timerRunning.not() } }

    Scaffold(topBar = {
        TopAppBar(colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ), navigationIcon = {
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        drawerState.open()
                    }
                }, modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open Menu",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }, title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                Text(
                    text = "$workoutNumberInWeek workout/week",
                    style = MaterialTheme.typography.displaySmall,
                    maxLines = 1,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
            }
        }, modifier = Modifier.border(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
            shape = CutCornerShape(4.dp)
        )
        )
    },
        floatingActionButton = {
            if (isWorkoutStarted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    FloatingActionButton(
                        onClick = {
                            showPauseDialog.value = true
                            if (timerRunning) workoutViewModel.pauseTimer()
                            else workoutViewModel.resumeTimer()
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = if (timerRunning) painterResource(id = R.drawable.ic_pause)
                            else painterResource(id = R.drawable.ic_play_arrow),
                            contentDescription = if (timerRunning) "Pause" else "Repeat",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            showEndWorkoutDialog.value = true
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(64.dp)
                    )
                    {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_stop),
                            contentDescription = "End",
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    FloatingActionButton(
                        onClick = {
                            workoutViewModel.checkForExistingWorkout()
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play_arrow),
                            contentDescription = "Start",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            top = paddingValues.calculateTopPadding() + 12.dp,
                            start = 8.dp,
                            end = 8.dp
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isWorkoutStarted) {
                        WorkoutNameTextField(
                            workoutName = workoutName, onValueChange = { newName ->
                                if (newName.length <= 20) {
                                    workoutViewModel.setWorkoutName(newName)
                                }
                            }, keyboardController = keyboardController
                        )

                        AddExerciseAndTimeRow(
                            onAddExerciseClick = { isSheetOpen.value = true },
                            timerValue = timerValue
                        )

                        ExerciseSelectionBottomSheet(
                            isSheetOpen = isSheetOpen,
                            sheetState = sheetState,
                            exerciseViewModel = exersiceViewModel,
                            workoutViewModel = workoutViewModel
                        )

                        selectedExercises.forEachIndexed { index, exercise ->
                            ExerciseCard(
                                index = index,
                                exercise = exercise,
                                exerciseSets = exerciseSetsMap[index] ?: emptyList(),
                                onAddSetClick = { exIndex ->
                                    workoutViewModel.addExerciseSet(exIndex, ExerciseSet(0, 0f))
                                },
                                onRepClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                    showRepDialog = true
                                },
                                onWeightClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                    showWeightDialog = true
                                },
                                workoutViewModel = workoutViewModel,
                                navController = navController
                            )
                        }

                    } else {


//                        Button(
//                            onClick = { workoutViewModel.checkForExistingWorkout() },
//                            border = BorderStroke(
//                                width = 2.dp, MaterialTheme.colorScheme.secondary
//                            ),
//                            modifier = Modifier.fillMaxWidth(),
//                        ) {
//                            Text(
//                                text = "Start Workout",
//                                color = MaterialTheme.colorScheme.onPrimary,
//                                style = MaterialTheme.typography.headlineMedium,
//                            )
//                        }

                    }

                }

            }



            ExistWorkoutDialog(
                showDialog = showOverwriteDialog,
                onDismiss = { workoutViewModel.dismissOverwriteDialog() },
                onConfirm = { workoutViewModel.confirmOverwrite() }
            )

            PauseWorkoutDialog(
                showDialog = showPauseDialog.value,
                onDismiss = { showPauseDialog.value = false },
                isPaused = isWorkoutPaused.value
            )

            EndWorkoutDialog(
                showDialog = showEndWorkoutDialog.value,
                onDismiss = { showEndWorkoutDialog.value = false },
                onConfirm = {
                    val updatedExercises = selectedExercises.mapIndexed { index, ex ->
                        ex.copy(sets = exerciseSetsMap[index] ?: emptyList())
                    }
                    workoutViewModel.saveWorkout(updatedExercises)
                    workoutViewModel.resetWorkout()
                    showEndWorkoutDialog.value = false
                }
            )

            RepsDialog(
                showDialog = showRepDialog,
                initialRep = exerciseSetsMap[currentExerciseIndex]?.get(currentSetIndex)?.rep ?: 0,
                onDismiss = { showRepDialog = false },
                onSave = { newRep ->
                    workoutViewModel.updateRep(currentExerciseIndex, currentSetIndex, newRep)
                    showRepDialog = false
                }
            )

            WeightDialog(
                showDialog = showWeightDialog,
                initialWeight = exerciseSetsMap[currentExerciseIndex]?.get(currentSetIndex)?.weigth
                    ?: 0f,
                onDismiss = { showWeightDialog = false },
                onSave = { newWeight ->
                    workoutViewModel.updateWeight(currentExerciseIndex, currentSetIndex, newWeight)
                    showWeightDialog = false
                }
            )


        }
    }
}