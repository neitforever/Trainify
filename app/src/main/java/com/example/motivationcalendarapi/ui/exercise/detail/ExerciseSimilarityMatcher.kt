package com.example.motivationcalendarapi.ui.exercise.detail

import com.example.motivationcalendarapi.model.Exercise
import java.util.Locale

fun findSimilarExercises(
    currentExercise: Exercise,
    allExercises: List<Exercise>,
    lang: String
): List<Exercise> {
    val currentBodyPart = currentExercise.getBodyPart(lang).normalizeMatchText()
    val currentTarget = currentExercise.getTarget(lang).normalizeMatchText()
    val currentSecondary = currentExercise.getSecondaryMuscles(lang)
        .map { it.normalizeMatchText() }
        .filter { it.isNotBlank() }
        .toSet()

    return allExercises
        .asSequence()
        .filter { it.id != currentExercise.id }
        .map { exercise ->
            val bodyPartScore = if (
                exercise.getBodyPart(lang).normalizeMatchText() == currentBodyPart && currentBodyPart.isNotBlank()
            ) 3 else 0

            val targetScore = if (
                exercise.getTarget(lang).normalizeMatchText() == currentTarget && currentTarget.isNotBlank()
            ) 4 else 0

            val secondaryScore = exercise.getSecondaryMuscles(lang)
                .map { it.normalizeMatchText() }
                .count { it in currentSecondary }

            exercise to (bodyPartScore + targetScore + secondaryScore)
        }
        .filter { it.second > 0 }
        .sortedWith(compareByDescending<Pair<Exercise, Int>> { it.second }
            .thenBy { it.first.getName(lang) })
        .take(5)
        .map { it.first }
        .toList()
}

private fun String.normalizeMatchText(): String = trim().lowercase(Locale.ROOT)

fun Exercise.getTechniqueSearchName(lang: String): String {
    return when (lang) {
        "en" -> getName("en")
        "ru", "be" -> getName("ru").ifBlank { getName(lang) }
        else -> getName("en").ifBlank { getName(lang) }
    }
}
