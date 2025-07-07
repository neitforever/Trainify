package com.example.motivationcalendarapi.ui.template.fragments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchBar
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchResultsList
import com.example.motivationcalendarapi.ui.workout.fragments.BodyPartsList
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseTemplate(
    isSheetOpen: Boolean,
    onDismiss: () -> Unit,
    sheetState: SheetState,
    exerciseViewModel: ExerciseViewModel,
    existingExercises: List<Exercise>,
    onExercisesSelected: (List<Exercise>) -> Unit
) {
    val addedExercises = remember { existingExercises }
    val selectedExercises = remember { mutableStateListOf<Exercise>() }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(isSheetOpen) {
        if (!isSheetOpen) {
            selectedExercises.clear()
            searchQuery = ""
        }
    }

    if (isSheetOpen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = onDismiss
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    modifier = Modifier.padding(16.dp)
                )

                when {
                    searchQuery.isNotEmpty() -> {
                        val searchResults by exerciseViewModel.searchExercises(searchQuery)
                            .collectAsState(initial = emptyList())

                        val filteredSearchResults = searchResults.filterNot { ex ->
                            addedExercises.any { it.id == ex.id }
                        }

                        SearchResultsList(
                            searchQuery = searchQuery,
                            searchResults = filteredSearchResults,
                            selectedExercises = selectedExercises,
                            addedExercises = addedExercises,
                            onExerciseSelected = { exercise ->
                                toggleExerciseSelection(selectedExercises, exercise)
                            },
                            onAddAll = {
                                onAddAllSelected(
                                    selectedExercises,
                                    onExercisesSelected,
                                    onDismiss
                                )
                            }
                        )
                    }

                    else -> {
                        BodyPartsList(
                            exerciseViewModel = exerciseViewModel,
                            selectedExercises = selectedExercises,
                            addedExercises = addedExercises,
                            onExerciseSelected = { exercise ->
                                toggleExerciseSelection(selectedExercises, exercise)
                            },
                            onAddAll = {
                                onAddAllSelected(
                                    selectedExercises,
                                    onExercisesSelected,
                                    onDismiss
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun onAddAllSelected(
    selected: List<Exercise>,
    onSuccess: (List<Exercise>) -> Unit,
    onDismiss: () -> Unit
) {
    onSuccess(selected)
    onDismiss()
}

private fun toggleExerciseSelection(
    selected: MutableList<Exercise>,
    exercise: Exercise
) {
    if (selected.any { it.id == exercise.id }) {
        selected.removeAll { it.id == exercise.id }
    } else {
        selected.add(exercise)
    }
}
