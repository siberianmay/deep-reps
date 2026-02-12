package com.deepreps.core.domain.provider

import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlanRequest

/**
 * Provider interface for AI-powered workout plan generation.
 *
 * Lives in :core:domain. The implementation (Gemini, OpenAI, local model)
 * lives in :core:network. Swappable via Hilt @Binds without touching domain or presentation.
 */
interface AiPlanProvider {

    /**
     * Generates a workout plan for the given request context.
     *
     * @return [GeneratedPlan] on success.
     * @throws AiPlanException on failure (network, parsing, rate limit).
     */
    suspend fun generatePlan(request: PlanRequest): GeneratedPlan
}

/**
 * Domain exception for AI plan generation failures.
 * Catch this in use cases and map to [DomainError.AiProviderError].
 */
class AiPlanException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
