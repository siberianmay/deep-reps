package com.deepreps.core.domain.model

/**
 * Domain representation of a muscle group.
 *
 * Maps 1:1 to the 7 canonical groups: Legs, Lower Back, Chest, Back, Shoulders, Arms, Core.
 * The [displayOrder] controls the order shown in the UI (muscle group selector).
 */
@Suppress("ForbiddenPublicDataClass")
data class MuscleGroupModel(
    val id: Long,
    val name: String,
    val displayOrder: Int,
)
