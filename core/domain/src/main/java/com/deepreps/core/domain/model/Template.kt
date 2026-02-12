package com.deepreps.core.domain.model

/**
 * Domain representation of a saved workout template.
 *
 * [muscleGroups] stores the list of muscle group IDs that this template targets.
 * Stored as a JSON array in Room; deserialized to a list in the domain model.
 */
data class Template(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val muscleGroups: List<Long>,
)
