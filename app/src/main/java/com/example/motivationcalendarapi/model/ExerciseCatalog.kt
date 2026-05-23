package com.example.motivationcalendarapi.model

data class LocalizedOption(
    val key: String,
    val localized: Map<String, String>
) {
    fun getLabel(lang: String): String {
        return localized[lang] ?: localized["en"] ?: key
    }
}

object ExerciseCatalog {
    val bodyParts: List<LocalizedOption> = BodyPart.all.map { bodyPart ->
        LocalizedOption(
            key = bodyPart.key,
            localized = bodyPart.toLocalizedMap()
        )
    }

    val equipment: List<LocalizedOption> = Equipment.all.map { equipment ->
        LocalizedOption(
            key = equipment.key,
            localized = equipment.toLocalizedMap()
        )
    }
}
