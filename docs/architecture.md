# Deep Reps -- Architecture Design Document

**Author:** Lead Android Developer / Technical Architect
**Status:** Approved for implementation
**Last updated:** 2026-02-11

This document is the technical blueprint for Deep Reps. Every architectural decision is made here, with rationale and trade-offs stated explicitly. The mid-senior developer, QA engineer, and DevOps engineer build from this document. If something contradicts this document, this document wins until it is formally amended.

---

## Table of Contents

1. [Tech Stack Selection](#1-tech-stack-selection)
2. [Module Structure](#2-module-structure)
3. [Data Architecture](#3-data-architecture)
4. [AI Provider Architecture](#4-ai-provider-architecture)
5. [State Management](#5-state-management)
6. [Performance Requirements](#6-performance-requirements)
7. [Security Considerations](#7-security-considerations)
8. [Project Structure](#8-project-structure)

---

## 1. Tech Stack Selection

### 1.1 Language: Kotlin

**Version:** Kotlin 2.0+ (latest stable at time of project setup)

No discussion needed. Kotlin is the only first-class language for Android development. Java is prohibited in new code. We use Kotlin-specific features aggressively: coroutines, Flow, sealed classes, extension functions, inline classes for type safety, and context receivers where appropriate.

### 1.2 UI Framework: Jetpack Compose

**Version:** BOM-managed, latest stable

All UI is Jetpack Compose. Zero XML layouts. We get declarative UI, first-class state management, animation APIs, and testability without the View system baggage. The entire team must be fluent in Compose recomposition semantics -- misunderstanding recomposition is the number one performance bug source in Compose apps.

### 1.3 Architecture Pattern: MVI (Model-View-Intent) with Clean Architecture Layers

**Decision:** MVI, not MVVM.

**Rationale:** The active workout screen is a complex, long-lived state machine with at least six states (idle, setup, generating_plan, active, paused, completed). MVVM with multiple mutable StateFlows leads to inconsistent state -- you end up with `isLoading = true` and `error != null` simultaneously because nothing enforces mutual exclusion. MVI solves this structurally:

- **Single immutable state object** per screen. The UI renders from one `UiState` sealed class. No partial state updates, no inconsistencies.
- **Intents** (user actions) are processed sequentially through a reducer, guaranteeing predictable state transitions.
- **Side effects** (navigation, toasts, vibration) are modeled explicitly as one-shot events via a `Channel` or `SharedFlow`, not baked into the state.

The cost: more boilerplate per screen (State, Intent, and SideEffect classes). Worth it for a workout logging app where state correctness is non-negotiable.

**Pattern per screen:**

```kotlin
// Contract file for each screen
data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.Idle,
    val exercises: List<WorkoutExerciseUi> = emptyList(),
    val activeRestTimer: RestTimerState? = null,
    val elapsedSeconds: Long = 0L,
)

sealed interface WorkoutIntent {
    data class CompleteSet(val exerciseId: Long, val setIndex: Int, val weight: Double, val reps: Int) : WorkoutIntent
    data class StartRestTimer(val durationSeconds: Int) : WorkoutIntent
    data object SkipRestTimer : WorkoutIntent
    data object PauseWorkout : WorkoutIntent
    data object ResumeWorkout : WorkoutIntent
    data object FinishWorkout : WorkoutIntent
}

sealed interface WorkoutSideEffect {
    data object Vibrate : WorkoutSideEffect
    data class NavigateToSummary(val sessionId: Long) : WorkoutSideEffect
}
```

```kotlin
// ViewModel processes intents, emits state + effects
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val completeSetUseCase: CompleteSetUseCase,
    private val workoutSessionRepository: WorkoutSessionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    private val _sideEffect = Channel<WorkoutSideEffect>(Channel.BUFFERED)
    val sideEffect: Flow<WorkoutSideEffect> = _sideEffect.receiveAsFlow()

    fun onIntent(intent: WorkoutIntent) {
        when (intent) {
            is WorkoutIntent.CompleteSet -> handleCompleteSet(intent)
            is WorkoutIntent.StartRestTimer -> handleStartRestTimer(intent)
            // ...
        }
    }

    private fun handleCompleteSet(intent: WorkoutIntent.CompleteSet) {
        viewModelScope.launch {
            completeSetUseCase(intent.exerciseId, intent.setIndex, intent.weight, intent.reps)
            // Update state immutably
            _state.update { currentState ->
                currentState.copy(
                    exercises = currentState.exercises.map { exercise ->
                        if (exercise.id == intent.exerciseId) {
                            exercise.copy(
                                sets = exercise.sets.mapIndexed { index, set ->
                                    if (index == intent.setIndex) set.copy(isCompleted = true) else set
                                }
                            )
                        } else exercise
                    }
                )
            }
        }
    }
}
```

**Clean Architecture layers are enforced by module boundaries**, not just package conventions. The three layers:

| Layer | Responsibility | Depends On |
|-------|---------------|------------|
| **presentation** | Compose UI, ViewModels, UI models, mappers | domain |
| **domain** | Use cases, repository interfaces, domain models | nothing (pure Kotlin) |
| **data** | Repository implementations, Room DAOs, network clients, mappers | domain |

The domain layer has zero Android dependencies. It is a pure Kotlin module. This is non-negotiable -- it enables unit testing without Robolectric and enforces proper separation.

### 1.4 Dependency Injection: Hilt

**Decision:** Hilt, not Koin.

**Rationale:**

- **Compile-time validation.** Hilt fails at build time if a dependency graph is broken. Koin fails at runtime. For a workout logging app where a crash during an active workout means lost data, compile-time safety wins.
- **Scoping.** Hilt has first-class support for `@ViewModelScoped`, `@ActivityRetainedScoped`, and `@SingletonScoped`. Workout state management needs precise lifecycle scoping. Koin can do this but requires manual discipline.
- **Team familiarity.** Hilt is the de facto standard for Android DI. The mid-senior dev is more likely to have experience with it.
- **Cost:** More annotation processing overhead at build time. Mitigated by KSP (not kapt) and incremental compilation.

Koin's advantages (no code generation, simpler setup, multiplatform) are irrelevant -- we are Android-only and will never go KMP for this project.

### 1.5 Database: Room

**Version:** Room 2.7+ with KSP

Room is the only serious choice for offline-first Android apps. SQLite underneath, type-safe query generation via KSP, first-class Flow/coroutines support, compile-time SQL validation.

Key configuration decisions:
- **KSP**, not kapt. Kapt is deprecated and slower.
- **Incremental annotation processing** enabled.
- **Write-ahead logging (WAL)** enabled for concurrent reads during writes (workout logging writes sets while the UI reads exercise lists).
- **Foreign keys enforced** for referential integrity. Losing an exercise reference from a workout set is a data corruption bug.

### 1.6 Networking: Ktor Client

**Decision:** Ktor Client, not Retrofit + OkHttp.

**Rationale:**

- **Kotlin-first.** Ktor is built in Kotlin, with native coroutine support. No Call adapters, no converter factories. Just `suspend fun`.
- **Lightweight.** We make exactly one type of network call: Gemini API requests. We do not have a REST backend with 50 endpoints. Retrofit's annotation-based interface generation is over-engineered for a single API.
- **Kotlinx Serialization integration** is native and seamless.
- **Engine flexibility.** We can swap the underlying engine (OkHttp, CIO, Android) without changing client code.

The cost: less community tooling for things like network interceptors and logging compared to OkHttp. Acceptable because our network surface is tiny.

```kotlin
// Ktor client setup
val httpClient = HttpClient(OkHttp) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = false
            encodeDefaults = true
        })
    }
    install(Logging) {
        level = LogLevel.BODY
        logger = object : Logger {
            override fun log(message: String) {
                Timber.tag("HTTP").d(message)
            }
        }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 10_000
    }
    defaultRequest {
        url("https://generativelanguage.googleapis.com/")
        contentType(ContentType.Application.Json)
    }
}
```

### 1.7 Serialization: Kotlinx Serialization

**Decision:** Kotlinx Serialization, not Moshi, not Gson.

**Rationale:**

- **Kotlin multiplatform and compiler plugin.** No reflection, no code generation at annotation processing time. The compiler plugin generates serializers directly.
- **Integrates natively** with Ktor and Room (via TypeConverters).
- **Sealed class serialization** works out of the box -- critical for our intent/state models.
- Gson is dead (no Kotlin support, reflection-based). Moshi is good but redundant when Kotlinx Serialization exists and integrates with our Ktor choice.

### 1.8 Image Loading: Coil

**Version:** Coil 3+ (Compose-first)

Coil is the only Kotlin-first image loader. Glide and Picasso are Java-legacy. We use Coil primarily for exercise anatomical diagrams (bundled as assets or loaded from local storage). Network image loading is minimal.

### 1.9 Navigation: Compose Navigation (Jetpack)

**Decision:** Jetpack Compose Navigation, not Voyager, not Decompose.

**Rationale:**

- **Google-maintained.** Receives updates alongside Compose itself. Breaking changes are documented in migration guides.
- **Type-safe navigation** with the `@Serializable` route approach (Navigation 2.8+). No more string-based route matching.
- **Deep link support** built in -- needed for notification taps (rest timer done, workout resume).
- **SavedStateHandle integration** for surviving process death.

Voyager is well-designed but community-maintained. For a production app that must be maintained for years, the risk of Voyager going unmaintained outweighs its ergonomic advantages.

```kotlin
// Type-safe routes
@Serializable
data object WorkoutSetupRoute

@Serializable
data class ActiveWorkoutRoute(val sessionId: Long)

@Serializable
data class WorkoutSummaryRoute(val sessionId: Long)

@Serializable
data class ExerciseDetailRoute(val exerciseId: Long)
```

### 1.10 State Management: StateFlow + Compose State

- **ViewModel layer:** `StateFlow<UiState>` as the single source of UI truth. Collected in Compose via `collectAsStateWithLifecycle()`.
- **Compose layer:** Local Compose `State` only for ephemeral UI state (text field focus, dropdown expansion, animation progress). Never for business state.
- **No LiveData.** StateFlow is strictly superior in a coroutine-based codebase.

### 1.11 Build System: Gradle with Version Catalogs

- **Gradle 8.x** with Kotlin DSL (`build.gradle.kts`).
- **Version Catalog** (`libs.versions.toml`) for centralized dependency management. No `buildSrc` -- version catalogs replaced that pattern.
- **Convention plugins** in a `build-logic` included build for shared build configuration across modules.

### 1.12 Logging: Timber

Timber for all logging. No `Log.d()` or `println()` anywhere. Timber trees are planted in debug builds only -- production builds have no log output.

### 1.13 Testing Stack

| Tool | Purpose |
|------|---------|
| **JUnit 5** | Test framework. JUnit 4 only where forced by Android instrumentation. |
| **Turbine** | Testing Kotlin Flows (StateFlow emissions from ViewModels). |
| **MockK** | Mocking. Kotlin-native, supports coroutines, suspend functions, and extension functions. Not Mockito. |
| **Compose UI Testing** | `createComposeRule()` for UI tests. Semantic-based assertions. |
| **Robolectric** | Unit-testing Android framework code without an emulator. Used sparingly -- prefer pure domain tests. |
| **Truth** (Google) | Assertion library. More readable than JUnit assertions. |

### 1.14 Other Libraries

| Library | Purpose |
|---------|---------|
| **Timber** | Logging |
| **LeakCanary** | Memory leak detection (debug only) |
| **Baseline Profiles** | Startup and runtime performance optimization |
| **Compose Compiler Reports** | Recomposition analysis during development |

---

## 2. Module Structure

### 2.1 Module Dependency Graph

```
:app
  +-- :feature:workout
  +-- :feature:exercise-library
  +-- :feature:progress
  +-- :feature:profile
  +-- :feature:ai-plan
  +-- :feature:templates
  +-- :feature:onboarding

:feature:workout
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:feature:exercise-library
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:feature:progress
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:feature:profile
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:feature:ai-plan
  +-- :core:domain
  +-- :core:network
  +-- :core:common

:feature:templates
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:feature:onboarding
  +-- :core:domain
  +-- :core:ui
  +-- :core:common

:core:data
  +-- :core:domain
  +-- :core:database
  +-- :core:network
  +-- :core:common

:core:database
  +-- :core:domain (for entity ↔ domain model mappers)
  +-- :core:common

:core:network
  +-- :core:common

:core:domain
  +-- :core:common

:core:ui
  +-- :core:domain (for domain models used in UI components)
  +-- :core:common

:core:common (no dependencies on other project modules)

:benchmark
  +-- :app
```

### 2.2 Module Descriptions

| Module | Type | Description |
|--------|------|-------------|
| `:app` | Application | Entry point. Hilt setup, navigation host, theme, splash screen. Contains no business logic. |
| `:feature:workout` | Feature | Active workout screen, set logging, rest timer UI, superset management, workout summary. |
| `:feature:exercise-library` | Feature | Exercise browsing by muscle group, exercise detail cards, exercise selection during workout setup. |
| `:feature:progress` | Feature | Charts, PR display, per-exercise metrics, per-group metrics, time range filtering. |
| `:feature:profile` | Feature | User profile, settings, unit preferences, body weight entries. |
| `:feature:ai-plan` | Feature | Plan generation orchestration, plan review/edit screen, fallback logic UI. |
| `:feature:templates` | Feature | Template creation, listing, loading, editing. |
| `:feature:onboarding` | Feature | Privacy/consent screen (Screen 0), experience level selection, unit preference, optional profile fields. |
| `:core:domain` | Library | Use cases, repository interfaces, domain models. Pure Kotlin -- zero Android dependencies. |
| `:core:data` | Library | Repository implementations. Bridges database and network into domain interfaces. |
| `:core:database` | Library | Room database, DAOs, entities, migrations, TypeConverters. |
| `:core:network` | Library | Ktor client setup, AI provider interface, Gemini implementation, request/response DTOs. |
| `:core:ui` | Library | Shared Compose components (buttons, cards, input fields, timer display), theme definition, design tokens. |
| `:core:common` | Library | Extension functions, constants, date/time utilities, result wrappers. Pure Kotlin. |
| `:benchmark` | Test | Macrobenchmark tests (startup, frame timing, scrolling). Depends on `:app`. Uses `AndroidBenchmarkConventionPlugin` from `build-logic`. |

### 2.3 Key Rules

1. **Feature modules never depend on other feature modules.** Communication between features goes through `:core:domain` or the navigation graph in `:app`.
2. **`:core:domain` has zero Android dependencies.** It is a pure Kotlin/JVM library module. `apply plugin: 'java-library'` plus `kotlin("jvm")`.
3. **`:core:data` is the only module that knows about both `:core:database` and `:core:network`.** Feature modules never import Room entities or Ktor clients directly.
4. **`:app` is the only module that sees all feature modules.** It assembles the navigation graph and provides the Hilt dependency graph root.

### 2.4 Build Time Optimization

- **Parallel module compilation.** With 14 modules, Gradle can compile independent modules in parallel. The dependency graph above is deliberately wide (many feature modules at the same level) rather than deep.
- **Convention plugins** in `build-logic/` to avoid duplicating `compileSdk`, `minSdk`, `kotlinOptions`, and compose compiler settings across modules.
- **KSP** instead of kapt everywhere. KSP supports incremental processing; kapt does not for most processors.
- **Build cache** enabled. CI shares a remote build cache (Gradle remote cache or GitHub Actions cache).
- **Non-transitive R classes** (`android.nonTransitiveRClass=true`) to reduce R class size and build time.
- **Configuration cache** enabled for Gradle task graph caching.
- **Target:** Clean build < 3 minutes on CI. Incremental build (single module change) < 30 seconds locally.

---

## 3. Data Architecture

### 3.1 Room Database Schema

```
+------------------+     +---------------------+     +------------------+
| muscle_groups    |     | exercises           |     | exercise_muscles |
|------------------|     |---------------------|     |------------------|
| id (PK)         |     | id (PK)             |     | exercise_id (FK) |
| name             |     | name                |     | muscle_group_id  |
| display_order    |     | description         |     |   (FK)           |
+------------------+     | equipment           |     | is_primary       |
                         | isolation_level     |     +------------------+
                         | primary_group_id(FK)|
                         | tips                |
                         | pros                |
                         | anatomy_asset_path  |
                         | display_order       |
                         +---------------------+

+-------------------------+     +------------------------+     +------------------+
| workout_sessions        |     | workout_exercises      |     | workout_sets     |
|-------------------------|     |------------------------|     |------------------|
| id (PK)                |     | id (PK)               |     | id (PK)         |
| started_at              |     | session_id (FK)        |     | workout_exercise |
| completed_at            |     | exercise_id (FK)       |     |   _id (FK)      |
| duration_seconds        |     | order_index            |     | set_index        |
| paused_duration_seconds |     | superset_group_id      |     | set_type         |
| status                  |     | rest_timer_seconds     |     | planned_weight   |
| notes                   |     | notes                  |     | planned_reps     |
| template_id (FK?)       |     +------------------------+     | actual_weight    |
+-------------------------+                                    | actual_reps      |
                                                               | is_completed     |
                                                               | completed_at     |
                                                               +------------------+

+------------------+     +------------------+     +---------------------+
| templates        |     | template_exercises|    | user_profile        |
|------------------|     |------------------|     |---------------------|
| id (PK)         |     | id (PK)         |     | id (PK, always 1)  |
| name             |     | template_id (FK) |     | experience_level    |
| created_at       |     | exercise_id (FK) |     | preferred_unit      |
| updated_at       |     | order_index      |     | age                 |
| muscle_groups    |     +------------------+     | height_cm           |
|   (JSON array)   |                              | gender              |
+------------------+                              | created_at          |
                                                  | updated_at          |
+---------------------+     +---------------------+
| personal_records    |     | body_weight_entries  |
|---------------------|     |---------------------|
| id (PK)            |     | id (PK)            |
| exercise_id (FK)    |     | weight_value (kg)   |
| record_type         |     | recorded_at         |
| weight_value        |     +---------------------+
| reps                |
| estimated_1rm       |
| achieved_at         |
| session_id (FK)     |
+---------------------+

+---------------------+
| cached_ai_plans     |
|---------------------|
| id (PK)            |
| exercise_ids_hash   |
| plan_json           |
| created_at          |
| experience_level    |
+---------------------+
```

### 3.2 Entity Definitions

```kotlin
// --- Muscle Groups & Exercises ---

@Entity(tableName = "muscle_groups")
data class MuscleGroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "display_order") val displayOrder: Int,
)

@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["primary_group_id"],
            onDelete = ForeignKey.RESTRICT,
        )
    ],
    indices = [Index("primary_group_id")],
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "stable_id") val stableId: String, // CSCS-defined string ID (e.g., "legs_barbell_squat"). Used in AI prompts, stable across app versions. Unique index.
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "equipment") val equipment: String, // "barbell", "dumbbell", "cable", "machine", "bodyweight", "kettlebell", "band", "ez_bar", "trap_bar" (9 types per exercise-science spec)
    @ColumnInfo(name = "movement_type") val movementType: String, // "compound" or "isolation" (renamed from isolationLevel per CSCS review)
    @ColumnInfo(name = "difficulty") val difficulty: String, // "beginner", "intermediate", "advanced" — CRITICAL: used for safety guardrails, auto-ordering, AI prompt
    @ColumnInfo(name = "primary_group_id") val primaryGroupId: Long,
    @ColumnInfo(name = "secondary_muscles") val secondaryMuscles: String, // JSON array of sub-muscle name strings (e.g., ["Glutes", "lower back", "core"]) — sub-muscle level, not group level
    @ColumnInfo(name = "tips") val tips: String, // stored as JSON array of strings
    @ColumnInfo(name = "pros") val pros: String, // stored as JSON array of strings
    @ColumnInfo(name = "anatomy_asset_path") val anatomyAssetPath: String,
    @ColumnInfo(name = "display_order") val displayOrder: Int, // ordering in exercise library browsing
    @ColumnInfo(name = "order_priority") val orderPriority: Int, // CSCS-defined priority for auto-ordering algorithm (lower = earlier in workout). Distinct from displayOrder.
    @ColumnInfo(name = "superset_tags") val supersetTags: String, // JSON array of compatibility tags per exercise-science Section 6.3.1
    @ColumnInfo(name = "auto_program_min_level") val autoProgramMinLevel: Int = 1, // Min experience level for AI auto-inclusion (1/2/3, 99=manual only)
)

@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exercise_id", "muscle_group_id"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["muscle_group_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("exercise_id"), Index("muscle_group_id")],
)
data class ExerciseMuscleEntity(
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "muscle_group_id") val muscleGroupId: Long,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean,
)

// --- Workout Session ---

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "started_at") val startedAt: Long, // epoch millis
    @ColumnInfo(name = "completed_at") val completedAt: Long?, // null if in progress
    @ColumnInfo(name = "duration_seconds") val durationSeconds: Long?,
    @ColumnInfo(name = "status") val status: String, // "active", "paused", "completed", "discarded", "abandoned", "crashed"
    // Status semantics:
    //   "active"    — workout currently in progress
    //   "paused"    — user explicitly paused
    //   "completed" — user finished normally
    //   "discarded" — user explicitly chose to discard the session (UI confirmation required)
    //   "abandoned" — user never returned; detected after 24-hour timeout by cleanup logic
    //   "crashed"   — app crashed during session; detected on next startup via active session recovery
    @ColumnInfo(name = "paused_duration_seconds") val pausedDurationSeconds: Long = 0, // Accumulated pause time in seconds. When paused, pause duration is accumulated. On resume: elapsed = currentTime - startedAt - pausedDurationSeconds. Survives process death because it is persisted to Room.
    @ColumnInfo(name = "notes") val notes: String?,
    @ColumnInfo(name = "template_id") val templateId: Long?,
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("session_id"), Index("exercise_id")],
)
data class WorkoutExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "session_id") val sessionId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    @ColumnInfo(name = "superset_group_id") val supersetGroupId: Int?, // null = not in a superset, same int = same group
    @ColumnInfo(name = "rest_timer_seconds") val restTimerSeconds: Int?,
    @ColumnInfo(name = "notes") val notes: String?,
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("workout_exercise_id")],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "workout_exercise_id") val workoutExerciseId: Long,
    @ColumnInfo(name = "set_index") val setIndex: Int,
    @ColumnInfo(name = "set_type") val setType: String, // "warmup", "working"
    @ColumnInfo(name = "planned_weight") val plannedWeight: Double?,
    @ColumnInfo(name = "planned_reps") val plannedReps: Int?,
    @ColumnInfo(name = "actual_weight") val actualWeight: Double?,
    @ColumnInfo(name = "actual_reps") val actualReps: Int?,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
    @ColumnInfo(name = "completed_at") val completedAt: Long?,
)

// --- Templates ---

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "muscle_groups_json") val muscleGroupsJson: String, // JSON array of group IDs
)

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["template_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("template_id"), Index("exercise_id")],
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "template_id") val templateId: Long,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
)

// --- User Profile ---

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Long = 1, // singleton row
    @ColumnInfo(name = "experience_level") val experienceLevel: Int, // 1=beginner, 2=intermediate, 3=advanced
    @ColumnInfo(name = "preferred_unit") val preferredUnit: String, // "kg" or "lbs"
    @ColumnInfo(name = "age") val age: Int?,
    @ColumnInfo(name = "height_cm") val heightCm: Double?,
    @ColumnInfo(name = "gender") val gender: String?, // "male", "female", or null
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Entity(tableName = "body_weight_entries")
data class BodyWeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "weight_value") val weightValue: Double, // ALWAYS stored in kg. Convert to kg on write, convert to display unit on read. See "Weight Storage Convention" note below.
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
)

// --- Weight Storage Convention ---
// ALL weight values in the database are stored in kilograms (kg).
// This applies to: body_weight_entries.weight_value, workout_sets.planned_weight,
// workout_sets.actual_weight, personal_records.weight_value, personal_records.estimated_1rm.
// Conversion to the user's preferred display unit (kg or lbs) happens at the repository/mapper
// layer on read. Conversion from display unit to kg happens at the repository layer on write.
// This eliminates unit ambiguity in queries, AI prompts, and PR calculations.
// Conversion factor: 1 kg = 2.20462 lbs.

// --- Personal Records ---

@Entity(
    tableName = "personal_records",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["session_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("exercise_id"), Index("session_id")],
)
data class PersonalRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Long,
    @ColumnInfo(name = "record_type") val recordType: String, // "weight", "reps", "estimated_1rm", "volume"
    @ColumnInfo(name = "weight_value") val weightValue: Double?,
    @ColumnInfo(name = "reps") val reps: Int?,
    @ColumnInfo(name = "estimated_1rm") val estimated1rm: Double?,
    @ColumnInfo(name = "achieved_at") val achievedAt: Long,
    @ColumnInfo(name = "session_id") val sessionId: Long?,
)

// --- Cached AI Plans ---

@Entity(
    tableName = "cached_ai_plans",
    indices = [Index("exercise_ids_hash")],
)
data class CachedAiPlanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "exercise_ids_hash") val exerciseIdsHash: String, // SHA-256 of sorted exercise IDs
    @ColumnInfo(name = "plan_json") val planJson: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "experience_level") val experienceLevel: Int,
)
```

### 3.3 Key Indices

Beyond the foreign key indices above, the following composite indices are critical for query performance:

```kotlin
// On workout_sessions: find sessions by status and date range
@Entity(
    indices = [
        Index("status"),
        Index("started_at"),
        Index(value = ["status", "started_at"]),
    ]
)

// On workout_sets: find all sets for an exercise across sessions (for progress tracking)
// This requires a JOIN, but indexing workout_exercise_id + is_completed helps
@Entity(
    indices = [
        Index("workout_exercise_id"),
        Index(value = ["workout_exercise_id", "is_completed"]),
    ]
)

// On personal_records: find PRs for an exercise by type
@Entity(
    indices = [
        Index(value = ["exercise_id", "record_type"]),
    ]
)
```

### 3.4 Repository Pattern

Each repository interface lives in `:core:domain`. The implementation lives in `:core:data`.

```kotlin
// :core:domain
interface WorkoutSessionRepository {
    fun observeActiveSession(): Flow<WorkoutSession?>
    fun observeSessionById(sessionId: Long): Flow<WorkoutSession?>
    fun observeSessionsForExercise(exerciseId: Long, limit: Int): Flow<List<WorkoutSession>>
    fun observeSessionsInRange(startEpoch: Long, endEpoch: Long): Flow<List<WorkoutSession>>
    suspend fun createSession(session: WorkoutSession): Long
    suspend fun updateSession(session: WorkoutSession)
    suspend fun completeSet(workoutExerciseId: Long, setIndex: Int, weight: Double, reps: Int)
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, orderIndex: Int) // (Phase 2) Mid-workout exercise modification
    suspend fun removeExerciseFromSession(workoutExerciseId: Long) // (Phase 2) Mid-workout exercise modification
    suspend fun reorderExercises(sessionId: Long, exerciseOrder: List<Long>) // (Phase 2) Mid-workout exercise modification
}
```

```kotlin
// :core:data
class WorkoutSessionRepositoryImpl @Inject constructor(
    private val workoutSessionDao: WorkoutSessionDao,
    private val workoutExerciseDao: WorkoutExerciseDao,
    private val workoutSetDao: WorkoutSetDao,
    private val dispatchers: DispatcherProvider,
) : WorkoutSessionRepository {

    override fun observeActiveSession(): Flow<WorkoutSession?> =
        workoutSessionDao.observeByStatus("active")
            .map { entity -> entity?.toDomain() }
            .flowOn(dispatchers.io)

    override suspend fun completeSet(
        workoutExerciseId: Long,
        setIndex: Int,
        weight: Double,
        reps: Int,
    ) = withContext(dispatchers.io) {
        workoutSetDao.updateActuals(
            workoutExerciseId = workoutExerciseId,
            setIndex = setIndex,
            actualWeight = weight,
            actualReps = reps,
            isCompleted = true,
            completedAt = System.currentTimeMillis(),
        )
    }
}
```

**Every query that powers a UI screen returns a `Flow`.** The UI reacts to database changes. No manual refresh. Room's invalidation tracker ensures Flows re-emit when underlying tables change.

### 3.5 Offline-First Strategy

**Principle:** Room is the single source of truth. The network is a secondary data source used exclusively for AI plan generation.

**Data flow for AI plans:**

```
User taps "Generate Plan"
  |
  v
Check network connectivity
  |
  +-- Online: Call Gemini API
  |     |
  |     +-- Success: Parse response, cache in cached_ai_plans table, return to UI
  |     +-- Failure: Fall through to offline fallback
  |
  +-- Offline: Offline fallback chain
        |
        +-- 1. Check cached_ai_plans for same exercise set (by hash)
        |     +-- Found & < 7 days old: Use cached plan
        |
        +-- 2. No cache: Generate baseline plan locally
        |     (experience-level-based defaults, no AI)
        |
        +-- 3. Baseline unavailable: Manual entry mode
              (empty plan, user fills in everything)
```

**There is no cloud sync of workout data in the MVP.** All workout logs, personal records, and user profile data are local-only. Cloud sync is a future architecture decision (see FEATURES.md section 7). The data layer is designed so that adding sync later requires implementing a `SyncRepository` decorator around existing repositories -- no changes to domain or presentation layers.

### 3.6 Data Migration Strategy

- **Room auto-migration** for simple schema changes (adding columns, adding tables).
- **Manual migration** (`Migration(fromVersion, toVersion)`) for destructive changes (renaming columns, changing types, splitting tables).
- **Every migration is tested.** A `MigrationTest` class uses Room's `MigrationTestHelper` to verify that data survives every version transition.
- **Pre-populated database** for the exercise library. The initial exercise data (muscle groups, exercises, exercise-muscle mappings) is shipped as a `.db` file in assets via `createFromAsset()`. Updates to the exercise library are delivered as migrations.
- **Database version starts at 1.** Version numbers increment by exactly 1.

```kotlin
@Database(
    entities = [
        MuscleGroupEntity::class,
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        UserProfileEntity::class,
        BodyWeightEntryEntity::class,
        PersonalRecordEntity::class,
        CachedAiPlanEntity::class,
    ],
    version = 1,
    exportSchema = true, // MANDATORY for migration testing
)
@TypeConverters(Converters::class)
abstract class DeepRepsDatabase : RoomDatabase() {
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun workoutExerciseDao(): WorkoutExerciseDao
    abstract fun workoutSetDao(): WorkoutSetDao
    abstract fun templateDao(): TemplateDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun personalRecordDao(): PersonalRecordDao
    abstract fun cachedAiPlanDao(): CachedAiPlanDao
}
```

### 3.7 Defensive Persistence

Losing a user's workout data is the single worst bug this app can have. The persistence strategy is defensive:

1. **Auto-save on every set completion.** When the user taps "done" on a set, the DAO write happens immediately. Not debounced, not batched. One set = one write.
2. **Transaction-based exercise additions.** Adding an exercise to a session creates the `WorkoutExerciseEntity` and all its `WorkoutSetEntity` rows in a single `@Transaction`. Either all rows are written or none.
3. **Active session detection on startup.** If the app starts and finds a `WorkoutSessionEntity` with `status = "active"`, it prompts the user to resume or discard. This handles process death, OS kill, and crashes.
4. **WAL mode** for concurrent reads/writes. The workout screen reads exercise/set data while the user is completing sets (writes). WAL prevents read-write contention.
5. **No in-memory-only state for completed sets.** The moment a set is completed, it exists in the database. The ViewModel's in-memory state is a projection of the database state, not the source of truth.

### 3.8 User Identity Strategy

**MVP:** Firebase `app_instance_id` is used as the `user_id` for analytics and crash reporting. This is a device-scoped, auto-generated identifier that requires no authentication flow.

**Limitation:** Reinstalling the app resets the `app_instance_id`. The user's analytics history is split across installs. There is no way to correlate pre-reinstall and post-reinstall data for the same user.

**Post-MVP requirement:** Cross-install identity requires Firebase Authentication (anonymous auth at minimum, or Google Sign-In). This would provide a stable `uid` that survives reinstalls and enables future cloud sync. This is explicitly flagged as a post-MVP decision -- it introduces authentication UI, token management, and backend dependency that are out of scope for the initial launch.

---

## 4. AI Provider Architecture

### 4.1 Provider Interface

The provider interface lives in `:core:domain`. It has zero knowledge of Gemini, HTTP, or any specific LLM API.

```kotlin
// :core:domain
interface AiPlanProvider {
    /**
     * Generates a workout plan for the given exercises and user context.
     * Returns a structured plan or a domain-specific error.
     */
    suspend fun generatePlan(request: PlanRequest): Result<GeneratedPlan>
}

data class PlanExercise(
    val exerciseId: Long,
    val stableId: String, // CSCS stable ID for AI prompt
    val name: String,
    val equipment: String,
    val movementType: String, // "compound" or "isolation" (renamed from isolationLevel)
    val difficulty: String, // "beginner", "intermediate", "advanced"
)

data class UserPlanProfile(
    val experienceLevel: Int, // 1, 2, or 3
    val bodyWeightKg: Double?,
    val age: Int?,
    val gender: String?,
)

data class ExerciseHistory(
    val exerciseId: Long,
    val exerciseName: String,
    val sessions: List<HistoricalSession>,
)

data class HistoricalSession(
    val date: Long,
    val sets: List<HistoricalSet>,
)

data class HistoricalSet(
    val weight: Double,
    val reps: Int,
    val setType: String,
)

data class GeneratedPlan(
    val exercisePlans: List<ExercisePlan>,
    val sessionSummary: SessionSummary? = null,
    val promptVersion: String = GeminiPromptBuilder.PROMPT_VERSION,
)

data class ExercisePlan(
    val exerciseId: Long, // Room PK for internal use
    val stableExerciseId: String, // CSCS stable ID used in AI prompts (e.g., "chest_barbell_bench_press")
    val warmupSets: List<PlannedSet>,
    val workingSets: List<PlannedSet>,
    val restSeconds: Int, // per-exercise rest timer from AI plan or CSCS defaults
    val notes: String? = null, // AI rationale for weight/rep selection (e.g., "Based on last session: 80kg x 8. +2.5kg progressive overload.")
)

data class PlannedSet(
    val weight: Double,
    val reps: Int,
    val setNumber: Int,
)

data class SessionSummary(
    val totalWorkingSets: Int,
    val estimatedDurationMinutes: Int,
    val volumeCheck: String, // "ok" or warning message if ceilings approached
)
```

### 4.2 Gemini Implementation

Lives in `:core:network`. This is the only module that knows about Gemini.

```kotlin
// :core:network
class GeminiPlanProvider @Inject constructor(
    private val httpClient: HttpClient,
    private val promptBuilder: GeminiPromptBuilder,
    private val responseParser: GeminiResponseParser,
    @GeminiApiKey private val apiKey: String,
) : AiPlanProvider {

    override suspend fun generatePlan(request: PlanRequest): Result<GeneratedPlan> =
        runCatching {
            val prompt = promptBuilder.build(request)
            val response = httpClient.post("v1beta/models/gemini-2.0-flash:generateContent") {
                parameter("key", apiKey)
                setBody(GeminiRequestBody(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.3f,
                        topP = 0.8f,
                        maxOutputTokens = 2048,
                        responseMimeType = "application/json",
                    ),
                ))
            }
            val body = response.body<GeminiResponse>()
            val text = body.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw AiProviderException("Empty response from Gemini")
            responseParser.parse(text, request.exercises)
        }.recoverCatching { throwable ->
            when (throwable) {
                is AiProviderException -> throw throwable
                is ClientRequestException -> throw AiProviderException(
                    "Gemini API error: ${throwable.response.status}",
                    throwable,
                )
                is HttpRequestTimeoutException -> throw AiProviderException(
                    "Gemini API timeout",
                    throwable,
                )
                else -> throw AiProviderException(
                    "Unexpected error calling Gemini API",
                    throwable,
                )
            }
        }
}
```

### 4.3 Prompt Construction

The `GeminiPromptBuilder` constructs a prompt from training history, user context, and CSCS-approved safety constraints. The prompt template MUST match `docs/exercise-science.md` Section 5.4 — the CSCS-approved canonical prompt. The developer implements the data injection.

**PROMPT_VERSION** must be tracked as a constant and stored with every generated plan (see `CachedAiPlanEntity`).

```kotlin
class GeminiPromptBuilder @Inject constructor(
    private val overlapDetector: CrossGroupOverlapDetector,
) {
    companion object {
        const val PROMPT_VERSION = "v2.0"
    }

    fun build(request: PlanRequest): String = buildString {
        appendLine("You are a certified strength & conditioning specialist. Generate a structured workout plan as JSON.")
        appendLine()
        // --- USER PROFILE ---
        appendLine("## User Profile")
        appendLine("- Experience level: ${experienceLevelLabel(request.userProfile.experienceLevel)}")
        request.userProfile.bodyWeightKg?.let { appendLine("- Body weight: ${it}kg") }
        request.userProfile.age?.let { appendLine("- Age: $it") }
        request.userProfile.gender?.let { appendLine("- Gender: $it") }
        appendLine()

        // --- PROGRESSION CONTEXT (per exercise-science Section 5.1) ---
        appendLine("## Progression Context")
        appendLine("- Periodization model: ${request.periodizationModel}") // "linear", "dup", "block"
        request.performanceTrend?.let { appendLine("- Performance trend: $it") } // "improving", "stalled", "regressing"
        request.weeksSinceDeload?.let { appendLine("- Weeks since last deload: $it") }
        if (request.deloadRecommended) appendLine("- ⚠️ DELOAD RECOMMENDED — reduce volume by 40-60%, reduce intensity by 10-15%")
        request.currentBlockPhase?.let { appendLine("- Current block phase: $it (week ${request.currentBlockWeek})") }
        appendLine()

        // --- EXERCISES ---
        appendLine("## Exercises (in order)")
        request.exercises.forEachIndexed { index, exercise ->
            appendLine("${index + 1}. ${exercise.name} [stable_id: ${exercise.stableId}] (${exercise.equipment}, ${exercise.movementType}, difficulty: ${exercise.difficulty})")
        }
        appendLine()

        // --- TRAINING HISTORY ---
        if (request.trainingHistory.isNotEmpty()) {
            appendLine("## Recent Training History (last 3-5 sessions per exercise)")
            request.trainingHistory.forEach { history ->
                appendLine("### ${history.exerciseName}")
                history.sessions.takeLast(5).forEach { session ->
                    appendLine("  Session (${formatDate(session.date)}):")
                    session.sets.forEach { set ->
                        appendLine("    ${set.weight}kg x ${set.reps} (${set.setType})")
                    }
                }
            }
            appendLine()
        }

        // --- SAFETY CONSTRAINTS (CRITICAL — per exercise-science Section 5.2) ---
        appendLine("## SAFETY CONSTRAINTS (NON-NEGOTIABLE)")
        appendLine("1. Max weight increase: 10% above the last working weight for any exercise. Never exceed 10kg absolute jump for barbells, 5kg for dumbbells.")
        appendLine("2. MRV ceiling: Do not exceed ${getMrvCeiling(request.userProfile.experienceLevel)} total working sets per muscle group per session.")
        appendLine("3. Total session volume: Maximum 30 working sets per session, maximum 6 working sets per exercise, maximum 12 exercises per session.")
        appendLine("4. Advanced exercise gating: Only include exercises with difficulty level <= user experience level. Never include advanced exercises for beginners.")
        appendLine("5. Warm-up sets: Heavy barbell compounds require 3 warm-up sets (empty bar, 50%, 75%). Moderate compounds require 2. Isolations require 1 or 0 (bodyweight).")
        appendLine("6. Max RPE 9-10 exercises: At most 2 exercises per session at near-max effort.")
        // Age-adjusted modifiers (per exercise-science Section 8.6)
        request.userProfile.age?.let { age ->
            when {
                age < 18 -> appendLine("7. AGE MODIFIER (under 18): Cap intensity at 85% 1RM. No singles (1-rep sets). Focus on movement quality.")
                age in 41..50 -> appendLine("7. AGE MODIFIER (41-50): Reduce max intensity by 2.5%. Add +15s rest between sets.")
                age in 51..60 -> appendLine("7. AGE MODIFIER (51-60): Reduce max intensity by 5%. Add 1 extra warm-up set per compound. Increase rest by 30s. Reduce weekly volume by 10%.")
                age > 60 -> appendLine("7. AGE MODIFIER (60+): Reduce max intensity by 10%. Add 2 extra warm-up sets. Prefer machine exercises over free weights. Increase rest by 45s. Reduce weekly volume by 20%.")
                else -> {} // 18-40: No age modifier needed
            }
        }
        appendLine("8. Weight rounding: Round all weights DOWN to nearest increment (barbell: 2.5kg, dumbbell: 2.5kg, cable/machine: 5kg).")
        appendLine("9. When no training history exists, use baseline tables from exercise-science Section 4 (body weight ratios by experience level).")
        appendLine()

        // --- EXERCISE-SPECIFIC CONTRAINDICATIONS (per exercise-science Section 8.4) ---
        val contraindications = buildList {
            if (request.exercises.any { it.stableId == "back_barbell_good_morning" }) {
                add("Good Morning: Max weight = 60% of squat working weight. Never appears in beginner plans.")
            }
            if (request.exercises.any { it.stableId == "shoulders_barbell_upright_row" }) {
                add("Barbell Upright Row: Pull to chest height only. Stop if shoulder pain occurs. Do not program above moderate weight for beginners.")
            }
            if (request.exercises.any { it.stableId == "arms_barbell_skull_crusher" }) {
                add("Skull Crusher: Maximum 3 working sets. Recommend EZ bar over straight bar.")
            }
            if (request.exercises.any { it.stableId == "back_barbell_deficit_deadlift" }) {
                val hasDeadliftRegression = request.trainingHistory
                    .filter { it.exerciseName == "Conventional Deadlift" }
                    .any { it.trend == "regressing" }
                if (hasDeadliftRegression) {
                    add("Deficit Deadlift: EXCLUDED — conventional deadlift history shows regression. Substitute with conventional deadlift.")
                } else {
                    add("Deficit Deadlift: Only program if conventional deadlift history shows no regression pattern.")
                }
            }
            if (request.exercises.any { it.stableId == "core_dragon_flag" || it.stableId == "core_ab_wheel_rollout" }) {
                if (request.userProfile.experienceLevel == 1) {
                    add("Dragon Flag / Ab Wheel Rollout: EXCLUDED for beginners — never auto-programmed at experience level 1.")
                }
            }
            if (request.exercises.any { it.stableId == "core_ab_wheel_rollout" }) {
                add("Ab Wheel Rollout: Maintain posterior pelvic tilt throughout. If lower back arches, reduce range of motion.")
            }
        }
        if (contraindications.isNotEmpty()) {
            appendLine("## EXERCISE-SPECIFIC SAFETY CONSTRAINTS (NON-NEGOTIABLE)")
            contraindications.forEach { appendLine("- $it") }
            appendLine()
        }

        // --- CROSS-GROUP FATIGUE (per exercise-science Section 2.2) ---
        val overlaps = overlapDetector.detect(request.exercises)
        if (overlaps.isNotEmpty()) {
            appendLine("## CROSS-GROUP FATIGUE WARNING")
            overlaps.forEach { overlap ->
                appendLine("- ${overlap.description}")
            }
            appendLine("Reduce isolation volume for overlapping muscles accordingly.")
            appendLine()
        }

        // --- OUTPUT FORMAT (aligned with exercise-science Section 5.3) ---
        appendLine("## Output Format")
        appendLine("Respond ONLY with valid JSON matching this schema:")
        appendLine("""
        {
          "exercise_plans": [
            {
              "exercise_id": "<stable_id string>",
              "warmup_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}],
              "working_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}],
              "rest_seconds": <number>,
              "notes": "<brief rationale for weight/rep selection>"
            }
          ],
          "session_summary": {
            "total_working_sets": <number>,
            "estimated_duration_minutes": <number>,
            "volume_check": "<ok or warning message>"
          }
        }
        """.trimIndent())
    }

    private fun getMrvCeiling(experienceLevel: Int): Int = when (experienceLevel) {
        1 -> 12 // beginner
        2 -> 16 // intermediate
        3 -> 20 // advanced
        else -> 12
    }

    private fun experienceLevelLabel(level: Int): String = when (level) {
        1 -> "Total Beginner (0-6 months)"
        2 -> "Intermediate (6-18 months)"
        3 -> "Advanced (18+ months)"
        else -> "Unknown"
    }
}
```

**`PlanRequest` data class** (extended with progression context per CSCS review Issue 3.2):

```kotlin
data class PlanRequest(
    val userProfile: UserPlanProfile,
    val exercises: List<ExerciseForPlan>,
    val trainingHistory: List<ExerciseHistory>,
    val periodizationModel: String, // "linear", "dup", "block" — determined by DetermineSessionDayTypeUseCase
    val performanceTrend: String?, // "improving", "stalled", "regressing"
    val weeksSinceDeload: Int?,
    val deloadRecommended: Boolean,
    val currentBlockPhase: String?, // "accumulation", "intensification", "realization", "deload" (advanced only)
    val currentBlockWeek: Int?, // week within current block
)
```

### 4.4 Token Limit Management

Gemini 2.0 Flash has a context window that is large enough for our use case. However, we impose self-limits to control cost and latency:

- **Max input tokens: ~2000.** The prompt builder tracks character count as a proxy (1 token ~ 4 chars). If training history would exceed the budget, it truncates oldest sessions first.
- **Max output tokens: 2048.** Set in `generationConfig`. Sufficient for 10-15 exercises with full set prescriptions.
- **Temperature: 0.3.** Low creativity -- we want consistent, predictable plans. Not creative writing.
- **Response MIME type: `application/json`.** Forces structured JSON output mode where supported.

### 4.5 Fallback Chain

Implemented as a `PlanGenerationUseCase` in `:core:domain`:

```kotlin
class GeneratePlanUseCase @Inject constructor(
    private val aiProvider: AiPlanProvider,
    private val cachedPlanRepository: CachedPlanRepository,
    private val baselinePlanGenerator: BaselinePlanGenerator,
    private val connectivityChecker: ConnectivityChecker,
) {
    suspend fun execute(request: PlanRequest): PlanResult {
        // Step 1: Try AI provider (online)
        if (connectivityChecker.isOnline()) {
            val aiResult = aiProvider.generatePlan(request)
            if (aiResult.isSuccess) {
                val plan = aiResult.getOrThrow()
                cachedPlanRepository.cachePlan(request.exercises, plan)
                return PlanResult.AiGenerated(plan)
            }
            // AI failed despite connectivity -- fall through
        }

        // Step 2: Try cached plan
        val cached = cachedPlanRepository.findCachedPlan(
            exerciseIds = request.exercises.map { it.exerciseId },
            maxAgeDays = 7,
        )
        if (cached != null) {
            return PlanResult.Cached(cached)
        }

        // Step 3: Baseline plan (experience-level defaults, no AI)
        val baseline = baselinePlanGenerator.generate(request)
        if (baseline != null) {
            return PlanResult.Baseline(baseline)
        }

        // Step 4: Manual entry (empty plan)
        return PlanResult.Manual
    }
}

sealed interface PlanResult {
    data class AiGenerated(val plan: GeneratedPlan) : PlanResult
    data class Cached(val plan: GeneratedPlan) : PlanResult
    data class Baseline(val plan: GeneratedPlan) : PlanResult
    data object Manual : PlanResult
}
```

### 4.6 Swapping Providers

To swap from Gemini to another LLM (OpenAI, Claude, local model):

1. Create a new class implementing `AiPlanProvider` (e.g., `OpenAiPlanProvider`).
2. Update the Hilt `@Binds` or `@Provides` in the network DI module to bind the new implementation.
3. Nothing else changes. The domain layer, presentation layer, and fallback chain are untouched.

```kotlin
// :core:network di module
@Module
@InstallIn(SingletonComponent::class)
abstract class AiProviderModule {
    @Binds
    abstract fun bindAiPlanProvider(impl: GeminiPlanProvider): AiPlanProvider
    // To swap: change GeminiPlanProvider to OpenAiPlanProvider
}
```

### 4.7 Missing Domain Use Cases (Added per CSCS + PO Cross-Reviews)

The following use cases are required in `:core:domain` and were identified as gaps by the CSCS, Product Owner, and Lead Dev cross-reviews.

**`OrderExercisesUseCase`** — Implements the auto-ordering algorithm from `exercise-science.md` Section 6. Pure Kotlin function. Rules: (1) compounds before isolations, (2) larger groups before smaller within compounds, (3) same order within isolations, (4) difficulty descending within same group/type, (5) core exercises always last.

**`ValidatePlanSafetyUseCase`** — Runs after AI plan parsing, before presenting to user. Validates: (a) weight jumps within 10% of last session per exercise, (b) volume ceilings not exceeded (30 total sets, 16/group, 6/exercise), (c) age-based intensity caps respected, (d) advanced exercises not included for beginners. Rejects non-compliant plans, triggers retry or adjusts weights downward.

**`DetectDeloadNeedUseCase`** — Checks: (a) weeks since last deload (scheduled: beginner 6wk, intermediate 4wk, advanced 5wk), (b) regression patterns (2+ consecutive sessions with weight/rep decrease), (c) user-requested flag. Returns `DeloadStatus` (not_needed / proactive_recommended / reactive_recommended / user_requested). Fed into `PlanRequest`.

**`DetermineSessionDayTypeUseCase`** — See Section 4.9 (Block Periodization State) for full specification. Handles DUP day-type cycling for intermediate users and block phase inference for advanced users. Result feeds into `PlanRequest.periodizationModel`.

**`DetectOvertrainingWarningsUseCase`** — Checks 5 trigger conditions from `exercise-science.md` Section 8.3: (1) MRV exceeded 2+ consecutive weeks, (2) performance regression 3+ sessions, (3) frequency > 6x/week for 2+ weeks, (4) same group 4+ times in 7 days, (5) session duration > 120 minutes. Returns list of warnings with severity and dismissal state.

**`BaselinePlanGenerator`** — Specified implementation: (a) look up user experience level and body weight, (b) apply BW ratio tables from `exercise-science.md` Sections 4.1-4.3, (c) calculate working weights with proper rounding (Section 8.7), (d) generate warm-up sets per protocol (Section 8.5), (e) apply gender fallback (male ratios - 15% when gender unknown), (f) apply age modifiers (Section 8.6). Returns a `GeneratedPlan`.

**`CrossGroupOverlapDetector`** — Analyzes selected exercise groups against the Cross-Group Activation Map (`exercise-science.md` Section 2.2). Detects when overlapping groups are selected (e.g., Chest + Arms shares triceps). Returns overlap descriptions for inclusion in AI prompt's CROSS-GROUP FATIGUE block.

**`CompleteOnboardingUseCase`** — Orchestrates onboarding completion: (a) creates the singleton `UserProfileEntity` row with experience level, preferred unit, and optional profile fields, (b) sets an `onboarding_completed` flag in SharedPreferences (not Room — must be readable before database opens to determine start destination). The navigation host in `:app` reads this flag on cold start to decide whether to route to the onboarding flow or the main app.

### 4.9 DUP Day-Type Cycling and Block Periodization State

**DUP day-type cycling (intermediate users):** `DetermineSessionDayTypeUseCase` examines the last N completed sessions for the selected muscle groups, identifies the most recent day type used (hypertrophy / strength / power), and returns the next day type in rotation. Cycling order: hypertrophy -> strength -> power -> hypertrophy. If no history exists, defaults to hypertrophy. Result feeds into `PlanRequest.periodizationModel` and the day-type-specific set/rep ranges.

**Block periodization (advanced users):**

For advanced users, block phase is computed heuristically from training history patterns rather than requiring explicit user tracking. The algorithm examines the last 4-6 weeks of session data: average intensity, volume trends, and rep ranges to infer the current phase (Accumulation, Intensification, Realization, or Deload). If training history is insufficient (fewer than 8 sessions), the phase defaults to Accumulation.

The computed phase and inferred week number within that phase are included in `PlanRequest.currentBlockPhase` and `PlanRequest.currentBlockWeek`. No separate `training_blocks` table is needed for MVP -- the phase is derived, not stored.

### 4.10 AnalyticsTracker Interface

The `AnalyticsTracker` interface lives in `:core:domain`. It defines event categories without coupling to any analytics SDK.

```kotlin
// :core:domain
interface AnalyticsTracker {
    /** Screen view events. Called from Composable (via LaunchedEffect on screen entry). */
    fun trackScreenView(screenName: String, screenClass: String? = null)

    /** User action events. Called from ViewModel when processing intents. */
    fun trackUserAction(action: String, params: Map<String, Any> = emptyMap())

    /** System-detected events. Called from use cases (e.g., deload triggered, PR detected). */
    fun trackSystemEvent(event: String, params: Map<String, Any> = emptyMap())

    /** Lifecycle events. Called from Application class or Activity. */
    fun trackLifecycleEvent(event: String, params: Map<String, Any> = emptyMap())
}
```

**Implementation:** `FirebaseAnalyticsTracker` in `:core:data` wraps `FirebaseAnalytics`. It is the only class that imports Firebase Analytics SDK classes.

**Placement rules (non-negotiable):**
- Screen views: from `@Composable` functions via `LaunchedEffect` on first composition.
- User actions: from ViewModel `onIntent()` handlers. Never from Composables directly.
- System events: from domain use cases (e.g., `DetectDeloadNeedUseCase` fires "deload_recommended").
- Lifecycle events: from `DeepRepsApplication.onCreate()` or `MainActivity` lifecycle callbacks.

`:core:domain` MUST NOT depend on Firebase. The `AnalyticsTracker` interface is pure Kotlin. Hilt binds `FirebaseAnalyticsTracker` to `AnalyticsTracker` in a `@Module` in `:core:data`.

### 4.11 Analytics Consent and ConsentManager

Analytics and crash reporting require explicit user consent. Consent state is managed by `ConsentManager` in `:core:data`.

**Why EncryptedSharedPreferences, not Room:** Consent state must be readable before the Room database is opened. Firebase Analytics collection must be disabled on `Application.onCreate()` before any analytics event fires. Room may not be initialized at that point. EncryptedSharedPreferences is available immediately.

```kotlin
// :core:data
class ConsentManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "consent_prefs",
            MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    var analyticsConsent: Boolean
        get() = prefs.getBoolean(KEY_ANALYTICS_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_ANALYTICS_CONSENT, value).apply()

    var crashlyticsConsent: Boolean
        get() = prefs.getBoolean(KEY_CRASHLYTICS_CONSENT, false)
        set(value) = prefs.edit().putBoolean(KEY_CRASHLYTICS_CONSENT, value).apply()

    val hasUserRespondedToConsent: Boolean
        get() = prefs.getBoolean(KEY_CONSENT_RESPONDED, false)

    fun markConsentResponded() =
        prefs.edit().putBoolean(KEY_CONSENT_RESPONDED, true).apply()

    companion object {
        private const val KEY_ANALYTICS_CONSENT = "analytics_consent"
        private const val KEY_CRASHLYTICS_CONSENT = "crashlytics_consent"
        private const val KEY_CONSENT_RESPONDED = "consent_responded"
    }
}
```

**Firebase initialization sequence in `DeepRepsApplication.onCreate()`:**

1. `FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)` -- collection disabled by default.
2. `FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)` -- same for Crashlytics.
3. Read `ConsentManager.analyticsConsent` and `ConsentManager.crashlyticsConsent`.
4. If consent was previously granted, enable collection: `setAnalyticsCollectionEnabled(true)`.
5. If `hasUserRespondedToConsent` is false, the onboarding flow presents the consent screen as **Screen 0** (before experience level selection). The user must respond before proceeding.

**Onboarding flow with consent (updated order):**
1. Screen 0: Privacy / consent (analytics + crashlytics toggles, brief explanation, "Continue" button)
2. Screen 1: Experience level selection
3. Screen 2: Unit preference (kg / lbs)
4. Screen 3: Optional profile fields (age, height, gender)

### 4.12 FeatureFlagProvider

The `FeatureFlagProvider` interface lives in `:core:domain`. Feature flags control runtime behavior without code changes or app updates.

```kotlin
// :core:domain
interface FeatureFlagProvider {
    /** Current snapshot of all feature flags. Immutable. */
    val flags: StateFlow<FeatureFlags>

    /** Fetches latest flags from remote config. Call on app start and periodically. */
    suspend fun refresh()
}

data class FeatureFlags(
    val isAiPlanEnabled: Boolean = true,
    val isBlockPeriodizationEnabled: Boolean = false,
    val maxExercisesPerSession: Int = 12,
    // Add flags as needed. Defaults are the fallback when remote config is unavailable.
)
```

**Implementation:** `FirebaseFeatureFlagProvider` in `:core:data` wraps Firebase Remote Config (`firebase-config-ktx`). On `refresh()`, it fetches remote values and emits a new immutable `FeatureFlags` instance. The `flags` StateFlow always holds the latest snapshot. If fetch fails (offline), the previous cached values or compile-time defaults are used.

### 4.13 WeightStepProvider

Utility in `:core:domain` that returns the appropriate weight increment based on equipment type.

```kotlin
// :core:domain
object WeightStepProvider {
    /**
     * Returns the minimum weight increment for the given equipment type.
     * Cable and machine exercises use larger increments (weight stacks).
     * All other equipment uses standard plate increments.
     */
    fun getIncrement(equipment: String, unit: String): Double = when {
        equipment in setOf("cable", "machine") -> if (unit == "kg") 5.0 else 10.0
        else -> if (unit == "kg") 2.5 else 5.0
    }
}
```

This is passed to the `NumberInput` composable in `:core:ui` to control the +/- step buttons. The AI prompt builder also uses these increments for weight rounding (Section 4.3, safety constraint 8).

### 4.14 Exercise Library Migration Strategy

The exercise library ships as a pre-populated `.db` file. Updates are delivered via Room migrations:

- **Exercise additions:** Standard Room migration that INSERTs new rows by `stableId`.
- **Metadata updates:** Migration UPDATEs specific rows by `stableId` (corrected tips, secondary muscles, etc.).
- **Exercise removal:** NEVER delete. Add `@ColumnInfo(name = "deprecated") val deprecated: Boolean = false` to `ExerciseEntity`. Deprecated exercises do not appear in the exercise picker but remain valid in historical workout data (preserves foreign key integrity).
- **Version tracking:** An `exercise_library_version` constant in `:core:data` is checked at database open. If the DB version is behind, the migration runs.

---

## 5. State Management

### 5.1 Active Workout State Machine

The workout is modeled as an explicit state machine. The states are:

```
                    +-----------+
                    |   Idle    | (no active workout)
                    +-----+-----+
                          |
                    user selects exercises
                          |
                    +-----v-----+
                    |   Setup   | (exercises selected, ready for plan)
                    +-----+-----+
                          |
                    tap "Generate Plan"
                          |
                +--------v---------+
                | GeneratingPlan   | (AI call in flight)
                +--------+---------+
                         |
               +---------+---------+
               |                   |
          plan received       plan failed (fallback)
               |                   |
               +--------+----------+
                        |
                  +-----v-----+
                  |   Active  | (workout in progress, timer running)
                  +-----+-----+
                        |
              +---------+---------+
              |                   |
         user pauses         user finishes
              |                   |
        +-----v-----+     +------v------+
        |   Paused  |     |  Completed  |
        +-----+-----+     +-------------+
              |
         user resumes
              |
        +-----v-----+
        |   Active  |
        +-----------+
```

These states are modeled as a sealed interface:

```kotlin
sealed interface WorkoutPhase {
    data object Idle : WorkoutPhase
    data class Setup(val selectedExercises: List<ExerciseSelection>) : WorkoutPhase
    data object GeneratingPlan : WorkoutPhase
    data class Active(val startedAtMillis: Long) : WorkoutPhase
    data class Paused(val pausedAtMillis: Long, val accumulatedSeconds: Long) : WorkoutPhase
    data class Completed(val sessionId: Long) : WorkoutPhase
}
```

**`WorkoutStateMachine`** -- A pure domain class in `:core:domain` that encapsulates all valid state transitions. The ViewModel delegates transition logic to this class rather than implementing it inline.

```kotlin
// :core:domain
class WorkoutStateMachine {
    /**
     * Returns the next phase if the transition is valid, or null if the intent
     * is not allowed from the current phase. The ViewModel checks the return:
     * null means the intent is silently ignored (invalid transition).
     */
    fun transition(currentPhase: WorkoutPhase, intent: WorkoutIntent): WorkoutPhase? =
        when (currentPhase) {
            is WorkoutPhase.Idle -> when (intent) {
                is WorkoutIntent.SelectExercises -> WorkoutPhase.Setup(intent.exercises)
                else -> null
            }
            is WorkoutPhase.Setup -> when (intent) {
                is WorkoutIntent.GeneratePlan -> WorkoutPhase.GeneratingPlan
                else -> null
            }
            is WorkoutPhase.GeneratingPlan -> when (intent) {
                is WorkoutIntent.PlanReceived -> WorkoutPhase.Active(startedAtMillis = System.currentTimeMillis())
                is WorkoutIntent.PlanFailed -> WorkoutPhase.Active(startedAtMillis = System.currentTimeMillis()) // fallback path
                else -> null
            }
            is WorkoutPhase.Active -> when (intent) {
                is WorkoutIntent.PauseWorkout -> WorkoutPhase.Paused(
                    pausedAtMillis = System.currentTimeMillis(),
                    accumulatedSeconds = currentPhase.startedAtMillis,
                )
                is WorkoutIntent.FinishWorkout -> WorkoutPhase.Completed(sessionId = intent.sessionId)
                else -> null // CompleteSet, StartRestTimer etc. do not change the phase
            }
            is WorkoutPhase.Paused -> when (intent) {
                is WorkoutIntent.ResumeWorkout -> WorkoutPhase.Active(startedAtMillis = currentPhase.accumulatedSeconds)
                else -> null
            }
            is WorkoutPhase.Completed -> null // terminal state, no transitions out
        }
}
```

The `WorkoutStateMachine` is a pure Kotlin class with zero Android dependencies, fully unit-testable without Robolectric. The ViewModel calls `stateMachine.transition(currentPhase, intent)` and only proceeds if the result is non-null.

### 5.2 Surviving Process Death

Android will kill the app's process when it is backgrounded and memory is low. This is not a crash -- it is expected behavior. The workout state MUST survive.

**Strategy: Database as truth, SavedStateHandle as pointer.**

1. The active workout session exists in Room from the moment the user starts it. Every set completion writes immediately to the database (Section 3.7).
2. The `SavedStateHandle` in the ViewModel stores only the `sessionId: Long` of the active workout. This survives process death.
3. On ViewModel initialization, if `savedStateHandle.get<Long>("sessionId")` is non-null, the ViewModel loads the session from Room and reconstructs UI state.
4. Transient UI state (e.g., which set row has focus, whether the rest timer animation is playing) is not saved. It is acceptable to lose this on process death -- the user resumes from a consistent data state.

```kotlin
@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val generatePlanUseCase: GeneratePlanUseCase,
    private val restTimerManager: RestTimerManager,
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state.asStateFlow()

    init {
        val savedSessionId = savedStateHandle.get<Long>(KEY_SESSION_ID)
        if (savedSessionId != null) {
            restoreSession(savedSessionId)
        } else {
            checkForAbandonedSession()
        }
    }

    private fun restoreSession(sessionId: Long) {
        viewModelScope.launch {
            workoutSessionRepository.observeSessionById(sessionId)
                .collect { session ->
                    if (session != null) {
                        _state.update { it.copy(phase = WorkoutPhase.Active(session.startedAt)) }
                        // rebuild exercise/set state from session
                    }
                }
        }
    }

    private fun checkForAbandonedSession() {
        viewModelScope.launch {
            val active = workoutSessionRepository.observeActiveSession().first()
            if (active != null) {
                _state.update {
                    it.copy(showResumeDialog = true, abandonedSessionId = active.id)
                }
            }
        }
    }

    fun onIntent(intent: WorkoutIntent) {
        when (intent) {
            is WorkoutIntent.CompleteSet -> {
                viewModelScope.launch {
                    workoutSessionRepository.completeSet(
                        intent.workoutExerciseId,
                        intent.setIndex,
                        intent.weight,
                        intent.reps,
                    )
                    // State updates reactively via Flow observation of Room
                }
            }
            // ... other intents
        }
    }

    companion object {
        private const val KEY_SESSION_ID = "active_session_id"
    }
}
```

### 5.3 Background Timer Management

Rest timers and workout duration must work when the app is backgrounded. Android aggressively kills background processes and throttles AlarmManager in Doze mode.

**Solution: Foreground Service with notification.**

When a workout is active, a `WorkoutForegroundService` runs with a persistent notification showing:
- Elapsed workout time
- Current rest timer countdown (if active)
- Quick actions: Pause, Resume, End Workout

```kotlin
class WorkoutForegroundService : Service() {

    @Inject lateinit var restTimerManager: RestTimerManager
    @Inject lateinit var notificationHelper: WorkoutNotificationHelper

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var elapsedTicker: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                startForeground(NOTIFICATION_ID, notificationHelper.buildOngoing(0L, null))
                startElapsedTicker()
            }
            ACTION_START_REST_TIMER -> {
                val durationSeconds = intent.getIntExtra(EXTRA_DURATION, 90)
                restTimerManager.start(durationSeconds)
            }
            ACTION_SKIP_REST_TIMER -> {
                restTimerManager.cancel()
            }
            ACTION_STOP -> {
                stopElapsedTicker()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY // restart service if killed
    }

    private fun startElapsedTicker() {
        elapsedTicker = coroutineScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            while (isActive) {
                val elapsed = (SystemClock.elapsedRealtime() - startTime) / 1000
                notificationHelper.updateElapsed(elapsed, restTimerManager.remainingSeconds.value)
                delay(1000L)
            }
        }
    }

    // ...
}
```

**Why a Foreground Service and not WorkManager or AlarmManager:**

- **WorkManager** is for deferrable background work. A rest timer is real-time, not deferrable. WorkManager can delay execution by minutes.
- **AlarmManager** with exact alarms requires the `SCHEDULE_EXACT_ALARM` permission (Android 12+) and is still subject to Doze mode restrictions. A foreground service with a notification is the only reliable way to keep a timer ticking.
- **Foreground Service** keeps the process alive and exempts it from Doze throttling. The persistent notification is actually desirable UX -- the user expects to see their workout timer in the notification shade.

The `RestTimerManager` is a singleton scoped to the application:

```kotlin
@Singleton
class RestTimerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val _remainingSeconds = MutableStateFlow<Int?>(null)
    val remainingSeconds: StateFlow<Int?> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start(durationSeconds: Int) {
        cancel()
        _remainingSeconds.value = durationSeconds
        _isRunning.value = true
        timerJob = scope.launch {
            var remaining = durationSeconds
            while (remaining > 0 && isActive) {
                delay(1000L)
                remaining--
                _remainingSeconds.value = remaining
            }
            if (remaining == 0) {
                _isRunning.value = false
                _remainingSeconds.value = null
                triggerTimerComplete()
            }
        }
    }

    fun cancel() {
        timerJob?.cancel()
        _remainingSeconds.value = null
        _isRunning.value = false
    }

    private fun triggerTimerComplete() {
        // Vibrate and play sound
        val vibrator = context.getSystemService<Vibrator>()
        vibrator?.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
```

### 5.4 Rest Timer Priority Chain

When a set is completed, the rest timer duration is determined by the following priority chain (first match wins):

1. **AI plan's `rest_seconds`** -- The per-exercise rest time from `ExercisePlan.restSeconds` in the generated plan. This is the AI's recommendation based on the user's training context.
2. **User per-exercise override** -- If the user manually edited the rest timer for this exercise during the session (stored in `WorkoutExerciseEntity.restTimerSeconds`). Overrides the AI recommendation for the remainder of the session.
3. **Global default from settings** -- The user's default rest timer preference from `UserProfile` settings (e.g., "always rest 90 seconds"). Used when no AI plan or per-exercise override exists.
4. **CSCS baseline** -- Experience-level and exercise-type based defaults from `exercise-science.md` Section 3. Compound exercises get longer rest (beginner: 90s, intermediate: 120s, advanced: 180s). Isolation exercises get shorter rest (beginner: 60s, intermediate: 75s, advanced: 90s). This is the fallback of last resort.

The resolution logic lives in a `ResolveRestTimerUseCase` in `:core:domain`. The ViewModel calls this after each set completion to determine the next rest timer duration.

### 5.5 Elapsed Time Accuracy

The workout elapsed timer uses `SystemClock.elapsedRealtime()` (time since boot, including sleep), NOT `System.currentTimeMillis()` (wall clock, subject to user changes and NTP adjustments). The session start time is stored as `System.currentTimeMillis()` in the database for display purposes, but duration calculations use `elapsedRealtime` deltas.

---

## 6. Performance Requirements

### 6.1 Targets

| Metric | Target | Measurement Method |
|--------|--------|--------------------|
| Cold startup | < 1.5s to first frame | Baseline Profile + Macrobenchmark |
| Warm startup | < 500ms to first frame | Macrobenchmark |
| Frame rate (scrolling) | 60fps, < 3 janky frames per scroll | Compose Metrics + JankStats |
| Frame rate (animations) | 60fps | JankStats |
| Database queries (common) | < 50ms | Room query logging + custom tracing |
| Database queries (complex/charts) | < 200ms | Room query logging |
| Memory (mid-range device) | < 150MB peak | Android Profiler / LeakCanary |
| APK size (release) | < 25MB | Gradle artifact size reporting |
| Install size | < 40MB | Play Console metrics |

### 6.2 Startup Optimization

1. **Baseline Profiles.** Generated via Macrobenchmark. Covers the critical startup path: app launch, exercise library load, workout screen render. Baseline Profiles enable AOT compilation of hot paths, eliminating JIT overhead on first launch.
2. **Lazy initialization.** Hilt modules use `@Provides` with lazy injection where possible. The Room database is not opened until the first query. Ktor client is not initialized until the first network call.
3. **App Startup library.** Use `androidx.startup` for any initializers (Timber, Coil). Remove any initializer that is not needed on every cold start.
4. **Splash screen.** Use the `SplashScreen` compat library. The splash screen shows the app icon while the first Compose frame renders. No custom splash screen activity.

### 6.3 Compose Performance

1. **Stability.** All UI state classes are `data class` with immutable properties. No `var`, no `MutableList`, no `Array`. Compose compiler marks these as stable, enabling recomposition skipping.
2. **`@Immutable` and `@Stable` annotations** on domain-to-UI mapper output classes where the compiler cannot infer stability (e.g., classes from other modules).
3. **Key-based LazyColumn.** Every `LazyColumn` uses `key = { item.id }` to avoid unnecessary recompositions when the list changes.
4. **`derivedStateOf`** for computed values that depend on rapidly changing state (e.g., formatting elapsed time from a Long counter).
5. **Lambda stabilization.** Event handler lambdas in `LazyColumn` items use `remember` with the item key to avoid recomposition of unchanged items. Alternatively, pass a method reference from the ViewModel.
6. **Compose Compiler Reports** are generated in CI to detect stability regressions. Any `UNSTABLE` class in the workout screen's UI model is a build failure.

### 6.4 Memory Management

- **No bitmap caching of exercise diagrams in memory.** Coil handles caching with a disk cache. In-memory cache is bounded to 25MB.
- **LazyColumn for all lists.** No `Column` with `forEach` for lists longer than 10 items.
- **LeakCanary** in debug builds. Any leak in the workout flow is a P0 bug.
- **ViewModel coroutines are scoped to `viewModelScope`.** No global coroutine scopes except the foreground service scope and the `RestTimerManager` scope, both of which have explicit lifecycle management.

---

## 7. Security Considerations

### 7.1 API Key Storage (Gemini)

The Gemini API key is a sensitive credential. It MUST NOT be:
- Hardcoded in source code
- Committed to version control
- Stored in `BuildConfig` fields generated from `local.properties` (these end up in the APK and are trivially extractable)

**Strategy:**

**API key environment variable convention:**
- Debug builds: `GEMINI_DEV_API_KEY` environment variable (or `gemini.dev.api.key` in `local.properties` as fallback for local development).
- Release builds: `GEMINI_PROD_API_KEY` environment variable (CI secret, never in `local.properties`).
- The Gradle build script reads the appropriate variable per build type and injects it as `BuildConfig.GEMINI_API_KEY`. This field is baked into the APK at compile time.
- `local.properties` is the fallback for local development only. It is gitignored and never present in CI.

For MVP / pre-revenue phase:
- Store the API key in `local.properties` (gitignored), injected via `BuildConfig` at build time. This is NOT secure against reverse engineering, but is acceptable for a pre-launch MVP where the attack surface is negligible.
- R8 obfuscation provides minimal protection.

For production / post-launch:
- **Backend proxy.** The mobile app calls our own backend endpoint, which holds the Gemini API key server-side and proxies the request. The mobile app authenticates to our backend via Firebase Auth or similar. The Gemini key never touches the device.
- This is the only architecturally sound approach. Any on-device key storage (EncryptedSharedPreferences, NDK obfuscation, etc.) is security theater against a determined attacker.

The `AiPlanProvider` interface is already designed to support this transition. The `GeminiPlanProvider` changes its URL from Gemini direct to our proxy endpoint. No domain or presentation changes.

```kotlin
// MVP: Direct Gemini call
// url("https://generativelanguage.googleapis.com/")

// Production: Backend proxy
// url("https://api.deepreps.com/")
```

### 7.2 Data Encryption at Rest

- **Room database encryption is NOT used at MVP.** SQLCipher adds ~5MB to APK size and measurable query overhead. The data stored (workout logs, body weight, exercise names) is not high-sensitivity PII.
- **If the product adds cloud sync or stores health data regulated by HIPAA/GDPR**, we revisit this decision. SQLCipher (via `net.zetetic:android-database-sqlcipher`) can be added without schema changes.
- **SharedPreferences** for non-sensitive settings use standard `SharedPreferences`. For any sensitive stored token (future auth tokens), use `EncryptedSharedPreferences` from `androidx.security.crypto`.

### 7.3 ProGuard / R8 Configuration

R8 is enabled for release builds. Configuration:

```proguard
# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all @Serializable data classes
-keep,includedescriptorclasses class com.deepreps.**$$serializer { *; }
-keepclassmembers class com.deepreps.** {
    *** Companion;
}
-keepclasseswithmembers class com.deepreps.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Ktor
-keep class io.ktor.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }

# Standard Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
```

### 7.4 Network Security Configuration

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <!-- Production: HTTPS only, no cleartext -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Debug: Allow cleartext for local development server -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

### 7.5 Input Validation

- All user-entered numeric values (weight, reps) are validated at the UI layer (Compose) and again at the repository layer before database writes.
- Weight range: 0-999 kg / 0-2200 lbs.
- Reps range: 0-999.
- Free-text notes: max 1000 characters, no HTML allowed.
- Template names: max 100 characters, trimmed.

---

## 8. Project Structure

### 8.1 Full Directory Tree

```
deep-reps/
|-- app/
|   |-- src/
|   |   |-- main/
|   |   |   |-- java/com/deepreps/app/
|   |   |   |   |-- DeepRepsApplication.kt
|   |   |   |   |-- MainActivity.kt
|   |   |   |   |-- navigation/
|   |   |   |   |   |-- DeepRepsNavHost.kt
|   |   |   |   |   |-- TopLevelDestination.kt
|   |   |   |   |-- di/
|   |   |   |   |   |-- AppModule.kt
|   |   |   |   |-- service/
|   |   |   |   |   |-- WorkoutForegroundService.kt
|   |   |   |-- res/
|   |   |   |   |-- xml/
|   |   |   |   |   |-- network_security_config.xml
|   |   |   |-- AndroidManifest.xml
|   |   |-- androidTest/
|   |   |-- test/
|   |-- build.gradle.kts
|
|-- feature/
|   |-- workout/
|   |   |-- src/main/java/com/deepreps/feature/workout/
|   |   |   |-- WorkoutScreen.kt
|   |   |   |-- WorkoutViewModel.kt
|   |   |   |-- WorkoutUiState.kt
|   |   |   |-- WorkoutIntent.kt
|   |   |   |-- WorkoutSideEffect.kt
|   |   |   |-- components/
|   |   |   |   |-- ExerciseCard.kt
|   |   |   |   |-- SetRow.kt
|   |   |   |   |-- RestTimerDisplay.kt
|   |   |   |   |-- SupersetGroup.kt
|   |   |   |   |-- WorkoutSummarySheet.kt
|   |   |   |-- mapper/
|   |   |   |   |-- WorkoutUiMapper.kt
|   |   |-- src/test/
|   |   |   |-- WorkoutViewModelTest.kt
|   |   |   |-- WorkoutUiMapperTest.kt
|   |   |-- src/androidTest/
|   |   |   |-- WorkoutScreenTest.kt
|   |   |-- build.gradle.kts
|   |
|   |-- exercise-library/
|   |   |-- src/main/java/com/deepreps/feature/exerciselibrary/
|   |   |   |-- ExerciseListScreen.kt
|   |   |   |-- ExerciseListViewModel.kt
|   |   |   |-- ExerciseDetailScreen.kt
|   |   |   |-- ExerciseDetailViewModel.kt
|   |   |   |-- ExerciseSelectionScreen.kt
|   |   |   |-- ExerciseSelectionViewModel.kt
|   |   |   |-- components/
|   |   |   |   |-- ExerciseListItem.kt
|   |   |   |   |-- MuscleGroupChip.kt
|   |   |   |   |-- AnatomyDiagram.kt
|   |   |   |-- mapper/
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|   |
|   |-- progress/
|   |   |-- src/main/java/com/deepreps/feature/progress/
|   |   |   |-- ProgressDashboardScreen.kt
|   |   |   |-- ProgressDashboardViewModel.kt
|   |   |   |-- ExerciseProgressScreen.kt
|   |   |   |-- ExerciseProgressViewModel.kt
|   |   |   |-- components/
|   |   |   |   |-- ProgressChart.kt
|   |   |   |   |-- PersonalRecordCard.kt
|   |   |   |   |-- TimeRangeSelector.kt
|   |   |   |   |-- VolumeChart.kt
|   |   |   |-- mapper/
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|   |
|   |-- profile/
|   |   |-- src/main/java/com/deepreps/feature/profile/
|   |   |   |-- ProfileScreen.kt
|   |   |   |-- ProfileViewModel.kt
|   |   |   |-- SettingsScreen.kt
|   |   |   |-- SettingsViewModel.kt
|   |   |   |-- components/
|   |   |   |   |-- BodyWeightEntryRow.kt
|   |   |   |   |-- UnitSelector.kt
|   |   |   |-- mapper/
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|   |
|   |-- ai-plan/
|   |   |-- src/main/java/com/deepreps/feature/aiplan/
|   |   |   |-- PlanReviewScreen.kt
|   |   |   |-- PlanReviewViewModel.kt
|   |   |   |-- PlanGenerationUiState.kt
|   |   |   |-- components/
|   |   |   |   |-- PlanExerciseCard.kt
|   |   |   |   |-- PlanSetRow.kt
|   |   |   |   |-- FallbackIndicator.kt
|   |   |   |-- mapper/
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|   |
|   |-- templates/
|   |   |-- src/main/java/com/deepreps/feature/templates/
|   |   |   |-- TemplateListScreen.kt
|   |   |   |-- TemplateListViewModel.kt
|   |   |   |-- CreateTemplateScreen.kt
|   |   |   |-- CreateTemplateViewModel.kt
|   |   |   |-- components/
|   |   |   |   |-- TemplateCard.kt
|   |   |   |-- mapper/
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|   |
|   |-- onboarding/
|   |   |-- src/main/java/com/deepreps/feature/onboarding/
|   |   |   |-- OnboardingScreen.kt
|   |   |   |-- OnboardingViewModel.kt
|   |   |   |-- ConsentScreen.kt
|   |   |   |-- components/
|   |   |   |   |-- ConsentToggleRow.kt
|   |   |   |   |-- ExperienceLevelSelector.kt
|   |   |   |   |-- UnitPreferenceSelector.kt
|   |   |-- src/test/
|   |   |-- src/androidTest/
|   |   |-- build.gradle.kts
|
|-- core/
|   |-- domain/
|   |   |-- src/main/java/com/deepreps/core/domain/
|   |   |   |-- model/
|   |   |   |   |-- MuscleGroup.kt
|   |   |   |   |-- Exercise.kt
|   |   |   |   |-- WorkoutSession.kt
|   |   |   |   |-- WorkoutExercise.kt
|   |   |   |   |-- WorkoutSet.kt
|   |   |   |   |-- Template.kt
|   |   |   |   |-- UserProfile.kt
|   |   |   |   |-- PersonalRecord.kt
|   |   |   |   |-- BodyWeightEntry.kt
|   |   |   |   |-- GeneratedPlan.kt
|   |   |   |   |-- ExperienceLevel.kt
|   |   |   |   |-- WeightUnit.kt
|   |   |   |   |-- SetType.kt
|   |   |   |   |-- Equipment.kt
|   |   |   |   |-- IsolationLevel.kt
|   |   |   |-- repository/
|   |   |   |   |-- ExerciseRepository.kt
|   |   |   |   |-- WorkoutSessionRepository.kt
|   |   |   |   |-- TemplateRepository.kt
|   |   |   |   |-- UserProfileRepository.kt
|   |   |   |   |-- PersonalRecordRepository.kt
|   |   |   |   |-- BodyWeightRepository.kt
|   |   |   |   |-- CachedPlanRepository.kt
|   |   |   |-- usecase/
|   |   |   |   |-- GeneratePlanUseCase.kt
|   |   |   |   |-- CompleteSetUseCase.kt
|   |   |   |   |-- CompleteOnboardingUseCase.kt
|   |   |   |   |-- CalculatePersonalRecordsUseCase.kt
|   |   |   |   |-- GetExerciseProgressUseCase.kt
|   |   |   |   |-- GetMuscleGroupVolumeUseCase.kt
|   |   |   |   |-- CalculateEstimated1rmUseCase.kt
|   |   |   |   |-- SaveTemplateUseCase.kt
|   |   |   |   |-- GetWorkoutSummaryUseCase.kt
|   |   |   |   |-- DetermineSessionDayTypeUseCase.kt
|   |   |   |   |-- ResolveRestTimerUseCase.kt
|   |   |   |-- provider/
|   |   |   |   |-- AiPlanProvider.kt
|   |   |   |   |-- AnalyticsTracker.kt
|   |   |   |   |-- FeatureFlagProvider.kt
|   |   |   |   |-- ConnectivityChecker.kt
|   |   |   |   |-- BaselinePlanGenerator.kt
|   |   |   |-- statemachine/
|   |   |   |   |-- WorkoutStateMachine.kt
|   |   |   |-- util/
|   |   |   |   |-- Estimated1rmCalculator.kt
|   |   |   |   |-- VolumeCalculator.kt
|   |   |   |   |-- WeightStepProvider.kt
|   |   |-- src/test/
|   |   |   |-- usecase/
|   |   |   |   |-- GeneratePlanUseCaseTest.kt
|   |   |   |   |-- CompleteSetUseCaseTest.kt
|   |   |   |   |-- CalculatePersonalRecordsUseCaseTest.kt
|   |   |   |-- util/
|   |   |   |   |-- Estimated1rmCalculatorTest.kt
|   |   |-- build.gradle.kts
|   |
|   |-- data/
|   |   |-- src/main/java/com/deepreps/core/data/
|   |   |   |-- repository/
|   |   |   |   |-- ExerciseRepositoryImpl.kt
|   |   |   |   |-- WorkoutSessionRepositoryImpl.kt
|   |   |   |   |-- TemplateRepositoryImpl.kt
|   |   |   |   |-- UserProfileRepositoryImpl.kt
|   |   |   |   |-- PersonalRecordRepositoryImpl.kt
|   |   |   |   |-- BodyWeightRepositoryImpl.kt
|   |   |   |   |-- CachedPlanRepositoryImpl.kt
|   |   |   |-- analytics/
|   |   |   |   |-- FirebaseAnalyticsTracker.kt
|   |   |   |-- consent/
|   |   |   |   |-- ConsentManager.kt
|   |   |   |-- featureflag/
|   |   |   |   |-- FirebaseFeatureFlagProvider.kt
|   |   |   |-- mapper/
|   |   |   |   |-- ExerciseMapper.kt
|   |   |   |   |-- WorkoutSessionMapper.kt
|   |   |   |   |-- TemplateMapper.kt
|   |   |   |   |-- UserProfileMapper.kt
|   |   |   |-- di/
|   |   |   |   |-- DataModule.kt
|   |   |   |   |-- RepositoryModule.kt
|   |   |-- src/test/
|   |   |   |-- repository/
|   |   |   |   |-- WorkoutSessionRepositoryImplTest.kt
|   |   |-- build.gradle.kts
|   |
|   |-- database/
|   |   |-- src/main/java/com/deepreps/core/database/
|   |   |   |-- DeepRepsDatabase.kt
|   |   |   |-- entity/
|   |   |   |   |-- MuscleGroupEntity.kt
|   |   |   |   |-- ExerciseEntity.kt
|   |   |   |   |-- ExerciseMuscleEntity.kt
|   |   |   |   |-- WorkoutSessionEntity.kt
|   |   |   |   |-- WorkoutExerciseEntity.kt
|   |   |   |   |-- WorkoutSetEntity.kt
|   |   |   |   |-- TemplateEntity.kt
|   |   |   |   |-- TemplateExerciseEntity.kt
|   |   |   |   |-- UserProfileEntity.kt
|   |   |   |   |-- BodyWeightEntryEntity.kt
|   |   |   |   |-- PersonalRecordEntity.kt
|   |   |   |   |-- CachedAiPlanEntity.kt
|   |   |   |-- dao/
|   |   |   |   |-- MuscleGroupDao.kt
|   |   |   |   |-- ExerciseDao.kt
|   |   |   |   |-- WorkoutSessionDao.kt
|   |   |   |   |-- WorkoutExerciseDao.kt
|   |   |   |   |-- WorkoutSetDao.kt
|   |   |   |   |-- TemplateDao.kt
|   |   |   |   |-- UserProfileDao.kt
|   |   |   |   |-- BodyWeightDao.kt
|   |   |   |   |-- PersonalRecordDao.kt
|   |   |   |   |-- CachedAiPlanDao.kt
|   |   |   |-- converter/
|   |   |   |   |-- Converters.kt
|   |   |   |-- migration/
|   |   |   |   |-- Migrations.kt
|   |   |   |-- di/
|   |   |   |   |-- DatabaseModule.kt
|   |   |-- src/main/assets/
|   |   |   |-- deep_reps.db  (pre-populated exercise library)
|   |   |-- src/androidTest/
|   |   |   |-- migration/
|   |   |   |   |-- MigrationTest.kt
|   |   |   |-- dao/
|   |   |   |   |-- WorkoutSessionDaoTest.kt
|   |   |   |   |-- ExerciseDaoTest.kt
|   |   |-- build.gradle.kts
|   |
|   |-- network/
|   |   |-- src/main/java/com/deepreps/core/network/
|   |   |   |-- gemini/
|   |   |   |   |-- GeminiPlanProvider.kt
|   |   |   |   |-- GeminiPromptBuilder.kt
|   |   |   |   |-- GeminiResponseParser.kt
|   |   |   |   |-- model/
|   |   |   |   |   |-- GeminiRequestBody.kt
|   |   |   |   |   |-- GeminiResponse.kt
|   |   |   |   |   |-- GeminiGenerationConfig.kt
|   |   |   |-- connectivity/
|   |   |   |   |-- AndroidConnectivityChecker.kt
|   |   |   |-- di/
|   |   |   |   |-- NetworkModule.kt
|   |   |   |   |-- AiProviderModule.kt
|   |   |-- src/test/
|   |   |   |-- gemini/
|   |   |   |   |-- GeminiPlanProviderTest.kt
|   |   |   |   |-- GeminiPromptBuilderTest.kt
|   |   |   |   |-- GeminiResponseParserTest.kt
|   |   |-- build.gradle.kts
|   |
|   |-- ui/
|   |   |-- src/main/java/com/deepreps/core/ui/
|   |   |   |-- theme/
|   |   |   |   |-- Theme.kt
|   |   |   |   |-- Color.kt
|   |   |   |   |-- Typography.kt
|   |   |   |   |-- Spacing.kt
|   |   |   |-- component/
|   |   |   |   |-- DeepRepsButton.kt
|   |   |   |   |-- DeepRepsTextField.kt
|   |   |   |   |-- DeepRepsCard.kt
|   |   |   |   |-- NumberInput.kt
|   |   |   |   |-- CountdownTimer.kt
|   |   |   |   |-- LoadingIndicator.kt
|   |   |   |   |-- ErrorState.kt
|   |   |   |   |-- EmptyState.kt
|   |   |-- build.gradle.kts
|   |
|   |-- common/
|   |   |-- src/main/java/com/deepreps/core/common/
|   |   |   |-- result/
|   |   |   |   |-- DomainResult.kt
|   |   |   |-- extension/
|   |   |   |   |-- LongExtensions.kt
|   |   |   |   |-- DoubleExtensions.kt
|   |   |   |   |-- FlowExtensions.kt
|   |   |   |-- dispatcher/
|   |   |   |   |-- DispatcherProvider.kt
|   |   |   |   |-- DefaultDispatcherProvider.kt
|   |   |   |-- constant/
|   |   |   |   |-- AppConstants.kt
|   |   |-- src/test/
|   |   |-- build.gradle.kts
|
|-- benchmark/
|   |-- src/androidTest/java/com/deepreps/benchmark/
|   |   |-- StartupBenchmark.kt
|   |   |-- WorkoutScrollBenchmark.kt
|   |-- build.gradle.kts
|
|-- build-logic/
|   |-- convention/
|   |   |-- src/main/kotlin/
|   |   |   |-- AndroidApplicationConventionPlugin.kt
|   |   |   |-- AndroidLibraryConventionPlugin.kt
|   |   |   |-- AndroidComposeConventionPlugin.kt
|   |   |   |-- AndroidHiltConventionPlugin.kt
|   |   |   |-- AndroidFeatureConventionPlugin.kt
|   |   |   |-- AndroidBenchmarkConventionPlugin.kt
|   |   |   |-- DetektConventionPlugin.kt
|   |   |   |-- JvmLibraryConventionPlugin.kt
|   |   |-- build.gradle.kts
|   |-- settings.gradle.kts
|
|-- gradle/
|   |-- libs.versions.toml
|   |-- wrapper/
|       |-- gradle-wrapper.properties
|
|-- docs/
|   |-- architecture.md (this document)
|
|-- .github/
|   |-- workflows/
|       |-- ci.yml
|       |-- release.yml
|
|-- build.gradle.kts (root)
|-- settings.gradle.kts
|-- gradle.properties
|-- .gitignore
|-- CLAUDE.md
|-- FEATURES.md
|-- TEAM.md
|-- README.md
```

### 8.2 Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Package | `com.deepreps.<layer>.<feature>` | `com.deepreps.feature.workout` |
| Screen Composable | `<Feature>Screen` | `WorkoutScreen`, `ProfileScreen` |
| ViewModel | `<Feature>ViewModel` | `WorkoutViewModel` |
| UI State | `<Feature>UiState` | `WorkoutUiState` |
| Intent | `<Feature>Intent` | `WorkoutIntent` |
| Side Effect | `<Feature>SideEffect` | `WorkoutSideEffect` |
| Repository interface | `<Entity>Repository` | `WorkoutSessionRepository` |
| Repository implementation | `<Entity>RepositoryImpl` | `WorkoutSessionRepositoryImpl` |
| Use case | `<Verb><Noun>UseCase` | `CompleteSetUseCase`, `GeneratePlanUseCase` |
| Room Entity | `<Name>Entity` | `WorkoutSessionEntity` |
| DAO | `<Name>Dao` | `WorkoutSessionDao` |
| Domain model | Plain name (no suffix) | `WorkoutSession`, `Exercise` |
| UI model | `<Name>Ui` | `WorkoutExerciseUi`, `SetRowUi` |
| Mapper | `<Source>To<Target>Mapper` or `<Name>Mapper` | `WorkoutUiMapper` |
| Hilt Module | `<Scope>Module` | `DatabaseModule`, `NetworkModule` |
| Compose Component | `DeepReps<Component>` (for shared) or `<Feature><Component>` (for feature-specific) | `DeepRepsButton`, `SetRow` |
| Test class | `<ClassUnderTest>Test` | `WorkoutViewModelTest` |
| Navigation route | `<Feature>Route` | `ActiveWorkoutRoute` |

### 8.3 Code Style Rules

1. **Max file length: 400 lines.** Files exceeding this must be split. Compose screens may reach 500 lines if the screen is genuinely complex (e.g., `WorkoutScreen`), but this requires review approval.
2. **Max function length: 40 lines.** Compose functions may be longer only for layout composition. Business logic within a composable must be extracted.
3. **No nested `if` deeper than 3 levels.** Use `when`, early returns, or extract functions.
4. **All public functions and classes have KDoc.** Internal/private functions have KDoc only when the purpose is non-obvious.
5. **No `!!` (non-null assertion).** Use `requireNotNull()` with a descriptive message, or handle nullability explicitly.
6. **No `var` in data classes or state objects.** All state is immutable. Mutation happens by creating new instances via `copy()`.
7. **Coroutine dispatchers are injected**, not hardcoded. Every repository takes a `DispatcherProvider` interface. This enables testing with `UnconfinedTestDispatcher`.
8. **No string resources in ViewModels.** ViewModels emit semantic UI state (e.g., `ErrorType.NetworkTimeout`). Composables map error types to string resources.
9. **Every PR must pass:** `./gradlew build` (compile + lint + unit tests). CI runs instrumented tests on a separate schedule.
10. **ktlint** enforced in CI. No manual formatting debates.

### 8.4 Version Catalog (libs.versions.toml)

```toml
[versions]
agp = "8.7.0"
kotlin = "2.1.0"
compose-bom = "2025.05.00"
compose-compiler = "2.1.0"
hilt = "2.53"
room = "2.7.0"
ktor = "3.1.0"
coroutines = "1.9.0"
lifecycle = "2.8.0"
navigation = "2.8.0"
coil = "3.1.0"
timber = "5.0.1"
kotlinx-serialization = "1.7.0"
junit5 = "5.11.0"
mockk = "1.13.12"
turbine = "1.2.0"
truth = "1.4.4"
leakcanary = "2.14"
detekt = "1.23.7"
firebase-bom = "33.7.0"

[libraries]
# Compose
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-runtime = { group = "androidx.compose.runtime", name = "runtime" }
compose-foundation = { group = "androidx.compose.foundation", name = "foundation" }
compose-animation = { group = "androidx.compose.animation", name = "animation" }
compose-ui-test = { group = "androidx.compose.ui", name = "ui-test-junit4" }
compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

# Lifecycle
lifecycle-runtime = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
lifecycle-viewmodel = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
lifecycle-savedstate = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-savedstate", version.ref = "lifecycle" }

# Navigation
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }

# Hilt
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version = "1.2.0" }

# Room
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

# Ktor
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
ktor-client-okhttp = { group = "io.ktor", name = "ktor-client-okhttp", version.ref = "ktor" }
ktor-client-content-negotiation = { group = "io.ktor", name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json = { group = "io.ktor", name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { group = "io.ktor", name = "ktor-client-logging", version.ref = "ktor" }
ktor-client-mock = { group = "io.ktor", name = "ktor-client-mock", version.ref = "ktor" }

# Kotlinx
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

# Image
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }

# Logging
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }

# Testing
junit5-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
junit5-params = { group = "org.junit.jupiter", name = "junit-jupiter-params", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
mockk-android = { group = "io.mockk", name = "mockk-android", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
truth = { group = "com.google.truth", name = "truth", version.ref = "truth" }

# Debug
leakcanary = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }

# Firebase (BOM-managed -- individual libraries omit version)
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics-ktx" }
firebase-crashlytics = { group = "com.google.firebase", name = "firebase-crashlytics-ktx" }
firebase-perf = { group = "com.google.firebase", name = "firebase-perf-ktx" }
firebase-config = { group = "com.google.firebase", name = "firebase-config-ktx" }

# Static Analysis
detekt-gradle = { group = "io.gitlab.arturbosch.detekt", name = "detekt-gradle-plugin", version.ref = "detekt" }
detekt-formatting = { group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version.ref = "detekt" }

[bundles]
compose = ["compose-ui", "compose-ui-tooling-preview", "compose-material3", "compose-runtime", "compose-foundation", "compose-animation"]
compose-debug = ["compose-ui-tooling", "compose-ui-test-manifest"]
lifecycle = ["lifecycle-runtime", "lifecycle-viewmodel", "lifecycle-savedstate"]
ktor = ["ktor-client-core", "ktor-client-okhttp", "ktor-client-content-negotiation", "ktor-serialization-json", "ktor-client-logging"]
room = ["room-runtime", "room-ktx"]
testing = ["junit5-api", "junit5-params", "mockk", "turbine", "truth", "kotlinx-coroutines-test"]

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
room = { id = "androidx.room", version.ref = "room" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
firebase-crashlytics-gradle = { id = "com.google.firebase.crashlytics", version = "3.0.3" }
firebase-perf-gradle = { id = "com.google.firebase.firebase-perf", version = "1.4.2" }
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
```

**Firebase dependency placement rules:**
- `:app` applies Firebase Gradle plugins (`google-services`, `firebase-crashlytics-gradle`, `firebase-perf-gradle`).
- `:core:data` declares Firebase SDK library dependencies (`firebase-analytics`, `firebase-crashlytics`, `firebase-config`). This is where `FirebaseAnalyticsTracker`, `FirebaseFeatureFlagProvider`, and `ConsentManager` implementations live.
- `:core:domain` MUST NOT depend on any Firebase artifact. All Firebase access is behind domain interfaces.

**Detekt integration:** The Detekt Gradle plugin is applied via `DetektConventionPlugin` in `build-logic`. It runs as part of the CI `lint` step. Custom rule sets can be added in `config/detekt/detekt.yml`.

---

## Appendix A: CI/CD Pipeline

### GitHub Actions -- CI

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [develop, main]
  pull_request:
    branches: [develop]

concurrency:
  group: ci-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '21'
      - uses: gradle/actions/setup-gradle@v4
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}
      - name: Lint
        run: ./gradlew lintDebug
      - name: Unit Tests
        run: ./gradlew testDebugUnitTest
      - name: Build
        env:
          GEMINI_DEV_API_KEY: ${{ secrets.GEMINI_DEV_API_KEY }}
        run: ./gradlew assembleDebug
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'
```

**Branch strategy note:** PR pipelines trigger on `pull_request: branches: [develop]` only -- all feature branches merge into `develop`. Merge builds trigger on `push: branches: [develop, main]`, covering both integration builds on `develop` and release-candidate validation on `main`. This aligns with the DevOps branching model: `feature/*` -> `develop` -> `main`.

### Release Pipeline

```yaml
# .github/workflows/release.yml
name: Release

on:
  push:
    tags: ['v*']

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '21'
      - uses: gradle/actions/setup-gradle@v4
      - name: Decode keystore
        env:
          KEYSTORE_BASE64: ${{ secrets.KEYSTORE_BASE64 }}
        run: echo "$KEYSTORE_BASE64" | base64 -d > app/release.keystore
      - name: Build Release AAB
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          GEMINI_PROD_API_KEY: ${{ secrets.GEMINI_PROD_API_KEY }}
        run: ./gradlew bundleRelease
      - name: Upload to Play Store (Internal Track)
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
          packageName: com.deepreps.app
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: internal
          status: completed
```

---

## Appendix B: Decision Log

| Decision | Choice | Alternatives Considered | Rationale |
|----------|--------|------------------------|-----------|
| Architecture pattern | MVI | MVVM | Workout state machine needs single-state-object guarantees. MVVM multiple StateFlows risk inconsistent state. |
| DI framework | Hilt | Koin | Compile-time graph validation. A DI failure during an active workout means data loss. |
| Networking | Ktor | Retrofit + OkHttp | Single API endpoint (Gemini). Ktor is lighter, Kotlin-native, and sufficient. |
| Serialization | Kotlinx Serialization | Moshi, Gson | Compiler plugin, no reflection, sealed class support, Ktor native integration. |
| Navigation | Jetpack Compose Navigation | Voyager, Decompose | Google-maintained, type-safe routes, SavedStateHandle integration, deep link support. |
| Database | Room | SQLDelight | Room has better Android tooling, migration testing helpers, and Compose integration. SQLDelight's multiplatform advantage is irrelevant (Android-only app). |
| Timer strategy | Foreground Service | WorkManager, AlarmManager | Real-time countdown requires foreground execution. WorkManager is deferrable. AlarmManager is throttled in Doze. |
| API key (MVP) | BuildConfig injection | EncryptedSharedPreferences, NDK | All on-device approaches are extractable. BuildConfig is simple for MVP. Backend proxy is the production plan. |
| Database encryption | Not at MVP | SQLCipher | Adds 5MB APK size + query overhead. Workout logs are not high-sensitivity PII. Revisit for cloud sync. |

---

## Appendix C: Android Version Targets

| Parameter | Value |
|-----------|-------|
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 35 (Android 15) |
| `compileSdk` | 35 |

**minSdk 26 rationale:** Covers 97%+ of active Android devices. Gives us Java 8 desugaring, `VibrationEffect` API, notification channels, and foreground service types without compat workarounds. Dropping below 26 gains negligible market share and costs significant development time in backward compatibility.
