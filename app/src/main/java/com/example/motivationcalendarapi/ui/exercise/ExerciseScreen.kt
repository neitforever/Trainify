package com.example.motivationcalendarapi.ui.exercise

import LoadingView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import com.example.motivationcalendarapi.ui.exercise.fragments.CollapsibleBodyPartItem
import com.example.motivationcalendarapi.ui.exercise.fragments.ExerciseItem
import com.example.motivationcalendarapi.model.getIconForBodyPart
import com.example.motivationcalendarapi.viewmodel.ExerciseViewModel
import java.util.Locale



@Composable
fun ExerciseScreen(
    navController: NavController,
    viewModel: ExerciseViewModel,
    paddingTopValues: Dp
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAndSaveExercises()
    }

    val bodyParts by viewModel.allBodyParts.collectAsState(initial = emptyList())
    val sortedBodyParts = remember(bodyParts) {
        bodyParts.sortedBy { it.lowercase(Locale.getDefault()) }
    }
    val expandedBodyParts = remember { mutableStateMapOf<String, Boolean>() }

    val favoriteExercises by viewModel.getFavoriteExercises().collectAsState(initial = emptyList())

    if (bodyParts.isEmpty()) {
        LoadingView()
    } else {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingTopValues)
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
                    val exercises by viewModel.getExercisesByBodyPart(bodyPart)
                        .collectAsState(initial = emptyList())
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        exercises.forEach { exercise ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate("${Screen.ExerciseDetailView.route}/${exercise.id}")
                                    }
                                    .padding(start = 16.dp, end = 16.dp)
                            ) {
                                ExerciseItem(
                                    exercise = exercise,
                                    onItemClick = { navController.navigate("exercise_detail/${exercise.id}") },
                                    onFavoriteClick = { viewModel.toggleFavorite(exercise) },
                                    isFavorite = favoriteExercises.any { it.id == exercise.id }
                                )
                            }
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
    }
}
















