package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository for the singleton user profile.
 *
 * The profile is created during onboarding (Screen 1-3) and updated in Settings.
 * Only one row exists (id=1).
 */
interface UserProfileRepository {

    /** Returns the user profile. Null if onboarding has not been completed. */
    suspend fun get(): UserProfile?

    /** Observe the user profile reactively. Emits null until onboarding completes. */
    fun observe(): Flow<UserProfile?>

    /** Creates or updates the singleton user profile row. */
    suspend fun save(profile: UserProfile)
}
