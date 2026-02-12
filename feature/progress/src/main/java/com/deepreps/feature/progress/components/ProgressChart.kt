package com.deepreps.feature.progress.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.progress.ChartDataPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Custom Canvas-based line chart for displaying weight progression over time.
 *
 * Design spec: design-system.md Section 3.5
 * - Container: full width card, radius-md, surface-low background
 * - Chart area height: 200dp
 * - Y-axis label width: 48dp
 * - X-axis label height: 24dp
 * - Data point visual dot: 8dp, touch target: 32dp
 * - Line stroke: 2dp, accent-primary
 * - Grid lines: 1dp, border-subtle, dashed
 * - PR data point: gold dot (#FFD43B)
 *
 * Handles edge cases:
 * - 0 data points: shows empty state
 * - 1 data point: single centered dot
 * - Many data points: connected line chart
 *
 * @param dataPoints Sorted (ascending by date) chart data points.
 * @param title Optional chart title.
 * @param currentValue Current best value text.
 * @param peakValue All-time peak value text.
 * @param deltaText Change text (e.g., "+5kg").
 * @param modifier External modifier.
 * @param lineColor Color for the chart line. Defaults to accent-primary.
 */
@Suppress("LongMethod")
@Composable
fun ProgressChart(
    dataPoints: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    title: String? = null,
    currentValue: String? = null,
    peakValue: String? = null,
    deltaText: String? = null,
    lineColor: Color = DeepRepsTheme.colors.accentPrimary,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space4),
        ) {
            // Title row
            if (title != null) {
                Text(
                    text = title,
                    style = typography.headlineSmall,
                    color = colors.onSurfacePrimary,
                )
                Spacer(modifier = Modifier.height(spacing.space3))
            }

            when {
                dataPoints.isEmpty() -> {
                    EmptyState(
                        title = "No data yet",
                        message = "Complete a workout to see progress",
                        modifier = Modifier.height(200.dp),
                    )
                }

                else -> {
                    ChartCanvas(
                        dataPoints = dataPoints,
                        lineColor = lineColor,
                        gridColor = colors.borderSubtle,
                        dotColor = lineColor,
                        prDotColor = PrGoldColor,
                        labelColor = colors.onSurfaceTertiary,
                        labelStyle = typography.labelSmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }
            }

            // Summary row
            if (currentValue != null || peakValue != null || deltaText != null) {
                Spacer(modifier = Modifier.height(spacing.space3))
                HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)
                Spacer(modifier = Modifier.height(spacing.space2))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (currentValue != null) {
                        SummaryItem(label = "Current", value = currentValue)
                    }
                    if (peakValue != null) {
                        SummaryItem(label = "Peak", value = peakValue)
                    }
                    if (deltaText != null) {
                        SummaryItem(label = "Change", value = deltaText)
                    }
                }
            }
        }
    }
}

/**
 * Canvas rendering of the line chart.
 *
 * Handles:
 * - Single data point: centered dot, no line
 * - Multiple data points: connected line with dots at each point
 * - Y-axis labels on left (48dp width)
 * - X-axis date labels at bottom (24dp height)
 * - Dashed grid lines
 */
@Suppress("LongMethod")
@Composable
private fun ChartCanvas(
    dataPoints: List<ChartDataPoint>,
    lineColor: Color,
    gridColor: Color,
    dotColor: Color,
    prDotColor: Color,
    labelColor: Color,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    val minWeight = remember(dataPoints) {
        dataPoints.minOfOrNull { it.weightKg } ?: 0.0
    }
    val maxWeight = remember(dataPoints) {
        dataPoints.maxOfOrNull { it.weightKg } ?: 0.0
    }
    val minDate = remember(dataPoints) {
        dataPoints.minOfOrNull { it.dateEpochMs } ?: 0L
    }
    val maxDate = remember(dataPoints) {
        dataPoints.maxOfOrNull { it.dateEpochMs } ?: 0L
    }

    Canvas(modifier = modifier) {
        val yAxisWidth = 48.dp.toPx()
        val xAxisHeight = 24.dp.toPx()
        val chartLeft = yAxisWidth
        val chartRight = size.width - 8.dp.toPx()
        val chartTop = 8.dp.toPx()
        val chartBottom = size.height - xAxisHeight
        val chartWidth = chartRight - chartLeft
        val chartHeight = chartBottom - chartTop

        // Draw Y-axis grid lines (4 lines)
        val yGridCount = 4
        val weightRange = if (maxWeight > minWeight) maxWeight - minWeight else 10.0
        val paddedMin = minWeight - (weightRange * 0.1)
        val paddedMax = maxWeight + (weightRange * 0.1)
        val paddedRange = paddedMax - paddedMin

        val dashEffect = PathEffect.dashPathEffect(
            floatArrayOf(8.dp.toPx(), 4.dp.toPx()),
            0f,
        )

        for (i in 0..yGridCount) {
            val fraction = i.toFloat() / yGridCount
            val y = chartBottom - (fraction * chartHeight)
            val weightValue = paddedMin + (fraction * paddedRange)

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(chartLeft, y),
                end = Offset(chartRight, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = dashEffect,
            )

            // Y-axis label
            val labelText = formatWeight(weightValue)
            drawAxisLabel(
                textMeasurer = textMeasurer,
                text = labelText,
                style = labelStyle,
                color = labelColor,
                x = 4.dp.toPx(),
                y = y - 8.dp.toPx(),
            )
        }

        if (dataPoints.size == 1) {
            // Single dot centered in the chart
            val centerX = chartLeft + chartWidth / 2
            val centerY = chartTop + chartHeight / 2
            val color = if (dataPoints[0].isPersonalRecord) prDotColor else dotColor
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(centerX, centerY),
            )
            return@Canvas
        }

        // Map data points to canvas coordinates
        val dateRange = if (maxDate > minDate) (maxDate - minDate).toFloat() else 1f

        val points = dataPoints.map { dp ->
            val xFraction = (dp.dateEpochMs - minDate).toFloat() / dateRange
            val yFraction = ((dp.weightKg - paddedMin) / paddedRange).toFloat()
            Offset(
                x = chartLeft + xFraction * chartWidth,
                y = chartBottom - yFraction * chartHeight,
            ) to dp
        }

        // Draw connecting lines
        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i].first,
                end = points[i + 1].first,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Draw dots
        points.forEach { (offset, dp) ->
            val color = if (dp.isPersonalRecord) prDotColor else dotColor
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = offset,
            )
        }

        // Draw X-axis labels (first and last date, plus middle if space permits)
        if (dataPoints.size >= 2) {
            val firstLabel = formatDateShort(dataPoints.first().dateEpochMs)
            val lastLabel = formatDateShort(dataPoints.last().dateEpochMs)

            drawAxisLabel(
                textMeasurer = textMeasurer,
                text = firstLabel,
                style = labelStyle,
                color = labelColor,
                x = chartLeft,
                y = chartBottom + 4.dp.toPx(),
            )

            drawAxisLabel(
                textMeasurer = textMeasurer,
                text = lastLabel,
                style = labelStyle,
                color = labelColor,
                x = chartRight - 40.dp.toPx(),
                y = chartBottom + 4.dp.toPx(),
            )
        }
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawAxisLabel(
    textMeasurer: TextMeasurer,
    text: String,
    style: TextStyle,
    color: Color,
    x: Float,
    y: Float,
) {
    val result = textMeasurer.measure(text, style.copy(color = color))
    drawText(
        textLayoutResult = result,
        topLeft = Offset(x, y),
    )
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
) {
    val typography = DeepRepsTheme.typography
    val colors = DeepRepsTheme.colors

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceTertiary,
        )
        Text(
            text = value,
            style = typography.labelLarge,
            color = colors.onSurfacePrimary,
        )
    }
}

// ---------------------------------------------------------------------------
// Formatting helpers
// ---------------------------------------------------------------------------

private val shortDateFormat = SimpleDateFormat("MMM d", Locale.US)

private fun formatDateShort(epochMs: Long): String {
    return shortDateFormat.format(Date(epochMs))
}

private fun formatWeight(kg: Double): String {
    return if (kg == kg.toLong().toDouble()) {
        "${kg.toLong()}"
    } else {
        String.format(Locale.US, "%.1f", kg)
    }
}

private val PrGoldColor = Color(0xFFFFD43B)

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Chart with data - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChartWithDataDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        ProgressChart(
            dataPoints = listOf(
                ChartDataPoint(now - 8 * oneWeekMs, 60.0),
                ChartDataPoint(now - 6 * oneWeekMs, 65.0),
                ChartDataPoint(now - 4 * oneWeekMs, 67.5),
                ChartDataPoint(now - 3 * oneWeekMs, 70.0, isPersonalRecord = true),
                ChartDataPoint(now - 2 * oneWeekMs, 67.5),
                ChartDataPoint(now - oneWeekMs, 72.5, isPersonalRecord = true),
            ),
            title = "Bench Press",
            currentValue = "72.5kg",
            peakValue = "72.5kg",
            deltaText = "+12.5kg",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Chart single point - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChartSinglePointDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ProgressChart(
            dataPoints = listOf(
                ChartDataPoint(System.currentTimeMillis(), 80.0),
            ),
            title = "Squat",
            currentValue = "80kg",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Chart empty - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChartEmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ProgressChart(
            dataPoints = emptyList(),
            title = "Deadlift",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Chart with data - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChartWithDataLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        ProgressChart(
            dataPoints = listOf(
                ChartDataPoint(now - 6 * oneWeekMs, 40.0),
                ChartDataPoint(now - 4 * oneWeekMs, 42.5),
                ChartDataPoint(now - 2 * oneWeekMs, 45.0),
                ChartDataPoint(now - oneWeekMs, 47.5, isPersonalRecord = true),
            ),
            title = "Overhead Press",
            currentValue = "47.5kg",
            peakValue = "47.5kg",
            deltaText = "+7.5kg",
            modifier = Modifier.padding(16.dp),
        )
    }
}
