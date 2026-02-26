# Implementation Plan: Navigation, Settings & Core Loop Completion

**Created:** 2026-02-26
**Status:** COMPLETED (2026-02-26)
**Scope:** 6 work items across 3 priorities

---

## Priority 1: Critical Path (Core Loop Completion)

### 1.1 Bug Fix: SkipSet Auto-Collapse

**Problem:** Skipping the last set of an exercise doesn't auto-minimize it and advance to the next exercise. Completing the last set does.

**Root Cause:** `handleCompleteSet()` in `WorkoutViewModel.kt` (lines 372-389) checks if all sets are done, then collapses the current exercise, expands the next one, scrolls to it, and starts a rest timer. `handleSkipSet()` (lines 509-531) only updates the set status — no auto-advance logic.

**Fix:**
1. Extract a `maybeAutoAdvanceExercise(workoutExerciseId: Long)` private method from `handleCompleteSet()` containing:
   - Check if exercise has remaining PLANNED/IN_PROGRESS sets
   - If none: compute `activeExerciseIndex`, update `isExpanded` flags, send `ScrollToExercise` side effect
2. Call `maybeAutoAdvanceExercise()` at the end of both `handleCompleteSet()` and `handleSkipSet()`
3. **No rest timer after skip-advance** — skipping implies the user wants to move on immediately
4. Update existing unit tests for `handleSkipSet` to verify auto-collapse behavior

**Files:**
- `feature/workout/src/main/java/com/deepreps/feature/workout/active/WorkoutViewModel.kt`
- `feature/workout/src/test/java/com/deepreps/feature/workout/active/WorkoutViewModelTest.kt`

**Effort:** Small (30-45 min)
**Risk:** Low. Edge case: user skips ALL sets of an exercise — should still auto-advance.

---

### 1.2 Wire Workout Summary Screen

**Problem:** Workout Summary is fully implemented (UI, ViewModel, navigation) but not registered in `DeepRepsNavHost.kt`. Completing a workout pops to home with zero feedback — no stats, no PRs, no "Save as Template" option.

**Impact:** Breaks the core retention loop. Every completed workout is a missed engagement hook.

**Fix:**
1. Import `workoutSummaryScreen` and `navigateToWorkoutSummary` in `DeepRepsNavHost.kt`
2. Register `workoutSummaryScreen(...)` in the NavHost builder with callbacks:
   - `onDismiss` → `navController.popBackStack(HOME_ROUTE, inclusive = false)`
   - `onNavigateToCreateTemplate` → `navController.navigateToCreateTemplate(exerciseIds)`
3. Replace line 144 (`navController.popBackStack(HOME_ROUTE, ...)`) with `navController.navigateToWorkoutSummary(sessionId)`
4. Add `popUpTo(HOME_ROUTE)` to clear the active workout from the back stack so back-press from summary goes home, not back to a completed workout

**Files:**
- `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt`

**Effort:** Small (30-60 min)
**Risk:** Low. The summary uses `ModalBottomSheet` — dismiss gesture + back stack pop may cause a brief empty screen flash. Cosmetic only.

---

### 1.3 Bottom Navigation Bar

**Problem:** No persistent navigation structure. Users can't access Progress, Library, or Settings. Everything is linear modal flows from a 2-button placeholder.

**Design Decision:** 4-tab `NavigationBar` (Material 3):

| Tab | Icon | Route | Module |
|-----|------|-------|--------|
| Home | `home` | `home` | `:app` |
| Library | `exercise` | `exercise_list` | `:feature:exercise-library` |
| Progress | `bar_chart` | `progress_dashboard` | `:feature:progress` |
| Profile | `person` | `profile_settings` | `:feature:profile` |

**Implementation:**
1. Create `BottomNavItem` sealed class in `:app` module with route, icon, label for each tab
2. Wrap `NavHost` content in `Scaffold` with `bottomBar` slot in `DeepRepsNavHost.kt` or `MainActivity.kt`
3. Use `navController.currentBackStackEntryAsState()` to drive active tab highlight
4. **Visibility rule:** Hide bottom bar during workout flows (setup, active workout, summary, plan review, onboarding). Show on all 4 top-level destinations and direct sub-screens (depth 1)
5. Tab switching uses `popUpTo(startDestination)` with `saveState = true`, `restoreState = true`, `launchSingleTop = true`
6. Hide animation: 200ms slide down (`motion-fast`). Show animation: 300ms slide up (`motion-standard`)

**Styling (per design-system.md 3.8):**
- Height: 80dp (68dp + 12dp gesture inset)
- Background: `surface-low` with `elevation-3`
- Active: filled icon, `accent-primary` tint, pill indicator
- Inactive: outlined icon, `on-surface-secondary` tint
- Labels: `label-small` (10sp, Medium weight)

**Files:**
- `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt` (major restructure)
- `app/src/main/java/com/deepreps/app/MainActivity.kt` (Scaffold wrapper)
- New: `app/src/main/java/com/deepreps/app/BottomNavItem.kt`

**Effort:** Medium (3-5 hours)
**Risk:** Medium. Bottom bar visibility logic is a common source of visual glitches. The crash recovery flow (`navigateToWorkoutSessionId` in `MainViewModel`) must still work. All `popBackStack(HOME_ROUTE, ...)` calls must be verified after restructure.

---

### 1.4 Wire Progress Dashboard

**Problem:** `ProgressDashboardScreen`, `ExerciseProgressScreen`, and `SessionDetailScreen` are fully implemented but not registered in `DeepRepsNavHost.kt`.

**Fix:**
1. Import and register all three progress screens in `DeepRepsNavHost.kt`:
   - `progressDashboardScreen(onNavigateToSessionDetail, onNavigateToExerciseProgress)`
   - `sessionDetailScreen(onNavigateBack)`
   - `exerciseProgressScreen(onNavigateBack)`
2. The Progress tab in bottom nav serves as the entry point (no separate button needed)

**Files:**
- `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt`

**Effort:** Small (30-60 min)
**Risk:** Low. Verify empty state handling when user has zero completed sessions.

---

## Priority 2: Essential UX (Same Sprint)

### 2.1 Settings/Profile Screen

**Problem:** `feature/profile/` module exists but is completely empty. Users cannot change unit preferences, analytics consent, or any settings after onboarding.

**Scope (MVP Settings):**

**Section 1: Profile**
- Experience level: SegmentedButton (Beginner / Intermediate / Advanced)
- Weight unit: SegmentedButton (kg / lbs)
- Body weight (optional): number input

**Section 2: Rest Timer Defaults**
- Warm-up rest: Slider 30-180s (15s steps)
- Working set rest: Slider 30-300s (15s steps)

**Section 3: Notifications**
- Rest timer notification: Switch
- Rest timer vibration: Switch

**Section 4: Privacy**
- Analytics consent: Switch (reads/writes `ConsentManager`)
- Performance monitoring: Switch (reads/writes `ConsentManager`)

**Section 5: About**
- Version number (static)
- Licenses (opens `OssLicensesActivity`)
- Privacy Policy (opens CustomTab)

**Implementation:**
1. Create MVI types in `feature/profile/`:
   - `SettingsUiState.kt`, `SettingsIntent.kt`, `SettingsSideEffect.kt`
2. Create `SettingsViewModel.kt`:
   - Reads from `UserProfileRepository`, `ConsentManager`
   - Writes on each toggle/change
3. Create `SettingsScreen.kt`:
   - Scrollable column with section cards
   - Each section: `surface-low` card, `radius-md`, items 56dp height
4. Create `navigation/SettingsNavigation.kt`:
   - Route: `"profile_settings"`
   - `NavGraphBuilder.settingsScreen(...)` extension
5. Register in `DeepRepsNavHost.kt`
6. Verify Hilt binding for `ConsentManager` exists in `DataModule.kt`

**Files:**
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsUiState.kt`
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsIntent.kt`
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsSideEffect.kt`
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsViewModel.kt`
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsScreen.kt`
- New: `feature/profile/src/main/java/com/deepreps/feature/profile/navigation/SettingsNavigation.kt`
- `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt`

**Effort:** Medium (3-4 hours)
**Risk:** Low-medium. Verify `ConsentManager` Hilt binding. Verify unit preference changes propagate reactively (not just read once at ViewModel init).

---

### 2.2 Home Screen Upgrade

**Problem:** `HomePlaceholder.kt` is two buttons. Needs to be a real dashboard.

**Content (top to bottom):**
1. **Top bar:** "Deep Reps" wordmark (headline-medium, left-aligned, 64dp, `surface-lowest`)
2. **Active workout banner** (conditional): Shows when a session is active/paused. Pulsing amber dot + timer + "Resume" button. 72dp, `status-warning-container`. Taps navigate to active workout. Uses `MainViewModel`'s existing crash recovery session ID.
3. **Primary CTA card:** "Start Workout" → muscle group selector. 120dp, `accent-primary` bg, full-width.
4. **Secondary card:** "From Template" → template list. 72dp, `surface-medium`, border `border-subtle`.
5. **Recent Templates** (horizontal scroll): Up to 5 most recently used. 200dp wide cards. "See All" link → template list. Hidden if no templates.
6. **Last Workout card:** Date, muscle groups, exercise count, duration, total volume. 96dp, `surface-low`. Tap → `SessionDetailScreen`. PR callout if applicable. Hidden if no completed sessions.

**Implementation:**
1. Create `HomeViewModel.kt` in `:app` module:
   - Queries last completed session from `WorkoutSessionRepository`
   - Queries recent templates from `TemplateRepository`
   - Observes active session state
2. Replace `HomePlaceholder.kt` content with the full layout
3. Add navigation callbacks for each interactive element

**Files:**
- `app/src/main/java/com/deepreps/app/HomePlaceholder.kt` (replace content)
- New: `app/src/main/java/com/deepreps/app/HomeViewModel.kt`
- `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt` (update home route callbacks)

**Effort:** Medium (3-4 hours)
**Risk:** Low. Mostly UI work with simple Room queries.

---

## Priority 3: Deferred (Phase 2)

### 3.1 Add Exercises to Template

**Decision:** Defer. "Save as Template" from Workout Summary covers the primary use case.

**When implemented:**
1. Add `[+ Add Exercise]` button at bottom of `CreateTemplateScreen`
2. Navigate to `ExerciseSelectionScreen` in "template mode" (all 7 groups visible, CTA: "Add to Template")
3. Use `SavedStateHandle` to pass selected exercise IDs back
4. `CreateTemplateViewModel` merges new exercises
5. Effort: 5-8 hours (navigation plumbing is the bulk)

### 3.2 Exercise Grouping (Supersets/Circuits)

**Decision:** Phase 2, Month 3. Not MVP.

**Current state (ready for Phase 2):**
- DB schema: `WorkoutExerciseEntity.supersetGroupId` (nullable Int) ✅
- Domain model: `WorkoutExercise.supersetGroupId` ✅
- Compatibility tags on all 78 exercises ✅
- Rules in `docs/exercise-science.md` Section 6.3 ✅

**Phase 2 scope:**
- Compatibility validation use case
- Group/ungroup use cases
- Rest timer rework (post-group timer)
- UI: visual grouping in ExerciseCard, long-press to create groups
- ViewModel: new intents for create/remove superset
- Estimated effort: 2-3 weeks

---

## Execution Order

```
┌─────────────────────────────────────────┐
│ Sprint 1 (Priority 1 - Critical Path)   │
│                                         │
│ Parallel Track A:                       │
│   1.1 SkipSet bug fix (30-45 min)      │
│   1.2 Wire Workout Summary (30-60 min) │
│                                         │
│ Then:                                   │
│   1.3 Bottom Navigation Bar (3-5 hrs)  │
│   1.4 Wire Progress Dashboard (30-60m) │
│                                         │
│ Sprint 1 Total: ~6-8 hours             │
├─────────────────────────────────────────┤
│ Sprint 2 (Priority 2 - Essential UX)   │
│                                         │
│ Parallel:                               │
│   2.1 Settings Screen (3-4 hrs)        │
│   2.2 Home Screen Upgrade (3-4 hrs)    │
│                                         │
│ Sprint 2 Total: ~4-8 hours             │
├─────────────────────────────────────────┤
│ Phase 2 (Deferred)                      │
│   3.1 Add Exercises to Template         │
│   3.2 Supersets/Circuits                │
└─────────────────────────────────────────┘
```

**Dependencies:**
- 1.3 (Bottom Nav) blocks 1.4 (Progress entry point) and 2.1 (Settings entry point)
- 1.2 (Workout Summary) should land before 2.2 (Home upgrade) since home references last workout data
- 1.1 (SkipSet fix) is independent — can be done first or in parallel

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| Bottom nav breaks crash recovery flow | High | Test `navigateToWorkoutSessionId` after restructure |
| `popBackStack(HOME_ROUTE)` calls break after nav restructure | Medium | Audit all 7+ call sites |
| ConsentManager not Hilt-injectable | Low | Add `@Provides` in DataModule if missing |
| Unit preference change doesn't propagate reactively | Medium | Verify `WeightUnit` flows through as Flow, not one-shot read |
| Empty Progress dashboard confuses new users | Low | Empty state already handled in ProgressDashboardScreen |
| Bottom bar flickers during transitions | Low | Use `AnimatedVisibility` with proper enter/exit specs |

---

## Verification Checklist

After implementation:
- [ ] Complete workout → Summary screen appears (not placeholder home)
- [ ] Summary "Save as Template" works
- [ ] Skip last set → exercise collapses, next expands
- [ ] Bottom nav visible on Home, Library, Progress, Profile
- [ ] Bottom nav hidden during workout setup, active workout, plan review, onboarding
- [ ] Tab switching preserves back stack state
- [ ] Progress tab shows empty state with zero sessions
- [ ] Progress tab shows session list after completing a workout
- [ ] Settings: unit change persists and reflects in workout screen
- [ ] Settings: consent toggles update ConsentManager
- [ ] Crash recovery still works (kill app during workout → resume prompt on relaunch)
- [ ] Back button behavior correct on all screens
- [ ] All existing unit tests pass
- [ ] `./gradlew assembleDebug testDebugUnitTest detekt lintDebug` passes
