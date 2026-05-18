package com.example.motivationcalendarapi.ui.workout

import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModelFactory
import com.example.motivationcalendarapi.viewmodel.health.HealthConnectViewModel
import com.example.motivationcalendarapi.repositories.health.HealthConnectRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.ui.dialogs.AutoDismissDialog
import com.example.motivationcalendarapi.ui.dialogs.EndWorkoutDialog
import com.example.motivationcalendarapi.ui.dialogs.ExistWorkoutDialog
import com.example.motivationcalendarapi.ui.dialogs.TimerCompleteDialog
import com.example.motivationcalendarapi.ui.dialogs.WarmupDialog
import com.example.motivationcalendarapi.ui.dialogs.WeightDialog
import com.example.motivationcalendarapi.ui.workout.detail.fragments.TotalWeightAndTimeRow
import com.example.motivationcalendarapi.ui.workout.fragments.ExerciseSelectionBottomSheet
import com.example.motivationcalendarapi.ui.workout.fragments.InActiveWorkoutScreen
import com.example.motivationcalendarapi.ui.workout.fragments.TimerBottomSheet
import com.example.motivationcalendarapi.ui.workout.fragments.WorkoutNameTextField
import com.example.motivationcalendarapi.utils.getStartAndEndOfCurrentWeek
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.motivationcalendar.ui.ExerciseCard
import com.motivationcalendar.ui.PauseWorkoutDialog
import com.motivationcalendar.ui.RepsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.ui.dialogs.FloatMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.FinishWorkoutWithEmptySetsDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    workoutViewModel: WorkoutViewModel,
    exerciseViewModel: ExerciseViewModel,
    navController: NavController,
    drawerState: MutableState<DrawerState>,
    lang: String,
    context: Context,
) {
    val workouts by workoutViewModel.allWorkouts.collectAsState()
    val timerValue by workoutViewModel.timerValue.collectAsState()
    val timerRunning by workoutViewModel.timerRunning.collectAsState()
    val isWorkoutStarted by workoutViewModel.isWorkoutStarted.collectAsState()
    val workoutName by workoutViewModel.workoutName.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val healthViewModel: HealthConnectViewModel = viewModel(
        factory = HealthConnectViewModelFactory(HealthConnectRepository(context))
    )
    val healthState by healthViewModel.uiState.collectAsState()
    val totalKg by workoutViewModel.totalKg.collectAsState()
    var showRepDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSetIndex by remember { mutableIntStateOf(0) }

    val (startOfWeek, endOfWeek) = remember { getStartAndEndOfCurrentWeek() }
    val workoutNumberInWeek by remember(workouts) {
        derivedStateOf {
            workouts.count { workout ->
                workout.timestamp in startOfWeek..endOfWeek
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isSheetOpen = remember { mutableStateOf(false) }


    var showTimeDialog by remember { mutableStateOf(false) }
    var showResistanceDialog by remember { mutableStateOf(false) }
    var showInclineDialog by remember { mutableStateOf(false) }

    val minRep by workoutViewModel.minRep.collectAsState()
    val maxRep by workoutViewModel.maxRep.collectAsState()
    val stepRep by workoutViewModel.stepRep.collectAsState()
    val minWeight by workoutViewModel.minWeight.collectAsState()
    val maxWeight by workoutViewModel.maxWeight.collectAsState()
    val stepWeight by workoutViewModel.stepWeight.collectAsState()

    val selectedExercises by workoutViewModel.selectedExercises.collectAsState()
    val exerciseSetsMap by workoutViewModel.exerciseSetsMap.collectAsState()


    val minCardioTime by workoutViewModel.minCardioTime.collectAsState()
    val maxCardioTime by workoutViewModel.maxCardioTime.collectAsState()
    val stepCardioTime by workoutViewModel.stepCardioTime.collectAsState()

    val minResistance by workoutViewModel.minResistance.collectAsState()
    val maxResistance by workoutViewModel.maxResistance.collectAsState()
    val stepResistance by workoutViewModel.stepResistance.collectAsState()

    val minIncline by workoutViewModel.minIncline.collectAsState()
    val maxIncline by workoutViewModel.maxIncline.collectAsState()
    val stepIncline by workoutViewModel.stepIncline.collectAsState()

    val showOverwriteDialog by workoutViewModel.showOverwriteDialog.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val showEndWorkoutDialog = remember { mutableStateOf(false) }
    val showEmptySetsDialog = remember { mutableStateOf(false) }
    val showPauseDialog = remember { mutableStateOf(false) }
    val isWorkoutPaused = remember { derivedStateOf { timerRunning.not() } }

    val warmupTimeState by workoutViewModel.warmupTime.collectAsState()

    var showWarmupBottomSheet by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    val warmupTime by workoutViewModel.warmupTime.collectAsState()
    var currentTime by remember(warmupTime) { mutableIntStateOf(warmupTime) }
    var isTimerRunning by remember { mutableStateOf(false) }

    DisposableEffect(isWorkoutStarted) {
        if (isWorkoutStarted) {
            healthViewModel.startHeartRateUpdates()
        } else {
            healthViewModel.stopHeartRateUpdates()
            healthViewModel.refresh()
        }

        onDispose {
            healthViewModel.stopHeartRateUpdates()
        }
    }

//    @Composable
//    fun rememberCalendarState(initialDate: LocalDate = LocalDate.now()) = remember {
//        CalendarState(initialDate)
//    }




    LaunchedEffect(Unit) {
        drawerState.value.close()
    }
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (vibrator.hasVibrator()) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                }
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ), navigationIcon = {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            drawerState.value.open()
                        }
                    }, modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.open_menu),
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
                        text = pluralStringResource(
                            R.plurals.workouts_per_week,
                            workoutNumberInWeek,
                            workoutNumberInWeek
                        ),
                        style = MaterialTheme.typography.headlineLarge,
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
                    }) {
                Row {
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
                                contentDescription = stringResource(R.string.warmup),
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
                                    contentDescription = if (timerRunning) stringResource(R.string.pause) else stringResource(R.string.repeat),
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
                                    contentDescription = stringResource(R.string.end),
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
                            ), contentDescription = stringResource(R.string.menu), modifier = Modifier.size(36.dp)
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
                        contentDescription = stringResource(R.string.start),
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
                }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (isWorkoutStarted) {
                    Column(
                        modifier = Modifier
                            .padding(
                                top = paddingValues.calculateTopPadding() + 12.dp,
                                start = 8.dp,
                                end = 8.dp
                            )
                            .verticalScroll(rememberScrollState())
                    ) {
                        WorkoutNameTextField(
                            workoutName = workoutName, onValueChange = { newName ->
                                if (newName.length <= 20) {
                                    workoutViewModel.setWorkoutName(newName)
                                }
                            }, keyboardController = keyboardController
                        )

                        TotalWeightAndTimeRow(
                            timerValue = timerValue,
                            totalKg = totalKg,
                            modifier = Modifier.padding(horizontal = 2.dp, vertical = 8.dp)
                        )

                        val currentHeartRate = healthState.currentHeartRate
                        val shouldShowHeartRate = healthState.hasPermissions && healthState.isSmartWatchDetected
                        if (shouldShowHeartRate) {
                            CurrentHeartRateCard(
                                heartRate = currentHeartRate,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp)
                            )
                        }

                        ExerciseSelectionBottomSheet(
                            isSheetOpen = isSheetOpen,
                            sheetState = sheetState,
                            exerciseViewModel = exerciseViewModel,
                            workoutViewModel = workoutViewModel,
                            navController = navController,
                            lang = lang
                        )

                        TimerBottomSheet(
                            showSheet = showWarmupBottomSheet,
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
                                    workoutViewModel.addExerciseSet(exIndex)
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
                                onTimeClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                    showTimeDialog = true
                                },
                                onResistanceClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                    showResistanceDialog = true
                                },
                                onInclineClick = { exIndex, setIndex ->
                                    currentExerciseIndex = exIndex
                                    currentSetIndex = setIndex
                                    showInclineDialog = true
                                },
                                onMoveUp = { workoutViewModel.moveExerciseUp(index) },
                                onMoveDown = { workoutViewModel.moveExerciseDown(index) },
                                canMoveUp = index > 0,
                                canMoveDown = index < selectedExercises.size - 1,
                                workoutViewModel = workoutViewModel,
                                navController = navController,
                                lang = lang
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clickable(onClick = {
                                    if (selectedExercises.isEmpty()) {
                                        isSheetOpen.value = true
                                    } else {
                                        isSheetOpen.value = true
                                    }
                                })
                                .padding(top = 12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (selectedExercises.isEmpty()) stringResource(R.string.add_exercise_or_template)
                                else stringResource(R.string.add_exercise),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
                    }
                }


                if (!isWorkoutStarted) {
                    InActiveWorkoutScreen(
                        workoutViewModel = workoutViewModel,
                        navController = navController,
                        paddingTop = paddingValues.calculateTopPadding()
                    )
                }



            }


            TimerCompleteDialog(
                showDialog = showTimerCompleteDialog,
                onDismiss = { showTimerCompleteDialog = false })

            WarmupDialog(
                showDialog = showTimerDialog,
                warmupTime = warmupTimeState,
                onDismiss = { showTimerDialog = false },
                onConfirm = { newTime ->
                    workoutViewModel.updateWarmupTime(newTime)
                })

            ExistWorkoutDialog(
                showDialog = showOverwriteDialog,
                onDismiss = { workoutViewModel.dismissOverwriteDialog() },
                onConfirm = { workoutViewModel.confirmOverwrite() })

            PauseWorkoutDialog(
                showDialog = showPauseDialog.value,
                onDismiss = { showPauseDialog.value = false },
                isPaused = isWorkoutPaused.value
            )


            val nameEmpty = stringResource(R.string.workout_name_cannot_be_empty)
            val repEmpty = stringResource(R.string.all_sets_must_have_reps)
            EndWorkoutDialog(
                showDialog = showEndWorkoutDialog.value,
                onDismiss = { showEndWorkoutDialog.value = false },
                onConfirm = {
                    val isNameEmpty = workoutName.isBlank()

                    if (isNameEmpty) {
                        validationMessage = buildString {
                            Log.d("messageM1", "name $nameEmpty")
                            if (isNameEmpty) append(nameEmpty)
                        }
                        showValidationDialog = true
                    } else if (workoutViewModel.hasEmptySets(lang)) {
                        showEndWorkoutDialog.value = false
                        showEmptySetsDialog.value = true
                    } else {
                        val updatedExercises = selectedExercises.mapIndexed { index, ex ->
                            ex.copy(sets = exerciseSetsMap[index] ?: emptyList())
                        }
                        coroutineScope.launch {
                            val averageHeartRate = healthViewModel.readAverageHeartRateSince(
                                workoutViewModel.getWorkoutStartTime()
                            )
                            workoutViewModel.saveWorkout(updatedExercises, averageHeartRate)
                            workoutViewModel.resetWorkout()
                            showEndWorkoutDialog.value = false
                        }
                    }
                })


            FinishWorkoutWithEmptySetsDialog(
                showDialog = showEmptySetsDialog.value,
                onDismiss = { showEmptySetsDialog.value = false },
                onConfirm = {
                    val cleanedExercises = workoutViewModel.buildWorkoutExercisesWithoutEmptySets(lang)

                    if (cleanedExercises.isEmpty()) {
                        validationMessage = repEmpty
                        showValidationDialog = true
                        showEmptySetsDialog.value = false
                    } else {
                        coroutineScope.launch {
                            val averageHeartRate = healthViewModel.readAverageHeartRateSince(
                                workoutViewModel.getWorkoutStartTime()
                            )
                            workoutViewModel.saveWorkout(cleanedExercises, averageHeartRate)
                            workoutViewModel.resetWorkout()
                            showEmptySetsDialog.value = false
                        }
                    }
                }
            )

            AutoDismissDialog(
                showDialog = showValidationDialog,
                onDismiss = { showValidationDialog = false },
                message = validationMessage
            )

            RepsDialog(
                showDialog = showRepDialog,
                initialRep = exerciseSetsMap[currentExerciseIndex]?.getOrNull(currentSetIndex)?.rep
                    ?: minRep,
                minRep = minRep,
                maxRep = maxRep,
                stepRep = stepRep,
                onDismiss = { showRepDialog = false },
                onSave = { newRep ->
                    exerciseSetsMap[currentExerciseIndex]?.let { sets ->
                        if (currentSetIndex < sets.size) {
                            workoutViewModel.updateRep(
                                currentExerciseIndex,
                                currentSetIndex,
                                newRep
                            )
                        }
                    }
                    showRepDialog = false
                }
            )

            WeightDialog(
                showDialog = showWeightDialog,
                initialWeight = exerciseSetsMap[currentExerciseIndex]?.getOrNull(currentSetIndex)?.weight ?: minWeight,
                minWeight = minWeight,
                maxWeight = maxWeight,
                stepWeight = stepWeight,
                onDismiss = { showWeightDialog = false },
                onSave = { newWeight ->
                    exerciseSetsMap[currentExerciseIndex]?.let { sets ->
                        if (currentSetIndex < sets.size) {
                            workoutViewModel.updateWeight(currentExerciseIndex, currentSetIndex, newWeight)
                        }
                    }
                    showWeightDialog = false
                }
            )
            FloatMetricDialog(
                showDialog = showTimeDialog,
                title = stringResource(R.string.edit_time),
                label = stringResource(R.string.time_minutes),
                initialValue = exerciseSetsMap[currentExerciseIndex]
                    ?.getOrNull(currentSetIndex)
                    ?.time ?: minCardioTime,
                minValue = minCardioTime,
                maxValue = maxCardioTime,
                stepValue = stepCardioTime,
                onDismiss = { showTimeDialog = false },
                onSave = { newTime ->
                    workoutViewModel.updateTime(
                        currentExerciseIndex,
                        currentSetIndex,
                        newTime
                    )
                    showTimeDialog = false
                }
            )

            FloatMetricDialog(
                showDialog = showResistanceDialog,
                title = stringResource(R.string.edit_resistance),
                label = stringResource(R.string.resistance_level),
                initialValue = exerciseSetsMap[currentExerciseIndex]
                    ?.getOrNull(currentSetIndex)
                    ?.resistance ?: minResistance,
                minValue = minResistance,
                maxValue = maxResistance,
                stepValue = stepResistance,
                onDismiss = { showResistanceDialog = false },
                onSave = { newResistance ->
                    workoutViewModel.updateResistance(
                        currentExerciseIndex,
                        currentSetIndex,
                        newResistance
                    )
                    showResistanceDialog = false
                }
            )

            FloatMetricDialog(
                showDialog = showInclineDialog,
                title = stringResource(R.string.edit_incline),
                label = stringResource(R.string.incline_percent),
                initialValue = exerciseSetsMap[currentExerciseIndex]
                    ?.getOrNull(currentSetIndex)
                    ?.incline ?: minIncline,
                minValue = minIncline,
                maxValue = maxIncline,
                stepValue = stepIncline,
                onDismiss = { showInclineDialog = false },
                onSave = { newIncline ->
                    workoutViewModel.updateIncline(
                        currentExerciseIndex,
                        currentSetIndex,
                        newIncline
                    )
                    showInclineDialog = false
                }
            )


        }
    }
}

@Composable
private fun CurrentHeartRateCard(
    heartRate: Long?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_heart_pulse),
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.current_heart_rate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = if (heartRate != null) {
                    stringResource(R.string.heart_rate_bpm_format, heartRate)
                } else {
                    stringResource(R.string.heart_rate_not_available)
                },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
