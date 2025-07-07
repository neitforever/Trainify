package com.example.motivationcalendarapi.ui.body_progress.fragments

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.example.motivationcalendarapi.model.BodyProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightHistoryChart(
    progressList: List<BodyProgress>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLocale = context.resources.configuration.locales[0] ?: Locale.getDefault()
    val dateFormatter = remember(currentLocale) {
        SimpleDateFormat("dd MMM", currentLocale)
    }

    val sortedList = progressList.sortedBy { it.timestamp }
    if (sortedList.isEmpty()) return

    val textColor = colorScheme.onSurfaceVariant
    val lineColor = colorScheme.primary.copy(alpha = 0.8f)
    val pointColor = colorScheme.primary
    val rectColor = colorScheme.surfaceVariant

    Canvas(modifier = modifier) {
        val padding = 40f
        val axisWidth = 2f
        val chartHeight = size.height - padding * 2
        val chartWidth = size.width - padding * 2

        val weights = sortedList.map { it.weight }
        val minWeight = weights.min() - 2.0
        val maxWeight = weights.max() + 2.0
        val dates = sortedList.map { it.timestamp.toFloat() }
        val minDate = dates.min()
        val maxDate = dates.max()

        drawRect(
            color = rectColor,
            size = Size(chartWidth, chartHeight),
            topLeft = Offset(padding, padding)
        )

        drawLine(
            color = textColor,
            start = Offset(padding, padding),
            end = Offset(padding, padding + chartHeight),
            strokeWidth = axisWidth
        )

        drawLine(
            color = textColor,
            start = Offset(padding, padding + chartHeight),
            end = Offset(padding + chartWidth, padding + chartHeight),
            strokeWidth = axisWidth
        )

        val yStep = (maxWeight - minWeight) / 5
        repeat(6) { i ->
            val yValue = minWeight + yStep * i
            val yPos = padding + chartHeight - (yValue - minWeight).toFloat() / (maxWeight - minWeight).toFloat() * chartHeight

            drawLine(
                color = textColor.copy(alpha = 0.2f),
                start = Offset(padding, yPos),
                end = Offset(padding + chartWidth, yPos),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "%.1f".format(yValue),
                padding - 35f,
                yPos + 5f,
                Paint().apply {
                    color = textColor.toArgb()
                    textSize = 24f
                    textAlign = Paint.Align.RIGHT
                }
            )
        }

        val pointRadius = 4f
        var lastX = 0f
        var lastY = 0f

        sortedList.forEachIndexed { index, progress ->
            val x = padding + (progress.timestamp - minDate) / (maxDate - minDate) * chartWidth
            val y = padding + chartHeight - (progress.weight - minWeight).toFloat() / (maxWeight - minWeight).toFloat() * chartHeight

            if (index > 0) {
                drawLine(
                    color = lineColor,
                    start = Offset(lastX, lastY),
                    end = Offset(x, y),
                    strokeWidth = 3f
                )
            }

            drawCircle(
                color = pointColor,
                radius = pointRadius * 2,
                center = Offset(x, y),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = pointColor,
                radius = pointRadius,
                center = Offset(x, y)
            )

            lastX = x
            lastY = y
        }

        val firstDate = sortedList.first().timestamp
        val lastDate = sortedList.last().timestamp
        val middleDate = (firstDate + lastDate) / 2

        listOf(firstDate, middleDate, lastDate).forEach { date ->
            val x = padding + (date - minDate) / (maxDate - minDate) * chartWidth
            drawContext.canvas.nativeCanvas.drawText(
                dateFormatter.format(Date(date.toLong())),
                x,
                padding + chartHeight + 30f,
                Paint().apply {
                    color = textColor.toArgb()
                    textSize = 24f
                    textAlign = Paint.Align.CENTER
                }
            )
        }
    }
}