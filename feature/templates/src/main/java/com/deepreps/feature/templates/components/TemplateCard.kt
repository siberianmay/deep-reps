package com.deepreps.feature.templates.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.templates.TemplateUi

/**
 * Card representing a saved workout template.
 *
 * Design spec: design-system.md Section 3.7.
 * - Template name: headline-medium
 * - Last used: body-small, on-surface-tertiary
 * - Muscle group chips + exercise count row
 * - Exercise preview: body-small, single line, ellipsized
 *
 * @param template The template UI model.
 * @param onClick Called when the card is tapped.
 * @param modifier External modifier.
 */
@Suppress("LongMethod")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateCard(
    template: TemplateUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle),
    ) {
        Column(
            modifier = Modifier.padding(spacing.space4),
        ) {
            // Row 1: Name + last used
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = template.name,
                    style = typography.headlineMedium,
                    color = colors.onSurfacePrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(spacing.space1))

            Text(
                text = template.lastUsedText,
                style = typography.bodySmall,
                color = colors.onSurfaceTertiary,
            )

            Spacer(modifier = Modifier.height(spacing.space3))

            // Row 2: Muscle group chips + exercise count
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(spacing.space2),
                    verticalArrangement = Arrangement.spacedBy(spacing.space1),
                ) {
                    template.muscleGroupNames.forEach { groupName ->
                        MuscleGroupLabel(name = groupName)
                    }
                }

                Spacer(modifier = Modifier.width(spacing.space2))

                Text(
                    text = "${template.exerciseCount} exerc.",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceSecondary,
                )
            }

            Spacer(modifier = Modifier.height(spacing.space2))

            // Row 3: Exercise preview
            Text(
                text = template.exercisePreview,
                style = typography.bodySmall,
                color = colors.onSurfaceTertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Small label chip for displaying a muscle group name on a template card.
 *
 * Uses the muscle group color at 15% opacity background per design spec.
 */
@Composable
private fun MuscleGroupLabel(
    name: String,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(radius.xl),
        color = colors.accentPrimaryContainer,
    ) {
        Text(
            text = name,
            style = typography.labelMedium,
            color = colors.accentPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Template Card - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun TemplateCardDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        TemplateCard(
            template = TemplateUi(
                id = 1,
                name = "Push Day A",
                muscleGroupNames = listOf("Chest", "Shoulders", "Arms"),
                exerciseCount = 5,
                exercisePreview = "Bench Press, OHP, Lateral Raise, ...",
                lastUsedText = "Used 3 days ago",
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Template Card - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TemplateCardLightPreview() {
    DeepRepsTheme(darkTheme = false) {
        TemplateCard(
            template = TemplateUi(
                id = 2,
                name = "Leg Day",
                muscleGroupNames = listOf("Legs"),
                exerciseCount = 4,
                exercisePreview = "Squat, Leg Press, Lunges, Calf Raise",
                lastUsedText = "Used today",
            ),
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
