package com.example.motivationcalendarapi.model.planning

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.motivationcalendarapi.model.ExtendedExercise
import java.util.UUID

@Entity(tableName = "planned_workouts")
data class PlannedWorkout(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val name: String = "",
    val nameLocalized: Map<String, String> = emptyMap(),
    val exercises: List<ExtendedExercise> = emptyList(),
    val sourceType: PlannedWorkoutSourceType = PlannedWorkoutSourceType.MANUAL,
    val linkedTemplateId: String? = null,
    val linkedCompletedWorkoutId: String? = null,
    val status: PlannedWorkoutStatus = PlannedWorkoutStatus.PLANNED,
    val aiReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class PlannedWorkoutStatus {
    PLANNED,
    COMPLETED,
    SKIPPED,
    MOVED,
    CANCELLED
}

enum class PlannedWorkoutSourceType {
    MANUAL,
    TEMPLATE,
    AI_GENERATED,
    AI_RECOMMENDED,
    COPIED_FROM_WORKOUT
}

fun PlannedWorkout.localizedName(lang: String): String {
    val normalized = when (lang.lowercase()) {
        "ru" -> "ru"
        "be", "by" -> "be"
        else -> "en"
    }
    return nameLocalized[normalized]
        ?: nameLocalized["en"]
        ?: nameLocalized["ru"]
        ?: nameLocalized["be"]
        ?: name
}
