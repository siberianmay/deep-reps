package com.deepreps.core.data.export

import com.deepreps.core.database.dao.BodyWeightDao
import com.deepreps.core.database.dao.CachedAiPlanDao
import com.deepreps.core.database.dao.PersonalRecordDao
import com.deepreps.core.database.dao.TemplateDao
import com.deepreps.core.database.dao.UserProfileDao
import com.deepreps.core.database.dao.WorkoutExerciseDao
import com.deepreps.core.database.dao.WorkoutSessionDao
import com.deepreps.core.database.dao.WorkoutSetDao
import com.deepreps.core.database.entity.BodyWeightEntryEntity
import com.deepreps.core.database.entity.PersonalRecordEntity
import com.deepreps.core.database.entity.TemplateEntity
import com.deepreps.core.database.entity.TemplateExerciseEntity
import com.deepreps.core.database.entity.WorkoutExerciseEntity
import com.deepreps.core.database.entity.WorkoutSessionEntity
import com.deepreps.core.database.entity.WorkoutSetEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataImporterTest {

    private lateinit var userProfileDao: UserProfileDao
    private lateinit var workoutSessionDao: WorkoutSessionDao
    private lateinit var workoutExerciseDao: WorkoutExerciseDao
    private lateinit var workoutSetDao: WorkoutSetDao
    private lateinit var templateDao: TemplateDao
    private lateinit var personalRecordDao: PersonalRecordDao
    private lateinit var bodyWeightDao: BodyWeightDao
    private lateinit var cachedAiPlanDao: CachedAiPlanDao
    private lateinit var importer: DataImporter

    private val testDispatcher = StandardTestDispatcher()

    /** Pass-through transaction runner for unit tests (no real DB transaction needed). */
    /** Pass-through transaction runner for unit tests (no real DB transaction needed). */
    private val testTransactionRunner = object : TransactionRunner {
        override suspend fun runInTransaction(block: suspend () -> Unit) = block()
    }

    @BeforeEach
    fun setup() {
        userProfileDao = mockk()
        workoutSessionDao = mockk()
        workoutExerciseDao = mockk()
        workoutSetDao = mockk()
        templateDao = mockk()
        personalRecordDao = mockk()
        bodyWeightDao = mockk()
        cachedAiPlanDao = mockk()

        stubDeleteMethods()
        stubInsertMethods()

        importer = DataImporter(
            transactionRunner = testTransactionRunner,
            userProfileDao = userProfileDao,
            workoutSessionDao = workoutSessionDao,
            workoutExerciseDao = workoutExerciseDao,
            workoutSetDao = workoutSetDao,
            templateDao = templateDao,
            personalRecordDao = personalRecordDao,
            bodyWeightDao = bodyWeightDao,
            cachedAiPlanDao = cachedAiPlanDao,
            dispatchers = TestDispatcherProvider(testDispatcher),
        )
    }

    @Test
    fun `importFromJson succeeds with valid backup`() = runTest(testDispatcher) {
        val json = buildValidBackupJson()

        val result = importer.importFromJson(json)

        assertThat(result.success).isTrue()
        assertThat(result.sessionsImported).isEqualTo(1)
        assertThat(result.templatesImported).isEqualTo(1)
        assertThat(result.personalRecordsImported).isEqualTo(1)
        assertThat(result.bodyWeightEntriesImported).isEqualTo(1)
    }

    @Test
    fun `importFromJson clears tables before inserting`() = runTest(testDispatcher) {
        val json = buildValidBackupJson()

        importer.importFromJson(json)

        coVerifyOrder {
            workoutSetDao.deleteAll()
            workoutExerciseDao.deleteAll()
            personalRecordDao.deleteAll()
            templateDao.deleteAllTemplateExercises()
            templateDao.deleteAll()
            workoutSessionDao.deleteAll()
            bodyWeightDao.deleteAll()
            userProfileDao.deleteAll()
            cachedAiPlanDao.deleteAll()
        }
    }

    @Test
    fun `importFromJson inserts data in FK-safe order`() = runTest(testDispatcher) {
        val json = buildValidBackupJson()

        importer.importFromJson(json)

        coVerifyOrder {
            userProfileDao.upsert(any())
            workoutSessionDao.insertAll(any())
            workoutExerciseDao.insertAll(any())
            workoutSetDao.insertAll(any())
            templateDao.insertAll(any())
            templateDao.insertTemplateExercises(any())
            personalRecordDao.insertAll(any())
            bodyWeightDao.insertAll(any())
        }
    }

    @Test
    fun `importFromJson maps entities correctly`() = runTest(testDispatcher) {
        val json = buildValidBackupJson()
        val sessionSlot = slot<List<WorkoutSessionEntity>>()
        coEvery { workoutSessionDao.insertAll(capture(sessionSlot)) } just Runs

        importer.importFromJson(json)

        val sessions = sessionSlot.captured
        assertThat(sessions).hasSize(1)
        assertThat(sessions[0].id).isEqualTo(10)
        assertThat(sessions[0].status).isEqualTo("completed")
        assertThat(sessions[0].startedAt).isEqualTo(1000L)
    }

    @Test
    fun `importFromJson fails on unsupported version`() = runTest(testDispatcher) {
        val json = buildValidBackupJson().replace("\"version\": 1", "\"version\": 99")

        val result = importer.importFromJson(json)

        assertThat(result.success).isFalse()
        assertThat(result.error).contains("Unsupported backup version")
    }

    @Test
    fun `importFromJson fails on invalid JSON`() = runTest(testDispatcher) {
        val result = importer.importFromJson("not valid json {{{")

        assertThat(result.success).isFalse()
        assertThat(result.error).isEqualTo("Failed to parse backup JSON")
    }

    @Test
    fun `importFromJson handles missing optional fields`() = runTest(testDispatcher) {
        val json = """
            {
                "version": 1,
                "exportedAt": "2026-02-26T20:00:00Z",
                "dbVersion": 3
            }
        """.trimIndent()

        val result = importer.importFromJson(json)

        assertThat(result.success).isTrue()
        assertThat(result.sessionsImported).isEqualTo(0)
        assertThat(result.templatesImported).isEqualTo(0)
    }

    @Test
    fun `importFromJson warns but succeeds on dbVersion mismatch`() =
        runTest(testDispatcher) {
            val json = buildValidBackupJson()
                .replace("\"dbVersion\": 3", "\"dbVersion\": 99")

            val result = importer.importFromJson(json)

            assertThat(result.success).isTrue()
        }

    @Test
    fun `importFromJson skips empty collections without calling insert`() =
        runTest(testDispatcher) {
            val json = """
            {
                "version": 1,
                "exportedAt": "2026-02-26T20:00:00Z",
                "dbVersion": 3,
                "workoutSessions": []
            }
        """.trimIndent()

            importer.importFromJson(json)

            coVerify(exactly = 0) { workoutSessionDao.insertAll(any()) }
        }

    @Test
    fun `importFromZip returns error when no backup json found`() =
        runTest(testDispatcher) {
            val emptyZip = createEmptyZipBytes()

            val result = importer.importFromZip(emptyZip.inputStream())

            assertThat(result.success).isFalse()
            assertThat(result.error).contains("No backup.json found")
        }

    @Test
    fun `importFromZip reads backup json from zip`() = runTest(testDispatcher) {
        val json = buildValidBackupJson()
        val zipBytes = createZipWithEntry("backup.json", json)

        val result = importer.importFromZip(zipBytes.inputStream())

        assertThat(result.success).isTrue()
        assertThat(result.sessionsImported).isEqualTo(1)
    }

    @Test
    fun `importFromJson does not insert userProfile when null`() =
        runTest(testDispatcher) {
            val json = """
            {
                "version": 1,
                "exportedAt": "2026-02-26T20:00:00Z",
                "dbVersion": 3,
                "userProfile": null
            }
        """.trimIndent()

            importer.importFromJson(json)

            coVerify(exactly = 0) { userProfileDao.upsert(any()) }
        }

    // --- Helpers ---

    private fun stubDeleteMethods() {
        coEvery { workoutSetDao.deleteAll() } just Runs
        coEvery { workoutExerciseDao.deleteAll() } just Runs
        coEvery { personalRecordDao.deleteAll() } just Runs
        coEvery { templateDao.deleteAllTemplateExercises() } just Runs
        coEvery { templateDao.deleteAll() } just Runs
        coEvery { workoutSessionDao.deleteAll() } just Runs
        coEvery { bodyWeightDao.deleteAll() } just Runs
        coEvery { userProfileDao.deleteAll() } just Runs
        coEvery { cachedAiPlanDao.deleteAll() } just Runs
    }

    @Suppress("MaxLineLength")
    private fun stubInsertMethods() {
        coEvery { userProfileDao.upsert(any()) } just Runs
        coEvery { workoutSessionDao.insertAll(any<List<WorkoutSessionEntity>>()) } just Runs
        coEvery { workoutExerciseDao.insertAll(any<List<WorkoutExerciseEntity>>()) } just Runs
        coEvery { workoutSetDao.insertAll(any<List<WorkoutSetEntity>>()) } just Runs
        coEvery { templateDao.insertAll(any<List<TemplateEntity>>()) } just Runs
        coEvery { templateDao.insertTemplateExercises(any<List<TemplateExerciseEntity>>()) } just Runs
        coEvery { personalRecordDao.insertAll(any<List<PersonalRecordEntity>>()) } just Runs
        coEvery { bodyWeightDao.insertAll(any<List<BodyWeightEntryEntity>>()) } just Runs
    }

    @Suppress("LongMethod")
    private fun buildValidBackupJson(): String = """
        {
            "version": 1,
            "exportedAt": "2026-02-26T20:00:00Z",
            "dbVersion": 3,
            "userProfile": {
                "id": 1,
                "experienceLevel": 2,
                "preferredUnit": "kg",
                "age": 30,
                "heightCm": 180.0,
                "gender": "male",
                "bodyWeightKg": 85.0,
                "compoundRepMin": 6,
                "compoundRepMax": 10,
                "isolationRepMin": 10,
                "isolationRepMax": 15,
                "createdAt": 1000,
                "updatedAt": 2000
            },
            "workoutSessions": [
                {
                    "id": 10,
                    "startedAt": 1000,
                    "completedAt": 2000,
                    "durationSeconds": 3600,
                    "pausedDurationSeconds": 0,
                    "status": "completed",
                    "notes": null,
                    "templateId": 1
                }
            ],
            "workoutExercises": [
                {
                    "id": 100,
                    "sessionId": 10,
                    "exerciseId": 1,
                    "orderIndex": 0,
                    "supersetGroupId": null,
                    "restTimerSeconds": 120,
                    "notes": null
                }
            ],
            "workoutSets": [
                {
                    "id": 1000,
                    "workoutExerciseId": 100,
                    "setIndex": 0,
                    "setType": "working",
                    "plannedWeight": 60.0,
                    "plannedReps": 8,
                    "actualWeight": 60.0,
                    "actualReps": 8,
                    "isCompleted": true,
                    "completedAt": 1500,
                    "status": "completed"
                }
            ],
            "templates": [
                {
                    "id": 1,
                    "name": "Push Day",
                    "createdAt": 1000,
                    "updatedAt": 2000,
                    "muscleGroupsJson": "[1,2]"
                }
            ],
            "templateExercises": [
                {
                    "id": 50,
                    "templateId": 1,
                    "exerciseId": 1,
                    "orderIndex": 0
                }
            ],
            "personalRecords": [
                {
                    "id": 500,
                    "exerciseId": 1,
                    "recordType": "weight",
                    "weightValue": 100.0,
                    "reps": 5,
                    "estimated1rm": 112.5,
                    "achievedAt": 1500,
                    "sessionId": 10
                }
            ],
            "bodyWeightEntries": [
                {
                    "id": 200,
                    "weightValue": 85.0,
                    "recordedAt": 1000
                }
            ]
        }
    """.trimIndent()

    private fun createEmptyZipBytes(): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(baos).use { it.finish() }
        return baos.toByteArray()
    }

    private fun createZipWithEntry(name: String, content: String): ByteArray {
        val baos = java.io.ByteArrayOutputStream()
        java.util.zip.ZipOutputStream(baos).use { zip ->
            zip.putNextEntry(java.util.zip.ZipEntry(name))
            zip.write(content.toByteArray())
            zip.closeEntry()
        }
        return baos.toByteArray()
    }
}
