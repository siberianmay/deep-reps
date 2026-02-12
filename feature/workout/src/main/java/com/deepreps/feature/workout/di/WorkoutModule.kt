package com.deepreps.feature.workout.di

import com.deepreps.core.domain.repository.UserProfileRepository
import com.deepreps.core.domain.statemachine.WorkoutStateMachine
import com.deepreps.core.domain.usecase.OrderExercisesUseCase
import com.deepreps.core.domain.usecase.ResolveRestTimerUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Hilt module providing workout-specific dependencies scoped to ViewModel lifecycle.
 */
@Module
@InstallIn(ViewModelComponent::class)
object WorkoutModule {

    @Provides
    @ViewModelScoped
    fun provideOrderExercisesUseCase(): OrderExercisesUseCase = OrderExercisesUseCase()

    @Provides
    @ViewModelScoped
    fun provideWorkoutStateMachine(): WorkoutStateMachine = WorkoutStateMachine()

    @Provides
    @ViewModelScoped
    fun provideResolveRestTimerUseCase(
        userProfileRepository: UserProfileRepository,
    ): ResolveRestTimerUseCase = ResolveRestTimerUseCase(userProfileRepository)
}
