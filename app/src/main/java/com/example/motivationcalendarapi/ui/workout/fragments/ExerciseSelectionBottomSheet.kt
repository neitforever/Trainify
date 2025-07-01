package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchBar
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchResultsList
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseSelectionBottomSheet(
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    exerciseViewModel: ExerciseViewModel,
    workoutViewModel: WorkoutViewModel,
    navController: NavController
) {
    if (isSheetOpen.value) {
        val selectedExercises = remember { mutableStateListOf<Exercise>() }
        val addedExercises by workoutViewModel.selectedExercises.collectAsState()
        val templates by workoutViewModel.templates.collectAsState(initial = emptyList())
        var showTemplates by remember { mutableStateOf(addedExercises.isEmpty()) }


        LaunchedEffect(addedExercises.isNotEmpty()) {
            if (addedExercises.isNotEmpty()) showTemplates = false
        }

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen.value = false
                selectedExercises.clear()
            }
        ) {
            if (addedExercises.isEmpty()) {
                SwitchSection(
                    showTemplates = showTemplates,
                    onSwitchChange = { showTemplates = it }
                )
            }


            if (showTemplates) {
                TemplatesListSection(
                    templates = templates,
                    onTemplateSelected = { template, templateName ->
                        workoutViewModel.setWorkoutName(templateName)
                        workoutViewModel.addExercisesFromTemplate(template)
                        isSheetOpen.value = false
                    },
                    onViewDetails = { template ->
                        navController.navigate("${Screen.TemplateDetailView.route}/${template.id}")
                    }
                )
            } else {
                var searchQuery by remember { mutableStateOf("") }
                val allSearchResults by exerciseViewModel.searchExercises(searchQuery)
                    .collectAsState(initial = emptyList())

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
}



