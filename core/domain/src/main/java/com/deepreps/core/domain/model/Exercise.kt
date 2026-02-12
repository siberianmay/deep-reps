package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType

/**
 * Domain representation of an exercise in the exercise library.
 *
 * All 78 exercises are pre-populated. No user-created exercises at launch.
 * [stableId] is the CSCS-defined string ID used in AI prompts (e.g., "legs_barbell_squat").
 * [orderPriority] is distinct from [displayOrder]: orderPriority drives the auto-ordering
 * algorithm, displayOrder drives the library browsing UI.
 */
data class Exercise(
    val id: Long,
    val stableId: String,
    val name: String,
    val description: String,
    val equipment: Equipment,
    val movementType: MovementType,
    val difficulty: Difficulty,
    val primaryGroupId: Long,
    val secondaryMuscles: List<String>,
    val tips: List<String>,
    val pros: List<String>,
    val displayOrder: Int,
    val orderPriority: Int,
    val supersetTags: List<String>,
    val autoProgramMinLevel: Int,
)
