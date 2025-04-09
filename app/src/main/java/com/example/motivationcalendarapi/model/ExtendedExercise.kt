package com.example.motivationcalendarapi.model

data class ExtendedExercise(
    val exercise: Exercise = Exercise(),
    val sets: List<ExerciseSet> = emptyList()
) {
    constructor() : this(Exercise(), emptyList())
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