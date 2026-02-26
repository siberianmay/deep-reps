package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.TemplateWithCount
import kotlinx.coroutines.flow.Flow

/**
 * Repository for workout templates.
 *
 * Templates are user-created presets that store an exercise selection and ordering.
 */
interface TemplateRepository {

    /** Returns all templates, most recently updated first. */
    fun getAll(): Flow<List<Template>>

    /**
     * Returns all templates with their exercise counts, most recently updated first.
     * Avoids N+1 queries by using a single JOIN + GROUP BY operation.
     */
    fun getAllWithExerciseCount(): Flow<List<TemplateWithCount>>

    /** Returns a template by ID. Null if not found. */
    suspend fun getById(id: Long): Template?

    /** Creates a new template and returns its generated ID. */
    suspend fun save(template: Template): Long

    /** Updates an existing template. */
    suspend fun update(template: Template)

    /** Deletes a template and its associated exercises (cascade). */
    suspend fun delete(template: Template)

    /** Returns exercises for a template, ordered by orderIndex. */
    suspend fun getExercisesForTemplate(templateId: Long): List<TemplateExercise>

    /** Observe exercises for a template reactively. */
    fun observeExercisesForTemplate(templateId: Long): Flow<List<TemplateExercise>>

    /** Replaces all exercises for a template (delete + insert in a transaction). */
    suspend fun saveExercises(templateId: Long, exercises: List<TemplateExercise>)
}
