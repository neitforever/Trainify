package com.example.motivationcalendarapi.model

data class ExtendedExercise(val exercise: Exercise, val sets:List<ExerciseSet>)


enum class SetStatus {
    NONE, WARMUP, FAILED, COMPLETED
}

data class ExerciseSet(
    val rep: Int,
    val weight: Float,
    val status: SetStatus = SetStatus.NONE
)