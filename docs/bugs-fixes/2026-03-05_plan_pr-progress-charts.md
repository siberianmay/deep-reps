# Implementation Plan: Personal Records List + Dual-Line Progress Charts

**Created:** 2026-03-05
**Status:** IN PROGRESS
**Scope:** Add PR records list to Progress tab + dual-line chart (Max Weight + Estimated 1RM) on ExerciseProgressScreen

---

## Overview

Two enhancements to the Progress feature:

1. **Progress Dashboard** gains a "Records | History" segmented control. The Records view shows all personal records grouped by muscle group, tapping an exercise navigates to its progress chart.
2. **ExerciseProgressScreen** gains a second line on the chart: estimated 1RM (Epley formula) rendered alongside the existing max weight line. Both lines on one chart, two colors, shared Y-axis.

---

## Execution Order & Dependencies

```
Phase A: Domain & Data Layer (no UI)
  A.1  ChartDataPoint model expansion
  A.2  PrSummaryUi model + grouping logic
  A.3  ExerciseProgressViewModel dual-line data loading
  A.4  ProgressDashboardViewModel PR list loading

Phase B: UI Components
  B.1  ProgressChart dual-line rendering (depends on A.1)
  B.2  Chart legend component (depends on B.1)
  B.3  PR list composables (depends on A.2)
  B.4  Records/History segmented control (depends on B.3)

Phase C: Screen Integration
  C.1  ExerciseProgressScreen integration (depends on A.3, B.1, B.2)
  C.2  ProgressDashboardScreen integration (depends on A.4, B.3, B.4)
  C.3  Navigation wiring (depends on C.2)

Phase D: Testing & Polish
  D.1  Unit tests for 1RM chart data computation
  D.2  Unit tests for PR list grouping
  D.3  ViewModel tests
  D.4  Visual QA + edge cases
```

---

## A.1 Expand ChartDataPoint Model

**Problem:** `ChartDataPoint` only holds `weightKg`. Need to support dual-line chart with both max weight and estimated 1RM per session.

**Impact:** Foundation for all chart changes.

**Implementation:**

1. In `ExerciseProgressUiState.kt`, update `ChartDataPoint`:
   ```kotlin
   data class ChartDataPoint(
       val dateEpochMs: Long,
       val weightKg: Double,
       val estimated1rmKg: Double? = null,
       val confidence: Estimated1rmCalculator.Confidence? = null,
       val isPersonalRecord: Boolean = false,
   )
   ```
2. Add confidence import from `core.domain.util.Estimated1rmCalculator`.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ExerciseProgressUiState.kt`

**Effort:** Small (<1hr)
**Risk:** Low

---

## A.2 PrSummaryUi Model + Grouping Logic

**Problem:** No data model exists for the PR list on the dashboard.

**Impact:** Required for the Records tab.

**Implementation:**

1. Create `PrSummaryUi` in `ProgressDashboardUiState.kt`:
   ```kotlin
   data class PrSummaryUi(
       val exerciseId: Long,
       val exerciseName: String,
       val muscleGroup: MuscleGroup,
       val bestWeightKg: Double,
       val bestReps: Int?,
       val achievedAtText: String,
   )
   ```
2. Add `DashboardTab` enum:
   ```kotlin
   enum class DashboardTab { RECORDS, HISTORY }
   ```
3. Add to `ProgressDashboardUiState`:
   ```kotlin
   val selectedTab: DashboardTab = DashboardTab.HISTORY,
   val personalRecords: Map<MuscleGroup, List<PrSummaryUi>> = emptyMap(),
   val isRecordsLoading: Boolean = false,
   ```
4. Add intent:
   ```kotlin
   data class SelectTab(val tab: DashboardTab) : ProgressDashboardIntent
   ```

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ProgressDashboardUiState.kt`
- `feature/progress/src/main/java/com/deepreps/feature/progress/ProgressDashboardIntent.kt`

**Effort:** Small (<1hr)
**Risk:** Low

---

## A.3 ExerciseProgressViewModel Dual-Line Data Loading

**Problem:** `loadChartData()` only computes max weight per session. Need to also compute estimated 1RM per session for the second chart line.

**Impact:** Core logic for the dual-line chart.

**Implementation:**

1. In `loadChartData()`, for each session's sets:
   - Existing: find best weight (heaviest completed working set) -> `weightKg`
   - New: for each completed working set, call `Estimated1rmCalculator.calculateWithConfidence(weight, reps)`. Take the set with the highest `estimatedKg`. Store result + confidence.
   - Sets with 21+ reps return null from the calculator -- excluded from 1RM line (gap in line).
   - Populate `ChartDataPoint.estimated1rmKg` and `ChartDataPoint.confidence`.
2. Add summary fields to `ExerciseProgressUiState`:
   ```kotlin
   val currentBestEstimated1rmKg: Double? = null,
   val allTimeBestEstimated1rmKg: Double? = null,
   ```
3. Inject `Estimated1rmCalculator` usage (it's an object, no DI needed -- direct call).
4. Handle bodyweight exercises:
   - Look up exercise via `exerciseRepository.getExerciseById(exerciseId)`.
   - If `exercise.equipment == Equipment.BODYWEIGHT`:
     - Look up `userProfileRepository.get()?.bodyWeightKg`.
     - If body weight is set: for sets where `actualWeightKg` is null or 0, substitute body weight. For sets with `actualWeightKg > 0`, use `bodyWeightKg + actualWeightKg` (added weight scenario).
     - If body weight is NOT set: `estimated1rmKg` stays null for all points. Add `isBodyweightMissingProfile: Boolean` to state for the UI prompt.
   - Non-bodyweight exercises: no change.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ExerciseProgressViewModel.kt`
- `feature/progress/src/main/java/com/deepreps/feature/progress/ExerciseProgressUiState.kt`

**Effort:** Medium (1-3hr)
**Risk:** Medium -- bodyweight edge cases need careful handling. The `actualWeightKg` field semantics for bodyweight exercises must be verified (is it null, 0, or body weight?).

---

## A.4 ProgressDashboardViewModel PR List Loading

**Problem:** Dashboard ViewModel has no PR loading logic.

**Impact:** Required for the Records tab.

**Implementation:**

1. Inject `PersonalRecordRepository` and `ExerciseRepository` (already injected).
2. New method `loadPersonalRecords()`:
   - Call `personalRecordRepository.observeAll()`.
   - For each record, look up exercise name and primary muscle group via `exerciseRepository`.
   - Group by `MuscleGroup` (derived from `exercise.primaryGroupId`).
   - Sort groups by `MuscleGroup.entries` order (Legs, Lower Back, Chest, Back, Shoulders, Arms, Core).
   - Within each group, sort by `bestWeightKg` descending (strongest lifts first).
   - Filter: only include exercises that have at least one `RecordType.MAX_WEIGHT` record.
   - Map to `PrSummaryUi` with formatted date.
3. Call `loadPersonalRecords()` when `selectedTab == RECORDS` (lazy load -- don't fetch until tab is selected).
4. Add `handleSelectTab()` intent handler.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ProgressDashboardViewModel.kt`
- `feature/progress/src/main/java/com/deepreps/feature/progress/ProgressDashboardIntent.kt`

**Effort:** Medium (1-3hr)
**Risk:** Low -- straightforward query + mapping. Performance is fine at MVP scale (<100 exercises).

---

## B.1 ProgressChart Dual-Line Rendering

**Problem:** `ProgressChart` and `ChartCanvas` draw a single line. Need to render two lines with different colors.

**Impact:** Core visual change.

**Implementation:**

1. Add parameter to `ProgressChart`:
   ```kotlin
   secondaryLineColor: Color? = null,  // null = single line mode (backward compatible)
   ```
2. In `ChartCanvas`, compute Y-axis range across BOTH `weightKg` and `estimated1rmKg` values to ensure both lines fit.
3. Draw the max weight line first (blue, `accentPrimary`), then the 1RM line on top (purple, `accentSecondary`).
4. For the 1RM line:
   - Skip data points where `estimated1rmKg == null` (create a gap in the line).
   - Render dots based on confidence: solid filled circle for HIGH/MODERATE, hollow circle (stroke only) for LOW.
5. When both lines overlap at a point (1-rep max: 1RM == weight), let dots overlap naturally. The 1RM dot renders on top but at the same position.
6. PR gold dots apply to whichever line the PR was detected on (currently MAX_WEIGHT only).

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/components/ProgressChart.kt`

**Effort:** Medium (2-4hr)
**Risk:** Medium -- Canvas drawing complexity. Y-axis scaling must account for both lines. Line gap handling (null 1RM points) needs clean path segmentation.

---

## B.2 Chart Legend Component

**Problem:** With two lines on the chart, the user needs to know which is which.

**Impact:** Readability of the dual-line chart.

**Implementation:**

1. Add a `ChartLegend` composable below the chart canvas, above the summary row:
   ```
   [blue dot] Max Weight    [purple dot] Est. 1RM
   ```
2. Spec:
   - Row, horizontally centered, `space-4` gap between items.
   - Each item: 8dp colored circle + 4dp gap + label (`labelSmall`, `onSurfaceSecondary`).
   - Height: 24dp.
   - Only rendered when `secondaryLineColor != null` (dual-line mode).
3. Place between the chart canvas and the `HorizontalDivider` in `ProgressChart`.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/components/ProgressChart.kt`

**Effort:** Small (<1hr)
**Risk:** Low

---

## B.3 PR List Composables

**Problem:** No UI exists for the personal records list.

**Impact:** Core new UI for the Records tab.

**Implementation:**

1. Create `PersonalRecordsContent` composable in a new file `PersonalRecordsList.kt`:
   - `LazyColumn` with sticky headers per muscle group.
   - Section header: 48dp, muscle group name (`headlineSmall`), expand/collapse chevron. Sections expanded by default, not persisted.
   - PR row: 64dp, 3dp left-edge color bar (using `DeepRepsTheme.colors.colorForMuscleGroup()`), exercise name (`bodyLarge`), best weight + date (`bodySmall`, `onSurfaceSecondary`), chevron.
   - Empty state: "Complete your first workout to start tracking records" (use existing `EmptyState` component).
   - Loading state: use existing `LoadingIndicator`.
2. Tapping a PR row emits `ViewExerciseProgress(exerciseId)` intent (already exists).
3. Weight display respects user's preferred unit (kg/lbs conversion at UI layer).

**Files:**
- New: `feature/progress/src/main/java/com/deepreps/feature/progress/components/PersonalRecordsList.kt`

**Effort:** Medium (2-3hr)
**Risk:** Low

---

## B.4 Records/History Segmented Control

**Problem:** No tab switcher exists on the Progress dashboard.

**Impact:** Entry point for the Records view.

**Implementation:**

1. Create a `DashboardTabSelector` composable (or reuse `TimeRangeSelector` pattern with 2 segments).
   - Same visual style as `TimeRangeSelector`: 40dp tall, `surfaceLow`/`accentPrimaryContainer` selection, `labelLarge` text, `radiusSm`.
   - Two segments: "Records" | "History".
   - Placed immediately below TopAppBar, above TimeRangeSelector.
2. `TimeRangeSelector` is only rendered when `selectedTab == HISTORY`.
3. Emits `SelectTab(tab)` intent.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/components/DashboardTabSelector.kt` (new) or inline in `ProgressDashboardScreen.kt`

**Effort:** Small (<1hr)
**Risk:** Low

---

## C.1 ExerciseProgressScreen Integration

**Problem:** Screen currently renders single-line chart. Need to wire dual-line chart + updated summary row.

**Impact:** User-facing chart enhancement.

**Implementation:**

1. Pass `secondaryLineColor = DeepRepsTheme.colors.accentSecondary` to `ProgressChart` (activates dual-line mode).
2. Update chart title: "Progress" (generic, since both metrics are shown).
3. Update summary row:
   - Show: "Max: 80.0kg | Est. 1RM: 93.3kg | Change: +12.5kg"
   - Change is computed from the max weight line (simpler, more intuitive).
4. If `isBodyweightMissingProfile == true`, show inline card below the chart: "Add your body weight in Settings to enable 1RM tracking for bodyweight exercises" with a tappable link. The 1RM line simply doesn't render (null points).
5. Update previews.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ExerciseProgressScreen.kt`

**Effort:** Small (1-2hr)
**Risk:** Low

---

## C.2 ProgressDashboardScreen Integration

**Problem:** Dashboard only shows session history. Need to wire Records tab.

**Impact:** User-facing new feature.

**Implementation:**

1. Add `DashboardTabSelector` below TopAppBar.
2. Branch on `state.selectedTab`:
   - `HISTORY`: existing content (TimeRangeSelector + session list).
   - `RECORDS`: `PersonalRecordsContent(records = state.personalRecords, ...)`.
3. Wire `SelectTab` intent.
4. Update previews.

**Files:**
- `feature/progress/src/main/java/com/deepreps/feature/progress/ProgressDashboardScreen.kt`

**Effort:** Small (1hr)
**Risk:** Low

---

## C.3 Navigation Wiring

**Problem:** Records tab items need to navigate to ExerciseProgressScreen.

**Impact:** Feature completeness.

**Implementation:**

1. Verify `onNavigateToExerciseProgress` callback is already wired in `ProgressDashboardScreen` -> yes, already exists.
2. The `ViewExerciseProgress` intent already triggers `NavigateToExerciseProgress` side effect -> already wired.
3. No new navigation routes needed. ExerciseProgressScreen already accepts `exerciseId` nav arg.
4. Verify in `DeepRepsNavHost.kt` that the exercise progress route is registered -> should already be there.

**Files:**
- Verify: `app/src/main/java/com/deepreps/app/DeepRepsNavHost.kt`

**Effort:** Small (<30min)
**Risk:** Low

---

## D.1 Unit Tests: 1RM Chart Data Computation

**Implementation:**

1. Test `ExerciseProgressViewModel` chart data loading:
   - Session with 5 sets at different weights/reps -> verify `estimated1rmKg` uses highest Epley estimate.
   - Session with only 25-rep sets -> verify `estimated1rmKg` is null (gap).
   - Bodyweight exercise with profile weight set -> verify substitution.
   - Bodyweight exercise without profile weight -> verify `estimated1rmKg` is null, `isBodyweightMissingProfile` is true.
   - Single-rep set -> verify `estimated1rmKg == weightKg`.
   - Confidence levels: 3 reps = HIGH, 8 reps = MODERATE, 15 reps = LOW.

**Files:**
- `feature/progress/src/test/java/com/deepreps/feature/progress/ExerciseProgressViewModelTest.kt`

**Effort:** Medium (1-2hr)
**Risk:** Low

---

## D.2 Unit Tests: PR List Grouping

**Implementation:**

1. Test `ProgressDashboardViewModel` PR loading:
   - Multiple exercises across groups -> verify correct grouping.
   - Exercises with no records -> verify excluded.
   - Sort order within group (heaviest first).
   - Weight unit conversion applied to display.

**Files:**
- `feature/progress/src/test/java/com/deepreps/feature/progress/ProgressDashboardViewModelTest.kt`

**Effort:** Small (1hr)
**Risk:** Low

---

## D.3 ViewModel Tests

**Implementation:**

1. Test tab switching intent on dashboard.
2. Test lazy loading: PR data only fetched when Records tab selected.
3. Test time range filtering still works on History tab.
4. Test error states for both tabs.

**Files:**
- `feature/progress/src/test/java/com/deepreps/feature/progress/ProgressDashboardViewModelTest.kt`
- `feature/progress/src/test/java/com/deepreps/feature/progress/ExerciseProgressViewModelTest.kt`

**Effort:** Small (1hr)
**Risk:** Low

---

## D.4 Visual QA + Edge Cases

**Implementation:**

1. Verify dual-line chart renders correctly:
   - Both lines visible, correct colors, Y-axis accommodates both.
   - Gaps in 1RM line when data points have null 1RM.
   - Overlap at 1-rep max points.
   - Hollow dots for LOW confidence points.
   - PR gold dots on correct line.
2. Verify PR list:
   - Muscle group sections expand/collapse.
   - Color bars match muscle group.
   - Tapping navigates to correct exercise.
   - Empty state renders.
3. Edge cases:
   - Exercise with only 1 session -> single dot on both lines.
   - Exercise with all 21+ rep sets -> max weight line only, no 1RM line.
   - No completed sessions -> empty state on both tabs.
4. Emulator test on Android 15.

**Files:** N/A (manual testing)

**Effort:** Medium (1-2hr)
**Risk:** Low

---

## Risk Register

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Y-axis scaling with two lines creates too much whitespace when 1RM is much higher than max weight | Medium | Low | Use padded range that includes both min/max across both lines. 10% padding is already in place. |
| `actualWeightKg` semantics unclear for bodyweight exercises (null vs 0 vs body weight) | Medium | Medium | Verify against `PrepopulateCallback.kt` and `WorkoutViewModel` set completion logic before implementing A.3. |
| Canvas performance with two lines + dots on large datasets (50+ sessions) | Low | Low | Current single-line chart handles this scale. Second line is O(n) additional draw calls. |
| PR list query joins slow with many exercises | Low | Low | At MVP scale (<100 exercises, <1000 records), this is negligible. |
| Users confused by dual-line chart without understanding 1RM | Medium | Low | Legend component (B.2) provides labels. Info icon on "Est. 1RM" label with one-sentence tooltip is a Phase 2 polish item. |

---

## Verification Checklist

- [ ] ExerciseProgressScreen shows two lines (blue = max weight, purple = est. 1RM) on the chart
- [ ] Y-axis accommodates both lines without clipping
- [ ] Legend below chart correctly labels both lines
- [ ] 1RM line has gaps where estimation is not possible (21+ reps only sessions)
- [ ] LOW confidence data points render as hollow dots on the 1RM line
- [ ] Overlap points (1-rep max) render cleanly without visual artifacts
- [ ] Summary row shows both "Max" and "Est. 1RM" current values
- [ ] Bodyweight exercises with profile weight set show correct 1RM (body weight + added weight)
- [ ] Bodyweight exercises without profile weight: 1RM line absent, inline prompt shown
- [ ] Progress Dashboard has "Records | History" segmented control
- [ ] Default tab is History (existing behavior preserved)
- [ ] Records tab shows PR list grouped by muscle group
- [ ] Only exercises with completed sets appear in PR list
- [ ] PR list items show exercise name, best weight (in user's unit), date achieved
- [ ] Tapping a PR list item navigates to ExerciseProgressScreen for that exercise
- [ ] Muscle group color bars render correctly on PR list items
- [ ] Sections are collapsible
- [ ] Empty state renders when no PRs exist
- [ ] TimeRangeSelector hidden on Records tab, visible on History tab
- [ ] All existing ExerciseProgressViewModelTest tests still pass
- [ ] New unit tests for 1RM computation and PR grouping pass
- [ ] `assembleDebug testDebugUnitTest detekt lintDebug` passes

---

## Total Estimated Effort

| Phase | Effort |
|-------|--------|
| A: Domain & Data | 3-6hr |
| B: UI Components | 4-8hr |
| C: Screen Integration | 2-4hr |
| D: Testing & Polish | 3-6hr |
| **Total** | **12-24hr** |
