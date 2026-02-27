package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.UserProfileEntity
import com.deepreps.core.domain.model.UserProfile
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.WeightUnit

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    experienceLevel = ExperienceLevel.fromValue(experienceLevel),
    preferredUnit = WeightUnit.fromValue(preferredUnit),
    age = age,
    heightCm = heightCm,
    gender = gender?.let { Gender.fromValue(it) },
    bodyWeightKg = bodyWeightKg,
    compoundRepMin = compoundRepMin,
    compoundRepMax = compoundRepMax,
    isolationRepMin = isolationRepMin,
    isolationRepMax = isolationRepMax,
    defaultWorkingSets = defaultWorkingSets,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    experienceLevel = experienceLevel.value,
    preferredUnit = preferredUnit.value,
    age = age,
    heightCm = heightCm,
    gender = gender?.value,
    bodyWeightKg = bodyWeightKg,
    compoundRepMin = compoundRepMin,
    compoundRepMax = compoundRepMax,
    isolationRepMin = isolationRepMin,
    isolationRepMax = isolationRepMax,
    defaultWorkingSets = defaultWorkingSets,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
