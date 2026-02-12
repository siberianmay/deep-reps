package com.deepreps.core.data.di

import com.deepreps.core.data.repository.BodyWeightRepositoryImpl
import com.deepreps.core.data.repository.CachedPlanRepositoryImpl
import com.deepreps.core.data.repository.ExerciseRepositoryImpl
import com.deepreps.core.data.repository.OnboardingRepositoryImpl
import com.deepreps.core.data.repository.PersonalRecordRepositoryImpl
import com.deepreps.core.data.repository.TemplateRepositoryImpl
import com.deepreps.core.data.repository.UserProfileRepositoryImpl
import com.deepreps.core.data.repository.WorkoutSessionRepositoryImpl
import com.deepreps.core.domain.repository.BodyWeightRepository
import com.deepreps.core.domain.repository.CachedPlanRepository
import com.deepreps.core.domain.repository.ExerciseRepository
import com.deepreps.core.domain.repository.OnboardingRepository
import com.deepreps.core.domain.repository.PersonalRecordRepository
import com.deepreps.core.domain.repository.TemplateRepository
import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.repository.WorkoutSessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module binding all 8 repository implementations to their domain interfaces.
 *
 * All repository implementations use @Inject constructor.
 * Feature modules depend only on the interfaces in :core:domain.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutSessionRepository(impl: WorkoutSessionRepositoryImpl): WorkoutSessionRepository

    @Binds
    @Singleton
    abstract fun bindTemplateRepository(impl: TemplateRepositoryImpl): TemplateRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindPersonalRecordRepository(impl: PersonalRecordRepositoryImpl): PersonalRecordRepository

    @Binds
    @Singleton
    abstract fun bindBodyWeightRepository(impl: BodyWeightRepositoryImpl): BodyWeightRepository

    @Binds
    @Singleton
    abstract fun bindCachedPlanRepository(impl: CachedPlanRepositoryImpl): CachedPlanRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(impl: OnboardingRepositoryImpl): OnboardingRepository
}
