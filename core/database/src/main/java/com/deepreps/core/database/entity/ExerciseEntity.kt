package com.deepreps.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["primary_group_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("primary_group_id"),
        Index(value = ["stable_id"], unique = true),
    ],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    /** CSCS-defined stable string ID (e.g., "legs_barbell_squat"). Used in AI prompts. Unique. */
    @ColumnInfo(name = "stable_id")
    val stableId: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "description")
    val description: String,
    /** One of: barbell, dumbbell, cable, machine, bodyweight, kettlebell, band, ez_bar, trap_bar */
    @ColumnInfo(name = "equipment")
    val equipment: String,
    /** "compound" or "isolation" */
    @ColumnInfo(name = "movement_type")
    val movementType: String,
    /** "beginner", "intermediate", "advanced" -- used for safety guardrails and auto-ordering */
    @ColumnInfo(name = "difficulty")
    val difficulty: String,
    @ColumnInfo(name = "primary_group_id")
    val primaryGroupId: Long,
    /** JSON array of sub-muscle name strings (e.g., ["Glutes", "lower back", "core"]) */
    @ColumnInfo(name = "secondary_muscles")
    val secondaryMuscles: String,
    /** JSON array of form cue strings (max 4) */
    @ColumnInfo(name = "tips")
    val tips: String,
    /** JSON array of benefit strings (max 3) */
    @ColumnInfo(name = "pros")
    val pros: String,
    /** Ordering in exercise library browsing */
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    /** CSCS-defined priority for auto-ordering (lower = earlier in workout). Distinct from displayOrder. */
    @ColumnInfo(name = "order_priority")
    val orderPriority: Int,
    /** JSON array of compatibility tags per exercise-science Section 6.3.1 */
    @ColumnInfo(name = "superset_tags")
    val supersetTags: String,
    /** Min experience level for AI auto-inclusion (1/2/3, 99=manual only) */
    @ColumnInfo(name = "auto_program_min_level", defaultValue = "1")
    val autoProgramMinLevel: Int = 1,
)
