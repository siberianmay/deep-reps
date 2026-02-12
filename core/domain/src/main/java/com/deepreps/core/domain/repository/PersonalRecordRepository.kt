package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.PersonalRecord
import kotlinx.coroutines.flow.Flow

/**
 * Repository for personal records.
 *
 * PRs are detected in real-time during workouts and confirmed when the session is saved.
 */
interface PersonalRecordRepository {

    /** Returns all PRs for a specific exercise, newest first. */
    suspend fun getByExercise(exerciseId: Long): List<PersonalRecord>

    /** Returns the single best PR for an exercise and record type. Null if no records. */
    suspend fun getBestByType(exerciseId: Long, recordType: String): PersonalRecord?

    /** Batch insert of PRs (used when a session is completed). */
    suspend fun insertAll(records: List<PersonalRecord>)

    /** Observe all PRs, newest first. */
    fun observeAll(): Flow<List<PersonalRecord>>

    /** Observe PRs for a specific exercise reactively. */
    fun observeByExercise(exerciseId: Long): Flow<List<PersonalRecord>>
}
