package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.motivationcalendarapi.model.Workout
import java.util.UUID

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val exercises: List<ExtendedExercise>,
    val timestamp: Long = System.currentTimeMillis()
)


{
    constructor() : this(
        id = "",
        name = "",
        exercises = emptyList(),
        timestamp = 0L
    )
}
