package com.example.motivationcalendarapi.ui.workout.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalendarDay(
    day: Int,
    hasWorkout: Boolean,
    hasManualPlan: Boolean,
    hasAiPlan: Boolean,
    hasSkippedPlan: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    workoutCount: Int = 0,
    plannedCount: Int = 0
) {
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = when {
        hasAiPlan -> colorScheme.tertiaryContainer.copy(alpha = 0.72f)
        hasManualPlan -> colorScheme.primaryContainer.copy(alpha = 0.62f)
        hasWorkout -> colorScheme.primaryContainer
        hasSkippedPlan -> colorScheme.errorContainer.copy(alpha = 0.70f)
        else -> colorScheme.surfaceVariant
    }
    val borderColor = when {
        isToday -> colorScheme.primary
        hasAiPlan -> colorScheme.tertiary
        hasManualPlan -> colorScheme.primary
        hasSkippedPlan -> colorScheme.error
        else -> colorScheme.outline
    }

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .border(
                width = if (isToday || hasManualPlan || hasAiPlan) 1.6.dp else 1.dp,
                color = borderColor,
                shape = MaterialTheme.shapes.small
            ),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = when {
                    hasSkippedPlan -> colorScheme.onErrorContainer
                    hasAiPlan -> colorScheme.onTertiaryContainer
                    hasWorkout || hasManualPlan -> colorScheme.primary
                    else -> colorScheme.onSurface
                },
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasWorkout) IndicatorDot(colorScheme.primary)
                if (hasManualPlan) IndicatorDot(colorScheme.secondary)
                if (hasAiPlan) IndicatorDot(colorScheme.tertiary)
                if (hasSkippedPlan) IndicatorDot(colorScheme.error)
            }

            val totalCount = workoutCount + plannedCount
            if (totalCount > 1) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(if (hasAiPlan) colorScheme.tertiary else colorScheme.primary)
                        .size(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = totalCount.toString(),
                        modifier = Modifier.offset(y = (-0.8).dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        lineHeight = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasAiPlan) colorScheme.onTertiary else colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(color)
    )
}
