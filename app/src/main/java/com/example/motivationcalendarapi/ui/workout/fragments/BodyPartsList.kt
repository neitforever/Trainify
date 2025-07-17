package com.example.motivationcalendarapi.ui.workout.fragments

import LoadingView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.ui.exercise.fragments.CollapsibleBodyPartItem
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import java.util.Locale

@Composable
fun BodyPartsList(
    exerciseViewModel: ExerciseViewModel,
    selectedExercises: List<Exercise>,
    addedExercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
    onAddAll: () -> Unit,
    lang: String
) {


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

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(sortedBodyParts) { bodyPart ->
                    val isExpanded = expandedBodyParts[bodyPart] == true

                    CollapsibleBodyPartItem(
                        bodyPart = bodyPart,
                        isExpanded = isExpanded,
                        onClick = { expandedBodyParts[bodyPart] = !isExpanded }
                    )

                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = fadeIn() + expandVertically(
                            expandFrom = Alignment.Top,
                            initialHeight = { 0 }
                        ),
                        exit = fadeOut() + shrinkVertically(
                            shrinkTowards = Alignment.Top
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val exercises by exerciseViewModel.getExercisesByBodyPart(bodyPart)
                            .collectAsState(initial = emptyList())

                        val filteredExercises = exercises.filterNot { ex ->
                            addedExercises.any { it.id == ex.id }
                        }

                        val (favorites, nonFavorites) = filteredExercises.partition { exercise ->
                            favoriteExercises.any { it.id == exercise.id }
                        }

                        val sortedFavorites = favorites.sortedBy { it.getName(lang) }
                        val sortedNonFavorites = nonFavorites.sortedBy { it.getName(lang) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            sortedFavorites.forEach { exercise ->
                                ExerciseSelectionItem(
                                    exercise = exercise,
                                    isFavorite = true,
                                    selectedOrder = selectedExercises.indexOfFirst { it.id == exercise.id }
                                        .takeIf { it >= 0 }?.plus(1),
                                    onItemClick = { onExerciseSelected(exercise) },
                                    lang = lang

                                )
                            }

                            sortedNonFavorites.forEach { exercise ->
                                ExerciseSelectionItem(
                                    exercise = exercise,
                                    isFavorite = false,
                                    selectedOrder = selectedExercises.indexOfFirst { it.id == exercise.id }
                                        .takeIf { it >= 0 }?.plus(1),
                                    onItemClick = { onExerciseSelected(exercise) },
                                    lang = lang
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(
                        modifier = Modifier
                            .absolutePadding(bottom = 200.dp)
                    )
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
                        text = stringResource(R.string.add_all_with_count, selectedExercises.size),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        }
    }
}

