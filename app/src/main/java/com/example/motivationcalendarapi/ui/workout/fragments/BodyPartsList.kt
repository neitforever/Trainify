package com.example.motivationcalendarapi.ui.workout.fragments

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
import java.text.Collator
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
    val bodyParts by exerciseViewModel.getBodyPartsLocalized(lang).collectAsState(initial = emptyList())
    val allExercises by exerciseViewModel.getAllExercises().collectAsState(initial = emptyList())
    val favoriteExercises by exerciseViewModel.getFavoriteExercises().collectAsState(initial = emptyList())

    BodyPartsList(
        bodyParts = bodyParts,
        allExercises = allExercises,
        favoriteExercises = favoriteExercises,
        selectedExercises = selectedExercises,
        addedExercises = addedExercises,
        onExerciseSelected = onExerciseSelected,
        onAddAll = onAddAll,
        lang = lang
    )
}

@Composable
fun BodyPartsList(
    bodyParts: List<String>,
    allExercises: List<Exercise>,
    favoriteExercises: List<Exercise>,
    selectedExercises: List<Exercise>,
    addedExercises: List<Exercise>,
    onExerciseSelected: (Exercise) -> Unit,
    onAddAll: () -> Unit,
    lang: String
) {
    val addedExerciseIds = remember(addedExercises) { addedExercises.map { it.id }.toSet() }
    val locale = remember(lang) { localeForWorkoutLanguage(lang) }
    val collator = remember(locale) { Collator.getInstance(locale).apply { strength = Collator.PRIMARY } }

    val exercisesByBodyPart = remember(allExercises, addedExerciseIds, lang, collator) {
        allExercises
            .filterNot { exercise -> exercise.id in addedExerciseIds }
            .groupBy { exercise -> exercise.getBodyPart(lang) }
            .mapValues { (_, exercises) ->
                exercises.sortedWith { first, second ->
                    collator.compare(first.getName(lang), second.getName(lang))
                }
            }
    }

    val visibleBodyParts = remember(bodyParts, exercisesByBodyPart) {
        bodyParts.filter { bodyPart -> !exercisesByBodyPart[bodyPart].isNullOrEmpty() }
    }

    val bodyPartSections = remember(visibleBodyParts, lang) {
        buildWorkoutBodyPartSections(bodyParts = visibleBodyParts, lang = lang)
            .filter { section -> section.items.isNotEmpty() }
    }

    val favoriteExerciseIds = remember(favoriteExercises) { favoriteExercises.map { it.id }.toSet() }
    val expandedBodyParts = remember { mutableStateMapOf<String, Boolean>() }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            bodyPartSections.forEach { section ->
                item(key = "header_${section.key}") {
                    WorkoutLibrarySectionHeader(
                        title = section.title,
                        description = section.description,
                        count = section.items.size,
                        iconRes = section.iconRes,
                        lang = lang,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 5.dp)
                    )
                }

                items(section.items, key = { bodyPart -> bodyPart }) { bodyPart ->
                    val exercises = exercisesByBodyPart[bodyPart].orEmpty()

                    if (exercises.isNotEmpty()) {
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
                            val (favorites, nonFavorites) = exercises.partition { exercise ->
                                exercise.id in favoriteExerciseIds
                            }

                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                favorites.forEach { exercise ->
                                    ExerciseSelectionItem(
                                        exercise = exercise,
                                        isFavorite = true,
                                        selectedOrder = selectedExercises.indexOfFirst { it.id == exercise.id }
                                            .takeIf { it >= 0 }?.plus(1),
                                        onItemClick = { onExerciseSelected(exercise) },
                                        lang = lang
                                    )
                                }

                                nonFavorites.forEach { exercise ->
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
                }
            }

            item {
                Spacer(
                    modifier = Modifier.absolutePadding(bottom = 200.dp)
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

private fun localeForWorkoutLanguage(lang: String): Locale {
    return when (lang.lowercase()) {
        "ru" -> Locale("ru")
        "be", "by" -> Locale("be")
        else -> Locale.ENGLISH
    }
}
