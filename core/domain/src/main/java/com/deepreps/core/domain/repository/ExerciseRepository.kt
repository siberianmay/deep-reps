package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.MuscleGroupModel
import kotlinx.coroutines.flow.Flow

/**
 * Repository for exercise library data.
 *
 * The exercise library is pre-populated and read-only at launch. No user-created exercises.
 * All queries are observable via Flow so the UI reacts to any future library updates.
 */
interface ExerciseRepository {

    /** Returns all 7 muscle groups ordered by [MuscleGroupModel.displayOrder]. */
    fun getMuscleGroups(): Flow<List<MuscleGroupModel>>

    /** Observe all exercises ordered by display order. */
    fun getAllExercises(): Flow<List<Exercise>>

    /** Returns all exercises for a given muscle group, ordered by display order. */
    fun getExercisesByGroup(groupId: Long): Flow<List<Exercise>>

    /** Returns a single exercise by its Room PK. Null if not found. */
    suspend fun getExerciseById(id: Long): Exercise?

    /** Returns multiple exercises by their Room PKs. */
    suspend fun getExercisesByIds(ids: List<Long>): List<Exercise>

    /** Searches exercises by name (case-insensitive partial match). */
    fun searchExercises(query: String): Flow<List<Exercise>>

    /** Returns exercises for a group, including their muscle group cross-references. */
    fun getExercisesWithMuscles(groupId: Long): Flow<List<Exercise>>
}
