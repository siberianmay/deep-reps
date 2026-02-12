package com.deepreps.core.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Standard card for Deep Reps.
 *
 * Uses `surface-low` background, `radius-md` corners, and tonal elevation
 * per design-system.md Section 2.5.
 *
 * @param modifier External modifier.
 * @param containerColor Override background color. Defaults to `surface-low`.
 * @param elevation Shadow/tonal elevation. Defaults to `elevation-1`.
 * @param content Card content via [ColumnScope].
 */
@Composable
fun DeepRepsCard(
    modifier: Modifier = Modifier,
    containerColor: Color = DeepRepsTheme.colors.surfaceLow,
    elevation: Dp = DeepRepsTheme.elevation.level1,
    content: @Composable ColumnScope.() -> Unit,
) {
    val radius = DeepRepsTheme.radius

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        content = content,
    )
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Card - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun CardDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Card content",
                    style = DeepRepsTheme.typography.bodyLarge,
                    color = DeepRepsTheme.colors.onSurfacePrimary,
                )
            }
        }
    }
}

@Preview(name = "Card - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CardLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        DeepRepsCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Card content",
                    style = DeepRepsTheme.typography.bodyLarge,
                    color = DeepRepsTheme.colors.onSurfacePrimary,
                )
            }
        }
    }
}

@Preview(name = "Elevated Card - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ElevatedCardDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        DeepRepsCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = DeepRepsTheme.colors.surfaceMedium,
            elevation = DeepRepsTheme.elevation.level3,
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Elevated card",
                    style = DeepRepsTheme.typography.bodyLarge,
                    color = DeepRepsTheme.colors.onSurfacePrimary,
                )
            }
        }
    }
}
