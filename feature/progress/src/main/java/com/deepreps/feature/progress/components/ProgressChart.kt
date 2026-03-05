@file:Suppress("TooManyFunctions")

package com.deepreps.feature.progress.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.util.Estimated1rmCalculator
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.feature.progress.ChartDataPoint
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
 * @param secondaryLineColor When non-null, enables dual-line mode for 1RM overlay.
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
    secondaryLineColor: Color? = null,
    currentLabel: String = "Current",
    peakLabel: String = "Peak",
    changeLabel: String = "Change",
    weightUnit: WeightUnit = WeightUnit.KG,
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
                        secondaryLineColor = secondaryLineColor,
                        gridColor = colors.borderSubtle,
                        dotColor = lineColor,
                        prDotColor = PrGoldColor,
                        labelColor = colors.onSurfaceTertiary,
                        labelStyle = typography.labelSmall,
                        weightUnit = weightUnit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )

                    if (secondaryLineColor != null) {
                        Spacer(modifier = Modifier.height(spacing.space2))
                        ChartLegend(
                            primaryColor = lineColor,
                            secondaryColor = secondaryLineColor,
                        )
                    }
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
                        SummaryItem(label = currentLabel, value = currentValue)
                    }
                    if (peakValue != null) {
                        SummaryItem(label = peakLabel, value = peakValue)
                    }
                    if (deltaText != null) {
                        SummaryItem(label = changeLabel, value = deltaText)
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
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun ChartCanvas(
    dataPoints: List<ChartDataPoint>,
    lineColor: Color,
    secondaryLineColor: Color?,
    gridColor: Color,
    dotColor: Color,
    prDotColor: Color,
    labelColor: Color,
    labelStyle: TextStyle,
    weightUnit: WeightUnit = WeightUnit.KG,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    // Compute Y-axis range across both weight AND estimated 1RM values
    val allValues = remember(dataPoints) {
        dataPoints.flatMap { dp ->
            listOfNotNull(dp.weightKg, dp.estimated1rmKg)
        }
    }
    val minWeight = remember(allValues) { allValues.minOrNull() ?: 0.0 }
    val maxWeight = remember(allValues) { allValues.maxOrNull() ?: 0.0 }
    val minDate = remember(dataPoints) { dataPoints.minOfOrNull { it.dateEpochMs } ?: 0L }
    val maxDate = remember(dataPoints) { dataPoints.maxOfOrNull { it.dateEpochMs } ?: 0L }

    Canvas(modifier = modifier) {
        val chartLayout = computeChartLayout(size)
        val weightRange = if (maxWeight > minWeight) maxWeight - minWeight else 10.0
        val paddedMin = minWeight - (weightRange * 0.1)
        val paddedMax = maxWeight + (weightRange * 0.1)
        val paddedRange = paddedMax - paddedMin

        drawGridAndLabels(
            chartLayout,
            paddedMin,
            paddedRange,
            gridColor,
            labelColor,
            labelStyle,
            textMeasurer,
            weightUnit,
        )

        if (dataPoints.size == 1) {
            drawSinglePoint(
                dataPoints[0],
                chartLayout,
                paddedMin,
                paddedRange,
                dotColor,
                prDotColor,
                secondaryLineColor,
            )
            return@Canvas
        }

        val dateRange = if (maxDate > minDate) (maxDate - minDate).toFloat() else 1f

        // Map data points to primary (weight) canvas coordinates
        val points = dataPoints.map { dp ->
            val offset =
                toCanvasOffset(dp.weightKg, dp.dateEpochMs, chartLayout, paddedMin, paddedRange, minDate, dateRange)
            offset to dp
        }

        // Draw primary line (max weight)
        drawConnectingLines(points.map { it.first }, lineColor)

        // Draw secondary line (1RM) if enabled
        if (secondaryLineColor != null) {
            drawSecondaryLine(dataPoints, chartLayout, paddedMin, paddedRange, minDate, dateRange, secondaryLineColor)
        }

        // Draw primary dots
        points.forEach { (offset, dp) ->
            val color = if (dp.isPersonalRecord) prDotColor else dotColor
            drawCircle(color = color, radius = 4.dp.toPx(), center = offset)
        }

        // Draw secondary dots on top
        if (secondaryLineColor != null) {
            drawSecondaryDots(dataPoints, chartLayout, paddedMin, paddedRange, minDate, dateRange, secondaryLineColor)
        }

        drawXAxisLabels(dataPoints, chartLayout, labelColor, labelStyle, textMeasurer)
    }
}

private data class ChartLayout(
    val chartLeft: Float,
    val chartRight: Float,
    val chartTop: Float,
    val chartBottom: Float,
    val chartWidth: Float,
    val chartHeight: Float,
)

private fun DrawScope.computeChartLayout(
    @Suppress("UnusedParameter") size: androidx.compose.ui.geometry.Size = this.size,
): ChartLayout {
    val yAxisWidth = 48.dp.toPx()
    val xAxisHeight = 24.dp.toPx()
    val chartLeft = yAxisWidth
    val chartRight = this.size.width - 8.dp.toPx()
    val chartTop = 8.dp.toPx()
    val chartBottom = this.size.height - xAxisHeight
    return ChartLayout(
        chartLeft = chartLeft,
        chartRight = chartRight,
        chartTop = chartTop,
        chartBottom = chartBottom,
        chartWidth = chartRight - chartLeft,
        chartHeight = chartBottom - chartTop,
    )
}

@Suppress("LongParameterList")
private fun DrawScope.drawGridAndLabels(
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    gridColor: Color,
    labelColor: Color,
    labelStyle: TextStyle,
    textMeasurer: TextMeasurer,
    weightUnit: WeightUnit = WeightUnit.KG,
) {
    val yGridCount = 4
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 4.dp.toPx()), 0f)

    for (i in 0..yGridCount) {
        val fraction = i.toFloat() / yGridCount
        val y = layout.chartBottom - (fraction * layout.chartHeight)
        val weightValueKg = paddedMin + (fraction * paddedRange)
        val displayValue = when (weightUnit) {
            WeightUnit.KG -> weightValueKg
            WeightUnit.LBS -> weightValueKg * KG_TO_LBS
        }

        drawLine(
            color = gridColor,
            start = Offset(layout.chartLeft, y),
            end = Offset(layout.chartRight, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = dashEffect,
        )

        drawAxisLabel(
            textMeasurer = textMeasurer,
            text = formatWeight(displayValue),
            style = labelStyle,
            color = labelColor,
            x = 4.dp.toPx(),
            y = y - 8.dp.toPx(),
        )
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawSinglePoint(
    dp: ChartDataPoint,
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    dotColor: Color,
    prDotColor: Color,
    secondaryLineColor: Color?,
) {
    val centerX = layout.chartLeft + layout.chartWidth / 2

    // Position primary dot using actual weight value on Y-axis
    val primaryYFraction = ((dp.weightKg - paddedMin) / paddedRange).toFloat()
    val primaryY = layout.chartBottom - primaryYFraction * layout.chartHeight
    val color = if (dp.isPersonalRecord) prDotColor else dotColor
    drawCircle(color = color, radius = 4.dp.toPx(), center = Offset(centerX, primaryY))

    // Draw 1RM dot at its actual Y position
    if (secondaryLineColor != null && dp.estimated1rmKg != null) {
        val secondaryYFraction = ((dp.estimated1rmKg - paddedMin) / paddedRange).toFloat()
        val secondaryY = layout.chartBottom - secondaryYFraction * layout.chartHeight
        draw1rmDot(Offset(centerX, secondaryY), dp.confidence, secondaryLineColor)
    }
}

@Suppress("LongParameterList")
private fun toCanvasOffset(
    valueKg: Double,
    dateEpochMs: Long,
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    minDate: Long,
    dateRange: Float,
): Offset {
    val xFraction = (dateEpochMs - minDate).toFloat() / dateRange
    val yFraction = ((valueKg - paddedMin) / paddedRange).toFloat()
    return Offset(
        x = layout.chartLeft + xFraction * layout.chartWidth,
        y = layout.chartBottom - yFraction * layout.chartHeight,
    )
}

private fun DrawScope.drawConnectingLines(points: List<Offset>, color: Color) {
    for (i in 0 until points.size - 1) {
        drawLine(
            color = color,
            start = points[i],
            end = points[i + 1],
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawSecondaryLine(
    dataPoints: List<ChartDataPoint>,
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    minDate: Long,
    dateRange: Float,
    color: Color,
) {
    // Build segments: sequences of consecutive non-null 1RM values
    var segmentStart: Int? = null
    for (i in dataPoints.indices) {
        if (dataPoints[i].estimated1rmKg != null) {
            if (segmentStart == null) segmentStart = i
        } else {
            if (segmentStart != null) {
                drawSegmentLines(
                    dataPoints, segmentStart, i - 1,
                    layout, paddedMin, paddedRange, minDate, dateRange, color,
                )
                segmentStart = null
            }
        }
    }
    if (segmentStart != null) {
        drawSegmentLines(
            dataPoints, segmentStart, dataPoints.size - 1,
            layout, paddedMin, paddedRange, minDate, dateRange, color,
        )
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawSegmentLines(
    dataPoints: List<ChartDataPoint>,
    start: Int,
    end: Int,
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    minDate: Long,
    dateRange: Float,
    color: Color,
) {
    for (i in start until end) {
        val from = toCanvasOffset(
            dataPoints[i].estimated1rmKg!!,
            dataPoints[i].dateEpochMs,
            layout,
            paddedMin,
            paddedRange,
            minDate,
            dateRange,
        )
        val to = toCanvasOffset(
            dataPoints[i + 1].estimated1rmKg!!,
            dataPoints[i + 1].dateEpochMs,
            layout,
            paddedMin,
            paddedRange,
            minDate,
            dateRange,
        )
        drawLine(color = color, start = from, end = to, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawSecondaryDots(
    dataPoints: List<ChartDataPoint>,
    layout: ChartLayout,
    paddedMin: Double,
    paddedRange: Double,
    minDate: Long,
    dateRange: Float,
    color: Color,
) {
    dataPoints.forEach { dp ->
        val estimated = dp.estimated1rmKg ?: return@forEach
        val offset = toCanvasOffset(estimated, dp.dateEpochMs, layout, paddedMin, paddedRange, minDate, dateRange)
        draw1rmDot(offset, dp.confidence, color)
    }
}

private fun DrawScope.draw1rmDot(
    center: Offset,
    confidence: Estimated1rmCalculator.Confidence?,
    color: Color,
) {
    if (confidence == Estimated1rmCalculator.Confidence.LOW) {
        // Hollow circle for low confidence
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = center,
            style = Stroke(width = 1.5.dp.toPx()),
        )
    } else {
        // Solid filled circle for high/moderate confidence
        drawCircle(color = color, radius = 4.dp.toPx(), center = center)
    }
}

private fun DrawScope.drawXAxisLabels(
    dataPoints: List<ChartDataPoint>,
    layout: ChartLayout,
    labelColor: Color,
    labelStyle: TextStyle,
    textMeasurer: TextMeasurer,
) {
    if (dataPoints.size >= 2) {
        drawAxisLabel(
            textMeasurer = textMeasurer,
            text = formatDateShort(dataPoints.first().dateEpochMs),
            style = labelStyle,
            color = labelColor,
            x = layout.chartLeft,
            y = layout.chartBottom + 4.dp.toPx(),
        )
        drawAxisLabel(
            textMeasurer = textMeasurer,
            text = formatDateShort(dataPoints.last().dateEpochMs),
            style = labelStyle,
            color = labelColor,
            x = layout.chartRight - 40.dp.toPx(),
            y = layout.chartBottom + 4.dp.toPx(),
        )
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
private fun ChartLegend(
    primaryColor: Color,
    secondaryColor: Color,
) {
    val typography = DeepRepsTheme.typography
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(color = primaryColor, label = "Max Weight")
        Spacer(modifier = Modifier.width(spacing.space4))
        LegendItem(color = secondaryColor, label = "Est. 1RM")
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
) {
    val typography = DeepRepsTheme.typography
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(spacing.space1))
        Text(
            text = label,
            style = typography.labelSmall,
            color = colors.onSurfaceSecondary,
        )
    }
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

private val shortDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

private fun formatDateShort(epochMs: Long): String {
    return Instant.ofEpochMilli(epochMs)
        .atZone(ZoneId.systemDefault())
        .format(shortDateFormatter)
}

private fun formatWeight(kg: Double): String {
    return if (kg == kg.toLong().toDouble()) {
        "${kg.toLong()}"
    } else {
        String.format(Locale.US, "%.1f", kg)
    }
}

private const val KG_TO_LBS = 2.20462

private val PrGoldColor = Color(0xFFFFD43B)

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Suppress("LongMethod")
@Preview(name = "Chart with data - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChartWithDataDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        ProgressChart(
            dataPoints = listOf(
                ChartDataPoint(
                    now - 8 * oneWeekMs,
                    60.0,
                    estimated1rmKg = 72.0,
                    confidence = Estimated1rmCalculator.Confidence.HIGH
                ),
                ChartDataPoint(
                    now - 6 * oneWeekMs,
                    65.0,
                    estimated1rmKg = 78.0,
                    confidence = Estimated1rmCalculator.Confidence.HIGH
                ),
                ChartDataPoint(
                    now - 4 * oneWeekMs,
                    67.5,
                    estimated1rmKg = 81.0,
                    confidence = Estimated1rmCalculator.Confidence.MODERATE
                ),
                ChartDataPoint(
                    now - 3 * oneWeekMs,
                    70.0,
                    isPersonalRecord = true,
                    estimated1rmKg = 84.0,
                    confidence = Estimated1rmCalculator.Confidence.HIGH
                ),
                ChartDataPoint(now - 2 * oneWeekMs, 67.5, estimated1rmKg = null),
                ChartDataPoint(
                    now - oneWeekMs,
                    72.5,
                    isPersonalRecord = true,
                    estimated1rmKg = 87.0,
                    confidence = Estimated1rmCalculator.Confidence.LOW
                ),
            ),
            title = "Progress",
            currentValue = "72.5kg",
            peakValue = "72.5kg",
            deltaText = "+12.5kg",
            secondaryLineColor = DeepRepsTheme.colors.accentSecondary,
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
