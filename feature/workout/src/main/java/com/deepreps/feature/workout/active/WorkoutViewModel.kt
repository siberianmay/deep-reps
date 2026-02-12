package com.deepreps.feature.workout.active

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.data.timer.RestTimerManager
import com.deepreps.core.data.timer.RestTimerState
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.statemachine.WorkoutEvent
import com.deepreps.core.domain.statemachine.WorkoutPhase
import com.deepreps.core.domain.statemachine.WorkoutStateMachine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the active workout screen (MVI pattern).
 *
 * Key invariants:
 * - Room is the source of truth. Every set completion writes to Room immediately.
 * - State survives process death via [SavedStateHandle] for the sessionId.
 * - Rest timer is managed by the singleton [RestTimerManager].
 * - The [WorkoutStateMachine] governs valid phase transitions.
 * - Elapsed time is calculated from SystemClock.elapsedRealtime() anchored at
 *   session start, minus accumulated paused duration.
 *
 * Analytics events (P0, per analytics-plan.md Section 1.5):
 * - workout_started: on successful session load
 * - set_completed: on each set completion
 * - workout_completed: on confirmed finish
 * - workout_abandoned: (not triggered from here -- triggered from session recovery)
 * - workout_resumed: on resume after pause
 */
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val restTimerManager: RestTimerManager,
    private val stateMachine: WorkoutStateMachine,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<WorkoutSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<WorkoutSideEffect> = _sideEffect.receiveAsFlow()

    /** The state machine phase, tracked in-memory. */
    private var currentPhase: WorkoutPhase = WorkoutPhase.Active()

    /** Elapsed time ticker job. */
    private var tickerJob: Job? = null

    /** elapsedRealtime() at which the session was started (or resumed after process death). */
    private var elapsedRealtimeAtStart: Long = 0L

    /** Accumulated paused duration in seconds (from the session entity). */
    private var accumulatedPauseSeconds: Long = 0L

    /** elapsedRealtime() at which the current pause started. */
    private var pauseStartElapsedRealtime: Long = 0L

    companion object {
        const val SESSION_ID_KEY = "session_id"
        const val NOTES_MAX_LENGTH = 1000
        const val NOTES_SAVE_DEBOUNCE_MS = 500L

        // Analytics event names per analytics-plan.md taxonomy
        private const val EVENT_WORKOUT_STARTED = "workout_started"
        private const val EVENT_SET_COMPLETED = "set_completed"
        private const val EVENT_WORKOUT_COMPLETED = "workout_completed"
        private const val EVENT_WORKOUT_RESUMED = "workout_resumed"
    }

    init {
        loadSession()
        observeRestTimer()
    }

    // ----- Public API -----

    fun onIntent(intent: WorkoutIntent) {
        when (intent) {
            is WorkoutIntent.CompleteSet -> handleCompleteSet(intent)
            is WorkoutIntent.UpdateSetWeight -> handleUpdateSetWeight(intent)
            is WorkoutIntent.UpdateSetReps -> handleUpdateSetReps(intent)
            is WorkoutIntent.AddSet -> handleAddSet(intent)
            is WorkoutIntent.DeleteSet -> handleDeleteSet(intent)
            is WorkoutIntent.SkipRestTimer -> handleSkipRestTimer()
            is WorkoutIntent.ExtendRestTimer -> handleExtendRestTimer()
            is WorkoutIntent.PauseWorkout -> handlePauseWorkout()
            is WorkoutIntent.ResumeWorkout -> handleResumeWorkout()
            is WorkoutIntent.RequestFinishWorkout -> handleRequestFinish()
            is WorkoutIntent.ConfirmFinishWorkout -> handleConfirmFinish()
            is WorkoutIntent.DismissFinishDialog -> handleDismissFinishDialog()
            is WorkoutIntent.ToggleExerciseExpanded -> handleToggleExpanded(intent)
            is WorkoutIntent.ToggleNotes -> handleToggleNotes(intent)
            is WorkoutIntent.UpdateNotes -> handleUpdateNotes(intent)
        }
    }

    // ----- Session Loading -----

    private fun loadSession() {
        viewModelScope.launch {
            try {
                val sessionId = savedStateHandle.get<Long>(SESSION_ID_KEY) ?: run {
                    // Try to find an active session
                    val active = workoutSessionRepository.getActiveSession()
                    active?.id ?: run {
                        _state.update {
                            it.copy(phase = WorkoutPhaseUi.Error("No active workout session found."))
                        }
                        return@launch
                    }
                }

                savedStateHandle[SESSION_ID_KEY] = sessionId

                val session = workoutSessionRepository.getSession(sessionId)
                if (session == null) {
                    _state.update {
                        it.copy(phase = WorkoutPhaseUi.Error("Workout session not found."))
                    }
                    return@launch
                }

                accumulatedPauseSeconds = session.pausedDurationSeconds

                // Load exercises and their sets
                val workoutExercises = workoutSessionRepository
                    .getExercisesForSession(sessionId)
                    .first()

                val exerciseUiList = buildExerciseUiList(workoutExercises)

                // Determine current phase from session status
                val isPaused = session.status == SessionStatus.PAUSED
                val phase = if (isPaused) WorkoutPhaseUi.Paused else WorkoutPhaseUi.Active
                currentPhase = if (isPaused) {
                    WorkoutPhase.Paused(
                        pausedAtMillis = System.currentTimeMillis(),
                        startedAtMillis = session.startedAt,
                        accumulatedPauseSeconds = accumulatedPauseSeconds,
                    )
                } else {
                    WorkoutPhase.Active(
                        startedAtMillis = session.startedAt,
                        accumulatedPauseSeconds = accumulatedPauseSeconds,
                    )
                }

                // Anchor the elapsed time calculation
                val elapsedSoFar = (System.currentTimeMillis() - session.startedAt) / 1000 - accumulatedPauseSeconds
                elapsedRealtimeAtStart = SystemClock.elapsedRealtime() - (elapsedSoFar * 1000)

                _state.update {
                    it.copy(
                        phase = phase,
                        exercises = exerciseUiList,
                        isPaused = isPaused,
                        sessionId = sessionId,
                        elapsedSeconds = elapsedSoFar.coerceAtLeast(0),
                    )
                }

                if (!isPaused) {
                    startTicker()
                }

                // Track workout_started analytics
                // Note: session.planSource is not yet on WorkoutSession model.
                // Uses "unknown" until the domain model is extended with plan_source tracking.
                val muscleGroups = exerciseUiList.map { it.equipment }.distinct()
                analyticsTracker.trackUserAction(
                    EVENT_WORKOUT_STARTED,
                    mapOf(
                        "exercise_count" to exerciseUiList.size,
                        "muscle_groups" to muscleGroups.joinToString(","),
                        "plan_source" to "unknown",
                    ),
                )

                // Auto-scroll to active exercise
                val activeIdx = _state.value.activeExerciseIndex
                if (activeIdx >= 0) {
                    _sideEffect.trySend(WorkoutSideEffect.ScrollToExercise(activeIdx))
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(phase = WorkoutPhaseUi.Error("Failed to load workout: ${e.message}"))
                }
            }
        }
    }

    private suspend fun buildExerciseUiList(
        workoutExercises: List<WorkoutExercise>,
    ): List<WorkoutExerciseUi> {
        return workoutExercises.map { we ->
            val exercise = exerciseRepository.getExerciseById(we.exerciseId)
            val sets = workoutSessionRepository
                .getSetsForExercise(we.id)
                .first()

            // Determine which sets should be IN_PROGRESS
            val setsWithStatus = markInProgressSet(sets)

            // Active exercise (first with incomplete sets) is expanded
            val hasIncomplete = setsWithStatus.any {
                it.status != SetStatus.COMPLETED && it.status != SetStatus.SKIPPED
            }

            WorkoutExerciseUi(
                id = we.id,
                exerciseId = we.exerciseId,
                name = exercise?.name ?: "Unknown Exercise",
                equipment = exercise?.equipment?.value ?: "",
                sets = setsWithStatus,
                orderIndex = we.orderIndex,
                isExpanded = hasIncomplete,
                notes = we.notes,
                restTimerSeconds = we.restTimerSeconds ?: 120,
            )
        }
    }

    /**
     * Marks the first PLANNED set as IN_PROGRESS for UI highlighting.
     * The domain model stores PLANNED for all non-completed sets;
     * the IN_PROGRESS visual state is a UI concern determined here.
     */
    private fun markInProgressSet(sets: List<WorkoutSet>): List<WorkoutSet> {
        var foundFirst = false
        return sets.map { set ->
            if (!foundFirst && set.status == SetStatus.PLANNED) {
                foundFirst = true
                set.copy(status = SetStatus.IN_PROGRESS)
            } else {
                set
            }
        }
    }

    // ----- Rest Timer Observation -----

    private fun observeRestTimer() {
        viewModelScope.launch {
            restTimerManager.state.collect { timerState ->
                _state.update { current ->
                    current.copy(
                        activeRestTimer = if (timerState == RestTimerState.IDLE) null else timerState,
                    )
                }
            }
        }
    }

    // ----- Elapsed Time Ticker -----

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                val elapsedMillis = SystemClock.elapsedRealtime() - elapsedRealtimeAtStart
                val elapsedSec = (elapsedMillis / 1000) - accumulatedPauseSeconds
                _state.update { it.copy(elapsedSeconds = elapsedSec.coerceAtLeast(0)) }
                delay(1_000L)
            }
        }
    }

    private fun stopTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    // ----- Intent Handlers -----

    private fun handleCompleteSet(intent: WorkoutIntent.CompleteSet) {
        viewModelScope.launch {
            // CRITICAL: Write to Room immediately (defensive persistence)
            workoutSessionRepository.completeSet(
                workoutExerciseId = intent.workoutExerciseId,
                setIndex = intent.setIndex,
                weight = intent.weight,
                reps = intent.reps,
            )

            // Track set_completed analytics
            val exerciseUi = _state.value.exercises.find { it.id == intent.workoutExerciseId }
            val setUi = exerciseUi?.sets?.find { it.id == intent.setId }
            analyticsTracker.trackUserAction(
                EVENT_SET_COMPLETED,
                mapOf(
                    "exercise_name" to (exerciseUi?.name ?: "unknown"),
                    "set_number" to (intent.setIndex + 1),
                    "set_type" to (setUi?.type?.value ?: "working"),
                    "weight" to intent.weight,
                    "reps" to intent.reps,
                    "unit" to "kg",
                ),
            )

            // Update in-memory state immutably
            _state.update { current ->
                val updatedExercises = current.exercises.map { exercise ->
                    if (exercise.id == intent.workoutExerciseId) {
                        val updatedSets = exercise.sets.map { set ->
                            if (set.id == intent.setId) {
                                set.copy(
                                    status = SetStatus.COMPLETED,
                                    actualWeightKg = intent.weight,
                                    actualReps = intent.reps,
                                    completedAt = System.currentTimeMillis(),
                                )
                            } else {
                                set
                            }
                        }
                        exercise.copy(sets = markInProgressSet(updatedSets))
                    } else {
                        exercise
                    }
                }
                current.copy(exercises = updatedExercises)
            }

            _sideEffect.trySend(WorkoutSideEffect.Vibrate)

            // Determine if we should start the rest timer
            val exerciseAfterUpdate = _state.value.exercises.find { it.id == intent.workoutExerciseId }
            val hasMoreSets = exerciseAfterUpdate?.sets?.any {
                it.status == SetStatus.PLANNED || it.status == SetStatus.IN_PROGRESS
            } ?: false

            if (hasMoreSets) {
                restTimerManager.start(exerciseAfterUpdate?.restTimerSeconds ?: 120)
            } else {
                // All sets of this exercise are done -- auto-advance
                val nextIdx = _state.value.activeExerciseIndex
                if (nextIdx >= 0) {
                    // Expand next exercise, collapse completed
                    _state.update { current ->
                        val updated = current.exercises.mapIndexed { index, ex ->
                            ex.copy(isExpanded = index == nextIdx)
                        }
                        current.copy(exercises = updated)
                    }
                    _sideEffect.trySend(WorkoutSideEffect.ScrollToExercise(nextIdx))
                    // Start rest timer before next exercise
                    val nextExercise = _state.value.exercises.getOrNull(nextIdx)
                    if (nextExercise != null) {
                        restTimerManager.start(nextExercise.restTimerSeconds)
                    }
                }
                // If no more exercises, the user can tap Finish
            }
        }
    }

    private fun handleUpdateSetWeight(intent: WorkoutIntent.UpdateSetWeight) {
        _state.update { current ->
            val updatedExercises = current.exercises.map { exercise ->
                if (exercise.id == intent.workoutExerciseId) {
                    val updatedSets = exercise.sets.map { set ->
                        if (set.id == intent.setId) {
                            set.copy(
                                actualWeightKg = intent.weight,
                                plannedWeightKg = intent.weight,
                            )
                        } else {
                            set
                        }
                    }
                    exercise.copy(sets = updatedSets)
                } else {
                    exercise
                }
            }
            current.copy(exercises = updatedExercises)
        }
    }

    private fun handleUpdateSetReps(intent: WorkoutIntent.UpdateSetReps) {
        _state.update { current ->
            val updatedExercises = current.exercises.map { exercise ->
                if (exercise.id == intent.workoutExerciseId) {
                    val updatedSets = exercise.sets.map { set ->
                        if (set.id == intent.setId) {
                            set.copy(
                                actualReps = intent.reps,
                                plannedReps = intent.reps,
                            )
                        } else {
                            set
                        }
                    }
                    exercise.copy(sets = updatedSets)
                } else {
                    exercise
                }
            }
            current.copy(exercises = updatedExercises)
        }
    }

    private fun handleAddSet(intent: WorkoutIntent.AddSet) {
        viewModelScope.launch {
            val exercise = _state.value.exercises.find { it.id == intent.workoutExerciseId }
                ?: return@launch

            val lastSet = exercise.sets.lastOrNull()
            val newSetIndex = (lastSet?.setNumber ?: 0) + 1
            val newWeight = lastSet?.actualWeightKg ?: lastSet?.plannedWeightKg ?: 0.0
            val newReps = lastSet?.actualReps ?: lastSet?.plannedReps ?: 0

            val newSet = WorkoutSet(
                id = 0, // Will be assigned by Room
                setNumber = newSetIndex,
                type = SetType.WORKING,
                status = SetStatus.PLANNED,
                plannedWeightKg = newWeight,
                plannedReps = newReps,
                actualWeightKg = null,
                actualReps = null,
            )

            // Persist to Room with correct workoutExerciseId
            workoutSessionRepository.insertSet(intent.workoutExerciseId, newSet)

            // Reload sets from Room to get the generated ID
            val updatedSets = workoutSessionRepository
                .getSetsForExercise(intent.workoutExerciseId)
                .first()

            _state.update { current ->
                val updatedExercises = current.exercises.map { ex ->
                    if (ex.id == intent.workoutExerciseId) {
                        ex.copy(sets = markInProgressSet(updatedSets))
                    } else {
                        ex
                    }
                }
                current.copy(exercises = updatedExercises)
            }
        }
    }

    private fun handleDeleteSet(intent: WorkoutIntent.DeleteSet) {
        // Only allow deletion of non-completed sets
        val exercise = _state.value.exercises.find { it.id == intent.workoutExerciseId }
            ?: return
        val set = exercise.sets.find { it.id == intent.setId } ?: return
        if (set.status == SetStatus.COMPLETED) return

        // CRITICAL: Delete from Room immediately (defensive persistence).
        // Must be persisted before updating in-memory state to prevent
        // ghost sets reappearing after process death.
        viewModelScope.launch {
            workoutSessionRepository.deleteSet(intent.setId)
        }

        _state.update { current ->
            val updatedExercises = current.exercises.map { ex ->
                if (ex.id == intent.workoutExerciseId) {
                    val filtered = ex.sets.filter { it.id != intent.setId }
                    ex.copy(sets = markInProgressSet(filtered))
                } else {
                    ex
                }
            }
            current.copy(exercises = updatedExercises)
        }
    }

    private fun handleSkipRestTimer() {
        restTimerManager.skip()
    }

    private fun handleExtendRestTimer() {
        restTimerManager.extend(30)
    }

    private fun handlePauseWorkout() {
        val event = WorkoutEvent.PauseWorkout(pausedAtMillis = System.currentTimeMillis())
        val newPhase = stateMachine.transition(currentPhase, event) ?: return
        currentPhase = newPhase

        pauseStartElapsedRealtime = SystemClock.elapsedRealtime()
        stopTicker()
        restTimerManager.pause()

        viewModelScope.launch {
            val sessionId = _state.value.sessionId
            workoutSessionRepository.updateStatus(
                id = sessionId,
                status = SessionStatus.PAUSED.value,
                completedAt = null,
            )
        }

        _state.update { it.copy(phase = WorkoutPhaseUi.Paused, isPaused = true) }
    }

    private fun handleResumeWorkout() {
        val event = WorkoutEvent.ResumeWorkout(resumedAtMillis = System.currentTimeMillis())
        val newPhase = stateMachine.transition(currentPhase, event) ?: return
        currentPhase = newPhase

        // Calculate additional pause duration
        val pauseDurationMillis = SystemClock.elapsedRealtime() - pauseStartElapsedRealtime
        val additionalPauseSeconds = pauseDurationMillis / 1000
        accumulatedPauseSeconds += additionalPauseSeconds

        // Track workout_resumed analytics
        analyticsTracker.trackUserAction(
            EVENT_WORKOUT_RESUMED,
            mapOf(
                "paused_duration_seconds" to additionalPauseSeconds,
                "sets_completed" to _state.value.completedSetCount,
            ),
        )

        viewModelScope.launch {
            val session = workoutSessionRepository.getSession(_state.value.sessionId)
            if (session != null) {
                workoutSessionRepository.updateSession(
                    session.copy(
                        status = SessionStatus.ACTIVE,
                        pausedDurationSeconds = accumulatedPauseSeconds,
                    ),
                )
            }
        }

        restTimerManager.resume()
        startTicker()

        _state.update { it.copy(phase = WorkoutPhaseUi.Active, isPaused = false) }
    }

    private fun handleRequestFinish() {
        _state.update { it.copy(showFinishDialog = true) }
    }

    private fun handleDismissFinishDialog() {
        _state.update { it.copy(showFinishDialog = false) }
    }

    private fun handleConfirmFinish() {
        val sessionId = _state.value.sessionId
        val event = WorkoutEvent.FinishWorkout(sessionId = sessionId)
        val newPhase = stateMachine.transition(currentPhase, event)
        if (newPhase != null) {
            currentPhase = newPhase
        }

        stopTicker()
        restTimerManager.cancel()

        viewModelScope.launch {
            val session = workoutSessionRepository.getSession(sessionId)
            if (session != null) {
                val completedAt = System.currentTimeMillis()
                val totalDuration = (completedAt - session.startedAt) / 1000 - accumulatedPauseSeconds
                workoutSessionRepository.updateSession(
                    session.copy(
                        status = SessionStatus.COMPLETED,
                        completedAt = completedAt,
                        durationSeconds = totalDuration.coerceAtLeast(0),
                        pausedDurationSeconds = accumulatedPauseSeconds,
                    ),
                )

                // Track workout_completed analytics
                val currentState = _state.value
                val totalVolumeKg = currentState.exercises.sumOf { exercise ->
                    exercise.sets
                        .filter { it.status == SetStatus.COMPLETED }
                        .sumOf { set ->
                            (set.actualWeightKg ?: 0.0) * (set.actualReps ?: 0)
                        }
                }

                analyticsTracker.trackUserAction(
                    EVENT_WORKOUT_COMPLETED,
                    mapOf(
                        "duration_seconds" to totalDuration.coerceAtLeast(0),
                        "total_volume_kg" to totalVolumeKg,
                        "exercise_count" to currentState.exercises.size,
                        "sets_completed" to currentState.completedSetCount,
                        "total_sets" to currentState.totalSetCount,
                    ),
                )
            }

            _state.update {
                it.copy(
                    phase = WorkoutPhaseUi.Completed(sessionId),
                    showFinishDialog = false,
                )
            }

            _sideEffect.trySend(WorkoutSideEffect.NavigateToSummary(sessionId))
        }
    }

    private fun handleToggleExpanded(intent: WorkoutIntent.ToggleExerciseExpanded) {
        _state.update { current ->
            val updatedExercises = current.exercises.map { ex ->
                if (ex.id == intent.workoutExerciseId) {
                    ex.copy(isExpanded = !ex.isExpanded)
                } else {
                    ex
                }
            }
            current.copy(exercises = updatedExercises)
        }
    }

    // ----- Notes Handlers -----

    /** Debounce job for notes persistence. */
    private var notesSaveJob: Job? = null

    private fun handleToggleNotes(intent: WorkoutIntent.ToggleNotes) {
        _state.update { current ->
            val expanded = current.notesExpandedExerciseIds
            val updated = if (intent.workoutExerciseId in expanded) {
                expanded - intent.workoutExerciseId
            } else {
                expanded + intent.workoutExerciseId
            }
            current.copy(notesExpandedExerciseIds = updated)
        }
    }

    private fun handleUpdateNotes(intent: WorkoutIntent.UpdateNotes) {
        // Enforce 1000 char limit
        val trimmedText = if (intent.text.length > NOTES_MAX_LENGTH) {
            intent.text.take(NOTES_MAX_LENGTH)
        } else {
            intent.text
        }

        // Update in-memory state immediately
        _state.update { current ->
            val updatedExercises = current.exercises.map { ex ->
                if (ex.id == intent.workoutExerciseId) {
                    ex.copy(notes = trimmedText.ifEmpty { null })
                } else {
                    ex
                }
            }
            current.copy(exercises = updatedExercises)
        }

        // Debounce the Room write (500ms)
        notesSaveJob?.cancel()
        notesSaveJob = viewModelScope.launch {
            delay(NOTES_SAVE_DEBOUNCE_MS)
            try {
                workoutSessionRepository.updateExerciseNotes(
                    workoutExerciseId = intent.workoutExerciseId,
                    notes = trimmedText.ifEmpty { null },
                )
            } catch (_: Exception) {
                // Silently fail -- notes are non-critical and the in-memory state is preserved
            }
        }
    }

    // ----- Formatting Helpers -----

    override fun onCleared() {
        super.onCleared()
        stopTicker()
        // Do NOT cancel the rest timer here -- it is managed by the singleton
        // and the foreground service. The timer survives ViewModel destruction.
    }
}
