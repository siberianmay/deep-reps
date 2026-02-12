package com.deepreps.feature.workout.active.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Paused workout overlay per design-system.md Section 4.7.1.
 *
 * Semi-transparent scrim over the workout content with a centered pause banner.
 * - Scrim: overlay-scrim at 50% opacity
 * - Banner: 280dp wide, 180dp tall, surface-high background, radius-lg, elevation-5
 * - Pause icon: 48dp, status-warning tint
 * - "PAUSED" label: headline-medium
 * - Paused duration: body-large, counts up (provided by caller)
 * - [Resume] button: full width, 56dp, accent-primary
 * - [End Workout] text: status-error
 *
 * @param pausedDurationText Formatted paused duration (e.g., "02:34").
 * @param onResume Callback when user taps Resume.
 * @param onEndWorkout Callback when user taps End Workout.
 */
@Suppress("LongMethod")
@Composable
fun PausedOverlay(
    pausedDurationText: String,
    onResume: () -> Unit,
    onEndWorkout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing
    val elevation = DeepRepsTheme.elevation

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.overlayScrim.copy(alpha = 0.5f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { /* consume clicks to prevent interaction with content behind */ },
            )
            .semantics { contentDescription = "Workout paused" },
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .width(280.dp)
                .height(180.dp),
            shape = RoundedCornerShape(radius.lg),
            color = colors.surfaceHigh,
            shadowElevation = elevation.level5,
        ) {
            Column(
                modifier = Modifier.padding(spacing.space4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(spacing.space4))

                // Pause icon
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = colors.statusWarning,
                )

                // "PAUSED" label
                Text(
                    text = "PAUSED",
                    style = typography.headlineMedium,
                    color = colors.onSurfacePrimary,
                )

                // Paused duration
                Text(
                    text = pausedDurationText,
                    style = typography.bodyLarge,
                    color = colors.onSurfaceSecondary,
                )

                Spacer(modifier = Modifier.weight(1f))

                // Resume button
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(radius.md),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentPrimary,
                    ),
                ) {
                    Text(
                        text = "Resume",
                        style = typography.labelLarge,
                        color = colors.onSurfacePrimary,
                    )
                }

                // End Workout text
                TextButton(onClick = onEndWorkout) {
                    Text(
                        text = "End Workout",
                        style = typography.bodyMedium,
                        color = colors.statusError,
                    )
                }
            }
        }
    }
}
