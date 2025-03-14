package com.example.motivationcalendarapi.mapper

import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.motivationcalendarapi.model.Converters
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseResponse

fun ExerciseResponse.toEntity(): Exercise {
    return Exercise(
        id = id,
        bodyPart = bodyPart,
        name = name,
        equipment = equipment,
        target = target,
        secondaryMuscles = secondaryMuscles,
        instructions = instructions,
        gifUrl = gifUrl,
        isFavorite = isFavorite,
        note = ""
    )
}