package com.deepreps.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Circular countdown timer display.
 *
 * Design spec: design-system.md Section 3.4.
 * - Circular progress ring: 160dp diameter, 8dp stroke width
 * - Timer digits: `number-large` (48sp, Bold), centered within ring
 * - Ring color: `status-warning` while counting, `status-error` in last 10s
 *
 * @param remainingSeconds Seconds remaining on the timer.
 * @param totalSeconds Total seconds the timer started from.
 * @param diameter Ring outer diameter.
 * @param strokeWidth Ring stroke width.
 * @param modifier External modifier.
 */
@Composable
fun CountdownTimer(
    remainingSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier,
    diameter: Dp = 160.dp,
    strokeWidth: Dp = 8.dp,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val progress = remember(remainingSeconds, totalSeconds) {
        if (totalSeconds <= 0) 0f
        else remainingSeconds.toFloat() / totalSeconds.toFloat()
    }

    val isUrgent = remainingSeconds in 1..10
    val isTimesUp = remainingSeconds <= 0

    val ringColor = when {
        isTimesUp -> colors.statusError
        isUrgent -> colors.statusError
        else -> colors.statusWarning
    }

    val trackColor = colors.surfaceHigh

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val displayText = if (isTimesUp) "GO" else "%d:%02d".format(minutes, seconds)

    val accessibilityText = if (isTimesUp) {
        "Rest timer complete"
    } else {
        "Rest timer, $minutes minutes $seconds seconds remaining"
    }

    Box(
        modifier = modifier
            .size(diameter)
            .semantics { contentDescription = accessibilityText },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = Size(
                width = size.width - strokePx,
                height = size.height - strokePx,
            )
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)

            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Progress arc (sweeps clockwise from 12 o'clock)
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }

        Text(
            text = displayText,
            style = typography.numberLarge,
            color = if (isTimesUp) colors.statusError else colors.onSurfacePrimary,
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Counting - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CountingDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CountdownTimer(
            remainingSeconds = 92,
            totalSeconds = 120,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Urgent - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun UrgentDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CountdownTimer(
            remainingSeconds = 7,
            totalSeconds = 120,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Times Up - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TimesUpDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CountdownTimer(
            remainingSeconds = 0,
            totalSeconds = 120,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Counting - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CountingLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        CountdownTimer(
            remainingSeconds = 45,
            totalSeconds = 90,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Full Timer - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun FullTimerDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        CountdownTimer(
            remainingSeconds = 120,
            totalSeconds = 120,
            modifier = Modifier.padding(16.dp),
        )
    }
}
