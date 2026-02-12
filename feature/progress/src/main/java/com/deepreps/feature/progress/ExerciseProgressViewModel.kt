package com.deepreps.feature.progress

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
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
                val completedSessions = workoutSessionRepository
                    .getCompletedSessions()
                    .first()

                val timeRange = _state.value.selectedTimeRange
                val filtered = ProgressDashboardViewModel.filterByTimeRange(
                    completedSessions,
                    timeRange,
                )

                val dataPoints = mutableListOf<ChartDataPoint>()

                for (session in filtered) {
                    val exercises = workoutSessionRepository
                        .getExercisesForSession(session.id)
                        .first()

                    val targetExercise = exercises.find { it.exerciseId == exerciseId }
                        ?: continue

                    val sets = workoutSessionRepository
                        .getSetsForExercise(targetExercise.id)
                        .first()

                    val bestWeight = findBestWeight(sets)
                    if (bestWeight != null) {
                        dataPoints.add(
                            ChartDataPoint(
                                dateEpochMs = session.startedAt,
                                weightKg = bestWeight,
                                isPersonalRecord = sets.any {
                                    it.isPersonalRecord &&
                                        it.actualWeightKg == bestWeight
                                },
                            ),
                        )
                    }
                }

                // Sort by date ascending for chart rendering
                val sorted = dataPoints.sortedBy { it.dateEpochMs }
                val currentBest = sorted.lastOrNull()?.weightKg
                val allTimeBest = sorted.maxByOrNull { it.weightKg }?.weightKg

                _state.update { current ->
                    current.copy(
                        chartData = sorted,
                        currentBestKg = currentBest,
                        allTimeBestKg = allTimeBest,
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
    }
}
