package com.deepreps.core.domain.repository

import com.deepreps.core.domain.model.BodyWeightEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository for body weight entries.
 *
 * All weights are stored in kg. Conversion to display unit happens in the UI layer.
 */
interface BodyWeightRepository {

    /** Returns all body weight entries, newest first. */
    fun getAll(): Flow<List<BodyWeightEntry>>

    /** Returns the most recent body weight entry. Null if no entries exist. */
    suspend fun getLatest(): BodyWeightEntry?

    /** Observe the most recent body weight entry reactively. */
    fun observeLatest(): Flow<BodyWeightEntry?>

    /** Inserts a new body weight entry and returns its generated ID. */
    suspend fun insert(entry: BodyWeightEntry): Long
}
