package com.example.motivationcalendarapi.repositories.technique_analysis

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.motivationcalendarapi.BuildConfig
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.technique_analysis.ExerciseTechniqueAnalysisResult
import com.example.motivationcalendarapi.model.technique_analysis.LocalizedText
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueIssue
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueIssueSeverity
import com.example.motivationcalendarapi.model.technique_analysis.TechniqueVerdict
import com.example.motivationcalendarapi.repositories.technique_analysis.video.TechniqueVideoPreprocessor
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class ExerciseTechniqueAnalysisRepository(
    private val context: Context
) {
    private val gson = Gson()

    private companion object {
        const val TAG = "TechniqueAnalysis"
        const val MAX_INLINE_VIDEO_BYTES = 60 * 1024 * 1024
        const val MAX_LOG_BODY_LENGTH = 2_000
    }

    suspend fun analyzeVideo(
        videoUri: Uri,
        currentExercise: Exercise,
        allExercises: List<Exercise>,
        lang: String,
        onStageChange: (TechniqueAnalysisStage) -> Unit = {}
    ): ExerciseTechniqueAnalysisResult = withContext(Dispatchers.IO) {
        val url = BuildConfig.GEMINI_TECHNIQUE_ANALYSIS_URL
        if (url.isBlank()) error("GEMINI_TECHNIQUE_ANALYSIS_URL is empty in local.properties")

        onStageChange(TechniqueAnalysisStage.PREPARING_VIDEO)
        val preparedVideo = TechniqueVideoPreprocessor(context).prepareForGemini(videoUri)

        val bytes = context.contentResolver.openInputStream(preparedVideo.uri)?.use { it.readBytes() }
            ?: error("Cannot read prepared video.")
        if (bytes.size > MAX_INLINE_VIDEO_BYTES) {
            preparedVideo.file.delete()
            error("Prepared video is too large for inline analysis. Try a shorter video.")
        }

        val requestBody = buildRequestJson(
            videoBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            mimeType = preparedVideo.mimeType,
            currentExercise = currentExercise,
            allExercises = allExercises,
            lang = lang
        )

        onStageChange(TechniqueAnalysisStage.UPLOADING_AND_ANALYZING)
        Log.d(TAG, "Gemini technique request prepared. bytes=${bytes.size}, mime=${preparedVideo.mimeType}, duration=${preparedVideo.durationMs}")
        try {
            val response = post(url, requestBody)
            Log.d(TAG, "Gemini technique response: ${response.take(MAX_LOG_BODY_LENGTH)}")
            parseResponse(response)
        } finally {
            preparedVideo.file.delete()
        }
    }

    private fun buildRequestJson(
        videoBase64: String,
        mimeType: String,
        currentExercise: Exercise,
        allExercises: List<Exercise>,
        lang: String
    ): String {
        val catalog = allExercises.joinToString("\n") { exercise ->
            "- id=${exercise.id}; en='${exercise.nameLocalized["en"].orEmpty()}'; ru='${exercise.nameLocalized["ru"].orEmpty()}'; be='${exercise.nameLocalized["be"].orEmpty()}'; body='${exercise.bodyPartLocalized["en"].orEmpty()}'; equipment='${exercise.equipmentLocalized["en"].orEmpty()}'"
        }
        val currentInstructions = currentExercise.instructionsLocalized.entries.joinToString("\n") { (code, steps) ->
            "$code: ${steps.joinToString(" | ")}"
        }

        val prompt = """
            You are a strict fitness technique analyst inside an Android fitness app.
            Analyze the uploaded exercise video. Use the video itself as the primary evidence.

            Current opened exercise:
            id=${currentExercise.id}
            en='${currentExercise.nameLocalized["en"].orEmpty()}'
            ru='${currentExercise.nameLocalized["ru"].orEmpty()}'
            be='${currentExercise.nameLocalized["be"].orEmpty()}'
            body='${currentExercise.bodyPartLocalized["en"].orEmpty()}'
            equipment='${currentExercise.equipmentLocalized["en"].orEmpty()}'
            expected instructions:
            $currentInstructions

            Available local exercise catalog. If the video shows another exercise, choose matchedExerciseId ONLY from this catalog. If no reliable match exists, return null.
            $catalog

            Requirements:
            - Return ONLY compact valid JSON. No markdown.
            - Localize user-facing strings into en, ru, and be.
            - Do not invent an exercise id. matchedExerciseId must be current id, one id from the catalog, or null.
            - isExpectedExercise must be false if the video does not show the current opened exercise.
            - If the video is not clear enough, use overallVerdict="UNCLEAR_VIDEO" and explain what must be re-recorded.
            - score is integer from 0 to 100.
            - confidence is from 0 to 1.
            - Use severity LOW, MEDIUM, HIGH.
            - timeHint may be like "00:03-00:06" or null.
            - Be practical: mention visible body position, range of motion, tempo, stability, breathing only when visible.
            - Do not provide medical diagnosis.

            JSON schema:
            {
              "isExpectedExercise": true,
              "detectedExerciseName": "",
              "matchedExerciseId": "${currentExercise.id}",
              "confidence": 0.0,
              "overallVerdict": "GOOD|NEEDS_IMPROVEMENT|UNSAFE|WRONG_EXERCISE|UNCLEAR_VIDEO",
              "score": 0,
              "summary": {"en":"", "ru":"", "be":""},
              "correctPoints": [{"en":"", "ru":"", "be":""}],
              "mistakes": [
                {
                  "title": {"en":"", "ru":"", "be":""},
                  "description": {"en":"", "ru":"", "be":""},
                  "severity": "LOW|MEDIUM|HIGH",
                  "timeHint": null
                }
              ],
              "safetyWarnings": [{"en":"", "ru":"", "be":""}],
              "recommendations": [{"en":"", "ru":"", "be":""}]
            }

            User language code: $lang
        """.trimIndent()

        return gson.toJson(JsonObject().apply {
            add("contents", JsonArray().apply {
                add(JsonObject().apply {
                    add("parts", JsonArray().apply {
                        add(JsonObject().apply { addProperty("text", prompt) })
                        add(JsonObject().apply {
                            add("inline_data", JsonObject().apply {
                                addProperty("mime_type", mimeType)
                                addProperty("data", videoBase64)
                            })
                        })
                    })
                })
            })
            add("generationConfig", JsonObject().apply {
                addProperty("temperature", 0.1)
                addProperty("response_mime_type", "application/json")
            })
        })
    }

    private fun parseResponse(response: String): ExerciseTechniqueAnalysisResult {
        val text = JsonParser.parseString(response).asJsonObject
            .getAsJsonArray("candidates")
            ?.firstOrNull()?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.firstOrNull()?.asJsonObject
            ?.get("text")?.asString
            ?: error("Gemini response does not contain text result.")

        val json = JsonParser.parseString(text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()).asJsonObject
        return ExerciseTechniqueAnalysisResult(
            isExpectedExercise = json.get("isExpectedExercise")?.asBoolean ?: false,
            detectedExerciseName = json.get("detectedExerciseName")?.asString.orEmpty(),
            matchedExerciseId = json.get("matchedExerciseId")?.takeIf { !it.isJsonNull }?.asString?.trim()?.ifBlank { null },
            confidence = (json.get("confidence")?.asFloat ?: 0f).coerceIn(0f, 1f),
            overallVerdict = json.get("overallVerdict")?.asString.toVerdict(),
            score = (json.get("score")?.asInt ?: 0).coerceIn(0, 100),
            summary = json.getAsJsonObject("summary").toLocalizedText(),
            correctPoints = json.getAsJsonArray("correctPoints").toLocalizedList(),
            mistakes = json.getAsJsonArray("mistakes")?.mapNotNull { item ->
                runCatching {
                    val obj = item.asJsonObject
                    TechniqueIssue(
                        title = obj.getAsJsonObject("title").toLocalizedText(),
                        description = obj.getAsJsonObject("description").toLocalizedText(),
                        severity = obj.get("severity")?.asString.toSeverity(),
                        timeHint = obj.get("timeHint")?.takeIf { !it.isJsonNull }?.asString?.trim()?.ifBlank { null }
                    )
                }.getOrNull()
            }.orEmpty(),
            safetyWarnings = json.getAsJsonArray("safetyWarnings").toLocalizedList(),
            recommendations = json.getAsJsonArray("recommendations").toLocalizedList()
        )
    }

    private fun post(url: String, body: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 30_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
            if (code !in 200..299) error("Gemini technique analysis error $code: $response")
            return response
        } catch (error: IOException) {
            throw IOException("Network error while analyzing technique: ${error.message}", error)
        } finally {
            connection.disconnect()
        }
    }

    private fun JsonObject?.toLocalizedText(): LocalizedText {
        if (this == null) return LocalizedText()
        return LocalizedText(
            en = get("en")?.asString.orEmpty(),
            ru = get("ru")?.asString.orEmpty(),
            be = get("be")?.asString.orEmpty()
        )
    }

    private fun JsonArray?.toLocalizedList(): List<LocalizedText> = this?.mapNotNull { item ->
        runCatching { item.asJsonObject.toLocalizedText() }.getOrNull()
    }.orEmpty()

    private fun String?.toVerdict(): TechniqueVerdict = runCatching {
        TechniqueVerdict.valueOf(this?.uppercase(Locale.ROOT).orEmpty())
    }.getOrDefault(TechniqueVerdict.UNCLEAR_VIDEO)

    private fun String?.toSeverity(): TechniqueIssueSeverity = runCatching {
        TechniqueIssueSeverity.valueOf(this?.uppercase(Locale.ROOT).orEmpty())
    }.getOrDefault(TechniqueIssueSeverity.MEDIUM)
}

enum class TechniqueAnalysisStage {
    PREPARING_VIDEO,
    UPLOADING_AND_ANALYZING
}
