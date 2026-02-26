package com.deepreps.feature.workout.active

import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.ui.component.ErrorState
import com.deepreps.core.ui.component.LoadingIndicator
import com.deepreps.core.ui.component.NumberInputSheet
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.workout.active.components.ExerciseCard
import com.deepreps.feature.workout.active.components.ExerciseInfoSheet
import com.deepreps.feature.workout.active.components.PausedOverlay
import com.deepreps.feature.workout.active.components.RestTimerBottomSheet

/**
 * Active workout screen -- the most important screen in the app.
 *
 * Design spec: design-system.md Section 4.7.
 * - Sticky header: elapsed time, pause button, end workout button
 * - LazyColumn of ExerciseCards
 * - Rest timer bottom sheet when active
 * - Paused overlay when paused
 * - KEEP_SCREEN_ON flag while active
 *
 * @param viewModel The workout ViewModel (injected via hiltViewModel at the NavGraph level).
 * @param onNavigateToSummary Callback to navigate to workout summary after completion.
 * @param onNavigateBack Callback for back navigation.
 */
@Suppress("LongMethod")
@Composable
fun WorkoutScreen(
    viewModel: WorkoutViewModel,
    onNavigateToSummary: (Long) -> Unit,
    @Suppress("UnusedParameter") onNavigateBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // KEEP_SCREEN_ON per design-system.md 4.7
    val view = LocalView.current
    DisposableEffect(Unit) {
        val window = (view.context as? android.app.Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Collect side effects
    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is WorkoutSideEffect.NavigateToSummary -> onNavigateToSummary(effect.sessionId)
                is WorkoutSideEffect.Vibrate -> { /* Vibration handled by RestTimerManager */ }
                is WorkoutSideEffect.ShowError -> { /* Phase 2: Show Snackbar */ }
                is WorkoutSideEffect.ScrollToExercise -> {
                    listState.animateScrollToItem(effect.exerciseIndex)
                }
            }
        }
    }

    when (val phase = state.phase) {
        is WorkoutPhaseUi.Loading -> {
            LoadingIndicator()
        }

        is WorkoutPhaseUi.Error -> {
            ErrorState(
                message = phase.message,
                onRetry = { /* Could reload */ },
            )
        }

        is WorkoutPhaseUi.Active, is WorkoutPhaseUi.Paused -> {
            ActiveWorkoutContent(
                state = state,
                listState = listState,
                onIntent = viewModel::onIntent,
            )
        }

        is WorkoutPhaseUi.Completed -> {
            // Navigation side effect handles this; show loading in the meantime
            LoadingIndicator()
        }
    }
}

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActiveWorkoutContent(
    state: WorkoutUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onIntent: (WorkoutIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography
    val spacing = DeepRepsTheme.spacing

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.surfaceLowest),
        ) {
            // --- Sticky Header (56dp) ---
            StickyHeader(
                elapsedSeconds = state.elapsedSeconds,
                isPaused = state.isPaused,
                onPause = { onIntent(WorkoutIntent.PauseWorkout) },
                onEndWorkout = { onIntent(WorkoutIntent.RequestFinishWorkout) },
            )

            // --- Exercise List ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = spacing.space2),
            ) {
                itemsIndexed(
                    items = state.exercises,
                    key = { _, exercise -> exercise.id },
                ) { index, exercise ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    ExerciseCard(
                        exercise = exercise,
                        isCurrentExercise = index == state.activeExerciseIndex,
                        onToggleExpand = {
                            onIntent(WorkoutIntent.ToggleExerciseExpanded(exercise.id))
                        },
                        onSetDone = { set ->
                            onIntent(
                                WorkoutIntent.CompleteSet(
                                    workoutExerciseId = exercise.id,
                                    setId = set.id,
                                    setIndex = set.setNumber,
                                    weight = set.actualWeightKg
                                        ?: set.plannedWeightKg
                                        ?: 0.0,
                                    reps = set.actualReps
                                        ?: set.plannedReps
                                        ?: 0,
                                ),
                            )
                        },
                        onWeightFieldClick = { set ->
                            onIntent(
                                WorkoutIntent.OpenWeightSheet(
                                    workoutExerciseId = exercise.id,
                                    setId = set.id,
                                    currentWeight = set.actualWeightKg
                                        ?: set.plannedWeightKg
                                        ?: 0.0,
                                    step = 2.5,
                                ),
                            )
                        },
                        onRepsFieldClick = { set ->
                            onIntent(
                                WorkoutIntent.OpenRepsSheet(
                                    workoutExerciseId = exercise.id,
                                    setId = set.id,
                                    currentReps = set.actualReps
                                        ?: set.plannedReps
                                        ?: 0,
                                ),
                            )
                        },
                        onSkipSet = { set ->
                            onIntent(
                                WorkoutIntent.SkipSet(
                                    setId = set.id,
                                    workoutExerciseId = exercise.id,
                                ),
                            )
                        },
                        onUnskipSet = { set ->
                            onIntent(
                                WorkoutIntent.UnskipSet(
                                    setId = set.id,
                                    workoutExerciseId = exercise.id,
                                ),
                            )
                        },
                        onDeleteSet = { set ->
                            onIntent(
                                WorkoutIntent.DeleteSet(
                                    setId = set.id,
                                    workoutExerciseId = exercise.id,
                                ),
                            )
                        },
                        onAddSet = {
                            onIntent(WorkoutIntent.AddSet(exercise.id))
                        },
                        isNotesExpanded = exercise.id in state.notesExpandedExerciseIds,
                        onToggleNotes = {
                            onIntent(WorkoutIntent.ToggleNotes(exercise.id))
                        },
                        onNotesChanged = { text ->
                            onIntent(WorkoutIntent.UpdateNotes(exercise.id, text))
                        },
                        onInfoClick = {
                            onIntent(WorkoutIntent.ShowExerciseInfo(exercise.exerciseId))
                        },
                    )
                }

                // Bottom padding so the last card is not hidden behind rest timer
                item {
                    Spacer(modifier = Modifier.height(if (state.activeRestTimer != null) 296.dp else 16.dp))
                }
            }
        }

        // --- Rest Timer Bottom Sheet ---
        AnimatedVisibility(
            visible = state.activeRestTimer != null && state.activeRestTimer.isActive,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            state.activeRestTimer?.let { timerState ->
                RestTimerBottomSheet(
                    timerState = timerState,
                    onSkip = { onIntent(WorkoutIntent.SkipRestTimer) },
                    onExtend = { onIntent(WorkoutIntent.ExtendRestTimer) },
                )
            }
        }

        // --- Paused Overlay ---
        if (state.isPaused) {
            PausedOverlay(
                pausedDurationText = "Paused",
                onResume = { onIntent(WorkoutIntent.ResumeWorkout) },
                onEndWorkout = { onIntent(WorkoutIntent.RequestFinishWorkout) },
            )
        }

        // --- Finish Confirmation Dialog ---
        if (state.showFinishDialog) {
            FinishWorkoutDialog(
                onConfirm = { onIntent(WorkoutIntent.ConfirmFinishWorkout) },
                onDismiss = { onIntent(WorkoutIntent.DismissFinishDialog) },
            )
        }

        // --- Number Input Bottom Sheet ---
        state.activeInputSheet?.let { sheetState ->
            when (sheetState) {
                is InputSheetState.Weight -> {
                    NumberInputSheet(
                        title = "Weight",
                        value = sheetState.currentValue,
                        step = sheetState.step,
                        minValue = 0.0,
                        maxValue = 500.0,
                        isDecimal = true,
                        unitLabel = "kg",
                        onConfirm = { newWeight ->
                            onIntent(
                                WorkoutIntent.UpdateSetWeight(
                                    workoutExerciseId = sheetState.workoutExerciseId,
                                    setId = sheetState.setId,
                                    weight = newWeight,
                                ),
                            )
                            onIntent(WorkoutIntent.CloseInputSheet)
                        },
                        onDismiss = { onIntent(WorkoutIntent.CloseInputSheet) },
                    )
                }

                is InputSheetState.Reps -> {
                    NumberInputSheet(
                        title = "Reps",
                        value = sheetState.currentValue.toDouble(),
                        step = 1.0,
                        minValue = 1.0,
                        maxValue = 50.0,
                        isDecimal = false,
                        onConfirm = { newReps ->
                            onIntent(
                                WorkoutIntent.UpdateSetReps(
                                    workoutExerciseId = sheetState.workoutExerciseId,
                                    setId = sheetState.setId,
                                    reps = newReps.toInt(),
                                ),
                            )
                            onIntent(WorkoutIntent.CloseInputSheet)
                        },
                        onDismiss = { onIntent(WorkoutIntent.CloseInputSheet) },
                    )
                }
            }
        }

        // --- Exercise Info Bottom Sheet ---
        state.exerciseInfoData?.let { exercise ->
            ExerciseInfoSheet(
                exercise = exercise,
                onDismiss = { onIntent(WorkoutIntent.DismissExerciseInfo) },
            )
        }
    }
}

/**
 * Sticky header: 56dp, surface-lowest background.
 * - [Pause] icon button: left-aligned
 * - Workout timer: center, headline-medium
 * - [End Workout] text button: right-aligned, status-error
 */
@Suppress("LongMethod")
@Composable
private fun StickyHeader(
    elapsedSeconds: Long,
    isPaused: Boolean,
    onPause: () -> Unit,
    onEndWorkout: () -> Unit,
) {
    val colors = DeepRepsTheme.colors
    val typography = DeepRepsTheme.typography

    val timerText = remember(elapsedSeconds) {
        formatElapsedTime(elapsedSeconds)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.surfaceLowest)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Pause button
        IconButton(
            onClick = onPause,
            enabled = !isPaused,
        ) {
            Icon(
                imageVector = Icons.Filled.Pause,
                contentDescription = "Pause workout",
                tint = colors.onSurfaceSecondary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Elapsed time
        Text(
            text = timerText,
            style = typography.headlineMedium,
            color = colors.onSurfacePrimary,
            modifier = Modifier.semantics {
                contentDescription = "Workout duration: $timerText"
            },
        )

        Spacer(modifier = Modifier.weight(1f))

        // End Workout
        TextButton(onClick = onEndWorkout) {
            Text(
                text = "End",
                style = typography.labelLarge,
                color = colors.statusError,
            )
        }
    }
}

@Composable
private fun FinishWorkoutDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "End Workout?")
        },
        text = {
            Text(text = "Are you sure you want to end this workout? Your progress has been saved.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "End Workout",
                    color = DeepRepsTheme.colors.statusError,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Continue")
            }
        },
    )
}

// --- Helpers ---

/**
 * Formats elapsed seconds as MM:SS or H:MM:SS.
 * Uses SystemClock.elapsedRealtime()-based seconds, excluding paused duration.
 */
internal fun formatElapsedTime(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
