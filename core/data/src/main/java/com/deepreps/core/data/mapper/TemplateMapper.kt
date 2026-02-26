package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.TemplateEntity
import com.deepreps.core.database.entity.TemplateExerciseEntity
import com.deepreps.core.database.entity.TemplateWithExerciseCountProjection
import com.deepreps.core.domain.model.Template
import com.deepreps.core.domain.model.TemplateExercise
import com.deepreps.core.domain.model.TemplateWithCount
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// --- Template ---

fun TemplateEntity.toDomain(): Template = Template(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    muscleGroups = parseMuscleGroupIds(muscleGroupsJson),
)

fun Template.toEntity(): TemplateEntity = TemplateEntity(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    muscleGroupsJson = encodeMuscleGroupIds(muscleGroups),
)

// --- TemplateExercise ---

fun TemplateExerciseEntity.toDomain(): TemplateExercise = TemplateExercise(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
)

fun TemplateExercise.toEntity(): TemplateExerciseEntity = TemplateExerciseEntity(
    id = id,
    templateId = templateId,
    exerciseId = exerciseId,
    orderIndex = orderIndex,
)

// --- TemplateWithCount ---

fun TemplateWithExerciseCountProjection.toDomain(): TemplateWithCount = TemplateWithCount(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    muscleGroups = parseMuscleGroupIds(muscleGroupsJson),
    exerciseCount = exerciseCount,
)

// --- JSON helpers ---

private val longListSerializer = ListSerializer(Long.serializer())

private fun parseMuscleGroupIds(jsonString: String): List<Long> =
    try {
        json.decodeFromString(longListSerializer, jsonString)
    } catch (_: Exception) {
        emptyList()
    }

private fun encodeMuscleGroupIds(ids: List<Long>): String =
    json.encodeToString(longListSerializer, ids)
