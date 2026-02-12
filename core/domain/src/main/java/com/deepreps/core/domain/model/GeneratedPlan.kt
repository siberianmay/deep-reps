package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.SetType

/**
 * Represents a complete AI-generated (or baseline-generated) workout plan.
 *
 * Contains the ordered list of exercise plans with their prescribed sets.
 * This is the output of [AiPlanProvider.generatePlan] and the offline fallback chain.
 */
data class GeneratedPlan(
    val exercises: List<ExercisePlan>,
)

/**
 * Plan for a single exercise within a generated plan.
 *
 * [exerciseId] is the Room PK for internal use.
 * [stableId] is the CSCS stable ID used in AI prompts.
 * [sets] contains both warmup and working sets in order.
 */
data class ExercisePlan(
    val exerciseId: Long,
    val stableId: String,
    val exerciseName: String,
    val sets: List<PlannedSet>,
    val restSeconds: Int = 90,
    val notes: String? = null,
)

/**
 * A single planned set within an exercise plan.
 *
 * [weight] is in kg. [reps] is the target rep count.
 * [setType] distinguishes warmup from working sets.
 */
data class PlannedSet(
    val setType: SetType,
    val weight: Double,
    val reps: Int,
    val restSeconds: Int = 90,
)
