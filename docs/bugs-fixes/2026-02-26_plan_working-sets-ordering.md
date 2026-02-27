# Implementation Plan: Working Sets Setting & Exercise Ordering Fix

**Created:** 2026-02-26
**Status:** COMPLETED (2026-02-26)
**Scope:** Add user-configurable working sets per exercise + fix exercise ordering in plan generation

---

## 1.1 Fix Exercise Ordering in Plan Generation (BUG-8)

**Problem:** `getExercisesByIds()` returns exercises in Room PK order, discarding the user's drag-to-reorder ordering.

**Impact:** Core workflow broken — user reorders exercises but the plan ignores it.

**Fix/Implementation:**

In `PlanReviewViewModel.buildExercisesForPlan()`, after fetching exercises from the repository, re-sort them to match the original `exerciseIds` order:

```kotlin
val exercises = exerciseRepository.getExercisesByIds(exerciseIds)
val exerciseMap = exercises.associateBy { it.id }
val orderedExercises = exerciseIds.mapNotNull { id -> exerciseMap[id] }
```

This preserves the user's ordering while still fetching full exercise data from Room.

**Files:**
- `feature/ai-plan/src/main/java/com/deepreps/feature/aiplan/PlanReviewViewModel.kt` (modify `buildExercisesForPlan`)

**Effort:** Small (<30 min)
**Risk:** Low. Pure reordering of already-fetched data.

---

## 2.1 Add Working Sets Field to UserProfile & Database

**Problem:** Working set count is hardcoded per experience level (Beginner=3, Intermediate=4, Advanced=5). User wants to override this globally.

**Impact:** Users doing multi-muscle-group sessions or preferring lower volume cannot configure this without manually editing every exercise after plan generation.

**Fix/Implementation:**

1. Add `defaultWorkingSets: Int` to `UserProfile` domain model (default: 0 = use experience-level default)
2. Add column to `UserProfileEntity`: `@ColumnInfo(name = "default_working_sets", defaultValue = "0") val defaultWorkingSets: Int = 0`
3. Room migration v3→v4: `ALTER TABLE user_profile ADD COLUMN default_working_sets INTEGER NOT NULL DEFAULT 0`
4. Update mappers in `UserProfileMapper.kt`
5. Add to `UserPlanProfile`: `val defaultWorkingSets: Int = 0`

Semantics: `0` means "use experience-level default" (current behavior). Any value 1-10 overrides the default.

Default values by experience level (unchanged, used when `defaultWorkingSets == 0`):
- Beginner: 3
- Intermediate: 4
- Advanced: 5

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/model/UserProfile.kt`
- `core/domain/src/main/java/com/deepreps/core/domain/model/PlanRequest.kt` (UserPlanProfile)
- `core/database/src/main/java/com/deepreps/core/database/entity/UserProfileEntity.kt`
- `core/data/src/main/java/com/deepreps/core/data/mapper/UserProfileMapper.kt`
- New: `core/database/src/main/java/com/deepreps/core/database/migration/Migration3To4.kt`
- `core/database/src/main/java/com/deepreps/core/database/DeepRepsDatabase.kt` (version bump, add migration)

**Effort:** Small (1 hr)
**Risk:** Low. Room migration with default value.

---

## 2.2 Add Working Sets UI to Settings Screen

**Problem:** No UI for working sets preference.

**Fix/Implementation:**

Add a "Working Sets per Exercise" row in the Settings screen's "Rep Ranges" section (or rename section to "Training Preferences"). Use a simple row with +/- stepper or a dropdown (3, 4, 5, or "Auto" which maps to 0).

Display: "Working Sets per Exercise: Auto (4)" where "Auto" shows the experience-level default in parentheses.

1. Add to `SettingsUiState`: `val defaultWorkingSets: Int` (0=auto)
2. Add to `SettingsIntent`: `data class SetDefaultWorkingSets(val count: Int) : SettingsIntent`
3. Add UI row with stepper (range 2-6, plus "Auto" option at 0)
4. Handle in `SettingsViewModel` — persist to UserProfileRepository

**Files:**
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsScreen.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsUiState.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsIntent.kt`
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsViewModel.kt`

**Effort:** Small (1 hr)
**Risk:** Low.

---

## 2.3 Wire Working Sets into BaselinePlanGenerator

**Problem:** `getWorkingSetsCount()` only reads experience level. Needs to respect user override.

**Fix/Implementation:**

1. In `BaselinePlanGenerator.generate()`, read `request.userProfile.defaultWorkingSets`
2. Modify `getWorkingSetsCount()` to accept override:
   ```kotlin
   private fun getWorkingSetsCount(level: Int, deloadRecommended: Boolean, override: Int): Int {
       val baseSets = if (override > 0) override else when (level) {
           1 -> 3; 2 -> 4; 3 -> 5; else -> 3
       }
       return if (deloadRecommended) (baseSets * 0.5).toInt().coerceAtLeast(2) else baseSets
   }
   ```

3. Wire through `PlanReviewViewModel.buildPlanRequest()` — include `defaultWorkingSets` in `UserPlanProfile`

**Files:**
- `core/domain/src/main/java/com/deepreps/core/domain/provider/BaselinePlanGenerator.kt`
- `feature/ai-plan/src/main/java/com/deepreps/feature/aiplan/PlanReviewViewModel.kt`

**Effort:** Small (<30 min)
**Risk:** Low. Deload logic still applies on top of override.

---

## Execution Order

```
1.1 Fix exercise ordering (<30 min) — no dependencies
2.1 UserProfile + DB migration (1 hr) — no dependencies
2.2 Settings UI (1 hr) — blocked by 2.1
2.3 Wire into generator (<30 min) — blocked by 2.1
```

1.1 and 2.1 can run in parallel. Total: ~3 hours.

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| Room migration v3→v4 fails on existing installs | High | Default value 0 for new column. Test migration. |
| User sets 2 working sets + Advanced = very low volume | Low | Valid choice. Deload halves it further (min 1). Set floor at 1, not 0. |
| Override interacts badly with deload | Low | Deload reduces override by 50% same as default. Consistent behavior. |

---

## Verification Checklist

- [ ] User reorders exercises → Generate Plan → plan shows exercises in user's order
- [ ] Settings shows "Working Sets per Exercise" with Auto + manual options
- [ ] Auto mode uses experience-level defaults (3/4/5)
- [ ] Manual override (e.g., 3) generates plans with 3 working sets
- [ ] Deload week reduces overridden count by 50% (min 2)
- [ ] Setting persists across app restart
- [ ] Changing experience level does NOT reset working sets override
- [ ] `./gradlew assembleDebug testDebugUnitTest detekt lintDebug` passes
