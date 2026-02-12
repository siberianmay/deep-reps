package com.deepreps.feature.progress

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.progress.components.ProgressChart
import com.deepreps.feature.progress.components.TimeRangeSelector
import java.util.Locale

/**
 * Exercise progress screen: weight progression chart for a single exercise.
 *
 * Design spec: design-system.md Section 4.10 / 3.5.
 * - Top app bar with exercise name + back arrow
 * - Time range selector
 * - ProgressChart component
 */
@Composable
fun ExerciseProgressScreen(
    onNavigateBack: () -> Unit,
    viewModel: ExerciseProgressViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ExerciseProgressContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExerciseProgressContent(
    state: ExerciseProgressUiState,
    onIntent: (ExerciseProgressIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = state.exerciseName.ifEmpty { "Exercise Progress" },
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

        // Time range selector
        TimeRangeSelector(
            selected = state.selectedTimeRange,
            onSelect = { onIntent(ExerciseProgressIntent.SelectTimeRange(it)) },
            modifier = Modifier.padding(horizontal = spacing.space4, vertical = spacing.space2),
        )

        Spacer(modifier = Modifier.height(spacing.space3))

        // Content
        when {
            state.isLoading -> {
                LoadingIndicator(message = "Loading chart data...")
            }

            state.errorType != null -> {
                ErrorState(
                    message = when (state.errorType) {
                        ExerciseProgressError.LoadFailed -> "Failed to load exercise history."
                    },
                    onRetry = { onIntent(ExerciseProgressIntent.Retry) },
                )
            }

            else -> {
                ProgressChart(
                    dataPoints = state.chartData,
                    title = "Weight Progression",
                    currentValue = formatWeight(state.currentBestKg, state.weightUnit),
                    peakValue = formatWeight(state.allTimeBestKg, state.weightUnit),
                    deltaText = computeDelta(state.chartData, state.weightUnit),
                    modifier = Modifier.padding(horizontal = spacing.space4),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatWeight(weightKg: Double?, unit: WeightUnit): String? {
    if (weightKg == null) return null
    val value = when (unit) {
        WeightUnit.KG -> weightKg
        WeightUnit.LBS -> weightKg * KG_TO_LBS
    }
    return String.format(Locale.US, "%.1f%s", value, unit.value)
}

/**
 * Computes the delta text between the first and last data point.
 * Returns null if fewer than 2 data points.
 */
private fun computeDelta(dataPoints: List<ChartDataPoint>, unit: WeightUnit): String? {
    if (dataPoints.size < 2) return null
    val first = dataPoints.first().weightKg
    val last = dataPoints.last().weightKg
    val deltaKg = last - first
    val deltaValue = when (unit) {
        WeightUnit.KG -> deltaKg
        WeightUnit.LBS -> deltaKg * KG_TO_LBS
    }
    val prefix = if (deltaValue >= 0) "+" else ""
    return String.format(Locale.US, "%s%.1f%s", prefix, deltaValue, unit.value)
}

private const val KG_TO_LBS = 2.20462

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Exercise Progress - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun ExerciseProgressDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        val now = System.currentTimeMillis()
        val oneWeekMs = 7 * 24 * 60 * 60 * 1000L
        ExerciseProgressContent(
            state = ExerciseProgressUiState(
                exerciseName = "Barbell Bench Press",
                chartData = listOf(
                    ChartDataPoint(now - 10 * oneWeekMs, 60.0),
                    ChartDataPoint(now - 8 * oneWeekMs, 62.5),
                    ChartDataPoint(now - 6 * oneWeekMs, 65.0),
                    ChartDataPoint(now - 4 * oneWeekMs, 67.5),
                    ChartDataPoint(now - 2 * oneWeekMs, 70.0, isPersonalRecord = true),
                ),
                selectedTimeRange = TimeRange.TWELVE_WEEKS,
                currentBestKg = 70.0,
                allTimeBestKg = 70.0,
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(
    name = "Exercise Progress Empty - Dark",
    showBackground = true,
    backgroundColor = 0xFF0A0A0F,
)
@Composable
private fun ExerciseProgressEmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ExerciseProgressContent(
            state = ExerciseProgressUiState(
                exerciseName = "Deadlift",
                chartData = emptyList(),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
