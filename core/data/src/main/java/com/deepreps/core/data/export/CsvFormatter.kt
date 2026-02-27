package com.deepreps.core.data.export

import com.deepreps.core.database.entity.ExerciseEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Generates human-readable CSV files from backup data.
 *
 * All CSV output uses RFC 4180 conventions: fields containing commas, quotes, or
 * newlines are double-quoted with internal quotes escaped by doubling.
 */
internal object CsvFormatter {

    private val isoFormatter: DateTimeFormatter =
        DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())

    private val dateOnlyFormatter: DateTimeFormatter =
        DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())

    /**
     * Denormalized workout CSV: one row per set, joined with session and exercise info.
     */
    fun formatWorkoutsCsv(
        data: BackupData,
        exerciseNames: Map<Long, String>,
    ): String = buildString {
        appendLine(
            "session_id,session_date,session_status,session_duration_sec," +
                "exercise_name,exercise_id,set_index,set_type," +
                "planned_weight_kg,planned_reps,actual_weight_kg,actual_reps,status",
        )

        val sessionsById = data.workoutSessions.associateBy { it.id }
        val exercisesById = data.workoutExercises.associateBy { it.id }

        @Suppress("LoopWithTooManyJumpStatements")
        for (set in data.workoutSets) {
            val workoutExercise = exercisesById[set.workoutExerciseId] ?: continue
            val session = sessionsById[workoutExercise.sessionId] ?: continue
            val exerciseName = exerciseNames[workoutExercise.exerciseId] ?: "Unknown"

            appendLine(
                buildCsvRow(
                    session.id,
                    formatEpoch(session.startedAt),
                    session.status,
                    session.durationSeconds ?: 0,
                    escapeCsv(exerciseName),
                    workoutExercise.exerciseId,
                    set.setIndex,
                    set.setType,
                    set.plannedWeight ?: "",
                    set.plannedReps ?: "",
                    set.actualWeight ?: "",
                    set.actualReps ?: "",
                    set.status,
                ),
            )
        }
    }

    /**
     * Template CSV: one row per template-exercise pair.
     */
    fun formatTemplatesCsv(
        data: BackupData,
        exerciseNames: Map<Long, String>,
    ): String = buildString {
        appendLine("template_id,template_name,exercise_order,exercise_id,exercise_name")

        val templatesById = data.templates.associateBy { it.id }

        for (te in data.templateExercises) {
            val template = templatesById[te.templateId] ?: continue
            val exerciseName = exerciseNames[te.exerciseId] ?: "Unknown"

            appendLine(
                buildCsvRow(
                    te.templateId,
                    escapeCsv(template.name),
                    te.orderIndex,
                    te.exerciseId,
                    escapeCsv(exerciseName),
                ),
            )
        }
    }

    /**
     * Personal records CSV: one row per record.
     */
    fun formatPersonalRecordsCsv(
        data: BackupData,
        exerciseNames: Map<Long, String>,
    ): String = buildString {
        appendLine(
            "exercise_id,exercise_name,record_type,weight_kg,reps,estimated_1rm_kg,achieved_at",
        )

        for (pr in data.personalRecords) {
            val exerciseName = exerciseNames[pr.exerciseId] ?: "Unknown"

            appendLine(
                buildCsvRow(
                    pr.exerciseId,
                    escapeCsv(exerciseName),
                    pr.recordType,
                    pr.weightValue ?: "",
                    pr.reps ?: "",
                    pr.estimated1rm ?: "",
                    formatEpoch(pr.achievedAt),
                ),
            )
        }
    }

    /**
     * Body weight history CSV.
     */
    fun formatBodyWeightCsv(data: BackupData): String = buildString {
        appendLine("date,weight_kg")

        for (entry in data.bodyWeightEntries) {
            appendLine(
                buildCsvRow(
                    formatDate(entry.recordedAt),
                    entry.weightValue,
                ),
            )
        }
    }

    /**
     * Builds a map of exercise ID to name from a list of exercise entities.
     */
    fun buildExerciseNameMap(exercises: List<ExerciseEntity>): Map<Long, String> =
        exercises.associate { it.id to it.name }

    private fun buildCsvRow(vararg values: Any): String =
        values.joinToString(",") { it.toString() }

    private fun formatEpoch(epochMillis: Long): String =
        isoFormatter.format(Instant.ofEpochMilli(epochMillis))

    private fun formatDate(epochMillis: Long): String =
        dateOnlyFormatter.format(Instant.ofEpochMilli(epochMillis))

    private fun escapeCsv(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
}
