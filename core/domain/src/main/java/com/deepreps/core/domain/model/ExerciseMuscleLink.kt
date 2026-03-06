package com.deepreps.core.domain.model

/**
 * Represents the relationship between an exercise and a muscle group.
 *
 * Maps directly from the `exercise_muscles` junction table.
 * Used to determine primary vs secondary muscle group highlighting.
 */
@Suppress("ForbiddenPublicDataClass")
data class ExerciseMuscleLink(
    val muscleGroupId: Long,
    val isPrimary: Boolean,
)
