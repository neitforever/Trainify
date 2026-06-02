package com.example.motivationcalendarapi.utils

import com.example.motivationcalendarapi.model.DifficultyLevel
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.model.Workout
import com.example.motivationcalendarapi.model.getCardType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Workout.localDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalDate()
}

fun List<Workout>.groupByLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): Map<LocalDate, List<Workout>> {
    return groupBy { workout -> workout.localDate(zoneId) }
        .mapValues { (_, workouts) -> workouts.sortedBy { it.timestamp } }
}

fun DifficultyLevel.rank(): Int {
    return when (this) {
        DifficultyLevel.EASY -> 1
        DifficultyLevel.NORMAL -> 2
        DifficultyLevel.HARD -> 3
    }
}


fun Workout.resolvedDifficulty(): DifficultyLevel {
    val validSets = exercises.flatMap { exercise ->
        exercise.sets
            .filter { set -> set.status != SetStatus.FAILED }
            .map { set -> exercise.exercise.getCardType() to set }
    }

    if (validSets.isEmpty()) return difficulty ?: DifficultyLevel.EASY

    val failedSetsCount = exercises.sumOf { exercise ->
        exercise.sets.count { set -> set.status == SetStatus.FAILED }
    }

    val exerciseCount = exercises.count { exercise ->
        exercise.sets.any { set -> set.status != SetStatus.FAILED }
    }

    val durationMinutes = (duration / 60f).coerceAtLeast(
        validSets.sumOf { (_, set) -> set.time.toDouble() }.toFloat()
    )

    val strengthVolume = validSets
        .filter { (cardType, set) ->
            cardType == ExerciseCardType.STRENGTH && set.rep > 0 && set.weight > 0f
        }
        .sumOf { (_, set) -> (set.weight * set.rep).toDouble() }
        .toFloat()

    val strengthWorkingSets = validSets.count { (cardType, set) ->
        cardType == ExerciseCardType.STRENGTH && set.rep > 0
    }

    val cardioLoadMinutes = validSets.sumOf { (cardType, set) ->
        when (cardType) {
            ExerciseCardType.BIKE -> {
                val resistanceMultiplier = 1.0 + (set.resistance.coerceAtLeast(0f) / 10f)
                set.time.coerceAtLeast(0f).toDouble() * resistanceMultiplier
            }
            ExerciseCardType.TREADMILL -> {
                val speedMultiplier = 1.0 + (set.resistance.coerceAtLeast(0f) / 8f)
                val inclineMultiplier = 1.0 + (set.incline.coerceAtLeast(0f) / 20f)
                set.time.coerceAtLeast(0f).toDouble() * speedMultiplier * inclineMultiplier
            }
            ExerciseCardType.STRENGTH -> 0.0
        }
    }.toFloat()

    val durationScore = when {
        durationMinutes >= 75f -> 3
        durationMinutes >= 45f -> 2
        durationMinutes >= 20f -> 1
        else -> 0
    }

    val exerciseScore = when {
        exerciseCount >= 7 -> 2
        exerciseCount >= 4 -> 1
        else -> 0
    }

    val strengthScore = when {
        strengthVolume >= 8_000f || strengthWorkingSets >= 18 -> 3
        strengthVolume >= 3_500f || strengthWorkingSets >= 10 -> 2
        strengthVolume >= 800f || strengthWorkingSets >= 4 -> 1
        else -> 0
    }

    val cardioScore = when {
        cardioLoadMinutes >= 55f -> 3
        cardioLoadMinutes >= 30f -> 2
        cardioLoadMinutes >= 12f -> 1
        else -> 0
    }

    val heartRateScore = when {
        averageHeartRate == null -> 0
        averageHeartRate >= 160L -> 3
        averageHeartRate >= 135L -> 2
        averageHeartRate >= 110L -> 1
        else -> 0
    }

    val failedSetsScore = when {
        failedSetsCount >= 5 -> 2
        failedSetsCount >= 2 -> 1
        else -> 0
    }

    val mixedWorkoutBonus = if (strengthScore > 0 && cardioScore > 0) 1 else 0
    val loadScore = if (strengthScore > cardioScore) strengthScore else cardioScore
    val totalScore = durationScore + exerciseScore + loadScore + heartRateScore + failedSetsScore + mixedWorkoutBonus

    return when {
        strengthVolume >= 10_000f -> DifficultyLevel.HARD
        cardioLoadMinutes >= 70f -> DifficultyLevel.HARD
        averageHeartRate != null && averageHeartRate >= 165L && durationMinutes >= 20f -> DifficultyLevel.HARD
        totalScore >= 7 -> DifficultyLevel.HARD
        totalScore >= 3 -> DifficultyLevel.NORMAL
        else -> DifficultyLevel.EASY
    }
}
