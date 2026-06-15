package com.example.motivationcalendarapi.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.motivationcalendarapi.R
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MAX_TIMER_MINUTES = 300
private const val MAX_TIMER_SECONDS = MAX_TIMER_MINUTES * 60 + 59
private const val WHEEL_ITEM_COUNT = 100_000
private val WheelViewportHeight = 232.dp
private val WheelItemHeight = 32.dp
private val WheelCenterHighlightHeight = 38.dp

@Composable
fun TimeMetricDialog(
    showDialog: Boolean,
    title: String,
    initialValueMinutes: Float,
    minValueMinutes: Float,
    maxValueMinutes: Float,
    stepValueMinutes: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    if (!showDialog) return

    val initialSeconds = ((initialValueMinutes * 60f).roundToInt())
        .coerceIn(0, MAX_TIMER_SECONDS)

    var minutes by remember(initialValueMinutes) {
        mutableIntStateOf((initialSeconds / 60).coerceIn(0, MAX_TIMER_MINUTES))
    }
    var seconds by remember(initialValueMinutes) {
        mutableIntStateOf((initialSeconds % 60).coerceIn(0, 59))
    }

    val selectedTotalSeconds = (minutes * 60 + seconds).coerceIn(0, MAX_TIMER_SECONDS)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = formatTimerValue(selectedTotalSeconds),
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TimeWheelPicker(
                        value = minutes,
                        range = 0..MAX_TIMER_MINUTES,
                        label = stringResource(R.string.minutes_short),
                        modifier = Modifier.weight(1f),
                        onValueChange = { newMinutes -> minutes = newMinutes }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .width(16.dp)
                            .padding(bottom = 26.dp)
                    )

                    TimeWheelPicker(
                        value = seconds,
                        range = 0..59,
                        label = stringResource(R.string.seconds_short),
                        modifier = Modifier.weight(1f),
                        onValueChange = { newSeconds -> seconds = newSeconds }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedTotalSeconds / 60f) }) {
                Text(
                    text = stringResource(R.string.save),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
private fun TimeWheelPicker(
    value: Int,
    range: IntRange,
    label: String,
    modifier: Modifier = Modifier,
    onValueChange: (Int) -> Unit
) {
    val safeValue = value.coerceIn(range.first, range.last)
    val rangeSize = (range.last - range.first + 1).coerceAtLeast(1)
    val centerBaseIndex = remember(range) {
        val half = WHEEL_ITEM_COUNT / 2
        half - half.floorMod(rangeSize)
    }
    val initialIndex = remember(range, safeValue) {
        centerBaseIndex + (safeValue - range.first).floorMod(rangeSize)
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    LaunchedEffect(value, range) {
        val targetIndex = centerBaseIndex + (safeValue - range.first).floorMod(rangeSize)
        val currentValue = range.first + listState.firstVisibleItemIndex.floorMod(rangeSize)
        if (currentValue != safeValue && !listState.isScrollInProgress) {
            listState.scrollToItem(targetIndex)
        }
    }

    LaunchedEffect(listState, range) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2f) - viewportCenter)
            }?.index ?: listState.firstVisibleItemIndex
        }.collect { index ->
            val selectedValue = range.first + index.floorMod(rangeSize)
            if (selectedValue != value) {
                onValueChange(selectedValue)
            }
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelViewportHeight)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelCenterHighlightHeight)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.24f),
                            shape = RoundedCornerShape(14.dp)
                        )
                )

                LazyColumn(
                    state = listState,
                    flingBehavior = flingBehavior,
                    contentPadding = PaddingValues(vertical = (WheelViewportHeight - WheelItemHeight) / 2),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WheelViewportHeight)
                ) {
                    items(WHEEL_ITEM_COUNT) { index ->
                        val itemValue = range.first + index.floorMod(rangeSize)
                        val layoutInfo = listState.layoutInfo
                        val visibleItem = layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }
                        val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2f
                        val distancePx = visibleItem?.let { item ->
                            abs((item.offset + item.size / 2f) - viewportCenter)
                        } ?: Float.MAX_VALUE
                        val itemStepPx = visibleItem?.size?.toFloat()?.coerceAtLeast(1f) ?: 1f
                        val centerProgress = (1f - distancePx / itemStepPx).coerceIn(0f, 1f)
                        val rawDistance = (distancePx / itemStepPx).roundToInt().coerceAtMost(2)

                        WheelValue(
                            value = itemValue,
                            distance = rawDistance,
                            centerProgress = centerProgress
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WheelValue(
    value: Int,
    distance: Int,
    centerProgress: Float
) {
    val safeDistance = distance.coerceAtMost(2)
    val baseColor = when (safeDistance) {
        0 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
        1 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.54f)
        2 -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.30f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.22f)
    }
    val color = lerp(
        start = baseColor,
        stop = MaterialTheme.colorScheme.primary,
        fraction = centerProgress
    )
    val style = if (centerProgress > 0.58f) {
        MaterialTheme.typography.headlineMedium
    } else {
        MaterialTheme.typography.titleLarge
    }
    val scale = (0.82f + centerProgress * 0.18f).coerceIn(0.82f, 1f)
    val alpha = (0.38f + centerProgress * 0.62f).coerceIn(0.38f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(WheelItemHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            style = style,
            color = color,
            textAlign = TextAlign.Center,
            fontWeight = if (centerProgress > 0.72f) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .widthIn(min = 72.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        )
    }
}

private fun Int.floorMod(size: Int): Int {
    if (size <= 0) return 0
    return ((this % size) + size) % size
}

private fun formatTimerValue(totalSeconds: Int): String {
    val safeSeconds = totalSeconds.coerceAtLeast(0)
    val minutes = safeSeconds / 60
    val seconds = safeSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
