package com.example.motivationcalendarapi.ui.template.fragments

import androidx.compose.runtime.Composable
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
        workoutViewModel = workoutViewModel,
        navController = navController,
        lang = lang,
        onDeleteExercise = onDelete,
        onSetStatusClick = onStatusClick,
        onDeleteSetClick = { exerciseIndex, setIndex ->
            onDeleteSet(templateId, exerciseIndex, setIndex)
        },
        showMaxSetMenu = false
    )
}
