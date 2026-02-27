# Implementation Plan: Rep Range Settings & Progressive Overload Fallback

**Created:** 2026-02-26
**Status:** COMPLETED (2026-02-26)
**Scope:** User-configurable rep ranges in Settings + double-progression fallback logic in BaselinePlanGenerator

---

## 1.1 Add Rep Range Fields to UserProfile & Database

**Problem:** `UserProfile` has no rep range preferences. The app hardcodes single rep targets (10, 8, 5) in `BaselinePlanGenerator.getTargetReps()`.

**Impact:** Users cannot customize their training intensity. The generator ignores experience-level-appropriate ranges.

**Fix/Implementation:**

1. Add fields to `UserProfile` domain model:
   ```kotlin
   val compoundRepMin: Int,    // default from experience level
   val compoundRepMax: Int,
   val isolationRepMin: Int,
   val isolationRepMax: Int,
   ```

2. Add columns to `UserProfileEntity` in Room:
   ```kotlin
   @ColumnInfo(name = "compound_rep_min") val compoundRepMin: Int,
   @ColumnInfo(name = "compound_rep_max") val compoundRepMax: Int,
   @ColumnInfo(name = "isolation_rep_min") val isolationRepMin: Int,
   @ColumnInfo(name = "isolation_rep_max") val isolationRepMax: Int,
   ```

3. Add Room migration (increment DB version).

4. Default values by experience level (from CSCS):

   | Level | Compound | Isolation |
   |-------|----------|-----------|
   | Beginner | 8-12 | 12-15 |
   | Intermediate | 6-10 | 10-15 |
   | Advanced | 4-8 | 8-15 |

5. When user changes experience level in Settings, auto-populate rep ranges with new defaults (but don't overwrite if user has customized them — track a `repRangeCustomized: Boolean` flag, or simpler: just always reset on level change since customization is an edge case for MVP).

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/model/UserProfile.kt`
- `core/database/src/main/java/com/deepreps/core/database/entity/UserProfileEntity.kt`
- `core/data/src/main/java/com/deepreps/core/data/mapper/UserProfileMapper.kt`
- `core/database/src/main/java/com/deepreps/core/database/DeepRepsDatabase.kt` (migration)

**Effort:** Small (1-2 hr)
**Risk:** Low. Room migration must handle existing users with null columns.

---

## 1.2 Add Rep Range UI to Settings Screen

**Problem:** No UI for rep range preferences. Users cannot adjust min/max reps.

**Fix/Implementation:**

1. Add a new "Rep Ranges" section to `SettingsScreen.kt` (between Profile and Rest Timer sections):

   ```
   REP RANGES
   ─────────────────────────────────
   Compound Exercises        8 - 12
   [========|----------]  [---|------]
     4    min          12  min   max  20

   Isolation Exercises      12 - 15
   [============|------]  [-----|----]
     4    min          20  min   max  20
   ```

   Use two `RangeSlider` components (Material 3) — one for compound, one for isolation. Each shows min-max as a range.

   Alternative (simpler): Two rows, each with two number inputs (min/max) or a custom `RangeSlider`.

2. Add intents to `SettingsIntent.kt`:
   ```kotlin
   data class SetCompoundRepRange(val min: Int, val max: Int) : SettingsIntent
   data class SetIsolationRepRange(val min: Int, val max: Int) : SettingsIntent
   ```

3. Add fields to `SettingsUiState.kt`:
   ```kotlin
   val compoundRepMin: Int,
   val compoundRepMax: Int,
   val isolationRepMin: Int,
   val isolationRepMax: Int,
   ```

4. Handle in `SettingsViewModel.kt` — persist to `UserProfileRepository`.

5. Validation: min >= 1, max <= 30, min < max, range width >= 2.

**Files:**
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsScreen.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsIntent.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsUiState.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsViewModel.kt`

**Effort:** Small-Medium (1-2 hr)
**Risk:** Low. RangeSlider UX needs care — discrete steps, clear labels.

---

## 2.1 Expose Exercise History Through Repository

**Problem:** `WorkoutSetDao.getCompletedSetsByExercise()` exists but is NOT exposed through `WorkoutSessionRepository`. The baseline generator cannot access past performance data.

**Fix/Implementation:**

1. Add to `WorkoutSessionRepository` interface:
   ```kotlin
   suspend fun getLastWorkingSetsForExercise(
       exerciseId: Long,
       sessionLimit: Int = 3
   ): List<HistoricalExerciseSession>
   ```

   Where `HistoricalExerciseSession` contains date + list of working sets (filtered, warm-up excluded).

2. Implement in `WorkoutSessionRepositoryImpl`:
   - Query completed sets via existing DAO method
   - Filter to `setType == WORKING` only
   - Group by session
   - Take last N sessions
   - Map to domain model

3. Alternatively, add a new DAO query that's more efficient:
   ```sql
   SELECT ws.*, we.exercise_id, wse.start_time as session_date
   FROM workout_sets ws
   INNER JOIN workout_exercises we ON ws.workout_exercise_id = we.id
   INNER JOIN workout_sessions wse ON we.session_id = wse.id
   WHERE we.exercise_id = :exerciseId
     AND ws.set_type = 'WORKING'
     AND ws.status = 'COMPLETED'
   ORDER BY wse.start_time DESC
   LIMIT :limit
   ```

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/repository/WorkoutSessionRepository.kt`
- `core/data/src/main/java/com/deepreps/core/data/repository/WorkoutSessionRepositoryImpl.kt`
- `core/database/src/main/java/com/deepreps/core/database/dao/WorkoutSetDao.kt` (new query)

**Effort:** Small (1 hr)
**Risk:** Low.

---

## 2.2 Implement Double-Progression Logic in BaselinePlanGenerator

**Problem:** `BaselinePlanGenerator` ignores `PlanRequest.trainingHistory` entirely. It generates fixed rep targets from hardcoded values. No weight adjustment based on past performance.

**Impact:** Every workout starts from scratch. Users see the same weights and reps regardless of progress. This is the #1 gap for offline users.

**Fix/Implementation:**

Replace `getTargetReps()` with a full `computeProgression()` method that implements double-progression:

```kotlin
data class ProgressionResult(
    val weightKg: Double,
    val targetReps: Int,    // within user's range
    val isStalled: Boolean, // 3+ sessions at same weight
)

fun computeProgression(
    exerciseId: Long,
    isCompound: Boolean,
    rangeMin: Int,          // from user settings
    rangeMax: Int,
    history: List<HistoricalSession>,  // last 3 sessions for this exercise
    bodyWeightKg: Double?,
    experienceLevel: ExperienceLevel,
): ProgressionResult
```

**Decision logic (from CSCS review):**

```
Input: last session's working sets for this exercise
Filter: only WORKING sets, only COMPLETED status

worstSetReps = MIN(reps across qualifying sets)
avgReps = AVG(reps across qualifying sets)
lastWeight = weight from last session's working sets

Case 1: worstSetReps >= rangeMax (all sets hit top of range)
  → nextWeight = lastWeight + increment
  → nextReps = rangeMin (reset after weight increase)
  → Cap: compound lower 10kg, compound upper 5kg, isolation 2.5kg

Case 2: avgReps >= rangeMin (on track, not maxed out)
  → nextWeight = lastWeight (hold)
  → nextReps = lastTargetReps + 1 (capped at rangeMax)

Case 3: avgReps < rangeMin AND worstSetReps < rangeMin - 2 (genuine struggle)
  → nextWeight = lastWeight * 0.95 (rounded to 1.25kg)
  → nextReps = rangeMin

Case 4: else (fatigue on last set only — hold)
  → nextWeight = lastWeight
  → nextReps = lastTargetReps

Stall detection:
  If same weight for 3 consecutive sessions → isStalled = true
```

**Weight increments:**

| Category | Increase | Decrease |
|----------|----------|----------|
| Lower body compound | +2.5 kg | -5% (round to 1.25) |
| Upper body compound | +1.25 kg | -5% (round to 1.25) |
| Isolation | +1.25 kg | -5% (round to 1.25) |

**First-time exercise (no history):** Fall back to existing BW ratio tables. This is the current behavior and remains valid as a cold-start.

**Stall warning:** When `isStalled == true`, add a note to `ExercisePlan.notes`: "Weight has been the same for 3 sessions. Consider a deload or trying a different rep scheme." Do NOT auto-deload in the fallback.

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/provider/BaselinePlanGenerator.kt` (major changes)
- New: `core/domain/src/main/java/com/deepreps/core/domain/calculator/ProgressionCalculator.kt` (extract pure logic)
- `core/domain/src/main/java/com/deepreps/core/domain/model/GeneratedPlan.kt` (if PlannedSet needs range fields)

**Effort:** Medium (3-4 hr)
**Risk:** Medium. Must handle edge cases: first session ever, exercise with zero history, user changed rep range between sessions, bodyweight exercises.

---

## 2.3 Update PlannedSet to Support Rep Ranges (Optional)

**Problem:** `PlannedSet.reps` is a single `Int`. The UI shows "10 reps" instead of "8-12 reps". This is technically functional (the progression logic targets a specific rep count within the range) but doesn't communicate the range to the user.

**Decision:** For MVP, keep `PlannedSet.reps` as a single target. The progression logic picks a specific target within the range. The user sees "do 10 reps" not "do 8-12 reps". This is simpler and matches how most apps work.

**Phase 2 enhancement:** Add `repRangeMin` and `repRangeMax` to `PlannedSet` so the UI can show "8-12" with the target highlighted. Requires UI changes in the active workout screen.

**Effort:** N/A (deferred)
**Risk:** N/A

---

## 2.4 Wire History Data into PlanRequest

**Problem:** `GeneratePlanUseCase` builds `PlanRequest` but may not populate `trainingHistory` correctly for the baseline generator path.

**Fix/Implementation:**

1. In `GeneratePlanUseCase`, before calling `BaselinePlanGenerator.generate()`:
   - For each exercise in the plan request, query last 3 sessions via the new repository method (item 2.1)
   - Populate `PlanRequest.trainingHistory` with real data
   - Also pass the user's rep range preferences from `UserProfile`

2. Ensure `BaselinePlanGenerator` receives rep ranges. Either:
   - Add `compoundRepRange` and `isolationRepRange` to `PlanRequest`
   - Or read them from `UserPlanProfile` (which maps from `UserProfile`)

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/usecase/GeneratePlanUseCase.kt`
- `core/domain/src/main/java/com/deepreps/core/domain/model/PlanRequest.kt` (add rep range fields)

**Effort:** Small (1 hr)
**Risk:** Low. Must not break the AI provider path (Gemini) which also uses PlanRequest.

---

## Execution Order

```
Phase A (Settings — can be done in parallel with Phase B):
  1.1 UserProfile + DB migration (1-2 hr)
  1.2 Settings UI for rep ranges (1-2 hr)  ← blocked by 1.1

Phase B (Progression Logic):
  2.1 Expose exercise history via repository (1 hr)
  2.2 ProgressionCalculator + BaselinePlanGenerator upgrade (3-4 hr) ← blocked by 2.1
  2.4 Wire history into PlanRequest (1 hr)  ← blocked by 2.1, 2.2

Dependency graph:
  1.1 → 1.2
  2.1 → 2.2 → 2.4
  1.1 + 2.1 can run in parallel (independent)
  2.2 needs 1.1 (for rep range fields in UserProfile)
```

**Total estimated effort:** 7-10 hours

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| Room migration fails on existing installs | High | Write migration test. Default rep range values for existing rows. |
| Progression logic produces unsafe weight jumps | High | Safety caps: 10kg lower compound, 5kg upper compound, 2.5kg isolation per session. |
| No history for new exercises (cold start) | Medium | Fall back to BW ratio tables (existing behavior). |
| User changes rep range mid-program | Low | Next plan generation picks up new range. No retroactive changes. |
| Stall detection false positives | Low | Require 3 consecutive sessions, not just 2. Use average + worst set, not worst set alone. |
| AI provider path (Gemini) breaks from PlanRequest changes | Medium | Add fields with defaults. Gemini prompt template must not break on new fields. |

---

## Verification Checklist

After implementation:
- [ ] Settings shows compound and isolation rep range sliders
- [ ] Changing experience level resets rep ranges to defaults
- [ ] Rep ranges persist across app restart
- [ ] First workout (no history): uses BW ratio tables, reps within user's range
- [ ] Second workout: targets adjusted based on first workout performance
- [ ] Hit max reps on all sets → next session increases weight, resets to min reps
- [ ] Miss min reps badly → next session decreases weight by ~5%
- [ ] Fatigue only on last set → weight holds
- [ ] Weight increase respects safety caps
- [ ] 3 sessions at same weight → stall note appears on exercise
- [ ] Warm-up sets excluded from progression logic
- [ ] Isolation exercises use isolation rep range, compounds use compound range
- [ ] AI path (Gemini) still works if API key is set
- [ ] `./gradlew assembleDebug testDebugUnitTest detekt lintDebug` passes
- [ ] Unit tests for ProgressionCalculator cover all 4 decision cases + edge cases
