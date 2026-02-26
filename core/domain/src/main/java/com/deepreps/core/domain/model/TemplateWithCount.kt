package com.deepreps.core.domain.model

/**
 * A template with its exercise count, used for displaying template lists without N+1 queries.
 */
data class TemplateWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val muscleGroups: List<Long>,
    val exerciseCount: Int,
)
