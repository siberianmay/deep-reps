package com.deepreps.feature.progress

import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * UI state for the read-only session detail screen.
 */
data class SessionDetailUiState(
    val dateText: String = "",
    val durationText: String = "",
    val totalVolumeKg: Double = 0.0,
    val totalSets: Int = 0,
    val exercises: List<SessionExerciseUi> = emptyList(),
    val notes: String? = null,
    val weightUnit: WeightUnit = WeightUnit.KG,
    val isLoading: Boolean = true,
    val errorType: SessionDetailError? = null,
)

/**
 * Exercise in a past session with its sets.
 */
data class SessionExerciseUi(
    val exerciseId: Long,
    val exerciseName: String,
    val sets: List<SessionSetUi>,
    val notes: String?,
)

/**
 * A single set in a past session, read-only.
 */
data class SessionSetUi(
    val setNumber: Int,
    val type: SetType,
    val weightKg: Double?,
    val reps: Int?,
    val isPersonalRecord: Boolean,
)

/**
 * Typed errors for the session detail screen.
 */
sealed interface SessionDetailError {
    data object LoadFailed : SessionDetailError
    data object NotFound : SessionDetailError
}
