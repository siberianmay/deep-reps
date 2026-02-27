package com.deepreps.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deepreps.core.database.DeepRepsDatabase
import com.deepreps.core.database.PrepopulateCallback
import com.deepreps.core.database.migration.MIGRATION_2_3
import com.deepreps.core.database.migration.MIGRATION_3_4
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    private const val DATABASE_NAME = "deep_reps.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): DeepRepsDatabase =
        Room.databaseBuilder(context, DeepRepsDatabase::class.java, DATABASE_NAME)
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .addCallback(PrepopulateCallback())
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMuscleGroupDao(db: DeepRepsDatabase): MuscleGroupDao = db.muscleGroupDao()

    @Provides
    fun provideExerciseDao(db: DeepRepsDatabase): ExerciseDao = db.exerciseDao()

    @Provides
    fun provideWorkoutSessionDao(db: DeepRepsDatabase): WorkoutSessionDao = db.workoutSessionDao()

    @Provides
    fun provideWorkoutExerciseDao(db: DeepRepsDatabase): WorkoutExerciseDao = db.workoutExerciseDao()

    @Provides
    fun provideWorkoutSetDao(db: DeepRepsDatabase): WorkoutSetDao = db.workoutSetDao()

    @Provides
    fun provideTemplateDao(db: DeepRepsDatabase): TemplateDao = db.templateDao()

    @Provides
    fun provideUserProfileDao(db: DeepRepsDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    fun provideBodyWeightDao(db: DeepRepsDatabase): BodyWeightDao = db.bodyWeightDao()

    @Provides
    fun providePersonalRecordDao(db: DeepRepsDatabase): PersonalRecordDao = db.personalRecordDao()

    @Provides
    fun provideCachedAiPlanDao(db: DeepRepsDatabase): CachedAiPlanDao = db.cachedAiPlanDao()
}
