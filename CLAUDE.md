# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Deep Reps is a free, AI-powered gym and strength training tracker for Android. **Phase 1 MVP is code-complete** — 15 epics implemented across 14 Gradle modules (~285 Kotlin source files, ~30 test files). Pending first build verification and QA pass.

## Build & Test

```bash
# Prerequisites: JDK 21 (Zulu), Android SDK 35
./gradlew build          # Full build
./gradlew test           # Unit tests (JUnit 5)
./gradlew lint           # Android lint
./gradlew detektMain     # Static analysis
```

- Firebase requires `app/google-services.json` — app compiles without it (analytics falls back to no-op)
- Gemini API key: set `GEMINI_API_KEY` in `local.properties`

## Architecture

- **Pattern:** MVI (Model-View-Intent) + Clean Architecture
- **DI:** Hilt with KSP (not kapt)
- **Database:** Room with WAL mode, 12 entities, pre-populated with 78 exercises
- **Network:** Ktor Client (OkHttp engine) for Gemini API
- **UI:** Jetpack Compose, Material 3, dark theme primary
- **Testing:** JUnit 5, MockK, Turbine, Truth
- **SDK:** compileSdk 35, targetSdk 35, minSdk 26, JVM target 21

## Module Dependency Graph

```
:app → all :feature:* modules + :core:data
:feature:* → :core:domain, :core:ui, :core:common (via AndroidFeatureConventionPlugin)
:feature:ai-plan → :core:network (additional explicit dep)
:core:data → :core:domain, :core:database, :core:network, :core:common
:core:database → :core:domain, :core:common
:core:network → :core:common
:core:ui → :core:domain, :core:common
:core:domain → :core:common
:core:common → (leaf module, no project deps)
```

## Convention Plugins (build-logic/)

8 plugins under `deepreps.*` namespace: `android.application`, `android.library`, `android.compose`, `android.hilt`, `android.feature`, `jvm.library`, `detekt`, `android.benchmark`. Feature modules apply `android.feature` which combines library + compose + hilt + standard dependencies.

## Key Patterns

### MVI in ViewModels
Every feature screen follows: `UiState` (data class, StateFlow), `Intent` (sealed interface), `SideEffect` (sealed interface, Channel). State updates via `_state.update { it.copy(...) }`. Side effects via `Channel<SideEffect>`.

### Domain Layer
Pure Kotlin — zero Android dependencies. Contains models, repository interfaces, provider interfaces, use cases, state machine, calculators.

### Repository Pattern
Interfaces in `:core:domain`, implementations in `:core:data`. All bound via Hilt `@Binds` in `RepositoryModule`. Flows use `flowOn(dispatchers.io)`, suspend functions use `withContext(dispatchers.io)`.

### Entity-Domain Mapping
Extension functions in `core/data/mapper/` (e.g., `ExerciseEntity.toDomain()`, `Exercise.toEntity()`). Enums stored as strings in Room, converted via `fromValue()` companion methods.

### Navigation
Each feature module has a `navigation/` package with route constants, `NavGraphBuilder` extensions, and `NavController` extensions. App-level wiring in `DeepRepsNavHost.kt`.

## Domain Rules

- **Weight storage:** All weights in kg internally. UI layer converts to lbs when user preference is lbs.
- **Session statuses:** active, paused, completed, discarded, abandoned, crashed
- **AI fallback chain:** Gemini API → Cached plan → Baseline generator → Manual entry
- **Exercise ordering:** Compounds first (by priority), then isolations, core always last
- **Analytics consent:** Defaults OFF. Stored in EncryptedSharedPreferences, not Room.
- **Room is truth:** No completed set data exists only in memory. Auto-save on every set completion.
- **Process death:** SavedStateHandle stores only `sessionId` (Long). Full state rebuilt from Room.

## Key Files

| File | Purpose |
|------|---------|
| `settings.gradle.kts` | All 14 modules + build-logic |
| `gradle/libs.versions.toml` | Version catalog (~140 lines) |
| `core/database/PrepopulateCallback.kt` | Seeds 7 muscle groups + 78 exercises |
| `core/domain/statemachine/WorkoutStateMachine.kt` | 6-state workout lifecycle |
| `core/domain/usecase/GeneratePlanUseCase.kt` | 4-level AI fallback chain |
| `core/domain/provider/BaselinePlanGenerator.kt` | Offline plan generation with BW ratio tables |
| `core/data/consent/ConsentManager.kt` | EncryptedSharedPreferences consent |
| `core/data/analytics/FirebaseAnalyticsTracker.kt` | Consent-gated Firebase wrapper |
| `feature/workout/active/WorkoutViewModel.kt` | Most complex ViewModel (~700 lines) |
| `app/DeepRepsNavHost.kt` | Root navigation graph |
| `app/MainViewModel.kt` | Crash recovery + session resume/discard |

## Documentation

| Document | Authority For |
|----------|--------------|
| `docs/product-strategy.md` | Business model, KPIs, retention targets, scope |
| `docs/architecture.md` | Technical blueprint (~2200 lines) |
| `docs/exercise-science.md` | CSCS canonical reference for all training logic |
| `docs/design-system.md` | UI specs, M3 tokens, component specs |
| `docs/analytics-plan.md` | Event taxonomy, dashboards, experiments |
| `docs/testing-strategy.md` | Test pyramid, 200+ test cases |
| `docs/devops-pipeline.md` | CI/CD, Firebase, deployment |
| `docs/implementation-plan/` | Phased build plan (Phase 0-1 complete) |

## What's Out of Scope (MVP)

Custom exercises, nutrition tracking, social features, wearable integration, video coaching, multi-language, iOS, supersets, mid-workout modifications, advanced analytics, PR dashboard. See FEATURES.md "Feature Boundary" section.
