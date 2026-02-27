package com.deepreps.core.data.export

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.database.dao.BodyWeightDao
import com.deepreps.core.database.dao.ExerciseDao
import com.deepreps.core.database.dao.PersonalRecordDao
import com.deepreps.core.database.dao.TemplateDao
import com.deepreps.core.database.dao.UserProfileDao
import com.deepreps.core.database.dao.WorkoutExerciseDao
import com.deepreps.core.database.dao.WorkoutSessionDao
import com.deepreps.core.database.dao.WorkoutSetDao
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports all user workout data to a zip file containing:
 * - `backup.json` -- full machine-readable export using [BackupData] schema (for import)
 * - `workouts.csv` -- denormalized one-row-per-set view
 * - `templates.csv` -- template-exercise mapping
 * - `personal_records.csv` -- PR history
 * - `body_weight.csv` -- body weight log
 *
 * All file I/O runs on [DispatcherProvider.io].
 */
@Singleton
@Suppress("LongParameterList")
class DataExporter @Inject constructor(
    private val userProfileDao: UserProfileDao,
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
    private val templateDao: TemplateDao,
    private val personalRecordDao: PersonalRecordDao,
    private val bodyWeightDao: BodyWeightDao,
    private val exerciseDao: ExerciseDao,
    private val dispatchers: DispatcherProvider,
) {

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    /**
     * Queries all user data from Room and writes a zip file to [outputDir].
     *
     * @param outputDir Directory where the zip will be created. Must exist and be writable.
     * @return The created zip [File].
     * @throws IllegalArgumentException if [outputDir] does not exist or is not a directory.
     */
    suspend fun exportToZip(outputDir: File): File = withContext(dispatchers.io) {
        require(outputDir.exists() && outputDir.isDirectory) {
            "Output directory does not exist: ${outputDir.absolutePath}"
        }

        Timber.d("Starting data export to %s", outputDir.absolutePath)

        val backupData = queryAllData()
        val exercises = exerciseDao.getAllOnce()
        val exerciseNames = CsvFormatter.buildExerciseNameMap(exercises)

        val jsonContent = json.encodeToString(BackupData.serializer(), backupData)
        val workoutsCsv = CsvFormatter.formatWorkoutsCsv(backupData, exerciseNames)
        val templatesCsv = CsvFormatter.formatTemplatesCsv(backupData, exerciseNames)
        val prCsv = CsvFormatter.formatPersonalRecordsCsv(backupData, exerciseNames)
        val bwCsv = CsvFormatter.formatBodyWeightCsv(backupData)

        val timestamp = DateTimeFormatter
            .ofPattern("yyyy-MM-dd_HHmmss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.now())

        val zipFile = File(outputDir, "deep_reps_export_$timestamp.zip")

        writeZip(zipFile) { zos ->
            zos.writeEntry("backup.json", jsonContent)
            zos.writeEntry("workouts.csv", workoutsCsv)
            zos.writeEntry("templates.csv", templatesCsv)
            zos.writeEntry("personal_records.csv", prCsv)
            zos.writeEntry("body_weight.csv", bwCsv)
        }

        Timber.d(
            "Export complete: %s (%d bytes)",
            zipFile.absolutePath,
            zipFile.length(),
        )

        zipFile
    }

    private suspend fun queryAllData(): BackupData {
        val userProfile = userProfileDao.get()
        val sessions = workoutSessionDao.getAllOnce()
        val exercises = workoutExerciseDao.getAllOnce()
        val sets = workoutSetDao.getAllOnce()
        val templates = templateDao.getAllOnce()
        val templateExercises = templateDao.getAllTemplateExercisesOnce()
        val personalRecords = personalRecordDao.getAllOnce()
        val bodyWeightEntries = bodyWeightDao.getAllOnce()

        val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        return BackupData(
            version = BACKUP_VERSION,
            exportedAt = now,
            dbVersion = DB_VERSION,
            userProfile = userProfile?.toBackup(),
            workoutSessions = sessions.map { it.toBackup() },
            workoutExercises = exercises.map { it.toBackup() },
            workoutSets = sets.map { it.toBackup() },
            templates = templates.map { it.toBackup() },
            templateExercises = templateExercises.map { it.toBackup() },
            personalRecords = personalRecords.map { it.toBackup() },
            bodyWeightEntries = bodyWeightEntries.map { it.toBackup() },
        )
    }

    private fun writeZip(zipFile: File, block: (ZipOutputStream) -> Unit) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            block(zos)
        }
    }

    private fun ZipOutputStream.writeEntry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    companion object {
        private const val BACKUP_VERSION = 1
        /** Must match DeepRepsDatabase version. */
        private const val DB_VERSION = 3
    }
}
