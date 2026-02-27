package com.deepreps.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds rep range preference columns to user_profile.
 *
 * Default values match intermediate level (the entity-level defaults).
 * Existing rows get these defaults via the ALTER TABLE DEFAULT clause.
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_profile ADD COLUMN compound_rep_min INTEGER NOT NULL DEFAULT 6")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN compound_rep_max INTEGER NOT NULL DEFAULT 10")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN isolation_rep_min INTEGER NOT NULL DEFAULT 10")
        db.execSQL("ALTER TABLE user_profile ADD COLUMN isolation_rep_max INTEGER NOT NULL DEFAULT 15")
    }
}
