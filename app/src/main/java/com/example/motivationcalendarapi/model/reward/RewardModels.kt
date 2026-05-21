package com.example.motivationcalendarapi.model.reward

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey

class RewardStringRefs(
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int
)

enum class RewardTier(val order: Int) {
    WOOD(1), BRONZE(2), SILVER(3), GOLD(4), PLATINUM(5)
}

enum class RewardType {
    TOTAL_WORKOUTS,
    EXERCISE_MAX_WEIGHT,
    DAILY_STEPS,
    WORKOUT_TOTAL_VOLUME,
    WORKOUT_TOTAL_REPS,
    WORKOUT_STREAK,
    WEEKLY_CONSISTENCY,
    BODY_PROGRESS_ENTRIES,
    AI_EXERCISE_CREATED,
    AI_TEMPLATE_CREATED,
    HEALTH_CONNECT_CONNECTED,
    EQUIPMENT_RECOGNIZER_USED
}

data class RewardDefinition(
    val id: String,
    val type: RewardType,
    val strings: RewardStringRefs,
    val foregroundIconName: String,
    val exerciseId: String? = null,
    val thresholds: Map<RewardTier, Double>
) {
    fun targetForNextTier(currentTier: RewardTier?): Double? {
        return thresholds
            .toSortedMap(compareBy { it.order })
            .entries
            .firstOrNull { (tier, _) -> tier.order > (currentTier?.order ?: 0) }
            ?.value
    }

    fun resolveTier(progress: Double): RewardTier? {
        return thresholds
            .filterValues { progress >= it }
            .keys
            .maxByOrNull { it.order }
    }
}

@Entity(tableName = "reward_table")
data class RewardEntity(
    @PrimaryKey val rewardId: String = "",
    val type: String = "",
    val exerciseId: String? = null,
    val currentTier: String? = null,
    val progress: Double = 0.0,
    val target: Double = 0.0,
    val unlockedAt: Long? = null,
    val updatedAt: Long = 0L,
    val isUnlocked: Boolean = false
)

@Entity(tableName = "reward_unlock_event_table")
data class RewardUnlockEventEntity(
    @PrimaryKey val eventId: String = "",
    val rewardId: String = "",
    val tier: String = "",
    val createdAt: Long = 0L,
    val shown: Boolean = false
)

data class RewardUiModel(
    val entity: RewardEntity,
    val definition: RewardDefinition,
    val currentTier: RewardTier?,
    val progressToNextTier: Float,
    val targetForNextTier: Double?
)
