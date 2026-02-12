package com.deepreps.feature.workout.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.usecase.OrderExercisesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the multi-step workout setup flow.
 *
 * Manages: muscle group selection -> exercise selection -> exercise ordering -> plan generation.
 * Alternative path: template loading -> exercise ordering -> plan generation.
 */
@HiltViewModel
class WorkoutSetupViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val templateRepository: TemplateRepository,
    private val orderExercisesUseCase: OrderExercisesUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutSetupUiState())
    val state: StateFlow<WorkoutSetupUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<WorkoutSetupSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<WorkoutSetupSideEffect> = _sideEffect.receiveAsFlow()

    fun onIntent(intent: WorkoutSetupIntent) {
        when (intent) {
            is WorkoutSetupIntent.ToggleGroup -> handleToggleGroup(intent.group)
            is WorkoutSetupIntent.SetExercises -> handleSetExercises(intent.exerciseIds)
            is WorkoutSetupIntent.MoveExercise -> handleMoveExercise(intent.fromIndex, intent.toIndex)
            is WorkoutSetupIntent.GeneratePlan -> handleGeneratePlan()
            is WorkoutSetupIntent.LoadTemplate -> handleLoadTemplate(intent.templateId)
            is WorkoutSetupIntent.Retry -> handleRetry()
            is WorkoutSetupIntent.Reset -> handleReset()
        }
    }

    private fun handleToggleGroup(group: MuscleGroup) {
        _state.update { current ->
            val updated = if (group in current.selectedGroups) {
                current.selectedGroups - group
            } else {
                current.selectedGroups + group
            }
            current.copy(selectedGroups = updated)
        }
    }

    private fun handleSetExercises(exerciseIds: Set<Long>) {
        viewModelScope.launch {
            try {
                val exercises = exerciseRepository.getExercisesByIds(exerciseIds.toList())
                val ordered = orderExercisesUseCase(exercises)
                val items = ordered.mapIndexed { index, exercise ->
                    exercise.toOrderItem(index)
                }
                _state.update { current ->
                    current.copy(
                        selectedExercises = items,
                        error = null,
                    )
                }
                _sideEffect.trySend(WorkoutSetupSideEffect.NavigateToExerciseOrder)
            } catch (_: Exception) {
                _state.update {
                    it.copy(error = WorkoutSetupError.ExerciseLoadFailed)
                }
                _sideEffect.trySend(
                    WorkoutSetupSideEffect.ShowError("Failed to load exercises."),
                )
            }
        }
    }

    private fun handleMoveExercise(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val exercises = current.selectedExercises.toMutableList()
            if (fromIndex in exercises.indices && toIndex in exercises.indices) {
                val item = exercises.removeAt(fromIndex)
                exercises.add(toIndex, item)
                val reindexed = exercises.mapIndexed { index, exercise ->
                    exercise.copy(orderIndex = index)
                }
                current.copy(selectedExercises = reindexed)
            } else {
                current
            }
        }
    }

    private fun handleGeneratePlan() {
        val exerciseIds = _state.value.selectedExercises.map { it.exerciseId }
        if (exerciseIds.isEmpty()) return

        _sideEffect.trySend(WorkoutSetupSideEffect.NavigateToPlanReview(exerciseIds))
    }

    private fun handleLoadTemplate(templateId: Long) {
        viewModelScope.launch {
            try {
                val template = templateRepository.getById(templateId) ?: return@launch
                val templateExercises = templateRepository.getExercisesForTemplate(templateId)
                val exerciseIds = templateExercises.map { it.exerciseId }
                val exercises = exerciseRepository.getExercisesByIds(exerciseIds)

                // Preserve the template's ordering
                val exerciseMap = exercises.associateBy { it.id }
                val orderedItems = templateExercises
                    .sortedBy { it.orderIndex }
                    .mapIndexedNotNull { index, templateExercise ->
                        exerciseMap[templateExercise.exerciseId]?.toOrderItem(index)
                    }

                _state.update { current ->
                    current.copy(
                        selectedExercises = orderedItems,
                        isFromTemplate = true,
                        templateName = template.name,
                        error = null,
                    )
                }
                _sideEffect.trySend(WorkoutSetupSideEffect.NavigateToExerciseOrder)
            } catch (_: Exception) {
                _state.update {
                    it.copy(error = WorkoutSetupError.ExerciseLoadFailed)
                }
                _sideEffect.trySend(
                    WorkoutSetupSideEffect.ShowError("Failed to load template."),
                )
            }
        }
    }

    private fun handleRetry() {
        _state.update { it.copy(error = null) }
    }

    private fun handleReset() {
        _state.value = WorkoutSetupUiState()
    }

    /**
     * Returns the list of selected group IDs for passing to ExerciseSelectionScreen.
     * Maps MuscleGroup enum ordinal to 1-indexed database ID.
     */
    fun getSelectedGroupIds(): List<Long> =
        _state.value.selectedGroups.map { (it.ordinal + 1).toLong() }
}

private fun Exercise.toOrderItem(index: Int): ExerciseOrderItem = ExerciseOrderItem(
    exerciseId = id,
    name = name,
    equipment = equipment.value,
    difficulty = difficulty.value,
    orderIndex = index,
)
