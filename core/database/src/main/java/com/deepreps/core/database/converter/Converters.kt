package com.deepreps.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Room TypeConverters for all custom types used in the database.
 *
 * JSON arrays (tips, pros, superset_tags, secondary_muscles, muscle_groups)
 * are serialized via kotlinx.serialization.
 *
 * Temporal types use epoch millis (UTC) for storage.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    // --- List<String> <-> JSON ---

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(value)

    @TypeConverter
    fun toStringList(value: String): List<String> =
        if (value.isBlank()) emptyList() else json.decodeFromString(value)

    // --- LocalDateTime <-> Long (epoch millis UTC) ---

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): Long? =
        value?.toInstant(ZoneOffset.UTC)?.toEpochMilli()

    @TypeConverter
    fun toLocalDateTime(value: Long?): LocalDateTime? =
        value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
        }

    // --- LocalDate <-> Long (epoch day) ---

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): Long? =
        value?.toEpochDay()

    @TypeConverter
    fun toLocalDate(value: Long?): LocalDate? =
        value?.let { LocalDate.ofEpochDay(it) }
}
