package com.example.motivationcalendarapi.model

fun Template.localizedName(lang: String): String {
    val normalizedLang = when (lang.lowercase()) {
        "ru" -> "ru"
        "be", "by" -> "be"
        "en" -> "en"
        else -> "en"
    }

    return nameLocalized[normalizedLang]
        ?: nameLocalized["en"]
        ?: nameLocalized["ru"]
        ?: nameLocalized["be"]
        ?: name
}