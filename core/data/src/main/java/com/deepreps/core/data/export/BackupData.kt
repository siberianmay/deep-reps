package com.deepreps.core.data.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level JSON structure for backup files.
 *
 * Field names match the Kotlin property names of the corresponding Room entity classes
 * so kotlinx.serialization maps directly without custom adapters. The JSON keys use
 * camelCase for entity fields (matching Kotlin properties) and snake_case for top-level
 * backup metadata.
 */
@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupData(
    val version: Int,
    val exportedAt: String,
    val dbVersion: Int,
    val userProfile: BackupUserProfile? = null,
    val workoutSessions: List<BackupWorkoutSession> = emptyList(),
    val workoutExercises: List<BackupWorkoutExercise> = emptyList(),
    val workoutSets: List<BackupWorkoutSet> = emptyList(),
    val templates: List<BackupTemplate> = emptyList(),
    val templateExercises: List<BackupTemplateExercise> = emptyList(),
    val personalRecords: List<BackupPersonalRecord> = emptyList(),
    val bodyWeightEntries: List<BackupBodyWeightEntry> = emptyList(),
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupUserProfile(
    val id: Long = 1,
    val experienceLevel: Int,
    val preferredUnit: String,
    val age: Int? = null,
    val heightCm: Double? = null,
    val gender: String? = null,
    val bodyWeightKg: Double? = null,
    val compoundRepMin: Int = 6,
    val compoundRepMax: Int = 10,
    val isolationRepMin: Int = 10,
    val isolationRepMax: Int = 15,
    val defaultWorkingSets: Int = 0,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupWorkoutSession(
    val id: Long,
    val startedAt: Long,
    val completedAt: Long? = null,
    val durationSeconds: Long? = null,
    val pausedDurationSeconds: Long = 0,
    val status: String,
    val notes: String? = null,
    val templateId: Long? = null,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupWorkoutExercise(
    val id: Long,
    val sessionId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
    val supersetGroupId: Int? = null,
    val restTimerSeconds: Int? = null,
    val notes: String? = null,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupWorkoutSet(
    val id: Long,
    val workoutExerciseId: Long,
    val setIndex: Int,
    val setType: String,
    val plannedWeight: Double? = null,
    val plannedReps: Int? = null,
    val actualWeight: Double? = null,
    val actualReps: Int? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val status: String = "planned",
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupTemplate(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    @SerialName("muscleGroupsJson")
    val muscleGroupsJson: String,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupTemplateExercise(
    val id: Long,
    val templateId: Long,
    val exerciseId: Long,
    val orderIndex: Int,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupPersonalRecord(
    val id: Long,
    val exerciseId: Long,
    val recordType: String,
    val weightValue: Double? = null,
    val reps: Int? = null,
    @SerialName("estimated1rm")
    val estimated1rm: Double? = null,
    val achievedAt: Long,
    val sessionId: Long? = null,
)

@Serializable
@Suppress("ForbiddenPublicDataClass")
data class BackupBodyWeightEntry(
    val id: Long,
    val weightValue: Double,
    val recordedAt: Long,
)
