package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "templates")
data class Template(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val exercises: List<ExtendedExercise> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)