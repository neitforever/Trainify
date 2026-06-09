package com.example.motivationcalendarapi.ui.exercise.analysis

import android.graphics.Paint
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.motivationcalendarapi.R
import com.example.motivationcalendarapi.model.ExerciseCardType
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisMetric
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisPeriod
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisPoint
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisRecord
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisResult
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisSummary
import com.example.motivationcalendarapi.model.analysis.ExerciseProjectedProgressPoint
import com.example.motivationcalendarapi.model.analysis.ExerciseWeeklyProgression
import com.example.motivationcalendarapi.model.analysis.ExerciseAnalysisViewMode
import com.example.motivationcalendarapi.viewmodel.analysis.ExerciseAnalysisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun ExerciseAnalysisSection(
    exerciseId: String,
    cardType: ExerciseCardType,
    lang: String,
    currentLocale: Locale,
    viewModel: ExerciseAnalysisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(exerciseId, cardType) {
        viewModel.observeExercise(exerciseId, cardType)
    }

    val result = uiState.result
    val text = remember(lang, currentLocale) { ExerciseAnalysisText(lang, currentLocale) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseAnalysisHeader(text.title)

                ExerciseAnalysisPeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    text = text,
                    onPeriodSelected = viewModel::changePeriod
                )

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                    return@Column
                }

                if (result.records.isEmpty()) {
                    EmptyAnalysisCard(text.emptyState)
                    return@Column
                }

                ExerciseAnalysisSummaryGrid(
                    summary = result.summary,
                    result = result,
                    text = text
                )

                if (result.cardType == ExerciseCardType.STRENGTH) {
                    OneRepMaxInfoCard(
                        text = text,
                        isExpanded = uiState.isOneRepMaxInfoExpanded,
                        onToggle = { viewModel.toggleOneRepMaxInfoExpanded() }
                    )
                }

                WeeklyProgressionCard(
                    weekly = result.weeklyProgression,
                    cardType = result.cardType,
                    text = text,
                    isExpanded = uiState.isWeeklyProgressionExpanded,
                    onToggle = { viewModel.toggleWeeklyProgressionExpanded() }
                )

                AnalysisViewSwitcher(
                    selectedMode = uiState.selectedViewMode,
                    text = text,
                    onModeSelected = viewModel::changeViewMode
                )

                SwipeableAnalysisContent(
                    selectedMode = uiState.selectedViewMode,
                    result = result,
                    selectedMetric = uiState.selectedMetric,
                    showAllRecords = uiState.showAllRecords,
                    text = text,
                    onModeSelected = viewModel::changeViewMode,
                    onMetricSelected = viewModel::changeMetric,
                    onToggleShowAll = viewModel::toggleShowAllRecords
                )

                ExerciseProjectionCard(
                    projections = result.projectedProgression,
                    sourceDays = result.projectionSourceDays,
                    isExpanded = uiState.isProjectionExpanded,
                    text = text,
                    onToggle = viewModel::toggleProjectionExpanded
                )


            }
        }
    }
}

@Composable
private fun ExerciseAnalysisHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_progress),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(7.dp)
                    .size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ExerciseAnalysisPeriodSelector(
    selectedPeriod: ExerciseAnalysisPeriod,
    text: ExerciseAnalysisText,
    onPeriodSelected: (ExerciseAnalysisPeriod) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        ExerciseAnalysisPeriod.entries.forEach { period ->
            val selected = period == selectedPeriod
            AssistChip(
                onClick = { onPeriodSelected(period) },
                label = { Text(text.periodLabel(period)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    labelColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
private fun ExerciseMetricSelector(
    cardType: ExerciseCardType,
    selectedMetric: ExerciseAnalysisMetric,
    hasActualBodyWeight: Boolean,
    text: ExerciseAnalysisText,
    onMetricSelected: (ExerciseAnalysisMetric) -> Unit
) {
    val metrics = when (cardType) {
        ExerciseCardType.STRENGTH -> buildList {
            add(ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX)
            add(ExerciseAnalysisMetric.VOLUME)
            add(ExerciseAnalysisMetric.MAX_WEIGHT)
            if (hasActualBodyWeight) add(ExerciseAnalysisMetric.RELATIVE_STRENGTH)
        }
        ExerciseCardType.BIKE -> listOf(ExerciseAnalysisMetric.TIME, ExerciseAnalysisMetric.RESISTANCE)
        ExerciseCardType.TREADMILL -> listOf(ExerciseAnalysisMetric.TIME, ExerciseAnalysisMetric.INCLINE)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        metrics.forEach { metric ->
            val selected = metric == selectedMetric
            AssistChip(
                onClick = { onMetricSelected(metric) },
                label = { Text(text.metricLabel(metric)) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                    labelColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}


@Composable
private fun OneRepMaxInfoCard(
    text: ExerciseAnalysisText,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
            .animateContentSize(animationSpec = tween(durationMillis = 100))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(15.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text.oneRepMaxInfoTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (isExpanded) "−" else "+",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 100)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 100))
            ) {
                Text(
                    text = text.oneRepMaxInfoBody,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun AnalysisViewSwitcher(
    selectedMode: ExerciseAnalysisViewMode,
    text: ExerciseAnalysisText,
    onModeSelected: (ExerciseAnalysisViewMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(ExerciseAnalysisViewMode.CHART, ExerciseAnalysisViewMode.TABLE).forEach { mode ->
            val selected = mode == selectedMode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable { onModeSelected(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text.viewModeLabel(mode),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SwipeableAnalysisContent(
    selectedMode: ExerciseAnalysisViewMode,
    result: ExerciseAnalysisResult,
    selectedMetric: ExerciseAnalysisMetric,
    showAllRecords: Boolean,
    text: ExerciseAnalysisText,
    onModeSelected: (ExerciseAnalysisViewMode) -> Unit,
    onMetricSelected: (ExerciseAnalysisMetric) -> Unit,
    onToggleShowAll: () -> Unit
) {
    var dragAccumulator by remember(selectedMode) { mutableStateOf(0f) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(24.dp)
            )
            .pointerInput(selectedMode) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onHorizontalDrag = { _, dragAmount -> dragAccumulator += dragAmount },
                    onDragEnd = {
                        val swipeThreshold = 90f
                        when {
                            dragAccumulator < -swipeThreshold -> onModeSelected(ExerciseAnalysisViewMode.TABLE)
                            dragAccumulator > swipeThreshold -> onModeSelected(ExerciseAnalysisViewMode.CHART)
                        }
                    }
                )
            }
    ) {
        AnimatedContent(
            targetState = selectedMode,
            transitionSpec = {
                val direction = if (targetState == ExerciseAnalysisViewMode.TABLE) 1 else -1
                (slideInHorizontally(
                    animationSpec = tween(260),
                    initialOffsetX = { fullWidth -> fullWidth * direction }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(260),
                    targetOffsetX = { fullWidth -> -fullWidth * direction }
                ))
            },
            label = "ExerciseAnalysisModeAnimation"
        ) { mode ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (mode == ExerciseAnalysisViewMode.CHART) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExerciseMetricSelector(
                            cardType = result.cardType,
                            selectedMetric = selectedMetric,
                            hasActualBodyWeight = result.hasActualBodyWeight,
                            text = text,
                            onMetricSelected = onMetricSelected
                        )
                        ExerciseProgressChart(
                            points = result.chartPoints,
                            metric = selectedMetric,
                            text = text,
                            currentLocale = text.locale,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        )
                    }
                } else {
                    ExerciseRecordsTable(
                        records = result.records,
                        cardType = result.cardType,
                        showAll = showAllRecords,
                        text = text,
                        currentLocale = text.locale,
                        onToggleShowAll = onToggleShowAll
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseAnalysisSummaryGrid(
    summary: ExerciseAnalysisSummary?,
    result: ExerciseAnalysisResult,
    text: ExerciseAnalysisText
) {
    if (summary == null) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryMetricCard(text.sessions, summary.sessionsCount.toString(), Modifier.weight(1f))
            SummaryMetricCard(text.sets, summary.completedSetsCount.toString(), Modifier.weight(1f))
        }
        when (result.cardType) {
            ExerciseCardType.STRENGTH -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryMetricCard(text.maxWeight, formatNumber(summary.maxWeight) + text.weightUnit, Modifier.weight(1f))
                    SummaryMetricCard(text.estimatedOneRepMax, summary.bestEstimatedOneRepMax?.let { formatNumber(it) + text.weightUnit } ?: "—", Modifier.weight(1f))
                }
                if (result.hasActualBodyWeight && summary.bestRelativeStrengthPercent != null) {
                    SummaryMetricCard(text.relativeStrength, formatNumber(summary.bestRelativeStrengthPercent) + "%", Modifier.fillMaxWidth())
                }
            }
            ExerciseCardType.BIKE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryMetricCard(text.totalTime, formatNumber(summary.totalTime) + text.minuteUnit, Modifier.weight(1f))
                    SummaryMetricCard(text.maxResistance, formatNumber(summary.maxResistance), Modifier.weight(1f))
                }
            }
            ExerciseCardType.TREADMILL -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    SummaryMetricCard(text.totalTime, formatNumber(summary.totalTime) + text.minuteUnit, Modifier.weight(1f))
                    SummaryMetricCard(text.maxIncline, formatNumber(summary.maxIncline) + "%", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
            shape = RoundedCornerShape(18.dp)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExerciseProgressChart(
    points: List<ExerciseAnalysisPoint>,
    metric: ExerciseAnalysisMetric,
    text: ExerciseAnalysisText,
    currentLocale: Locale,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = text.chartTitle(metric),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            if (points.size < 2) {
                Box(modifier = modifier, contentAlignment = Alignment.Center) {
                    Text(
                        text = text.chartEmpty,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                AnalysisLineChart(points = points, valueSuffix = text.metricSuffix(metric), currentLocale = currentLocale, modifier = modifier)
            }
        }
    }
}

@Composable
private fun AnalysisLineChart(
    points: List<ExerciseAnalysisPoint>,
    valueSuffix: String,
    currentLocale: Locale,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val primarySoft = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dateFormatter = remember(currentLocale) { SimpleDateFormat("dd MMM", currentLocale) }

    Canvas(modifier = modifier) {
        val leftPadding = 58f
        val rightPadding = 18f
        val topPadding = 24f
        val bottomPadding = 42f
        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - topPadding - bottomPadding
        if (chartWidth <= 0f || chartHeight <= 0f) return@Canvas

        val values = points.map { it.value }
        val rawMin = values.minOrNull() ?: 0f
        val rawMax = values.maxOrNull() ?: 0f
        val range = max(1f, rawMax - rawMin)
        val minValue = max(0f, rawMin - range * 0.12f)
        val maxValue = rawMax + range * 0.12f
        val valueRange = max(1f, maxValue - minValue)
        val minTime = points.minOf { it.timestamp }
        val maxTime = points.maxOf { it.timestamp }
        val timeRange = max(1L, maxTime - minTime)

        drawRoundRect(
            color = surfaceVariant.copy(alpha = 0.55f),
            topLeft = Offset(leftPadding, topPadding),
            size = Size(chartWidth, chartHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
        )

        repeat(5) { index ->
            val fraction = index / 4f
            val y = topPadding + chartHeight - chartHeight * fraction
            drawLine(
                color = axisColor.copy(alpha = 0.16f),
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1.2f
            )
            val labelValue = minValue + valueRange * fraction
            drawContext.canvas.nativeCanvas.drawText(
                formatNumber(labelValue) + valueSuffix,
                leftPadding - 8f,
                y + 8f,
                Paint().apply {
                    color = axisColor.toArgb()
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        fun xFor(timestamp: Long): Float = leftPadding + ((timestamp - minTime).toFloat() / timeRange.toFloat()) * chartWidth
        fun yFor(value: Float): Float = topPadding + chartHeight - ((value - minValue) / valueRange) * chartHeight

        val path = Path()
        val fillPath = Path()
        points.forEachIndexed { index, point ->
            val x = xFor(point.timestamp)
            val y = yFor(point.value)
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, topPadding + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(xFor(points.last().timestamp), topPadding + chartHeight)
        fillPath.close()

        drawPath(path = fillPath, color = primarySoft)
        drawPath(path = path, color = primary, style = Stroke(width = 5f, cap = StrokeCap.Round))

        points.forEach { point ->
            val x = xFor(point.timestamp)
            val y = yFor(point.value)
            drawCircle(color = Color.White, radius = 8f, center = Offset(x, y))
            drawCircle(color = primary, radius = 6f, center = Offset(x, y))
        }

        listOf(points.first(), points[points.size / 2], points.last()).distinctBy { it.timestamp }.forEach { point ->
            val x = xFor(point.timestamp)
            drawContext.canvas.nativeCanvas.drawText(
                dateFormatter.format(Date(point.timestamp)),
                x,
                topPadding + chartHeight + 30f,
                Paint().apply {
                    color = axisColor.toArgb()
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}

@Composable
private fun ExerciseProjectionCard(
    projections: List<ExerciseProjectedProgressPoint>,
    sourceDays: Int?,
    isExpanded: Boolean,
    text: ExerciseAnalysisText,
    onToggle: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
            .animateContentSize(animationSpec = tween(durationMillis = 100))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggle() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = text.projectionTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = if (projections.isEmpty()) text.projectionNotEnoughData else text.projectionSubtitle(sourceDays),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    text = if (isExpanded) "−" else "+",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 100)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 100))
            ) {
                if (projections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = text.projectionFriendlyEmpty,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        projections.forEach { point ->
                            ProjectionRow(point = point, text = text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectionRow(point: ExerciseProjectedProgressPoint, text: ExerciseAnalysisText) {
    val rowShape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                shape = rowShape
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text.weekOffset(point.weekOffset),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = text.estimatedOneRepMax,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatNumber(point.projectedValue) + text.weightUnit,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            point.projectedRelativeStrengthPercent?.let {
                Text(
                    text = formatNumber(it) + "%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeeklyProgressionCard(
    weekly: List<ExerciseWeeklyProgression>,
    cardType: ExerciseCardType,
    text: ExerciseAnalysisText,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    if (weekly.isEmpty()) return
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                shape = RoundedCornerShape(22.dp)
            )
            .animateContentSize(animationSpec = tween(durationMillis = 100))
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onToggle() }
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = text.weeklyProgression,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    Text(
                        text = text.weeklyProgressionSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Text(
                    text = if (isExpanded) "−" else "+",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(durationMillis = 100)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 100))
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weekly.takeLast(6).forEach { week ->
                        val mainValue = when (cardType) {
                            ExerciseCardType.STRENGTH -> week.bestEstimatedOneRepMax?.let { formatNumber(it) + text.weightUnit } ?: "—"
                            ExerciseCardType.BIKE -> formatNumber(week.totalTime) + text.minuteUnit
                            ExerciseCardType.TREADMILL -> formatNumber(week.totalTime) + text.minuteUnit
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = text.weekPeriod(week.weekStartTimestamp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = text.sessionsCount(week.sessionsCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = text.weeklyMainLabel(cardType),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = mainValue,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExerciseRecordsTable(
    records: List<ExerciseAnalysisRecord>,
    cardType: ExerciseCardType,
    showAll: Boolean,
    text: ExerciseAnalysisText,
    currentLocale: Locale,
    onToggleShowAll: () -> Unit
) {
    val optimizedRecords = remember(records, cardType) {
        if (cardType == ExerciseCardType.STRENGTH) {
            records
                .groupBy { it.workoutId }
                .values
                .flatMap { workoutRecords ->
                    val maxOneRep = workoutRecords.mapNotNull { it.estimatedOneRepMax }.maxOrNull() ?: 0f
                    if (maxOneRep <= 0f) emptyList() else {
                        workoutRecords.filter { record ->
                            val value = record.estimatedOneRepMax ?: 0f
                            value >= maxOneRep * 0.8f
                        }
                    }
                }
                .sortedWith(compareByDescending<ExerciseAnalysisRecord> { it.timestamp }.thenByDescending { it.estimatedOneRepMax ?: 0f })
                .distinctBy { record ->
                    val day = record.timestamp / (24L * 60L * 60L * 1000L)
                    val oneRep = record.estimatedOneRepMax?.let { (it * 10).toInt() } ?: 0
                    "${day}_${(record.weight * 10).toInt()}_${record.reps}_$oneRep"
                }
        } else {
            records.sortedByDescending { it.timestamp }
        }
    }
    val visibleRecords = if (showAll) optimizedRecords else optimizedRecords.take(10)
    val dateFormatter = remember(currentLocale) { SimpleDateFormat("dd.MM", currentLocale) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                RecordRow(
                    cells = if (cardType == ExerciseCardType.STRENGTH) {
                        listOf(text.date, text.result, text.estimatedOneRepMaxShort)
                    } else {
                        listOf(text.date, text.time, text.resistanceOrIncline(cardType))
                    },
                    isHeader = true
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                visibleRecords.forEach { record ->
                    val cells = if (cardType == ExerciseCardType.STRENGTH) {
                        listOf(
                            dateFormatter.format(Date(record.timestamp)),
                            "${formatNumber(record.weight)}${text.weightUnit} × ${record.reps}",
                            record.estimatedOneRepMax?.let { formatNumber(it) + text.weightUnit } ?: "—"
                        )
                    } else {
                        listOf(
                            dateFormatter.format(Date(record.timestamp)),
                            formatNumber(record.time) + text.minuteUnit,
                            if (cardType == ExerciseCardType.BIKE) formatNumber(record.resistance) else formatNumber(record.incline) + "%"
                        )
                    }
                    RecordRow(cells = cells, isHeader = false)
                }
            }
            if (optimizedRecords.size > 10) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (showAll) text.showLess else text.showAll,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleShowAll() }
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordRow(cells: List<String>, isHeader: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        cells.forEachIndexed { index, cell ->
            val weight = when (index) {
                0 -> 0.75f
                1 -> 1.25f
                else -> 0.95f
            }
            Text(
                text = cell,
                style = if (isHeader) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
                color = if (isHeader) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(weight),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun EmptyAnalysisCard(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private class ExerciseAnalysisText(private val lang: String, val locale: Locale) {
    val title = when (lang) { "ru" -> "Анализ результатов"; "be" -> "Аналіз вынікаў"; else -> "Results analysis" }
    val sessions = when (lang) { "ru" -> "Тренировок"; "be" -> "Трэніровак"; else -> "Sessions" }
    val sets = when (lang) { "ru" -> "Подходов"; "be" -> "Падыходаў"; else -> "Sets" }
    val totalVolume = when (lang) { "ru" -> "Общий объём"; "be" -> "Агульны аб'ём"; else -> "Total volume" }
    val maxWeight = when (lang) { "ru" -> "Макс. вес"; "be" -> "Макс. вага"; else -> "Max weight" }
    val estimatedOneRepMax = when (lang) { "ru" -> "1ПМ"; "be" -> "1ПМ"; else -> "1RM" }
    val estimatedOneRepMaxShort = when (lang) { "ru" -> "1ПМ"; "be" -> "1ПМ"; else -> "1RM" }
    val relativeStrength = when (lang) { "ru" -> "Относительная сила"; "be" -> "Адносная сіла"; else -> "Relative strength" }
    val totalTime = when (lang) { "ru" -> "Общее время"; "be" -> "Агульны час"; else -> "Total time" }
    val maxResistance = when (lang) { "ru" -> "Макс. сопротивление"; "be" -> "Макс. супраціў"; else -> "Max resistance" }
    val maxIncline = when (lang) { "ru" -> "Макс. наклон"; "be" -> "Макс. нахіл"; else -> "Max incline" }
    val projectionTitle = when (lang) { "ru" -> "Расчётная прогрессия"; "be" -> "Разліковая прагрэсія"; else -> "Projected progression" }
    val weeklyProgression = when (lang) { "ru" -> "Прогрессия по неделям"; "be" -> "Прагрэсія па тыднях"; else -> "Weekly progression" }
    val weeklyProgressionSubtitle = when (lang) { "ru" -> "Лучшие значения по каждой неделе"; "be" -> "Лепшыя значэнні па кожным тыдні"; else -> "Best values for each week" }
    val records = when (lang) { "ru" -> "Значимые подходы"; "be" -> "Значныя падыходы"; else -> "Key sets" }
    val date = when (lang) { "ru" -> "Дата"; "be" -> "Дата"; else -> "Date" }
    val set = when (lang) { "ru" -> "Подх."; "be" -> "Падх."; else -> "Set" }
    val result = when (lang) { "ru" -> "Результат"; "be" -> "Вынік"; else -> "Result" }
    val weight = when (lang) { "ru" -> "Вес"; "be" -> "Вага"; else -> "Weight" }
    val reps = when (lang) { "ru" -> "Повт."; "be" -> "Паўт."; else -> "Reps" }
    val volume = when (lang) { "ru" -> "Объём"; "be" -> "Аб'ём"; else -> "Volume" }
    val time = when (lang) { "ru" -> "Время"; "be" -> "Час"; else -> "Time" }
    val showAll = when (lang) { "ru" -> "Показать все"; "be" -> "Паказаць усе"; else -> "Show all" }
    val showLess = when (lang) { "ru" -> "Свернуть"; "be" -> "Згарнуць"; else -> "Show less" }
    val emptyState = when (lang) { "ru" -> "Пока нет сохранённых результатов по этому упражнению за выбранный период."; "be" -> "Пакуль няма захаваных вынікаў па гэтым практыкаванні за выбраны перыяд."; else -> "No saved results for this exercise in the selected period yet." }
    val chartEmpty = when (lang) { "ru" -> "Для графика нужно минимум две тренировки."; "be" -> "Для графіка патрэбны мінімум дзве трэніроўкі."; else -> "At least two sessions are needed for the chart." }
    val projectionNotEnoughData = when (lang) { "ru" -> "Появится, когда по истории будет достаточно данных."; "be" -> "З'явіцца, калі ў гісторыі будзе дастаткова даных."; else -> "Appears when there is enough history." }
    val projectionFriendlyEmpty = when (lang) { "ru" -> "Прогноз скрыт, потому что сейчас данных недостаточно или динамика не является положительной. Это защищает экран от необоснованных расчётов."; "be" -> "Прагноз схаваны, бо цяпер недастаткова даных або дынаміка не з'яўляецца станоўчай."; else -> "The forecast is hidden because there is not enough data or the trend is not positive. This avoids unsupported estimates." }
    val oneRepMaxInfoTitle = when (lang) { "ru" -> "Что такое 1ПМ"; "be" -> "Што такое 1ПМ"; else -> "What 1RM means" }
    val oneRepMaxInfoBody = when (lang) {
        "ru" -> "1ПМ — это ориентировочный максимум на одно повторение. В приложении он рассчитывается по весу и количеству повторений, поэтому используется только для сравнения динамики, а не как реальный проверенный максимум."
        "be" -> "1ПМ — гэта арыенціровачны максімум на адно паўтарэнне. У дадатку ён разлічваецца па вазе і колькасці паўтарэнняў."
        else -> "1RM is an estimated one-repetition maximum. The app calculates it from weight and reps, so it is used for trend comparison, not as a tested maximum."
    }
    val chart = when (lang) { "ru" -> "График"; "be" -> "Графік"; else -> "Chart" }
    val table = when (lang) { "ru" -> "Таблица"; "be" -> "Табліца"; else -> "Table" }

    val weightUnit = when (lang) { "ru" -> " кг"; "be" -> " кг"; else -> " kg" }
    val minuteUnit = when (lang) { "ru" -> " мин"; "be" -> " хв"; else -> " min" }

    fun periodLabel(period: ExerciseAnalysisPeriod): String = when (period) {
        ExerciseAnalysisPeriod.LAST_7_DAYS -> when (lang) { "ru" -> "7 дней"; "be" -> "7 дзён"; else -> "7 days" }
        ExerciseAnalysisPeriod.LAST_30_DAYS -> when (lang) { "ru" -> "30 дней"; "be" -> "30 дзён"; else -> "30 days" }
        ExerciseAnalysisPeriod.LAST_90_DAYS -> when (lang) { "ru" -> "90 дней"; "be" -> "90 дзён"; else -> "90 days" }
    }

    fun viewModeLabel(mode: ExerciseAnalysisViewMode): String = when (mode) {
        ExerciseAnalysisViewMode.CHART -> chart
        ExerciseAnalysisViewMode.TABLE -> table
    }

    fun metricLabel(metric: ExerciseAnalysisMetric): String = when (metric) {
        ExerciseAnalysisMetric.VOLUME -> totalVolume
        ExerciseAnalysisMetric.MAX_WEIGHT -> weight
        ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX -> estimatedOneRepMax
        ExerciseAnalysisMetric.RELATIVE_STRENGTH -> relativeStrength
        ExerciseAnalysisMetric.TIME -> time
        ExerciseAnalysisMetric.RESISTANCE -> maxResistance
        ExerciseAnalysisMetric.INCLINE -> maxIncline
    }

    fun chartTitle(metric: ExerciseAnalysisMetric): String = when (lang) {
        "ru" -> "Динамика: ${metricLabel(metric)}"
        "be" -> "Дынаміка: ${metricLabel(metric)}"
        else -> "Trend: ${metricLabel(metric)}"
    }

    fun metricSuffix(metric: ExerciseAnalysisMetric): String = when (metric) {
        ExerciseAnalysisMetric.RELATIVE_STRENGTH -> "%"
        ExerciseAnalysisMetric.INCLINE -> "%"
        ExerciseAnalysisMetric.TIME -> minuteUnit
        ExerciseAnalysisMetric.VOLUME,
        ExerciseAnalysisMetric.MAX_WEIGHT,
        ExerciseAnalysisMetric.ESTIMATED_ONE_REP_MAX -> weightUnit
        else -> ""
    }

    fun projectionSubtitle(sourceDays: Int?): String = when (lang) {
        "ru" -> "Оценка на 4 недели"
        "be" -> "Ацэнка на 4 тыдні"
        else -> "4-week estimate"
    }

    fun weekOffset(offset: Int): String = when (lang) {
        "ru" -> "Через $offset нед."
        "be" -> "Праз $offset тыд."
        else -> "In $offset week${if (offset == 1) "" else "s"}"
    }

    fun weekPeriod(weekStartTimestamp: Long): String {
        val formatter = SimpleDateFormat("dd.MM", locale)
        val start = Date(weekStartTimestamp)
        val end = Date(weekStartTimestamp + 6L * 24L * 60L * 60L * 1000L)
        return "${formatter.format(start)}–${formatter.format(end)}"
    }

    fun weeklyMainLabel(cardType: ExerciseCardType): String = when (cardType) {
        ExerciseCardType.STRENGTH -> estimatedOneRepMax
        ExerciseCardType.BIKE, ExerciseCardType.TREADMILL -> time
    }

    fun sessionsCount(count: Int): String = when (lang) {
        "ru" -> "Тренировок: $count"
        "be" -> "Трэніровак: $count"
        else -> "Sessions: $count"
    }

    fun resistanceOrIncline(cardType: ExerciseCardType): String = when (cardType) {
        ExerciseCardType.BIKE -> maxResistance
        ExerciseCardType.TREADMILL -> maxIncline
        ExerciseCardType.STRENGTH -> weight
    }
}

private fun formatNumber(value: Float): String {
    return if (value % 1f == 0f) {
        value.toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
}
