package com.deepreps.core.database.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.deepreps.core.database.entity.ExerciseEntity
import com.deepreps.core.database.entity.ExerciseMuscleEntity
import com.deepreps.core.database.entity.MuscleGroupEntity

/**
 * Exercise entity with its associated muscle groups loaded via the junction table.
 *
 * Used by [ExerciseDao.getExercisesWithMuscles] to avoid N+1 queries when
 * displaying exercises with their primary and secondary muscle group tags.
 */
data class ExerciseWithMuscles(
    @Embedded
    val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ExerciseMuscleEntity::class,
            parentColumn = "exercise_id",
            entityColumn = "muscle_group_id",
        ),
    )
    val muscleGroups: List<MuscleGroupEntity>,
)
