package com.deepreps.core.domain.usecase

import com.deepreps.core.domain.model.CrossGroupOverlap
import com.deepreps.core.domain.model.ExerciseForPlan
import javax.inject.Inject

/**
 * Detects cross-group muscle activation overlap when multiple muscle groups
 * are trained in the same session.
 *
 * Per exercise-science.md Section 2.2 Cross-Group Activation Map:
 * - Chest (bench press) significantly loads Shoulders (anterior delt) and Arms (triceps)
 * - Back (rows) significantly loads Arms (biceps), Shoulders (rear delts), Lower Back
 * - Shoulders (overhead press) significantly loads Arms (triceps) and Chest (upper)
 * - Legs (squats) significantly loads Lower Back (erectors) and Core (bracing)
 * - Lower Back (deadlifts) significantly loads Legs, Back (traps), Arms (grip)
 *
 * Returns overlap warnings for inclusion in the AI prompt's CROSS-GROUP FATIGUE block.
 */
class CrossGroupOverlapDetector @Inject constructor() {

    /**
     * Detects overlapping muscle activation between selected exercises.
     *
     * Groups exercises by primary group, then checks all pairs against the
     * cross-group activation map.
     */
    fun detect(exercises: List<ExerciseForPlan>): List<CrossGroupOverlap> {
        val groups = exercises.map { it.primaryGroup }.distinct()
        if (groups.size <= 1) return emptyList()

        val overlaps = mutableListOf<CrossGroupOverlap>()

        for (i in groups.indices) {
            for (j in i + 1 until groups.size) {
                val overlap = checkOverlap(groups[i], groups[j])
                if (overlap != null) {
                    overlaps.add(overlap)
                }
            }
        }

        return overlaps
    }

    private fun checkOverlap(groupA: String, groupB: String): CrossGroupOverlap? {
        val key = setOf(groupA, groupB)

        return OVERLAP_MAP[key]?.let { (sharedMuscles, description) ->
            CrossGroupOverlap(
                primaryGroup = groupA,
                overlappingGroup = groupB,
                sharedMuscles = sharedMuscles,
                description = description,
            )
        }
    }

    companion object {
        /**
         * Cross-group activation map from exercise-science.md Section 2.2.
         *
         * Keys are unordered pairs (Sets) of muscle group values.
         * Values are (shared muscles, description for prompt).
         */
        private val OVERLAP_MAP: Map<Set<String>, Pair<List<String>, String>> = mapOf(
            setOf("chest", "shoulders") to Pair(
                listOf("anterior deltoid", "triceps"),
                "Chest + Shoulders: pressing movements share anterior deltoid and triceps load. " +
                    "Reduce anterior delt isolation volume.",
            ),
            setOf("chest", "arms") to Pair(
                listOf("triceps"),
                "Chest + Arms: bench press variations provide substantial triceps stimulus. " +
                    "Reduce triceps isolation volume by 1-2 sets.",
            ),
            setOf("shoulders", "arms") to Pair(
                listOf("triceps"),
                "Shoulders + Arms: overhead pressing significantly loads triceps. " +
                    "Reduce triceps isolation volume.",
            ),
            setOf("back", "arms") to Pair(
                listOf("biceps", "forearms"),
                "Back + Arms: rowing and pulling movements provide significant biceps stimulus. " +
                    "Reduce biceps isolation volume by 1-2 sets.",
            ),
            setOf("back", "shoulders") to Pair(
                listOf("rear deltoid"),
                "Back + Shoulders: rows and face pulls share posterior deltoid activation. " +
                    "Reduce rear delt isolation volume.",
            ),
            setOf("back", "lower_back") to Pair(
                listOf("erector spinae", "traps"),
                "Back + Lower Back: rows involve isometric lower back loading; deadlifts heavily load traps. " +
                    "Moderate total spinal loading volume.",
            ),
            setOf("legs", "lower_back") to Pair(
                listOf("glutes", "hamstrings", "erector spinae"),
                "Legs + Lower Back: squats load erectors; deadlifts load glutes and hamstrings. " +
                    "Reduce hip hinge isolation volume if both groups trained.",
            ),
            setOf("legs", "core") to Pair(
                listOf("core stabilizers"),
                "Legs + Core: heavy squats and lunges demand significant core bracing. " +
                    "Direct core volume can be reduced.",
            ),
            setOf("lower_back", "core") to Pair(
                listOf("erector spinae", "core stabilizers"),
                "Lower Back + Core: deadlifts demand heavy core bracing. " +
                    "Reduce anti-extension core volume.",
            ),
        )
    }
}
