package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.Alignment
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.Template
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchBar
import com.example.motivationcalendarapi.ui.exercise.fragments.SearchResultsList
import androidx.compose.foundation.lazy.items

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
                    onTemplateSelected = { template ->
                        template.exercises.forEach { ex ->
                            workoutViewModel.addExercise(
                                ExtendedExercise(ex.exercise, ex.sets)
                            )
                        }
                        isSheetOpen.value = false
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


@Composable
private fun SwitchSection(
    showTemplates: Boolean,
    onSwitchChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Выберите тип:",
            style = MaterialTheme.typography.titleMedium
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Упражнения")
            Switch(
                checked = showTemplates,
                onCheckedChange = onSwitchChange
            )
            Text("Шаблоны")
        }
    }
}

@Composable
fun TemplatesListSection(
    templates: List<Template>,
    onTemplateSelected: (Template) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(templates) { template ->
            TemplateSelectionItem(
                template = template,
                onTemplateClick = { onTemplateSelected(it) }
            )
        }
    }
}

@Composable
fun TemplateSelectionItem(
    template: Template,
    onTemplateClick: (Template) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTemplateClick(template) }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = template.name,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Упражнений: ${template.exercises.size}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}