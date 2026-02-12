package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.PersonalRecordEntity
import com.deepreps.core.domain.model.PersonalRecord
import com.deepreps.core.domain.model.enums.RecordType

fun PersonalRecordEntity.toDomain(): PersonalRecord = PersonalRecord(
    id = id,
    exerciseId = exerciseId,
    recordType = RecordType.fromValue(recordType),
    weightValue = weightValue,
    reps = reps,
    estimated1rm = estimated1rm,
    achievedAt = achievedAt,
    sessionId = sessionId,
)

fun PersonalRecord.toEntity(): PersonalRecordEntity = PersonalRecordEntity(
    id = id,
    exerciseId = exerciseId,
    recordType = recordType.value,
    weightValue = weightValue,
    reps = reps,
    estimated1rm = estimated1rm,
    achievedAt = achievedAt,
    sessionId = sessionId,
)
