package com.deepreps.feature.exerciselibrary.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Placeholder for the anatomical muscle diagram.
 *
 * Per design-system.md Section 4.5: the real implementation will load
 * `resources/anatomy_template.svg` (a stock male front+back SVG) and fill
 * muscle zone paths programmatically:
 * - Primary muscles: muscle group color at 85% opacity
 * - Secondary muscles: muscle group color at 30% opacity
 * - Non-highlighted zones: #717171 base fill
 * - Face/hands: always #9A9A9A
 * - Body outline: #4A4A54 stroke, 1.5dp
 *
 * Phase 2: Implement SVG-based anatomy highlighting per architecture.md.
 *       This requires:
 *       1. The cleaned SVG asset with named paths per sub-muscle zone
 *       2. A custom Compose Canvas or AndroidView rendering the SVG
 *       3. A mapping from exercise secondaryMuscles strings to SVG path IDs
 *       4. Programmatic fill color changes on the parsed SVG DOM
 *       Candidate approach: Use `coil-svg` to decode the SVG, then use a custom
 *       Painter that overrides fill colors based on the exercise's muscle data.
 *       Alternatively, use AndroidX graphics to parse and render SVG paths natively.
 *
 * @param exerciseId The exercise whose muscles to highlight (unused in placeholder).
 * @param primaryGroupId The primary muscle group ID for color selection.
 * @param modifier External modifier.
 */
@Composable
fun AnatomyDiagram(
    @Suppress("UnusedParameter") exerciseId: Long,
    primaryGroupId: Long,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    val muscleGroup = muscleGroupFromId(primaryGroupId)
    val groupColor = if (muscleGroup != null) {
        colors.colorForMuscleGroup(muscleGroup)
    } else {
        colors.onSurfaceTertiary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = groupColor.copy(alpha = 0.08f),
                shape = RoundedCornerShape(radius.md),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Anatomy Diagram",
            style = typography.bodyLarge,
            color = groupColor.copy(alpha = 0.6f),
        )
    }
}

/**
 * Maps a muscle group database ID back to the [MuscleGroup] enum.
 * Returns null if the ID doesn't map to a known group.
 *
 * Assumes 1-indexed IDs matching [MuscleGroup] enum ordinal + 1.
 */
private fun muscleGroupFromId(groupId: Long): MuscleGroup? {
    val index = (groupId - 1).toInt()
    return MuscleGroup.entries.getOrNull(index)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Chest - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChestDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 1,
            primaryGroupId = 3, // Chest
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Legs - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun LegsDiagramDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        AnatomyDiagram(
            exerciseId = 2,
            primaryGroupId = 1, // Legs
            modifier = Modifier.padding(16.dp),
        )
    }
}
