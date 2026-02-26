package com.deepreps.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.deepreps.core.database.entity.TemplateEntity
import com.deepreps.core.database.entity.TemplateExerciseEntity
import com.deepreps.core.database.entity.TemplateWithExerciseCountProjection
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Insert
    suspend fun insert(template: TemplateEntity): Long

    @Update
    suspend fun update(template: TemplateEntity)

    @Delete
    suspend fun delete(template: TemplateEntity)

    @Query("SELECT * FROM templates ORDER BY updated_at DESC")
    fun getAll(): Flow<List<TemplateEntity>>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun getById(id: Long): TemplateEntity?

    // --- Template Exercises ---

    @Insert
    suspend fun insertTemplateExercise(exercise: TemplateExerciseEntity): Long

    @Insert
    suspend fun insertTemplateExercises(exercises: List<TemplateExerciseEntity>)

    @Query("SELECT * FROM template_exercises WHERE template_id = :templateId ORDER BY order_index ASC")
    suspend fun getTemplateExercises(templateId: Long): List<TemplateExerciseEntity>

    @Query("SELECT * FROM template_exercises WHERE template_id = :templateId ORDER BY order_index ASC")
    fun observeTemplateExercises(templateId: Long): Flow<List<TemplateExerciseEntity>>

    @Query("DELETE FROM template_exercises WHERE template_id = :templateId")
    suspend fun deleteTemplateExercises(templateId: Long)

    // --- Template Projections ---

    /**
     * Returns all templates with exercise count, ordered by most recently updated first.
     * Uses a LEFT JOIN to count exercises per template, ensuring empty templates show count 0.
     */
    @Query(
        """
        SELECT t.id, t.name, t.created_at, t.updated_at, t.muscle_groups_json,
               COUNT(te.id) as exercise_count
        FROM templates t
        LEFT JOIN template_exercises te ON t.id = te.template_id
        GROUP BY t.id
        ORDER BY t.updated_at DESC
        """,
    )
    fun getAllWithExerciseCount(): Flow<List<TemplateWithExerciseCountProjection>>
}
