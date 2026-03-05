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
import com.deepreps.feature.progress.DashboardTab

private val tabs = DashboardTab.entries

private fun DashboardTab.label(): String = when (this) {
    DashboardTab.RECORDS -> "Records"
    DashboardTab.HISTORY -> "History"
}

/**
 * Two-segment tab selector for switching between Records and History views.
 *
 * Matches the visual style of [TimeRangeSelector]: 40dp tall, surfaceLow/accentPrimaryContainer
 * fill, borderSubtle/accentPrimary border, labelLarge text, radiusSm shape.
 */
@Suppress("LongMethod")
@Composable
fun DashboardTabSelector(
    selected: DashboardTab,
    onSelect: (DashboardTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val radius = DeepRepsTheme.radius

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selected
            val shape = when (tab) {
                tabs.first() -> RoundedCornerShape(
                    topStart = radius.sm,
                    bottomStart = radius.sm,
                )
                tabs.last() -> RoundedCornerShape(
                    topEnd = radius.sm,
                    bottomEnd = radius.sm,
                )
                else -> RoundedCornerShape(0.dp)
            }

            Surface(
                onClick = { onSelect(tab) },
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .semantics {
                        contentDescription =
                            "${tab.label()}, ${if (isSelected) "selected" else "not selected"}"
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
                        text = tab.label(),
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

@Preview(name = "Tab Selector Records - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TabSelectorRecordsDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DashboardTabSelector(
            selected = DashboardTab.RECORDS,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Tab Selector History - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TabSelectorHistoryDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DashboardTabSelector(
            selected = DashboardTab.HISTORY,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
