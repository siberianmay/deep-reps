package com.deepreps.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Chip component with muscle-group-specific colors.
 *
 * Design spec: design-system.md Section 3.3 states.
 * - Selected: muscle group color background at 15% opacity, muscle group color border (2dp)
 * - Unselected: `surface-low` background, `border-subtle` 1dp border
 *
 * @param muscleGroup The muscle group this chip represents.
 * @param isSelected Whether the chip is currently selected.
 * @param onClick Click handler.
 * @param modifier External modifier.
 */
@Composable
fun MuscleGroupChip(
    muscleGroup: MuscleGroup,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    val groupColor = colors.colorForMuscleGroup(muscleGroup)
    val displayName = remember(muscleGroup) { muscleGroupDisplayName(muscleGroup) }

    val accessibilityText = "$displayName, ${if (isSelected) "selected" else "not selected"}"

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .semantics { contentDescription = accessibilityText },
        label = {
            Text(
                text = displayName,
                style = typography.labelMedium,
                color = if (isSelected) groupColor else colors.onSurfaceSecondary,
            )
        },
        shape = RoundedCornerShape(radius.sm),
        border = if (isSelected) {
            BorderStroke(width = 2.dp, color = groupColor)
        } else {
            BorderStroke(width = 1.dp, color = colors.borderSubtle)
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = colors.surfaceLow,
            selectedContainerColor = groupColor.copy(alpha = 0.15f),
            labelColor = colors.onSurfaceSecondary,
            selectedLabelColor = groupColor,
        ),
    )
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

fun muscleGroupDisplayName(group: MuscleGroup): String = when (group) {
    MuscleGroup.LEGS -> "Legs"
    MuscleGroup.LOWER_BACK -> "Lower Back"
    MuscleGroup.CHEST -> "Chest"
    MuscleGroup.BACK -> "Back"
    MuscleGroup.SHOULDERS -> "Shoulders"
    MuscleGroup.ARMS -> "Arms"
    MuscleGroup.CORE -> "Core"
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Chips - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ChipsDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MuscleGroup.entries.forEachIndexed { index, group ->
                MuscleGroupChip(
                    muscleGroup = group,
                    isSelected = index < 3, // First 3 selected for preview
                    onClick = {},
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Chips - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChipsLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MuscleGroup.entries.forEachIndexed { index, group ->
                MuscleGroupChip(
                    muscleGroup = group,
                    isSelected = index % 2 == 0,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(name = "Single selected - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SingleSelectedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        MuscleGroupChip(
            muscleGroup = MuscleGroup.CHEST,
            isSelected = true,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Single unselected - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SingleUnselectedDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        MuscleGroupChip(
            muscleGroup = MuscleGroup.LEGS,
            isSelected = false,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
