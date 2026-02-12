package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.repository.TemplateRepository
import javax.inject.Inject

/**
 * Saves a workout template with its exercise list and ordering.
 *
 * Used from:
 * - Template creation screen (new blank template or from workout summary)
 * - Template edit screen (updates existing template)
 *
 * Validation: name must be 1-60 chars, exercises must be 1-15 items.
 */
class SaveTemplateUseCase @Inject constructor(
    private val templateRepository: TemplateRepository,
) {
    /**
     * Creates a new template with the given name and ordered exercise IDs.
     *
     * @param name Template display name (1-60 characters).
     * @param exerciseIds Ordered list of exercise IDs (1-15 items).
     * @param muscleGroupIds List of muscle group IDs this template targets.
     * @return The generated template ID.
     * @throws InvalidTemplateException if validation fails.
     */
    suspend operator fun invoke(
        name: String,
        exerciseIds: List<Long>,
        muscleGroupIds: List<Long>,
    ): Long {
        validate(name, exerciseIds)

        val now = System.currentTimeMillis()
        val template = Template(
            id = 0,
            name = name.trim(),
            createdAt = now,
            updatedAt = now,
            muscleGroups = muscleGroupIds,
        )

        val templateId = templateRepository.save(template)

        val exercises = exerciseIds.mapIndexed { index, exerciseId ->
            TemplateExercise(
                id = 0,
                templateId = templateId,
                exerciseId = exerciseId,
                orderIndex = index,
            )
        }
        templateRepository.saveExercises(templateId, exercises)

        return templateId
    }

    /**
     * Updates an existing template's name and exercises.
     *
     * @param templateId The ID of the template to update.
     * @param name Updated display name (1-60 characters).
     * @param exerciseIds Updated ordered list of exercise IDs (1-15 items).
     * @param muscleGroupIds Updated list of muscle group IDs.
     * @throws InvalidTemplateException if validation fails.
     */
    suspend fun update(
        templateId: Long,
        name: String,
        exerciseIds: List<Long>,
        muscleGroupIds: List<Long>,
    ) {
        validate(name, exerciseIds)

        val existing = templateRepository.getById(templateId)
            ?: throw InvalidTemplateException("Template not found")

        val updated = existing.copy(
            name = name.trim(),
            updatedAt = System.currentTimeMillis(),
            muscleGroups = muscleGroupIds,
        )
        templateRepository.update(updated)

        val exercises = exerciseIds.mapIndexed { index, exerciseId ->
            TemplateExercise(
                id = 0,
                templateId = templateId,
                exerciseId = exerciseId,
                orderIndex = index,
            )
        }
        templateRepository.saveExercises(templateId, exercises)
    }

    private fun validate(name: String, exerciseIds: List<Long>) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_NAME_LENGTH) {
            throw InvalidTemplateException(
                "Template name must be 1-$MAX_NAME_LENGTH characters",
            )
        }
        if (exerciseIds.isEmpty() || exerciseIds.size > MAX_EXERCISES) {
            throw InvalidTemplateException(
                "Template must have 1-$MAX_EXERCISES exercises",
            )
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 60
        const val MAX_EXERCISES = 15
    }
}

/**
 * Exception thrown when template validation fails.
 */
class InvalidTemplateException(message: String) : IllegalArgumentException(message)
