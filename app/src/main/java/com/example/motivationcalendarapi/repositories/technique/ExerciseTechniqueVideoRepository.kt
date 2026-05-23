package com.example.motivationcalendarapi.repositories.technique

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.technique.ExerciseTechniqueVideo

class ExerciseTechniqueVideoRepository(
    appDatabase: WorkoutDatabase,
    private val api: ExerciseTechniqueVideoApi = ExerciseTechniqueVideoApi(),
    private val cache: ExerciseTechniqueVideoCacheDataSource = ExerciseTechniqueVideoCacheDataSource(appDatabase)
) {
    private companion object {
        const val CACHE_TTL_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }

    suspend fun getVideosForExercise(
        exerciseId: String,
        exerciseName: String,
        lang: String,
        forceRefresh: Boolean = false
    ): List<ExerciseTechniqueVideo> {
        runCatching { cache.syncPendingLocalCachesToFirestore() }

        if (!forceRefresh) {
            val local = cache.getLocalCachedVideos(exerciseId, lang)
            if (local != null && local.isFresh(CACHE_TTL_MILLIS)) {
                return local.videos
            }

            val remote = runCatching { cache.getRemoteCachedVideos(exerciseId, lang) }.getOrNull()
            if (remote != null && remote.isFresh(CACHE_TTL_MILLIS)) {
                return remote.videos
            }
        }

        val now = System.currentTimeMillis()
        val videos = api.searchTechniqueVideos(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            lang = lang,
            maxResults = 5
        )

        cache.saveLocalVideos(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            lang = lang,
            videos = videos,
            cachedAtMillis = now,
            pendingSync = true
        )
        runCatching {
            cache.saveRemoteVideos(exerciseId, exerciseName, lang, videos, now)
        }
        return videos
    }
}
