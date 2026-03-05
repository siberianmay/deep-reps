package com.deepreps.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.model.enums.RecordType
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.PersonalRecordRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job

/**
 * ViewModel for the progress dashboard screen.
 *
 * Loads completed sessions, computes summary stats, and groups by date.
 * Respects user's preferred weight unit for display.
 */
@HiltViewModel
class ProgressDashboardViewModel @Inject constructor(
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userProfileRepository: UserProfileRepository,
    private val personalRecordRepository: PersonalRecordRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProgressDashboardUiState())
    val state: StateFlow<ProgressDashboardUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<ProgressDashboardSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<ProgressDashboardSideEffect> = _sideEffect.receiveAsFlow()

    private var sessionsJob: Job? = null
    private var recordsJob: Job? = null

    init {
        loadWeightUnit()
        observeSessions()
    }

    fun onIntent(intent: ProgressDashboardIntent) {
        when (intent) {
            is ProgressDashboardIntent.SelectTimeRange -> handleSelectTimeRange(intent.timeRange)
            is ProgressDashboardIntent.ViewSession -> handleViewSession(intent.sessionId)
            is ProgressDashboardIntent.ViewExerciseProgress -> handleViewExerciseProgress(
                intent.exerciseId,
            )
            is ProgressDashboardIntent.Retry -> handleRetry()
            is ProgressDashboardIntent.SelectTab -> handleSelectTab(intent.tab)
        }
    }

    private fun handleSelectTimeRange(timeRange: TimeRange) {
        _state.update { it.copy(selectedTimeRange = timeRange) }
        observeSessions()
    }

    private fun handleViewSession(sessionId: Long) {
        _sideEffect.trySend(ProgressDashboardSideEffect.NavigateToSessionDetail(sessionId))
    }

    private fun handleViewExerciseProgress(exerciseId: Long) {
        _sideEffect.trySend(ProgressDashboardSideEffect.NavigateToExerciseProgress(exerciseId))
    }

    private fun handleRetry() {
        _state.update { it.copy(errorType = null, isLoading = true) }
        observeSessions()
    }

    private fun handleSelectTab(tab: DashboardTab) {
        _state.update { it.copy(selectedTab = tab) }
        if (tab == DashboardTab.RECORDS) {
            loadPersonalRecords()
        }
    }

    private fun loadPersonalRecords() {
        recordsJob?.cancel()
        _state.update { it.copy(isRecordsLoading = true) }
        recordsJob = personalRecordRepository.observeAll()
            .onEach { records -> processPersonalRecords(records) }
            .catch {
                _state.update { it.copy(isRecordsLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun processPersonalRecords(records: List<PersonalRecord>) {
        val maxWeightRecords = records.filter { it.recordType == RecordType.MAX_WEIGHT }

        val bestByExercise = maxWeightRecords
            .groupBy { it.exerciseId }
            .mapValues { (_, exerciseRecords) ->
                exerciseRecords.maxByOrNull { it.weightValue ?: 0.0 }
            }
            .values
            .filterNotNull()

        val summaries = bestByExercise.mapNotNull { record ->
            mapRecordToSummary(record)
        }

        val grouped = summaries
            .groupBy { it.muscleGroup }
            .mapValues { (_, list) -> list.sortedByDescending { it.bestWeightKg } }

        _state.update { it.copy(personalRecords = grouped, isRecordsLoading = false) }
    }

    private suspend fun mapRecordToSummary(record: PersonalRecord): PrSummaryUi? {
        val exercise = exerciseRepository.getExerciseById(record.exerciseId) ?: return null
        val muscleGroup = MuscleGroup.fromId(exercise.primaryGroupId) ?: return null

        return PrSummaryUi(
            exerciseId = exercise.id,
            exerciseName = exercise.name,
            muscleGroup = muscleGroup,
            bestWeightKg = record.weightValue ?: 0.0,
            bestReps = record.reps,
            achievedAtText = formatDate(record.achievedAt),
        )
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

    private fun observeSessions() {
        sessionsJob?.cancel()
        sessionsJob = workoutSessionRepository.getCompletedSessions()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { sessions ->
                val timeRange = _state.value.selectedTimeRange
                val filtered = filterByTimeRange(sessions, timeRange)
                val summaries = filtered.map { session -> mapToSummary(session) }

                _state.update { current ->
                    current.copy(
                        recentSessions = summaries,
                        isLoading = false,
                        errorType = null,
                    )
                }
            }
            .catch {
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        errorType = ProgressDashboardError.LoadFailed,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun mapToSummary(session: WorkoutSession): SessionSummaryUi {
        val exercises = workoutSessionRepository
            .getExercisesForSession(session.id)
            .first()

        var totalVolume = 0.0
        var totalSets = 0

        for (exercise in exercises) {
            val sets = workoutSessionRepository
                .getSetsForExercise(exercise.id)
                .first()
            for (set in sets) {
                val weight = set.actualWeightKg
                val reps = set.actualReps
                if (set.status == SetStatus.COMPLETED && weight != null && reps != null) {
                    totalVolume += weight * reps
                    totalSets++
                }
            }
        }

        val muscleGroupNames = computeMuscleGroupNames(exercises)

        return SessionSummaryUi(
            sessionId = session.id,
            dateText = formatDate(session.startedAt),
            durationText = formatDuration(session.durationSeconds),
            exerciseCount = exercises.size,
            totalVolumeKg = totalVolume,
            muscleGroupNames = muscleGroupNames,
            setCount = totalSets,
        )
    }

    private suspend fun computeMuscleGroupNames(
        exercises: List<WorkoutExercise>,
    ): String {
        val exerciseIds = exercises.map { it.exerciseId }
        if (exerciseIds.isEmpty()) return ""

        val exerciseDetails = exerciseRepository.getExercisesByIds(exerciseIds)
        val groupIds = exerciseDetails.map { it.primaryGroupId }.distinct()

        return groupIds.mapNotNull { groupId ->
            MuscleGroup.fromId(groupId)?.let { group ->
                when (group) {
                    MuscleGroup.LEGS -> "Legs"
                    MuscleGroup.LOWER_BACK -> "Lower Back"
                    MuscleGroup.CHEST -> "Chest"
                    MuscleGroup.BACK -> "Back"
                    MuscleGroup.SHOULDERS -> "Shoulders"
                    MuscleGroup.ARMS -> "Arms"
                    MuscleGroup.CORE -> "Core"
                }
            }
        }.joinToString(", ")
    }

    companion object {

        private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

        internal fun formatDate(epochMillis: Long): String {
            return Instant.ofEpochMilli(epochMillis)
                .atZone(ZoneId.systemDefault())
                .format(dateFormatter)
        }

        internal fun formatDuration(seconds: Long?): String {
            if (seconds == null || seconds <= 0) return "--"
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            return if (hours > 0) {
                "${hours}h ${minutes}m"
            } else {
                "${minutes}m"
            }
        }

        internal fun filterByTimeRange(
            sessions: List<WorkoutSession>,
            timeRange: TimeRange,
        ): List<WorkoutSession> {
            val weeks = timeRange.weeks ?: return sessions
            val cutoffMs = System.currentTimeMillis() - (weeks * 7L * 24 * 60 * 60 * 1000)
            return sessions.filter { it.startedAt >= cutoffMs }
        }
    }
}
