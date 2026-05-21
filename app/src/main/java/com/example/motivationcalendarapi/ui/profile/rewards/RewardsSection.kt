package com.example.motivationcalendarapi.ui.profile.rewards

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.reward.RewardUiModel

private const val COLLAPSED_REWARDS_COUNT = 3

@Composable
fun RewardsSection(
    rewards: List<RewardUiModel>,
    modifier: Modifier = Modifier
) {
    var selectedReward by remember { mutableStateOf<RewardUiModel?>(null) }
    var expanded by remember { mutableStateOf(false) }
    val sortedRewards = remember(rewards) { rewards.sortedForProfile() }
    val collapsedRewards = remember(sortedRewards) { sortedRewards.take(COLLAPSED_REWARDS_COUNT) }
    val hiddenRewards = remember(sortedRewards) { sortedRewards.drop(COLLAPSED_REWARDS_COUNT) }
    val hasHiddenRewards = hiddenRewards.isNotEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(durationMillis = 260)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.rewards_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(14.dp))

            RewardsGrid(
                rewards = collapsedRewards,
                onRewardClick = { selectedReward = it }
            )

            AnimatedVisibility(
                visible = expanded && hasHiddenRewards,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 280),
                    expandFrom = Alignment.Top
                ) + fadeIn(tween(durationMillis = 180)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 240),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(tween(durationMillis = 140))
            ) {
                Column {
                    Spacer(modifier = Modifier.height(18.dp))
                    RewardsGrid(
                        rewards = hiddenRewards,
                        onRewardClick = { selectedReward = it }
                    )
                }
            }

            if (hasHiddenRewards) {
                Spacer(modifier = Modifier.height(14.dp))
                ExpandRewardsHandle(
                    expanded = expanded,
                    onClick = { expanded = !expanded }
                )
            }
        }
    }

    selectedReward?.let { reward ->
        RewardDetailsBottomSheet(
            reward = reward,
            onDismiss = { selectedReward = null }
        )
    }
}

@Composable
private fun RewardsGrid(
    rewards: List<RewardUiModel>,
    onRewardClick: (RewardUiModel) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        rewards.chunked(3).forEach { rowRewards ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowRewards.forEach { reward ->
                    RewardItem(
                        reward = reward,
                        onClick = { onRewardClick(reward) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowRewards.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ExpandRewardsHandle(
    expanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(84.dp)
                .height(5.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.50f),
                    shape = RoundedCornerShape(50)
                )
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = stringResource(if (expanded) R.string.rewards_collapse else R.string.rewards_expand),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
