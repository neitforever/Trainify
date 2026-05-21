package com.example.motivationcalendarapi.ui.profile.rewards

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.reward.RewardTier
import com.example.motivationcalendarapi.model.reward.RewardUiModel

@Composable
fun RewardItem(
    reward: RewardUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RewardIcon(reward = reward, size = 68)
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = stringResource(reward.definition.strings.titleResId),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = reward.currentTier?.toDisplayName() ?: stringResource(R.string.reward_locked),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun RewardIcon(
    reward: RewardUiModel,
    size: Int
) {
    val backgroundRes = backgroundResForTier(reward.currentTier)
    val foregroundRes = foregroundResForName(reward.definition.foregroundIconName)
    val title = stringResource(reward.definition.strings.titleResId)
    val unlockedAlpha = if (reward.currentTier == null) 0.42f else 1f
    val foregroundAlpha = if (reward.currentTier == null) 0.34f else 1f

    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier
                .size(size.dp)
                .alpha(unlockedAlpha)
        )
        Image(
            painter = painterResource(id = foregroundRes),
            contentDescription = title,
            modifier = Modifier
                .size((size * 0.52f).dp)
                .alpha(foregroundAlpha)
        )
    }
}

fun backgroundResForTier(tier: RewardTier?): Int = when (tier) {
    RewardTier.WOOD -> R.drawable.ic_reward_bg_wood
    RewardTier.BRONZE -> R.drawable.ic_reward_bg_bronze
    RewardTier.SILVER -> R.drawable.ic_reward_bg_silver
    RewardTier.GOLD -> R.drawable.ic_reward_bg_gold
    RewardTier.PLATINUM -> R.drawable.ic_reward_bg_platinum
    null -> R.drawable.ic_reward_bg_locked
}

fun foregroundResForName(name: String): Int = when (name) {
    "reward_fg_workouts" -> R.drawable.ic_reward_fg_workouts
    "reward_fg_total_weight" -> R.drawable.ic_reward_fg_total_weight
    "reward_fg_reps" -> R.drawable.ic_reward_fg_reps
    "reward_fg_steps" -> R.drawable.ic_reward_fg_steps
    "reward_fg_streak" -> R.drawable.ic_reward_fg_streak
    "reward_fg_consistency" -> R.drawable.ic_reward_fg_consistency
    "reward_fg_body_progress" -> R.drawable.ic_reward_fg_body_progress
    "reward_fg_ai_exercise" -> R.drawable.ic_reward_fg_ai_exercise
    "reward_fg_ai_template" -> R.drawable.ic_reward_fg_ai_template
    "reward_fg_health_connect" -> R.drawable.ic_reward_fg_health_connect
    "reward_fg_equipment_recognizer" -> R.drawable.ic_reward_fg_equipment_recognizer
    else -> R.drawable.ic_reward_fg_exercise_record
}
