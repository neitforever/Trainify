package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val nameLocalized: Map<String, String> = emptyMap(),
    val duration: Int = 0,
    val timestamp: Long = 0,
    val averageHeartRate: Long? = null,
    val exercises: List<ExtendedExercise> = emptyList(),
    val difficulty: DifficultyLevel? = null,
)