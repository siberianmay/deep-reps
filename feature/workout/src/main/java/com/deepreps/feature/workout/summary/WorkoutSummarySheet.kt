package com.deepreps.feature.workout.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.core.ui.theme.PrGold
import java.util.Locale

/**
 * Post-workout summary shown as a ModalBottomSheet.
 *
 * Design spec: design-system.md Section 4.8.
 * - Duration, exercises completed, total working sets
 * - Per-group volume breakdown
 * - Total tonnage
 * - PR highlights with gold star icon
 * - "Done" and "Save as Template" actions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummarySheet(
    onDismiss: () -> Unit,
    onNavigateToCreateTemplate: (exerciseIds: List<Long>) -> Unit,
    viewModel: WorkoutSummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is WorkoutSummarySideEffect.NavigateToHome -> onDismiss()
                is WorkoutSummarySideEffect.NavigateToCreateTemplate -> {
                    onNavigateToCreateTemplate(effect.exerciseIds)
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DeepRepsTheme.colors.surfaceLow,
    ) {
        WorkoutSummarySheetContent(
            state = state,
            onIntent = viewModel::onIntent,
        )
    }
}

@Suppress("LongMethod")
@Composable
internal fun WorkoutSummarySheetContent(
    state: WorkoutSummaryUiState,
    onIntent: (WorkoutSummaryIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4, vertical = spacing.space4),
    ) {
        when {
            state.isLoading -> {
                LoadingIndicator(message = "Calculating summary...")
            }

            state.errorMessage != null -> {
                ErrorState(
                    message = state.errorMessage,
                    onRetry = { onIntent(WorkoutSummaryIntent.Retry) },
                )
            }

            else -> {
                // Title
                Text(
                    text = "Workout Complete",
                    style = typography.displaySmall,
                    color = colors.onSurfacePrimary,
                )

                Spacer(modifier = Modifier.height(spacing.space5))

                // Summary stats row
                SummaryStatsRow(
                    durationText = state.durationText,
                    exerciseCount = state.exerciseCount,
                    totalWorkingSets = state.totalWorkingSets,
                )

                Spacer(modifier = Modifier.height(spacing.space4))

                // Tonnage
                TonnageRow(
                    tonnageKg = state.totalTonnageKg,
                    weightUnit = state.weightUnit,
                )

                Spacer(modifier = Modifier.height(spacing.space4))

                HorizontalDivider(color = colors.surfaceHighest, thickness = 1.dp)

                Spacer(modifier = Modifier.height(spacing.space4))

                // Per-group volume breakdown
                if (state.perGroupVolume.isNotEmpty()) {
                    Text(
                        text = "Volume by Group",
                        style = typography.headlineSmall,
                        color = colors.onSurfacePrimary,
                    )

                    Spacer(modifier = Modifier.height(spacing.space2))

                    state.perGroupVolume.forEach { group ->
                        GroupVolumeRow(
                            group = group,
                            weightUnit = state.weightUnit,
                        )
                        Spacer(modifier = Modifier.height(spacing.space1))
                    }

                    Spacer(modifier = Modifier.height(spacing.space3))
                }

                // PR highlights
                if (state.personalRecords.isNotEmpty()) {
                    HorizontalDivider(color = colors.surfaceHighest, thickness = 1.dp)

                    Spacer(modifier = Modifier.height(spacing.space4))

                    Text(
                        text = "Personal Records",
                        style = typography.headlineSmall,
                        color = PrGold,
                    )

                    Spacer(modifier = Modifier.height(spacing.space2))

                    state.personalRecords.forEach { pr ->
                        PersonalRecordRow(
                            pr = pr,
                            weightUnit = state.weightUnit,
                        )
                        Spacer(modifier = Modifier.height(spacing.space2))
                    }

                    Spacer(modifier = Modifier.height(spacing.space3))
                }

                HorizontalDivider(color = colors.surfaceHighest, thickness = 1.dp)

                Spacer(modifier = Modifier.height(spacing.space5))

                // Action buttons
                Button(
                    onClick = { onIntent(WorkoutSummaryIntent.Dismiss) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentPrimary,
                        contentColor = colors.surfaceLowest,
                    ),
                    shape = RoundedCornerShape(radius.md),
                ) {
                    Text(
                        text = "Done",
                        style = typography.labelLarge,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.space2))

                OutlinedButton(
                    onClick = { onIntent(WorkoutSummaryIntent.SaveAsTemplate) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(radius.md),
                ) {
                    Text(
                        text = "Save as Template",
                        style = typography.labelLarge,
                        color = colors.accentPrimary,
                    )
                }

                // Bottom padding for bottom sheet handle space
                Spacer(modifier = Modifier.height(spacing.space4))
            }
        }
    }
}

@Composable
private fun SummaryStatsRow(
    durationText: String,
    exerciseCount: Int,
    totalWorkingSets: Int,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        SummaryStatItem(value = durationText, label = "Duration")
        SummaryStatItem(value = "$exerciseCount", label = "Exercises")
        SummaryStatItem(value = "$totalWorkingSets", label = "Working Sets")
    }
}

@Composable
private fun SummaryStatItem(
    value: String,
    label: String,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = typography.headlineMedium,
            color = colors.onSurfacePrimary,
        )
        Text(
            text = label,
            style = typography.bodySmall,
            color = colors.onSurfaceTertiary,
        )
    }
}

@Composable
private fun TonnageRow(
    tonnageKg: Double,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val tonnageText = formatTonnage(tonnageKg, weightUnit)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceMedium,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Total Tonnage",
                style = typography.bodyMedium,
                color = colors.onSurfaceSecondary,
            )
            Text(
                text = tonnageText,
                style = typography.headlineMedium,
                color = colors.onSurfacePrimary,
            )
        }
    }
}

@Composable
private fun GroupVolumeRow(
    group: GroupVolumeUi,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val tonnageText = formatTonnage(group.tonnageKg, weightUnit)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = group.groupName,
            style = typography.bodyMedium,
            color = colors.onSurfacePrimary,
        )
        Text(
            text = "${group.workingSets} sets \u00B7 $tonnageText",
            style = typography.bodySmall,
            color = colors.onSurfaceSecondary,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun PersonalRecordRow(
    pr: PersonalRecordUi,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val weightText = formatPrWeight(pr.weightKg, weightUnit)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = PrGold.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.space3, vertical = spacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Personal record",
                modifier = Modifier.size(24.dp),
                tint = PrGold,
            )

            Spacer(modifier = Modifier.width(spacing.space2))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pr.exerciseName,
                    style = typography.bodyMedium,
                    color = colors.onSurfacePrimary,
                )
                Text(
                    text = "$weightText x ${pr.reps} reps",
                    style = typography.bodySmall,
                    color = PrGold,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatTonnage(tonnageKg: Double, unit: WeightUnit): String {
    val value = when (unit) {
        WeightUnit.KG -> tonnageKg
        WeightUnit.LBS -> tonnageKg * KG_TO_LBS
    }
    val suffix = unit.value
    return if (value >= 1000) {
        String.format(Locale.US, "%.1fk%s", value / 1000, suffix)
    } else {
        String.format(Locale.US, "%.0f%s", value, suffix)
    }
}

private fun formatPrWeight(weightKg: Double, unit: WeightUnit): String {
    val value = when (unit) {
        WeightUnit.KG -> weightKg
        WeightUnit.LBS -> weightKg * KG_TO_LBS
    }
    return String.format(Locale.US, "%.1f%s", value, unit.value)
}

private const val KG_TO_LBS = 2.20462

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Summary Sheet - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SummarySheetDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        WorkoutSummarySheetContent(
            state = WorkoutSummaryUiState(
                isLoading = false,
                durationText = "1h 12m",
                exerciseCount = 5,
                totalWorkingSets = 18,
                totalTonnageKg = 8500.0,
                perGroupVolume = listOf(
                    GroupVolumeUi("Chest", 12, 5400.0),
                    GroupVolumeUi("Shoulders", 6, 3100.0),
                ),
                personalRecords = listOf(
                    PersonalRecordUi("Barbell Bench Press", 100.0, 5, RecordType.MAX_WEIGHT),
                    PersonalRecordUi("Overhead Press", 60.0, 8, RecordType.MAX_WEIGHT),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview(
    name = "Summary Sheet No PRs - Dark",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun SummarySheetNoPrsPreview() {
    DeepRepsTheme(darkTheme = true) {
        WorkoutSummarySheetContent(
            state = WorkoutSummaryUiState(
                isLoading = false,
                durationText = "45m",
                exerciseCount = 3,
                totalWorkingSets = 9,
                totalTonnageKg = 3200.0,
                perGroupVolume = listOf(
                    GroupVolumeUi("Back", 9, 3200.0),
                ),
                personalRecords = emptyList(),
            ),
            onIntent = {},
        )
    }
}
