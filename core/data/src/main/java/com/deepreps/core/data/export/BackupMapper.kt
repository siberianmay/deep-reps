package com.deepreps.core.data.export

import com.deepreps.core.database.entity.BodyWeightEntryEntity
import com.deepreps.core.database.entity.PersonalRecordEntity
import com.deepreps.core.database.entity.TemplateEntity
import com.deepreps.core.database.entity.TemplateExerciseEntity
import com.deepreps.core.database.entity.UserProfileEntity
import com.deepreps.core.database.entity.WorkoutExerciseEntity
import com.deepreps.core.database.entity.WorkoutSessionEntity
import com.deepreps.core.database.entity.WorkoutSetEntity

// --- Backup → Entity ---

internal fun BackupUserProfile.toEntity(): UserProfileEntity =
    UserProfileEntity(
        id = id,
        experienceLevel = experienceLevel,
        preferredUnit = preferredUnit,
        age = age,
        heightCm = heightCm,
        gender = gender,
        bodyWeightKg = bodyWeightKg,
        compoundRepMin = compoundRepMin,
        compoundRepMax = compoundRepMax,
        isolationRepMin = isolationRepMin,
        isolationRepMax = isolationRepMax,
        defaultWorkingSets = defaultWorkingSets,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun BackupWorkoutSession.toEntity(): WorkoutSessionEntity =
    WorkoutSessionEntity(
        id = id,
        startedAt = startedAt,
        completedAt = completedAt,
        durationSeconds = durationSeconds,
        pausedDurationSeconds = pausedDurationSeconds,
        status = status,
        notes = notes,
        templateId = templateId,
    )

internal fun BackupWorkoutExercise.toEntity(): WorkoutExerciseEntity =
    WorkoutExerciseEntity(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        orderIndex = orderIndex,
        supersetGroupId = supersetGroupId,
        restTimerSeconds = restTimerSeconds,
        notes = notes,
    )

internal fun BackupWorkoutSet.toEntity(): WorkoutSetEntity =
    WorkoutSetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setIndex = setIndex,
        setType = setType,
        plannedWeight = plannedWeight,
        plannedReps = plannedReps,
        actualWeight = actualWeight,
        actualReps = actualReps,
        isCompleted = isCompleted,
        completedAt = completedAt,
        status = status,
    )

internal fun BackupTemplate.toEntity(): TemplateEntity =
    TemplateEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        muscleGroupsJson = muscleGroupsJson,
    )

internal fun BackupTemplateExercise.toEntity(): TemplateExerciseEntity =
    TemplateExerciseEntity(
        id = id,
        templateId = templateId,
        exerciseId = exerciseId,
        orderIndex = orderIndex,
    )

internal fun BackupPersonalRecord.toEntity(): PersonalRecordEntity =
    PersonalRecordEntity(
        id = id,
        exerciseId = exerciseId,
        recordType = recordType,
        weightValue = weightValue,
        reps = reps,
        estimated1rm = estimated1rm,
        achievedAt = achievedAt,
        sessionId = sessionId,
    )

internal fun BackupBodyWeightEntry.toEntity(): BodyWeightEntryEntity =
    BodyWeightEntryEntity(
        id = id,
        weightValue = weightValue,
        recordedAt = recordedAt,
    )

// --- Entity → Backup ---

internal fun UserProfileEntity.toBackup(): BackupUserProfile =
    BackupUserProfile(
        id = id,
        experienceLevel = experienceLevel,
        preferredUnit = preferredUnit,
        age = age,
        heightCm = heightCm,
        gender = gender,
        bodyWeightKg = bodyWeightKg,
        compoundRepMin = compoundRepMin,
        compoundRepMax = compoundRepMax,
        isolationRepMin = isolationRepMin,
        isolationRepMax = isolationRepMax,
        defaultWorkingSets = defaultWorkingSets,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun WorkoutSessionEntity.toBackup(): BackupWorkoutSession =
    BackupWorkoutSession(
        id = id,
        startedAt = startedAt,
        completedAt = completedAt,
        durationSeconds = durationSeconds,
        pausedDurationSeconds = pausedDurationSeconds,
        status = status,
        notes = notes,
        templateId = templateId,
    )

internal fun WorkoutExerciseEntity.toBackup(): BackupWorkoutExercise =
    BackupWorkoutExercise(
        id = id,
        sessionId = sessionId,
        exerciseId = exerciseId,
        orderIndex = orderIndex,
        supersetGroupId = supersetGroupId,
        restTimerSeconds = restTimerSeconds,
        notes = notes,
    )

internal fun WorkoutSetEntity.toBackup(): BackupWorkoutSet =
    BackupWorkoutSet(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setIndex = setIndex,
        setType = setType,
        plannedWeight = plannedWeight,
        plannedReps = plannedReps,
        actualWeight = actualWeight,
        actualReps = actualReps,
        isCompleted = isCompleted,
        completedAt = completedAt,
        status = status,
    )

internal fun TemplateEntity.toBackup(): BackupTemplate =
    BackupTemplate(
        id = id,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
        muscleGroupsJson = muscleGroupsJson,
    )

internal fun TemplateExerciseEntity.toBackup(): BackupTemplateExercise =
    BackupTemplateExercise(
        id = id,
        templateId = templateId,
        exerciseId = exerciseId,
        orderIndex = orderIndex,
    )

internal fun PersonalRecordEntity.toBackup(): BackupPersonalRecord =
    BackupPersonalRecord(
        id = id,
        exerciseId = exerciseId,
        recordType = recordType,
        weightValue = weightValue,
        reps = reps,
        estimated1rm = estimated1rm,
        achievedAt = achievedAt,
        sessionId = sessionId,
    )

internal fun BodyWeightEntryEntity.toBackup(): BackupBodyWeightEntry =
    BackupBodyWeightEntry(
        id = id,
        weightValue = weightValue,
        recordedAt = recordedAt,
    )
