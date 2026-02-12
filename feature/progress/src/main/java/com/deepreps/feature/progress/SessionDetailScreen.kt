package com.deepreps.feature.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.core.ui.theme.PrGold
import java.util.Locale

/**
 * Read-only session detail screen showing a past workout session.
 *
 * Design spec: design-system.md Section 4.9.2.
 * - Top app bar with date + back arrow
 * - Summary row: duration, sets, volume
 * - Exercise cards with sets, weights, reps
 * - PR badges on personal record sets
 * - Notes section if present
 */
@Composable
fun SessionDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SessionDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SessionDetailContent(
        state = state,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SessionDetailContent(
    state: SessionDetailUiState,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = if (state.dateText.isNotEmpty()) state.dateText else "Session Detail",
                    style = typography.headlineMedium,
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate back",
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceLowest,
                titleContentColor = colors.onSurfacePrimary,
                navigationIconContentColor = colors.onSurfacePrimary,
            ),
        )

        when {
            state.isLoading -> {
                LoadingIndicator(message = "Loading session...")
            }

            state.errorType != null -> {
                ErrorState(
                    message = when (state.errorType) {
                        SessionDetailError.NotFound -> "Session not found."
                        SessionDetailError.LoadFailed -> "Failed to load session details."
                    },
                    onRetry = onNavigateBack,
                )
            }

            state.exercises.isEmpty() && !state.isLoading -> {
                EmptyState(
                    title = "No exercises",
                    message = "This session has no exercise data",
                )
            }

            else -> {
                SessionDetailBody(state = state)
            }
        }
    }
}

@Composable
private fun SessionDetailBody(state: SessionDetailUiState) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing
    val radius = DeepRepsTheme.radius

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spacing.space4),
    ) {
        Spacer(modifier = Modifier.height(spacing.space4))

        // Summary row
        SessionSummaryRow(
            durationText = state.durationText,
            totalSets = state.totalSets,
            totalVolumeKg = state.totalVolumeKg,
            weightUnit = state.weightUnit,
        )

        Spacer(modifier = Modifier.height(spacing.space5))

        // Session notes
        if (!state.notes.isNullOrBlank()) {
            Text(
                text = "Session Notes",
                style = typography.headlineSmall,
                color = colors.onSurfacePrimary,
            )
            Spacer(modifier = Modifier.height(spacing.space2))
            Text(
                text = state.notes,
                style = typography.bodyMedium,
                color = colors.onSurfaceSecondary,
            )
            Spacer(modifier = Modifier.height(spacing.space5))
        }

        // Exercise list
        Text(
            text = "Exercises",
            style = typography.headlineSmall,
            color = colors.onSurfacePrimary,
        )

        Spacer(modifier = Modifier.height(spacing.space3))

        state.exercises.forEachIndexed { index, exercise ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(spacing.space3))
            }
            SessionExerciseCard(
                exercise = exercise,
                weightUnit = state.weightUnit,
            )
        }

        // Bottom spacing for scroll overscroll
        Spacer(modifier = Modifier.height(spacing.space8))
    }
}

/**
 * Summary stats row: duration, sets, volume.
 */
@Composable
private fun SessionSummaryRow(
    durationText: String,
    totalSets: Int,
    totalVolumeKg: Double,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val volumeText = formatSessionVolume(totalVolumeKg, weightUnit)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.space4),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SummaryStatItem(label = "Duration", value = durationText)
            SummaryStatItem(label = "Sets", value = "$totalSets")
            SummaryStatItem(label = "Volume", value = volumeText)
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
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

/**
 * Exercise card showing the exercise name, notes, and set rows.
 */
@Composable
private fun SessionExerciseCard(
    exercise: SessionExerciseUi,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
    ) {
        Column(
            modifier = Modifier.padding(spacing.space4),
        ) {
            // Exercise name
            Text(
                text = exercise.exerciseName,
                style = typography.headlineLarge,
                color = colors.onSurfacePrimary,
            )

            // Exercise notes
            if (!exercise.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(spacing.space1))
                Text(
                    text = exercise.notes,
                    style = typography.bodySmall,
                    color = colors.onSurfaceTertiary,
                )
            }

            Spacer(modifier = Modifier.height(spacing.space3))

            // Set header row
            SetHeaderRow()

            Spacer(modifier = Modifier.height(spacing.space1))

            HorizontalDivider(
                color = colors.surfaceHighest,
                thickness = 1.dp,
            )

            Spacer(modifier = Modifier.height(spacing.space1))

            // Set rows
            exercise.sets.forEach { set ->
                SessionSetRow(set = set, weightUnit = weightUnit)
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun SetHeaderRow() {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Set",
            style = typography.labelMedium,
            color = colors.onSurfaceTertiary,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = "Type",
            style = typography.labelMedium,
            color = colors.onSurfaceTertiary,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = "Weight",
            style = typography.labelMedium,
            color = colors.onSurfaceTertiary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "Reps",
            style = typography.labelMedium,
            color = colors.onSurfaceTertiary,
            modifier = Modifier.width(48.dp),
        )
        // Spacer for PR icon column
        Spacer(modifier = Modifier.width(28.dp))
    }
}

@Composable
private fun SessionSetRow(
    set: SessionSetUi,
    weightUnit: WeightUnit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val weightText = formatSetWeight(set.weightKg, weightUnit)
    val repsText = set.reps?.toString() ?: "--"
    val typeLabel = when (set.type) {
        SetType.WARMUP -> "Warmup"
        SetType.WORKING -> "Working"
    }
    val typeColor = when (set.type) {
        SetType.WARMUP -> colors.onSurfaceTertiary
        SetType.WORKING -> colors.onSurfaceSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${set.setNumber}",
            style = typography.bodyMedium,
            color = colors.onSurfaceSecondary,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = typeLabel,
            style = typography.bodySmall,
            color = typeColor,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = weightText,
            style = typography.bodyMedium,
            color = colors.onSurfacePrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = repsText,
            style = typography.bodyMedium,
            color = colors.onSurfacePrimary,
            modifier = Modifier.width(48.dp),
        )
        if (set.isPersonalRecord) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Personal record",
                modifier = Modifier.size(20.dp),
                tint = PrGold,
            )
        } else {
            Spacer(modifier = Modifier.width(28.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatSessionVolume(volumeKg: Double, unit: WeightUnit): String {
    val value = when (unit) {
        WeightUnit.KG -> volumeKg
        WeightUnit.LBS -> volumeKg * KG_TO_LBS
    }
    val suffix = unit.value
    return if (value >= 1000) {
        String.format(Locale.US, "%.1fk%s", value / 1000, suffix)
    } else {
        String.format(Locale.US, "%.0f%s", value, suffix)
    }
}

private fun formatSetWeight(weightKg: Double?, unit: WeightUnit): String {
    if (weightKg == null) return "--"
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

@Preview(name = "Session Detail - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun SessionDetailDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        SessionDetailContent(
            state = SessionDetailUiState(
                dateText = "Feb 10, 2026",
                durationText = "1h 12m",
                totalVolumeKg = 8500.0,
                totalSets = 18,
                exercises = listOf(
                    SessionExerciseUi(
                        exerciseId = 1L,
                        exerciseName = "Barbell Bench Press",
                        sets = listOf(
                            SessionSetUi(1, SetType.WARMUP, 40.0, 12, false),
                            SessionSetUi(2, SetType.WORKING, 80.0, 8, false),
                            SessionSetUi(3, SetType.WORKING, 85.0, 6, true),
                            SessionSetUi(4, SetType.WORKING, 80.0, 8, false),
                        ),
                        notes = "Felt strong today",
                    ),
                    SessionExerciseUi(
                        exerciseId = 2L,
                        exerciseName = "Incline Dumbbell Press",
                        sets = listOf(
                            SessionSetUi(1, SetType.WORKING, 30.0, 10, false),
                            SessionSetUi(2, SetType.WORKING, 30.0, 10, false),
                            SessionSetUi(3, SetType.WORKING, 30.0, 8, false),
                        ),
                        notes = null,
                    ),
                ),
                notes = "Great chest session",
                isLoading = false,
            ),
            onNavigateBack = {},
        )
    }
}

@Preview(
    name = "Session Detail Loading - Dark",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun SessionDetailLoadingPreview() {
    DeepRepsTheme(darkTheme = true) {
        SessionDetailContent(
            state = SessionDetailUiState(),
            onNavigateBack = {},
        )
    }
}
