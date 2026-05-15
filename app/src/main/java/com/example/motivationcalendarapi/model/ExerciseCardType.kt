package com.example.motivationcalendarapi.model

enum class ExerciseCardType {
    STRENGTH,
    BIKE,
    TREADMILL
}

data class ExerciseCardTypeOption(
    val type: ExerciseCardType,
    val key: String,
    val localized: Map<String, String>
) {
    fun getLabel(lang: String): String {
        return localized[lang]
            ?: localized["en"]
            ?: key
    }
}

object ExerciseCardTypeCatalog {

    val cardTypes = listOf(
        ExerciseCardTypeOption(
            type = ExerciseCardType.STRENGTH,
            key = "strength",
            localized = mapOf(
                "en" to "strength",
                "ru" to "силовое упражнение",
                "be" to "сілавое практыкаванне"
            )
        ),
        ExerciseCardTypeOption(
            type = ExerciseCardType.BIKE,
            key = "bike",
            localized = mapOf(
                "en" to "exercise bike",
                "ru" to "велотренажёр",
                "be" to "велатрэнажор"
            )
        ),
        ExerciseCardTypeOption(
            type = ExerciseCardType.TREADMILL,
            key = "treadmill",
            localized = mapOf(
                "en" to "treadmill",
                "ru" to "беговая дорожка",
                "be" to "бегавая дарожка"
            )
        )
    )

    fun getOption(type: ExerciseCardType): ExerciseCardTypeOption {
        return cardTypes.firstOrNull { it.type == type }
            ?: cardTypes.first { it.type == ExerciseCardType.STRENGTH }
    }

    fun getLabel(type: ExerciseCardType, lang: String): String {
        return getOption(type).getLabel(lang)
    }

    fun parse(rawType: String?): ExerciseCardType {
        return runCatching {
            ExerciseCardType.valueOf(rawType.orEmpty().trim().uppercase())
        }.getOrDefault(ExerciseCardType.STRENGTH)
    }
}

fun Exercise.getCardType(): ExerciseCardType {
    return ExerciseCardTypeCatalog.parse(cardType)
}

fun Exercise.getCardType(lang: String): ExerciseCardType {
    return getCardType()
}

fun Exercise.getCardTypeLabel(lang: String): String {
    return ExerciseCardTypeCatalog.getLabel(getCardType(), lang)
}