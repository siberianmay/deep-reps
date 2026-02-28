package com.deepreps.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Adds name column to workout_sessions for session naming.
 *
 * Defaults to NULL — sessions without a name fall back to date display in the UI.
 * Sessions created from templates will have the template name set as default.
 */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE workout_sessions ADD COLUMN name TEXT DEFAULT NULL")
    }
}
