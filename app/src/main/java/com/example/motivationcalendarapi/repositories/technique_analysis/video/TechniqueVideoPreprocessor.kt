package com.example.motivationcalendarapi.repositories.technique_analysis.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import androidx.media3.transformer.VideoEncoderSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TechniqueVideoPreprocessor(
    private val context: Context
) {
    companion object {
        const val MAX_SOURCE_DURATION_MS = 3 * 60 * 1000L
        const val MAX_OUTPUT_BYTES = 55 * 1024 * 1024L
        const val TARGET_HEIGHT = 720
        const val TARGET_VIDEO_BITRATE = 1_800_000
    }

    suspend fun prepareForGemini(sourceUri: Uri): PreparedTechniqueVideo {
        val metadata = readMetadata(sourceUri)
        if (metadata.durationMs > MAX_SOURCE_DURATION_MS) {
            error("Video is too long. Select a video up to 3 minutes.")
        }

        val outputFile = File(context.cacheDir, "technique_analysis_${System.currentTimeMillis()}.mp4")
        transcodeToMp4(sourceUri, outputFile)

        if (!outputFile.exists() || outputFile.length() <= 0L) {
            error("Cannot prepare video for analysis.")
        }
        if (outputFile.length() > MAX_OUTPUT_BYTES) {
            outputFile.delete()
            error("Prepared video is still too large. Try a shorter or less detailed video.")
        }

        return PreparedTechniqueVideo(
            uri = outputFile.toUri(),
            file = outputFile,
            mimeType = "video/mp4",
            durationMs = metadata.durationMs,
            originalWidth = metadata.width,
            originalHeight = metadata.height,
            sizeBytes = outputFile.length()
        )
    }

    private fun readMetadata(uri: Uri): TechniqueVideoMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            TechniqueVideoMetadata(duration, width, height)
        } finally {
            retriever.release()
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun transcodeToMp4(inputUri: Uri, outputFile: File) = withContext(Dispatchers.Main.immediate) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val editedMediaItem = EditedMediaItem.Builder(MediaItem.fromUri(inputUri))
                .setEffects(
                    Effects(
                        emptyList(),
                        listOf(Presentation.createForHeight(TARGET_HEIGHT))
                    )
                )
                .build()

            val transformer = Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .setEncoderFactory(
                    DefaultEncoderFactory.Builder(context)
                        .setRequestedVideoEncoderSettings(
                            VideoEncoderSettings.Builder()
                                .setBitrate(TARGET_VIDEO_BITRATE)
                                .build()
                        )
                        .build()
                )
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }

                    override fun onError(
                        composition: Composition,
                        exportResult: ExportResult,
                        exportException: ExportException
                    ) {
                        outputFile.delete()
                        if (continuation.isActive) continuation.resumeWithException(exportException)
                    }
                })
                .build()

            continuation.invokeOnCancellation {
                transformer.cancel()
                outputFile.delete()
            }

            outputFile.parentFile?.mkdirs()
            if (outputFile.exists()) outputFile.delete()
            transformer.start(editedMediaItem, outputFile.absolutePath)
        }
    }
}

data class TechniqueVideoMetadata(
    val durationMs: Long,
    val width: Int,
    val height: Int
)

data class PreparedTechniqueVideo(
    val uri: Uri,
    val file: File,
    val mimeType: String,
    val durationMs: Long,
    val originalWidth: Int,
    val originalHeight: Int,
    val sizeBytes: Long
)
