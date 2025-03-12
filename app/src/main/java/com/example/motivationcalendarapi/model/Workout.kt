package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val duration: Int,
    val timestamp: Long,
    val exercises: List<ExtendedExercise>,
)

