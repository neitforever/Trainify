package com.example.motivationcalendarapi.model

data class ExtendedExercise(
    val exercise: Exercise,
    val sets: List<ExerciseSet>
) {
    constructor() : this(Exercise(), listOf(ExerciseSet(rep = 0, weight = 0f, status = SetStatus.NONE)))
}


enum class SetStatus {
    NONE, WARMUP, FAILED, COMPLETED
}

data class ExerciseSet(
    val rep: Int,
    val weight: Float,
    val status: SetStatus = SetStatus.NONE
){
    constructor() : this(0, 0f,SetStatus.NONE)
}