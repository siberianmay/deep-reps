package com.deepreps.core.domain.model

import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.Gender
import com.deepreps.core.domain.model.enums.WeightUnit

/**
 * Domain representation of the singleton user profile.
 *
 * Height in cm, body weight in kg (internal storage convention).
 * [gender] is nullable -- when null, baseline calculations use male ratios reduced by 15%.
 */
data class UserProfile(
    val id: Long = 1,
    val experienceLevel: ExperienceLevel,
    val preferredUnit: WeightUnit,
    val age: Int?,
    val heightCm: Double?,
    val gender: Gender?,
    val bodyWeightKg: Double?,
    val createdAt: Long,
    val updatedAt: Long,
)
