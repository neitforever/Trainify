package com.example.motivationcalendarapi.model

data class ExtendedExercise(
    val exercise: Exercise = Exercise(),
    val sets: List<ExerciseSet> = listOf(
        ExerciseSet(
            rep = 0,
            weight = 0f,
            time = 0f,
            resistance = 0f,
            incline = 0f,
            status = SetStatus.NONE
        )
    )
)

enum class SetStatus {
    NONE, WARMUP, FAILED, COMPLETED
}

data class ExerciseSet(
    val rep: Int = 0,
    val weight: Float = 0f,

    val time: Float = 0f,
    val resistance: Float = 0f,
    val incline: Float = 0f,

    val status: SetStatus = SetStatus.NONE
)