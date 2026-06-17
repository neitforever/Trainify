package com.example.motivationcalendarapi.ui.workout.fragments

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ClusterSetData
import com.example.motivationcalendarapi.model.ClusterSetPart
import com.example.motivationcalendarapi.model.ExerciseSet
import com.example.motivationcalendarapi.model.ExerciseSetType
import com.example.motivationcalendarapi.model.SetStatus
import com.example.motivationcalendarapi.ui.fragments.StatusIcon
import com.example.motivationcalendarapi.utils.formatCompactDecimal

@Composable
fun AdvancedSetReadonlyCard(
    set: ExerciseSet,
    modifier: Modifier = Modifier
) {
    when (set.type) {
        ExerciseSetType.DROP_SET -> DropSetReadonlyCard(set = set, modifier = modifier)
        ExerciseSetType.CLUSTER_SET -> ClusterSetReadonlyCard(set = set, modifier = modifier)
        ExerciseSetType.NORMAL -> Unit
    }
}

@Composable
private fun DropSetReadonlyCard(
    set: ExerciseSet,
    modifier: Modifier = Modifier
) {
    AdvancedSetReadonlyContainer(
        title = stringResource(R.string.drop_set),
        iconRes = R.drawable.ic_drop_set,
        modifier = modifier
    ) {
        AdvancedSetReadonlyHeader(
            first = stringResource(R.string.part),
            second = stringResource(R.string.rep),
            third = stringResource(R.string.Weight),
            fourth = stringResource(R.string.status)
        )

        set.dropSetParts.forEachIndexed { partIndex, part ->
            AdvancedSetReadonlyRow(
                firstValue = (partIndex + 1).toString(),
                repValue = part.rep.toString(),
                weightValue = formatCompactDecimal(part.weight),
                status = part.status
            )
        }
    }
}

@Composable
private fun ClusterSetReadonlyCard(
    set: ExerciseSet,
    modifier: Modifier = Modifier
) {
    val rest = set.clusterSetData?.restBetweenClustersSec ?: 20
    val clusterParts = clusterPartsFromSet(set)

    AdvancedSetReadonlyContainer(
        title = stringResource(R.string.cluster_set),
        iconRes = R.drawable.ic_cluster_set,
        modifier = modifier
    ) {
        AdvancedSetReadonlyMetricCell(
            title = stringResource(R.string.cluster_rest),
            value = stringResource(R.string.seconds_short_value, rest),
            modifier = Modifier.fillMaxWidth()
        )

        AdvancedSetReadonlyHeader(
            first = stringResource(R.string.part),
            second = stringResource(R.string.rep),
            third = stringResource(R.string.Weight),
            fourth = stringResource(R.string.status)
        )

        clusterParts.forEachIndexed { clusterIndex, cluster ->
            AdvancedSetReadonlyRow(
                firstValue = (clusterIndex + 1).toString(),
                repValue = cluster.rep.toString(),
                weightValue = formatCompactDecimal(cluster.weight),
                status = cluster.status
            )
        }
    }
}

private fun clusterPartsFromSet(set: ExerciseSet): List<ClusterSetPart> {
    if (set.clusterSetParts.isNotEmpty()) return set.clusterSetParts
    val cluster = set.clusterSetData ?: ClusterSetData(
        weight = set.weight,
        clusterCount = 1,
        repsPerCluster = set.rep.coerceAtLeast(1),
        restBetweenClustersSec = 20
    )
    return List(cluster.clusterCount.coerceAtLeast(1)) {
        ClusterSetPart(
            weight = cluster.weight,
            rep = cluster.repsPerCluster.coerceAtLeast(1),
            status = SetStatus.NONE
        )
    }
}

@Composable
private fun AdvancedSetReadonlyContainer(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.44f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdvancedTechniqueReadonlyBadge(text = title, iconRes = iconRes)
            content()
        }
    }
}

@Composable
private fun AdvancedSetReadonlyMetricCell(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(54.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedSetReadonlyHeader(
    first: String,
    second: String,
    third: String,
    fourth: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdvancedReadonlyHeaderText(text = first, modifier = Modifier.width(54.dp))
        AdvancedReadonlyHeaderText(text = second, modifier = Modifier.weight(1f))
        AdvancedReadonlyHeaderText(text = third, modifier = Modifier.weight(1f))
        AdvancedReadonlyHeaderText(text = fourth, modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun AdvancedReadonlyHeaderText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AdvancedSetReadonlyRow(
    firstValue: String,
    repValue: String,
    weightValue: String,
    status: SetStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AdvancedSetReadonlyPlainCell(value = firstValue, modifier = Modifier.width(54.dp))
        AdvancedSetReadonlyValueCell(value = repValue, modifier = Modifier.weight(1f))
        AdvancedSetReadonlyValueCell(value = weightValue, modifier = Modifier.weight(1f))
        AdvancedSetReadonlyStatusCell(status = status, modifier = Modifier.width(64.dp))
    }
}

@Composable
private fun AdvancedSetReadonlyPlainCell(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedSetReadonlyValueCell(
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.small
            )
            .padding(horizontal = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value,
            style = if (value.isLongTimerCell()) {
                MaterialTheme.typography.labelSmall.copy(fontSize = 15.sp, lineHeight = 15.sp, letterSpacing = 0.sp)
            } else {
                MaterialTheme.typography.bodyLarge
            },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AdvancedSetReadonlyStatusCell(
    status: SetStatus,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        StatusIcon(status = status)
    }
}

@Composable
private fun AdvancedTechniqueReadonlyBadge(
    text: String,
    iconRes: Int
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun String.isLongTimerCell(): Boolean {
    return contains(":") && length >= 6
}
