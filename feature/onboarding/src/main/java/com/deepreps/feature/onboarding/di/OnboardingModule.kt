package com.deepreps.feature.onboarding.di

import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.usecase.CompleteOnboardingUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module providing onboarding-specific dependencies scoped to ViewModel lifecycle.
 */
@Module
@InstallIn(ViewModelComponent::class)
object OnboardingModule {

    @Provides
    @ViewModelScoped
    fun provideCompleteOnboardingUseCase(
        userProfileRepository: UserProfileRepository,
    ): CompleteOnboardingUseCase = CompleteOnboardingUseCase(userProfileRepository)
}
