package com.deepreps.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Hilt module providing data-layer singletons.
 *
 * ConsentManager is @Singleton with @Inject constructor, so Hilt provides it directly.
 * Initialization (reading persisted prefs into StateFlow) is triggered in Application.onCreate()
 * by calling consentManager.initialize() -- not in the DI graph.
 *
 * Also provides the application-scoped [CoroutineScope] used by long-lived singletons
 * such as [RestTimerManager] that must outlive any individual ViewModel.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Application-scoped coroutine scope for singletons that need to run
     * coroutines independently of any ViewModel lifecycle.
     *
     * Uses [SupervisorJob] so that a failure in one child does not cancel siblings.
     * Uses [Dispatchers.Main] as the default context; IO work should explicitly
     * switch via withContext(Dispatchers.IO).
     */
    @Provides
    @Singleton
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
