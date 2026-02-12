package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType

/**
 * Represents a single set in a workout exercise.
 *
 * All weights are stored in kilograms. UI layer handles unit conversion.
 * [id] is the Room auto-generated PK. Zero for not-yet-persisted sets.
 * [completedAt] is epoch millis when the set was marked done. Null until completion.
 */
data class WorkoutSet(
    val id: Long = 0,
    val setNumber: Int,
    val type: SetType,
    val status: SetStatus,
    val plannedWeightKg: Double?,
    val plannedReps: Int?,
    val actualWeightKg: Double?,
    val actualReps: Int?,
    val completedAt: Long? = null,
    val isPersonalRecord: Boolean = false,
)
