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

    val bodyParts = listOf(
        LocalizedOption(
            key = "waist",
            localized = mapOf(
                "en" to "waist",
                "ru" to "талия",
                "be" to "талія"
            )
        ),
        LocalizedOption(
            key = "back",
            localized = mapOf(
                "en" to "back",
                "ru" to "спина",
                "be" to "спіна"
            )
        ),
        LocalizedOption(
            key = "chest",
            localized = mapOf(
                "en" to "chest",
                "ru" to "грудь",
                "be" to "грудзі"
            )
        ),
        LocalizedOption(
            key = "upper legs",
            localized = mapOf(
                "en" to "upper legs",
                "ru" to "верхние части ног",
                "be" to "верхнія часткі ног"
            )
        ),
        LocalizedOption(
            key = "biceps",
            localized = mapOf(
                "en" to "biceps",
                "ru" to "бицепс",
                "be" to "біцэпс"
            )
        ),
        LocalizedOption(
            key = "shoulders",
            localized = mapOf(
                "en" to "shoulders",
                "ru" to "плечи",
                "be" to "плячы"
            )
        ),
        LocalizedOption(
            key = "triceps",
            localized = mapOf(
                "en" to "triceps",
                "ru" to "трицепс",
                "be" to "трыцэпс"
            )
        ),
        LocalizedOption(
            key = "lower legs",
            localized = mapOf(
                "en" to "lower legs",
                "ru" to "нижние части ног",
                "be" to "ніжнія часткі ног"
            )
        ),
        LocalizedOption(
            key = "cardio",
            localized = mapOf(
                "en" to "cardio",
                "ru" to "кардио",
                "be" to "кардыё"
            )
        ),
        LocalizedOption(
            key = "neck",
            localized = mapOf(
                "en" to "neck",
                "ru" to "шея",
                "be" to "шыя"
            )
        )
    )

    val equipment = listOf(
        LocalizedOption(
            key = "rope",
            localized = mapOf(
                "en" to "rope",
                "ru" to "канат",
                "be" to "вяроўка"
            )
        ),
        LocalizedOption(
            key = "assisted",
            localized = mapOf(
                "en" to "assisted",
                "ru" to "с поддержкой",
                "be" to "з падтрымкай"
            )
        ),
        LocalizedOption(
            key = "body weight",
            localized = mapOf(
                "en" to "body weight",
                "ru" to "собственный вес",
                "be" to "уласны вес"
            )
        ),
        LocalizedOption(
            key = "weighted",
            localized = mapOf(
                "en" to "weighted",
                "ru" to "утяжеленный",
                "be" to "утяжелены"
            )
        ),
        LocalizedOption(
            key = "hammer",
            localized = mapOf(
                "en" to "hammer",
                "ru" to "хамер",
                "be" to "молат"
            )
        ),
        LocalizedOption(
            key = "bosu ball",
            localized = mapOf(
                "en" to "bosu ball",
                "ru" to "мяч босу",
                "be" to "мяч босу"
            )
        ),
        LocalizedOption(
            key = "smith machine",
            localized = mapOf(
                "en" to "smith machine",
                "ru" to "смита",
                "be" to "сміта"
            )
        ),
        LocalizedOption(
            key = "medicine ball",
            localized = mapOf(
                "en" to "medicine ball",
                "ru" to "медицинский мяч",
                "be" to "медыцынскі мяч"
            )
        ),
        LocalizedOption(
            key = "stability ball",
            localized = mapOf(
                "en" to "stability ball",
                "ru" to "фитбол",
                "be" to "фітбол"
            )
        ),
        LocalizedOption(
            key = "kettlebell",
            localized = mapOf(
                "en" to "kettlebell",
                "ru" to "гиря",
                "be" to "гіра"
            )
        ),
        LocalizedOption(
            key = "barbell",
            localized = mapOf(
                "en" to "barbell",
                "ru" to "штанга",
                "be" to "штанга"
            )
        ),
        LocalizedOption(
            key = "dumbbell",
            localized = mapOf(
                "en" to "dumbbell",
                "ru" to "гантель",
                "be" to "гантэль"
            )
        ),
        LocalizedOption(
            key = "cable",
            localized = mapOf(
                "en" to "cable",
                "ru" to "кабель",
                "be" to "кабель"
            )
        ),
        LocalizedOption(
            key = "sled machine",
            localized = mapOf(
                "en" to "sled machine",
                "ru" to "тренажер слэд",
                "be" to "тренажер слэд"
            )
        ),
        LocalizedOption(
            key = "band",
            localized = mapOf(
                "en" to "band",
                "ru" to "эспандер",
                "be" to "эспандэр"
            )
        ),
        LocalizedOption(
            key = "ez barbell",
            localized = mapOf(
                "en" to "ez barbell",
                "ru" to "EZ-штанга",
                "be" to "EZ-штанга"
            )
        ),
        LocalizedOption(
            key = "trap bar",
            localized = mapOf(
                "en" to "trap bar",
                "ru" to "трап-гриф",
                "be" to "трап-грыф"
            )
        ),
        LocalizedOption(
            key = "wheel roller",
            localized = mapOf(
                "en" to "wheel roller",
                "ru" to "ролик",
                "be" to "коцік"
            )
        ),
        LocalizedOption(
            key = "leverage machine",
            localized = mapOf(
                "en" to "leverage machine",
                "ru" to "рычажный тренажер",
                "be" to "рычагавы трэнажор"
            )
        ),
        LocalizedOption(
            key = "olympic barbell",
            localized = mapOf(
                "en" to "olympic barbell",
                "ru" to "олимпийская штанга",
                "be" to "алімпійская штанга"
            )
        )
    )
}