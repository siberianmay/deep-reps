package com.deepreps.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds default_working_sets column to user_profile.
 *
 * 0 = use experience-level default (Beginner=3, Intermediate=4, Advanced=5).
 * Values 1-10 override the default working sets per exercise.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN default_working_sets INTEGER NOT NULL DEFAULT 0")
    }
}
