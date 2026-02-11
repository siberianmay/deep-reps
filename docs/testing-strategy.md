# Testing Strategy -- Deep Reps

**Document Owner:** QA Engineer / Mobile Test Specialist
**Last Updated:** 2026-02-11
**Status:** Approved for implementation
**Architecture Reference:** `docs/architecture.md`
**Feature Reference:** `FEATURES.md`

This document defines the complete testing strategy for Deep Reps. Every feature in FEATURES.md has corresponding test coverage defined here. Every architectural component in architecture.md has a testing approach specified. If something is not tested according to this document, it is not shippable.

---

## Table of Contents

1. [Test Strategy Overview](#1-test-strategy-overview)
2. [Unit Test Plan](#2-unit-test-plan)
3. [Integration Test Plan](#3-integration-test-plan)
4. [UI / E2E Test Plan](#4-ui--e2e-test-plan)
5. [Android-Specific Tests](#5-android-specific-tests)
6. [Device Compatibility Matrix](#6-device-compatibility-matrix)
7. [Performance Test Plan](#7-performance-test-plan)
8. [Data Integrity Tests](#8-data-integrity-tests)
9. [Regression Test Plan](#9-regression-test-plan)
10. [Bug Reporting Template](#10-bug-reporting-template)

---

## 1. Test Strategy Overview

### 1.1 Testing Philosophy

**Core principle: Data integrity is non-negotiable.** A single workout data loss bug destroys user trust permanently. All testing priorities flow from this constraint.

**Risk-based prioritization:**

1. **Critical path (P0):** Workout data persistence, state restoration after process death, crash recovery with no set data loss.
2. **Functional correctness (P1):** AI plan generation fallback chain, PR detection accuracy, 1RM calculations, progress metric calculations.
3. **User experience (P2):** Rest timer accuracy across backgrounding, offline fallback behavior, UI responsiveness during active workout.
4. **Polish (P3):** Animation smoothness, accessibility compliance, edge-case UX (empty states, boundary values).

### 1.2 Testing Pyramid

```
          /  E2E  \            6 critical flows automated
         / (slow,  \           Espresso + UI Automator
        / expensive)\          Run: pre-release, nightly
       +-----------+
      / Integration  \         Room DAOs, Repository+ViewModel,
     /  (medium cost) \        AI mock provider, session lifecycle
    +------------------+       Run: per PR (instrumented), nightly
   /     Unit Tests     \      Domain logic, ViewModels, mappers,
  /    (fast, cheap)     \     calculations, state machine
 +------------------------+    Run: every commit, every PR
```

**Rationale:** Most defects are caught cheapest at the unit level. Integration tests verify that Room queries return correct data and that repository-to-ViewModel wiring works. E2E tests are reserved exclusively for workflows where failure is catastrophic (data loss, crash on critical path, broken onboarding). Maintaining E2E tests is expensive -- every new E2E test must justify its existence by covering a scenario that unit + integration tests cannot catch.

### 1.3 Coverage Targets

| Test Layer | Minimum Coverage | Measurement Tool | Enforcement |
|------------|------------------|------------------|-------------|
| **Unit Tests** | 80% line coverage | JaCoCo | CI gate -- PR fails below threshold |
| **Integration Tests** | 70% of data paths | JaCoCo (instrumented) | Tracked per module, reviewed quarterly |
| **UI Tests** | N/A (scenario-based) | Functional coverage | Every screen has at least one happy-path test |
| **E2E Tests** | 100% of critical paths | See section 4.1 | 6 mandatory flows; all must pass for release |

**What counts as "covered":**

- Every domain calculation (1RM, volume, PR detection) has unit tests with boundary values.
- Every repository method that writes data has integration tests verifying persistence.
- Every DAO query has at least one test with realistic data.
- Every screen has at least one Compose UI test for the happy path.
- Every critical user flow (onboarding through workout completion) has an E2E test.

### 1.4 Testing Tools

| Tool | Version | Purpose | Module Scope |
|------|---------|---------|--------------|
| **JUnit 5** | 5.11.x | Test framework for unit tests | All modules |
| **JUnit 4** | 4.13.x | Required by Android instrumentation (Compose test rules, Room test helpers) | `:core:database`, feature `androidTest` |
| **MockK** | 1.13.x | Kotlin-native mocking -- coroutines, suspend functions, extension functions | All unit test suites |
| **Turbine** | 1.2.x | Testing Kotlin Flows -- asserting StateFlow emissions from ViewModels | `:feature:*` ViewModel tests |
| **Google Truth** | 1.4.x | Assertion library -- readable fluent assertions | All test suites |
| **Compose UI Testing** | BOM-managed | `createComposeRule()`, semantic assertions, node finders | `:feature:*` UI tests |
| **Espresso** | 3.6.x | View-based assertions when Compose interop is needed | `:app` E2E tests |
| **UI Automator** | 2.3.x | Cross-app interactions (notifications, system dialogs, process death simulation) | `:app` E2E tests |
| **Robolectric** | 4.13.x | Unit-testing Android framework code without emulator. Used sparingly. | `:core:data`, `:core:network` |
| **Room Testing** | 2.7.x | `MigrationTestHelper` for migration verification | `:core:database` |
| **Ktor Mock Engine** | 3.1.x | Mock HTTP responses for Gemini API tests | `:core:network` |
| **kotlinx-coroutines-test** | 1.9.x | `UnconfinedTestDispatcher`, `runTest`, `advanceTimeBy` | All coroutine-based tests |
| **Macrobenchmark** | 1.3.x | Startup time, frame timing, baseline profile generation | `:benchmark` module |
| **JaCoCo** | 0.8.x | Code coverage reporting and CI threshold enforcement | All modules |
| **LeakCanary** | 2.14 | Memory leak detection in debug builds | `:app` debug variant |

### 1.5 CI Integration

**Per-PR pipeline (GitHub Actions -- `ci.yml`):**

1. `./gradlew lintDebug` -- Static analysis.
2. `./gradlew testDebugUnitTest` -- All unit tests across all modules.
3. `./gradlew jacocoTestReport` -- Coverage report. Fail if `:core:domain` < 80%.
4. `./gradlew assembleDebug` -- Build verification.
5. Upload test results as artifacts.

**Nightly pipeline (scheduled):**

1. Full unit test suite.
2. Instrumented integration tests on Firebase Test Lab (API 28 + API 34 emulators).
3. E2E smoke suite (6 critical flows).
4. Macrobenchmark baseline profile generation.
5. APK size tracking.

**Pre-release pipeline (manual trigger or tag push):**

1. Full unit + integration + E2E suite on Firebase Test Lab device matrix.
2. Full Macrobenchmark performance suite.
3. Monkey testing (10,000 events).
4. Coverage report for all modules.

### 1.6 Test File Organization

Tests follow the architecture module structure. Every source file `Foo.kt` has a corresponding `FooTest.kt` in the same module's test source set.

```
module/src/test/         -- JVM unit tests (JUnit 5, MockK, Turbine)
module/src/androidTest/  -- Instrumented tests (JUnit 4, Room, Compose)
```

---

## 2. Unit Test Plan

All unit tests run on the JVM. No Android framework dependency. No emulator required. Target: sub-10-second execution for the entire unit test suite.

### 2.1 Domain Layer -- Workout State Machine

**Class under test:** `WorkoutPhase` sealed interface and state transition logic.

The workout state machine (Idle -> Setup -> GeneratingPlan -> Active -> Paused/Completed) is the single most important piece of domain logic. Every invalid transition must be rejected.

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Idle to Setup on exercise selection | `WorkoutPhase.Idle` + select exercises | `WorkoutPhase.Setup(selectedExercises)` |
| 2 | Setup to GeneratingPlan on plan request | `WorkoutPhase.Setup` + generate intent | `WorkoutPhase.GeneratingPlan` |
| 3 | GeneratingPlan to Active on plan received | `WorkoutPhase.GeneratingPlan` + plan result | `WorkoutPhase.Active(startedAtMillis)` |
| 4 | GeneratingPlan to Active on fallback (offline) | `WorkoutPhase.GeneratingPlan` + fallback plan | `WorkoutPhase.Active(startedAtMillis)` |
| 5 | Active to Paused on pause intent | `WorkoutPhase.Active` + pause | `WorkoutPhase.Paused(pausedAtMillis, accumulated)` |
| 6 | Paused to Active on resume intent | `WorkoutPhase.Paused` + resume | `WorkoutPhase.Active(adjustedStart)` |
| 7 | Active to Completed on finish intent | `WorkoutPhase.Active` + finish | `WorkoutPhase.Completed(sessionId)` |
| 8 | Reject Idle to Active (no setup) | `WorkoutPhase.Idle` + finish intent | No transition, state unchanged |
| 9 | Reject Completed to Active (re-entry) | `WorkoutPhase.Completed` + resume intent | No transition, state unchanged |
| 10 | Paused to Completed (finish while paused) | `WorkoutPhase.Paused` + finish intent | `WorkoutPhase.Completed(sessionId)` |

### 2.2 Domain Layer -- Estimated 1RM Calculations

**Class under test:** `Estimated1rmCalculator`

Formulas: Epley (`weight * (1 + reps / 30)`) and Brzycki (`weight * 36 / (37 - reps)`).

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Epley formula: standard input | 100kg x 8 reps | 126.67 (rounded to 2 decimals) |
| 2 | Brzycki formula: standard input | 100kg x 8 reps | 124.14 (rounded to 2 decimals) |
| 3 | 1 rep (actual max) | 150kg x 1 rep | 150.0 (both formulas) |
| 4 | High rep count (15 reps) | 60kg x 15 reps | Epley: 90.0, verify Brzycki diverges |
| 5 | Zero reps | 100kg x 0 reps | 0.0 or null (no meaningful 1RM) |
| 6 | Negative weight | -50kg x 5 reps | `IllegalArgumentException` or 0.0 |
| 7 | Very high reps (30+) | 40kg x 35 reps | Epley: reasonable value, Brzycki: may produce negative/infinite -- verify guard |
| 8 | Boundary: reps = 37 for Brzycki | 100kg x 37 reps | Brzycki denominator = 0 -- verify no division by zero |
| 9 | Floating point weight | 72.5kg x 6 reps | Both formulas produce valid result |
| 10 | Unit-agnostic (lbs input) | 225lbs x 5 reps | Calculator works on raw numbers, unit irrelevant |

### 2.3 Domain Layer -- PR Detection

**Class under test:** `CalculatePersonalRecordsUseCase`

PR types: weight PR, rep PR, estimated 1RM PR, volume PR.

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | New weight PR detected | Previous best: 100kg, new set: 105kg x 5 | `PersonalRecord(type=weight, weight=105)` |
| 2 | No PR when weight equals previous best | Previous best: 100kg, new set: 100kg x 5 | No new PR emitted |
| 3 | Rep PR at same weight | Previous best: 100kg x 6, new set: 100kg x 8 | `PersonalRecord(type=reps, reps=8, weight=100)` |
| 4 | Estimated 1RM PR | Previous best 1RM: 120kg, new set yields 1RM: 125kg | `PersonalRecord(type=estimated_1rm, estimated1rm=125)` |
| 5 | Volume PR (session) | Previous best volume: 5000kg, new session: 5500kg | `PersonalRecord(type=volume)` |
| 6 | Multiple PRs in single session | New weight PR + new rep PR in same workout | Both PRs returned in list |
| 7 | No PR on first-ever workout | No history, first set: 80kg x 10 | All records are PRs (first entry = baseline) |
| 8 | Warm-up sets excluded from weight PR | Working set: 80kg, warm-up set: 90kg x 2 (hypothetical) | Weight PR should only consider working sets per business rule |
| 9 | PR detection after set edit | User edits completed set from 100kg to 95kg | Previous PR at 100kg should be re-evaluated |
| 10 | PR across different exercises | Bench press PR and squat PR in same session | PRs tracked per-exercise, no cross-contamination |
| 11 | Estimated 1RM not triggered from high-rep set (>10 reps) | Previous best 1RM: 105kg (from 90kg x 5). New set: 60kg x 15 (Epley: ~90kg). | No 1RM PR — sets over 10 reps are excluded from 1RM estimation per `exercise-science.md` Section 7.5 |

### 2.4 Domain Layer -- Exercise Ordering

**Class under test:** Auto-ordering logic from FEATURES.md section 2.3.

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Compounds before isolations | [Bicep curl (iso), Squat (compound)] | [Squat, Bicep curl] |
| 2 | Larger muscles before smaller within compounds | [Lunges, Squats, Deadlifts] | [Squats/Deadlifts before Lunges] |
| 3 | Single exercise (no ordering needed) | [Bench press] | [Bench press] |
| 4 | All isolation exercises | [Bicep curl, Tricep pushdown, Lateral raise] | CSCS-defined ordering preserved |
| 5 | All compound exercises | [Squat, Bench press, Barbell row] | Ordered by muscle group size |
| 6 | Mixed groups (multi-group workout) | Chest + Arms exercises | Chest compounds first, then arm isolations |
| 7 | Empty exercise list | [] | [] (no crash) |
| 8 | Exercises from same group, mixed isolation levels | [Pec fly (iso), Bench press (compound), Incline DB press (compound)] | [Bench, Incline DB, Pec fly] |
| 9 | Core exercises always last (Rule 5) | [Ab Wheel Rollout (core, compound), Barbell Back Squat (legs, compound)] | [Squat, Ab Wheel Rollout] — core always last regardless of compound/isolation |
| 10 | Difficulty tiebreaker within same group/type | [DB Shoulder Press (beginner, shoulder, compound), OHP (intermediate, shoulder, compound), Arnold Press (intermediate, shoulder, compound)] | [OHP or Arnold first (intermediate), then DB Shoulder Press (beginner)] |
| 11 | Full multi-group with core | Chest compounds + Arms compounds + Arms isolations + Core exercises | Chest compounds, Arms compounds, Chest isolations, Arms isolations, Core exercises (all core at end) |

### 2.5 Domain Layer -- Volume Calculations

**Class under test:** `VolumeCalculator`

Volume = sets x reps x weight. Per-exercise, per-muscle-group, per-session.

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Single exercise volume | 3 sets x 10 reps x 80kg | 2400kg |
| 2 | Multi-exercise session volume | Exercise A: 2400kg, Exercise B: 1500kg | 3900kg total |
| 3 | Per-muscle-group volume | Chest exercises: 3000kg, Back exercises: 2500kg | `{chest: 3000, back: 2500}` |
| 4 | Warm-up sets excluded from volume | 2 warm-up sets + 4 working sets | Only working set volume counted |
| 5 | Zero reps set (skipped) | 0 reps x 100kg | 0 volume for that set |
| 6 | Float precision (72.5kg x 8 reps x 3 sets) | 72.5 x 8 x 3 | 1740.0 (verify no float drift) |
| 7 | Weekly volume aggregation | 3 sessions with chest work | Sum of chest volume across sessions |
| 8 | Total tonnage for session | All exercises, all working sets | Sum of all (weight x reps) across session |
| 9 | Volume with partially completed sets | 4 planned sets, only 3 completed | Volume from 3 completed sets only |
| 10 | Volume comparison (current vs previous session) | Session A: 3000kg, Session B: 3200kg | Delta: +200kg, +6.67% |

### 2.6 Domain Layer -- AI Plan Generation Fallback Chain

**Class under test:** `GeneratePlanUseCase`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Online + AI success | Connected, Gemini returns valid JSON | `PlanResult.AiGenerated(plan)` |
| 2 | Online + AI failure -> cached plan | Connected, Gemini 500, cached plan < 7 days | `PlanResult.Cached(plan)` |
| 3 | Online + AI failure -> no cache -> baseline | Connected, Gemini 500, no cache | `PlanResult.Baseline(plan)` |
| 4 | Online + AI failure -> no cache -> no baseline -> manual | Connected, all fallbacks fail | `PlanResult.Manual` |
| 5 | Offline -> cached plan | Not connected, cached plan exists | `PlanResult.Cached(plan)` |
| 6 | Offline -> no cache -> baseline | Not connected, no cache | `PlanResult.Baseline(plan)` |
| 7 | Offline -> manual entry | Not connected, no cache, no baseline | `PlanResult.Manual` |
| 8 | Cached plan expired (> 7 days) | Offline, cached plan 8 days old | Skip cache, fall through to baseline |
| 9 | AI timeout (30s) | Connected, Gemini hangs | Falls through to cached/baseline after timeout |
| 10 | AI returns malformed JSON | Connected, Gemini returns invalid JSON | Parsing fails, falls through to cached/baseline |

### 2.7 Domain Layer -- AI Prompt Builder

**Class under test:** `GeminiPromptBuilder`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Prompt includes experience level | Beginner user | Prompt contains "Total Beginner (0-6 months)" |
| 2 | Prompt includes body weight when provided | 80kg user | Prompt contains "Body weight: 80.0kg" |
| 3 | Prompt omits body weight when null | No body weight | Prompt does not contain "Body weight" line |
| 4 | Prompt includes all exercises in order | 3 exercises | All 3 listed with index, name, equipment, isolation level |
| 5 | Prompt includes training history | 2 sessions of history | History section present with sets |
| 6 | Prompt omits history section when empty | No prior sessions | No "Recent Training History" section |
| 7 | Prompt truncates history if exceeding token budget | 10 sessions of history, budget allows 3 | Only last 3 sessions included |
| 8 | Output format JSON schema present | Any input | JSON schema block present in prompt |
| 9 | Prompt includes safety constraint: max weight jump | Any input | Prompt contains "Never suggest a working weight more than 10%" |
| 10 | Prompt includes safety constraint: MRV ceiling | Any input | Prompt contains MRV ceiling reference |
| 11 | Prompt includes safety constraint: advanced exercise gating | Beginner user | Prompt contains "Only include 'advanced' exercises if experience level >= 2" |
| 12 | Prompt includes safety constraint: warm-up requirement | Any input | Prompt contains "at least 1 warm-up set per compound" |
| 13 | Prompt includes age modifier for user over 50 | Age = 55 | Prompt contains age-adjusted safety modifier from Section 8.6 |
| 14 | Prompt includes cross-group fatigue warning | Chest + Shoulders + Arms selected | Prompt contains overlap warning for shared muscles |

### 2.8 Domain Layer -- Safety Guardrails (exercise-science.md Section 8)

**Classes under test:** `ValidatePlanSafetyUseCase`, `WeightJumpValidator`, `VolumeCeilingValidator`, `AgeModifierCalculator`, `WeightRounder`

#### 2.8.1 Maximum Weight Jump Per Session (Section 8.1)

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Barbell compound within 10% limit | Last session: 100kg, AI suggests: 107.5kg | Valid (7.5% increase) |
| 2 | Barbell compound exceeds 10% relative limit | Last session: 100kg, AI suggests: 112.5kg | Rejected (12.5% > 10%) |
| 3 | Barbell compound exceeds 10kg absolute limit | Last session: 80kg, AI suggests: 92.5kg | Rejected (12.5kg > 10kg) |
| 4 | Dumbbell compound within 5kg absolute limit | Last session: 20kg, AI suggests: 25kg | Valid (5kg = limit) |
| 5 | Dumbbell compound exceeds 5kg absolute limit | Last session: 20kg, AI suggests: 27.5kg | Rejected (7.5kg > 5kg) |
| 6 | Isolation within 15% relative limit | Last session: 30kg, AI suggests: 34kg | Valid (13.3% < 15%) |
| 7 | Isolation exceeds 5kg absolute limit | Last session: 15kg, AI suggests: 22.5kg | Rejected (7.5kg > 5kg) |
| 8 | Cold start exception — no history | No previous sessions, baseline weight | Not rejected (cold start bypass) |
| 9 | Whichever limit reached first applies | Last session: 40kg, AI suggests: 47.5kg | Rejected (absolute 7.5kg > 5kg for isolation, even though relative 18.75% > 15%) |
| 10 | Machine compound within 15% relative limit | Last session: 60kg, AI suggests: 67.5kg | Valid (12.5% < 15%) |

#### 2.8.2 Volume Ceilings Per Session (Section 8.2)

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Beginner within total set range | 14 total working sets | Valid |
| 2 | Beginner exceeds total set ceiling | 17 total working sets | Warning (exceeds 16 beginner ceiling) |
| 3 | Hard maximum exceeded (all levels) | 31 total working sets | Warning (exceeds 30 hard max) |
| 4 | Per-group ceiling for intermediate | 13 sets for chest (intermediate) | Warning (exceeds 12 per-group ceiling) |
| 5 | Per-exercise ceiling for beginner | 4 sets for one exercise (beginner) | Warning (exceeds 3 per-exercise ceiling) |
| 6 | Total exercises exceed ceiling | 13 exercises (any level) | Warning (exceeds 12 hard max) |
| 7 | Advanced within range | 23 total sets, 12 per group, 5 per exercise | Valid |

#### 2.8.3 Age-Adjusted Safety Modifiers (Section 8.6)

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Under 18: cap at 85% 1RM | Age 16, est 1RM = 100kg | Max suggested weight = 85kg |
| 2 | Under 18: no singles | Age 17, AI suggests 1-rep set | Rejected (min reps = 2) |
| 3 | Under 18: extra warm-up | Age 16 | Warm-up count = standard + 1 |
| 4 | 18-40: no modification | Age 25 | Standard protocols unchanged |
| 5 | 41-50: intensity reduction | Age 45, max intensity 90% | Adjusted to 87.5% (90% - 2.5%) |
| 6 | 51-60: full modifier set | Age 55 | Intensity -5%, +1 warm-up, +30s rest, -10% weekly volume |
| 7 | 60+: full modifier set | Age 65 | Intensity -10%, +2 warm-ups, +45s rest, -20% volume, prefer machines |
| 8 | Age modifier attenuates with performance | Age 55, advanced-level strength standards | Modifiers reduced based on demonstrated capacity |

#### 2.8.4 Advanced Exercise Gating

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Advanced exercise excluded for beginner | Beginner, Good Morning in plan | Rejected |
| 2 | Advanced exercise allowed for intermediate | Intermediate, Good Morning in plan | Allowed |
| 3 | Dragon Flag excluded from auto-program | Any level, auto-generated plan | Dragon Flag never appears (auto_program_min_level = 99) |
| 4 | Dragon Flag allowed if manually selected | Advanced, user manually adds Dragon Flag | Allowed |
| 5 | Deficit Deadlift requires good conventional history | No conventional deadlift history | Excluded |

#### 2.8.5 Warm-Up Set Requirements (Section 8.5)

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Heavy compound gets 3 warm-up sets | Barbell Bench Press | 3 warm-up sets (empty bar, 50%, 75%) |
| 2 | Moderate compound gets 2 warm-up sets | Leg Press | 2 warm-up sets (50%, 75%) |
| 3 | Light compound gets 1 warm-up set | Push-Up | 1 warm-up set (bodyweight) |
| 4 | Isolation gets 1 warm-up set | Barbell Curl | 1 warm-up set (50%) |
| 5 | Bodyweight isolation gets 0 warm-up | Plank | 0 warm-up sets |
| 6 | Age 50+: heavy compound gets 4 warm-up sets | Age 55, Squat | 4 warm-ups (empty bar, 40%, 60%, 80%) |
| 7 | Zero warm-up for compound always rejected | Any compound, AI returns 0 warm-ups | Plan rejected |

#### 2.8.6 Weight Rounding (Section 8.7)

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Barbell rounds to 2.5kg | Calculated: 67.3kg | Rounded: 65kg (down to nearest 2.5) |
| 2 | Cable/machine rounds to 5kg | Calculated: 33.7kg | Rounded: 30kg (down to nearest 5) |
| 3 | Always round DOWN | Calculated: 67.4kg barbell | Rounded: 65kg (not 67.5) |
| 4 | Imperial: barbell rounds to 5lbs | Calculated: 147lbs | Rounded: 145lbs |
| 5 | Imperial: machine rounds to 10lbs | Calculated: 73lbs | Rounded: 70lbs |
| 6 | Zero weight preserved | Bodyweight exercise, 0kg added | Returns 0 (not rounded to 2.5) |

### 2.9 Domain Layer -- Baseline Plan Generator (exercise-science.md Section 4)

**Class under test:** `BaselinePlanGenerator`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Beginner male baseline weight | Male, 80kg BW, Bench Press | Working weight: 32.5kg (0.40 x 80, rounded to 2.5) |
| 2 | Beginner female baseline weight | Female, 60kg BW, Squat | Working weight: 21kg (0.35 x 60, rounded down) |
| 3 | Gender unknown fallback | Unknown, 80kg BW, Bench Press | Working weight: 27.5kg (0.40 x 80 x 0.85, rounded down) |
| 4 | Intermediate male baseline | Male, 85kg BW, Deadlift | Working weight: 106.25kg → 105kg (1.25 x 85, rounded) |
| 5 | Advanced female baseline | Female, 65kg BW, OHP | Working weight: 25kg (0.40 x 65, rounded down) |
| 6 | Beginner set count | Beginner, any exercise | 3 working sets |
| 7 | Intermediate set count | Intermediate compound | 3-4 working sets |
| 8 | Advanced set count | Advanced compound | 4-5 working sets |
| 9 | Beginner warm-up sets for compound | Beginner, Squat | 2 warm-up sets (empty bar + 50%) |
| 10 | Beginner rep range (compound) | Beginner, compound exercise | 10-15 reps |
| 11 | Intermediate rep range (isolation) | Intermediate, isolation exercise | 10-15 reps |
| 12 | Gender fallback applies to all levels | Advanced, unknown gender, 90kg BW, Squat | Working weight: 114.75kg → 112.5kg (1.50 x 90 x 0.85, rounded) |
| 13 | Rest timer defaults from Section 8.8 | Intermediate, heavy compound | Rest = 120s |

### 2.10 Domain Layer -- Overtraining Warning Detection (exercise-science.md Section 8.3)

**Class under test:** `DetectOvertrainingWarningsUseCase`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | MRV exceeded 2+ weeks | Chest: 22 sets/week for 2 consecutive weeks (intermediate MRV ceiling = 16) | Medium warning: volume exceeded |
| 2 | MRV exceeded 1 week only | Chest: 22 sets/week for 1 week | No warning (needs 2+ consecutive) |
| 3 | Performance regression 3+ sessions | Bench Press weight/reps decreased 3 consecutive sessions | High warning: performance regression |
| 4 | Performance regression 2 sessions | Bench Press decreased 2 sessions | No warning (needs 3+) |
| 5 | Training frequency > 6x/week for 2+ weeks | 7 sessions/week for 2 weeks | High warning: excessive frequency |
| 6 | Training frequency > 6x/week for 1 week | 7 sessions in 1 week | No warning (needs 2+ consecutive) |
| 7 | Same group 4+ times in 7 days | Chest trained 4 times in 7-day window | Medium warning: excessive group frequency |
| 8 | Same group 3 times in 7 days | Chest trained 3 times | No warning (3x is within range) |
| 9 | Session duration > 120 min | Workout duration: 130 min | Low warning: cortisol elevation |
| 10 | Warning dismissal cooldown | User dismisses MRV warning | Same warning suppressed for 7 days |
| 11 | Multiple warnings simultaneously | MRV exceeded + regression detected | Both warnings returned |

### 2.11 Domain Layer -- Cross-Group Volume Interaction (exercise-science.md Section 2.2)

**Class under test:** `CrossGroupOverlapDetector`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Chest + Arms overlap (tricep) | User selects Chest + Arms | Warning: reduce tricep isolation volume (pressing provides tricep stimulus) |
| 2 | Chest + Shoulders overlap (anterior delt) | User selects Chest + Shoulders | Warning: reduce anterior delt isolation volume |
| 3 | Back + Arms overlap (bicep) | User selects Back + Arms | Warning: reduce bicep isolation volume (rows provide bicep stimulus) |
| 4 | Legs + Lower Back overlap | User selects Legs + Lower Back | Warning: flag erector spinae overlap (squats + deadlifts) |

### 2.12 Domain Layer -- Superset Compatibility Rules **(Phase 2)** (exercise-science.md Section 6.3)

**Class under test:** `SupersetCompatibilityValidator`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Valid antagonist pair | Bicep Curl + Tricep Pushdown | Allowed (antagonist pairing) |
| 2 | Same-group pair rejected | Barbell Curl + Dumbbell Curl (both Arms isolation) | Rejected: same muscle group |
| 3 | Two heavy compounds rejected | Barbell Squat + Deadlift | Rejected: never pair heavy compounds |
| 4 | Heavy compound + any exercise rejected | Barbell Squat + Leg Curl | Rejected: heavy compound cannot be in superset |
| 5 | Valid unrelated pair | Bench Press + Barbell Row | Allowed (chest + back, unrelated) |

### 2.13 Data Layer -- Repository Mapper Tests

**Classes under test:** `WorkoutSessionMapper`, `ExerciseMapper`, `TemplateMapper`, `UserProfileMapper`

| # | Test Case | Input | Expected Output |
|---|-----------|-------|-----------------|
| 1 | Entity to domain model (workout session) | `WorkoutSessionEntity` with all fields | `WorkoutSession` domain model with correct mapping |
| 2 | Domain model to entity (workout session) | `WorkoutSession` domain model | `WorkoutSessionEntity` with correct field mapping |
| 3 | Null optional fields preserved | Entity with `completedAt = null` | Domain model with `completedAt = null` |
| 4 | Exercise entity to domain with muscles | Entity + muscle list | Domain `Exercise` with primary/secondary muscles |
| 5 | Template entity with JSON muscle groups | `muscleGroupsJson = "[1,2,3]"` | Domain `Template` with `muscleGroupIds = [1,2,3]` |
| 6 | Invalid JSON in muscle groups | `muscleGroupsJson = "invalid"` | Exception or empty list (verify defensive handling) |
| 7 | Unit conversion in mapper (kg/lbs) | Entity stores kg, user prefers lbs | Mapper converts correctly (1kg = 2.20462lbs) |

### 2.14 ViewModel Tests

**Classes under test:** All ViewModels across feature modules.

Each ViewModel test uses Turbine to assert StateFlow emissions and MockK for repository/use case dependencies.

**WorkoutViewModel (`:feature:workout`):**

| # | Test Case | Setup | Assertion |
|---|-----------|-------|-----------|
| 1 | Initial state is Idle | No saved session | `state.value.phase == WorkoutPhase.Idle` |
| 2 | Restore session from SavedStateHandle | `savedStateHandle["sessionId"] = 42L` | State transitions to Active, exercises loaded |
| 3 | CompleteSet intent persists to repository | Send `CompleteSet(exerciseId, setIndex, weight, reps)` | `repository.completeSet()` called with correct args |
| 4 | CompleteSet updates UI state immutably | Complete set at index 2 | `state.exercises[x].sets[2].isCompleted == true`, other sets unchanged |
| 5 | FinishWorkout emits NavigateToSummary side effect | Send `FinishWorkout` | `sideEffect` emits `NavigateToSummary(sessionId)` |
| 6 | StartRestTimer starts timer via manager | Send `StartRestTimer(90)` | `restTimerManager.start(90)` called |
| 7 | SkipRestTimer cancels timer | Send `SkipRestTimer` | `restTimerManager.cancel()` called |
| 8 | Abandoned session detected on init | Active session in DB, no SavedStateHandle | `state.showResumeDialog == true` |

**PlanReviewViewModel (`:feature:ai-plan`):**

| # | Test Case | Setup | Assertion |
|---|-----------|-------|-----------|
| 1 | Loading state during plan generation | Trigger generate | State shows `isLoading = true` |
| 2 | Plan received updates exercise plans | AI returns valid plan | State contains exercise plans with warmup + working sets |
| 3 | Fallback indicator shown for cached plan | `PlanResult.Cached` | State has `planSource = PlanSource.Cached` |
| 4 | Manual entry mode on full fallback failure | `PlanResult.Manual` | State has `planSource = PlanSource.Manual`, empty sets |
| 5 | User edits planned weight | Edit weight from 80 to 85 | State reflects updated weight, no re-generation |

**ProgressDashboardViewModel (`:feature:progress`):**

| # | Test Case | Setup | Assertion |
|---|-----------|-------|-----------|
| 1 | Time range change updates data | Switch from 4 weeks to 12 weeks | Repository queried with new date range, state updated |
| 2 | Empty state when no workout history | No sessions in DB | State has `isEmpty = true` |
| 3 | PR list populated | PRs exist for user | State contains PR list with correct types |
| 4 | Volume chart data aggregated correctly | 4 weeks of sessions | Weekly volume data points emitted |

---

## 3. Integration Test Plan

Integration tests run on an Android device or emulator. They verify that Room, repositories, and ViewModels work together with real database operations. These use JUnit 4 (Android instrumentation requirement).

### 3.1 Room DAO -- CRUD Operations

**Test environment:** In-memory Room database (`Room.inMemoryDatabaseBuilder`), allowing garbage collection after each test. `@Before` creates the database, `@After` closes it.

**MuscleGroupDao + ExerciseDao:**

| # | Test Case | Operation | Assertion |
|---|-----------|-----------|-----------|
| 1 | Insert and retrieve all muscle groups | Insert 7 groups, query all | 7 groups returned in display_order |
| 2 | Insert exercise with foreign key | Insert group, then exercise with `primaryGroupId` | Exercise retrievable, FK intact |
| 3 | FK violation on insert (no parent group) | Insert exercise with nonexistent `primaryGroupId` | `SQLiteConstraintException` thrown |
| 4 | Query exercises by muscle group | Insert exercises across groups, query by group ID | Only exercises for that group returned |
| 5 | Exercise-muscle mapping (primary/secondary) | Insert exercise-muscle rows | Query returns correct `isPrimary` flags |

**WorkoutSessionDao + WorkoutExerciseDao + WorkoutSetDao:**

| # | Test Case | Operation | Assertion |
|---|-----------|-----------|-----------|
| 1 | Create session and verify status | Insert session with `status = "active"` | `observeByStatus("active")` emits session |
| 2 | Add exercise to session | Insert workout_exercise row | Retrievable by session_id |
| 3 | Add sets to exercise | Insert 4 workout_set rows | All 4 retrievable, ordered by `setIndex` |
| 4 | Complete a set (update actuals) | `updateActuals(weight=80, reps=8, isCompleted=true)` | Set row updated, `completedAt` non-null |
| 5 | Delete exercise cascades sets | Delete workout_exercise | All associated workout_sets deleted |
| 6 | Delete session cascades everything | Delete workout_session | Exercises and sets cascaded |
| 7 | Query active session singleton | 1 active + 2 completed sessions | `observeByStatus("active")` returns only 1 |
| 8 | Reorder exercises | Update `orderIndex` for 3 exercises | Query returns exercises in new order |

**TemplateDao:**

| # | Test Case | Operation | Assertion |
|---|-----------|-----------|-----------|
| 1 | Create template with exercises | Insert template + template_exercises | Template retrievable with exercise list |
| 2 | Delete template cascades exercises | Delete template | `template_exercises` rows deleted |
| 3 | Update template name | Update `name` field | Reflected on next query |
| 4 | List templates ordered by `updated_at` | Insert 3 templates with different timestamps | Returned in descending `updated_at` order |

**PersonalRecordDao:**

| # | Test Case | Operation | Assertion |
|---|-----------|-----------|-----------|
| 1 | Insert and query PRs by exercise | Insert 3 PRs for exercise 1 | All 3 returned for exercise 1 |
| 2 | Query PRs by type | Insert weight PR + rep PR | Filter by `record_type = "weight"` returns 1 |
| 3 | Delete PRs on exercise cascade | Delete exercise | PRs for that exercise cascade-deleted |

**UserProfileDao:**

| # | Test Case | Operation | Assertion |
|---|-----------|-----------|-----------|
| 1 | Insert singleton profile | Insert with `id = 1` | Retrievable |
| 2 | Update profile fields | Update age, height | New values persisted |
| 3 | Upsert behavior (conflict replace) | Insert twice with `id = 1` | Second insert overwrites first |

### 3.2 Complex Queries

| # | Test Case | Query | Assertion |
|---|-----------|-------|-----------|
| 1 | Sessions in date range | `observeSessionsInRange(start, end)` | Only sessions within range returned |
| 2 | Sessions for a specific exercise | `observeSessionsForExercise(exerciseId, limit=5)` | Returns up to 5 sessions containing that exercise, ordered by date desc |
| 3 | Weekly volume per muscle group | Custom query joining sessions, exercises, sets, muscle groups | Correct aggregation of weight x reps for working sets within 7-day window |
| 4 | Exercise progress (1RM over time) | Query max estimated 1RM per session for an exercise | Time series of 1RM values, one per session |
| 5 | PR detection query | Find max weight for exercise across all sessions | Returns the all-time max |
| 6 | Cached plan lookup by hash | `findByExerciseIdsHash(hash)` where hash < 7 days | Returns cached plan |
| 7 | Cached plan expired | `findByExerciseIdsHash(hash)` where hash > 7 days | Returns null (filtered by created_at) |

### 3.3 Repository + ViewModel Integration

These tests wire a real in-memory Room database to the actual repository implementation, with a real ViewModel. Only the AI provider is mocked.

| # | Test Case | Flow | Assertion |
|---|-----------|------|-----------|
| 1 | Full workout session lifecycle | Create session -> add exercises -> complete sets -> finish | Session status transitions: active -> completed. All sets persisted. |
| 2 | Template save and load | Save template from completed workout -> load template -> verify exercises | Template exercises match original selection and order |
| 3 | PR detection end-to-end | Complete sets exceeding previous best -> check PR repository | New PR rows inserted for correct exercise and type |
| 4 | Profile update flows to plan context | Update user profile (experience level, weight) -> trigger plan generation (mocked AI) | Mocked AI provider receives updated profile in PlanRequest |
| 5 | Abandoned session recovery | Create active session -> simulate ViewModel destruction -> new ViewModel init | New ViewModel detects abandoned session, offers resume dialog |

### 3.4 AI Provider Mock Tests

**Class under test:** `GeminiPlanProvider` with Ktor mock engine.

| # | Test Case | Mock Response | Assertion |
|---|-----------|---------------|-----------|
| 1 | Successful plan generation | 200 OK with valid JSON | `Result.success(GeneratedPlan)` with correct exercise plans |
| 2 | Empty Gemini response | 200 OK with empty candidates | `AiProviderException("Empty response from Gemini")` |
| 3 | HTTP 429 rate limit | 429 Too Many Requests | `AiProviderException` with status info |
| 4 | HTTP 500 server error | 500 Internal Server Error | `AiProviderException("Gemini API error: 500")` |
| 5 | Request timeout | Mock engine delays > 30s | `AiProviderException("Gemini API timeout")` |
| 6 | Malformed JSON response | 200 OK with invalid JSON | Parsing exception wrapped in `AiProviderException` |
| 7 | Valid JSON but wrong schema | 200 OK with JSON missing required fields | Parser handles gracefully or throws descriptive error |
| 8 | Network unreachable | Mock engine throws IOException | `AiProviderException("Unexpected error...")` |

### 3.5 Gemini Response Parser Tests

**Class under test:** `GeminiResponseParser`

| # | Test Case | Input JSON | Assertion |
|---|-----------|------------|-----------|
| 1 | Valid complete response | Full JSON with warmup + working sets | Parsed `GeneratedPlan` with all exercises |
| 2 | Missing warmup sets | JSON with only working sets | `warmupSets` is empty list, no crash |
| 3 | Extra fields ignored | JSON with additional unknown fields | Parsed correctly (ignoreUnknownKeys) |
| 4 | Exercise ID mismatch | Response contains exercise_id not in request | Parser filters or flags mismatch |
| 5 | Zero weight in plan | `"weight": 0` | Parsed as 0.0 (bodyweight exercise) |
| 6 | Negative values | `"weight": -10` | Rejected or flagged as invalid |

### 3.6 Template Full Cycle

| # | Step | Action | Verification |
|---|------|--------|-------------|
| 1 | Create | Save template "Push Day" with 4 exercises | Template row in DB, 4 template_exercise rows |
| 2 | List | Query all templates | "Push Day" appears in list |
| 3 | Load | Load template into workout setup | ExerciseSelection matches template exercises and order |
| 4 | Modify | Remove 1 exercise, add 2 new ones, rename | Template updated, exercise list reflects changes |
| 5 | Delete | Delete template | Template and template_exercises removed, no orphans |

### 3.7 Workout Session Lifecycle (Full Integration)

This is the most critical integration test. It simulates a complete workout from start to finish.

```
Step 1: User selects Chest + Arms groups
Step 2: User selects 4 exercises (2 chest, 2 arms)
Step 3: Auto-ordering applied (compounds first)
Step 4: Plan generation (mocked AI provider returns plan)
Step 5: Session created in DB with status = "active"
Step 6: User completes warm-up set 1 for exercise 1
    -> Verify: set row updated, isCompleted = true, completedAt set
Step 7: User completes all working sets for exercise 1
Step 8: User adds note to exercise 1
Step 9: User creates superset (exercises 3 + 4)
    -> Verify: superset_group_id matches for both exercises
Step 10: User completes remaining exercises
Step 11: User finishes workout
    -> Verify: session status = "completed", duration_seconds set
Step 12: Summary screen data correct
    -> Verify: total volume, exercise count, PR detection, duration
```

---

## 4. UI / E2E Test Plan

### 4.1 Critical Flows -- Automated E2E Tests

These 6 flows MUST pass before any release. All use Espresso + UI Automator + Compose testing APIs. They run on real devices or emulators via Firebase Test Lab.

#### Flow 1: Onboarding -> First Workout

```
Precondition: Fresh app install, no data.

Steps:
1. App launches -> Onboarding screen displayed
2. Select experience level: "Intermediate"
3. Select preferred unit: "kg"
4. (Optional fields skipped)
5. Tap "Get Started"
6. Home screen displayed with empty state
7. Tap "Start Workout"
8. Muscle group selection screen displayed
9. Select "Chest"
10. Exercise list for Chest displayed
11. Select "Bench Press" and "Incline Dumbbell Press"
12. Tap "Continue"
13. Auto-ordered exercise list displayed (Bench Press first)
14. Tap "Generate Plan"
15. Loading indicator shown
16. Plan review screen displayed (mocked AI response)
17. Tap "Start Workout"
18. Active workout screen displayed
19. Complete 1 warm-up set: enter 40kg x 10, tap done
20. Complete 1 working set: enter 80kg x 8, tap done
21. Rest timer starts automatically
22. Skip rest timer
23. Tap "Finish Workout"
24. Workout summary screen displayed
25. Verify: duration > 0, exercises completed = 2, volume > 0

Assertions:
- All data persisted in Room (query DB directly in test)
- Session status = "completed"
- No crash at any step
```

#### Flow 2: Template -> Plan -> Log -> Summary

```
Precondition: User has completed at least 1 workout. Template "Push Day" exists.

Steps:
1. Tap "Start Workout"
2. Tap "Load Template"
3. Select "Push Day"
4. Exercises pre-populated from template
5. Tap "Generate Plan"
6. Plan review screen with AI plan
7. Edit one planned weight (change 80kg to 85kg)
8. Tap "Start Workout"
9. Complete all sets for all exercises, logging actual weights/reps
10. Tap "Finish Workout"
11. Summary screen: verify PRs detected if applicable
12. Verify: logged actuals match what user entered, not planned values

Assertions:
- Template exercises loaded in correct order
- Edited plan values reflected
- Actual logged values (not planned) persisted
- Summary calculations correct
```

#### Flow 3: Superset Creation and Logging

```
Precondition: Active workout with 4 exercises.

Steps:
1. Long-press exercise 3
2. Long-press exercise 4 (or use grouping UI)
3. Tap "Create Superset"
4. Exercises 3 and 4 visually grouped
5. Complete set 1 of exercise 3
6. UI prompts to complete set 1 of exercise 4 (no rest timer between)
7. Complete set 1 of exercise 4
8. Rest timer starts (runs after full superset round)
9. Complete remaining superset sets
10. Finish workout

Assertions:
- superset_group_id matches for exercises 3 and 4
- Rest timer only started after completing both exercises in group
- Summary correctly calculates volume for supersetted exercises
```

#### Flow 4: Progress Charts and Metrics

```
Precondition: User has 5+ completed workouts with Bench Press over 4+ weeks.

Steps:
1. Navigate to Progress tab
2. Dashboard shows training consistency chart
3. Select "Chest" muscle group
4. Per-group metrics displayed (weekly volume, tonnage, frequency)
5. Select "Bench Press" exercise
6. 1RM progression chart displayed
7. Top set weight chart displayed
8. Volume load chart displayed
9. Change time range: "Last 4 weeks" -> "Last 12 weeks"
10. Charts update with expanded data
11. PRs section shows weight PR and rep PR entries

Assertions:
- Charts render without crash
- Data points match calculated values from raw workout data
- Time range filter correctly scopes data
- PR badges displayed for correct records
```

#### Flow 5: Profile and Unit Switching

```
Precondition: User has completed workouts in kg.

Steps:
1. Navigate to Profile screen
2. Current experience level displayed: "Intermediate"
3. Update body weight: enter 82kg
4. Body weight entry added to history
5. Navigate to Settings
6. Switch units from kg to lbs
7. Navigate back to workout history
8. All weights displayed in lbs (80kg -> 176.4lbs)
9. Start new workout
10. Weight inputs default to lbs
11. Complete a set in lbs
12. View workout summary -- all values in lbs
13. Switch back to kg in settings
14. Previous workout data displays correctly in kg

Assertions:
- Unit conversion applied consistently across all screens
- Raw data stored in original unit (kg), display conversion only
- No precision loss on round-trip conversion
- Body weight history correctly time-series tracked
```

#### Flow 6: Mid-Workout Modifications

```
Precondition: Active workout with 3 exercises, 2 exercises partially completed.

Steps:
1. Active workout in progress, exercise 1 fully completed
2. Exercise 2 partially completed (2 of 4 sets done)
3. Tap "Add Exercise"
4. Quick exercise picker opens
5. Select "Face Pulls" (not in original plan)
6. Face Pulls added to end of exercise list with empty sets
7. Swipe to delete exercise 3 (not yet started)
8. Confirm deletion
9. Drag to reorder: move Face Pulls before exercise 2
10. Continue logging sets for exercise 2
11. Add extra set beyond planned count for exercise 2
12. Finish workout

Assertions:
- Added exercise persisted with workout
- Deleted exercise removed from session
- Reorder reflected in order_index
- Extra set logged and included in volume calculations
- Summary includes added exercise, excludes deleted one
```

### 4.2 Compose UI Component Tests

These are faster, narrower tests using `createComposeRule()`. They test individual composables in isolation with mock data.

**SetRow component:**

| # | Test Case | Assertion |
|---|-----------|-----------|
| 1 | Displays planned weight and reps | Weight field shows "80", reps field shows "8" |
| 2 | Done checkbox toggleable | Tapping checkbox triggers onComplete callback |
| 3 | Warm-up set type indicator visible | "Warm-up" label or badge displayed |
| 4 | Large touch targets for gym use | Clickable areas >= 48dp |
| 5 | Weight input accepts decimal (72.5) | Decimal keyboard, input validated |

**RestTimerDisplay component:**

| # | Test Case | Assertion |
|---|-----------|-----------|
| 1 | Countdown displays formatted time | 90 seconds -> "1:30" |
| 2 | Skip button visible and functional | Tap skip triggers onSkip callback |
| 3 | Timer reaches zero -> visual indicator | Color change or animation at 0 |

**ExerciseCard component:**

| # | Test Case | Assertion |
|---|-----------|-----------|
| 1 | Displays exercise name and set count | "Bench Press - 4 sets" visible |
| 2 | Notes icon visible when notes exist | Notes indicator shown |
| 3 | Superset visual grouping | Grouped exercises indented/bracketed |

**WorkoutSummarySheet component:**

| # | Test Case | Assertion |
|---|-----------|-----------|
| 1 | Duration displayed correctly | "45 min" visible |
| 2 | Total volume calculated | "12,500 kg" visible |
| 3 | PR badges displayed | PR icon next to exercises with new PRs |
| 4 | Comparison to previous session shown | "+5% volume" or similar delta |

---

## 5. Android-Specific Tests

These tests address Android platform behaviors that are invisible in unit tests but cause real-world failures.

### 5.1 Process Death During Active Workout (CRITICAL)

**This is the highest-priority Android-specific test.** Android will kill the app process when backgrounded under memory pressure. The user's workout data MUST survive.

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Process death mid-workout | Start workout -> complete 3 sets -> background app -> simulate process death (UI Automator `am kill` or `ActivityScenario.recreate()`) -> relaunch app | App prompts resume, 3 completed sets present, workout timer resumes from correct elapsed time |
| 2 | Process death during plan generation | Start plan generation -> background app -> kill process -> relaunch | Either returns to setup state (re-trigger generation) or shows cached/baseline plan |
| 3 | Process death with rest timer active | Start rest timer -> background -> kill -> relaunch | Rest timer state is lost (acceptable per architecture doc). Workout data intact. |
| 4 | SavedStateHandle verification | Kill process -> relaunch | `savedStateHandle["sessionId"]` restored, ViewModel loads session from Room |
| 5 | Multiple process death cycles | Kill -> resume -> log 2 more sets -> kill -> resume -> finish | All sets persisted across both kills, summary totals correct |

**Implementation approach:**

```kotlin
@Test
fun processDeathPreservesCompletedSets() {
    // 1. Launch workout, complete 3 sets
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    // ... interact with UI to complete 3 sets ...

    // 2. Simulate process death
    scenario.recreate()

    // 3. Verify resume dialog appears
    onView(withText("Resume Workout")).check(matches(isDisplayed()))

    // 4. Tap resume
    onView(withText("Resume")).perform(click())

    // 5. Verify 3 sets still completed
    // ... assert set checkboxes are checked, weight/reps populated ...
}
```

### 5.2 Configuration Changes

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Rotation during active workout | Rotate device from portrait to landscape mid-workout | All UI state preserved: completed sets, current input values, timer |
| 2 | Rotation during plan generation (loading state) | Start plan generation -> rotate | Loading state maintained, request not duplicated |
| 3 | Dark mode toggle | Switch system theme while workout is active | Theme updates, no data loss, timer continues |
| 4 | Font size change | Change system font size to "Largest" | Workout screen remains usable, no text truncation on critical fields |
| 5 | Locale change | Change system locale mid-workout | App does not restart with data loss (English-only but locale change should not crash) |

### 5.3 Background and Foreground Transitions

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Background 30+ minutes | Start workout -> background app for 30 min -> foreground | Workout timer shows correct elapsed time (30min+). Foreground service kept process alive. |
| 2 | Background < 1 minute | Quick app switch and return | No visible state change, timer seamless |
| 3 | Foreground service notification | Background workout | Notification shows: elapsed time, rest timer (if active), quick actions |
| 4 | Notification actions (Pause/End) | Tap "Pause" in notification | Workout pauses, notification updates to show "Resume" |
| 5 | Return from notification tap | Tap notification body | App foregrounds on workout screen, correct state |

### 5.4 Doze Mode vs Timer

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Doze mode with active workout | Device enters Doze (screen off, stationary) during active workout | Foreground service exempts app from Doze. Timer continues. |
| 2 | Rest timer completion in Doze | Rest timer set, device in Doze | Timer fires correctly. Vibration/notification delivered. |
| 3 | Battery Saver mode | Enable battery saver during workout | Foreground service unaffected. Timer accuracy maintained. |

### 5.5 Battery Optimization

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | App battery-optimized (default) | Workout with default battery optimization | Foreground service prevents optimization from killing process |
| 2 | User removes from battery optimization whitelist | Manufacturer-specific battery kill (Xiaomi, Samsung, OnePlus) | Foreground service + START_STICKY provides best-effort survival |
| 3 | Adaptive battery restricts app | App restricted by Adaptive Battery | Foreground service exempts app during active workout |

### 5.6 Phone Call Interrupt

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Incoming call during set logging | User mid-input, phone rings, user answers | Input field values preserved after call ends |
| 2 | Long phone call (10+ min) | Active workout -> 10 min call -> return to app | Workout timer includes call duration (correct behavior -- timer tracks real time). All data intact. |
| 3 | Call declines | Decline call while on workout screen | No state disruption |

### 5.7 Split-Screen / Multi-Window

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Split-screen with music app | Open Deep Reps + Spotify in split-screen | Workout screen renders correctly in half-screen, touch targets still usable |
| 2 | Resize split-screen | Drag divider to change proportions | Compose recomposes correctly, no crash |
| 3 | Exit split-screen | Return to full screen | No state loss |

### 5.8 Storage Scenarios

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Low storage | < 50MB free storage during workout | Sets still persist (Room write operations degrade gracefully) |
| 2 | Clear app data | User clears data from system settings | App returns to onboarding, all data erased cleanly |
| 3 | App update with active session | Update app while session is active (status = "active" in DB) | Post-update launch detects active session, offers resume |

---

## 6. Device Compatibility Matrix

### 6.1 API Level Coverage

| API Level | Android Version | Coverage Priority | Notes |
|-----------|-----------------|-------------------|-------|
| 26 | 8.0 (Oreo) | Medium | minSdk. Test all critical paths. Notification channels required. |
| 28 | 9.0 (Pie) | Medium | Foreground service changes. |
| 29 | 10 | Medium | Scoped storage (not critical for us -- no file I/O). |
| 30 | 11 | High | Foreground service type declaration required. |
| 31 | 12 | High | `SCHEDULE_EXACT_ALARM` permission. Splash screen API. |
| 33 | 13 | High | Per-app language, notification permission (`POST_NOTIFICATIONS`). |
| 34 | 14 | High | Predictive back gesture, foreground service type changes. |
| 35 | 15 | Critical | targetSdk. Full compliance testing required. |

### 6.2 Device Tiers

**Tier 1 -- Low-end (test performance floor):**

| Device | API | RAM | Notes |
|--------|-----|-----|-------|
| Samsung Galaxy A14 | API 33 | 4GB | High market share in emerging markets |
| Samsung Galaxy A05 | API 33 | 4GB | Baseline budget device |
| Xiaomi Redmi 12 | API 33 | 4GB | Dominant in South/Southeast Asia |
| Pixel 4a | API 34 | 6GB | Reference low-mid device |

**Tier 2 -- Mid-range (primary target):**

| Device | API | RAM | Notes |
|--------|-----|-----|-------|
| Samsung Galaxy A54 | API 34 | 8GB | Best-selling mid-range globally |
| Samsung Galaxy A35 | API 34 | 6GB | Popular mid-range |
| Pixel 7a | API 35 | 8GB | Stock Android reference |
| OnePlus Nord CE 3 | API 34 | 8GB | OxygenOS testing |
| Xiaomi Redmi Note 13 Pro | API 34 | 8GB | MIUI/HyperOS testing |

**Tier 3 -- High-end (flagship experience):**

| Device | API | RAM | Notes |
|--------|-----|-----|-------|
| Pixel 9 | API 35 | 12GB | Stock Android latest |
| Samsung Galaxy S24 | API 34 | 8GB | One UI latest |
| OnePlus 12 | API 34 | 12GB | OxygenOS latest |

### 6.3 Screen Sizes

| Category | Size Range | Representative Device | Test Focus |
|----------|------------|----------------------|------------|
| Small | < 5.5" | Pixel 4a | Content fit, scrolling, touch targets |
| Medium | 5.5" - 6.5" | Galaxy A54, Pixel 7a | Primary design target |
| Large | 6.5" - 7" | Galaxy S24 Ultra | Layout stretching, padding |
| Tablet | 7"+ | Galaxy Tab S9 FE | Not primary target, but verify no crash |
| Foldable | Variable | Galaxy Z Fold 5 | Folding/unfolding during workout, split-screen |

### 6.4 Firebase Test Lab Configuration

```yaml
# Firebase Test Lab matrix for CI
devices:
  # Critical path testing (every PR)
  smoke:
    - model: oriole       # Pixel 6
      version: 33
    - model: tangorpro    # Pixel Tablet
      version: 34

  # Full matrix (nightly / pre-release)
  full:
    - model: oriole       # Pixel 6
      version: 33
    - model: husky        # Pixel 8 Pro
      version: 34
    - model: tokay        # Pixel 9
      version: 35
    - model: a]54         # Galaxy A54
      version: 34
      locale: en_US
    - model: b0q          # Galaxy S22
      version: 33
    - model: redfin       # Pixel 5
      version: 30
    # API 26 (minSdk) virtual device
    - model: Nexus5X
      version: 26
      type: virtual

  # Performance testing
  benchmark:
    - model: oriole       # Pixel 6 (consistent hardware for benchmarks)
      version: 33
```

### 6.5 OEM-Specific Known Issues

| OEM | Issue | Test Strategy |
|-----|-------|---------------|
| Samsung | Aggressive battery optimization kills background services | Verify foreground service survives on Samsung devices specifically |
| Xiaomi | MIUI auto-start restriction blocks foreground service | Test with and without auto-start permission granted |
| OnePlus | Battery optimization "Smart standby" can restrict apps | Test OxygenOS-specific power saving modes |
| Huawei | EMUI restricts background activity aggressively | Document workaround (user must whitelist app) |
| Oppo/Vivo | ColorOS/FuntouchOS background restrictions similar to Xiaomi | Verify notification delivery after background restriction |

---

## 7. Performance Test Plan

### 7.1 Startup Performance

**Tool:** Macrobenchmark library in `:benchmark` module.

| Metric | Target | Measurement | Failure Threshold |
|--------|--------|-------------|-------------------|
| Cold startup (TTID) | < 1.5s | `StartupBenchmark` with `StartupMode.COLD` | > 2.0s on Pixel 6 |
| Warm startup (TTID) | < 500ms | `StartupBenchmark` with `StartupMode.WARM` | > 800ms on Pixel 6 |
| Hot startup (TTID) | < 200ms | `StartupBenchmark` with `StartupMode.HOT` | > 400ms on Pixel 6 |
| Time to interactive (workout screen) | < 2.0s from tap | Custom trace from nav start to first frame | > 3.0s |

**Baseline Profile:**

A baseline profile is generated by the Macrobenchmark module and covers:

1. App startup path (Application.onCreate -> MainActivity -> NavHost).
2. Exercise library loading (Room query + list render).
3. Active workout screen first composition.
4. Progress chart rendering.

Baseline profile is regenerated nightly and committed to the repository. It is bundled with the release APK via the `baselineProfile` Gradle plugin.

```kotlin
@ExperimentalBaselineProfilesApi
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generateBaselineProfile() = baselineProfileRule.collectBaselineProfile(
        packageName = "com.deepreps.app"
    ) {
        // Critical startup path
        startActivityAndWait()

        // Navigate to workout flow
        device.findObject(By.text("Start Workout")).click()
        device.waitForIdle()

        // Navigate to progress
        device.findObject(By.desc("Progress")).click()
        device.waitForIdle()
    }
}
```

### 7.2 Frame Rate Performance

**Tool:** JankStats library + Macrobenchmark `FrameTimingBenchmark`.

| Scenario | Target | Failure Threshold |
|----------|--------|-------------------|
| Scrolling exercise list (30+ exercises) | 60fps, < 3 janky frames per scroll | > 5 janky frames |
| Scrolling workout sets during active workout | 60fps, < 2 janky frames | > 4 janky frames |
| Rest timer countdown animation | 60fps, 0 janky frames | > 1 janky frame |
| Progress chart render + scroll | 60fps, < 5 janky frames | > 8 janky frames |
| Drag-to-reorder exercises | 60fps during drag | Visible stutter |

**Compose Compiler Reports:**

Generated in CI for every PR. Configuration:

```kotlin
// build.gradle.kts (convention plugin)
composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}
```

**CI enforcement rule:** Any `restartable but not skippable` composable in `:feature:workout` or `:core:ui` components used during active workout is flagged as a warning. Any `UNSTABLE` class in workout UI models is a build failure.

### 7.3 Memory Performance

| Metric | Target | Tool |
|--------|--------|------|
| Peak memory (workout screen, mid-range device) | < 150MB | Android Profiler / `adb shell dumpsys meminfo` |
| Peak memory (progress charts, 6 months data) | < 180MB | Android Profiler |
| Memory after 1 hour active workout | No growth > 10MB over baseline | Heap dump comparison |
| Bitmap cache (Coil) | < 25MB in-memory | Coil config verification |
| No memory leaks in workout flow | Zero leaks | LeakCanary (debug builds) |

**LeakCanary integration:**

```kotlin
// Debug build only
class DebugDeepRepsApplication : DeepRepsApplication() {
    override fun onCreate() {
        super.onCreate()
        // LeakCanary auto-installs. Any leak is a test failure.
    }
}
```

Any LeakCanary report during manual QA or automated testing of the workout flow is an automatic P0 bug.

### 7.4 Database Query Performance

| Query | Max Time | Test Approach |
|-------|----------|---------------|
| Load active session with exercises and sets | < 50ms | Custom tracing in DAO test |
| Load exercise list for muscle group | < 30ms | Custom tracing |
| Load workout history (last 5 sessions) | < 100ms | Custom tracing with realistic data (50+ sessions in DB) |
| Aggregate weekly volume per muscle group | < 100ms | Custom tracing with 3 months of data |
| 1RM progression query (all time) | < 200ms | Custom tracing with 6 months of data |
| PR detection query (per exercise) | < 50ms | Custom tracing |
| Insert completed set | < 20ms | Custom tracing |

**Test approach:** Seed the database with realistic data volumes (200+ workout sessions, 2000+ sets, 50+ PRs) and measure query execution time. Use `System.nanoTime()` around DAO calls in instrumented tests.

### 7.5 APK and Install Size

| Metric | Target | Measurement |
|--------|--------|-------------|
| Release APK size (universal) | < 25MB | `./gradlew assembleRelease`, check artifact size |
| AAB download size (Play Store) | < 15MB per device config | Play Console pre-launch report |
| Install size on device | < 40MB | `adb shell pm path` + `du` |
| Database size after 6 months of use | < 10MB | Seed DB with 6 months of realistic data, measure `.db` file |

**Size regression detection:** CI tracks APK size per build. Alert if size increases by > 500KB between commits.

### 7.6 Network Performance

| Metric | Target | Test Approach |
|--------|--------|---------------|
| Gemini API round-trip (P95) | < 5s | Production monitoring (post-launch) |
| Request timeout handling | Graceful fallback within 35s | Ktor mock with delay > 30s |
| Offline detection speed | < 500ms | ConnectivityManager callback timing |
| Cached plan lookup | < 10ms | Room query timing |

---

## 8. Data Integrity Tests

Data integrity is the most critical quality attribute for Deep Reps. These tests verify that workout data is never lost, corrupted, or miscalculated under any circumstances.

### 8.1 Crash Recovery

| # | Scenario | Steps | Verification |
|---|----------|-------|-------------|
| 1 | Crash after set completion | Complete set -> force crash (throw exception in test) -> relaunch | Completed set persisted in DB |
| 2 | Crash during set completion | Trigger crash mid-DAO-write (difficult to simulate, but test transaction isolation) | Either set is fully written or fully absent (transactional) |
| 3 | Crash during exercise addition | Add exercise to session -> crash during @Transaction | Either all rows (exercise + sets) written or none (atomic) |
| 4 | Crash during plan generation | AI call in flight -> crash -> relaunch | Session in "active" or "setup" state, no partial plan data |
| 5 | OOM during progress chart render | Large dataset, low-memory device | App killed gracefully by OS, workout data in Room unaffected |
| 6 | ANR during set save | Verify completeSet is not on main thread | StrictMode verification -- all DB writes on IO dispatcher |

### 8.2 Database Migration Tests

**Tool:** Room `MigrationTestHelper`

**Principle:** Every migration from version N to N+1 is tested. The test seeds version N database with known data, runs the migration, and verifies all data is intact in version N+1 schema.

| # | Test Case | Migration | Assertion |
|---|-----------|-----------|-----------|
| 1 | Schema 1 to 2 (when applicable) | All tables present post-migration | Row counts preserved, data values unchanged |
| 2 | Add column migration | New nullable column added to `workout_sessions` | Existing rows have NULL for new column, no data loss |
| 3 | Add table migration | New table added (e.g., `cached_ai_plans`) | Existing tables unaffected, new table created empty |
| 4 | Destructive migration guard | Attempt schema change that would lose data | Test verifies migration is MANUAL (not auto-fallback destructive) |
| 5 | Full migration chain | Version 1 -> latest | Sequential migrations applied, data intact at each step |
| 6 | Pre-populated database compatibility | Exercise library `.db` file matches schema version | `createFromAsset()` succeeds, all exercises queryable |

```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DeepRepsDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate1To2() {
        // Create version 1 database
        val db = helper.createDatabase(TEST_DB, 1).apply {
            // Seed with known data
            execSQL("INSERT INTO workout_sessions (id, started_at, status) VALUES (1, 1000, 'active')")
            close()
        }

        // Run migration
        val migratedDb = helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)

        // Verify data survived
        val cursor = migratedDb.query("SELECT * FROM workout_sessions WHERE id = 1")
        assertThat(cursor.count).isEqualTo(1)
        cursor.moveToFirst()
        assertThat(cursor.getString(cursor.getColumnIndex("status"))).isEqualTo("active")
    }
}
```

### 8.3 Unit Conversion Integrity

| # | Test Case | Input | Expected |
|---|-----------|-------|----------|
| 1 | kg to lbs conversion | 100.0 kg | 220.462 lbs |
| 2 | lbs to kg conversion | 225.0 lbs | 102.058 kg |
| 3 | Round-trip conversion (kg -> lbs -> kg) | 72.5 kg | 72.5 kg (within 0.01 tolerance) |
| 4 | Round-trip conversion (lbs -> kg -> lbs) | 135.0 lbs | 135.0 lbs (within 0.01 tolerance) |
| 5 | Zero weight | 0 kg | 0 lbs |
| 6 | Display rounding (user sees clean numbers) | 72.5 kg -> lbs | Displayed as "159.8" lbs (1 decimal), stored as full precision |
| 7 | Volume calculation unit consistency | 3 sets x 80kg x 10 reps in kg mode | 2400 kg volume; if switched to lbs: 5291.1 lbs volume |
| 8 | 1RM calculation unit consistency | 100kg x 8 reps -> Epley 1RM | Same 1RM regardless of display unit (calc always on raw stored value) |

**Key rule:** The database stores weights in the unit they were entered. Unit conversion is display-only. The `preferred_unit` setting controls display, not storage.

### 8.4 PR Recalculation After Edits

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Edit completed set weight downward | Set was 100kg (weight PR), user edits to 95kg | PR recalculated. If 95kg < previous PR, the PR is either removed or reverted to previous best. |
| 2 | Edit completed set reps downward | Set was 10 reps at 80kg (rep PR), edited to 8 | Rep PR recalculated. Previous best at 80kg becomes current PR. |
| 3 | Delete a set that held a PR | Delete set with weight PR | PR for that exercise recalculated from remaining history |
| 4 | Delete entire workout that held PRs | Delete a workout session | All PRs from that session re-evaluated against remaining history |
| 5 | Add set retroactively | Edit completed workout, add a set with higher weight | New PR detected and recorded |

### 8.5 Auto-Save Verification

| # | Scenario | Steps | Expected |
|---|----------|-------|----------|
| 1 | Every set completion triggers DB write | Complete set, immediately query DB | Row exists with `isCompleted = true`, `completedAt` non-null |
| 2 | No batching of writes | Complete 3 sets rapidly, query after each | Each set individually present after its completion |
| 3 | Note save is immediate | Add exercise note, background app | Note persisted in `workout_exercises.notes` |
| 4 | Exercise addition is transactional | Add exercise to session mid-workout | Exercise row + all planned set rows exist (atomic) |
| 5 | Reorder persists immediately | Drag to reorder, background app | `order_index` values updated in DB |

---

## 9. Regression Test Plan

### 9.1 Smoke Suite (Per PR)

Runs on every pull request via CI. Must complete in < 15 minutes.

| # | Test | Type | Pass Criteria |
|---|------|------|---------------|
| 1 | App launches without crash | Instrumented | First frame rendered |
| 2 | Onboarding completes | Instrumented | Profile saved to Room |
| 3 | Exercise list loads | Instrumented | 7 muscle groups, exercises present |
| 4 | Workout session can be started | Instrumented | Session created with status "active" |
| 5 | Set can be completed | Instrumented | Set persisted with actuals |
| 6 | Workout can be finished | Instrumented | Session status "completed" |
| 7 | All unit tests pass | JVM | 0 failures |
| 8 | Lint clean | Static analysis | 0 errors (warnings tolerated) |
| 9 | Build succeeds | Gradle | Debug APK generated |
| 10 | Coverage threshold met | JaCoCo | `:core:domain` >= 80% |

### 9.2 Full Regression Suite (Pre-Release)

Runs before every release candidate. Expected duration: 1-2 hours on Firebase Test Lab.

| Category | Tests | Device Matrix |
|----------|-------|---------------|
| All unit tests | Complete JVM suite | N/A (JVM) |
| All integration tests | All DAO + repository tests | API 28, API 33, API 35 |
| All 6 E2E critical flows | Full flow tests | API 28, API 33, API 35, Galaxy A54, Pixel 9 |
| Process death scenarios | 5 process death tests | API 33 (Pixel 6) |
| Configuration change tests | Rotation + theme + font size | API 34 |
| Performance benchmarks | Startup + frame rate | Pixel 6 (API 33) |
| Monkey testing | 10,000 random events | API 34 |

### 9.3 Regression Trigger Criteria

| Change Type | Required Testing |
|-------------|-----------------|
| Domain logic change (use case, calculator) | Full unit test suite + affected integration tests |
| DAO / migration change | All DAO tests + migration tests + E2E smoke |
| ViewModel change | ViewModel unit tests + affected UI tests |
| Compose UI change (`:core:ui` or feature screen) | UI component tests + affected E2E flows |
| Dependency version bump | Full regression suite |
| New feature addition | New tests + full regression suite |
| Bug fix | Regression test for the bug + smoke suite |
| Build/config change | Full build verification + smoke suite |

### 9.4 Test Maintenance

| Activity | Frequency | Owner |
|----------|-----------|-------|
| Review flaky tests | Weekly | QA Engineer |
| Update device matrix | Quarterly | QA Engineer |
| Regenerate baseline profiles | Monthly (or on perf regression) | Dev + QA |
| Review and prune E2E tests | Per release | QA Engineer |
| Update test data seeds | When schema changes | Developer |
| Macrobenchmark baseline update | Per release | QA Engineer |

**Flaky test policy:** A test that fails intermittently (> 2 flaky failures in 30 days) is quarantined, investigated, and either fixed or rewritten. Flaky tests are never permanently disabled -- they are either made reliable or deleted.

### 9.5 Test Data Management

**Unit tests:** Use factory functions to create test fixtures. No shared mutable state between tests.

```kotlin
object TestFixtures {
    fun workoutSession(
        id: Long = 1L,
        status: String = "active",
        startedAt: Long = System.currentTimeMillis(),
    ) = WorkoutSessionEntity(
        id = id,
        startedAt = startedAt,
        completedAt = null,
        durationSeconds = null,
        status = status,
        notes = null,
        templateId = null,
    )

    fun workoutSet(
        id: Long = 1L,
        workoutExerciseId: Long = 1L,
        setIndex: Int = 0,
        setType: String = "working",
        plannedWeight: Double = 80.0,
        plannedReps: Int = 8,
    ) = WorkoutSetEntity(
        id = id,
        workoutExerciseId = workoutExerciseId,
        setIndex = setIndex,
        setType = setType,
        plannedWeight = plannedWeight,
        plannedReps = plannedReps,
        actualWeight = null,
        actualReps = null,
        isCompleted = false,
        completedAt = null,
    )
}
```

**Integration tests:** Use in-memory Room database. Each test creates its own database instance. No cross-test contamination.

**E2E tests:** Use a test-specific Hilt module that provides:
- In-memory Room database (pre-seeded with exercise library).
- Ktor mock engine (returns deterministic AI plans).
- Fake `ConnectivityChecker` (controllable online/offline state).

---

## 10. Bug Reporting Template

### 10.1 Severity Definitions

| Severity | Label | Description | Response SLA | Examples |
|----------|-------|-------------|--------------|----------|
| **P0** | Critical | Data loss, crash on critical path, workout cannot be completed or data is corrupted. App is unusable. | Fix within 24 hours. Hotfix release. | Set data lost after process death. App crashes when completing a set. Database migration destroys workout history. |
| **P1** | High | Major feature broken, blocking UX, incorrect calculations that affect user decisions. App is usable but core functionality impaired. | Fix within 3 days. Included in next release. | AI plan generation always fails (no fallback). PR detection reports false PRs. Rest timer does not fire notification. Progress charts show wrong data. |
| **P2** | Medium | Minor feature broken, cosmetic issues affecting usability, edge case failures. Workaround exists. | Fix within 1 sprint (2 weeks). | Template loading loses exercise order. Unit switching does not update one screen. Superset visual grouping broken. |
| **P3** | Low | Cosmetic issues, minor UX friction, non-blocking. | Backlog. Fix when convenient. | Animation jank on low-end device. Typo in exercise description. Padding inconsistency on one screen. |
| **P4** | Trivial | Nitpick, subjective improvement, suggestion. | Backlog. May or may not be addressed. | "Could the button be slightly larger?" Minor color shade difference from design spec. |

### 10.2 Bug Report Fields

```
## Bug Report

**Title:** [Concise description of the bug]

**Severity:** P0 / P1 / P2 / P3 / P4

**Reporter:** [Name]
**Date:** [YYYY-MM-DD]
**Build:** [Version code / commit hash]

### Environment
- **Device:** [Manufacturer + Model, e.g., Samsung Galaxy A54]
- **Android Version:** [e.g., Android 14 (API 34)]
- **App Version:** [e.g., 1.0.0 (build 42)]
- **Network State:** [Online / Offline / Weak connection]
- **Battery Level:** [e.g., 45%]
- **Battery Saver:** [On / Off]

### Steps to Reproduce
1. [Step 1]
2. [Step 2]
3. [Step 3]
...

### Expected Behavior
[What should happen]

### Actual Behavior
[What actually happens]

### Frequency
[Always / Intermittent (N out of M attempts) / Once]

### Screenshots / Video
[Attach screenshots or screen recording]

### Logs
[Logcat output, crash stack trace, or ANR trace if applicable]

### Additional Context
[Any other relevant information: workout state, number of exercises,
 time into workout, etc.]
```

### 10.3 Bug Triage Process

1. **P0:** Immediately assigned. All other work stops. Fix verified within 24 hours. Hotfix release if the bug is in production.
2. **P1:** Assigned within 1 business day. Root cause identified within 2 days. Fix in next release.
3. **P2:** Triaged in weekly bug review. Prioritized against feature work. Fix within current sprint if capacity allows.
4. **P3/P4:** Reviewed monthly. Batched with related work. May be rejected if effort outweighs impact.

### 10.4 Bug Lifecycle

```
New -> Triaged -> Assigned -> In Progress -> In Review -> Verified -> Closed
                                                |
                                                +-> Cannot Reproduce -> Closed (with note)
                                                +-> Won't Fix -> Closed (with rationale)
                                                +-> Duplicate -> Closed (linked to original)
```

**Verification rule:** The QA engineer verifies every bug fix on the exact device and OS version from the original report. A fix verified only on an emulator is not considered verified if the original bug was on a physical device.

---

## Appendix A: Test Naming Convention

All tests follow this pattern:

```
fun methodUnderTest_condition_expectedResult()
```

Examples:

```kotlin
fun calculateEstimated1rm_epleyWith100kgAnd8Reps_returns126Point67()
fun completeSet_withValidInput_persistsToDatabase()
fun generatePlan_whenOfflineAndNoCachedPlan_returnsBaselinePlan()
fun workoutViewModel_onProcessDeathAndRestore_resumesFromSavedSession()
```

## Appendix B: Test Environment Setup

### Local Development

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run unit tests for a specific module
./gradlew :core:domain:test

# Run instrumented tests (requires emulator or device)
./gradlew connectedDebugAndroidTest

# Run instrumented tests for a specific module
./gradlew :core:database:connectedDebugAndroidTest

# Generate coverage report
./gradlew jacocoTestReport

# Run Macrobenchmark
./gradlew :benchmark:connectedReleaseAndroidTest

# Generate Compose Compiler reports
./gradlew assembleRelease -PcomposeCompilerReports=true
```

### CI Environment

- **JDK:** Zulu 21
- **Gradle:** 8.x (wrapper)
- **Emulator (CI):** API 33 x86_64, Pixel 6 skin
- **Firebase Test Lab:** Configured via `gcloud` CLI in release pipeline

## Appendix C: Coverage Exclusions

The following are excluded from JaCoCo coverage calculations:

- Hilt-generated code (`*_Hilt*`, `*_Factory*`, `*_MembersInjector*`)
- Room-generated DAO implementations (`*_Impl*`)
- Kotlinx Serialization generated code (`*$$serializer*`)
- BuildConfig files
- Android Manifest-related generated code
- Compose compiler-generated code (`*ComposableSingletons*`)
- Convention plugin code in `build-logic/`

```kotlin
// JaCoCo exclusion patterns (build.gradle.kts)
val jacocoExcludes = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/*_Hilt*.*",
    "**/*_Factory.*",
    "**/*_MembersInjector.*",
    "**/*_Impl.*",
    "**/*$$serializer*.*",
    "**/*ComposableSingletons*.*",
    "**/di/*Module*.*",
)
```
