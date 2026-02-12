package com.deepreps.feature.onboarding

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
import androidx.compose.ui.text.style.TextAlign
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Screen 1: Welcome screen.
 *
 * Design spec: design-system.md Section 4.1, Screen 1.
 * - App logo + name centered at 120dp from top
 * - Tagline below name
 * - [Get Started] button at bottom
 * - Background: surface-lowest
 */
@Suppress("LongMethod")
@Composable
internal fun WelcomeStepScreen(
    onIntent: (OnboardingIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = spacing.space4),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(modifier = Modifier.height(spacing.space10))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Phase 2: Add branded gym silhouette illustration when asset is available

            Spacer(modifier = Modifier.height(spacing.space10))

            Text(
                text = "Deep Reps",
                style = typography.displayMedium,
                color = colors.onSurfacePrimary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(spacing.space2))

            Text(
                text = "AI-powered strength tracking",
                style = typography.bodyLarge,
                color = colors.onSurfaceSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        DeepRepsButton(
            text = "Get Started",
            onClick = { onIntent(OnboardingIntent.NextStep) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space8),
        )
    }
}
