package com.deepreps.core.domain.model

/**
 * Domain representation of an exercise within a template.
 *
 * Defines the default exercise order when loading a template.
 */
@Suppress("ForbiddenPublicDataClass")
data class TemplateExercise(
    val id: Long,
    val templateId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
)
