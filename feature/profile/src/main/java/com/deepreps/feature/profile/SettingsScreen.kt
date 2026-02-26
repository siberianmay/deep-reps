@file:Suppress("LongMethod")

package com.deepreps.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Settings/Profile screen. Displays user profile options, privacy toggles,
 * and app information in a scrollable column with section cards.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = colors.accentPrimary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4, vertical = spacing.space4),
    ) {
        // Screen title
        Text(
            text = "Settings",
            style = typography.headlineLarge,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space6))

        // Profile section
        ProfileSection(state = state, onIntent = onIntent)

        Spacer(modifier = Modifier.height(spacing.space6))

        // Privacy section
        PrivacySection(state = state, onIntent = onIntent)

        Spacer(modifier = Modifier.height(spacing.space6))

        // About section
        AboutSection(state = state)

        Spacer(modifier = Modifier.height(spacing.space8))
    }
}

// ---------------------------------------------------------------------------
// Profile section
// ---------------------------------------------------------------------------

@Composable
private fun ProfileSection(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    SectionHeader(title = "PROFILE")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.surfaceLow)
            .padding(spacing.space4),
    ) {
        // Experience level
        Text(
            text = "Experience Level",
            style = typography.labelLarge,
            color = colors.onSurfaceSecondary,
        )
        Spacer(modifier = Modifier.height(spacing.space2))
        ExperienceLevelSelector(
            selected = state.experienceLevel,
            onSelect = { onIntent(SettingsIntent.SetExperienceLevel(it)) },
        )

        Spacer(modifier = Modifier.height(spacing.space5))

        // Weight unit
        SettingsRow(label = "Weight Unit") {
            WeightUnitToggle(
                selected = state.weightUnit,
                onSelect = { onIntent(SettingsIntent.SetWeightUnit(it)) },
            )
        }

        Spacer(modifier = Modifier.height(spacing.space5))

        // Body weight
        val unitLabel = when (state.weightUnit) {
            WeightUnit.KG -> "kg"
            WeightUnit.LBS -> "lbs"
        }
        Text(
            text = "Body Weight ($unitLabel)",
            style = typography.labelLarge,
            color = colors.onSurfaceSecondary,
        )
        Spacer(modifier = Modifier.height(spacing.space2))
        OutlinedTextField(
            value = state.bodyWeightDisplay,
            onValueChange = { onIntent(SettingsIntent.SetBodyWeight(it)) },
            placeholder = {
                Text(
                    text = "Optional",
                    style = typography.bodyMedium,
                    color = colors.onSurfaceTertiary,
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = colors.onSurfacePrimary,
                unfocusedTextColor = colors.onSurfacePrimary,
                focusedBorderColor = colors.accentPrimary,
                unfocusedBorderColor = colors.borderSubtle,
                cursorColor = colors.accentPrimary,
            ),
            textStyle = typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// ---------------------------------------------------------------------------
// Privacy section
// ---------------------------------------------------------------------------

@Composable
private fun PrivacySection(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    SectionHeader(title = "PRIVACY")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.surfaceLow)
            .padding(spacing.space4),
    ) {
        SwitchRow(
            label = "Analytics",
            description = "Help improve Deep Reps by sharing anonymous usage data",
            checked = state.analyticsConsent,
            onCheckedChange = { onIntent(SettingsIntent.SetAnalyticsConsent(it)) },
        )

        Spacer(modifier = Modifier.height(spacing.space4))

        SwitchRow(
            label = "Performance Monitoring",
            description = "Share crash reports to help fix bugs faster",
            checked = state.performanceConsent,
            onCheckedChange = { onIntent(SettingsIntent.SetPerformanceConsent(it)) },
        )
    }
}

// ---------------------------------------------------------------------------
// About section
// ---------------------------------------------------------------------------

@Composable
private fun AboutSection(state: SettingsUiState) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    SectionHeader(title = "ABOUT")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.surfaceLow)
            .padding(spacing.space4),
    ) {
        SettingsRow(label = "App") {
            Text(
                text = "Deep Reps",
                style = typography.bodyMedium,
                color = colors.onSurfaceTertiary,
            )
        }

        Spacer(modifier = Modifier.height(spacing.space3))

        SettingsRow(label = "Version") {
            Text(
                text = state.appVersion,
                style = typography.bodyMedium,
                color = colors.onSurfaceTertiary,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable components
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(title: String) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Text(
        text = title,
        style = typography.labelMedium,
        color = colors.onSurfaceTertiary,
        modifier = Modifier.padding(bottom = spacing.space2),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    content: @Composable () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = typography.bodyLarge,
            color = colors.onSurfacePrimary,
        )
        content()
    }
}

@Composable
private fun SwitchRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
            )
            Text(
                text = description,
                style = typography.bodySmall,
                color = colors.onSurfaceTertiary,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accentPrimary,
                checkedTrackColor = colors.accentPrimaryContainer,
                uncheckedThumbColor = colors.onSurfaceTertiary,
                uncheckedTrackColor = colors.surfaceMedium,
                uncheckedBorderColor = colors.borderSubtle,
            ),
        )
    }
}

@Composable
private fun ExperienceLevelSelector(
    selected: ExperienceLevel,
    onSelect: (ExperienceLevel) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.sm))
            .background(colors.surfaceMedium),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        ExperienceLevel.entries.forEach { level ->
            val isSelected = level == selected
            val bgColor = if (isSelected) colors.accentPrimary else colors.surfaceMedium
            val textColor = if (isSelected) colors.surfaceLowest else colors.onSurfaceSecondary
            val displayName = level.name.lowercase().replaceFirstChar { it.uppercase() }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(radius.sm))
                    .background(bgColor)
                    .height(40.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(level) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = displayName,
                    style = typography.labelLarge,
                    color = textColor,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun WeightUnitToggle(
    selected: WeightUnit,
    onSelect: (WeightUnit) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(radius.sm))
            .background(colors.surfaceMedium),
    ) {
        WeightUnit.entries.forEach { unit ->
            val isSelected = unit == selected
            val bgColor = if (isSelected) colors.accentPrimary else colors.surfaceMedium
            val textColor = if (isSelected) colors.surfaceLowest else colors.onSurfaceSecondary

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(radius.sm))
                    .background(bgColor)
                    .height(36.dp)
                    .padding(horizontal = DeepRepsTheme.spacing.space4)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onSelect(unit) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = unit.value.uppercase(),
                    style = typography.labelLarge,
                    color = textColor,
                )
            }
        }
    }
}
