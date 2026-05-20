package com.example.motivationcalendarapi.repositories.ai

import android.util.Log
import com.example.motivationcalendarapi.BuildConfig
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExtendedExercise
import com.example.motivationcalendarapi.model.SetStatus
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.URL
import java.io.IOException
import java.util.Locale
import java.util.UUID
import kotlin.math.max

class GeminiAiGenerationApi {
    private val gson = Gson()

    private companion object {
        const val TAG = "GeminiAiGeneration"
    }

    suspend fun generateExercise(
        prompt: String,
        selectedBodyPart: String,
        selectedEquipment: String,
        levelRange: String,
        lang: String,
        localExercises: List<Exercise>
    ): Exercise = withContext(Dispatchers.IO) {
        val url = BuildConfig.GEMINI_EXERCISE_GENERATION_URL
        if (url.isBlank()) error("GEMINI_EXERCISE_GENERATION_URL is empty in local.properties")

        val modelPrompt = buildExercisePrompt(prompt, selectedBodyPart, selectedEquipment, levelRange, lang, localExercises)
        logLarge("generateExercise.prompt", modelPrompt)

        val request = buildGeminiRequest(
            modelPrompt,
            temperature = 0.35
        )
        logLarge("generateExercise.requestBody", request)

        val responseText = post(url, request)
        val result = parseCandidateJson(responseText, "generateExercise")
        parseExercise(result, selectedBodyPart, selectedEquipment)
    }

    suspend fun generateTemplate(
        prompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        levelRange: String,
        lang: String,
        localExercises: List<Exercise>
    ): GeneratedTemplateDraft = withContext(Dispatchers.IO) {
        val url = BuildConfig.GEMINI_TEMPLATE_GENERATION_URL
        if (url.isBlank()) error("GEMINI_TEMPLATE_GENERATION_URL is empty in local.properties")
        if (localExercises.isEmpty()) error("No local exercises found")

        val allowedExercises = localExercises.filter { exercise ->
            val body = exercise.getBodyPart(lang)
            val equipment = exercise.getEquipment(lang)
            (selectedBodyParts.isEmpty() || selectedBodyParts.contains(body)) &&
                    (selectedEquipment.isEmpty() || selectedEquipment.contains(equipment))
        }.ifEmpty { localExercises }

        val modelPrompt = buildTemplatePrompt(prompt, selectedBodyParts, selectedEquipment, levelRange, lang, allowedExercises)
        logLarge("generateTemplate.prompt", modelPrompt)

        val request = buildGeminiRequest(
            modelPrompt,
            temperature = 0.25
        )
        logLarge("generateTemplate.requestBody", request)

        val responseText = post(url, request)
        val result = parseCandidateJson(responseText, "generateTemplate")
        parseTemplate(result, allowedExercises)
    }

    private fun buildGeminiRequest(prompt: String, temperature: Double): String {
        val root = JsonObject()
        root.add("contents", JsonArray().apply {
            add(JsonObject().apply {
                add("parts", JsonArray().apply {
                    add(JsonObject().apply { addProperty("text", prompt) })
                })
            })
        })
        root.add("generationConfig", JsonObject().apply {
            addProperty("temperature", temperature)
            addProperty("response_mime_type", "application/json")
        })
        return gson.toJson(root)
    }

    private fun buildExercisePrompt(
        userPrompt: String,
        selectedBodyPart: String,
        selectedEquipment: String,
        levelRange: String,
        lang: String,
        exercises: List<Exercise>
    ): String {
        val catalog = exercises.joinToString("\n") { exercise ->
            "- id=${exercise.id}; en='${exercise.getName("en")}'; ru='${exercise.getName("ru")}'; be='${exercise.getName("be")}'; bodyPart='${exercise.getBodyPart(lang)}'; equipment='${exercise.getEquipment(lang)}'; cardType='${exercise.cardType}'"
        }
        return """
            You generate ONE new exercise for a fitness Android app.
            The app supports exactly these languages: en, ru, be.
            User language: $lang.
            User request: $userPrompt
            Selected body part: $selectedBodyPart
            Selected equipment: $selectedEquipment
            User selected difficulty/load range: $levelRange

            Existing local exercises for style and duplication control:
            $catalog

            Rules:
            - Return ONLY compact valid JSON. No markdown.
            - Generate a NEW exercise, not an exact duplicate of existing exercises.
            - Use the selected body part and selected equipment.
            - Choose cardType correctly: STRENGTH, BIKE, or TREADMILL.
            - If the equipment/body part is cardio-related, choose BIKE or TREADMILL only when appropriate; otherwise choose STRENGTH.
            - Fill all localized fields for en, ru, be.
            - Instructions must contain 3 to 5 clear safe steps for every language.
            - secondaryMusclesLocalized may be empty arrays.

            JSON schema:
            {
              "nameLocalized":{"en":"","ru":"","be":""},
              "bodyPartLocalized":{"en":"","ru":"","be":""},
              "equipmentLocalized":{"en":"","ru":"","be":""},
              "targetLocalized":{"en":"","ru":"","be":""},
              "secondaryMusclesLocalized":{"en":[""],"ru":[""],"be":[""]},
              "cardType":"STRENGTH",
              "instructionsLocalized":{"en":[""],"ru":[""],"be":[""]},
              "note":""
            }
        """.trimIndent()
    }

    private fun buildTemplatePrompt(
        userPrompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        levelRange: String,
        lang: String,
        allowedExercises: List<Exercise>
    ): String {
        val catalog = allowedExercises.joinToString("\n") { exercise ->
            "- id=${exercise.id}; en='${exercise.getName("en")}'; ru='${exercise.getName("ru")}'; be='${exercise.getName("be")}'; bodyPart='${exercise.getBodyPart(lang)}'; equipment='${exercise.getEquipment(lang)}'; cardType='${exercise.cardType}'"
        }
        return """
            You generate ONE workout template for a fitness Android app.
            User language: $lang.
            User request: $userPrompt
            Selected body parts: ${selectedBodyParts.joinToString()}
            Selected equipment: ${selectedEquipment.joinToString()}
            User selected difficulty/load range: $levelRange

            Allowed local exercises. You MUST use ONLY these ids:
            $catalog

            Rules:
            - Return ONLY compact valid JSON. No markdown.
            - Do NOT invent exercises.
            - Use only exerciseId values from the allowed list.
            - Select exercises according to selected body parts and equipment.
            - Do not add abs/cardio/warmup unless requested.
            - Prefer 4 to 8 exercises.
            - Use 3 sets by default.
            - Use 12 repetitions by default for strength exercises.
            - Choose medium realistic weight/resistance/time values.
            - Fill template name for en, ru, be.

            JSON schema:
            {
              "nameLocalized":{"en":"","ru":"","be":""},
              "items":[
                {
                  "exerciseId":"existing_id",
                  "sets":[
                    {"rep":12,"weight":0.0,"time":0.0,"resistance":0.0,"incline":0.0},
                    {"rep":12,"weight":0.0,"time":0.0,"resistance":0.0,"incline":0.0},
                    {"rep":12,"weight":0.0,"time":0.0,"resistance":0.0,"incline":0.0}
                  ]
                }
              ]
            }
        """.trimIndent()
    }

    private fun post(proxyUrl: String, body: String): String {
        val connection = (URL(proxyUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            connectTimeout = 30_000
            readTimeout = 90_000
            doOutput = true
        }
        logLarge("post.url", proxyUrl)
        logLarge("post.requestBody", body)

        try {
            OutputStreamWriter(connection.outputStream).use { it.write(body) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.let { BufferedReader(InputStreamReader(it)).use { reader -> reader.readText() } }.orEmpty()
            Log.d(TAG, "post.responseCode=$code")
            logLarge("post.rawResponse", response)
            if (code !in 200..299) {
                if (code == 503 || response.contains("high demand", ignoreCase = true) || response.contains("overloaded", ignoreCase = true)) {
                    throw AiGenerationHighDemandException()
                }
                error("Gemini proxy error $code: $response")
            }
            return response
        } catch (error: UnknownHostException) {
            Log.e(TAG, "Cannot resolve Gemini proxy host. Check generation URL: $proxyUrl", error)
            throw AiGenerationNetworkException("Не удалось найти proxy-сервер. Проверь URL генерации в local.properties: $proxyUrl", error)
        } catch (error: SocketTimeoutException) {
            Log.e(TAG, "Gemini generation request timed out. proxyUrl=$proxyUrl", error)
            throw AiGenerationNetworkException("Истекло время ожидания ответа proxy-сервера. Проверь Worker и интернет-соединение.", error)
        } catch (error: ConnectException) {
            Log.e(TAG, "Cannot connect to Gemini generation proxy. proxyUrl=$proxyUrl", error)
            throw AiGenerationNetworkException("Не удалось подключиться к proxy-серверу. Проверь адрес Worker и доступность сети.", error)
        } catch (error: IOException) {
            Log.e(TAG, "Gemini generation IO error. proxyUrl=$proxyUrl", error)
            throw AiGenerationNetworkException("Ошибка сети при обращении к proxy-серверу: ${error.message}", error)
        }
    }

    private fun parseCandidateJson(response: String, operation: String): JsonObject {
        val root = JsonParser.parseString(response).asJsonObject
        val parts = root.getAsJsonArray("candidates")
            ?.firstOrNull()?.asJsonObject
            ?.getAsJsonObject("content")
            ?.getAsJsonArray("parts")
            ?: error("Gemini response does not contain parts")

        parts.forEachIndexed { index, element ->
            val part = element.asJsonObject
            val thought = part.get("thought")?.asBoolean == true
            val text = part.get("text")?.asString.orEmpty()
            logLarge("$operation.responsePart[$index].thought=$thought", text)
        }

        val text = parts
            .mapNotNull { element ->
                val part = element.asJsonObject
                val isThought = part.get("thought")?.asBoolean == true
                val partText = part.get("text")?.asString
                if (!isThought && !partText.isNullOrBlank()) partText else null
            }
            .lastOrNull()
            ?: parts.mapNotNull { it.asJsonObject.get("text")?.asString }
                .lastOrNull { it.isNotBlank() }
            ?: error("Gemini response does not contain text result")

        logLarge("$operation.extractedText", text)

        val cleaned = text
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        logLarge("$operation.cleanedJson", cleaned)

        return JsonParser.parseString(cleaned).asJsonObject
    }

    private fun logLarge(label: String, value: String) {
        if (value.isBlank()) {
            Log.d(TAG, "$label=<blank>")
            return
        }

        val chunkSize = 3500
        value.chunked(chunkSize).forEachIndexed { index, chunk ->
            Log.d(TAG, "$label part ${index + 1}: $chunk")
        }
    }

    private fun parseExercise(json: JsonObject, selectedBodyPart: String, selectedEquipment: String): Exercise {
        val cardType = json.get("cardType")?.asString?.uppercase(Locale.ROOT)
            ?.takeIf { it in ExerciseCardType.entries.map { type -> type.name } }
            ?: ExerciseCardType.STRENGTH.name
        return Exercise(
            id = UUID.randomUUID().toString(),
            nameLocalized = json.stringMap("nameLocalized"),
            bodyPartLocalized = json.stringMap("bodyPartLocalized").ensureNotBlank(selectedBodyPart),
            equipmentLocalized = json.stringMap("equipmentLocalized").ensureNotBlank(selectedEquipment),
            targetLocalized = json.stringMap("targetLocalized"),
            secondaryMusclesLocalized = json.stringListMap("secondaryMusclesLocalized"),
            cardType = cardType,
            instructionsLocalized = json.stringListMap("instructionsLocalized"),
            gifUrl = "",
            favorite = false,
            note = json.get("note")?.asString.orEmpty()
        ).normalizedForSave("en")
    }

    private fun parseTemplate(json: JsonObject, allowedExercises: List<Exercise>): GeneratedTemplateDraft {
        val byId = allowedExercises.associateBy { it.id }
        val items = json.getAsJsonArray("items")?.mapNotNull { element ->
            val item = element.asJsonObject
            val exercise = byId[item.get("exerciseId")?.asString] ?: return@mapNotNull null
            val sets = item.getAsJsonArray("sets")?.map { setElement ->
                val set = setElement.asJsonObject
                ExerciseSet(
                    rep = max(0, set.get("rep")?.asInt ?: 12),
                    weight = max(0f, set.get("weight")?.asFloat ?: 0f),
                    time = max(0f, set.get("time")?.asFloat ?: 0f),
                    resistance = max(0f, set.get("resistance")?.asFloat ?: 0f),
                    incline = max(0f, set.get("incline")?.asFloat ?: 0f),
                    status = SetStatus.NONE
                )
            }?.takeIf { it.isNotEmpty() } ?: List(3) { ExerciseSet(rep = 12, status = SetStatus.NONE) }
            ExtendedExercise(exercise = exercise, sets = sets)
        }.orEmpty()

        if (items.isEmpty()) error("Generated template does not contain valid local exercises")
        return GeneratedTemplateDraft(
            nameLocalized = json.stringMap("nameLocalized"),
            exercises = items.distinctBy { it.exercise.id }
        )
    }

    private fun JsonObject.stringMap(name: String): Map<String, String> {
        val obj = getAsJsonObject(name) ?: return emptyMap()
        return listOf("en", "ru", "be").associateWith { lang -> obj.get(lang)?.asString.orEmpty() }
    }

    private fun JsonObject.stringListMap(name: String): Map<String, List<String>> {
        val obj = getAsJsonObject(name) ?: return emptyMap()
        return listOf("en", "ru", "be").associateWith { lang ->
            obj.getAsJsonArray(lang)?.mapNotNull { it.asString.takeIf(String::isNotBlank) }.orEmpty()
        }
    }

    private fun Map<String, String>.ensureNotBlank(fallback: String): Map<String, String> {
        val safe = values.firstOrNull { it.isNotBlank() } ?: fallback
        return mapOf(
            "en" to (this["en"]?.takeIf { it.isNotBlank() } ?: safe),
            "ru" to (this["ru"]?.takeIf { it.isNotBlank() } ?: safe),
            "be" to (this["be"]?.takeIf { it.isNotBlank() } ?: safe)
        )
    }
}

data class GeneratedTemplateDraft(
    val nameLocalized: Map<String, String>,
    val exercises: List<ExtendedExercise>
)

class AiGenerationNetworkException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)

class AiGenerationHighDemandException(cause: Throwable? = null) : Exception(cause)
