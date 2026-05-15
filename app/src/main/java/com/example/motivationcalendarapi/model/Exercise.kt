package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.firebase.database.PropertyName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String = "",
    val bodyPartLocalized: Map<String, String> = emptyMap(),
    val nameLocalized: Map<String, String> = emptyMap(),
    val equipmentLocalized: Map<String, String> = emptyMap(),
    val targetLocalized: Map<String, String> = emptyMap(),
    val secondaryMusclesLocalized: Map<String, List<String>> = emptyMap(),
    val cardType: String = ExerciseCardType.STRENGTH.name,
    @TypeConverters(Converters::class)
    val instructionsLocalized: Map<String, List<String>> = emptyMap(),
    val gifUrl: String = "",
    @field:PropertyName("favorite")
    var favorite: Boolean = false,
    val note: String = ""
) {
    fun getBodyPart(lang: String): String =
        bodyPartLocalized[lang] ?: bodyPartLocalized["en"] ?: ""

    fun getName(lang: String): String =
        nameLocalized[lang] ?: nameLocalized["en"] ?: ""

    fun getEquipment(lang: String): String =
        equipmentLocalized[lang] ?: equipmentLocalized["en"] ?: ""

    fun getTarget(lang: String): String =
        targetLocalized[lang] ?: targetLocalized["en"] ?: ""

    fun getSecondaryMuscles(lang: String): List<String> =
        secondaryMusclesLocalized[lang] ?: secondaryMusclesLocalized["en"] ?: emptyList()

    fun getInstructions(lang: String): List<String> =
        instructionsLocalized[lang] ?: instructionsLocalized["en"] ?: emptyList()

    fun isValidForCreate(lang: String): Boolean {
        return getName(lang).isNotBlank() &&
                getBodyPart(lang).isNotBlank() &&
                getEquipment(lang).isNotBlank() &&
                getInstructions(lang).isNotEmpty()
    }

    fun normalizedForSave(currentLang: String): Exercise {
        val safeName = fillMissingStringLocalizations(nameLocalized, currentLang)
        val safeInstructions = fillMissingListLocalizations(instructionsLocalized, currentLang)

        return copy(
            nameLocalized = safeName,
            instructionsLocalized = safeInstructions
        )
    }

    private fun fillMissingStringLocalizations(
        source: Map<String, String>,
        currentLang: String
    ): Map<String, String> {
        val fallback = source[currentLang]
            ?: source["en"]
            ?: source["ru"]
            ?: source["be"]
            ?: ""

        return mapOf(
            "en" to (source["en"] ?: fallback),
            "ru" to (source["ru"] ?: fallback),
            "be" to (source["be"] ?: fallback)
        )
    }

    private fun fillMissingListLocalizations(
        source: Map<String, List<String>>,
        currentLang: String
    ): Map<String, List<String>> {
        val fallback = source[currentLang]
            ?: source["en"]
            ?: source["ru"]
            ?: source["be"]
            ?: emptyList()

        return mapOf(
            "en" to (source["en"] ?: fallback),
            "ru" to (source["ru"] ?: fallback),
            "be" to (source["be"] ?: fallback)
        )
    }
}


class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromExtendedExerciseList(value: List<ExtendedExercise>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toExtendedExerciseList(value: String?): List<ExtendedExercise>? {
        val type = object : TypeToken<List<ExtendedExercise>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? = gson.toJson(value)

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        return value?.let { gson.fromJson(it, object : TypeToken<Map<String, String>>() {}.type) }
    }

    @TypeConverter
    fun fromStringListMap(value: Map<String, List<String>>?): String? = gson.toJson(value)

    @TypeConverter
    fun toStringListMap(value: String?): Map<String, List<String>>? {
        return value?.let { gson.fromJson(it, object : TypeToken<Map<String, List<String>>>() {}.type) }
    }
}