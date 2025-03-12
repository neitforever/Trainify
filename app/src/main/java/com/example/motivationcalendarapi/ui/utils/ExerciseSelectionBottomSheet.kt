package com.example.motivationcalendarapi.ui.utils

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import com.example.motivationcalendarapi.viewmodel.WorkoutViewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExtendedExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel
) {
    if (isSheetOpen.value) {
        val selectedExercises = remember { mutableStateListOf<Exercise>() }
        val addedExercises by workoutViewModel.selectedExercises.collectAsState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen.value = false
                selectedExercises.clear()
            }
        ) {
            var searchQuery by remember { mutableStateOf("") }
            val allSearchResults by exerciseViewModel.searchExercises(searchQuery)
                .collectAsState(initial = emptyList())

            // Фильтруем результаты поиска
            val filteredSearchResults = allSearchResults.filterNot { exercise ->
                addedExercises.any { it.exercise.id == exercise.id }
            }

            Column {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(16.dp)
                )

                when {
                    searchQuery.isNotEmpty() -> SearchResultsList(
                        searchQuery = searchQuery,
                        searchResults = allSearchResults,
                        selectedExercises = selectedExercises,
                        addedExercises = addedExercises.map { it.exercise },
                        onExerciseSelected = { exercise ->
                            if (selectedExercises.any { it.id == exercise.id }) {
                                selectedExercises.removeAll { it.id == exercise.id }
                            } else {
                                selectedExercises.add(exercise)
                            }
                        },
                        onAddAll = {
                            selectedExercises.forEach { exercise ->
                                workoutViewModel.addExercise(
                                    ExtendedExercise(exercise, emptyList())
                                )
                            }
                            selectedExercises.clear()
                            isSheetOpen.value = false
                        }
                    )

                    else -> BodyPartsList(
                        exerciseViewModel = exerciseViewModel,
                        selectedExercises = selectedExercises,
                        addedExercises = addedExercises.map { it.exercise },
                        onExerciseSelected = { exercise ->
                            if (selectedExercises.any { it.id == exercise.id }) {
                                selectedExercises.removeAll { it.id == exercise.id }
                            } else {
                                selectedExercises.add(exercise)
                            }
                        },
                        onAddAll = {
                            selectedExercises.forEach { exercise ->
                                workoutViewModel.addExercise(
                                    ExtendedExercise(exercise, emptyList())
                                )
                            }
                            selectedExercises.clear()
                            isSheetOpen.value = false
                        }
                    )
                }
            }
        }
    }
}