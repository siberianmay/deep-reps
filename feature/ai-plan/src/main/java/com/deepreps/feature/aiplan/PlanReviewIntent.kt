package com.deepreps.feature.aiplan

/**
 * MVI intents for the Plan Review screen.
 *
 * Processed sequentially by [PlanReviewViewModel.onIntent].
 */
sealed interface PlanReviewIntent {
    /** Initial load -- triggers plan generation via fallback chain. */
    data class LoadPlan(
        val exerciseIds: List<Long>,
    ) : PlanReviewIntent

    /** User changed the weight for a specific set. */
    data class UpdateWeight(
        val exerciseIndex: Int,
        val setIndex: Int,
        val newWeight: Double,
    ) : PlanReviewIntent

    /** User changed the reps for a specific set. */
    data class UpdateReps(
        val exerciseIndex: Int,
        val setIndex: Int,
        val newReps: Int,
    ) : PlanReviewIntent

    /** User confirmed the plan and wants to start the workout. */
    data object ConfirmPlan : PlanReviewIntent

    /** User wants to regenerate the plan (retry AI). */
    data object RegeneratePlan : PlanReviewIntent

    /** User dismissed safety warnings. */
    data object DismissSafetyWarnings : PlanReviewIntent

    /** User tapped [+] to add a working set to an exercise. */
    data class AddWorkingSet(val exerciseIndex: Int) : PlanReviewIntent

    /** User tapped [-] to remove the last working set from an exercise. */
    data class RemoveLastWorkingSet(val exerciseIndex: Int) : PlanReviewIntent
}
