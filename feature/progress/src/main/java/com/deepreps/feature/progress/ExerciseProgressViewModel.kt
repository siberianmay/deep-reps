package com.deepreps.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import com.deepreps.core.domain.util.Estimated1rmCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the exercise progress (weight chart) screen.
 *
 * Loads all historical sessions containing the given exercise, computes
 * the best weight per session, and presents chart data points sorted by date.
 *
 * Navigation argument "exerciseId" is required.
 */
@HiltViewModel
class ExerciseProgressViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userProfileRepository: UserProfileRepository,
) : ViewModel() {

    private val exerciseId: Long = checkNotNull(savedStateHandle.get<Long>(EXERCISE_ID_ARG))

    private val _state = MutableStateFlow(ExerciseProgressUiState())
    val state: StateFlow<ExerciseProgressUiState> = _state.asStateFlow()

    init {
        loadWeightUnit()
        loadExerciseName()
        loadChartData()
    }

    fun onIntent(intent: ExerciseProgressIntent) {
        when (intent) {
            is ExerciseProgressIntent.SelectTimeRange -> handleSelectTimeRange(intent.timeRange)
            is ExerciseProgressIntent.Retry -> handleRetry()
        }
    }

    private fun handleSelectTimeRange(timeRange: TimeRange) {
        _state.update { it.copy(selectedTimeRange = timeRange) }
        loadChartData()
    }

    private fun handleRetry() {
        _state.update { it.copy(errorType = null, isLoading = true) }
        loadExerciseName()
        loadChartData()
    }

    private fun loadWeightUnit() {
        viewModelScope.launch {
            try {
                val profile = userProfileRepository.get()
                if (profile != null) {
                    _state.update { it.copy(weightUnit = profile.preferredUnit) }
                }
            } catch (_: Exception) {
                // Keep default kg
            }
        }
    }

    private fun loadExerciseName() {
        viewModelScope.launch {
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                if (exercise != null) {
                    _state.update { it.copy(exerciseName = exercise.name) }
                }
            } catch (_: Exception) {
                // Name stays empty, not a critical failure
            }
        }
    }

    @Suppress("LongMethod")
    private fun loadChartData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val exercise = exerciseRepository.getExerciseById(exerciseId)
                val isBodyweight = exercise?.equipment == Equipment.BODYWEIGHT
                val bodyWeightKg = resolveBodyWeightKg(isBodyweight)
                val bodyweightMissing = isBodyweight && bodyWeightKg == null

                val completedSessions = workoutSessionRepository
                    .getCompletedSessions()
                    .first()

                val timeRange = _state.value.selectedTimeRange
                val filtered = ProgressDashboardViewModel.filterByTimeRange(
                    completedSessions,
                    timeRange,
                )

                val dataPoints = filtered.mapNotNull { session ->
                    buildDataPoint(session.id, session.startedAt, isBodyweight, bodyWeightKg)
                }

                val sorted = dataPoints.sortedBy { it.dateEpochMs }
                val currentBest = sorted.lastOrNull()?.weightKg
                val allTimeBest = sorted.maxByOrNull { it.weightKg }?.weightKg
                val current1rm = sorted.lastOrNull()?.estimated1rmKg
                val allTime1rm = sorted.mapNotNull { it.estimated1rmKg }.maxOrNull()

                _state.update { current ->
                    current.copy(
                        chartData = sorted,
                        currentBestKg = currentBest,
                        allTimeBestKg = allTimeBest,
                        currentBestEstimated1rmKg = current1rm,
                        allTimeBestEstimated1rmKg = allTime1rm,
                        isBodyweightMissingProfile = bodyweightMissing,
                        isLoading = false,
                        errorType = null,
                    )
                }
            } catch (_: Exception) {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = ExerciseProgressError.LoadFailed,
                    )
                }
            }
        }
    }

    private suspend fun resolveBodyWeightKg(isBodyweight: Boolean): Double? {
        if (!isBodyweight) return null
        return try {
            userProfileRepository.get()?.bodyWeightKg
        } catch (_: Exception) {
            null
        }
    }

    private suspend fun buildDataPoint(
        sessionId: Long,
        startedAt: Long,
        isBodyweight: Boolean,
        bodyWeightKg: Double?,
    ): ChartDataPoint? {
        val exercises = workoutSessionRepository
            .getExercisesForSession(sessionId)
            .first()

        val targetExercise = exercises.find { it.exerciseId == exerciseId }
            ?: return null

        val sets = workoutSessionRepository
            .getSetsForExercise(targetExercise.id)
            .first()

        val bestWeight = findBestWeight(sets) ?: return null

        val best1rm = computeBest1rm(sets, isBodyweight, bodyWeightKg)

        return ChartDataPoint(
            dateEpochMs = startedAt,
            weightKg = bestWeight,
            isPersonalRecord = sets.any {
                it.isPersonalRecord && it.actualWeightKg == bestWeight
            },
            estimated1rmKg = best1rm?.estimatedKg,
            confidence = best1rm?.confidence,
        )
    }

    companion object {
        const val EXERCISE_ID_ARG = "exerciseId"

        /**
         * Finds the best (heaviest) completed weight from a list of sets.
         * Returns null if no completed sets with actual weight exist.
         */
        internal fun findBestWeight(sets: List<WorkoutSet>): Double? {
            return sets
                .filter { it.status == SetStatus.COMPLETED && it.actualWeightKg != null }
                .maxByOrNull { it.actualWeightKg!! }
                ?.actualWeightKg
        }

        /**
         * Computes the best estimated 1RM across all completed working sets.
         *
         * For bodyweight exercises, substitutes body weight when actual weight
         * is null/zero, or adds body weight to actual weight.
         * Returns null if no valid 1RM can be computed.
         */
        internal fun computeBest1rm(
            sets: List<WorkoutSet>,
            isBodyweight: Boolean,
            bodyWeightKg: Double?,
        ): Best1rmResult? {
            if (isBodyweight && bodyWeightKg == null) return null

            return sets
                .filter { it.status == SetStatus.COMPLETED && it.actualReps != null }
                .mapNotNull { set ->
                    val effectiveWeight = resolveEffectiveWeight(set, isBodyweight, bodyWeightKg)
                        ?: return@mapNotNull null
                    val reps = set.actualReps ?: return@mapNotNull null
                    val result = Estimated1rmCalculator.calculateWithConfidence(effectiveWeight, reps)
                        ?: return@mapNotNull null
                    Best1rmResult(result.estimatedKg, result.confidence)
                }
                .maxByOrNull { it.estimatedKg }
        }

        private fun resolveEffectiveWeight(
            set: WorkoutSet,
            isBodyweight: Boolean,
            bodyWeightKg: Double?,
        ): Double? {
            if (!isBodyweight) return set.actualWeightKg
            val bw = bodyWeightKg ?: return null
            val actual = set.actualWeightKg
            return if (actual == null || actual == 0.0) bw else bw + actual
        }
    }

    internal data class Best1rmResult(
        val estimatedKg: Double,
        val confidence: Estimated1rmCalculator.Confidence,
    )
}
