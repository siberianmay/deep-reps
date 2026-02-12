package com.deepreps.core.network.di

import com.deepreps.core.domain.provider.AiPlanProvider
import com.deepreps.core.network.gemini.GeminiPlanProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module binding the AI plan provider.
 *
 * Per architecture.md Section 4.6:
 * To swap from Gemini to another LLM, change [GeminiPlanProvider] to the new implementation.
 * Nothing else in the codebase changes.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AiProviderModule {

    @Binds
    abstract fun bindAiPlanProvider(impl: GeminiPlanProvider): AiPlanProvider
}
