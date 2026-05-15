package com.example.motivationcalendarapi.repositories.equipment

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.example.motivationcalendarapi.BuildConfig
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import java.util.Locale

class GeminiEquipmentRecognitionApi(private val context: Context) {
    private val gson = Gson()

    suspend fun recognize(imageUri: Uri, allowedEquipment: List<EquipmentCandidate>): GeminiEquipmentResult =
        withContext(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isBlank()) error("Gemini API key is empty. Add GEMINI_API_KEY=your_key to local.properties.")
            if (allowedEquipment.isEmpty()) error("No equipment list found in local exercises.")

            val bytes = context.contentResolver.openInputStream(imageUri)?.use { it.readBytes() }
                ?: error("Cannot read selected image.")
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)

            val body = buildRequestJson(base64, mimeType, allowedEquipment)
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", apiKey)
                    connectTimeout = 30_000
                    readTimeout = 60_000
                    doOutput = true
                }

                OutputStreamWriter(connection.outputStream).use { it.write(body) }

                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val response = BufferedReader(InputStreamReader(stream)).use { it.readText() }
                if (code !in 200..299) {
                    if (code == 503 || response.contains("high demand", ignoreCase = true) || response.contains("overloaded", ignoreCase = true)) {
                        throw EquipmentRecognitionHighDemandException()
                    }
                    if (code == 403 || response.contains("location", ignoreCase = true) || response.contains("country", ignoreCase = true)) {
                        throw EquipmentRecognitionNetworkException()
                    }
                    error("Gemini API error $code: $response")
                }

                parseResponse(response)
            } catch (error: UnknownHostException) {
                throw EquipmentRecognitionNetworkException(error)
            } catch (error: SocketTimeoutException) {
                throw EquipmentRecognitionNetworkException(error)
            } catch (error: ConnectException) {
                throw EquipmentRecognitionNetworkException(error)
            } catch (error: IOException) {
                throw EquipmentRecognitionNetworkException(error)
            }
        }

    private fun buildRequestJson(base64: String, mimeType: String, allowedEquipment: List<EquipmentCandidate>): String {
        val equipmentJson = allowedEquipment.joinToString(separator = "\n") { candidate ->
            "- ${candidate.key}: en='${candidate.nameEn}', ru='${candidate.nameRu}', be='${candidate.nameBe}'"
        }

        val prompt = """
            You are an exercise equipment classifier for a fitness Android app.
            Choose equipment ONLY from this list. Do not invent another equipment key.
            $equipmentJson

            Return ONLY valid compact JSON without markdown:
            {
              "equipment_key": "one_allowed_key",
              "confidence": 0.0,
              "description_en": "short description in English, 1-2 sentences",
              "description_ru": "short description in Russian, 1-2 sentences",
              "description_be": "short description in Belarusian, 1-2 sentences",
              "alternatives": [
                {"equipment_key":"another_allowed_key", "confidence":0.0},
                {"equipment_key":"another_allowed_key", "confidence":0.0}
              ]
            }
            Confidence must be from 0 to 1. Alternatives must also use only allowed keys.
        """.trimIndent()

        val root = JsonObject()
        val contents = JsonArray()
        val content = JsonObject()
        val parts = JsonArray()

        parts.add(JsonObject().apply { addProperty("text", prompt) })
        parts.add(JsonObject().apply {
            add("inline_data", JsonObject().apply {
                addProperty("mime_type", mimeType)
                addProperty("data", base64)
            })
        })

        content.add("parts", parts)
        contents.add(content)
        root.add("contents", contents)
        root.add("generationConfig", JsonObject().apply {
            addProperty("temperature", 0.1)
            addProperty("response_mime_type", "application/json")
        })
        return gson.toJson(root)
    }

    private fun parseResponse(response: String): GeminiEquipmentResult {
        val root = JsonParser.parseString(response).asJsonObject
        val text = root.getAsJsonArray("candidates")
            ?.firstOrNull()?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?.firstOrNull()?.asJsonObject
            ?.get("text")?.asString
            ?: error("Gemini response does not contain text result.")

        val cleaned = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val result = JsonParser.parseString(cleaned).asJsonObject

        val alternatives = result.getAsJsonArray("alternatives")?.mapNotNull { element ->
            runCatching {
                val item = element.asJsonObject
                EquipmentAlternative(
                    equipmentKey = item.get("equipment_key").asString.normalizeKey(),
                    confidence = item.get("confidence")?.asFloat ?: 0f
                )
            }.getOrNull()
        } ?: emptyList()

        return GeminiEquipmentResult(
            equipmentKey = result.get("equipment_key").asString.normalizeKey(),
            confidence = result.get("confidence")?.asFloat ?: 0f,
            descriptionEn = result.get("description_en")?.asString.orEmpty(),
            descriptionRu = result.get("description_ru")?.asString.orEmpty(),
            descriptionBe = result.get("description_be")?.asString.orEmpty(),
            alternatives = alternatives
        )
    }

    private fun String.normalizeKey(): String = trim().lowercase(Locale.ROOT).replace(" ", "_").replace("-", "_")
}

class EquipmentRecognitionNetworkException(cause: Throwable? = null) : Exception(cause)

class EquipmentRecognitionHighDemandException(cause: Throwable? = null) : Exception(cause)

data class EquipmentCandidate(
    val key: String,
    val nameEn: String,
    val nameRu: String,
    val nameBe: String
)

data class EquipmentAlternative(
    val equipmentKey: String,
    val confidence: Float
)

data class GeminiEquipmentResult(
    val equipmentKey: String,
    val confidence: Float,
    val descriptionEn: String,
    val descriptionRu: String,
    val descriptionBe: String,
    val alternatives: List<EquipmentAlternative>
)
