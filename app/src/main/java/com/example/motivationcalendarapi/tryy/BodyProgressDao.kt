package com.example.motivationcalendarapi.tryy

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyProgressDao {
    @Insert
    suspend fun insert(progress: BodyProgress)

    @Query("SELECT * FROM BodyProgress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<BodyProgress>>

    @Delete
    suspend fun delete(progress: BodyProgress)
}