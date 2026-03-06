package com.deepreps.core.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.R
import com.deepreps.core.ui.theme.DeepRepsTheme

private val BaseColor = Color(0xFF2E2E3A)
private const val PRIMARY_ALPHA = 0.85f
private const val SECONDARY_ALPHA = 0.30f
private val ImageHeight = 280.dp

/**
 * Multi-layer VectorDrawable-based anatomy diagram.
 *
 * Renders a base body silhouette with overlay layers for each highlighted muscle group.
 * Primary muscles are shown at 85% opacity in their accent color; secondary muscles
 * at 30% opacity.
 *
 * @param muscleHighlights Map of muscle groups to their highlight level.
 * @param modifier External modifier.
 */
@Suppress("LongMethod")
@Composable
fun AnatomyDiagram(
    muscleHighlights: Map<MuscleGroup, HighlightLevel>,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val primaryGroup = muscleHighlights.entries
        .firstOrNull { it.value == HighlightLevel.PRIMARY }
        ?.key
    val hasSecondary = muscleHighlights.values.any { it == HighlightLevel.SECONDARY }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ImageHeight)
                .graphicsLayer { },
        ) {
            // Base silhouette layer — always rendered
            Image(
                painter = painterResource(R.drawable.ic_anatomy_base),
                contentDescription = "Body silhouette",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(BaseColor),
            )

            // Muscle group overlay layers
            muscleHighlights.forEach { (group, level) ->
                val groupColor = colors.colorForMuscleGroup(group)
                val alpha = when (level) {
                    HighlightLevel.PRIMARY -> PRIMARY_ALPHA
                    HighlightLevel.SECONDARY -> SECONDARY_ALPHA
                }

                Image(
                    painter = painterResource(drawableForGroup(group)),
                    contentDescription = formatMuscleGroupLabel(group),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(groupColor.copy(alpha = alpha)),
                )
            }
        }

        // Legend — only shown when secondary highlights exist
        if (hasSecondary && primaryGroup != null) {
            AnatomyLegend(primaryColor = colors.colorForMuscleGroup(primaryGroup))
        }

        // Primary muscle group label
        if (primaryGroup != null) {
            Text(
                text = formatMuscleGroupLabel(primaryGroup),
                style = typography.labelMedium,
                color = colors.colorForMuscleGroup(primaryGroup),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AnatomyLegend(primaryColor: Color) {
    val typography = DeepRepsTheme.typography
    val colors = DeepRepsTheme.colors

    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendItem(
            color = primaryColor.copy(alpha = PRIMARY_ALPHA),
            label = "Primary",
            labelColor = colors.onSurfaceSecondary,
        )
        Spacer(modifier = Modifier.width(16.dp))
        LegendItem(
            color = primaryColor.copy(alpha = SECONDARY_ALPHA),
            label = "Secondary",
            labelColor = colors.onSurfaceSecondary,
        )
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    labelColor: Color,
) {
    val typography = DeepRepsTheme.typography

    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color,
            content = {},
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = typography.bodySmall,
            color = labelColor,
        )
    }
}

private fun drawableForGroup(group: MuscleGroup): Int = when (group) {
    MuscleGroup.CHEST -> R.drawable.ic_anatomy_chest
    MuscleGroup.SHOULDERS -> R.drawable.ic_anatomy_shoulders
    MuscleGroup.ARMS -> R.drawable.ic_anatomy_arms
    MuscleGroup.CORE -> R.drawable.ic_anatomy_core
    MuscleGroup.LEGS -> R.drawable.ic_anatomy_legs
    MuscleGroup.BACK -> R.drawable.ic_anatomy_back
    MuscleGroup.LOWER_BACK -> R.drawable.ic_anatomy_lower_back
}

/**
 * Formats a [MuscleGroup] enum value as a user-facing label.
 * e.g. LOWER_BACK -> "Lower Back"
 */
internal fun formatMuscleGroupLabel(group: MuscleGroup): String =
    group.value
        .split("_")
        .joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(
    name = "Chest primary, shoulders+arms secondary",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun ChestWithSecondariesPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            muscleHighlights = mapOf(
                MuscleGroup.CHEST to HighlightLevel.PRIMARY,
                MuscleGroup.SHOULDERS to HighlightLevel.SECONDARY,
                MuscleGroup.ARMS to HighlightLevel.SECONDARY,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "Legs primary only",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun LegsPrimaryOnlyPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            muscleHighlights = mapOf(
                MuscleGroup.LEGS to HighlightLevel.PRIMARY,
            ),
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(
    name = "No highlights - base silhouette",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun NoHighlightsPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            muscleHighlights = emptyMap(),
            modifier = Modifier.padding(16.dp),
        )
    }
}
