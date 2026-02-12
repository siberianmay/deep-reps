package com.deepreps.feature.templates

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deepreps.core.domain.model.enums.MuscleGroup
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.usecase.InvalidTemplateException
import com.deepreps.core.domain.usecase.SaveTemplateUseCase
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
 * ViewModel for creating or editing a workout template.
 *
 * Supports two entry paths:
 * 1. New template: blank state or pre-populated from completed workout exercises.
 * 2. Edit existing: loads template data from repository.
 *
 * Navigation argument "templateId" controls edit mode.
 * Navigation argument "exerciseIds" pre-populates from workout summary path.
 */
@HiltViewModel
class CreateTemplateViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saveTemplateUseCase: SaveTemplateUseCase,
    private val templateRepository: TemplateRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CreateTemplateUiState())
    val state: StateFlow<CreateTemplateUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<CreateTemplateSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<CreateTemplateSideEffect> = _sideEffect.receiveAsFlow()

    private val templateId: Long? = savedStateHandle.get<Long>(TEMPLATE_ID_ARG)
    private val prePopulatedExerciseIds: String? =
        savedStateHandle.get<String>(EXERCISE_IDS_ARG)

    init {
        if (templateId != null && templateId > 0) {
            loadExistingTemplate(templateId)
        } else if (prePopulatedExerciseIds != null) {
            loadExercisesFromIds(prePopulatedExerciseIds)
        }
    }

    fun onIntent(intent: CreateTemplateIntent) {
        when (intent) {
            is CreateTemplateIntent.UpdateName -> handleUpdateName(intent.name)
            is CreateTemplateIntent.Save -> handleSave()
            is CreateTemplateIntent.RemoveExercise -> handleRemoveExercise(intent.exerciseId)
            is CreateTemplateIntent.MoveExercise -> handleMoveExercise(
                intent.fromIndex,
                intent.toIndex,
            )
            is CreateTemplateIntent.Close -> handleClose()
        }
    }

    private fun handleUpdateName(name: String) {
        _state.update {
            it.copy(
                name = name,
                nameError = null,
            )
        }
    }

    private fun handleSave() {
        val current = _state.value

        // Validate name
        val trimmedName = current.name.trim()
        if (trimmedName.isEmpty()) {
            _state.update { it.copy(nameError = "Template name required") }
            return
        }
        if (trimmedName.length > CreateTemplateUiState.MAX_NAME_LENGTH) {
            _state.update {
                it.copy(nameError = "Name must be ${CreateTemplateUiState.MAX_NAME_LENGTH} characters or less")
            }
            return
        }

        // Validate exercises
        if (current.exercises.isEmpty()) {
            _state.update { it.copy(exerciseError = "Add at least one exercise") }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            try {
                val exerciseIds = current.exercises
                    .sortedBy { it.orderIndex }
                    .map { it.exerciseId }

                val muscleGroupIds = computeMuscleGroupIds(exerciseIds)

                if (current.isEditing && current.templateId != null) {
                    saveTemplateUseCase.update(
                        templateId = current.templateId,
                        name = trimmedName,
                        exerciseIds = exerciseIds,
                        muscleGroupIds = muscleGroupIds,
                    )
                } else {
                    saveTemplateUseCase(
                        name = trimmedName,
                        exerciseIds = exerciseIds,
                        muscleGroupIds = muscleGroupIds,
                    )
                }

                _state.update { it.copy(isSaving = false) }
                _sideEffect.trySend(
                    CreateTemplateSideEffect.TemplateSaved("Template saved"),
                )
            } catch (e: InvalidTemplateException) {
                _state.update { it.copy(isSaving = false) }
                _sideEffect.trySend(
                    CreateTemplateSideEffect.ShowError(e.message ?: "Validation failed"),
                )
            } catch (_: Exception) {
                _state.update { it.copy(isSaving = false) }
                _sideEffect.trySend(
                    CreateTemplateSideEffect.ShowError("Failed to save template"),
                )
            }
        }
    }

    private fun handleRemoveExercise(exerciseId: Long) {
        _state.update { current ->
            val updated = current.exercises
                .filter { it.exerciseId != exerciseId }
                .mapIndexed { index, exercise ->
                    exercise.copy(orderIndex = index)
                }
            val muscleGroupNames = computeMuscleGroupNamesSync(updated)
            current.copy(
                exercises = updated,
                muscleGroupNames = muscleGroupNames,
                exerciseError = null,
            )
        }
    }

    private fun handleMoveExercise(fromIndex: Int, toIndex: Int) {
        _state.update { current ->
            val exercises = current.exercises.toMutableList()
            if (fromIndex in exercises.indices && toIndex in exercises.indices) {
                val item = exercises.removeAt(fromIndex)
                exercises.add(toIndex, item)
                val reindexed = exercises.mapIndexed { index, exercise ->
                    exercise.copy(orderIndex = index)
                }
                current.copy(exercises = reindexed)
            } else {
                current
            }
        }
    }

    private fun handleClose() {
        _sideEffect.trySend(CreateTemplateSideEffect.NavigateBack)
    }

    private fun loadExistingTemplate(id: Long) {
        viewModelScope.launch {
            try {
                val template = templateRepository.getById(id) ?: return@launch
                val templateExercises = templateRepository.getExercisesForTemplate(id)
                val exerciseIds = templateExercises.map { it.exerciseId }
                val exercises = exerciseRepository.getExercisesByIds(exerciseIds)
                val exerciseMap = exercises.associateBy { it.id }

                val exerciseUiList = templateExercises
                    .sortedBy { it.orderIndex }
                    .mapIndexedNotNull { index, te ->
                        exerciseMap[te.exerciseId]?.let { exercise ->
                            TemplateExerciseUi(
                                exerciseId = exercise.id,
                                name = exercise.name,
                                orderIndex = index,
                            )
                        }
                    }

                val muscleGroupNames = template.muscleGroups.mapNotNull { groupId ->
                    TemplateListViewModel.muscleGroupNameFromId(groupId)
                }

                _state.update {
                    it.copy(
                        templateId = id,
                        name = template.name,
                        exercises = exerciseUiList,
                        muscleGroupNames = muscleGroupNames,
                        isEditing = true,
                    )
                }
            } catch (_: Exception) {
                _sideEffect.trySend(
                    CreateTemplateSideEffect.ShowError("Failed to load template"),
                )
            }
        }
    }

    private fun loadExercisesFromIds(idsString: String) {
        viewModelScope.launch {
            try {
                val ids = idsString.split(",")
                    .mapNotNull { it.trim().toLongOrNull() }

                if (ids.isEmpty()) return@launch

                val exercises = exerciseRepository.getExercisesByIds(ids)
                val exerciseUiList = exercises.mapIndexed { index, exercise ->
                    TemplateExerciseUi(
                        exerciseId = exercise.id,
                        name = exercise.name,
                        orderIndex = index,
                    )
                }

                val muscleGroupNames = computeMuscleGroupNamesSync(exerciseUiList)

                _state.update {
                    it.copy(
                        exercises = exerciseUiList,
                        muscleGroupNames = muscleGroupNames,
                    )
                }
            } catch (_: Exception) {
                _sideEffect.trySend(
                    CreateTemplateSideEffect.ShowError("Failed to load exercises"),
                )
            }
        }
    }

    /**
     * Computes muscle group IDs from exercise IDs by looking up each
     * exercise's primaryGroupId.
     */
    private suspend fun computeMuscleGroupIds(exerciseIds: List<Long>): List<Long> {
        val exercises = exerciseRepository.getExercisesByIds(exerciseIds)
        return exercises.map { it.primaryGroupId }.distinct()
    }

    /**
     * Synchronous version for state updates where we don't have
     * access to the full exercise details.
     * Returns cached group names based on current exercise data.
     */
    private fun computeMuscleGroupNamesSync(
        exercises: List<TemplateExerciseUi>,
    ): List<String> {
        // Without primaryGroupId in TemplateExerciseUi, we preserve existing muscle group names.
        // The authoritative muscle group computation happens in handleSave via exerciseRepository.
        return _state.value.muscleGroupNames
    }

    companion object {
        const val TEMPLATE_ID_ARG = "templateId"
        const val EXERCISE_IDS_ARG = "exerciseIds"
    }
}
