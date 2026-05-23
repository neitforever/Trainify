package com.example.motivationcalendarapi.model.technique

data class ExerciseTechniqueVideo(
    val videoId: String = "",
    val title: String = "",
    val channelTitle: String = "",
    val thumbnailUrl: String = "",
    val youtubeUrl: String = "",
    val source: String = "youtube",
    val cachedAtMillis: Long = 0L
) {
    fun isValid(): Boolean = YOUTUBE_VIDEO_ID_REGEX.matches(videoId)

    companion object {
        private val YOUTUBE_VIDEO_ID_REGEX = Regex("^[a-zA-Z0-9_-]{11}$")

        fun buildShortsUrl(videoId: String): String = "https://www.youtube.com/shorts/$videoId"

        fun extractYoutubeVideoId(url: String): String? {
            val patterns = listOf(
                Regex("youtube\\.com/watch\\?v=([a-zA-Z0-9_-]{11})"),
                Regex("youtu\\.be/([a-zA-Z0-9_-]{11})"),
                Regex("youtube\\.com/shorts/([a-zA-Z0-9_-]{11})"),
                Regex("youtube\\.com/embed/([a-zA-Z0-9_-]{11})")
            )
            return patterns.firstNotNullOfOrNull { pattern ->
                pattern.find(url)?.groupValues?.getOrNull(1)
            }
        }
    }
}
