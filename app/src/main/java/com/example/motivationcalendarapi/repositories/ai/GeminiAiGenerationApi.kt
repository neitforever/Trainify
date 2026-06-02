package com.example.motivationcalendarapi.repositories.ai

import android.util.Log
import com.example.motivationcalendarapi.BuildConfig
import com.example.motivationcalendarapi.model.Exercise
import com.example.motivationcalendarapi.model.Equipment
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.getCardType
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
        difficulty: String,
        minExercises: Int,
        maxExercises: Int,
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

        val modelPrompt = buildTemplatePrompt(prompt, selectedBodyParts, selectedEquipment, difficulty, minExercises, maxExercises, lang, allowedExercises)
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


    suspend fun translateTemplateName(
        name: String,
        sourceLang: String
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val url = BuildConfig.GEMINI_TRANSLATION_URL
        if (url.isBlank()) error("GEMINI_TRANSLATION_URL is empty in local.properties")

        val trimmedName = name.trim()
        if (trimmedName.isBlank()) return@withContext emptyMap()

        val modelPrompt = buildTemplateNameTranslationPrompt(trimmedName, sourceLang)
        logLarge("translateTemplateName.prompt", modelPrompt)

        val request = buildGeminiRequest(
            modelPrompt,
            temperature = 0.05
        )
        logLarge("translateTemplateName.requestBody", request)

        val responseText = post(url, request)
        val result = parseCandidateJson(responseText, "translateTemplateName")
        result.stringMap("nameLocalized").ensureNotBlank(trimmedName)
    }


    suspend fun suggestExerciseSelection(
        exerciseName: String,
        selectionType: String,
        lang: String,
        options: List<SelectionSuggestionOption>
    ): String? = withContext(Dispatchers.IO) {
        val url = BuildConfig.GEMINI_SELECTION_SUGGESTION_URL
        if (url.isBlank()) error("GEMINI_SELECTION_SUGGESTION_URL is empty in local.properties")

        val safeExerciseName = exerciseName.trim()
        if (safeExerciseName.isBlank() || options.isEmpty()) return@withContext null

        val modelPrompt = buildSelectionSuggestionPrompt(
            exerciseName = safeExerciseName,
            selectionType = selectionType,
            lang = lang,
            options = options
        )
        logLarge("suggestExerciseSelection.prompt", modelPrompt)

        val request = buildGeminiRequest(
            modelPrompt,
            temperature = 0.1
        )
        logLarge("suggestExerciseSelection.requestBody", request)

        val responseText = post(url, request)
        val result = parseCandidateJson(responseText, "suggestExerciseSelection")
        val selectedKey = result.get("selectedKey")?.asString?.trim().orEmpty()
        selectedKey.takeIf { key -> options.any { it.key == key } }
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



    private fun buildTemplateNameTranslationPrompt(name: String, sourceLang: String): String {
        val normalizedLang = when (sourceLang.lowercase(Locale.ROOT)) {
            "ru" -> "ru"
            "be", "by" -> "be"
            "en" -> "en"
            else -> "en"
        }

        return """
            Translate a user-created workout template name into three UI languages.
            Source language code: $normalizedLang
            Source name: $name

            Requirements:
            - Return ONLY compact valid JSON. No markdown.
            - Preserve the meaning and fitness context.
            - Do not add explanations, emojis, quotes, or extra words.
            - Use natural short template names for a fitness app.
            - If the name contains a proper noun, brand, or untranslatable token, preserve it.

            JSON schema:
            {"nameLocalized":{"en":"","ru":"","be":""}}
        """.trimIndent()
    }

    private fun buildSelectionSuggestionPrompt(
        exerciseName: String,
        selectionType: String,
        lang: String,
        options: List<SelectionSuggestionOption>
    ): String {
        val normalizedType = when (selectionType) {
            "body_part" -> "body part / primary muscle group"
            "equipment" -> "equipment"
            else -> selectionType
        }
        val catalog = options.joinToString("\n") { option ->
            "- key=${option.key}; en='${option.en}'; ru='${option.ru}'; be='${option.be}'"
        }

        return """
            You help choose the best $normalizedType for a fitness exercise.
            Exercise name: $exerciseName
            User language: $lang

            Available options. You MUST choose exactly one key from this list:
            $catalog

            Rules:
            - Return ONLY compact valid JSON. No markdown.
            - Do not invent keys.
            - If the exercise name is ambiguous, choose the most common fitness meaning.
            - Prefer the primary trained muscle group for body_part.
            - Prefer the most typical required equipment for equipment.

            JSON schema:
            {"selectedKey":"existing_key"}
        """.trimIndent()
    }

    private fun buildExercisePrompt(
        userPrompt: String,
        selectedBodyPart: String,
        selectedEquipment: String,
        levelRange: String,
        lang: String,
        exercises: List<Exercise>
    ): String {
        val currentLang = lang.takeIf { it in listOf("en", "ru", "be") } ?: "en"
        val existingNames = exercises
            .filter { exercise ->
                val bodyPart = exercise.getBodyPart(currentLang)
                val equipment = exercise.getEquipment(currentLang)
                bodyPart.equals(selectedBodyPart, ignoreCase = true) ||
                        (selectedEquipment.isNotBlank() && equipment.equals(selectedEquipment, ignoreCase = true))
            }
            .ifEmpty { exercises }
            .mapIndexed { index, exercise ->
                val localizedName = exercise.getName(currentLang).ifBlank { exercise.getName("en") }
                "${index + 1}. ${localizedName} / ${exercise.getName("en")}"
            }
            .distinct()
            .take(80)
            .joinToString("\n")

        val equipmentCatalog = Equipment.all.joinToString("\n") { equipment ->
            val names = equipment.toLocalizedMap()
            "- en='${names["en"]}'; ru='${names["ru"]}'; be='${names["be"]}'"
        }
        val equipmentRule = if (selectedEquipment.isBlank()) {
            "Equipment is optional: choose exactly ONE most suitable equipment from the available equipment list below and put it into equipmentLocalized."
        } else {
            "Required equipment: $selectedEquipment. Use this equipment in equipmentLocalized."
        }

        return """
            Generate ONE new fitness exercise.
            Language for user-facing style: $currentLang.
            User request: $userPrompt
            Required body part: $selectedBodyPart
            $equipmentRule
            Difficulty/load range: $levelRange

            Available equipment. If equipment is optional, choose only one item from this list:
            $equipmentCatalog

            Avoid duplicating these existing exercise names:
            $existingNames

            Rules:
            - Return ONLY compact valid JSON. No markdown.
            - The generated exercise must use the required body part.
            - If equipment was not selected by the user, infer the most appropriate equipment from the exercise concept and the available equipment list.
            - Do not invent equipment outside the available equipment list.
            - Fill en, ru, be fields.
            - Choose cardType: STRENGTH, BIKE, or TREADMILL.
            - Use BIKE/TREADMILL only for cardio-machine exercises; otherwise use STRENGTH.
            - Instructions: 3 to 5 short safe steps per language.
            - secondaryMusclesLocalized may be empty arrays.

            JSON schema:
            {"nameLocalized":{"en":"","ru":"","be":""},"bodyPartLocalized":{"en":"","ru":"","be":""},"equipmentLocalized":{"en":"","ru":"","be":""},"targetLocalized":{"en":"","ru":"","be":""},"secondaryMusclesLocalized":{"en":[""],"ru":[""],"be":[""]},"cardType":"STRENGTH","instructionsLocalized":{"en":[""],"ru":[""],"be":[""]},"note":""}
        """.trimIndent()
    }

    private fun buildTemplatePrompt(
        userPrompt: String,
        selectedBodyParts: List<String>,
        selectedEquipment: List<String>,
        difficulty: String,
        minExercises: Int,
        maxExercises: Int,
        lang: String,
        allowedExercises: List<Exercise>
    ): String {
        val equipmentRule = if (selectedEquipment.isEmpty()) {
            "Selected equipment: Let AI choose. Use any suitable equipment from the allowed local exercises; do not invent new equipment."
        } else {
            "Selected equipment: ${selectedEquipment.joinToString()}. Select exercises according to this equipment list."
        }
        val catalog = allowedExercises.joinToString("\n") { exercise ->
            "- id=${exercise.id}; en='${exercise.getName("en")}'; ru='${exercise.getName("ru")}'; be='${exercise.getName("be")}'; bodyPart='${exercise.getBodyPart(lang)}'; equipment='${exercise.getEquipment(lang)}'; cardType='${exercise.cardType}'"
        }
        return """
            You generate ONE workout template for a fitness Android app.
            User language: $lang.
            User request: $userPrompt
            Selected body parts: ${selectedBodyParts.joinToString()}
            $equipmentRule
            Selected difficulty: $difficulty
            Required exercise count range: from $minExercises to $maxExercises exercises

            Allowed local exercises. You MUST use ONLY these ids:
            $catalog

            Rules:
            - Return ONLY compact valid JSON. No markdown.
            - Do NOT invent exercises.
            - Use only exerciseId values from the allowed list.
            - Select exercises according to selected body parts.
            - If equipment was selected manually, respect that equipment list.
            - If equipment is set to Let AI choose, choose suitable equipment from the allowed local exercises only.
            - Do not add abs/cardio/warmup unless requested.
            - Use at most ONE cardio exercise in the whole template.
            - Generate from $minExercises to $maxExercises exercises.
            - Use the selected difficulty to choose realistic load values.
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
                if (code == 500 || code == 503 || response.contains("high demand", ignoreCase = true) || response.contains("overloaded", ignoreCase = true)) {
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
            equipmentLocalized = json.stringMap("equipmentLocalized").ensureNotBlank(selectedEquipment.ifBlank { Equipment.BodyWeight.getLabel("en") }),
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
        var cardioAdded = false
        val items = json.getAsJsonArray("items")?.mapNotNull { element ->
            val item = element.asJsonObject
            val exercise = byId[item.get("exerciseId")?.asString] ?: return@mapNotNull null
            val cardType = exercise.getCardType("en")

            if (cardType != ExerciseCardType.STRENGTH) {
                if (cardioAdded) return@mapNotNull null
                cardioAdded = true
            }

            val parsedSets = item.getAsJsonArray("sets")?.map { setElement ->
                val set = setElement.asJsonObject
                sanitizeGeneratedTemplateSet(
                    cardType = cardType,
                    rep = set.get("rep")?.asInt,
                    weight = set.get("weight")?.asFloat,
                    time = set.get("time")?.asFloat,
                    resistance = set.get("resistance")?.asFloat,
                    incline = set.get("incline")?.asFloat
                )
            }.orEmpty()
            val sets = when (cardType) {
                ExerciseCardType.STRENGTH -> parsedSets.takeIf { it.isNotEmpty() } ?: defaultTemplateSets(cardType)
                ExerciseCardType.BIKE,
                ExerciseCardType.TREADMILL -> listOf(parsedSets.firstOrNull() ?: defaultTemplateSets(cardType).first())
            }

            ExtendedExercise(exercise = exercise, sets = sets)
        }.orEmpty().distinctBy { it.exercise.id }

        if (items.isEmpty()) error("Generated template does not contain valid local exercises")
        return GeneratedTemplateDraft(
            nameLocalized = json.stringMap("nameLocalized"),
            exercises = items
        )
    }

    private fun sanitizeGeneratedTemplateSet(
        cardType: ExerciseCardType,
        rep: Int?,
        weight: Float?,
        time: Float?,
        resistance: Float?,
        incline: Float?
    ): ExerciseSet {
        return when (cardType) {
            ExerciseCardType.STRENGTH -> ExerciseSet(
                rep = max(1, rep ?: 12),
                weight = max(0f, weight ?: 30f),
                status = SetStatus.NONE
            )

            ExerciseCardType.BIKE -> ExerciseSet(
                time = max(1f, time?.takeIf { it > 0f } ?: 20f),
                resistance = max(1f, resistance?.takeIf { it > 0f } ?: 5f),
                status = SetStatus.NONE
            )

            ExerciseCardType.TREADMILL -> ExerciseSet(
                time = max(1f, time?.takeIf { it > 0f } ?: 20f),
                resistance = max(1f, resistance?.takeIf { it > 0f } ?: 6f),
                incline = max(0f, incline?.takeIf { it > 0f } ?: 3f),
                status = SetStatus.NONE
            )
        }
    }

    private fun defaultTemplateSets(cardType: ExerciseCardType): List<ExerciseSet> {
        return when (cardType) {
            ExerciseCardType.STRENGTH -> List(3) { ExerciseSet(rep = 12, weight = 30f, status = SetStatus.NONE) }
            ExerciseCardType.BIKE -> listOf(ExerciseSet(time = 20f, resistance = 5f, status = SetStatus.NONE))
            ExerciseCardType.TREADMILL -> listOf(ExerciseSet(time = 20f, resistance = 6f, incline = 3f, status = SetStatus.NONE))
        }
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


data class SelectionSuggestionOption(
    val key: String,
    val en: String,
    val ru: String,
    val be: String
)
