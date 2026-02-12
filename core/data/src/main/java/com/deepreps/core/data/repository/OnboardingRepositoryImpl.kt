package com.deepreps.core.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.deepreps.core.domain.repository.OnboardingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * SharedPreferences-backed implementation of [OnboardingRepository].
 *
 * Uses a separate SharedPreferences file (not EncryptedSharedPreferences)
 * because this flag contains no sensitive data and must be readable
 * synchronously on cold start to determine the navigation start destination.
 */
internal class OnboardingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : OnboardingRepository {

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    override fun isOnboardingCompleted(): Boolean =
        prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)

    override fun setOnboardingCompleted() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, true).apply()
    }

    companion object {
        private const val PREFS_NAME = "onboarding_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
