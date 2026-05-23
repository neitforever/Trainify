package com.example.motivationcalendarapi.model.technique

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercise_technique_video_cache")
data class ExerciseTechniqueVideoCacheEntity(
    @PrimaryKey val cacheKey: String = "",
    val exerciseId: String = "",
    val exerciseName: String = "",
    val lang: String = "",
    val videosJson: String = "[]",
    val cachedAtMillis: Long = 0L,
    val syncedAtMillis: Long = 0L,
    val pendingSync: Boolean = false
) {
    companion object {
        fun buildCacheKey(exerciseId: String, lang: String): String = "${exerciseId}_$lang"
    }
}
