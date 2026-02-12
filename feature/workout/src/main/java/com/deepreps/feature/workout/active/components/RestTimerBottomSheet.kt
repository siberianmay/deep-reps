package com.deepreps.feature.workout.active.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.data.timer.RestTimerState
import com.deepreps.core.ui.component.CountdownTimer
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Rest timer bottom sheet per design-system.md Section 3.4.
 *
 * Container: Bottom sheet, 280dp tall, full width, radius-lg top corners.
 * - "REST" label: label-large, on-surface-secondary, 16dp above ring
 * - Circular progress ring: 160dp diameter (CountdownTimer component)
 * - Skip button: 56dp height, radius-xl, secondary style
 * - +30s button: 56dp height, radius-xl, secondary style
 * - Buttons: 24dp from bottom, 16dp horizontal margin
 *
 * Auto-dismisses when timer reaches 0 (handled by the caller observing RestTimerState).
 *
 * @param timerState Current rest timer state.
 * @param onSkip Callback when user taps Skip.
 * @param onExtend Callback when user taps +30s.
 * @param modifier External modifier.
 */
@Suppress("LongMethod")
@Composable
fun RestTimerBottomSheet(
    timerState: RestTimerState,
    onSkip: () -> Unit,
    onExtend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(topStart = radius.lg, topEnd = radius.lg))
            .background(colors.surfaceLow)
            .padding(horizontal = spacing.space4)
            .semantics {
                contentDescription = "Rest timer, ${timerState.remainingSeconds} seconds remaining"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(spacing.space4))

        // "REST" label
        Text(
            text = "REST",
            style = typography.labelLarge,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        // Circular countdown timer
        CountdownTimer(
            remainingSeconds = timerState.remainingSeconds,
            totalSeconds = timerState.totalSeconds,
        )

        Spacer(modifier = Modifier.weight(1f))

        // Buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space6),
            horizontalArrangement = Arrangement.spacedBy(spacing.space4),
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(radius.xl),
            ) {
                Text(
                    text = "Skip",
                    style = typography.labelLarge,
                )
            }

            OutlinedButton(
                onClick = onExtend,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(radius.xl),
            ) {
                Text(
                    text = "+30s",
                    style = typography.labelLarge,
                )
            }
        }
    }
}
