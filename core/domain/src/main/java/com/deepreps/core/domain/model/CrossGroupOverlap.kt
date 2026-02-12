package com.deepreps.core.domain.model

/**
 * Describes a detected overlap between muscle groups selected for the same session.
 *
 * Fed into the AI prompt's CROSS-GROUP FATIGUE section so the LLM can
 * reduce redundant volume for shared muscles.
 */
data class CrossGroupOverlap(
    val primaryGroup: String,
    val overlappingGroup: String,
    val sharedMuscles: List<String>,
    val description: String,
)
