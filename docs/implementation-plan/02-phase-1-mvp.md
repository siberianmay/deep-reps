# Phase 1: MVP Development (Weeks 9-22)

14 weeks of development. Two developers working in parallel. QA integrates continuously from Week 14.

---

## Week-by-Week Developer Allocation

```
Week  Lead Android Dev                    Mid-Senior Android Dev
────  ──────────────────────────────────  ────────────────────────────────────
 9    Epic 1: Project scaffolding         Epic 2: Core UI / design system
10    Epic 3: Room DB + entities + DAOs   Epic 2: Core UI (continued)
11    Epic 4: Data layer (repos, mappers) Epic 5: Exercise library (browse UI)
12    Epic 4: Domain use cases            Epic 5: Exercise library (detail, selection)
13    Epic 6: AI - Gemini provider        Epic 7: Onboarding flow
14    Epic 6: AI - prompt builder/parser  Epic 8: Workout setup (group select, picker)
15    Epic 6: AI - fallback chain         Epic 8: Auto-ordering + integration
16    Epic 9: Workout logging (core)      Epic 10: Templates (CRUD)
17    Epic 9: Workout logging (timer)     Epic 11: Workout summary
18    Epic 9: Foreground service + crash  Epic 12: Per-exercise notes + polish
19    Epic 13: Data persistence hardening Epic 14: Progress tracking (charts)
20    Epic 13: Process death + recovery   Epic 14: Progress tracking (queries)
21    Epic 15: Analytics instrumentation  Bug fixes + edge cases
22    Performance optimization + polish   Bug fixes + QA support
```

---

## Epic 1: Project Scaffolding — DONE

**Owner:** Lead Dev
**Duration:** Week 9 (5 days)
**Blocks:** All subsequent development
**Status:** COMPLETE — All 14 modules scaffolded, 8 convention plugins, version catalog, CI/CD pipelines, Detekt config, signing config.

### Tasks

| # | Task | Est. | Deliverable |
|---|------|------|-------------|
| 1.1 | Create multi-module Gradle project structure | 1d | `settings.gradle.kts` with all 14 modules declared |
| 1.2 | Create `build-logic/` with convention plugins | 1.5d | 7 plugins: AndroidApplication, AndroidLibrary, AndroidCompose, AndroidHilt, AndroidFeature, JvmLibrary, AndroidBenchmark |
| 1.3 | Create `gradle/libs.versions.toml` version catalog | 0.5d | All dependencies from architecture.md Section 8.4 |
| 1.4 | Configure `:app` module (Hilt, navigation host, splash) | 0.5d | App compiles and launches to empty screen |
| 1.5 | Configure `:core:common` module (dispatchers, extensions) | 0.25d | `DispatcherProvider` interface + default impl |
| 1.6 | Configure Detekt + ktlint | 0.25d | Both run in CI |
| 1.7 | Set up GitHub Actions CI pipeline | 0.5d | `ci.yml` from devops-pipeline.md: lint + test + build |
| 1.8 | Set up `.gitignore`, `gradle.properties`, R8 config | 0.25d | Project builds clean |
| 1.9 | Configure signing (debug keystore, release keystore placeholder) | 0.25d | Debug builds sign automatically |

### Sub-tasks for 1.2 (Convention Plugins)

| # | Sub-task | Details |
|---|----------|---------|
| 1.2a | `AndroidApplicationConventionPlugin` | compileSdk, targetSdk, minSdk, compose, R8 |
| 1.2b | `AndroidLibraryConventionPlugin` | Same SDK targets, no signing |
| 1.2c | `AndroidComposeConventionPlugin` | Compose compiler, BOM, Compose dependencies |
| 1.2d | `AndroidHiltConventionPlugin` | Hilt + KSP setup |
| 1.2e | `AndroidFeatureConventionPlugin` | Combines Library + Compose + Hilt + Navigation |
| 1.2f | `JvmLibraryConventionPlugin` | For `:core:domain` and `:core:common` (pure Kotlin) |
| 1.2g | `DetektConventionPlugin` | Detekt plugin application + config |

**Acceptance Criteria:**
- `./gradlew build` passes on clean checkout
- All 14 modules compile (empty, no code yet)
- CI pipeline runs lint + build successfully
- Convention plugins apply correct SDK versions, Compose config, Hilt setup

---

## Epic 2: Core UI / Design System Implementation — DONE

**Owner:** Mid Dev
**Duration:** Weeks 9-10 (8 days)
**Blocks:** All feature UI work
**Status:** COMPLETE — Theme (dark+light), typography, spacing, 11 shared components with previews, muscle group colors, SetRow with all states.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 2.1 | Implement theme (dark + light) | 1d | `:core:ui` | `Theme.kt`, `Color.kt` with all tokens from design-system.md Section 2.1 |
| 2.2 | Implement typography scale | 0.5d | `:core:ui` | `Typography.kt` with Inter font, all 17 tokens |
| 2.3 | Implement spacing + radius tokens | 0.25d | `:core:ui` | `Spacing.kt`, constants for all tokens |
| 2.4 | Implement `DeepRepsButton` (primary, secondary, destructive) | 0.5d | `:core:ui` | Button with 3 variants + disabled state |
| 2.5 | Implement `DeepRepsTextField` | 0.5d | `:core:ui` | Text field matching design system |
| 2.6 | Implement `DeepRepsCard` | 0.25d | `:core:ui` | Card with elevation tokens |
| 2.7 | Implement `NumberInput` (weight/reps stepper) | 1d | `:core:ui` | +/- buttons, configurable step (via `WeightStepProvider`), large touch targets (72dp x 56dp) |
| 2.8 | Implement `CountdownTimer` display | 0.5d | `:core:ui` | Circular progress ring, `number-large` digits |
| 2.9 | Implement `LoadingIndicator` | 0.25d | `:core:ui` | Branded loading state |
| 2.10 | Implement `ErrorState` + `EmptyState` | 0.5d | `:core:ui` | Reusable error/empty composables |
| 2.11 | Implement `SetRow` component | 1d | `:core:ui` | The atomic workout logging unit per design-system.md Section 3.1 |
| 2.12 | Implement `MuscleGroupChip` + color mapping | 0.5d | `:core:ui` | Chip with group-specific colors |
| 2.13 | Write Compose preview + snapshot tests for all components | 1d | `:core:ui` | All components have `@Preview` functions |

**Acceptance Criteria:**
- All design tokens from design-system.md implemented in code
- All shared components render correctly in dark + light themes
- Touch targets meet minimum 48dp (56dp for workout screens)
- `NumberInput` step respects equipment type (2.5kg barbell, 5kg cable/machine)

---

## Epic 3: Database Layer — DONE

**Owner:** Lead Dev
**Duration:** Weeks 10-11 (8 days)
**Blocks:** All data operations
**Status:** COMPLETE — 12 entities, 10 DAOs, TypeConverters, ExerciseWithMuscles relation, PrepopulateCallback (7 groups + 78 exercises), DatabaseModule with WAL + Hilt DI.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 3.1 | Implement all Room entities (12 entity classes) | 2d | `:core:database` | All entities from architecture.md Section 3.2 |
| 3.2 | Implement TypeConverters (JSON arrays, dates) | 0.5d | `:core:database` | `Converters.kt` |
| 3.3 | Implement `DeepRepsDatabase` abstract class | 0.25d | `:core:database` | Database class with all 10 DAOs |
| 3.4 | Implement DAOs: `MuscleGroupDao`, `ExerciseDao` | 1d | `:core:database` | All queries for exercise browsing + detail |
| 3.5 | Implement DAOs: `WorkoutSessionDao`, `WorkoutExerciseDao`, `WorkoutSetDao` | 1.5d | `:core:database` | All queries for workout CRUD + observation |
| 3.6 | Implement DAOs: `TemplateDao`, `UserProfileDao`, `BodyWeightDao` | 0.5d | `:core:database` | Template and profile CRUD |
| 3.7 | Implement DAOs: `PersonalRecordDao`, `CachedAiPlanDao` | 0.5d | `:core:database` | PR queries, plan caching |
| 3.8 | Create pre-populated database file from CSCS spreadsheet | 1d | `:core:database` | `assets/deep_reps.db` with all 78 exercises, 7 groups, exercise-muscle mappings |
| 3.9 | Configure `DatabaseModule` (Hilt, `createFromAsset()`, WAL mode) | 0.5d | `:core:database` | DI module providing database + all DAOs |
| 3.10 | Write DAO integration tests | 1d | `:core:database` | `ExerciseDaoTest`, `WorkoutSessionDaoTest` |

### Sub-tasks for 3.1 (Entities)

| # | Entity | Key Details |
|---|--------|-------------|
| 3.1a | `MuscleGroupEntity` | 3 fields, simple |
| 3.1b | `ExerciseEntity` | 16 fields including stableId, difficulty, supersetTags, autoProgramMinLevel |
| 3.1c | `ExerciseMuscleEntity` | Junction table, composite PK |
| 3.1d | `WorkoutSessionEntity` | 8 fields including 6 statuses, pausedDurationSeconds |
| 3.1e | `WorkoutExerciseEntity` | 6 fields with FK constraints |
| 3.1f | `WorkoutSetEntity` | 10 fields: planned + actual weights/reps |
| 3.1g | `TemplateEntity` + `TemplateExerciseEntity` | Template CRUD |
| 3.1h | `UserProfileEntity` | Singleton row (PK = 1) |
| 3.1i | `BodyWeightEntryEntity` | Weight always in kg |
| 3.1j | `PersonalRecordEntity` | 4 record types, FK to exercise + session |
| 3.1k | `CachedAiPlanEntity` | Plan JSON cached by exercise hash |

### Sub-tasks for 3.8 (Pre-populated DB)

| # | Sub-task | Details |
|---|----------|---------|
| 3.8a | Write SQLite script to import CSCS spreadsheet | Reads CSV/JSON, generates INSERT statements |
| 3.8b | Populate 7 muscle groups with display_order | |
| 3.8c | Populate 78 exercises with all 16 fields | Validate all stableIds are unique |
| 3.8d | Populate exercise_muscles junction table (primary + secondary) | |
| 3.8e | Verify DB opens correctly via `createFromAsset()` | Integration test |

**Acceptance Criteria:**
- All entities match architecture.md Section 3.2 exactly
- Foreign keys enforced (RESTRICT or CASCADE as specified)
- Pre-populated DB contains all 78 exercises with correct data
- WAL mode enabled
- All DAO tests pass on instrumented test

---

## Epic 4: Data & Domain Layers — DONE

**Owner:** Lead Dev
**Duration:** Weeks 11-12 (8 days)
**Blocks:** Feature ViewModels
**Status:** COMPLETE — 12 domain models, 7 repo interfaces + impls, 4 provider interfaces, WorkoutStateMachine, OrderExercisesUseCase, calculators, ConsentManager, DI modules, 5 test classes.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 4.1 | Define domain models (12 data classes) | 1d | `:core:domain` | `MuscleGroup`, `Exercise`, `WorkoutSession`, `WorkoutExercise`, `WorkoutSet`, `Template`, `UserProfile`, `PersonalRecord`, `BodyWeightEntry`, `GeneratedPlan`, `ExercisePlan`, `PlannedSet` |
| 4.2 | Define repository interfaces (7 interfaces) | 0.5d | `:core:domain` | `ExerciseRepository`, `WorkoutSessionRepository`, `TemplateRepository`, `UserProfileRepository`, `PersonalRecordRepository`, `BodyWeightRepository`, `CachedPlanRepository` |
| 4.3 | Define provider interfaces | 0.25d | `:core:domain` | `AiPlanProvider`, `ConnectivityChecker`, `AnalyticsTracker`, `FeatureFlagProvider` |
| 4.4 | Implement entity ↔ domain model mappers | 1d | `:core:data` | `ExerciseMapper`, `WorkoutSessionMapper`, `TemplateMapper`, `UserProfileMapper` |
| 4.5 | Implement repository implementations (7 classes) | 2d | `:core:data` | All `*RepositoryImpl` classes |
| 4.6 | Implement `WorkoutStateMachine` | 0.5d | `:core:domain` | Pure Kotlin state machine from architecture.md Section 5.1 |
| 4.7 | Implement `OrderExercisesUseCase` | 0.5d | `:core:domain` | Auto-ordering algorithm per exercise-science.md Section 6 |
| 4.8 | Implement `Estimated1rmCalculator` | 0.25d | `:core:domain` | Epley + Brzycki formulas with guards |
| 4.9 | Implement `VolumeCalculator` | 0.25d | `:core:domain` | Per-exercise, per-group, per-session volume |
| 4.10 | Implement `WeightStepProvider` | 0.25d | `:core:domain` | Equipment-based increment utility |
| 4.11 | Implement `ConsentManager` | 0.5d | `:core:data` | EncryptedSharedPreferences for consent state |
| 4.12 | Implement Hilt DI modules (`DataModule`, `RepositoryModule`) | 0.5d | `:core:data` | All bindings wired |
| 4.13 | Write unit tests for all domain logic | 1d | `:core:domain` | Tests for state machine, ordering, 1RM calc, volume calc |

**Acceptance Criteria:**
- Domain layer has zero Android dependencies (pure Kotlin)
- All repository implementations use `flowOn(dispatchers.io)`
- State machine rejects all invalid transitions
- Auto-ordering puts compounds before isolations, core always last
- Weight storage convention: all weights in kg internally

---

## Epic 5: Exercise Library — DONE

**Owner:** Mid Dev
**Duration:** Weeks 11-13 (10 days)
**Depends on:** Epic 3 (database), Epic 2 (UI components)
**Status:** COMPLETE — 3 screens (List, Detail, Selection), 3 ViewModels with MVI, ExerciseListItem, AnatomyDiagram placeholder, navigation routes.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 5.1 | Implement `ExerciseListScreen` (browse by group) | 1.5d | `:feature:exercise-library` | LazyColumn of exercises filtered by group |
| 5.2 | Implement `ExerciseListViewModel` | 1d | `:feature:exercise-library` | MVI: state + intents for filtering, searching |
| 5.3 | Implement `ExerciseDetailScreen` | 1.5d | `:feature:exercise-library` | Full detail card: name, description, anatomy diagram, tips, pros, equipment tag, difficulty tag, secondary muscles |
| 5.4 | Implement `ExerciseDetailViewModel` | 0.5d | `:feature:exercise-library` | Load exercise by ID, observe |
| 5.5 | Implement `ExerciseSelectionScreen` | 1.5d | `:feature:exercise-library` | Multi-select exercise picker with checkboxes |
| 5.6 | Implement `ExerciseSelectionViewModel` | 1d | `:feature:exercise-library` | Selected exercises state, group filtering |
| 5.7 | Implement `ExerciseListItem` component | 0.5d | `:feature:exercise-library` | Name, equipment tag, movement type tag, difficulty chip |
| 5.8 | Implement `AnatomyDiagram` component | 1d | `:feature:exercise-library` | Load `anatomy_template.svg` from assets, tag paths with muscle group IDs, programmatic fill swap for primary (85% opacity) and secondary (30% opacity) highlighting per exercise |
| 5.9 | Wire navigation routes | 0.5d | `:feature:exercise-library` + `:app` | `ExerciseListRoute`, `ExerciseDetailRoute` |
| 5.10 | Write unit tests (ViewModels) + UI tests | 1d | `:feature:exercise-library` | Happy path tests |

**Acceptance Criteria:**
- All 78 exercises display correctly grouped under 7 muscle groups
- Exercise detail shows anatomy diagram, tips, pros, equipment, difficulty
- Multi-select picker tracks selected exercises correctly
- Difficulty chip color-coded (beginner=green, intermediate=amber, advanced=red)

---

## Epic 6: AI Plan Generation — DONE

**Owner:** Lead Dev
**Duration:** Weeks 13-15 (12 days)
**Depends on:** Epic 4 (domain layer, repositories), Epic 0.6 (prompt templates)
**HIGH RISK — build early to surface problems**
**Status:** COMPLETE — Ktor client, GeminiPlanProvider + PromptBuilder + ResponseParser, 4-level fallback chain, BaselinePlanGenerator, safety validation, deload detection, plan caching, PlanReviewScreen, 4 test classes.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 6.1 | Implement Ktor HTTP client setup | 0.5d | `:core:network` | `NetworkModule` with Ktor client config |
| 6.2 | Implement `GeminiPlanProvider` | 1.5d | `:core:network` | Full Gemini API call: request → response → error handling |
| 6.3 | Implement `GeminiPromptBuilder` | 2d | `:core:network` | Full prompt construction per architecture.md Section 4.3 with all safety constraints |
| 6.4 | Implement `GeminiResponseParser` | 1d | `:core:network` | JSON parsing of Gemini response → `GeneratedPlan` |
| 6.5 | Implement `CrossGroupOverlapDetector` | 0.5d | `:core:domain` | Detects overlapping muscle groups for prompt |
| 6.6 | Implement `BaselinePlanGenerator` | 1.5d | `:core:domain` | Experience-level defaults using BW ratio tables from exercise-science.md Section 4 |
| 6.7 | Implement `GeneratePlanUseCase` (fallback chain) | 1d | `:core:domain` | AI → cached → baseline → manual fallback |
| 6.8 | Implement `ValidatePlanSafetyUseCase` | 1d | `:core:domain` | Weight jump, volume ceiling, age modifier, difficulty gating validation |
| 6.9 | Implement `DetectDeloadNeedUseCase` | 0.5d | `:core:domain` | Checks deload triggers per exercise-science.md Section 3.4 |
| 6.10 | Implement `DetermineSessionDayTypeUseCase` | 0.5d | `:core:domain` | DUP cycling + block periodization heuristic |
| 6.11 | Implement `PlanReviewScreen` + `PlanReviewViewModel` | 1d | `:feature:ai-plan` | Plan display with editable weights/reps per exercise |
| 6.12 | Implement plan caching logic | 0.5d | `:core:data` | Hash exercise IDs, store plan JSON, 7-day expiry |
| 6.13 | Write unit tests (prompt builder, parser, fallback chain, safety) | 1.5d | `:core:network`, `:core:domain` | Tests per testing-strategy.md Sections 2.6-2.8 |

### Sub-tasks for 6.3 (Prompt Builder)

| # | Sub-task | Details |
|---|----------|---------|
| 6.3a | User profile section (experience, weight, age, gender) | |
| 6.3b | Progression context section (periodization model, trend, deload) | |
| 6.3c | Exercises section (ordered list with stableId, equipment, movement, difficulty) | |
| 6.3d | Training history section (last 3-5 sessions per exercise, truncation logic) | |
| 6.3e | Safety constraints section (all 9 rules) | |
| 6.3f | Cross-group fatigue section (if overlaps detected) | |
| 6.3g | Output format JSON schema section | |
| 6.3h | Token budget tracking (max ~2000 input tokens) | |

**Acceptance Criteria:**
- Gemini API call succeeds with valid API key
- Response is parsed into `GeneratedPlan` with all exercises
- Fallback chain works: AI → cached → baseline → manual
- Safety validation catches weight jumps >10%, volume ceiling violations
- Prompt includes all 9 safety constraints
- Plan caching works (same exercise set returns cached plan within 7 days)

---

## Epic 7: Onboarding Flow — DONE

**Owner:** Mid Dev
**Duration:** Weeks 13-14 (5 days)
**Depends on:** Epic 2 (UI), Epic 4 (ConsentManager, UserProfileRepository)
**Status:** COMPLETE — 4-screen flow (consent, experience, units, profile), OnboardingViewModel, CompleteOnboardingUseCase, navigation wiring, test.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 7.1 | Implement `ConsentScreen` (Screen 0) | 1d | `:feature:onboarding` | Analytics + Crashlytics toggles (default OFF), privacy explanation, "Continue" button |
| 7.2 | Implement experience level selector (Screen 1) | 0.5d | `:feature:onboarding` | 3 options: Beginner / Intermediate / Advanced |
| 7.3 | Implement unit preference selector (Screen 2) | 0.5d | `:feature:onboarding` | kg / lbs toggle |
| 7.4 | Implement optional profile fields (Screen 3) | 0.5d | `:feature:onboarding` | Age, height, gender — all optional, skip button |
| 7.5 | Implement `OnboardingViewModel` with MVI | 1d | `:feature:onboarding` | State management across 4 screens |
| 7.6 | Implement `CompleteOnboardingUseCase` | 0.5d | `:core:domain` | Creates UserProfile, sets onboarding_completed flag |
| 7.7 | Wire navigation: onboarding ↔ main app | 0.5d | `:app` | Check onboarding_completed on cold start |
| 7.8 | Write tests | 0.5d | `:feature:onboarding` | ViewModel + use case tests |

**Acceptance Criteria:**
- Consent defaults to OFF (no analytics until user opts in)
- Onboarding_completed flag in SharedPreferences (not Room)
- Skipping optional fields still completes onboarding
- On relaunch, app goes directly to main screen (not onboarding again)

---

## Epic 8: Workout Setup Flow — DONE

**Owner:** Mid Dev
**Duration:** Weeks 14-15 (8 days)
**Depends on:** Epic 5 (exercise selection), Epic 4 (OrderExercisesUseCase)
**Status:** COMPLETE — MuscleGroupSelector, ExerciseOrder with drag-reorder, WorkoutSetupViewModel, template loading path, navigation, test.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 8.1 | Implement `MuscleGroupSelector` screen | 1.5d | `:feature:workout` | 2-column grid of 7 groups per design-system.md Section 3.3 |
| 8.2 | Implement exercise picker integration | 1d | `:feature:workout` | Navigate to exercise selection, receive selected exercises back |
| 8.3 | Implement auto-ordering display | 0.5d | `:feature:workout` | Show ordered exercise list, allow manual drag reorder |
| 8.4 | Implement drag-to-reorder | 1d | `:feature:workout` | LazyColumn with drag handles |
| 8.5 | Implement "Generate Plan" CTA | 0.5d | `:feature:workout` | Button that triggers AI plan generation flow |
| 8.6 | Implement "Load Template" entry path | 0.5d | `:feature:workout` | Alternative entry that skips group/exercise selection |
| 8.7 | Wire setup → plan review → active workout navigation | 1d | `:feature:workout` + `:app` | Full navigation flow |
| 8.8 | Implement setup ViewModel (MVI) | 1.5d | `:feature:workout` | State: selected groups, selected exercises, ordered list |
| 8.9 | Write tests | 0.5d | | ViewModel tests |

**Acceptance Criteria:**
- User can select 1+ muscle groups
- Exercise picker shows exercises for selected groups
- Auto-ordering applies compounds-first rule with core-always-last
- User can manually reorder via drag
- Both "new workout" and "load template" paths work

---

## Epic 9: Active Workout Logging — DONE

**Owner:** Lead Dev
**Duration:** Weeks 16-18 (12 days)
**Depends on:** Epic 3 (DAOs), Epic 4 (state machine, repos), Epic 6 (plan data)
**HIGH RISK — most complex UI in the app**
**Status:** COMPLETE — WorkoutScreen, WorkoutViewModel (MVI), ExerciseCard, RestTimerBottomSheet, PausedOverlay, RestTimerManager, WorkoutActiveNavigation, WorkoutViewModelTest.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 9.1 | Implement `WorkoutScreen` layout | 2d | `:feature:workout` | LazyColumn of ExerciseCards, each containing SetRows |
| 9.2 | Implement `WorkoutViewModel` (MVI) | 2d | `:feature:workout` | Full intent handling: CompleteSet, StartRest, Pause, Resume, Finish |
| 9.3 | Implement `ExerciseCard` component | 1d | `:feature:workout` | Collapsible card per design-system.md Section 3.2 |
| 9.4 | Implement set completion flow | 1d | `:feature:workout` | Tap done → write to DB → update state → start rest timer |
| 9.5 | Implement `WorkoutForegroundService` | 1.5d | `:app` | Persistent notification with elapsed time, rest timer, quick actions |
| 9.6 | Implement `RestTimerManager` (singleton) | 0.5d | `:core:data` | Countdown with StateFlow, vibration on complete |
| 9.7 | Implement `ResolveRestTimerUseCase` | 0.5d | `:core:domain` | 4-level priority chain: AI plan → user override → global default → CSCS baseline |
| 9.8 | Implement `RestTimer` bottom sheet UI | 1d | `:feature:workout` | Circular progress, skip button, +30s button per design-system.md Section 3.4 |
| 9.9 | Implement add/delete set functionality | 0.5d | `:feature:workout` | User can add extra sets or skip planned sets |
| 9.10 | Implement workout completion flow | 0.5d | `:feature:workout` | Finish button → update session status → navigate to summary |
| 9.11 | Implement workout pause/resume | 0.5d | `:feature:workout` | Pause accumulates `pausedDurationSeconds` |
| 9.12 | Implement elapsed time display | 0.5d | `:feature:workout` | `SystemClock.elapsedRealtime()` based, excludes paused duration |
| 9.13 | Write unit tests (ViewModel, state machine integration) | 1d | `:feature:workout` | Tests per testing-strategy.md |

**Acceptance Criteria:**
- Set completion writes to DB immediately (auto-save)
- Rest timer runs in foreground service (survives backgrounding)
- Rest timer vibrates on completion
- Elapsed time excludes paused duration
- All set data persists across app backgrounding
- Workout can be paused and resumed

---

## Epic 10: Workout Templates — DONE

**Owner:** Mid Dev
**Duration:** Weeks 16-17 (5 days)
**Depends on:** Epic 3 (TemplateDao), Epic 4 (TemplateRepository)
**Status:** COMPLETE — TemplateListScreen, CreateTemplateScreen, ViewModels, SaveTemplateUseCase, TemplateCard component, navigation, TemplateListViewModelTest, CreateTemplateViewModelTest.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 10.1 | Implement `TemplateListScreen` | 1d | `:feature:templates` | LazyColumn of saved templates |
| 10.2 | Implement `TemplateListViewModel` | 0.5d | `:feature:templates` | Load templates, delete template |
| 10.3 | Implement `CreateTemplateScreen` | 0.5d | `:feature:templates` | Name input + exercise list display |
| 10.4 | Implement `SaveTemplateUseCase` | 0.5d | `:core:domain` | Save exercise selection + order as template |
| 10.5 | Implement save-as-template from workout summary | 0.5d | `:feature:workout` | "Save as template" button on summary |
| 10.6 | Implement load-template in workout setup | 0.5d | `:feature:workout` | Template loading skips group/exercise selection |
| 10.7 | Implement template edit/delete | 0.5d | `:feature:templates` | Edit name, delete with confirmation |
| 10.8 | Write tests | 0.5d | | ViewModel + use case tests |

**Acceptance Criteria:**
- Templates store exercise list + order (not weights/reps)
- Loading a template populates workout setup with saved exercises
- Templates can be created from workout summary or setup flow
- Delete requires confirmation dialog

---

## Epic 11: Workout Complete Summary — DONE

**Owner:** Mid Dev
**Duration:** Weeks 17-18 (4 days)
**Depends on:** Epic 9 (completed workout data), Epic 4 (VolumeCalculator)
**Status:** COMPLETE — GetWorkoutSummaryUseCase, CalculatePersonalRecordsUseCase, WorkoutSummary model, WorkoutSummarySheet (ModalBottomSheet), ViewModel + MVI contracts, navigation, PR gold star highlights, tests.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 11.1 | Implement `GetWorkoutSummaryUseCase` | 0.5d | `:core:domain` | Calculates duration, exercise count, volume, tonnage |
| 11.2 | Implement `WorkoutSummarySheet` | 1.5d | `:feature:workout` | Duration, exercises completed, per-group volume, total tonnage, PR highlights |
| 11.3 | Implement basic PR detection (weight PR only for MVP) | 1d | `:core:domain` | `CalculatePersonalRecordsUseCase` — compare against historical best |
| 11.4 | Implement PR highlight in summary | 0.5d | `:feature:workout` | Gold star icon, PR badge for new records |
| 11.5 | Write tests | 0.5d | | Summary calculation + PR detection tests |

**Acceptance Criteria:**
- Summary shows accurate duration (excluding paused time)
- Volume calculation excludes warm-up sets
- Weight PRs detected correctly (working sets only)
- "Save as Template" button visible

---

## Epic 12: Per-Exercise Notes — DONE

**Owner:** Mid Dev
**Duration:** Week 18 (2 days)
**Depends on:** Epic 9 (workout logging)
**Status:** COMPLETE — Notes icon added to ExerciseCard header, expandable TextField (max 1000 chars), debounced auto-save to WorkoutExerciseEntity.notes, notes visible in SessionDetailScreen, NotesIntegrationTest.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 12.1 | Add notes icon to ExerciseCard header | 0.25d | `:feature:workout` | Notes icon in overflow or inline |
| 12.2 | Implement notes text field (max 1000 chars) | 0.5d | `:feature:workout` | Expandable text field below exercise |
| 12.3 | Persist notes to `WorkoutExerciseEntity.notes` | 0.25d | `:core:data` | Auto-save on text change (debounced) |
| 12.4 | Show notes in workout history | 0.5d | `:feature:progress` | Display notes when viewing past sessions |
| 12.5 | Write tests | 0.5d | | Persistence + display tests |

**Acceptance Criteria:**
- Notes auto-save (no explicit save button)
- Notes visible in session history
- 1000 character max enforced at UI layer

---

## Epic 13: Data Persistence & Crash Recovery

**Owner:** Lead Dev
**Duration:** Weeks 19-20 (8 days)
**Depends on:** Epic 9 (active workout)
**HIGH RISK -- data integrity is non-negotiable**
**Status:** COMPLETE

### Tasks

| # | Task | Est. | Module | Status | Deliverable |
|---|------|------|--------|--------|-------------|
| 13.1 | Implement SavedStateHandle integration | 1d | `:feature:workout` | DONE (pre-existing) | `sessionId` survives process death via `SESSION_ID_KEY` in SavedStateHandle |
| 13.2 | Implement session restoration on ViewModel init | 1d | `:feature:workout` | DONE (pre-existing) | `loadSession()` reads SavedStateHandle first, falls back to `getActiveSession()`, rebuilds full UI state from Room |
| 13.3 | Implement abandoned session detection | 0.5d | `:core:domain` | DONE | `DetectAbandonedSessionUseCase` checks for active/paused sessions on startup |
| 13.4 | Implement 24-hour timeout for abandoned sessions | 0.5d | `:core:domain` | DONE | `CleanupStaleSessionsUseCase` marks sessions >24h as ABANDONED |
| 13.5 | Implement crash detection ("crashed" status) | 0.5d | `:app` | DONE | `SessionRecoveryUseCase` orchestrates cleanup + detection. `MainViewModel` marks as CRASHED on discard |
| 13.6 | Implement discard confirmation flow | 0.5d | `:feature:workout` | DONE | `ResumeOrDiscardDialog` composable + `MainViewModel` resume/discard handlers |
| 13.7 | Verify auto-save on every set completion | 0.5d | `:feature:workout` | DONE | Audited: `completeSet()` writes to Room immediately. Fixed `handleDeleteSet` to persist deletion. Fixed `handleAddSet` FK bug. |
| 13.8 | Verify transaction-based exercise additions | 0.5d | `:core:data` | DONE | Added `insertExerciseWithSets()` with `database.withTransaction{}`. Added `insertSet(workoutExerciseId, set)`. |
| 13.9 | Write process death tests | 1.5d | `:core:domain`, `:feature:workout` | DONE | `DetectAbandonedSessionUseCaseTest`, `CleanupStaleSessionsUseCaseTest`, `CrashRecoveryTest` |
| 13.10 | Write crash recovery test | 1d | `:feature:workout` | DONE | `CrashRecoveryTest` covers process death restoration, paused restoration, auto-save verification |

### Additional Fixes (discovered during audit)

- **BUG FIX:** `handleAddSet` was calling `insertSets()` with `workoutExerciseId = 0`, causing FK violation. Fixed to use new `insertSet(workoutExerciseId, set)`.
- **BUG FIX:** `handleDeleteSet` was only removing sets from in-memory state, not Room. On process death, deleted sets would reappear. Fixed to call `deleteSet()` immediately.
- **NEW:** Added `WorkoutSetDao.deleteById()` for set deletion persistence.
- **NEW:** Added `WorkoutSessionDao.getStaleActiveSessions()` for 24h cleanup query.
- **NEW:** Added `WorkoutSessionRepository.deleteSet()`, `insertSet()`, `getStaleActiveSessions()`, `insertExerciseWithSets()`.
- **NEW:** Injected `DeepRepsDatabase` into `WorkoutSessionRepositoryImpl` for cross-DAO transactions.

**Acceptance Criteria:**
- Process death during workout loses AT MOST the current in-progress set -- VERIFIED
- All previously completed sets survive process death -- VERIFIED
- On relaunch after crash: user sees "Resume or Discard?" dialog -- VERIFIED
- Abandoned sessions auto-detect after 24 hours -- VERIFIED
- No in-memory-only state for completed sets (Room is truth) -- VERIFIED

---

## Epic 14: Basic Progress Tracking — DONE

**Owner:** Mid Dev
**Duration:** Weeks 19-20 (8 days)
**Depends on:** Epic 3 (DAOs), Epic 4 (repos + calculators)
**Status:** COMPLETE — ProgressDashboardScreen, ExerciseProgressScreen, SessionDetailScreen, ProgressChart (Canvas line chart), TimeRangeSelector, 3 ViewModels, ProgressNavigation, ProgressDashboardViewModelTest, ExerciseProgressViewModelTest.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 14.1 | Implement `ProgressDashboardScreen` | 1d | `:feature:progress` | MVP: session history list + per-exercise navigation |
| 14.2 | Implement `ProgressDashboardViewModel` | 0.5d | `:feature:progress` | Load recent sessions, group by date |
| 14.3 | Implement `ExerciseProgressScreen` | 1.5d | `:feature:progress` | Weight progression chart over time for one exercise |
| 14.4 | Implement `ExerciseProgressViewModel` | 1d | `:feature:progress` | Load historical sets, compute chart data |
| 14.5 | Implement `ProgressChart` composable | 1.5d | `:feature:progress` | Line chart per design-system.md Section 3.5 |
| 14.6 | Implement `TimeRangeSelector` (4W/12W/6M/All) | 0.5d | `:feature:progress` | Segmented control, filters chart data |
| 14.7 | Implement session history detail view | 0.5d | `:feature:progress` | View past workout: exercises, sets, weights, notes |
| 14.8 | Write tests | 1d | | ViewModel tests, chart data calculation tests |

**Acceptance Criteria:**
- Session history shows all completed workouts ordered by date
- Per-exercise chart shows weight progression with correct time ranges
- Chart handles edge cases: 1 session (single dot), no data (empty state)
- Time range selector works (4W, 12W, 6M, All)

---

## Epic 15: Analytics Instrumentation — DONE

**Owner:** Lead Dev + Data Analyst
**Duration:** Weeks 20-22 (8 days)
**Depends on:** All feature epics substantially complete
**Status:** COMPLETE — FirebaseAnalyticsTracker + NoOpAnalyticsTracker, FirebaseFeatureFlagProvider + NoOpFeatureFlagProvider, AnalyticsModule DI, AppLifecycleTracker (ProcessLifecycleOwner), consent-gated collection, P0 events instrumented in OnboardingViewModel, WorkoutViewModel, PlanReviewViewModel. Tests for tracker consent gating + event mapping.

### Tasks

| # | Task | Est. | Module | Deliverable |
|---|------|------|--------|-------------|
| 15.1 | Implement `FirebaseAnalyticsTracker` | 1d | `:core:data` | Wraps Firebase Analytics SDK, implements `AnalyticsTracker` interface |
| 15.2 | Wire Firebase initialization with consent check | 0.5d | `:app` | Collection disabled by default, enabled only after consent |
| 15.3 | Instrument P0 onboarding events (#1-6) | 0.5d | `:feature:onboarding` | All 6 onboarding events fire correctly |
| 15.4 | Instrument P0 workout events (#23, 24, 35, 36, 37) | 1d | `:feature:workout` | workout_started, set_completed, workout_completed, workout_abandoned, workout_resumed |
| 15.5 | Instrument P0 AI plan events (#17-20) | 0.5d | `:feature:ai-plan` | ai_plan_requested, received, failed, fallback_used |
| 15.6 | Instrument P0 lifecycle events (#48-50) | 0.5d | `:app` | app_session_start, app_session_end, app_crash_detected |
| 15.7 | Implement `FirebaseFeatureFlagProvider` | 1d | `:core:data` | Firebase Remote Config integration |
| 15.8 | Set up Firebase project (Analytics, Crashlytics, Remote Config) | 0.5d | DevOps + Lead | Firebase console configured, google-services.json in project |
| 15.9 | Verify all P0 events in Firebase DebugView | 0.5d | DA + Lead | All events appear with correct properties |
| 15.10 | Write analytics verification tests | 0.5d | | Mock tracker captures expected events |

**Acceptance Criteria:**
- Analytics disabled by default (consent OFF)
- After consent, all P0 events fire with correct properties
- Events visible in Firebase DebugView
- Crashlytics reports crashes with correct metadata
- Feature flags load from Remote Config with local defaults fallback

---

## Week 22 Gate: Internal Alpha

**Feature-complete MVP.** ALL of the following must be true:

- [ ] All 14 MVP features implemented and working
- [ ] Core loop works end-to-end: select → plan → log → summary
- [ ] Crash recovery verified (process death test passes)
- [ ] P0 analytics events firing correctly
- [ ] Unit test coverage ≥ 80% on `:core:domain`
- [ ] No P0 or P1 bugs open
- [ ] Build passes CI (lint + tests + build)
- [ ] APK size < 25MB

**Decision maker:** CEO / Product Owner
