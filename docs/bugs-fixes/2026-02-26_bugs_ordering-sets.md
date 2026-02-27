# Bug Report: 2026-02-26

## BUG-8: Generated plan ignores user exercise ordering
**Status:** FIXED (2026-02-26)
**Priority:** P1
**Reported:** 2026-02-26
**Screen:** Plan Review (`PlanReviewViewModel.kt`)

### Problem
User reorders exercises on ExerciseOrderScreen (drag-to-reorder), taps "Generate Plan", but the generated plan shows exercises in database PK order instead of the user's custom order.

### Root Cause
`PlanReviewViewModel.buildExercisesForPlan()` at line 263 calls `exerciseRepository.getExercisesByIds(exerciseIds)`. The underlying Room DAO query (`SELECT * FROM exercises WHERE id IN (:ids)`) returns results sorted by primary key, not by the order of the input `ids` list. The user's ordering is preserved through navigation (comma-separated string in route), but lost at the repository fetch step.

### Files
- `feature/ai-plan/src/main/java/com/deepreps/feature/aiplan/PlanReviewViewModel.kt` (line 263)
- `core/data/src/main/java/com/deepreps/core/data/repository/ExerciseRepositoryImpl.kt` (line 42-45)
