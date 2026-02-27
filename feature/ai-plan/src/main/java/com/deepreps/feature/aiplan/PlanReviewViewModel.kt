package com.deepreps.feature.aiplan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.DeloadStatus
import com.deepreps.core.domain.model.ExerciseForPlan
import com.deepreps.core.domain.model.ExerciseHistory
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.PlanResult
import com.deepreps.core.domain.model.UserPlanProfile
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.provider.AnalyticsTracker
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.usecase.DetectDeloadNeedUseCase
import com.deepreps.core.domain.usecase.DetermineSessionDayTypeUseCase
import com.deepreps.core.domain.usecase.GeneratePlanUseCase
import com.deepreps.core.domain.usecase.ValidatePlanSafetyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Plan Review screen.
 *
 * MVI pattern: intents in, state + side effects out.
 * Orchestrates plan generation, safety validation, and plan editing.
 *
 * Analytics events (P0, per analytics-plan.md Section 1.4):
 * - ai_plan_requested: when plan generation starts
 * - ai_plan_received: when plan generation succeeds (with latency_ms)
 * - ai_plan_failed: when plan generation fails
 * - ai_plan_fallback_used: when a non-AI plan source is used
 */
@HiltViewModel
@Suppress("LongParameterList", "TooManyFunctions")
class PlanReviewViewModel @Inject constructor(
    @Suppress("UnusedPrivateProperty") private val savedStateHandle: SavedStateHandle,
    private val generatePlanUseCase: GeneratePlanUseCase,
    private val validatePlanSafetyUseCase: ValidatePlanSafetyUseCase,
    private val detectDeloadNeedUseCase: DetectDeloadNeedUseCase,
    private val determineSessionDayTypeUseCase: DetermineSessionDayTypeUseCase,
    private val exerciseRepository: ExerciseRepository,
    private val userProfileRepository: UserProfileRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _state = MutableStateFlow(PlanReviewUiState())
    val state: StateFlow<PlanReviewUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<PlanReviewSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<PlanReviewSideEffect> = _sideEffect.receiveAsFlow()

    // Cached request for regeneration
    private var lastPlanRequest: PlanRequest? = null
    private var lastExerciseIds: List<Long> = emptyList()

    fun onIntent(intent: PlanReviewIntent) {
        when (intent) {
            is PlanReviewIntent.LoadPlan -> handleLoadPlan(intent.exerciseIds)
            is PlanReviewIntent.UpdateWeight -> handleUpdateWeight(intent)
            is PlanReviewIntent.UpdateReps -> handleUpdateReps(intent)
            is PlanReviewIntent.ConfirmPlan -> handleConfirmPlan()
            is PlanReviewIntent.RegeneratePlan -> handleRegeneratePlan()
            is PlanReviewIntent.DismissSafetyWarnings -> {
                _state.value = _state.value.copy(showSafetyWarnings = false)
            }
            is PlanReviewIntent.AddWorkingSet -> handleAddWorkingSet(intent)
            is PlanReviewIntent.RemoveLastWorkingSet ->
                handleRemoveLastWorkingSet(intent)
        }
    }

    private fun handleLoadPlan(exerciseIds: List<Long>) {
        lastExerciseIds = exerciseIds
        viewModelScope.launch {
            _state.value = _state.value.copy(
                phase = PlanReviewUiState.Phase.Generating,
            )

            @Suppress("TooGenericExceptionCaught")
            try {
                val request = buildPlanRequest(exerciseIds)
                lastPlanRequest = request
                trackPlanRequested(request)
                executePlanGeneration(request)
            } catch (e: Exception) {
                Timber.e(e, "Failed to build plan request")
                trackPlanFailed("request_build_error", e)
                _state.value = _state.value.copy(
                    phase = PlanReviewUiState.Phase.Error,
                )
                _sideEffect.send(
                    PlanReviewSideEffect.ShowError(
                        "Failed to generate plan: ${e.message}",
                    ),
                )
            }
        }
    }

    private fun handleRegeneratePlan() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                phase = PlanReviewUiState.Phase.Generating,
            )

            @Suppress("TooGenericExceptionCaught")
            try {
                val actualRequest = lastPlanRequest
                    ?: buildPlanRequest(lastExerciseIds)
                lastPlanRequest = actualRequest
                trackPlanRequested(actualRequest)
                executePlanGeneration(actualRequest)
            } catch (e: Exception) {
                Timber.e(e, "Failed to regenerate plan")
                trackPlanFailed("regeneration_error", e)
                _state.value = _state.value.copy(
                    phase = PlanReviewUiState.Phase.Error,
                )
                _sideEffect.send(
                    PlanReviewSideEffect.ShowError(
                        "Failed to regenerate plan: ${e.message}",
                    ),
                )
            }
        }
    }

    private suspend fun executePlanGeneration(request: PlanRequest) {
        val startTimeMs = System.currentTimeMillis()
        val result = generatePlanUseCase.execute(request)
        val latencyMs = System.currentTimeMillis() - startTimeMs

        val source = mapResultToSource(result)
        trackPlanResult(result, latencyMs)

        val plan = result.plan
        if (plan == null) {
            _state.value = _state.value.copy(
                phase = PlanReviewUiState.Phase.Error,
            )
            _sideEffect.send(
                PlanReviewSideEffect.ShowError(
                    "Could not generate a plan. Please try again.",
                ),
            )
            return
        }

        applyPlanToState(plan, request, source)
    }

    private suspend fun applyPlanToState(
        plan: GeneratedPlan,
        request: PlanRequest,
        source: PlanReviewUiState.PlanSource,
    ) {
        val violations = validatePlanSafetyUseCase.validate(plan, request)
        _state.value = _state.value.copy(
            phase = PlanReviewUiState.Phase.PlanReady,
            exercisePlans = plan.exercises.map {
                EditableExercisePlan.fromDomain(it)
            },
            planSource = source,
            safetyViolations = violations,
            showSafetyWarnings = violations.isNotEmpty(),
        )
    }

    private fun mapResultToSource(
        result: PlanResult,
    ): PlanReviewUiState.PlanSource = when (result) {
        is PlanResult.AiGenerated -> PlanReviewUiState.PlanSource.AI_GENERATED
        is PlanResult.Cached -> PlanReviewUiState.PlanSource.CACHED
        is PlanResult.Baseline -> PlanReviewUiState.PlanSource.BASELINE
        is PlanResult.Manual -> PlanReviewUiState.PlanSource.MANUAL
    }

    @Suppress("LongMethod")
    private suspend fun buildPlanRequest(
        exerciseIds: List<Long>,
    ): PlanRequest {
        val profile = userProfileRepository.get()
            ?: throw IllegalStateException(
                "User profile not found -- onboarding incomplete",
            )

        val exercisesForPlan = buildExercisesForPlan(exerciseIds)

        val userPlanProfile = UserPlanProfile(
            experienceLevel = profile.experienceLevel.value,
            bodyWeightKg = profile.bodyWeightKg,
            age = profile.age,
            gender = profile.gender?.value,
            compoundRepMin = profile.compoundRepMin,
            compoundRepMax = profile.compoundRepMax,
            isolationRepMin = profile.isolationRepMin,
            isolationRepMax = profile.isolationRepMax,
            defaultWorkingSets = profile.defaultWorkingSets,
        )

        val exerciseNameMap = exercisesForPlan.associate { it.exerciseId to it.name }

        val trainingHistory = exerciseIds.mapNotNull { exerciseId ->
            val sessions = workoutSessionRepository
                .getRecentWorkingSetsForExercise(exerciseId, sessionLimit = 3)
            if (sessions.isEmpty()) {
                null
            } else {
                ExerciseHistory(
                    exerciseId = exerciseId,
                    exerciseName = exerciseNameMap[exerciseId].orEmpty(),
                    sessions = sessions,
                    trend = null,
                )
            }
        }

        val deloadStatus = detectDeloadNeedUseCase.execute(
            experienceLevel = userPlanProfile.experienceLevel,
            weeksSinceLastDeload = null, // Phase 2: track from session history
            exerciseHistories = trainingHistory,
        )

        val periodization = determineSessionDayTypeUseCase.execute(
            experienceLevel = userPlanProfile.experienceLevel,
            exerciseHistories = trainingHistory,
        )

        return PlanRequest(
            userProfile = userPlanProfile,
            exercises = exercisesForPlan,
            trainingHistory = trainingHistory,
            periodizationModel = periodization.periodizationModel,
            performanceTrend = null, // Phase 2: compute from history
            weeksSinceDeload = null, // Phase 2: track
            deloadRecommended = deloadStatus != DeloadStatus.NOT_NEEDED,
            currentBlockPhase = periodization.blockPhase,
            currentBlockWeek = periodization.blockWeek,
        )
    }

    private suspend fun buildExercisesForPlan(
        exerciseIds: List<Long>,
    ): List<ExerciseForPlan> {
        val exercises = exerciseRepository.getExercisesByIds(exerciseIds)
        val exerciseMap = exercises.associateBy { it.id }
        val orderedExercises = exerciseIds.mapNotNull { id -> exerciseMap[id] }
        return orderedExercises.map { exercise ->
            val muscleGroupIndex = (exercise.primaryGroupId - 1).toInt()
            val primaryGroup = MuscleGroup.entries
                .getOrNull(muscleGroupIndex)?.value ?: ""
            ExerciseForPlan(
                exerciseId = exercise.id,
                stableId = exercise.stableId,
                name = exercise.name,
                equipment = exercise.equipment.value,
                movementType = exercise.movementType.value,
                difficulty = exercise.difficulty.value,
                primaryGroup = primaryGroup,
            )
        }
    }

    private fun handleAddWorkingSet(intent: PlanReviewIntent.AddWorkingSet) {
        val currentPlans = _state.value.exercisePlans.toMutableList()
        if (intent.exerciseIndex !in currentPlans.indices) return

        val exercise = currentPlans[intent.exerciseIndex]
        val workingSets = exercise.sets.filter { it.setType == "working" }

        if (workingSets.size >= MAX_WORKING_SETS) return

        val lastWorkingSet = workingSets.lastOrNull() ?: return
        val newSet = EditableSet(
            index = exercise.sets.size,
            setType = "working",
            weight = lastWorkingSet.weight,
            reps = lastWorkingSet.reps,
            restSeconds = lastWorkingSet.restSeconds,
        )

        val updatedSets = exercise.sets + newSet
        currentPlans[intent.exerciseIndex] = exercise.copy(sets = updatedSets)
        _state.value = _state.value.copy(exercisePlans = currentPlans)
    }

    private fun handleRemoveLastWorkingSet(
        intent: PlanReviewIntent.RemoveLastWorkingSet,
    ) {
        val currentPlans = _state.value.exercisePlans.toMutableList()
        if (intent.exerciseIndex !in currentPlans.indices) return

        val exercise = currentPlans[intent.exerciseIndex]
        val workingSets = exercise.sets.filter { it.setType == "working" }

        if (workingSets.size <= MIN_WORKING_SETS) return

        val lastWorkingSetIndex =
            exercise.sets.indexOfLast { it.setType == "working" }
        if (lastWorkingSetIndex < 0) return

        val updatedSets = exercise.sets.toMutableList()
            .apply { removeAt(lastWorkingSetIndex) }
        val reindexed = updatedSets.mapIndexed { index, set ->
            set.copy(index = index)
        }
        currentPlans[intent.exerciseIndex] =
            exercise.copy(sets = reindexed)
        _state.value = _state.value.copy(exercisePlans = currentPlans)
    }

    private fun handleUpdateWeight(intent: PlanReviewIntent.UpdateWeight) {
        val currentPlans = _state.value.exercisePlans.toMutableList()
        if (intent.exerciseIndex !in currentPlans.indices) return

        val exercise = currentPlans[intent.exerciseIndex]
        val updatedSets = exercise.sets.toMutableList()
        if (intent.setIndex !in updatedSets.indices) return

        updatedSets[intent.setIndex] = updatedSets[intent.setIndex].copy(
            weight = intent.newWeight.coerceAtLeast(0.0),
        )
        currentPlans[intent.exerciseIndex] = exercise.copy(sets = updatedSets)

        _state.value = _state.value.copy(exercisePlans = currentPlans)
    }

    private fun handleUpdateReps(intent: PlanReviewIntent.UpdateReps) {
        val currentPlans = _state.value.exercisePlans.toMutableList()
        if (intent.exerciseIndex !in currentPlans.indices) return

        val exercise = currentPlans[intent.exerciseIndex]
        val updatedSets = exercise.sets.toMutableList()
        if (intent.setIndex !in updatedSets.indices) return

        updatedSets[intent.setIndex] = updatedSets[intent.setIndex].copy(
            reps = intent.newReps.coerceIn(1, 50),
        )
        currentPlans[intent.exerciseIndex] = exercise.copy(sets = updatedSets)

        _state.value = _state.value.copy(exercisePlans = currentPlans)
    }

    @Suppress("LongMethod")
    private fun handleConfirmPlan() {
        val exercisePlans = _state.value.exercisePlans
        if (exercisePlans.isEmpty()) return

        viewModelScope.launch {
            _state.value = _state.value.copy(
                phase = PlanReviewUiState.Phase.Generating,
            )

            @Suppress("TooGenericExceptionCaught")
            try {
                val sessionId = createSessionFromPlan(exercisePlans)
                _sideEffect.send(
                    PlanReviewSideEffect.NavigateToWorkout(sessionId),
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to create workout session from plan")
                _state.value = _state.value.copy(
                    phase = PlanReviewUiState.Phase.PlanReady,
                )
                _sideEffect.send(
                    PlanReviewSideEffect.ShowError(
                        "Failed to start workout: ${e.message}",
                    ),
                )
            }
        }
    }

    /**
     * Persists the confirmed plan as a WorkoutSession with exercises and sets in Room.
     *
     * Uses [WorkoutSessionRepository.insertExerciseWithSets] for transactional writes
     * per exercise (exercise + its sets atomically).
     *
     * @return The Room-generated session ID.
     */
    @Suppress("LongMethod")
    private suspend fun createSessionFromPlan(
        exercisePlans: List<EditableExercisePlan>,
    ): Long {
        val now = System.currentTimeMillis()

        val session = WorkoutSession(
            id = 0,
            startedAt = now,
            completedAt = null,
            durationSeconds = null,
            pausedDurationSeconds = 0,
            status = SessionStatus.ACTIVE,
            notes = null,
            templateId = null,
        )

        val sessionId = workoutSessionRepository.createSession(session)

        exercisePlans.forEachIndexed { orderIndex, plan ->
            val exercise = WorkoutExercise(
                id = 0,
                sessionId = sessionId,
                exerciseId = plan.exerciseId,
                orderIndex = orderIndex,
                supersetGroupId = null,
                restTimerSeconds = plan.restSeconds,
                notes = plan.notes,
            )

            val sets = plan.sets.mapIndexed { setIndex, editableSet ->
                WorkoutSet(
                    id = 0,
                    setNumber = setIndex + 1,
                    type = SetType.fromValue(editableSet.setType),
                    status = SetStatus.PLANNED,
                    plannedWeightKg = editableSet.weight,
                    plannedReps = editableSet.reps,
                    actualWeightKg = null,
                    actualReps = null,
                    completedAt = null,
                    isPersonalRecord = false,
                )
            }

            workoutSessionRepository.insertExerciseWithSets(exercise, sets)
        }

        return sessionId
    }

    private fun trackPlanRequested(request: PlanRequest) {
        analyticsTracker.trackUserAction(
            EVENT_AI_PLAN_REQUESTED,
            mapOf(
                "exercise_count" to request.exercises.size,
                "experience_level" to request.userProfile.experienceLevel,
                "has_history" to request.trainingHistory.isNotEmpty(),
            ),
        )
    }

    private fun trackPlanFailed(errorType: String, e: Exception) {
        analyticsTracker.trackUserAction(
            EVENT_AI_PLAN_FAILED,
            mapOf(
                "error_type" to errorType,
                "error_message" to (e.message ?: "unknown"),
            ),
        )
    }

    private fun trackPlanResult(result: PlanResult, latencyMs: Long) {
        val planSourceString = when (result) {
            is PlanResult.AiGenerated -> "ai"
            is PlanResult.Cached -> "cached"
            is PlanResult.Baseline -> "baseline"
            is PlanResult.Manual -> "manual"
        }

        when (result) {
            is PlanResult.AiGenerated -> {
                analyticsTracker.trackUserAction(
                    EVENT_AI_PLAN_RECEIVED,
                    mapOf(
                        "latency_ms" to latencyMs,
                        "exercise_count" to
                            (result.plan?.exercises?.size ?: 0),
                        "plan_source" to planSourceString,
                    ),
                )
            }

            is PlanResult.Cached, is PlanResult.Baseline -> {
                analyticsTracker.trackUserAction(
                    EVENT_AI_PLAN_FALLBACK_USED,
                    mapOf(
                        "fallback_type" to planSourceString,
                        "latency_ms" to latencyMs,
                    ),
                )
            }

            is PlanResult.Manual -> {
                analyticsTracker.trackUserAction(
                    EVENT_AI_PLAN_FALLBACK_USED,
                    mapOf("fallback_type" to "manual"),
                )
            }
        }
    }

    companion object {
        private const val MIN_WORKING_SETS = 2
        private const val MAX_WORKING_SETS = 6

        // Event names matching analytics-plan.md taxonomy (Section 1.4)
        private const val EVENT_AI_PLAN_REQUESTED = "ai_plan_requested"
        private const val EVENT_AI_PLAN_RECEIVED = "ai_plan_received"
        private const val EVENT_AI_PLAN_FAILED = "ai_plan_failed"
        private const val EVENT_AI_PLAN_FALLBACK_USED =
            "ai_plan_fallback_used"
    }
}
