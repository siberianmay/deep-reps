package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.MovementType

/**
 * Orders exercises according to the CSCS auto-ordering algorithm.
 *
 * Per exercise-science.md Section 6:
 * 1. Compounds before isolations (non-core)
 * 2. Within same type: large muscle groups before small (by group priority)
 * 3. Within same group and type: advanced before intermediate before beginner
 * 4. Core exercises always last (regardless of compound/isolation)
 *
 * This is a default ordering. The user can freely reorder after.
 * Pure Kotlin, no dependencies.
 */
class OrderExercisesUseCase {

    operator fun invoke(exercises: List<Exercise>): List<Exercise> {
        val (coreExercises, nonCoreExercises) = exercises.partition { exercise ->
            groupPriority(exercise.primaryGroupId) == CORE_PRIORITY
        }

        val (nonCoreCompounds, nonCoreIsolations) = nonCoreExercises.partition { exercise ->
            exercise.movementType == MovementType.COMPOUND
        }

        val sortedCompounds = nonCoreCompounds.sortedWith(exerciseComparator)
        val sortedIsolations = nonCoreIsolations.sortedWith(exerciseComparator)
        val sortedCore = coreExercises.sortedWith(coreComparator)

        return sortedCompounds + sortedIsolations + sortedCore
    }

    private val exerciseComparator: Comparator<Exercise> =
        compareBy<Exercise> { exercise -> exercise.orderPriority }
            .thenBy { exercise -> difficultySort(exercise.difficulty) }

    private val coreComparator: Comparator<Exercise> =
        compareBy<Exercise> { exercise ->
            if (exercise.movementType == MovementType.COMPOUND) 0 else 1
        }.thenBy { exercise -> difficultySort(exercise.difficulty) }

    companion object {
        private const val CORE_PRIORITY = 7

        /**
         * Maps group IDs to ordering priorities. Lower = earlier.
         *
         * This uses [Exercise.orderPriority] from the database as the primary sort,
         * but when that field is not set or equal, group-based priority is the tiebreaker.
         * The CSCS-assigned orderPriority on each exercise already encodes these rules,
         * so this function is only needed for the core-last separation.
         */
        private fun groupPriority(primaryGroupId: Long): Int {
            // Group IDs 1-7 map directly to the priority table in exercise-science Section 6.1.
            // The actual mapping depends on the pre-populated database ordering.
            // Core is always group ID 7 in the pre-populated database.
            return primaryGroupId.toInt()
        }

        /**
         * Advanced (1) before Intermediate (2) before Beginner (3).
         * Harder exercises demand more neural freshness.
         */
        private fun difficultySort(difficulty: Difficulty): Int = when (difficulty) {
            Difficulty.ADVANCED -> 1
            Difficulty.INTERMEDIATE -> 2
            Difficulty.BEGINNER -> 3
        }
    }
}
