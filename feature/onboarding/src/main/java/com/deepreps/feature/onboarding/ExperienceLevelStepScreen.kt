package com.deepreps.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Screen 2: Experience Level selection.
 *
 * Design spec: design-system.md Section 4.1, Screen 2.
 * - Title: "What's your training experience?"
 * - 3 selectable cards: Beginner, Intermediate, Advanced
 * - Selection state: accent-primary border (2dp), accent-primary-container background
 * - [Continue] button: disabled until selection made
 */
@Composable
internal fun ExperienceLevelStepScreen(
    selectedLevel: ExperienceLevel?,
    onIntent: (OnboardingIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.space4),
    ) {
        Spacer(modifier = Modifier.height(spacing.space7))

        Text(
            text = "What's your training experience?",
            style = typography.displaySmall,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        Text(
            text = "This helps us generate your first workout plan",
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space6))

        ExperienceLevelCard(
            level = ExperienceLevel.BEGINNER,
            title = "Total Beginner",
            description = "0-6 months of gym experience",
            icon = Icons.Filled.Person,
            isSelected = selectedLevel == ExperienceLevel.BEGINNER,
            onClick = { onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.BEGINNER)) },
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        ExperienceLevelCard(
            level = ExperienceLevel.INTERMEDIATE,
            title = "Intermediate",
            description = "6-18 months, comfortable with main lifts",
            icon = Icons.Filled.FitnessCenter,
            isSelected = selectedLevel == ExperienceLevel.INTERMEDIATE,
            onClick = {
                onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.INTERMEDIATE))
            },
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        ExperienceLevelCard(
            level = ExperienceLevel.ADVANCED,
            title = "Advanced",
            description = "18+ months, structured programming",
            icon = Icons.Filled.Star,
            isSelected = selectedLevel == ExperienceLevel.ADVANCED,
            onClick = { onIntent(OnboardingIntent.SetExperienceLevel(ExperienceLevel.ADVANCED)) },
        )

        Spacer(modifier = Modifier.weight(1f))

        DeepRepsButton(
            text = "Continue",
            onClick = { onIntent(OnboardingIntent.NextStep) },
            enabled = selectedLevel != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space8),
        )
    }
}

/**
 * Selectable experience level card.
 *
 * Per design spec: full width minus 32dp, 88dp height, radius-md.
 * Selected: accent-primary border (2dp), accent-primary-container background.
 */
@Composable
private fun ExperienceLevelCard(
    level: ExperienceLevel,
    title: String,
    description: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val containerColor = if (isSelected) {
        colors.accentPrimaryContainer
    } else {
        colors.surfaceLow
    }

    val border = if (isSelected) {
        BorderStroke(2.dp, colors.accentPrimary)
    } else {
        BorderStroke(1.dp, colors.borderSubtle)
    }

    val accessibilityText = "$title, $description, ${if (isSelected) "selected" else "not selected"}"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .semantics { contentDescription = accessibilityText },
        shape = RoundedCornerShape(radius.md),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) colors.accentPrimary else colors.onSurfaceSecondary,
            )

            Column {
                Text(
                    text = title,
                    style = typography.headlineSmall,
                    color = if (isSelected) {
                        colors.accentPrimary
                    } else {
                        colors.onSurfacePrimary
                    },
                )

                Text(
                    text = description,
                    style = typography.bodySmall,
                    color = colors.onSurfaceSecondary,
                    maxLines = 2,
                )
            }
        }
    }
}
