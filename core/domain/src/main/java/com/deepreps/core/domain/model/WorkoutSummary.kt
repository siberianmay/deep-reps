@file:Suppress("ForbiddenPublicDataClass")

package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.RecordType

/**
 * Domain model for a completed workout summary.
 *
 * All weight values are in kg. UI layer handles unit conversion.
 * [personalRecords] contains only records detected in this specific session.
 */
data class WorkoutSummary(
    val sessionId: Long,
    val durationSeconds: Long,
    val exerciseCount: Int,
    val totalWorkingSets: Int,
    val totalTonnageKg: Double,
    val perGroupVolume: List<GroupVolume>,
    val personalRecords: List<DetectedPr>,
)

/**
 * Working set volume for a single muscle group.
 */
data class GroupVolume(
    val groupName: String,
    val workingSets: Int,
    val tonnageKg: Double,
)

/**
 * A personal record detected during workout completion.
 */
data class DetectedPr(
    val exerciseId: Long,
    val exerciseName: String,
    val weightKg: Double,
    val reps: Int,
    val recordType: RecordType,
)
