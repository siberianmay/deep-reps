package com.deepreps.core.domain.repository

/**
 * Repository for onboarding completion state.
 *
 * The onboarding_completed flag is stored in SharedPreferences (NOT Room)
 * per architecture.md. It must be readable before the Room database is opened
 * to determine the start destination.
 */
interface OnboardingRepository {

    /** Returns true if onboarding has been completed. */
    fun isOnboardingCompleted(): Boolean

    /** Marks onboarding as completed. */
    fun setOnboardingCompleted()
}
