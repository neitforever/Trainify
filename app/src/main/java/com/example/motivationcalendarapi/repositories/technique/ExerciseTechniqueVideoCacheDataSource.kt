package com.example.motivationcalendarapi.repositories.technique

import com.example.motivationcalendarapi.database.WorkoutDatabase
import com.example.motivationcalendarapi.model.technique.ExerciseTechniqueVideo
import com.example.motivationcalendarapi.model.technique.ExerciseTechniqueVideoCacheEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ExerciseTechniqueVideoCacheDataSource(
    private val appDatabase: WorkoutDatabase,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val gson: Gson = Gson()
) {
    private val dao = appDatabase.exerciseTechniqueVideoCacheDao()

    private val currentUserId: String?
        get() = auth.currentUser?.uid

    suspend fun getLocalCachedVideos(exerciseId: String, lang: String): CachedTechniqueVideos? {
        val cacheKey = ExerciseTechniqueVideoCacheEntity.buildCacheKey(exerciseId, lang)
        val entity = dao.getCache(cacheKey) ?: return null
        val videos = parseVideos(entity.videosJson)
        return CachedTechniqueVideos(videos, entity.cachedAtMillis)
    }

    suspend fun getRemoteCachedVideos(exerciseId: String, lang: String): CachedTechniqueVideos? {
        val userId = currentUserId ?: return null
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .document(exerciseId)
            .get()
            .await()

        if (!snapshot.exists()) return null
        val localizedTechniqueVideos = snapshot.get("techniqueVideosLocalized") as? Map<*, *> ?: return null
        val langCache = localizedTechniqueVideos[lang] as? Map<*, *> ?: return null
        val cachedAtMillis = (langCache["cachedAtMillis"] as? Number)?.toLong() ?: 0L
        val exerciseName = langCache["exerciseName"] as? String ?: ""
        val videosRaw = langCache["videos"] as? List<*> ?: return null
        val videos = videosRaw.mapNotNull { raw ->
            val map = raw as? Map<*, *> ?: return@mapNotNull null
            ExerciseTechniqueVideo(
                videoId = map["videoId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                channelTitle = map["channelTitle"] as? String ?: "",
                thumbnailUrl = map["thumbnailUrl"] as? String ?: "",
                youtubeUrl = map["youtubeUrl"] as? String ?: "",
                source = map["source"] as? String ?: "youtube",
                cachedAtMillis = (map["cachedAtMillis"] as? Number)?.toLong() ?: cachedAtMillis
            ).takeIf { it.isValid() }
        }

        saveLocalVideos(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            lang = lang,
            videos = videos,
            cachedAtMillis = cachedAtMillis,
            pendingSync = false
        )

        return CachedTechniqueVideos(videos, cachedAtMillis)
    }

    suspend fun saveLocalVideos(
        exerciseId: String,
        exerciseName: String,
        lang: String,
        videos: List<ExerciseTechniqueVideo>,
        cachedAtMillis: Long = System.currentTimeMillis(),
        pendingSync: Boolean = currentUserId != null
    ) {
        val validVideos = videos.filter { it.isValid() }
        val cacheKey = ExerciseTechniqueVideoCacheEntity.buildCacheKey(exerciseId, lang)
        dao.insertCache(
            ExerciseTechniqueVideoCacheEntity(
                cacheKey = cacheKey,
                exerciseId = exerciseId,
                exerciseName = exerciseName,
                lang = lang,
                videosJson = gson.toJson(validVideos),
                cachedAtMillis = cachedAtMillis,
                syncedAtMillis = if (pendingSync) 0L else System.currentTimeMillis(),
                pendingSync = pendingSync
            )
        )
    }

    suspend fun saveRemoteVideos(
        exerciseId: String,
        exerciseName: String,
        lang: String,
        videos: List<ExerciseTechniqueVideo>,
        cachedAtMillis: Long = System.currentTimeMillis()
    ) {
        val userId = currentUserId ?: return
        val documentId = ExerciseTechniqueVideoCacheEntity.buildCacheKey(exerciseId, lang)
        val validVideos = videos.filter { it.isValid() }
        val langPayload = mapOf(
            "exerciseName" to exerciseName,
            "cachedAtMillis" to cachedAtMillis,
            "videos" to validVideos.map { video ->
                mapOf(
                    "videoId" to video.videoId,
                    "title" to video.title,
                    "channelTitle" to video.channelTitle,
                    "thumbnailUrl" to video.thumbnailUrl,
                    "youtubeUrl" to video.youtubeUrl,
                    "source" to video.source,
                    "cachedAtMillis" to cachedAtMillis
                )
            }
        )

        firestore.collection("users")
            .document(userId)
            .collection("exercises")
            .document(exerciseId)
            .update("techniqueVideosLocalized.$lang", langPayload)
            .await()

        dao.markSynced(documentId, System.currentTimeMillis())
    }

    suspend fun syncPendingLocalCachesToFirestore() {
        if (currentUserId == null) return
        dao.getPendingSyncCaches().forEach { entity ->
            val videos = parseVideos(entity.videosJson)
            saveRemoteVideos(
                exerciseId = entity.exerciseId,
                exerciseName = entity.exerciseName,
                lang = entity.lang,
                videos = videos,
                cachedAtMillis = entity.cachedAtMillis
            )
        }
    }

    private fun parseVideos(videosJson: String): List<ExerciseTechniqueVideo> {
        return runCatching {
            val type = object : TypeToken<List<ExerciseTechniqueVideo>>() {}.type
            gson.fromJson<List<ExerciseTechniqueVideo>>(videosJson, type)
                .orEmpty()
                .filter { it.isValid() }
        }.getOrDefault(emptyList())
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { result -> continuation.resume(result) }
            addOnFailureListener { error -> continuation.resumeWithException(error) }
        }
}

data class CachedTechniqueVideos(
    val videos: List<ExerciseTechniqueVideo>,
    val cachedAtMillis: Long
) {
    fun isFresh(ttlMillis: Long): Boolean {
        return videos.isNotEmpty() && System.currentTimeMillis() - cachedAtMillis <= ttlMillis
    }
}
