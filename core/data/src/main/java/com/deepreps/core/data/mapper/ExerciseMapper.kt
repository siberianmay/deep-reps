package com.deepreps.core.data.mapper

import com.deepreps.core.database.entity.ExerciseEntity
import com.deepreps.core.database.entity.MuscleGroupEntity
import com.deepreps.core.domain.model.Exercise
import com.deepreps.core.domain.model.MuscleGroupModel
import com.deepreps.core.domain.model.enums.Difficulty
import com.deepreps.core.domain.model.enums.Equipment
import com.deepreps.core.domain.model.enums.MovementType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }

// --- MuscleGroup ---

fun MuscleGroupEntity.toDomain(): MuscleGroupModel = MuscleGroupModel(
    id = id,
    name = name,
    displayOrder = displayOrder,
)

fun MuscleGroupModel.toEntity(): MuscleGroupEntity = MuscleGroupEntity(
    id = id,
    name = name,
    displayOrder = displayOrder,
)

// --- Exercise ---

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    stableId = stableId,
    name = name,
    description = description,
    equipment = Equipment.fromValue(equipment),
    movementType = MovementType.fromValue(movementType),
    difficulty = Difficulty.fromValue(difficulty),
    primaryGroupId = primaryGroupId,
    secondaryMuscles = parseJsonStringList(secondaryMuscles),
    tips = parseJsonStringList(tips),
    pros = parseJsonStringList(pros),
    displayOrder = displayOrder,
    orderPriority = orderPriority,
    supersetTags = parseJsonStringList(supersetTags),
    autoProgramMinLevel = autoProgramMinLevel,
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    stableId = stableId,
    name = name,
    description = description,
    equipment = equipment.value,
    movementType = movementType.value,
    difficulty = difficulty.value,
    primaryGroupId = primaryGroupId,
    secondaryMuscles = toJsonStringList(secondaryMuscles),
    tips = toJsonStringList(tips),
    pros = toJsonStringList(pros),
    displayOrder = displayOrder,
    orderPriority = orderPriority,
    supersetTags = toJsonStringList(supersetTags),
    autoProgramMinLevel = autoProgramMinLevel,
)

// --- JSON helpers ---

private val stringListSerializer = ListSerializer(String.serializer())

internal fun parseJsonStringList(jsonString: String): List<String> =
    try {
        json.decodeFromString(stringListSerializer, jsonString)
    } catch (_: Exception) {
        emptyList()
    }

internal fun toJsonStringList(list: List<String>): String =
    json.encodeToString(stringListSerializer, list)
