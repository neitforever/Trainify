package com.example.motivationcalendarapi.ui.utils

import LoadingView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel
) {
    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false }
        ) {
            LaunchedEffect(Unit) {
                exerciseViewModel.fetchAndSaveExercises()
            }



            val bodyParts by exerciseViewModel.allBodyParts.collectAsState(initial = emptyList())
            val sortedBodyParts = remember(bodyParts) {
                bodyParts.sortedBy { it.lowercase(Locale.getDefault()) }
            }

            val favoriteExercises by exerciseViewModel.getFavoriteExercises()
                .collectAsState(initial = emptyList())
            val expandedBodyParts = remember { mutableStateMapOf<String, Boolean>() }

            if (bodyParts.isEmpty()) {
                LoadingView()
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedBodyParts) { bodyPart ->
                        val isExpanded = expandedBodyParts[bodyPart] ?: false
                        CollapsibleBodyPartItem(
                            bodyPart = bodyPart,
                            isExpanded = isExpanded,
                            onClick = { expandedBodyParts[bodyPart] = !isExpanded }
                        )

                        if (isExpanded) {
                            val exercises by exerciseViewModel.getExercisesByBodyPart(bodyPart)
                                .collectAsState(initial = emptyList())

                            val (favorites, nonFavorites) = exercises.partition { exercise ->
                                favoriteExercises.any { it.id == exercise.id }
                            }

                            val sortedFavorites = favorites.sortedBy { it.name }
                            val sortedNonFavorites = nonFavorites.sortedBy { it.name }

                            sortedFavorites.forEach { exercise ->
                                ExerciseSelectionItem(
                                    exercise = exercise,
                                    isFavorite = true,
                                    onItemClick = {
                                        workoutViewModel.addExercise(
                                            ExtendedExercise(exercise, emptyList())
                                        )
                                        isSheetOpen.value = false
                                    }
                                )
                            }

                            sortedNonFavorites.forEach { exercise ->
                                ExerciseSelectionItem(
                                    exercise = exercise,
                                    isFavorite = false,
                                    onItemClick = {
                                        workoutViewModel.addExercise(
                                            ExtendedExercise(exercise, emptyList())
                                        )
                                        isSheetOpen.value = false
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