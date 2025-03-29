package com.example.motivationcalendarapi.tryy

import com.motivationcalendar.data.WorkoutDatabase
import kotlinx.coroutines.flow.Flow

class BodyProgressRepository(private val database: WorkoutDatabase) {
    fun getAllProgress(): Flow<List<BodyProgress>> {
        return database.bodyProgressDao().getAllProgress()
    }

    suspend fun insert(progress: BodyProgress) {
        database.bodyProgressDao().insert(progress)
    }

    suspend fun delete(progress: BodyProgress) {
        database.bodyProgressDao().delete(progress)
    }
}