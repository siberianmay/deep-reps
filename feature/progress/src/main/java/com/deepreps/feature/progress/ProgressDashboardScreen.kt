package com.deepreps.feature.progress

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.model.enums.WeightUnit
import com.deepreps.core.ui.component.EmptyState
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.progress.components.TimeRangeSelector
import java.util.Locale

/**
 * Progress dashboard screen: session history and navigation to exercise progress.
 *
 * Design spec: design-system.md Section 4.9.
 * - Top app bar with "Progress" title
 * - Time range selector (segmented control)
 * - Recent workouts list grouped by date
 */
@Composable
fun ProgressDashboardScreen(
    onNavigateToSessionDetail: (sessionId: Long) -> Unit,
    onNavigateToExerciseProgress: (exerciseId: Long) -> Unit,
    viewModel: ProgressDashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is ProgressDashboardSideEffect.NavigateToSessionDetail -> {
                    onNavigateToSessionDetail(effect.sessionId)
                }
                is ProgressDashboardSideEffect.NavigateToExerciseProgress -> {
                    onNavigateToExerciseProgress(effect.exerciseId)
                }
            }
        }
    }

    ProgressDashboardContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ProgressDashboardContent(
    state: ProgressDashboardUiState,
    onIntent: (ProgressDashboardIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val spacing = DeepRepsTheme.spacing
    val typography = DeepRepsTheme.typography

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "Progress",
                    style = typography.headlineMedium,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.surfaceLowest,
                titleContentColor = colors.onSurfacePrimary,
            ),
        )

        // Time range selector
        TimeRangeSelector(
            selected = state.selectedTimeRange,
            onSelect = { onIntent(ProgressDashboardIntent.SelectTimeRange(it)) },
            modifier = Modifier.padding(horizontal = spacing.space4, vertical = spacing.space2),
        )

        // Content area
        when {
            state.isLoading -> {
                LoadingIndicator(message = "Loading progress...")
            }

            state.errorType != null -> {
                ErrorState(
                    message = when (state.errorType) {
                        ProgressDashboardError.LoadFailed -> "Failed to load workout history."
                    },
                    onRetry = { onIntent(ProgressDashboardIntent.Retry) },
                )
            }

            state.recentSessions.isEmpty() -> {
                EmptyState(
                    title = "No workouts yet",
                    message = "Complete your first workout to see it here",
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = spacing.space4,
                        vertical = spacing.space3,
                    ),
                    verticalArrangement = Arrangement.spacedBy(spacing.space2),
                ) {
                    // Section header
                    item {
                        Text(
                            text = "Recent Workouts",
                            style = typography.headlineSmall,
                            color = colors.onSurfacePrimary,
                            modifier = Modifier.padding(
                                top = spacing.space6,
                                bottom = spacing.space3,
                            ),
                        )
                    }

                    items(
                        items = state.recentSessions,
                        key = { it.sessionId },
                    ) { session ->
                        SessionHistoryItem(
                            session = session,
                            weightUnit = state.weightUnit,
                            onClick = {
                                onIntent(ProgressDashboardIntent.ViewSession(session.sessionId))
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * Session history list item.
 *
 * Design spec: Section 4.9.1
 * - 88dp height, surface-low background, radius-md
 * - Line 1: date + duration
 * - Line 2: muscle groups
 * - Line 3: set count + volume + chevron
 */
@Composable
private fun SessionHistoryItem(
    session: SessionSummaryUi,
    weightUnit: WeightUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val radius = DeepRepsTheme.radius
    val spacing = DeepRepsTheme.spacing

    val volumeText = formatVolume(session.totalVolumeKg, weightUnit)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(radius.md),
        color = colors.surfaceLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(spacing.space4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                // Line 1: Date + Duration
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = session.dateText,
                        style = typography.bodyMedium,
                        color = colors.onSurfacePrimary,
                    )
                    Text(
                        text = session.durationText,
                        style = typography.bodyMedium,
                        color = colors.onSurfaceSecondary,
                    )
                }

                Spacer(modifier = Modifier.height(spacing.space1))

                // Line 2: Muscle groups
                Text(
                    text = session.muscleGroupNames,
                    style = typography.bodySmall,
                    color = colors.onSurfaceSecondary,
                )

                Spacer(modifier = Modifier.height(spacing.space1))

                // Line 3: Sets + Volume
                Text(
                    text = "${session.setCount} sets \u00B7 $volumeText volume",
                    style = typography.bodySmall,
                    color = colors.onSurfaceSecondary,
                )
            }

            // Chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View session detail",
                modifier = Modifier.size(24.dp),
                tint = colors.onSurfaceTertiary,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun formatVolume(volumeKg: Double, unit: WeightUnit): String {
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

private const val KG_TO_LBS = 2.20462

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(name = "Dashboard - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun DashboardDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ProgressDashboardContent(
            state = ProgressDashboardUiState(
                recentSessions = listOf(
                    SessionSummaryUi(
                        sessionId = 1,
                        dateText = "Feb 10, 2026",
                        durationText = "1h 12m",
                        exerciseCount = 5,
                        totalVolumeKg = 8500.0,
                        muscleGroupNames = "Chest, Shoulders",
                        setCount = 18,
                    ),
                    SessionSummaryUi(
                        sessionId = 2,
                        dateText = "Feb 8, 2026",
                        durationText = "55m",
                        exerciseCount = 4,
                        totalVolumeKg = 6200.0,
                        muscleGroupNames = "Back, Arms",
                        setCount = 16,
                    ),
                ),
                isLoading = false,
                selectedTimeRange = TimeRange.TWELVE_WEEKS,
            ),
            onIntent = {},
        )
    }
}

@Preview(name = "Dashboard Empty - Dark", showBackground = true, backgroundColor = 0xFF0A0A0F)
@Composable
private fun DashboardEmptyDarkPreview() {
    DeepRepsTheme(darkTheme = true) {
        ProgressDashboardContent(
            state = ProgressDashboardUiState(
                recentSessions = emptyList(),
                isLoading = false,
            ),
            onIntent = {},
        )
    }
}
