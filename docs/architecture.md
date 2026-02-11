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
| `:feature:onboarding` | Feature | Experience level selection, unit preference, optional profile fields. |
| `:core:domain` | Library | Use cases, repository interfaces, domain models. Pure Kotlin -- zero Android dependencies. |
| `:core:data` | Library | Repository implementations. Bridges database and network into domain interfaces. |
| `:core:database` | Library | Room database, DAOs, entities, migrations, TypeConverters. |
| `:core:network` | Library | Ktor client setup, AI provider interface, Gemini implementation, request/response DTOs. |
| `:core:ui` | Library | Shared Compose components (buttons, cards, input fields, timer display), theme definition, design tokens. |
| `:core:common` | Library | Extension functions, constants, date/time utilities, result wrappers. Pure Kotlin. |

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

+---------------------+     +------------------------+     +------------------+
| workout_sessions    |     | workout_exercises      |     | workout_sets     |
|---------------------|     |------------------------|     |------------------|
| id (PK)            |     | id (PK)               |     | id (PK)         |
| started_at          |     | session_id (FK)        |     | workout_exercise |
| completed_at        |     | exercise_id (FK)       |     |   _id (FK)      |
| duration_seconds    |     | order_index            |     | set_index        |
| status              |     | superset_group_id      |     | set_type         |
| notes               |     | rest_timer_seconds     |     | planned_weight   |
| template_id (FK?)   |     | notes                  |     | planned_reps     |
+---------------------+     +------------------------+     | actual_weight    |
                                                            | actual_reps      |
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
| exercise_id (FK)    |     | weight_value        |
| record_type         |     | unit                |
| weight_value        |     | recorded_at         |
| reps                |     +---------------------+
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
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "description") val description: String,
    @ColumnInfo(name = "equipment") val equipment: String, // "barbell", "dumbbell", "cable", "machine", "bodyweight"
    @ColumnInfo(name = "isolation_level") val isolationLevel: String, // "compound", "isolation"
    @ColumnInfo(name = "primary_group_id") val primaryGroupId: Long,
    @ColumnInfo(name = "tips") val tips: String, // stored as JSON array of strings
    @ColumnInfo(name = "pros") val pros: String, // stored as JSON array of strings
    @ColumnInfo(name = "anatomy_asset_path") val anatomyAssetPath: String,
    @ColumnInfo(name = "display_order") val displayOrder: Int,
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
    @ColumnInfo(name = "status") val status: String, // "active", "paused", "completed", "abandoned"
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
    @ColumnInfo(name = "weight_value") val weightValue: Double,
    @ColumnInfo(name = "unit") val unit: String,
    @ColumnInfo(name = "recorded_at") val recordedAt: Long,
)

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
    suspend fun addExerciseToSession(sessionId: Long, exerciseId: Long, orderIndex: Int)
    suspend fun removeExerciseFromSession(workoutExerciseId: Long)
    suspend fun reorderExercises(sessionId: Long, exerciseOrder: List<Long>)
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

data class PlanRequest(
    val exercises: List<PlanExercise>,
    val userProfile: UserPlanProfile,
    val trainingHistory: List<ExerciseHistory>,
)

data class PlanExercise(
    val exerciseId: Long,
    val name: String,
    val equipment: String,
    val isolationLevel: String,
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
)

data class ExercisePlan(
    val exerciseId: Long,
    val warmupSets: List<PlannedSet>,
    val workingSets: List<PlannedSet>,
)

data class PlannedSet(
    val weight: Double,
    val reps: Int,
    val setNumber: Int,
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

The `GeminiPromptBuilder` constructs a prompt from training history and user context. The CSCS reviews and approves the prompt template. The developer implements the data injection.

```kotlin
class GeminiPromptBuilder @Inject constructor() {

    fun build(request: PlanRequest): String = buildString {
        appendLine("You are a strength training coach. Generate a workout plan as JSON.")
        appendLine()
        appendLine("## User Profile")
        appendLine("- Experience level: ${experienceLevelLabel(request.userProfile.experienceLevel)}")
        request.userProfile.bodyWeightKg?.let { appendLine("- Body weight: ${it}kg") }
        request.userProfile.age?.let { appendLine("- Age: $it") }
        request.userProfile.gender?.let { appendLine("- Gender: $it") }
        appendLine()
        appendLine("## Exercises (in order)")
        request.exercises.forEachIndexed { index, exercise ->
            appendLine("${index + 1}. ${exercise.name} (${exercise.equipment}, ${exercise.isolationLevel})")
        }
        appendLine()
        if (request.trainingHistory.isNotEmpty()) {
            appendLine("## Recent Training History")
            request.trainingHistory.forEach { history ->
                appendLine("### ${history.exerciseName}")
                history.sessions.takeLast(3).forEach { session ->
                    appendLine("  Session (${formatDate(session.date)}):")
                    session.sets.forEach { set ->
                        appendLine("    ${set.weight}kg x ${set.reps} (${set.setType})")
                    }
                }
            }
            appendLine()
        }
        appendLine("## Instructions")
        appendLine("For each exercise, provide warm-up sets and working sets.")
        appendLine("Base recommendations on the training history when available.")
        appendLine("Apply progressive overload: slight weight or rep increases from last session.")
        appendLine("If no history exists, use conservative starting weights for the experience level.")
        appendLine()
        appendLine("## Output Format")
        appendLine("Respond ONLY with valid JSON matching this schema:")
        appendLine("""
        {
          "exercise_plans": [
            {
              "exercise_id": <number>,
              "warmup_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}],
              "working_sets": [{"weight": <number>, "reps": <number>, "set_number": <number>}]
            }
          ]
        }
        """.trimIndent())
    }

    private fun experienceLevelLabel(level: Int): String = when (level) {
        1 -> "Total Beginner (0-6 months)"
        2 -> "Intermediate (6-18 months)"
        3 -> "Advanced (18+ months)"
        else -> "Unknown"
    }
}
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

### 5.4 Elapsed Time Accuracy

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
|   |   |   |-- components/
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
|   |   |   |   |-- CalculatePersonalRecordsUseCase.kt
|   |   |   |   |-- GetExerciseProgressUseCase.kt
|   |   |   |   |-- GetMuscleGroupVolumeUseCase.kt
|   |   |   |   |-- CalculateEstimated1rmUseCase.kt
|   |   |   |   |-- SaveTemplateUseCase.kt
|   |   |   |   |-- GetWorkoutSummaryUseCase.kt
|   |   |   |-- provider/
|   |   |   |   |-- AiPlanProvider.kt
|   |   |   |   |-- ConnectivityChecker.kt
|   |   |   |   |-- BaselinePlanGenerator.kt
|   |   |   |-- util/
|   |   |   |   |-- Estimated1rmCalculator.kt
|   |   |   |   |-- VolumeCalculator.kt
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
|-- build-logic/
|   |-- convention/
|   |   |-- src/main/kotlin/
|   |   |   |-- AndroidApplicationConventionPlugin.kt
|   |   |   |-- AndroidLibraryConventionPlugin.kt
|   |   |   |-- AndroidComposeConventionPlugin.kt
|   |   |   |-- AndroidHiltConventionPlugin.kt
|   |   |   |-- AndroidFeatureConventionPlugin.kt
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
```

---

## Appendix A: CI/CD Pipeline

### GitHub Actions -- CI

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

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
        run: ./gradlew assembleDebug
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: '**/build/reports/tests/'
```

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
          GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
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
