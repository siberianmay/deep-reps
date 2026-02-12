package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.enums.ExperienceLevel
import com.deepreps.core.domain.model.enums.MovementType
import com.deepreps.core.domain.repository.UserProfileRepository

/**
 * Resolves the rest timer duration for a given exercise using a 4-level priority chain.
 *
 * Priority (highest first):
 * 1. AI plan recommendation ([aiPlanRestSeconds]) -- if the AI plan specified rest for this exercise.
 * 2. User per-exercise override ([userOverrideSeconds]) -- if the user customized rest for this exercise.
 * 3. User global default from Settings -- a single fallback value the user set.
 * 4. CSCS baseline -- derived from exercise type + user experience level per exercise-science.md Section 8.8.
 *
 * This class has zero Android dependencies (pure Kotlin domain layer).
 */
class ResolveRestTimerUseCase(
    private val userProfileRepository: UserProfileRepository,
) {

    /**
     * Returns the rest duration in seconds for the given exercise.
     *
     * @param exercise The exercise to resolve rest for.
     * @param aiPlanRestSeconds Rest seconds from the AI-generated plan, or null if not available.
     * @param userOverrideSeconds Per-exercise user override, or null if not set.
     * @param userGlobalDefaultSeconds Global default from user settings, or null if not set.
     */
    @Suppress("ReturnCount")
    suspend operator fun invoke(
        exercise: Exercise,
        aiPlanRestSeconds: Int? = null,
        userOverrideSeconds: Int? = null,
        userGlobalDefaultSeconds: Int? = null,
    ): Int {
        // Priority 1: AI plan recommendation
        if (aiPlanRestSeconds != null && aiPlanRestSeconds > 0) {
            return aiPlanRestSeconds
        }

        // Priority 2: User per-exercise override
        if (userOverrideSeconds != null && userOverrideSeconds > 0) {
            return userOverrideSeconds
        }

        // Priority 3: User global default
        if (userGlobalDefaultSeconds != null && userGlobalDefaultSeconds > 0) {
            return userGlobalDefaultSeconds
        }

        // Priority 4: CSCS baseline (exercise-science.md Section 8.8)
        val profile = userProfileRepository.get()
        val level = profile?.experienceLevel ?: ExperienceLevel.INTERMEDIATE
        return cscsBaselineRestSeconds(exercise.movementType, level, exercise.primaryGroupId)
    }

    companion object {

        // Core group ID = 7 in the database (Legs=1, LowerBack=2, Chest=3, Back=4,
        // Shoulders=5, Arms=6, Core=7) per MuscleGroup enum ordinal+1.
        private const val CORE_GROUP_ID = 7L

        /**
         * CSCS baseline rest timer values from exercise-science.md Section 8.8.
         *
         * Heavy compound: Beginner=90, Intermediate=120, Advanced=180
         * Moderate compound (fallback for compounds that are not "heavy"):
         *   Beginner=75, Intermediate=105, Advanced=120
         * Isolation: Beginner=60, Intermediate=75, Advanced=75
         * Core (all levels): 60
         * Warm-up sets (all): 60 -- handled separately by the caller.
         *
         * We use COMPOUND as heavy compound for simplicity since the Exercise model
         * does not distinguish heavy vs moderate compounds. The AI plan or user override
         * should handle the nuance.
         */
        fun cscsBaselineRestSeconds(
            movementType: MovementType,
            experienceLevel: ExperienceLevel,
            primaryGroupId: Long,
        ): Int {
            // Core exercises always 60s regardless of level
            if (primaryGroupId == CORE_GROUP_ID) return 60

            return when (movementType) {
                MovementType.COMPOUND -> when (experienceLevel) {
                    ExperienceLevel.BEGINNER -> 90
                    ExperienceLevel.INTERMEDIATE -> 120
                    ExperienceLevel.ADVANCED -> 180
                }
                MovementType.ISOLATION -> when (experienceLevel) {
                    ExperienceLevel.BEGINNER -> 60
                    ExperienceLevel.INTERMEDIATE -> 75
                    ExperienceLevel.ADVANCED -> 75
                }
            }
        }
    }
}
