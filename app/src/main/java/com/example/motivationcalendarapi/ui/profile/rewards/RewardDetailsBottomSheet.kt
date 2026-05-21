package com.example.motivationcalendarapi.ui.profile.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.reward.RewardTier
import com.example.motivationcalendarapi.model.reward.RewardUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardDetailsBottomSheet(
    reward: RewardUiModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 720.dp)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, top = 10.dp, end = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RewardIcon(reward = reward, size = 118)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(reward.definition.strings.titleResId),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = reward.currentTier?.toDisplayName() ?: stringResource(R.string.reward_not_unlocked_yet),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(reward.definition.strings.descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
            SegmentedRewardProgress(progress = reward.progressToNextTier)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = buildProgressText(reward),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))
            RewardTierList(reward = reward)
        }
    }
}

@Composable
private fun SegmentedRewardProgress(progress: Float) {
    val safeProgress = progress.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (safeProgress > 0f) {
                Box(
                    modifier = Modifier
                        .weight(safeProgress)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            if (safeProgress < 1f) {
                Box(
                    modifier = Modifier
                        .weight(1f - safeProgress)
                        .height(8.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f))
                )
            }
        }
    }
}

@Composable
private fun RewardTierList(reward: RewardUiModel) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        reward.definition.thresholds.toSortedMap(compareBy { it.order }).forEach { (tier, target) ->
            val isCurrent = reward.currentTier == tier
            val isUnlocked = (reward.currentTier?.order ?: 0) >= tier.order
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isCurrent -> MaterialTheme.colorScheme.primaryContainer
                        isUnlocked -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 2.dp else 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TierDot(tier = tier, active = isUnlocked)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tier.toDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.reward_progress_format, formatNumber(reward.entity.progress), formatNumber(target)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = formatNumber(target),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TierDot(tier: RewardTier, active: Boolean) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(RoundedCornerShape(50))
            .background(
                if (active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant
            )
    )
}
