package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.MovementType

/**
 * Orders exercises according to the CSCS auto-ordering algorithm with
 * round-robin interleaving across muscle groups.
 *
 * Per exercise-science.md Section 6:
 * 1. Non-core exercises are grouped by [Exercise.primaryGroupId]
 * 2. Within each group: compounds before isolations, then by orderPriority,
 *    then by difficulty (advanced first)
 * 3. Groups are interleaved via round-robin (largest group first)
 * 4. Core exercises always last (sorted: compounds first, then by difficulty)
 *
 * This is a default ordering. The user can freely reorder after.
 * Pure Kotlin, no dependencies.
 */
class OrderExercisesUseCase {

    operator fun invoke(exercises: List<Exercise>): List<Exercise> {
        val (coreExercises, nonCoreExercises) = exercises.partition { exercise ->
            groupPriority(exercise.primaryGroupId) == CORE_PRIORITY
        }

        val interleaved = interleaveByMuscleGroup(nonCoreExercises)
        val sortedCore = coreExercises.sortedWith(coreComparator)

        return interleaved + sortedCore
    }

    /**
     * Groups exercises by [Exercise.primaryGroupId], sorts each group internally
     * (compounds first, then orderPriority, then difficulty), and interleaves
     * across groups via round-robin.
     *
     * Groups are ordered by size descending so that the largest muscle group
     * leads each round. Ties in size are broken by the first exercise's
     * orderPriority (lower = earlier) for deterministic output.
     */
    private fun interleaveByMuscleGroup(exercises: List<Exercise>): List<Exercise> {
        if (exercises.isEmpty()) return emptyList()

        val groupQueues: List<ArrayDeque<Exercise>> = exercises
            .groupBy { it.primaryGroupId }
            .values
            .map { group -> ArrayDeque(group.sortedWith(withinGroupComparator)) }
            .sortedWith(
                compareByDescending<ArrayDeque<Exercise>> { it.size }
                    .thenBy { it.first().orderPriority }
            )

        val result = mutableListOf<Exercise>()
        while (groupQueues.any { it.isNotEmpty() }) {
            for (queue in groupQueues) {
                if (queue.isNotEmpty()) {
                    result.add(queue.removeFirst())
                }
            }
        }
        return result
    }

    /**
     * Sorts exercises within a single muscle group:
     * compounds first, then by orderPriority, then by difficulty (advanced first).
     */
    private val withinGroupComparator: Comparator<Exercise> =
        compareBy<Exercise> { exercise ->
            if (exercise.movementType == MovementType.COMPOUND) 0 else 1
        }
            .thenBy { exercise -> exercise.orderPriority }
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
