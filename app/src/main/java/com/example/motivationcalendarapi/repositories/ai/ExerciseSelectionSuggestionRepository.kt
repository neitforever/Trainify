package com.example.motivationcalendarapi.repositories.ai

import com.example.motivationcalendarapi.model.LocalizedOption

class ExerciseSelectionSuggestionRepository(
    private val api: GeminiAiGenerationApi = GeminiAiGenerationApi()
) {
    suspend fun suggestBodyPart(
        exerciseName: String,
        lang: String,
        options: List<LocalizedOption>
    ): String? {
        return suggestSelection(
            exerciseName = exerciseName,
            selectionType = "body_part",
            lang = lang,
            options = options
        )
    }

    suspend fun suggestEquipment(
        exerciseName: String,
        lang: String,
        options: List<LocalizedOption>
    ): String? {
        return suggestSelection(
            exerciseName = exerciseName,
            selectionType = "equipment",
            lang = lang,
            options = options
        )
    }

    private suspend fun suggestSelection(
        exerciseName: String,
        selectionType: String,
        lang: String,
        options: List<LocalizedOption>
    ): String? {
        val apiOptions = options.map { option ->
            SelectionSuggestionOption(
                key = option.key,
                en = option.localized["en"].orEmpty(),
                ru = option.localized["ru"].orEmpty(),
                be = option.localized["be"].orEmpty()
            )
        }

        return api.suggestExerciseSelection(
            exerciseName = exerciseName,
            selectionType = selectionType,
            lang = lang,
            options = apiOptions
        )
    }
}
