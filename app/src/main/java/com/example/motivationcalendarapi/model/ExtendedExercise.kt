package com.example.motivationcalendarapi.model

data class ExtendedExercise(val exercise: Exercise, val sets:List<ExerciseSet>)


data class ExerciseSet(val rep:Int,val weigth:Float)