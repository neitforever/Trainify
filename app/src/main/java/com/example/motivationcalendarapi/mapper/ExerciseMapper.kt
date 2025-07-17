//package com.example.motivationcalendarapi.mapper
//
//import android.util.Log
//import com.example.motivationcalendarapi.model.Exercise
//import com.example.motivationcalendarapi.model.ExerciseResponse
//
//fun ExerciseResponse.toEntity(): Exercise {
//    Log.d("Mapping exercise", "${this.id} isFavorite=${this.favorite}")
//    return Exercise(
//        id = id,
//        bodyPart = bodyPart,
//        name = name,
//        equipment = equipment,
//        target = target,
//        secondaryMuscles = secondaryMuscles,
//        instructions = instructions,
//        gifUrl = gifUrl,
//        favorite = favorite,
//        note = ""
//    )
//}