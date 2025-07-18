package com.example.motivationcalendarapi.ui.workout.detail

import LoadingView
import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.ui.dialogs.DeleteWorkoutDialog
import com.example.motivationcalendarapi.ui.dialogs.SaveTemplateDialog
import com.example.motivationcalendarapi.ui.dialogs.TemplateSavedDialog
import com.example.motivationcalendarapi.ui.theme.EASY_COLOR
import com.example.motivationcalendarapi.ui.theme.HARD_COLOR
import com.example.motivationcalendarapi.ui.theme.NORMAL_COLOR
import com.example.motivationcalendarapi.ui.workout.detail.fragments.ExerciseCardHistory
import com.example.motivationcalendarapi.ui.workout.detail.fragments.TotalWeightAndTimeRow
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import com.example.motivationcalendarapi.utils.formatDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryDetailScreen(
    workoutId: String?,
    workoutViewModel: WorkoutViewModel,
    navController: NavController,
    paddingValues: Dp,
    lang: String
) {

    val selectedWorkout = remember { mutableStateOf<Workout?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val showSaveTemplateDialog = remember { mutableStateOf(false) }
    val showSaveSuccessDialog = remember { mutableStateOf(false) }
    val savedTemplateName = remember { mutableStateOf("") }
    val context = LocalContext.current

    if (showSaveTemplateDialog.value) {
        SaveTemplateDialog(
            showDialog = true,
            onDismiss = { showSaveTemplateDialog.value = false },
            onConfirm = { name ->
                selectedWorkout.value?.let { workout ->
                    val exercises = workout.exercises
                    val templateWorkout = workout.copy(
                        name = name,
                        exercises = exercises,
                        duration = 0,
                        timestamp = System.currentTimeMillis()
                    )
                    workoutViewModel.saveAsTemplate(templateWorkout, name)
                    savedTemplateName.value = name
                    showSaveSuccessDialog.value = true
                }
            }
        )
    }

    TemplateSavedDialog(
        showDialog = showSaveSuccessDialog.value,
        templateName = savedTemplateName.value,
        onDismiss = { showSaveSuccessDialog.value = false }
    )

    if (showDeleteDialog.value) {
        DeleteWorkoutDialog(
            showDialog = true,
            onDismiss = { showDeleteDialog.value = false },
            onConfirm = {
                selectedWorkout.value?.let {
                    workoutViewModel.deleteWorkout(it)
                    navController.navigate(Screen.WorkoutHistory.route) {
                        popUpTo(Screen.WorkoutHistory.route) { inclusive = true }
                    }
                }
            }
        )
    }

    LaunchedEffect(workoutId) {
        workoutId?.let { id ->
            CoroutineScope(Dispatchers.Default).launch {
                selectedWorkout.value = workoutViewModel.getWorkoutById(id)
            }
        }
    }




    Scaffold(
        floatingActionButton = {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FloatingActionButton(
                    onClick = { showSaveTemplateDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_save),
                        contentDescription = stringResource(R.string.save_template),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                FloatingActionButton(
                    onClick = { showDeleteDialog.value = true },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = stringResource(R.string.delete_workout),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }) { paddingValue ->
        if (selectedWorkout.value != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues)
                    .padding(bottom = paddingValue.calculateBottomPadding())
                    .padding(horizontal = 10.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        selectedWorkout.value?.let { workout ->
                            val difficulty = workoutViewModel.calculateWorkoutDifficulty(workout)
                            val iconColor = when (difficulty) {
                                DifficultyLevel.EASY -> EASY_COLOR
                                DifficultyLevel.NORMAL -> NORMAL_COLOR
                                DifficultyLevel.HARD -> HARD_COLOR
                            }
                            Icon(
                                painter = painterResource(
                                    id = when (difficulty) {
                                        DifficultyLevel.EASY -> R.drawable.ic_smile_easy
                                        DifficultyLevel.NORMAL -> R.drawable.ic_smile_normal
                                        DifficultyLevel.HARD -> R.drawable.ic_smile_hard
                                    }
                                ),
                                contentDescription = stringResource(R.string.workout_difficulty),
                                tint = iconColor,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(end = 8.dp)
                            )
                        }
                        Text(text = selectedWorkout.value?.name?.replaceFirstChar { it.uppercase() }
                            ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(context, selectedWorkout.value?.timestamp ?: 0L),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                selectedWorkout.value?.let { workout ->
                    val totalKg = workoutViewModel.calculateTotalKg(workout)

                    TotalWeightAndTimeRow(
                        timerValue = workout.duration,
                        totalKg = totalKg,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = stringResource(R.string.exercises_performed),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 4.dp)
                            .fillMaxWidth()
                            .align(Alignment.Start)
                    )


                    selectedWorkout.value?.let { workout ->
                        workout.exercises.forEachIndexed { index, extendedExercise ->
                            ExerciseCardHistory(
                                index = index,
                                exercise = extendedExercise,
                                exerciseSets = extendedExercise.sets,
                                workoutViewModel = workoutViewModel,
                                navController = navController,
                                lang = lang
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.absolutePadding(bottom = 200.dp))
            }
        } else {
            LoadingView()
        }
    }
}







