package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.BodyWeightEntryEntity
import com.deepreps.core.domain.model.BodyWeightEntry

fun BodyWeightEntryEntity.toDomain(): BodyWeightEntry = BodyWeightEntry(
    id = id,
    weightKg = weightValue,
    recordedAt = recordedAt,
)

fun BodyWeightEntry.toEntity(): BodyWeightEntryEntity = BodyWeightEntryEntity(
    id = id,
    weightValue = weightKg,
    recordedAt = recordedAt,
)
