package com.deepreps.core.data.export

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.database.dao.BodyWeightDao
import com.deepreps.core.database.dao.CachedAiPlanDao
import com.deepreps.core.database.dao.PersonalRecordDao
import com.deepreps.core.database.dao.TemplateDao
import com.deepreps.core.database.dao.UserProfileDao
import com.deepreps.core.database.dao.WorkoutExerciseDao
import com.deepreps.core.database.dao.WorkoutSessionDao
import com.deepreps.core.database.dao.WorkoutSetDao
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.InputStream
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Imports user data from a JSON backup string or a ZIP archive containing `backup.json`.
 *
 * The import runs inside a single Room transaction. If any step fails, the entire
 * operation rolls back and existing data is preserved. Pre-populated data (exercises,
 * muscle groups) is never touched — only user data tables are cleared.
 */
@Suppress("LongParameterList")
class DataImporter @Inject constructor(
    private val transactionRunner: TransactionRunner,
    private val userProfileDao: UserProfileDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
    private val templateDao: TemplateDao,
    private val personalRecordDao: PersonalRecordDao,
    private val bodyWeightDao: BodyWeightDao,
    private val cachedAiPlanDao: CachedAiPlanDao,
    private val dispatchers: DispatcherProvider,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Imports data from a raw JSON string.
     *
     * The JSON must conform to the [BackupData] schema with `version == 1`.
     * Database version mismatches produce a warning but do not fail the import.
     */
    suspend fun importFromJson(jsonString: String): ImportResult =
        withContext(dispatchers.io) {
            try {
                val backup = parseBackup(jsonString)
                    ?: return@withContext ImportResult(
                        success = false,
                        error = "Failed to parse backup JSON",
                    )

                val validationError = validate(backup)
                if (validationError != null) {
                    return@withContext ImportResult(success = false, error = validationError)
                }

                insertData(backup)
            } catch (e: IllegalArgumentException) {
                Timber.e(e, "Import validation failed")
                ImportResult(success = false, error = e.message ?: "Validation error")
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Timber.e(e, "Import failed")
                ImportResult(success = false, error = "Import failed: ${e.message}")
            }
        }

    /**
     * Imports data from a ZIP archive containing a `backup.json` entry.
     *
     * Reads the first entry named `backup.json` from the stream, extracts it,
     * and delegates to [importFromJson].
     */
    suspend fun importFromZip(inputStream: InputStream): ImportResult =
        withContext(dispatchers.io) {
            try {
                val jsonString = extractJsonFromZip(inputStream)
                    ?: return@withContext ImportResult(
                        success = false,
                        error = "No backup.json found in ZIP archive",
                    )
                importFromJson(jsonString)
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Timber.e(e, "Failed to read ZIP archive")
                ImportResult(success = false, error = "Failed to read ZIP: ${e.message}")
            }
        }

    private fun parseBackup(jsonString: String): BackupData? =
        try {
            json.decodeFromString<BackupData>(jsonString)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            Timber.e(e, "JSON parse error")
            null
        }

    private fun validate(backup: BackupData): String? {
        if (backup.version != SUPPORTED_VERSION) {
            return "Unsupported backup version: ${backup.version}. Expected $SUPPORTED_VERSION."
        }
        if (backup.dbVersion != CURRENT_DB_VERSION) {
            Timber.w(
                "Backup dbVersion %d differs from current %d. Proceeding with import.",
                backup.dbVersion,
                CURRENT_DB_VERSION,
            )
        }
        return null
    }

    /**
     * Clears user data tables and inserts backup data inside a single transaction.
     *
     * Insertion order respects foreign key constraints:
     * 1. user_profile (no FK dependencies)
     * 2. workout_sessions (no FK to user data)
     * 3. workout_exercises (FK → workout_sessions, exercises)
     * 4. workout_sets (FK → workout_exercises)
     * 5. templates (no FK dependencies)
     * 6. template_exercises (FK → templates, exercises)
     * 7. personal_records (FK → exercises, workout_sessions)
     * 8. body_weight_entries (no FK dependencies)
     */
    @Suppress("LongMethod")
    private suspend fun insertData(backup: BackupData): ImportResult {
        transactionRunner.runInTransaction {
            // Delete child tables first (reverse FK order) to avoid constraint violations.
            workoutSetDao.deleteAll()
            workoutExerciseDao.deleteAll()
            personalRecordDao.deleteAll()
            templateDao.deleteAllTemplateExercises()
            templateDao.deleteAll()
            workoutSessionDao.deleteAll()
            bodyWeightDao.deleteAll()
            userProfileDao.deleteAll()
            // Clear cached AI plans — stale after a data restore.
            cachedAiPlanDao.deleteAll()

            // Insert in FK-safe order.
            backup.userProfile?.let { userProfileDao.upsert(it.toEntity()) }

            if (backup.workoutSessions.isNotEmpty()) {
                workoutSessionDao.insertAll(
                    backup.workoutSessions.map { it.toEntity() },
                )
            }
            if (backup.workoutExercises.isNotEmpty()) {
                workoutExerciseDao.insertAll(
                    backup.workoutExercises.map { it.toEntity() },
                )
            }
            if (backup.workoutSets.isNotEmpty()) {
                workoutSetDao.insertAll(
                    backup.workoutSets.map { it.toEntity() },
                )
            }
            if (backup.templates.isNotEmpty()) {
                templateDao.insertAll(
                    backup.templates.map { it.toEntity() },
                )
            }
            if (backup.templateExercises.isNotEmpty()) {
                templateDao.insertTemplateExercises(
                    backup.templateExercises.map { it.toEntity() },
                )
            }
            if (backup.personalRecords.isNotEmpty()) {
                personalRecordDao.insertAll(
                    backup.personalRecords.map { it.toEntity() },
                )
            }
            if (backup.bodyWeightEntries.isNotEmpty()) {
                bodyWeightDao.insertAll(
                    backup.bodyWeightEntries.map { it.toEntity() },
                )
            }
        }

        return ImportResult(
            success = true,
            sessionsImported = backup.workoutSessions.size,
            templatesImported = backup.templates.size,
            personalRecordsImported = backup.personalRecords.size,
            bodyWeightEntriesImported = backup.bodyWeightEntries.size,
        )
    }

    private fun extractJsonFromZip(inputStream: InputStream): String? {
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == BACKUP_JSON_FILENAME) {
                    return zip.bufferedReader().readText()
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return null
    }

    companion object {
        private const val SUPPORTED_VERSION = 1
        private const val CURRENT_DB_VERSION = 3
        private const val BACKUP_JSON_FILENAME = "backup.json"
    }
}
