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
    status = deriveSetStatus(isCompleted),
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
)

/**
 * Derives the domain SetStatus from the entity's isCompleted boolean.
 *
 * The entity stores a simple boolean. The domain model has richer status semantics.
 * PLANNED and IN_PROGRESS are both represented as isCompleted=false in the entity.
 * We default to PLANNED here; the ViewModel determines IN_PROGRESS based on UI state.
 */
private fun deriveSetStatus(isCompleted: Boolean): SetStatus =
    if (isCompleted) SetStatus.COMPLETED else SetStatus.PLANNED
