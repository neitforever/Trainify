package com.example.motivationcalendarapi.ui.template.fragments

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavController
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.motivationcalendar.ui.ExerciseCard

@Composable
fun ExerciseTemplateItem(
    index: Int,
    exercise: ExtendedExercise,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canCreateSupersetWithPrevious: Boolean = canMoveUp,
    canCreateSupersetWithNext: Boolean = canMoveDown,
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
    lang: String,
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
    ExerciseCard(
        index = index,
        exercise = exercise,
        exerciseSets = exerciseSets,
        onAddSetClick = onAddSetClick,
        onRepClick = onRepClick,
        onWeightClick = onWeightClick,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown,
        onTimeClick = onTimeClick,
        onResistanceClick = onResistanceClick,
        onInclineClick = onInclineClick,
        canMoveUp = canMoveUp,
        canMoveDown = canMoveDown,
        canCreateSupersetWithPrevious = canCreateSupersetWithPrevious,
        canCreateSupersetWithNext = canCreateSupersetWithNext,
        workoutViewModel = workoutViewModel,
        navController = navController,
        lang = lang,
        supersetLabel = supersetLabel,
        isSupersetFirst = isSupersetFirst,
        isSupersetLast = isSupersetLast,
        supersetBlockStartIndex = supersetBlockStartIndex,
        supersetBlockEndIndex = supersetBlockEndIndex,
        isExerciseDragging = isExerciseDragging,
        isExerciseMergeTarget = isExerciseMergeTarget,
        exerciseDragOffsetY = exerciseDragOffsetY,
        onDeleteExercise = onDelete,
        onSetStatusClick = onStatusClick,
        onDeleteSetClick = { exerciseIndex, setIndex ->
            onDeleteSet(templateId, exerciseIndex, setIndex)
        },
        showMaxSetMenu = false,
        onSetTechniqueClick = { exerciseIndex, setIndex, newSet ->
            workoutViewModel.updateTemplateSet(templateId, exerciseIndex, setIndex, newSet)
        },
        onCreateSupersetWithNext = { exerciseIndex ->
            workoutViewModel.createTemplateSupersetWithNext(templateId, exerciseIndex)
        },
        onCreateSupersetWithPrevious = { exerciseIndex ->
            workoutViewModel.createTemplateSupersetWithPrevious(templateId, exerciseIndex)
        },
        onRemoveFromSuperset = { exerciseIndex ->
            workoutViewModel.removeTemplateExerciseFromSuperset(templateId, exerciseIndex)
        },
        modifier = modifier,
        onExerciseDragStart = onExerciseDragStart,
        onExerciseDrag = onExerciseDrag,
        onExerciseDragEnd = onExerciseDragEnd
    )
}
