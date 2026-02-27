package com.deepreps.core.data.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.room.withTransaction
import com.deepreps.core.common.dispatcher.DefaultDispatcherProvider
import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.data.export.TransactionRunner
import com.deepreps.core.database.DeepRepsDatabase
import com.deepreps.core.domain.provider.ConnectivityChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider =
        DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideTransactionRunner(
        database: DeepRepsDatabase,
    ): TransactionRunner = object : TransactionRunner {
        override suspend fun runInTransaction(block: suspend () -> Unit) {
            database.withTransaction { block() }
        }
    }

    @Provides
    @Singleton
    fun provideConnectivityChecker(
        @ApplicationContext context: Context,
    ): ConnectivityChecker = object : ConnectivityChecker {
        override fun isOnline(): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
    }
}
