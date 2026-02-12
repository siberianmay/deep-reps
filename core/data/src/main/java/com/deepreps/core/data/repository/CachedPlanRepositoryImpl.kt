package com.deepreps.core.data.repository

import com.deepreps.core.common.dispatcher.DispatcherProvider
import com.deepreps.core.database.dao.CachedAiPlanDao
import com.deepreps.core.database.entity.CachedAiPlanEntity
import com.deepreps.core.domain.model.ExercisePlan
import com.deepreps.core.domain.model.GeneratedPlan
import com.deepreps.core.domain.model.PlannedSet
import com.deepreps.core.domain.model.enums.SetType
import com.deepreps.core.domain.repository.CachedPlanRepository
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject

internal class CachedPlanRepositoryImpl @Inject constructor(
    private val cachedAiPlanDao: CachedAiPlanDao,
    private val dispatchers: DispatcherProvider,
) : CachedPlanRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun getByHash(hash: String, experienceLevel: Int): GeneratedPlan? =
        withContext(dispatchers.io) {
            val entity = cachedAiPlanDao.getByHash(hash, experienceLevel) ?: return@withContext null
            deserializePlan(entity.planJson)
        }

    override suspend fun save(hash: String, experienceLevel: Int, plan: GeneratedPlan) =
        withContext(dispatchers.io) {
            val planJson = serializePlan(plan)
            cachedAiPlanDao.insert(
                CachedAiPlanEntity(
                    exerciseIdsHash = hash,
                    planJson = planJson,
                    createdAt = System.currentTimeMillis(),
                    experienceLevel = experienceLevel,
                ),
            )
            Unit
        }

    override suspend fun deleteExpired(before: Long) =
        withContext(dispatchers.io) {
            cachedAiPlanDao.deleteExpired(before)
        }

    private fun serializePlan(plan: GeneratedPlan): String {
        val dto = CachedPlanDto(
            exercises = plan.exercises.map { exercise ->
                CachedExercisePlanDto(
                    exerciseId = exercise.exerciseId,
                    stableId = exercise.stableId,
                    exerciseName = exercise.exerciseName,
                    restSeconds = exercise.restSeconds,
                    notes = exercise.notes,
                    sets = exercise.sets.map { set ->
                        CachedPlannedSetDto(
                            setType = set.setType.value,
                            weight = set.weight,
                            reps = set.reps,
                            restSeconds = set.restSeconds,
                        )
                    },
                )
            },
        )
        return json.encodeToString(CachedPlanDto.serializer(), dto)
    }

    private fun deserializePlan(planJson: String): GeneratedPlan? =
        try {
            val dto = json.decodeFromString(CachedPlanDto.serializer(), planJson)
            GeneratedPlan(
                exercises = dto.exercises.map { exercise ->
                    ExercisePlan(
                        exerciseId = exercise.exerciseId,
                        stableId = exercise.stableId,
                        exerciseName = exercise.exerciseName,
                        restSeconds = exercise.restSeconds,
                        notes = exercise.notes,
                        sets = exercise.sets.map { set ->
                            PlannedSet(
                                setType = SetType.fromValue(set.setType),
                                weight = set.weight,
                                reps = set.reps,
                                restSeconds = set.restSeconds,
                            )
                        },
                    )
                },
            )
        } catch (_: Exception) {
            null
        }
}

@Serializable
private data class CachedPlanDto(
    val exercises: List<CachedExercisePlanDto>,
)

@Serializable
private data class CachedExercisePlanDto(
    val exerciseId: Long,
    val stableId: String,
    val exerciseName: String,
    val restSeconds: Int,
    val notes: String? = null,
    val sets: List<CachedPlannedSetDto>,
)

@Serializable
private data class CachedPlannedSetDto(
    val setType: String,
    val weight: Double,
    val reps: Int,
    val restSeconds: Int,
)
