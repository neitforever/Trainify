package com.example.motivationcalendarapi.repositories.technique

import com.example.motivationcalendarapi.BuildConfig
import com.example.motivationcalendarapi.model.technique.ExerciseTechniqueVideo
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL

class ExerciseTechniqueVideoApi {

    suspend fun searchTechniqueVideos(
        exerciseId: String,
        exerciseName: String,
        lang: String,
        maxResults: Int = 5
    ): List<ExerciseTechniqueVideo> = withContext(Dispatchers.IO) {
        val endpoint = BuildConfig.YOUTUBE_TECHNIQUE_SEARCH_URL
        if (endpoint.isBlank()) {
            error("YOUTUBE_TECHNIQUE_SEARCH_URL is empty. Add it to local.properties and point it to the Cloudflare Worker /exercise-technique-videos endpoint.")
        }

        val separator = if (endpoint.contains("?")) "&" else "?"
        val requestUrl = endpoint + separator + listOf(
            "exerciseId=${exerciseId.urlEncode()}",
            "exerciseName=${exerciseName.urlEncode()}",
            "lang=${lang.urlEncode()}",
            "maxResults=$maxResults"
        ).joinToString("&")

        val connection = (URL(requestUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 20_000
            readTimeout = 25_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            if (code !in 200..299) {
                error("Technique video search failed: HTTP $code $body")
            }

            parseVideos(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseVideos(body: String): List<ExerciseTechniqueVideo> {
        val root = JsonParser.parseString(body).asJsonObject
        val videos = root.getAsJsonArray("videos") ?: return emptyList()
        val now = System.currentTimeMillis()

        return videos.mapNotNull { element ->
            val item = element.asJsonObject
            val videoId = item.get("videoId")?.asString.orEmpty()
            val video = ExerciseTechniqueVideo(
                videoId = videoId,
                title = item.get("title")?.asString.orEmpty(),
                channelTitle = item.get("channelTitle")?.asString.orEmpty(),
                thumbnailUrl = item.get("thumbnailUrl")?.asString.orEmpty(),
                youtubeUrl = item.get("youtubeUrl")?.asString
                    ?: ExerciseTechniqueVideo.buildShortsUrl(videoId),
                cachedAtMillis = now
            )
            video.takeIf { it.isValid() }
        }
    }

    private fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")
}
