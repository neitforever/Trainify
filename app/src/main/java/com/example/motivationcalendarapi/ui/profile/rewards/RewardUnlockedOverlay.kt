package com.example.motivationcalendarapi.ui.profile.rewards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.reward.RewardUiModel
import com.example.motivationcalendarapi.model.reward.RewardUnlockEventEntity
import kotlinx.coroutines.delay

@Composable
fun RewardUnlockedOverlay(
    events: List<RewardUnlockEventEntity>,
    rewards: List<RewardUiModel>,
    onShown: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val event = events.firstOrNull()
    val reward = event?.let { current -> rewards.firstOrNull { it.entity.rewardId == current.rewardId } }
    var visible by remember(event?.eventId) { mutableStateOf(false) }

    LaunchedEffect(event?.eventId) {
        if (event != null) {
            visible = true
            delay(2200)
            visible = false
            delay(1000)
            onShown(event.eventId)
        }
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedVisibility(
            visible = visible && event != null && reward != null,
            enter = slideInVertically(animationSpec = tween(260), initialOffsetY = { -it }) + fadeIn(tween(220)),
            exit = slideOutVertically(animationSpec = tween(220), targetOffsetY = { -it }) + fadeOut(tween(180))
        ) {
            if (reward != null && event != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RewardIcon(reward = reward, size = 58)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.reward_new),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(reward.definition.strings.titleResId),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.tier.toTierDisplayName(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
