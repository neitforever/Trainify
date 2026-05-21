package com.example.motivationcalendarapi.ui.profile.rewards

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.reward.RewardTier
import com.example.motivationcalendarapi.model.reward.RewardUiModel

@Composable
fun RewardTier.toDisplayName(): String = when (this) {
    RewardTier.WOOD -> stringResource(R.string.reward_tier_wood)
    RewardTier.BRONZE -> stringResource(R.string.reward_tier_bronze)
    RewardTier.SILVER -> stringResource(R.string.reward_tier_silver)
    RewardTier.GOLD -> stringResource(R.string.reward_tier_gold)
    RewardTier.PLATINUM -> stringResource(R.string.reward_tier_platinum)
}

@Composable
fun String.toTierDisplayName(): String = runCatching { RewardTier.valueOf(this) }.getOrNull()?.toDisplayName() ?: this

@Composable
fun buildProgressText(reward: RewardUiModel): String {
    val target = reward.targetForNextTier ?: return stringResource(R.string.reward_max_tier_unlocked)
    return stringResource(R.string.reward_progress_format, formatNumber(reward.entity.progress), formatNumber(target))
}

fun formatNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format("%.1f", value)
}

fun List<RewardUiModel>.sortedForProfile(): List<RewardUiModel> = sortedWith(
    compareByDescending<RewardUiModel> { it.currentTier?.order ?: 0 }
        .thenBy { it.currentTier == null }
        .thenBy { it.definition.id }
)
