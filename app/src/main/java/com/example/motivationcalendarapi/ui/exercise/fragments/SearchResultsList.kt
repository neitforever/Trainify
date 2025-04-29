package com.example.motivationcalendarapi.ui.exercise.fragments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.ui.workout.fragments.ExerciseSelectionItem

@Composable
fun SearchResultsList(
    searchQuery: String,
    searchResults: List<Exercise>,
    selectedExercises: List<Exercise>,
    addedExercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
    onAddAll: () -> Unit
) {
    val filteredResults = searchResults.filterNot { ex ->
        addedExercises.any { it.id == ex.id }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (filteredResults.isEmpty()) {
            NotFoundExerciseView(searchQuery)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                items(filteredResults) { exercise ->
                    ExerciseSelectionItem(
                        exercise = exercise,
                        isFavorite = exercise.favorite,
                        selectedOrder = selectedExercises
                            .indexOfFirst { it.id == exercise.id }
                            .takeIf { it >= 0 }
                            ?.plus(1),
                        onItemClick = { onExerciseSelected(exercise) }
                    )
                }
            }
        }

        if (selectedExercises.isNotEmpty()) {
            Button(
                onClick = onAddAll,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = "Add All (${selectedExercises.size})",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}