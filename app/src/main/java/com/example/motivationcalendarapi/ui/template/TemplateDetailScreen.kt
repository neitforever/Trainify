package com.example.motivationcalendarapi.ui.template

import LoadingView
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
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
import kotlinx.coroutines.delay
import com.motivationcalendar.ui.RepsDialog

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
    val exerciseCardBounds = remember { mutableStateMapOf<Int, Rect>() }
    val templateListState = rememberLazyListState()
    var draggedExerciseIndex by remember { mutableStateOf<Int?>(null) }
    var draggedExerciseY by remember { mutableStateOf<Float?>(null) }
    var draggedExerciseStartY by remember { mutableStateOf<Float?>(null) }
    var draggedExerciseFingerY by remember { mutableStateOf<Float?>(null) }
    var draggedExerciseAnchorDeltaY by remember { mutableStateOf(0f) }
    var draggedExerciseOffsetY by remember { mutableStateOf(0f) }
    var draggedExerciseInsertIndex by remember { mutableStateOf<Int?>(null) }
    var draggedExerciseMergeTargetIndex by remember { mutableStateOf<Int?>(null) }
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val autoScrollThresholdPx = with(density) { 96.dp.toPx() }
    val autoScrollStepPx = with(density) { 36.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    fun resolveExerciseDrop(y: Float, exerciseCount: Int): Pair<Int?, Int> {
        val exercises = template?.exercises.orEmpty()
        val sourceIndex = draggedExerciseIndex
        val draggedGroupId = sourceIndex?.let { exercises.getOrNull(it)?.supersetGroupId }
        val draggedIndices = exercises.mapIndexedNotNull { index, item ->
            when {
                sourceIndex == null -> null
                draggedGroupId != null && item.supersetGroupId == draggedGroupId -> index
                index == sourceIndex -> index
                else -> null
            }
        }.toSet()
        val bounds = exerciseCardBounds.toList().sortedBy { it.second.top }
        val targetIndex = bounds.firstOrNull { (index, rect) ->
            index !in draggedIndices && y in (rect.top + rect.height * 0.28f)..(rect.bottom - rect.height * 0.28f)
        }?.first
        if (targetIndex != null) return targetIndex to targetIndex
        val insertIndex = bounds.count { (_, rect) -> y > rect.center.y }.coerceIn(0, exerciseCount)
        return null to insertIndex
    }

    fun updateExerciseDragPreview(y: Float, exerciseCount: Int) {
        val (targetIndex, insertIndex) = resolveExerciseDrop(y, exerciseCount)
        draggedExerciseMergeTargetIndex = targetIndex
        draggedExerciseInsertIndex = if (targetIndex == null) insertIndex else null
    }

    fun draggedExerciseBlockBounds(sourceIndex: Int?, exercises: List<ExtendedExercise>): Rect? {
        if (sourceIndex == null) return null
        val sourceExercise = exercises.getOrNull(sourceIndex) ?: return exerciseCardBounds[sourceIndex]
        val groupId = sourceExercise.supersetGroupId
        val indices = if (groupId == null) {
            listOf(sourceIndex)
        } else {
            exercises.mapIndexedNotNull { index, item -> if (item.supersetGroupId == groupId) index else null }
        }
        val bounds = indices.mapNotNull { exerciseCardBounds[it] }
        if (bounds.isEmpty()) return exerciseCardBounds[sourceIndex]
        return Rect(
            left = bounds.minOf { it.left },
            top = bounds.minOf { it.top },
            right = bounds.maxOf { it.right },
            bottom = bounds.maxOf { it.bottom }
        )
    }

    fun visibleExerciseDragOffset(rawOffset: Float, sourceIndex: Int?, exercises: List<ExtendedExercise>): Float {
        val bounds = draggedExerciseBlockBounds(sourceIndex, exercises) ?: return rawOffset
        val topLimit = with(density) { 8.dp.toPx() }
        val bottomLimit = screenHeightPx - with(density) { 8.dp.toPx() }
        val minOffset = topLimit - bounds.top
        val maxOffset = bottomLimit - bounds.bottom
        val lower = minOf(minOffset, maxOffset)
        val upper = maxOf(minOffset, maxOffset)
        return rawOffset.coerceIn(lower, upper)
    }

    fun draggedExerciseDropY(sourceIndex: Int?, visibleOffset: Float, exercises: List<ExtendedExercise>): Float {
        val bounds = draggedExerciseBlockBounds(sourceIndex, exercises)
        return if (bounds != null) bounds.center.y + visibleOffset else draggedExerciseFingerY ?: 0f
    }

    LaunchedEffect(draggedExerciseIndex, draggedExerciseFingerY, template?.exercises?.size) {
        val exerciseCount = template?.exercises?.size ?: 0
        while (draggedExerciseIndex != null) {
            val fingerY = draggedExerciseFingerY
            val autoScrollDelta = when {
                fingerY == null -> 0f
                fingerY < autoScrollThresholdPx -> -autoScrollStepPx
                fingerY > screenHeightPx - autoScrollThresholdPx -> autoScrollStepPx
                else -> 0f
            }
            if (autoScrollDelta != 0f) {
                templateListState.scrollBy(autoScrollDelta)
                draggedExerciseFingerY?.let { currentFingerY ->
                    val projectedCardCenterY = currentFingerY - draggedExerciseAnchorDeltaY
                    draggedExerciseY = projectedCardCenterY
                    updateExerciseDragPreview(projectedCardCenterY, exerciseCount)
                }
                delay(16L)
            } else {
                delay(32L)
            }
        }
    }

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
                state = templateListState,
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
                        val currentGroupId = exercise.supersetGroupId
                        val isSupersetFirst = currentGroupId == null || index == 0 || exercises[index - 1].supersetGroupId != currentGroupId
                        val isSupersetLast = currentGroupId == null || index == exercises.lastIndex || exercises[index + 1].supersetGroupId != currentGroupId
                        val blockStart = if (currentGroupId == null) index else exercises.indexOfFirst { it.supersetGroupId == currentGroupId }
                        val blockEnd = if (currentGroupId == null) index else exercises.indexOfLast { it.supersetGroupId == currentGroupId }
                        val supersetNumber = currentGroupId?.let { groupId -> exercises.take(index + 1).mapNotNull { it.supersetGroupId }.distinct().indexOf(groupId) + 1 }

                        val draggedIndex = draggedExerciseIndex
                        val draggedGroupId = draggedIndex?.let { exercises.getOrNull(it)?.supersetGroupId }
                        val isDraggedExercise = draggedIndex == index || (draggedGroupId != null && currentGroupId == draggedGroupId)
                        val mergeTargetGroupId = draggedExerciseMergeTargetIndex?.let { exercises.getOrNull(it)?.supersetGroupId }
                        val isExerciseMergeTarget = !isDraggedExercise && (draggedExerciseMergeTargetIndex == index || (mergeTargetGroupId != null && currentGroupId == mergeTargetGroupId))
                        val exerciseDragVisualOffsetY = if (isDraggedExercise) {
                            val projectedCenterY = draggedExerciseY
                            val currentBlockBounds = draggedExerciseBlockBounds(draggedIndex, exercises)
                            if (projectedCenterY != null && currentBlockBounds != null) {
                                projectedCenterY - currentBlockBounds.center.y
                            } else {
                                0f
                            }
                        } else {
                            0f
                        }
                        val canMoveExerciseUp = if (currentGroupId != null && index > blockStart) true else blockStart > 0
                        val canMoveExerciseDown = if (currentGroupId != null && index < blockEnd) true else blockEnd < exercises.lastIndex

                        val showInsertionGapBefore = draggedExerciseIndex != null && draggedExerciseInsertIndex == index
                        val insertionGapBefore by animateDpAsState(
                            targetValue = if (showInsertionGapBefore) 22.dp else 0.dp,
                            label = "templateExerciseInsertGapBefore"
                        )
                        Spacer(modifier = Modifier.height(insertionGapBefore))

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
                                template?.id?.let { workoutViewModel.moveTemplateExerciseUp(it, index) }
                            },
                            onMoveDown = {
                                template?.id?.let { workoutViewModel.moveTemplateExerciseDown(it, index) }
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
                            canMoveUp = canMoveExerciseUp,
                            canMoveDown = canMoveExerciseDown,
                            canCreateSupersetWithPrevious = blockStart > 0,
                            canCreateSupersetWithNext = blockEnd < exercises.lastIndex,
                            navController = navController,
                            exerciseSets = exercise.sets,
                            workoutViewModel = workoutViewModel,
                            lang = lang,
                            supersetLabel = supersetNumber?.let { stringResource(R.string.superset_number_format, it) },
                            isSupersetFirst = isSupersetFirst,
                            isSupersetLast = isSupersetLast,
                            supersetBlockStartIndex = blockStart,
                            supersetBlockEndIndex = blockEnd,
                            isExerciseDragging = isDraggedExercise,
                            isExerciseMergeTarget = isExerciseMergeTarget,
                            exerciseDragOffsetY = exerciseDragVisualOffsetY,
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val position = coordinates.positionInRoot()
                                exerciseCardBounds[index] = Rect(
                                    left = position.x,
                                    top = position.y,
                                    right = position.x + coordinates.size.width,
                                    bottom = position.y + coordinates.size.height
                                )
                            },
                            onExerciseDragStart = { dragIndex, offset ->
                                draggedExerciseIndex = dragIndex
                                draggedExerciseStartY = null
                                draggedExerciseFingerY = null
                                draggedExerciseAnchorDeltaY = 0f
                                draggedExerciseOffsetY = 0f
                                exerciseCardBounds[dragIndex]?.let { rect ->
                                    val fingerY = rect.top + offset.y
                                    val blockCenterY = draggedExerciseBlockBounds(dragIndex, exercises)?.center?.y ?: rect.center.y
                                    draggedExerciseAnchorDeltaY = fingerY - blockCenterY
                                    draggedExerciseStartY = fingerY
                                    draggedExerciseFingerY = fingerY
                                    draggedExerciseY = blockCenterY
                                    updateExerciseDragPreview(blockCenterY, exercises.size)
                                }
                            },
                            onExerciseDrag = { dragAmount ->
                                val fingerY = (draggedExerciseFingerY ?: draggedExerciseStartY ?: 0f) + dragAmount.y
                                draggedExerciseFingerY = fingerY

                                val projectedCardCenterY = fingerY - draggedExerciseAnchorDeltaY
                                updateExerciseDragPreview(projectedCardCenterY, exercises.size)

                                draggedExerciseOffsetY = 0f
                                draggedExerciseY = projectedCardCenterY
                            },
                            onExerciseDragEnd = {
                                val sourceIndex = draggedExerciseIndex
                                val dropY = draggedExerciseY
                                val currentTemplateId = template?.id
                                if (sourceIndex != null && dropY != null && currentTemplateId != null) {
                                    val (targetIndex, insertIndex) = resolveExerciseDrop(dropY, exercises.size)
                                    workoutViewModel.handleTemplateExerciseDragDrop(currentTemplateId, sourceIndex, targetIndex, insertIndex)
                                }
                                draggedExerciseIndex = null
                                draggedExerciseY = null
                                draggedExerciseStartY = null
                                draggedExerciseFingerY = null
                                draggedExerciseAnchorDeltaY = 0f
                                draggedExerciseOffsetY = 0f
                                draggedExerciseInsertIndex = null
                                draggedExerciseMergeTargetIndex = null
                            }
                        )
                    }
                    item {
                        val showInsertionGapAfterLast = draggedExerciseIndex != null && draggedExerciseInsertIndex == exercises.size
                        val insertionGapAfterLast by animateDpAsState(
                            targetValue = if (showInsertionGapAfterLast) 22.dp else 0.dp,
                            label = "templateExerciseInsertGapAfterLast"
                        )
                        Spacer(modifier = Modifier.height(insertionGapAfterLast))
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