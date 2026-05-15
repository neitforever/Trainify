package com.example.motivationcalendarapi.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Keep
@Entity
data class BodyProgress(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val photoPath: String? = null,
    val weight: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)


{
    constructor() : this("", null, 0.0, System.currentTimeMillis())
}