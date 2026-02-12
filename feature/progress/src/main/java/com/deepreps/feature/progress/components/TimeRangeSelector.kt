package com.deepreps.feature.progress.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.progress.TimeRange

/**
 * Segmented control for selecting time range filters.
 *
 * Design spec: design-system.md Section 4.9
 * - Options: 4W, 12W, 6M, All
 * - 40dp tall, full width minus 32dp margin
 * - radius-sm segments
 * - Default: 12W
 *
 * @param selected Currently selected time range.
 * @param onSelect Callback when a range is tapped.
 * @param modifier External modifier.
 */
@Suppress("LongMethod")
@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selected
            val shape = when (range) {
                TimeRange.entries.first() -> RoundedCornerShape(
                    topStart = radius.sm,
                    bottomStart = radius.sm,
                )
                TimeRange.entries.last() -> RoundedCornerShape(
                    topEnd = radius.sm,
                    bottomEnd = radius.sm,
                )
                else -> RoundedCornerShape(0.dp)
            }

            Surface(
                onClick = { onSelect(range) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .semantics {
                        contentDescription = "${range.label}, ${if (isSelected) "selected" else "not selected"}"
                    },
                shape = shape,
                color = if (isSelected) colors.accentPrimaryContainer else colors.surfaceLow,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) colors.accentPrimary else colors.borderSubtle,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = range.label,
                        style = DeepRepsTheme.typography.labelLarge,
                        color = if (isSelected) {
                            colors.accentPrimary
                        } else {
                            colors.onSurfaceSecondary
                        },
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "TimeRange - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TimeRangeDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        TimeRangeSelector(
            selected = TimeRange.TWELVE_WEEKS,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "TimeRange All - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TimeRangeAllDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        TimeRangeSelector(
            selected = TimeRange.ALL,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "TimeRange - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TimeRangeLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        TimeRangeSelector(
            selected = TimeRange.SIX_MONTHS,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
