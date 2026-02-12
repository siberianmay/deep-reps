package com.deepreps.feature.aiplan

import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.SafetyViolation

/**
 * MVI state for the Plan Review screen.
 *
 * Represents the complete, immutable UI state at any point in time.
 * The screen renders from this single object -- no partial updates.
 */
@Suppress("ForbiddenPublicDataClass")
data class PlanReviewUiState(
    val phase: Phase = Phase.Loading,
    val exercisePlans: List<EditableExercisePlan> = emptyList(),
    val planSource: PlanSource = PlanSource.LOADING,
    val safetyViolations: List<SafetyViolation> = emptyList(),
    val showSafetyWarnings: Boolean = false,
) {

    enum class Phase {
        Loading,
        Generating,
        PlanReady,
        Error,
    }

    enum class PlanSource {
        LOADING,
        AI_GENERATED,
        CACHED,
        BASELINE,
        MANUAL,
    }
}

/**
 * An exercise plan with editable weight/reps fields.
 *
 * The user can modify any weight/rep value on the plan review screen
 * before starting the workout.
 */
@Suppress("ForbiddenPublicDataClass")
data class EditableExercisePlan(
    val exerciseId: Long,
    val stableId: String,
    val exerciseName: String,
    val sets: List<EditableSet>,
    val restSeconds: Int,
    val notes: String?,
) {
    companion object {
        fun fromDomain(plan: ExercisePlan): EditableExercisePlan = EditableExercisePlan(
            exerciseId = plan.exerciseId,
            stableId = plan.stableId,
            exerciseName = plan.exerciseName,
            sets = plan.sets.mapIndexed { index, set ->
                EditableSet(
                    index = index,
                    setType = set.setType.value,
                    weight = set.weight,
                    reps = set.reps,
                    restSeconds = set.restSeconds,
                )
            },
            restSeconds = plan.restSeconds,
            notes = plan.notes,
        )
    }
}

@Suppress("ForbiddenPublicDataClass")
data class EditableSet(
    val index: Int,
    val setType: String,
    val weight: Double,
    val reps: Int,
    val restSeconds: Int,
)
