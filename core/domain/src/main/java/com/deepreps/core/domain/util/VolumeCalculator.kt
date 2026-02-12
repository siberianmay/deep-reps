package com.deepreps.core.domain.util

import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType

/**
 * Volume calculations for workout analytics.
 *
 * - Per-exercise volume: count of working sets (warmup excluded).
 * - Per-group volume: sum of per-exercise volumes for all exercises in a group.
 * - Per-session volume: total working sets across all exercises.
 * - Tonnage: sum of (weight * reps) for all working sets.
 *
 * Pure Kotlin, no dependencies.
 */
object VolumeCalculator {

    /**
     * Counts working sets for a single exercise.
     * Warmup sets are excluded. Only completed sets are counted.
     */
    fun workingSetsForExercise(sets: List<WorkoutSet>): Int =
        sets.count { it.type == SetType.WORKING && it.status.isCompleted() }

    /**
     * Counts total working sets across all exercises in a session.
     *
     * @param exerciseSets map of exerciseId to its list of sets.
     */
    fun workingSetsForSession(exerciseSets: Map<Long, List<WorkoutSet>>): Int =
        exerciseSets.values.sumOf { sets -> workingSetsForExercise(sets) }

    /**
     * Counts total working sets for a muscle group.
     *
     * @param exerciseSets map of exerciseId to its list of sets (pre-filtered to group).
     */
    fun workingSetsForGroup(exerciseSets: Map<Long, List<WorkoutSet>>): Int =
        workingSetsForSession(exerciseSets)

    /**
     * Calculates tonnage (total volume load) for a list of sets.
     *
     * Tonnage = sum of (actual_weight * actual_reps) for all completed working sets.
     * Warmup sets are excluded. Sets without actual weight/reps are excluded.
     *
     * @return tonnage in kg.
     */
    fun tonnage(sets: List<WorkoutSet>): Double =
        sets.filter { it.type == SetType.WORKING && it.status.isCompleted() }
            .sumOf { set ->
                val weight = set.actualWeightKg ?: 0.0
                val reps = set.actualReps ?: 0
                weight * reps
            }

    /**
     * Calculates tonnage across multiple exercises.
     */
    fun sessionTonnage(exerciseSets: Map<Long, List<WorkoutSet>>): Double =
        exerciseSets.values.sumOf { sets -> tonnage(sets) }

    /**
     * Extension to check if a SetStatus represents completion.
     * Uses string comparison against the enum value to avoid coupling to SetStatus internals.
     */
    private fun SetStatus.isCompleted(): Boolean =
        this == SetStatus.COMPLETED
}
