package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlanRequest
import com.deepreps.core.domain.model.PlanResult
import com.deepreps.core.domain.provider.AiPlanProvider
import com.deepreps.core.domain.provider.BaselinePlanGenerator
import com.deepreps.core.domain.provider.ConnectivityChecker
import com.deepreps.core.domain.repository.CachedPlanRepository
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject

/**
 * Implements the 4-level fallback chain for plan generation.
 *
 * Per architecture.md Section 4.5:
 * 1. Try AI (GeminiPlanProvider) if online
 * 2. If AI fails, check cached plan (CachedPlanRepository)
 * 3. If no cache, generate baseline plan (BaselinePlanGenerator)
 * 4. If all else fails, return empty plan (user enters manually)
 *
 * Also handles:
 * - Caching successful AI plans
 * - Deleting expired cache entries (7 days)
 * - Tracking which level was used (for analytics)
 */
class GeneratePlanUseCase @Inject constructor(
    private val aiProvider: AiPlanProvider,
    private val cachedPlanRepository: CachedPlanRepository,
    private val baselinePlanGenerator: BaselinePlanGenerator,
    private val connectivityChecker: ConnectivityChecker,
) {

    /**
     * Generates a plan using the 4-level fallback chain.
     *
     * This is a suspend function -- safe to call from ViewModel scope.
     * Internally catches all exceptions and maps them to [PlanResult] variants.
     */
    @Suppress("ReturnCount")
    suspend fun execute(request: PlanRequest): PlanResult {
        val cacheHash = computeCacheHash(request)
        val experienceLevel = request.userProfile.experienceLevel

        // Opportunistically clean expired cache entries
        cleanExpiredCache()

        // Step 1: Try AI provider if online
        if (connectivityChecker.isOnline()) {
            val aiResult = tryAiProvider(request)
            if (aiResult != null) {
                // Cache the successful plan
                cachedPlanRepository.save(cacheHash, experienceLevel, aiResult)
                return PlanResult.AiGenerated(aiResult)
            }
            // AI failed despite connectivity -- fall through
        }

        // Step 2: Try cached plan
        val cached = cachedPlanRepository.getByHash(cacheHash, experienceLevel)
        if (cached != null) {
            return PlanResult.Cached(cached)
        }

        // Step 3: Baseline plan (experience-level defaults, no AI)
        val baseline = baselinePlanGenerator.generate(request)
        if (baseline != null) {
            return PlanResult.Baseline(baseline)
        }

        // Step 4: Manual entry (empty plan)
        return PlanResult.Manual
    }

    private suspend fun tryAiProvider(request: PlanRequest): GeneratedPlan? =
        try {
            aiProvider.generatePlan(request)
        } catch (_: Exception) {
            null
        }

    private suspend fun cleanExpiredCache() {
        try {
            val sevenDaysAgo = System.currentTimeMillis() - CACHE_EXPIRY_MS
            cachedPlanRepository.deleteExpired(sevenDaysAgo)
        } catch (_: Exception) {
            // Cache cleanup failure is non-critical; ignore
        }
    }

    companion object {
        private const val CACHE_EXPIRY_MS = 7L * 24 * 60 * 60 * 1000 // 7 days

        /**
         * Computes a SHA-256 hash of sorted exercise stable IDs.
         * Used as cache key for plan lookup.
         */
        fun computeCacheHash(request: PlanRequest): String {
            val sortedIds = request.exercises
                .map { it.stableId }
                .sorted()
                .joinToString(",")
            val bytes = MessageDigest.getInstance("SHA-256").digest(sortedIds.toByteArray())
            return bytes.joinToString("") { String.format(Locale.US, "%02x", it) }
        }
    }
}
