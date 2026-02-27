@file:Suppress("LongMethod")

package com.deepreps.feature.profile

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.theme.DeepRepsTheme
import java.io.File

/**
 * Settings/Profile screen. Displays user profile options, privacy toggles,
 * data export/import, and app information in a scrollable column with section cards.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Holds the cached export file until the user picks a save location via SAF.
    var pendingExportFile by remember { mutableStateOf<File?>(null) }

    // SAF launcher: user picks where to save the exported zip.
    val exportSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip"),
    ) { uri ->
        val file = pendingExportFile
        if (uri != null && file != null && file.exists()) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { output ->
                    file.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
                Toast.makeText(context, "Backup saved", Toast.LENGTH_SHORT).show()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to save backup: ${e.message}",
                    Toast.LENGTH_LONG,
                ).show()
            }
        }
        pendingExportFile = null
    }

    // SAF launcher: user picks a zip/json file to import.
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            viewModel.onIntent(SettingsIntent.ImportData(uri))
        }
    }

    // Collect one-shot side effects.
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is SettingsSideEffect.ExportReady -> {
                    pendingExportFile = effect.file
                    exportSaveLauncher.launch(effect.file.name)
                }
                is SettingsSideEffect.ImportComplete -> {
                    val r = effect.result
                    Toast.makeText(
                        context,
                        "Imported ${r.sessionsImported} sessions, " +
                            "${r.templatesImported} templates",
                        Toast.LENGTH_LONG,
                    ).show()
                }
                is SettingsSideEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent,
        onImportConfirmed = {
            importLauncher.launch(arrayOf("application/zip", "application/json"))
        },
    )
}

@Composable
private fun SettingsContent(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
    onImportConfirmed: () -> Unit,
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

    // Import confirmation dialog
    if (state.showImportConfirmDialog) {
        ImportConfirmDialog(
            onConfirm = {
                onIntent(SettingsIntent.DismissImportDialog)
                onImportConfirmed()
            },
            onDismiss = { onIntent(SettingsIntent.DismissImportDialog) },
        )
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

        // Rep ranges section
        RepRangesSection(state = state, onIntent = onIntent)

        Spacer(modifier = Modifier.height(spacing.space6))

        // Privacy section
        PrivacySection(state = state, onIntent = onIntent)

        Spacer(modifier = Modifier.height(spacing.space6))

        // Data section
        DataSection(state = state, onIntent = onIntent)

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
// Rep ranges section
// ---------------------------------------------------------------------------

@Composable
private fun RepRangesSection(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    SectionHeader(title = "TRAINING PREFERENCES")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.surfaceLow)
            .padding(spacing.space4),
    ) {
        WorkingSetsSelector(
            selected = state.defaultWorkingSets,
            experienceLevel = state.experienceLevel,
            onSelect = { onIntent(SettingsIntent.SetDefaultWorkingSets(it)) },
        )

        Spacer(modifier = Modifier.height(spacing.space5))

        RepRangeRow(
            label = "Compound Exercises",
            min = state.compoundRepMin,
            max = state.compoundRepMax,
            onRangeChange = { min, max ->
                onIntent(SettingsIntent.SetCompoundRepRange(min, max))
            },
        )

        Spacer(modifier = Modifier.height(spacing.space5))

        RepRangeRow(
            label = "Isolation Exercises",
            min = state.isolationRepMin,
            max = state.isolationRepMax,
            onRangeChange = { min, max ->
                onIntent(SettingsIntent.SetIsolationRepRange(min, max))
            },
        )
    }
}

@Composable
private fun WorkingSetsSelector(
    selected: Int,
    experienceLevel: ExperienceLevel,
    onSelect: (Int) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    val options = listOf(0, 2, 3, 4, 5, 6)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Working Sets per Exercise",
            style = typography.labelLarge,
            color = colors.onSurfaceSecondary,
        )

        Spacer(modifier = Modifier.height(spacing.space2))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(radius.sm))
                .background(colors.surfaceMedium),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            options.forEach { value ->
                val isSelected = value == selected
                val bgColor = if (isSelected) colors.accentPrimary else colors.surfaceMedium
                val textColor = if (isSelected) colors.surfaceLowest else colors.onSurfaceSecondary
                val label = if (value == 0) "Auto" else value.toString()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(radius.sm))
                        .background(bgColor)
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onSelect(value) },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = typography.labelLarge,
                        color = textColor,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        if (selected == 0) {
            val defaultSets = when (experienceLevel) {
                ExperienceLevel.BEGINNER -> 3
                ExperienceLevel.INTERMEDIATE -> 4
                ExperienceLevel.ADVANCED -> 5
            }
            val levelName = experienceLevel.name.lowercase().replaceFirstChar { it.uppercase() }

            Spacer(modifier = Modifier.height(spacing.space1))

            Text(
                text = "Using default for $levelName: $defaultSets sets",
                style = typography.bodySmall,
                color = colors.onSurfaceTertiary,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepRangeRow(
    label: String,
    min: Int,
    max: Int,
    onRangeChange: (Int, Int) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
            )
            Text(
                text = "$min - $max reps",
                style = typography.labelLarge,
                color = colors.accentPrimary,
            )
        }

        Spacer(modifier = Modifier.height(DeepRepsTheme.spacing.space2))

        RangeSlider(
            value = min.toFloat()..max.toFloat(),
            onValueChange = { range ->
                onRangeChange(range.start.toInt(), range.endInclusive.toInt())
            },
            valueRange = 1f..30f,
            steps = 28,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentPrimary,
                activeTrackColor = colors.accentPrimary,
                inactiveTrackColor = colors.surfaceMedium,
            ),
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
// Data section
// ---------------------------------------------------------------------------

@Composable
private fun DataSection(
    state: SettingsUiState,
    onIntent: (SettingsIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    SectionHeader(title = "DATA")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius.md))
            .background(colors.surfaceLow)
            .padding(spacing.space4),
    ) {
        // Export row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = !state.isExporting) {
                    onIntent(SettingsIntent.ExportData)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Export Data",
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
            )
            if (state.isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.accentPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = ">",
                    style = typography.bodyLarge,
                    color = colors.onSurfaceTertiary,
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.space2))

        // Import row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(enabled = !state.isImporting) {
                    onIntent(SettingsIntent.RequestImport)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Import Data",
                style = typography.bodyLarge,
                color = colors.onSurfacePrimary,
            )
            if (state.isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colors.accentPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = ">",
                    style = typography.bodyLarge,
                    color = colors.onSurfaceTertiary,
                )
            }
        }
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
// Import confirmation dialog
// ---------------------------------------------------------------------------

@Composable
private fun ImportConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Import Data?")
        },
        text = {
            Text(
                text = "This will replace all existing workout data. " +
                    "This cannot be undone. Continue?",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "Import",
                    color = DeepRepsTheme.colors.statusError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
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
