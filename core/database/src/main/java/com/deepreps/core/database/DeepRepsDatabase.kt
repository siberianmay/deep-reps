package com.deepreps.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.deepreps.core.database.converter.Converters
import com.deepreps.core.database.dao.BodyWeightDao
import com.deepreps.core.database.dao.CachedAiPlanDao
import com.deepreps.core.database.dao.ExerciseDao
import com.deepreps.core.database.dao.MuscleGroupDao
import com.deepreps.core.database.dao.PersonalRecordDao
import com.deepreps.core.database.dao.TemplateDao
import com.deepreps.core.database.dao.UserProfileDao
import com.deepreps.core.database.dao.WorkoutExerciseDao
import com.deepreps.core.database.dao.WorkoutSessionDao
import com.deepreps.core.database.dao.WorkoutSetDao
import com.deepreps.core.database.entity.BodyWeightEntryEntity
import com.deepreps.core.database.entity.CachedAiPlanEntity
import com.deepreps.core.database.entity.ExerciseEntity
import com.deepreps.core.database.entity.ExerciseMuscleEntity
import com.deepreps.core.database.entity.MuscleGroupEntity
import com.deepreps.core.database.entity.PersonalRecordEntity
import com.deepreps.core.database.entity.TemplateEntity
import com.deepreps.core.database.entity.TemplateExerciseEntity
import com.deepreps.core.database.entity.UserProfileEntity
import com.deepreps.core.database.entity.WorkoutExerciseEntity
import com.deepreps.core.database.entity.WorkoutSessionEntity
import com.deepreps.core.database.entity.WorkoutSetEntity

@Database(
    entities = [
        MuscleGroupEntity::class,
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        UserProfileEntity::class,
        BodyWeightEntryEntity::class,
        PersonalRecordEntity::class,
        CachedAiPlanEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class DeepRepsDatabase : RoomDatabase() {
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun templateDao(): TemplateDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun cachedAiPlanDao(): CachedAiPlanDao
}
