package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.BodyProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: BodyProgress)

    @Query("SELECT * FROM BodyProgress ORDER BY timestamp DESC")
    fun getAllProgress(): Flow<List<BodyProgress>>

    @Delete
    suspend fun delete(progress: BodyProgress)

    @Query("DELETE FROM BodyProgress")
    suspend fun deleteAll()
}