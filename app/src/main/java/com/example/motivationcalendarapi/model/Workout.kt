package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.motivationcalendarapi.model.BodyProgress
import java.util.UUID

@Entity(tableName = "workout_table")
data class Workout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val duration: Int,
    val timestamp: Long,
    val exercises: List<ExtendedExercise>,
) {
    constructor() : this("", "", 0, 0, emptyList())
}
