package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class BodyProgress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoPath: String? = null,
    val weight: Double,
    val timestamp: Long = System.currentTimeMillis()
)