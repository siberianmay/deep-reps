package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.GeneratedPlan

/**
 * Repository for cached AI-generated plans.
 *
 * Plans are keyed by a SHA-256 hash of sorted exercise IDs + experience level.
 * Cached plans older than 7 days are eligible for cleanup.
 */
interface CachedPlanRepository {

    /**
     * Returns a cached plan matching the exercise set hash and experience level.
     * Null if no matching cache entry exists.
     */
    suspend fun getByHash(hash: String, experienceLevel: Int): GeneratedPlan?

    /** Saves a generated plan to the cache. */
    suspend fun save(hash: String, experienceLevel: Int, plan: GeneratedPlan)

    /** Deletes cached plans created before the given epoch millis. */
    suspend fun deleteExpired(before: Long)
}
