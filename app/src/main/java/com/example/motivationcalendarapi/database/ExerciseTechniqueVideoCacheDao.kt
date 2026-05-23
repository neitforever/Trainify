package com.example.motivationcalendarapi.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.motivationcalendarapi.model.technique.ExerciseTechniqueVideoCacheEntity

@Dao
interface ExerciseTechniqueVideoCacheDao {
    @Query("SELECT * FROM exercise_technique_video_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCache(cacheKey: String): ExerciseTechniqueVideoCacheEntity?

    @Query("SELECT * FROM exercise_technique_video_cache WHERE pendingSync = 1")
    suspend fun getPendingSyncCaches(): List<ExerciseTechniqueVideoCacheEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: ExerciseTechniqueVideoCacheEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaches(caches: List<ExerciseTechniqueVideoCacheEntity>)

    @Query("UPDATE exercise_technique_video_cache SET pendingSync = 0, syncedAtMillis = :syncedAtMillis WHERE cacheKey = :cacheKey")
    suspend fun markSynced(cacheKey: String, syncedAtMillis: Long)

    @Query("DELETE FROM exercise_technique_video_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCache(cacheKey: String)

    @Query("DELETE FROM exercise_technique_video_cache")
    suspend fun deleteAllCaches()
}
