@file:Suppress("ForbiddenPublicDataClass")

package com.deepreps.core.domain.model

/**
 * Full context required for AI plan generation.
 *
 * This replaces the minimal [PlanGenerationContext] with the complete set of fields
 * specified in architecture.md Section 4.3.
 *
 * Consumed by [AiPlanProvider], [BaselinePlanGenerator], and the fallback chain.
 * Pure Kotlin -- no Android dependencies.
 */
data class PlanRequest(
    val userProfile: UserPlanProfile,
    val exercises: List<ExerciseForPlan>,
    val trainingHistory: List<ExerciseHistory>,
    val periodizationModel: String,
    val performanceTrend: String?,
    val weeksSinceDeload: Int?,
    val deloadRecommended: Boolean,
    val currentBlockPhase: String?,
    val currentBlockWeek: Int?,
)

/**
 * Subset of user profile relevant to plan generation.
 * Avoids leaking the full [UserProfile] into the provider layer.
 */
data class UserPlanProfile(
    val experienceLevel: Int,
    val bodyWeightKg: Double?,
    val age: Int?,
    val gender: String?,
    val compoundRepMin: Int = 8,
    val compoundRepMax: Int = 12,
    val isolationRepMin: Int = 12,
    val isolationRepMax: Int = 15,
    val defaultWorkingSets: Int = 0,
)

/**
 * Exercise metadata relevant for prompt construction.
 * Uses stable IDs for the AI prompt and Room PKs for internal mapping.
 */
data class ExerciseForPlan(
    val exerciseId: Long,
    val stableId: String,
    val name: String,
    val equipment: String,
    val movementType: String,
    val difficulty: String,
    val primaryGroup: String,
)

/**
 * Training history for a single exercise, containing the last N sessions.
 */
data class ExerciseHistory(
    val exerciseId: Long,
    val exerciseName: String,
    val sessions: List<HistoricalSession>,
    val trend: String? = null,
)

/**
 * A single historical session for an exercise.
 */
data class HistoricalSession(
    val date: Long,
    val sets: List<HistoricalSet>,
)

/**
 * A single historical set within a session.
 */
data class HistoricalSet(
    val weight: Double,
    val reps: Int,
    val setType: String,
)
