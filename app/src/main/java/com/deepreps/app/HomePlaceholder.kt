package com.deepreps.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.deepreps.core.ui.component.ButtonVariant
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Temporary home screen placeholder until the Home feature module is built.
 *
 * Provides a "Start Workout" button to test the workout setup flow.
 */
@Composable
fun HomePlaceholder(
    onStartWorkout: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Deep Reps",
            style = typography.displayMedium,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        Text(
            text = "Welcome! Ready to train?",
            style = typography.bodyLarge,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space7))

        DeepRepsButton(
            text = "Start Workout",
            onClick = onStartWorkout,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        DeepRepsButton(
            text = "From Template",
            onClick = { /* Navigate to template list when implemented */ },
            variant = ButtonVariant.Secondary,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
