package com.deepreps.feature.aiplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Plan Review screen.
 *
 * Displays the generated plan with editable weights/reps per exercise.
 * Shows which fallback level was used (AI/cached/baseline).
 * User can modify any weight/reps before starting the workout.
 */
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
                is PlanReviewSideEffect.NavigateToWorkout -> onNavigateToWorkout(effect.sessionId)
                is PlanReviewSideEffect.ShowError -> {
                    // In a real app, show a Snackbar via SnackbarHostState
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        when (state.phase) {
            PlanReviewUiState.Phase.Loading,
            PlanReviewUiState.Phase.Generating -> LoadingContent()
            PlanReviewUiState.Phase.Error -> ErrorContent(
                onRetry = { viewModel.onIntent(PlanReviewIntent.RegeneratePlan) },
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
            onDismiss = { viewModel.onIntent(PlanReviewIntent.DismissSafetyWarnings) },
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

@Composable
private fun PlanReadyContent(
    state: PlanReviewUiState,
    onIntent: (PlanReviewIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Plan source indicator
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
                        onIntent(PlanReviewIntent.UpdateWeight(exerciseIndex, setIndex, weight))
                    },
                    onRepsChange = { setIndex, reps ->
                        onIntent(PlanReviewIntent.UpdateReps(exerciseIndex, setIndex, reps))
                    },
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom bar with Start Workout button
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
                androidx.compose.material3.Button(
                    onClick = { onIntent(PlanReviewIntent.ConfirmPlan) },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Start Workout")
                }
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
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = color,
        )
    }
}

@Composable
private fun ExercisePlanCard(
    exercisePlan: EditableExercisePlan,
    exerciseIndex: Int,
    onWeightChange: (setIndex: Int, weight: Double) -> Unit,
    onRepsChange: (setIndex: Int, reps: Int) -> Unit,
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

            // Header row
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            exercisePlan.sets.forEachIndexed { setIndex, set ->
                SetRow(
                    set = set,
                    setIndex = setIndex,
                    onWeightChange = { onWeightChange(setIndex, it) },
                    onRepsChange = { onRepsChange(setIndex, it) },
                )
            }
        }
    }
}

@Composable
private fun SetRow(
    set: EditableSet,
    setIndex: Int,
    onWeightChange: (Double) -> Unit,
    onRepsChange: (Int) -> Unit,
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
            text = if (set.weight > 0) String.format("%.1f", set.weight) else "BW",
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
