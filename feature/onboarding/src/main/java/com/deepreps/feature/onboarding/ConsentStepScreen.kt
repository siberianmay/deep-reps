package com.deepreps.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.deepreps.core.ui.component.DeepRepsButton
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Screen 0: Privacy & Consent.
 *
 * Design spec: design-system.md Section 4.1, Screen 0.
 * - Title: "Your Data, Your Choice" -- display-small, 32dp from top
 * - Body text explaining data storage
 * - Two toggle switches: analytics (default OFF), crashlytics (default OFF)
 * - Subtitle: "You can change these anytime in Settings"
 * - [Continue] button: bottom-pinned, always enabled
 */
@Composable
internal fun ConsentStepScreen(
    analyticsConsent: Boolean,
    crashlyticsConsent: Boolean,
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
            text = "Your Data, Your Choice",
            style = typography.displaySmall,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        Text(
            text = "Deep Reps stores all workout data locally on your device. " +
                "We collect anonymous usage analytics to improve the app.",
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space6))

        // Analytics toggle
        ConsentToggleRow(
            label = "Analytics (crash reports & usage data)",
            checked = analyticsConsent,
            onCheckedChange = { onIntent(OnboardingIntent.SetAnalyticsConsent(it)) },
            accessibilityLabel = "Analytics consent toggle",
        )

        Spacer(modifier = Modifier.height(spacing.space3))

        // Crashlytics toggle
        ConsentToggleRow(
            label = "Performance monitoring",
            checked = crashlyticsConsent,
            onCheckedChange = { onIntent(OnboardingIntent.SetCrashlyticsConsent(it)) },
            accessibilityLabel = "Performance monitoring consent toggle",
        )

        Spacer(modifier = Modifier.height(spacing.space3))

        Text(
            text = "You can change these anytime in Settings",
            style = typography.bodySmall,
            color = colors.onSurfaceTertiary,
        )

        Spacer(modifier = Modifier.weight(1f))

        DeepRepsButton(
            text = "Continue",
            onClick = { onIntent(OnboardingIntent.NextStep) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = spacing.space8),
        )
    }
}

/**
 * A row with a label and an M3 Switch.
 * 56dp minimum height per design spec.
 */
@Composable
private fun ConsentToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accessibilityLabel: String,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = accessibilityLabel },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            color = colors.onSurfacePrimary,
            modifier = Modifier.weight(1f),
        )

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accentPrimary,
                checkedTrackColor = colors.accentPrimaryContainer,
                uncheckedThumbColor = colors.onSurfaceTertiary,
                uncheckedTrackColor = colors.surfaceHigh,
                uncheckedBorderColor = colors.borderSubtle,
            ),
        )
    }
}
