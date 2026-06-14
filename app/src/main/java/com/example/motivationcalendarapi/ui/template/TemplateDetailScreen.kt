package com.example.motivationcalendarapi.ui.template

import LoadingView
import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.getCardType
import com.example.motivationcalendarapi.model.localizedName
import com.example.motivationcalendarapi.ui.dialogs.DeleteTemplateDialog
import com.example.motivationcalendarapi.ui.dialogs.FloatMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.TimeMetricDialog
import com.example.motivationcalendarapi.ui.dialogs.WeightDialog
import com.example.motivationcalendarapi.ui.components.TrainifyNameTextField
import com.example.motivationcalendarapi.ui.template.fragments.AddExerciseTemplate
import com.example.motivationcalendarapi.ui.template.fragments.ExerciseTemplateItem
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.motivationcalendar.ui.RepsDialog
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: String?,
    navController: NavController,
    paddingTopValues: Dp,
    workoutViewModel: WorkoutViewModel,
    exerciseViewModel: ExerciseViewModel,
    lang: String
) {
    val template by workoutViewModel
        .getTemplateById(templateId ?: "")
        .collectAsState(initial = null)

    var templateNameDraft by remember(templateId, lang) { mutableStateOf("") }
    var templateNameFieldFocused by remember { mutableStateOf(false) }
    val templateNameSource = template?.localizedName(lang)?.replaceFirstChar { it.uppercase() } ?: ""
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(template?.id, lang, templateNameSource) {
        if (!templateNameFieldFocused) {
            templateNameDraft = templateNameSource
        }
    }

    fun saveTemplateNameIfNeeded() {
        val updatedName = templateNameDraft.trim()
        val currentName = templateNameSource.trim()

        if (templateId != null && updatedName.isNotBlank() && updatedName != currentName) {
            workoutViewModel.updateTemplateNameLocalized(
                templateId = templateId,
                lang = lang,
                newName = updatedName
            )
        }
    }

    var showDeleteTemplateDialog by remember { mutableStateOf(false) }
    var showUpdateFromWorkoutSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showRepDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showTimeDialog by remember { mutableStateOf(false) }
    var showResistanceDialog by remember { mutableStateOf(false) }
    var showInclineDialog by remember { mutableStateOf(false) }

    var currentExerciseIndex by remember { mutableIntStateOf(0) }
    var currentSetIndex by remember { mutableIntStateOf(0) }

    val (showExerciseSheet, setShowExerciseSheet) = remember { mutableStateOf(false) }
    val allWorkouts by workoutViewModel.allWorkouts.collectAsState()

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

    var isTemplateFabExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            TemplateDetailFabMenu(
                isExpanded = isTemplateFabExpanded,
                onToggle = { isTemplateFabExpanded = !isTemplateFabExpanded },
                onAddExercise = {
                    isTemplateFabExpanded = false
                    setShowExerciseSheet(true)
                },
                onUpdateFromWorkout = {
                    isTemplateFabExpanded = false
                    showUpdateFromWorkoutSheet = true
                },
                onDelete = {
                    isTemplateFabExpanded = false
                    showDeleteTemplateDialog = true
                }
            )
        }
    ) { paddingValues ->
        if (template == null) {
            LoadingView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                    .padding(top = paddingTopValues)
            ) {
                item {
                    TrainifyNameTextField(
                        value = templateNameDraft,
                        onValueChange = { templateNameDraft = it },
                        placeholder = stringResource(R.string.workout_template_split),
                        leadingIconRes = R.drawable.ic_template,
                        imeAction = ImeAction.Done,
                        onDone = {
                            saveTemplateNameIfNeeded()
                            keyboardController?.hide()
                            focusManager.clearFocus(force = true)
                        },
                        onFocusChanged = { focused ->
                            if (!focused && templateNameFieldFocused) {
                                saveTemplateNameIfNeeded()
                            }
                            templateNameFieldFocused = focused
                        },
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                    )
                }

                template?.exercises?.let { exercises ->
                    itemsIndexed(exercises) { index, exercise ->
                        ExerciseTemplateItem(
                            index = index,
                            exercise = exercise,
                            templateId = template?.id ?: "",
                            onDelete = {
                                template?.let {
                                    val updatedExercises = it.exercises.toMutableList()
                                        .apply { removeAt(index) }

                                    workoutViewModel.updateTemplateExercises(
                                        it.id,
                                        updatedExercises
                                    )
                                }
                            },
                            onMoveUp = {
                                if (index > 0) {
                                    val updated = exercises.toMutableList().apply {
                                        Collections.swap(this, index, index - 1)
                                    }

                                    workoutViewModel.updateTemplateExercises(
                                        template!!.id,
                                        updated
                                    )
                                }
                            },
                            onMoveDown = {
                                if (index < exercises.size - 1) {
                                    val updated = exercises.toMutableList().apply {
                                        Collections.swap(this, index, index + 1)
                                    }

                                    workoutViewModel.updateTemplateExercises(
                                        template!!.id,
                                        updated
                                    )
                                }
                            },
                            onAddSetClick = { exIndex ->
                                template?.id?.let { id ->
                                    workoutViewModel.addSetToTemplate(id, exIndex)
                                }
                            },
                            onDeleteSet = { currentTemplateId, exIndex, setIndex ->
                                workoutViewModel.removeTemplateSet(
                                    currentTemplateId,
                                    exIndex,
                                    setIndex
                                )
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
                            onStatusClick = { exIndex, setIndex, newStatus ->
                                template?.id?.let { id ->
                                    exercise.sets.getOrNull(setIndex)?.let { currentSet ->
                                        val updatedSet = currentSet.copy(status = newStatus)

                                        workoutViewModel.updateTemplateSet(
                                            id,
                                            exIndex,
                                            setIndex,
                                            updatedSet
                                        )
                                    }
                                }
                            },
                            canMoveUp = index > 0,
                            canMoveDown = index < exercises.size - 1,
                            navController = navController,
                            exerciseSets = exercise.sets,
                            workoutViewModel = workoutViewModel,
                            lang = lang
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(200.dp))
                }
            }


            if (showUpdateFromWorkoutSheet) {
                template?.let { currentTemplate ->
                    TemplateUpdateFromWorkoutSheet(
                        template = currentTemplate,
                        workouts = allWorkouts,
                        lang = lang,
                        minRep = minRep,
                        maxRep = maxRep,
                        stepRep = stepRep,
                        minWeight = minWeight,
                        maxWeight = maxWeight,
                        stepWeight = stepWeight,
                        minCardioTime = minCardioTime,
                        maxCardioTime = maxCardioTime,
                        stepCardioTime = stepCardioTime,
                        minResistance = minResistance,
                        maxResistance = maxResistance,
                        stepResistance = stepResistance,
                        minIncline = minIncline,
                        maxIncline = maxIncline,
                        stepIncline = stepIncline,
                        onDismiss = { showUpdateFromWorkoutSheet = false },
                        onApply = { updatedExercises ->
                            workoutViewModel.updateTemplateExercises(
                                currentTemplate.id,
                                updatedExercises
                            )
                        }
                    )
                }
            }

            if (showExerciseSheet) {
                AddExerciseTemplate(
                    isSheetOpen = showExerciseSheet,
                    onDismiss = { setShowExerciseSheet(false) },
                    sheetState = sheetState,
                    exerciseViewModel = exerciseViewModel,
                    existingExercises = template?.exercises?.map { it.exercise } ?: emptyList(),
                    onExercisesSelected = { newExercises ->
                        template?.let { currentTemplate ->
                            val updated = currentTemplate.exercises + newExercises.map { exercise ->
                                ExtendedExercise(
                                    exercise = exercise,
                                    sets = listOf(
                                        createDefaultTemplateSet(
                                            cardType = exercise.getCardType(lang),
                                            minRep = minRep,
                                            minWeight = minWeight,
                                            minCardioTime = minCardioTime,
                                            minResistance = minResistance,
                                            minIncline = minIncline
                                        )
                                    )
                                )
                            }

                            workoutViewModel.updateTemplateExercises(
                                currentTemplate.id,
                                updated
                            )
                        }
                    },
                    lang = lang
                )
            }
        }
    }

    RepsDialog(
        showDialog = showRepDialog,
        initialRep = template
            ?.exercises
            ?.getOrNull(currentExerciseIndex)
            ?.sets
            ?.getOrNull(currentSetIndex)
            ?.rep ?: minRep,
        minRep = minRep,
        maxRep = maxRep,
        stepRep = stepRep,
        onDismiss = { showRepDialog = false },
        onSave = { newRep ->
            template?.id?.let { id ->
                template
                    ?.exercises
                    ?.getOrNull(currentExerciseIndex)
                    ?.sets
                    ?.getOrNull(currentSetIndex)
                    ?.let { currentSet ->
                        val updatedSet = currentSet.copy(rep = newRep)

                        workoutViewModel.updateTemplateSet(
                            id,
                            currentExerciseIndex,
                            currentSetIndex,
                            updatedSet
                        )
                    }
            }

            showRepDialog = false
        }
    )

    WeightDialog(
        showDialog = showWeightDialog,
        initialWeight = template
            ?.exercises
            ?.getOrNull(currentExerciseIndex)
            ?.sets
            ?.getOrNull(currentSetIndex)
            ?.weight ?: minWeight,
        minWeight = minWeight,
        maxWeight = maxWeight,
        stepWeight = stepWeight,
        onDismiss = { showWeightDialog = false },
        onSave = { newWeight ->
            template?.id?.let { id ->
                template
                    ?.exercises
                    ?.getOrNull(currentExerciseIndex)
                    ?.sets
                    ?.getOrNull(currentSetIndex)
                    ?.let { currentSet ->
                        val updatedSet = currentSet.copy(weight = newWeight)

                        workoutViewModel.updateTemplateSet(
                            id,
                            currentExerciseIndex,
                            currentSetIndex,
                            updatedSet
                        )
                    }
            }

            showWeightDialog = false
        }
    )

    TimeMetricDialog(
        showDialog = showTimeDialog,
        title = stringResource(R.string.edit_time),
        initialValueMinutes = template
            ?.exercises
            ?.getOrNull(currentExerciseIndex)
            ?.sets
            ?.getOrNull(currentSetIndex)
            ?.time ?: minCardioTime,
        minValueMinutes = minCardioTime,
        maxValueMinutes = maxCardioTime,
        stepValueMinutes = stepCardioTime,
        onDismiss = { showTimeDialog = false },
        onSave = { newTime ->
            template?.id?.let { id ->
                template
                    ?.exercises
                    ?.getOrNull(currentExerciseIndex)
                    ?.sets
                    ?.getOrNull(currentSetIndex)
                    ?.let { currentSet ->
                        val updatedSet = currentSet.copy(time = newTime)

                        workoutViewModel.updateTemplateSet(
                            id,
                            currentExerciseIndex,
                            currentSetIndex,
                            updatedSet
                        )
                    }
            }

            showTimeDialog = false
        }
    )

    FloatMetricDialog(
        showDialog = showResistanceDialog,
        title = stringResource(R.string.edit_resistance),
        label = stringResource(R.string.resistance_level),
        initialValue = template
            ?.exercises
            ?.getOrNull(currentExerciseIndex)
            ?.sets
            ?.getOrNull(currentSetIndex)
            ?.resistance ?: minResistance,
        minValue = minResistance,
        maxValue = maxResistance,
        stepValue = stepResistance,
        onDismiss = { showResistanceDialog = false },
        onSave = { newResistance ->
            template?.id?.let { id ->
                template
                    ?.exercises
                    ?.getOrNull(currentExerciseIndex)
                    ?.sets
                    ?.getOrNull(currentSetIndex)
                    ?.let { currentSet ->
                        val updatedSet = currentSet.copy(resistance = newResistance)

                        workoutViewModel.updateTemplateSet(
                            id,
                            currentExerciseIndex,
                            currentSetIndex,
                            updatedSet
                        )
                    }
            }

            showResistanceDialog = false
        }
    )

    FloatMetricDialog(
        showDialog = showInclineDialog,
        title = stringResource(R.string.edit_incline),
        label = stringResource(R.string.incline_percent),
        initialValue = template
            ?.exercises
            ?.getOrNull(currentExerciseIndex)
            ?.sets
            ?.getOrNull(currentSetIndex)
            ?.incline ?: minIncline,
        minValue = minIncline,
        maxValue = maxIncline,
        stepValue = stepIncline,
        onDismiss = { showInclineDialog = false },
        onSave = { newIncline ->
            template?.id?.let { id ->
                template
                    ?.exercises
                    ?.getOrNull(currentExerciseIndex)
                    ?.sets
                    ?.getOrNull(currentSetIndex)
                    ?.let { currentSet ->
                        val updatedSet = currentSet.copy(incline = newIncline)

                        workoutViewModel.updateTemplateSet(
                            id,
                            currentExerciseIndex,
                            currentSetIndex,
                            updatedSet
                        )
                    }
            }

            showInclineDialog = false
        }
    )

    DeleteTemplateDialog(
        showDialog = showDeleteTemplateDialog,
        onDismiss = { showDeleteTemplateDialog = false },
        onConfirm = {
            template?.let {
                workoutViewModel.deleteTemplate(it)
                navController.popBackStack()
            }

            showDeleteTemplateDialog = false
        }
    )
}

@Composable
private fun TemplateDetailFabMenu(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddExercise: () -> Unit,
    onUpdateFromWorkout: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .wrapContentSize(Alignment.BottomEnd)
    ) {
        if (isExpanded) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TemplateMatrixFabButton(
                    iconResId = R.drawable.ic_add,
                    contentDescription = stringResource(R.string.add_exercise),
                    onClick = onAddExercise
                )

                TemplateMatrixFabButton(
                    iconResId = R.drawable.ic_delete,
                    contentDescription = stringResource(R.string.delete_template),
                    onClick = onDelete,
                    iconTint = MaterialTheme.colorScheme.errorContainer
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TemplateMatrixFabButton(
                    iconResId = R.drawable.ic_progress,
                    contentDescription = stringResource(R.string.template_update_from_workout_title),
                    onClick = onUpdateFromWorkout
                )

                TemplateMatrixFabButton(
                    iconResId = R.drawable.ic_close,
                    contentDescription = stringResource(R.string.hide),
                    onClick = onToggle
                )
            }
        } else {
            TemplateMatrixFabButton(
                iconResId = R.drawable.ic_menu,
                contentDescription = stringResource(R.string.template_menu),
                onClick = onToggle
            )
        }
    }
}

@Composable
private fun TemplateMatrixFabButton(
    iconResId: Int,
    contentDescription: String,
    onClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    iconSize: Int = 36
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(iconSize.dp)
        )
    }
}

private fun createDefaultTemplateSet(
    cardType: ExerciseCardType,
    minRep: Int,
    minWeight: Float,
    minCardioTime: Float,
    minResistance: Float,
    minIncline: Float
): ExerciseSet {
    return when (cardType) {
        ExerciseCardType.STRENGTH -> {
            ExerciseSet(
                rep = minRep,
                weight = minWeight,
                status = SetStatus.NONE
            )
        }

        ExerciseCardType.BIKE -> {
            ExerciseSet(
                time = minCardioTime,
                resistance = minResistance,
                status = SetStatus.NONE
            )
        }

        ExerciseCardType.TREADMILL -> {
            ExerciseSet(
                time = minCardioTime,
                resistance = minResistance,
                incline = minIncline,
                status = SetStatus.NONE
            )
        }
    }
}