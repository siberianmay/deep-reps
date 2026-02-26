package com.deepreps.feature.aiplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.util.Locale

/**
 * Plan Review screen.
 *
 * Displays the generated plan with editable weights/reps per exercise.
 * Shows which fallback level was used (AI/cached/baseline).
 * User can modify any weight/reps before starting the workout.
 */
@Suppress("LongMethod")
@Composable
fun PlanReviewScreen(
    exerciseIds: List<Long>,
    onNavigateToWorkout: (sessionId: Long) -> Unit,
    onBack: () -> Unit,
    viewModel: PlanReviewViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(exerciseIds) {
        viewModel.onIntent(PlanReviewIntent.LoadPlan(exerciseIds))
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is PlanReviewSideEffect.NavigateToWorkout -> {
                    onNavigateToWorkout(effect.sessionId)
                }
                is PlanReviewSideEffect.ShowError -> {
                    // Phase 2: show a Snackbar via SnackbarHostState
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (state.phase) {
            PlanReviewUiState.Phase.Loading,
            PlanReviewUiState.Phase.Generating -> LoadingContent()
            PlanReviewUiState.Phase.Error -> ErrorContent(
                onRetry = {
                    viewModel.onIntent(PlanReviewIntent.RegeneratePlan)
                },
                onBack = onBack,
            )
            PlanReviewUiState.Phase.PlanReady -> PlanReadyContent(
                state = state,
                onIntent = viewModel::onIntent,
            )
        }
    }

    if (state.showSafetyWarnings && state.safetyViolations.isNotEmpty()) {
        SafetyWarningsDialog(
            violations = state.safetyViolations.map { it.message },
            onDismiss = {
                viewModel.onIntent(PlanReviewIntent.DismissSafetyWarnings)
            },
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Generating your plan...",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Failed to generate plan",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                TextButton(onClick = onBack) {
                    Text("Go Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Suppress("LongMethod")
@Composable
private fun PlanReadyContent(
    state: PlanReviewUiState,
    onIntent: (PlanReviewIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        PlanSourceBanner(source = state.planSource)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            itemsIndexed(
                items = state.exercisePlans,
                key = { _, plan -> plan.exerciseId },
            ) { exerciseIndex, exercisePlan ->
                ExercisePlanCard(
                    exercisePlan = exercisePlan,
                    exerciseIndex = exerciseIndex,
                    onWeightChange = { setIndex, weight ->
                        onIntent(
                            PlanReviewIntent.UpdateWeight(
                                exerciseIndex,
                                setIndex,
                                weight,
                            ),
                        )
                    },
                    onRepsChange = { setIndex, reps ->
                        onIntent(
                            PlanReviewIntent.UpdateReps(
                                exerciseIndex,
                                setIndex,
                                reps,
                            ),
                        )
                    },
                    onAddWorkingSet = {
                        onIntent(
                            PlanReviewIntent.AddWorkingSet(exerciseIndex),
                        )
                    },
                    onRemoveLastWorkingSet = {
                        onIntent(
                            PlanReviewIntent.RemoveLastWorkingSet(
                                exerciseIndex,
                            ),
                        )
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        PlanBottomBar(onIntent = onIntent)
    }
}

@Composable
private fun PlanBottomBar(
    onIntent: (PlanReviewIntent) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                onClick = { onIntent(PlanReviewIntent.RegeneratePlan) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Regenerate")
            }
            Button(
                onClick = { onIntent(PlanReviewIntent.ConfirmPlan) },
                modifier = Modifier.weight(1f),
            ) {
                Text("Start Workout")
            }
        }
    }
}

@Composable
private fun PlanSourceBanner(source: PlanReviewUiState.PlanSource) {
    val (text, color) = when (source) {
        PlanReviewUiState.PlanSource.AI_GENERATED ->
            "AI Generated" to MaterialTheme.colorScheme.primary
        PlanReviewUiState.PlanSource.CACHED ->
            "Cached Plan" to MaterialTheme.colorScheme.tertiary
        PlanReviewUiState.PlanSource.BASELINE ->
            "Offline Baseline" to MaterialTheme.colorScheme.secondary
        PlanReviewUiState.PlanSource.MANUAL ->
            "Manual Entry" to MaterialTheme.colorScheme.outline
        PlanReviewUiState.PlanSource.LOADING ->
            "Loading..." to MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun ExercisePlanCard(
    exercisePlan: EditableExercisePlan,
    @Suppress("UnusedParameter") exerciseIndex: Int,
    onWeightChange: (setIndex: Int, weight: Double) -> Unit,
    onRepsChange: (setIndex: Int, reps: Int) -> Unit,
    onAddWorkingSet: () -> Unit,
    onRemoveLastWorkingSet: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = exercisePlan.exerciseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            if (exercisePlan.notes != null) {
                Text(
                    text = exercisePlan.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Text(
                text = "Rest: ${exercisePlan.restSeconds}s",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            SetHeaderRow()

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
            )

            exercisePlan.sets.forEachIndexed { setIndex, set ->
                SetRow(
                    set = set,
                    setIndex = setIndex,
                    onWeightChange = { onWeightChange(setIndex, it) },
                    onRepsChange = { onRepsChange(setIndex, it) },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val workingSetCount = exercisePlan.sets.count {
                it.setType == "working"
            }
            WorkingSetStepper(
                count = workingSetCount,
                onIncrement = onAddWorkingSet,
                onDecrement = onRemoveLastWorkingSet,
                minCount = MIN_WORKING_SETS,
                maxCount = MAX_WORKING_SETS,
            )
        }
    }
}

private const val MIN_WORKING_SETS = 2
private const val MAX_WORKING_SETS = 6

@Suppress("LongMethod")
@Composable
private fun WorkingSetStepper(
    count: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    minCount: Int,
    maxCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Working sets:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(12.dp))

        IconButton(
            onClick = onDecrement,
            enabled = count > minCount,
            modifier = Modifier.size(40.dp),
        ) {
            Text(
                text = "\u2212",
                style = MaterialTheme.typography.titleLarge,
                color = if (count > minCount) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }

        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.Center,
        )

        IconButton(
            onClick = onIncrement,
            enabled = count < maxCount,
            modifier = Modifier.size(40.dp),
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleLarge,
                color = if (count < maxCount) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
    }
}

@Composable
private fun SetHeaderRow() {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Set",
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "Type",
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "Weight (kg)",
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = "Reps",
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun SetRow(
    set: EditableSet,
    setIndex: Int,
    @Suppress("UnusedParameter") onWeightChange: (Double) -> Unit,
    @Suppress("UnusedParameter") onRepsChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${setIndex + 1}",
            modifier = Modifier.weight(0.15f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = set.setType.replaceFirstChar { it.uppercase() },
            modifier = Modifier.weight(0.2f),
            style = MaterialTheme.typography.bodySmall,
            color = if (set.setType == "warmup") {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )
        Text(
            text = formatWeight(set.weight),
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = "${set.reps}",
            modifier = Modifier.weight(0.3f),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun formatWeight(weight: Double): String =
    if (weight > 0) String.format(Locale.US, "%.1f", weight) else "BW"

@Composable
private fun SafetyWarningsDialog(
    violations: List<String>,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Safety Warnings") },
        text = {
            Column {
                violations.forEach { violation ->
                    Text(
                        text = violation,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Understood")
            }
        },
    )
}
