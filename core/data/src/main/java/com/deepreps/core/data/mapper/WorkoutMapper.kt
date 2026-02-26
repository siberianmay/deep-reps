package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.WorkoutExerciseEntity
import com.deepreps.core.database.entity.WorkoutSessionEntity
import com.deepreps.core.database.entity.WorkoutSetEntity
import com.deepreps.core.domain.model.WorkoutExercise
import com.deepreps.core.domain.model.WorkoutSession
import com.deepreps.core.domain.model.WorkoutSet
import com.deepreps.core.domain.model.enums.SessionStatus
import com.deepreps.core.domain.model.enums.SetStatus
import com.deepreps.core.domain.model.enums.SetType

// --- WorkoutSession ---

fun WorkoutSessionEntity.toDomain(): WorkoutSession = WorkoutSession(
    id = id,
    startedAt = startedAt,
    completedAt = completedAt,
    durationSeconds = durationSeconds,
    pausedDurationSeconds = pausedDurationSeconds,
    status = SessionStatus.fromValue(status),
    notes = notes,
    templateId = templateId,
)

fun WorkoutSession.toEntity(): WorkoutSessionEntity = WorkoutSessionEntity(
    id = id,
    startedAt = startedAt,
    completedAt = completedAt,
    durationSeconds = durationSeconds,
    pausedDurationSeconds = pausedDurationSeconds,
    status = status.value,
    notes = notes,
    templateId = templateId,
)

// --- WorkoutExercise ---

fun WorkoutExerciseEntity.toDomain(): WorkoutExercise = WorkoutExercise(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    supersetGroupId = supersetGroupId,
    restTimerSeconds = restTimerSeconds,
    notes = notes,
)

fun WorkoutExercise.toEntity(): WorkoutExerciseEntity = WorkoutExerciseEntity(
    id = id,
    sessionId = sessionId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
    supersetGroupId = supersetGroupId,
    restTimerSeconds = restTimerSeconds,
    notes = notes,
)

// --- WorkoutSet ---

fun WorkoutSetEntity.toDomain(): WorkoutSet = WorkoutSet(
    id = id,
    setNumber = setIndex,
    type = SetType.fromValue(setType),
    status = deriveSetStatus(status, isCompleted),
    plannedWeightKg = plannedWeight,
    plannedReps = plannedReps,
    actualWeightKg = actualWeight,
    actualReps = actualReps,
    completedAt = completedAt,
)

fun WorkoutSet.toEntity(workoutExerciseId: Long): WorkoutSetEntity = WorkoutSetEntity(
    id = id,
    workoutExerciseId = workoutExerciseId,
    setIndex = setNumber,
    setType = type.value,
    plannedWeight = plannedWeightKg,
    plannedReps = plannedReps,
    actualWeight = actualWeightKg,
    actualReps = actualReps,
    isCompleted = status == SetStatus.COMPLETED,
    completedAt = completedAt,
    status = status.value,
)

/**
 * Derives the domain SetStatus from the entity's status string column.
 * Falls back to the legacy isCompleted boolean for forward-compatibility
 * with rows that may not have the status column populated.
 *
 * PLANNED and IN_PROGRESS are both stored as "planned" in the entity.
 * The ViewModel determines IN_PROGRESS based on UI state.
 */
private fun deriveSetStatus(status: String, isCompleted: Boolean): SetStatus =
    when (status) {
        SetStatus.COMPLETED.value -> SetStatus.COMPLETED
        SetStatus.SKIPPED.value -> SetStatus.SKIPPED
        else -> if (isCompleted) SetStatus.COMPLETED else SetStatus.PLANNED
    }
