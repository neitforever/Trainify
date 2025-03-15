package com.motivationcalendar.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.utils.dialogs.EndWorkoutDialog
import com.example.motivationcalendarapi.ui.utils.ExerciseSelectionBottomSheet
import com.example.motivationcalendarapi.ui.utils.TimerBottomSheet
import com.example.motivationcalendarapi.ui.utils.TotalWeightAndTimeRow
import com.example.motivationcalendarapi.ui.utils.WorkoutNameTextField
import com.example.motivationcalendarapi.ui.utils.dialogs.AutoDismissDialog
import com.example.motivationcalendarapi.ui.utils.dialogs.ExistWorkoutDialog
import com.example.motivationcalendarapi.ui.utils.dialogs.TimerCompleteDialog
import com.example.motivationcalendarapi.ui.utils.dialogs.WarmupDialog
import com.example.motivationcalendarapi.ui.utils.dialogs.WeightDialog
import kotlinx.coroutines.delay


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

    val totalKg by workoutViewModel.totalKg.collectAsState()
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
    val newSet = remember { mutableStateOf(ExerciseSet(rep = 0, weight = 0f)) }

    val showOverwriteDialog by workoutViewModel.showOverwriteDialog.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val showEndWorkoutDialog = remember { mutableStateOf(false) }
    val showPauseDialog = remember { mutableStateOf(false) }
    val isWorkoutPaused = remember { derivedStateOf { timerRunning.not() } }

    val warmupTimeState by workoutViewModel.warmupTime.collectAsState()

    var showWarmupBottomSheet by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    val warmupTime by workoutViewModel.warmupTime.collectAsState()
    var currentTime by remember(warmupTime) { mutableIntStateOf(warmupTime) }
    var isTimerRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var isMenuExpanded by remember { mutableStateOf(false) }

    var showValidationDialog by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf("") }


    var showTimerCompleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning, currentTime) {
        if (isTimerRunning && currentTime > 0) {
            delay(1000L)
            currentTime--
        } else if (currentTime == 0) {
            isTimerRunning = false
            showTimerCompleteDialog = true
            currentTime = warmupTime

            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        }
    }

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

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .wrapContentSize(Alignment.BottomEnd)
                        .pointerInput(Unit) {
                            detectTapGestures {
                                isMenuExpanded = false
                            }
                        }
                ) {
                    Row{
                        AnimatedVisibility(
                            visible = isMenuExpanded,
                            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
                            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    showWarmupBottomSheet = true
                                    isMenuExpanded = false
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_time),
                                    contentDescription = "Warmup",
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AnimatedVisibility(
                            visible = isMenuExpanded,
                            enter = fadeIn() + expandHorizontally(expandFrom = Alignment.End),
                            exit = fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.End)
                        ) {


                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        showPauseDialog.value = true
                                        if (timerRunning) workoutViewModel.pauseTimer()
                                        else workoutViewModel.resumeTimer()
                                        isMenuExpanded = false
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        painter = if (timerRunning) painterResource(id = R.drawable.ic_pause)
                                        else painterResource(id = R.drawable.ic_play_arrow),
                                        contentDescription = if (timerRunning) "Pause" else "Repeat",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                FloatingActionButton(
                                    onClick = {
                                        showEndWorkoutDialog.value = true
                                        isMenuExpanded = false
                                    },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_stop),
                                        contentDescription = "End",
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                        }

                        FloatingActionButton(
                            onClick = { isMenuExpanded = !isMenuExpanded },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isMenuExpanded) R.drawable.ic_close
                                    else R.drawable.ic_menu
                                ), contentDescription = "Menu", modifier = Modifier.size(36.dp)
                            )
                        }
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
                .pointerInput(Unit) {
                    detectTapGestures { isMenuExpanded = false }
                }
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

                        TotalWeightAndTimeRow(
                            timerValue = timerValue, totalKg = totalKg
                        )

                        ExerciseSelectionBottomSheet(
                            isSheetOpen = isSheetOpen,
                            sheetState = sheetState,
                            exerciseViewModel = exersiceViewModel,
                            workoutViewModel = workoutViewModel
                        )

                        TimerBottomSheet(showSheet = showWarmupBottomSheet,
                            currentTime = currentTime,
                            warmupTime = warmupTime,
                            isTimerRunning = isTimerRunning,
                            onDismiss = { showWarmupBottomSheet = false },
                            onRestartClick = {
                                currentTime = warmupTime
                                isTimerRunning = false
                            },
                            onToggleTimer = { isTimerRunning = !isTimerRunning },
                            onEditTimeClick = { showTimerDialog = true })

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
                                onStatusClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                },
                                onMoveUp = { workoutViewModel.moveExerciseUp(index) },
                                onMoveDown = { workoutViewModel.moveExerciseDown(index) },
                                canMoveUp = index > 0,
                                canMoveDown = index < selectedExercises.size - 1,
                                workoutViewModel = workoutViewModel,
                                navController = navController
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clickable(onClick = {
                                    isSheetOpen.value = true
                                })
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center


                        ) {

                            Text(
                                text = "Add Exercises",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
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


            TimerCompleteDialog(showDialog = showTimerCompleteDialog,
                onDismiss = { showTimerCompleteDialog = false })

            WarmupDialog(showDialog = showTimerDialog,
                warmupTime = warmupTimeState,
                onDismiss = { showTimerDialog = false },
                onConfirm = { newTime ->
                    workoutViewModel.updateWarmupTime(newTime)
                })

            ExistWorkoutDialog(showDialog = showOverwriteDialog,
                onDismiss = { workoutViewModel.dismissOverwriteDialog() },
                onConfirm = { workoutViewModel.confirmOverwrite() })

            PauseWorkoutDialog(
                showDialog = showPauseDialog.value,
                onDismiss = { showPauseDialog.value = false },
                isPaused = isWorkoutPaused.value
            )

            EndWorkoutDialog(
                showDialog = showEndWorkoutDialog.value,
                onDismiss = { showEndWorkoutDialog.value = false },
                onConfirm = {
                    val isNameEmpty = workoutName.isBlank()
                    val hasInvalidSets = selectedExercises.any { ex ->
                        val sets = exerciseSetsMap[selectedExercises.indexOf(ex)] ?: emptyList()
                        sets.any { it.rep <= 0 || it.weight <= 0f }
                    }

                    if (isNameEmpty || hasInvalidSets) {
                        validationMessage = buildString {
                            if (isNameEmpty) append("Workout name cannot be empty.\n")
                            if (hasInvalidSets) append("All sets must have reps and weight filled.")
                        }
                        showValidationDialog = true
                    } else {
                        val updatedExercises = selectedExercises.mapIndexed { index, ex ->
                            ex.copy(sets = exerciseSetsMap[index] ?: emptyList())
                        }
                        workoutViewModel.saveWorkout(updatedExercises)
                        workoutViewModel.resetWorkout()
                        showEndWorkoutDialog.value = false
                    }
                }
            )

            AutoDismissDialog(
                showDialog = showValidationDialog,
                onDismiss = { showValidationDialog = false },
                message = validationMessage
            )

            RepsDialog(showDialog = showRepDialog,
                initialRep = exerciseSetsMap[currentExerciseIndex]?.get(currentSetIndex)?.rep ?: 0,
                onDismiss = { showRepDialog = false },
                onSave = { newRep ->
                    workoutViewModel.updateRep(currentExerciseIndex, currentSetIndex, newRep)
                    showRepDialog = false
                })

            WeightDialog(showDialog = showWeightDialog,
                initialWeight = exerciseSetsMap[currentExerciseIndex]?.get(currentSetIndex)?.weight
                    ?: 0f,
                onDismiss = { showWeightDialog = false },
                onSave = { newWeight ->
                    workoutViewModel.updateWeight(currentExerciseIndex, currentSetIndex, newWeight)
                    showWeightDialog = false
                })


        }
    }
}