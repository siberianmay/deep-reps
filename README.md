# Deep Reps

A free, AI-powered gym and strength training tracker for Android.

## Status

**Phase 1 MVP — code complete.** 15 epics implemented across 14 Gradle modules. Pending first build verification and QA pass.

## What It Does

1. Select muscle groups (7 groups, 78 exercises)
2. AI generates a personalized session plan (Gemini API)
3. Log sets during the workout with real-time tracking
4. Review summary with PR detection and volume stats
5. Track progress over time with per-exercise charts

The app works fully offline except for AI plan generation, which falls back to cached plans or a baseline generator when offline.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.1 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVI + Clean Architecture |
| DI | Hilt (KSP) |
| Database | Room (WAL mode, 12 entities) |
| Network | Ktor Client (OkHttp engine) |
| AI | Gemini API behind swappable provider interface |
| Analytics | Firebase Analytics + Crashlytics (consent-gated, defaults OFF) |
| CI/CD | GitHub Actions (lint, test, build, Play Store upload) |
| Testing | JUnit 5, MockK, Turbine, Truth |
| Min SDK | 26 (Android 8.0) |
| JDK | Zulu 21 |

## Module Structure

```
:app                          Single-activity host, navigation, lifecycle
:feature:workout              Setup, active logging, summary, notes
:feature:exercise-library     Browse, detail, multi-select
:feature:progress             Dashboard, per-exercise charts, session history
:feature:templates            CRUD for workout templates
:feature:ai-plan              Plan review with safety warnings
:feature:onboarding           4-screen consent + profile flow
:feature:profile              User settings (placeholder)
:core:domain                  Pure Kotlin models, use cases, state machine
:core:data                    Repository impls, mappers, consent, analytics
:core:database                Room entities, DAOs, prepopulate callback
:core:network                 Ktor client, Gemini provider, DTOs
:core:ui                      Theme, design tokens, 11 shared components
:core:common                  Dispatchers, result types
:benchmark                    Baseline profiles (placeholder)
```

## Key Features (MVP)

- AI-powered workout plan generation with 4-level fallback chain
- CSCS-validated exercise ordering (compounds first, core last)
- Real-time set logging with auto-save to Room on every completion
- Rest timer with foreground service (survives backgrounding)
- Workout pause/resume with accurate elapsed time
- Weight PR detection on workout completion
- Crash recovery: resume or discard dialog on relaunch
- 24-hour abandoned session cleanup
- Workout templates (save/load/edit/delete)
- Per-exercise notes (debounced auto-save, 1000 char max)
- Progress charts with time range filtering (4W/12W/6M/All)
- Session history with detailed exercise/set breakdown
- Safety validation (weight jumps, volume ceilings, age gating, difficulty gating)
- Deload detection (scheduled + reactive)
- Analytics instrumentation with consent gating (defaults OFF)

## Building

Prerequisites: JDK 21 (Zulu recommended), Android SDK 35.

```bash
# First build
./gradlew build

# Run tests
./gradlew test

# Run lint + detekt
./gradlew lint detektMain
```

Firebase features require `app/google-services.json` from your Firebase project. The app compiles and runs without it (analytics falls back to no-op).

Gemini API key: set `GEMINI_API_KEY` in `local.properties` or as a CI secret.

## Documentation

| Document | Purpose |
|----------|---------|
| [FEATURES.md](FEATURES.md) | Complete feature specification |
| [TEAM.md](TEAM.md) | Team composition and roles |
| [docs/product-strategy.md](docs/product-strategy.md) | Business model, KPIs, retention targets |
| [docs/architecture.md](docs/architecture.md) | Technical blueprint |
| [docs/exercise-science.md](docs/exercise-science.md) | CSCS exercise reference |
| [docs/design-system.md](docs/design-system.md) | UI specs, M3 tokens, component specs |
| [docs/analytics-plan.md](docs/analytics-plan.md) | Event taxonomy, dashboards |
| [docs/testing-strategy.md](docs/testing-strategy.md) | Test pyramid, 200+ test cases |
| [docs/implementation-plan/](docs/implementation-plan/) | Phased build plan |

## Architecture Decisions

- **Offline-first**: All features work without connectivity except AI plan generation
- **Weight storage**: All weights in kg internally, converted at the UI layer
- **AI abstraction**: Gemini sits behind `AiPlanProvider` interface — swappable without touching feature code
- **Consent defaults OFF**: Analytics and Crashlytics collection disabled until explicit user opt-in
- **Room is truth**: No completed workout data exists only in memory. Auto-save on every set completion.
- **Process death safe**: `SavedStateHandle` stores `sessionId`, full state rebuilt from Room

## License

All rights reserved.
