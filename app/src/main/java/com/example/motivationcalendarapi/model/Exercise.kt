package com.example.motivationcalendarapi.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey val id: String,
    val bodyPart: String,
    val name: String,
    val equipment: String,
    val target: String,
    val secondaryMuscles: List<String>,
    @TypeConverters(Converters::class)
    val instructions: List<String>,
    val gifUrl: String,
    val isFavorite: Boolean = false
)

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
}
