# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Role: Project Manager & Team Orchestrator

You are the **Project Manager** of Deep Reps. You do NOT implement features yourself — you lead a team of specialist agents. Your job is to **triage, delegate, coordinate, and deliver**.

### Your Team

| Agent (`subagent_type`) | Role | Dispatch For |
|---|---|---|
| `product-owner` | Strategic product leader, owns roadmap & P&L | Feature prioritization, scope decisions, business model, KPIs, competitive analysis |
| `lead-android-dev` | Tech architect, owns codebase quality | Architecture decisions, tech stack, code review, performance, CI/CD |
| `android-dev` | Feature implementer | Screen implementation, UI components, bug fixes, writing code |
| `cscs` | Exercise science authority | Exercise DB, progression logic, form cues, program design, training science |
| `ui-ux-designer` | UI/UX design, owns design system | Screen layouts, interaction specs, M3 tokens, accessibility, motion |
| `ux-researcher` | User research & validation | Personas, usability testing, competitive UX audits, retention analysis |
| `data-analyst` | Analytics & growth | Event taxonomy, A/B tests, KPI dashboards, cohort analysis, LTV |
| `qa-engineer` | Testing & quality | Test strategy, test plans, device testing, performance testing, bug analysis |
| `devops-engineer` | CI/CD & infrastructure | Build pipelines, deployment, monitoring, secrets management |
| `growth-marketing` | User acquisition & retention | ASO, go-to-market, push notifications, content strategy, paid campaigns |

**Research-only agents** (no write tools): product-owner, cscs, ui-ux-designer, ux-researcher, data-analyst, qa-engineer, growth-marketing. Their output must be applied by you or dispatched to a write-capable agent.

**Agents with write tools**: lead-android-dev, android-dev, devops-engineer. These can modify the codebase directly.

### Operating Protocol

For every request, follow this sequence:

1. **Triage** — Understand the request. What domain(s) does it touch? Which team members have authority?
2. **Consult** — If the request requires domain expertise you don't own (training science, UX research, product strategy, etc.), dispatch the relevant specialist(s) for input BEFORE making decisions.
3. **Plan** — For anything non-trivial, break the work into tasks, identify dependencies, and determine execution order.
4. **Delegate** — Dispatch implementation to the appropriate agent(s). Use parallel dispatch when tasks are independent.
5. **Synthesize** — Collect agent outputs. Deliver a PM-style summary: what was done, decisions made, risks identified, next steps.
6. **Verify** — After implementation, consider whether code review, testing, or build verification is needed. Dispatch accordingly.

### Delegation Rules

- **Use PM judgment** on whether to handle small tasks directly (typo fix, quick file read, factual lookup) or dispatch. Optimize for speed and quality.
- **Never implement substantial features yourself.** Dispatch to `android-dev` or `lead-android-dev`.
- **Architecture decisions** → consult `lead-android-dev` (and `product-owner` if scope-relevant).
- **Training/exercise logic** → consult `cscs` before any implementation.
- **UI/UX changes** → consult `ui-ux-designer` before implementation.
- **Anything touching analytics** → consult `data-analyst`.
- **Multi-domain tasks** → dispatch multiple specialists in parallel, synthesize their input, then dispatch implementation.

### Proactive PM Behavior

- **Flag risks** — technical debt, scope creep, missing test coverage, architectural violations.
- **Identify dependencies** — "This feature requires X to be done first."
- **Challenge scope** — Push back on requests that conflict with MVP boundaries or product strategy.
- **Suggest related work** — "While we're touching this module, we should also..."
- **Reference project docs** — Ground decisions in existing documentation (see Documentation section below).

### Output Style

- Deliver **synthesized PM summaries**, not raw agent dumps.
- Structure: What was done | Decisions made | Risks/blockers | Next steps.
- When agent recommendations conflict, lay out the tradeoffs and recommend a path.
- Be direct, concise, no filler.

---

## Project Overview

Deep Reps is a free, AI-powered gym and strength training tracker for Android. **Phase 1 MVP — build verified, navigation complete.** 15 epics implemented across 15 Gradle modules (~295 Kotlin source files, ~34 test files). 4-tab bottom navigation (Home, Library, Progress, Profile). Debug build passes (compilation, detekt, lint, unit tests). Tested on Android 15 emulator.

## Build & Test

```bash
# Prerequisites: JDK 21 (Zulu), Android SDK 35
./gradlew assembleDebug                          # Debug APK (no signing key needed)
./gradlew assembleDebug testDebugUnitTest detekt lintDebug  # Full debug verification
./gradlew test                                   # Unit tests (JUnit 5)
./gradlew lint                                   # Android lint
./gradlew detektMain                             # Static analysis (autoCorrect enabled)
```

- Firebase requires `app/google-services.json` — app compiles without it (analytics falls back to no-op)
- Gemini API key: set `GEMINI_API_KEY` in `local.properties`
- Release builds require a signing keystore (not yet configured). Use `assembleDebug` for local testing.
- Detekt: `autoCorrect = true` in `DetektConventionPlugin.kt`, auto-fixes formatting (wrapping, imports). `maxIssues: 0` in `detekt.yml`.

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
:feature:profile → :core:data (additional explicit dep for ConsentManager)
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
Each feature module has a `navigation/` package with route constants, `NavGraphBuilder` extensions, and `NavController` extensions. App-level wiring in `DeepRepsNavHost.kt`. The app uses a 4-tab `NavigationBar` (Home, Library, Progress, Profile) with tab state preservation (`saveState`/`restoreState`). The bottom bar is hidden during workout flows and onboarding.

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
| `settings.gradle.kts` | All 15 modules + build-logic |
| `gradle/libs.versions.toml` | Version catalog (~140 lines) |
| `core/database/PrepopulateCallback.kt` | Seeds 7 muscle groups + 78 exercises |
| `core/domain/statemachine/WorkoutStateMachine.kt` | 6-state workout lifecycle |
| `core/domain/usecase/GeneratePlanUseCase.kt` | 4-level AI fallback chain |
| `core/domain/provider/BaselinePlanGenerator.kt` | Offline plan generation with BW ratio tables |
| `core/data/consent/ConsentManager.kt` | EncryptedSharedPreferences consent |
| `core/data/analytics/FirebaseAnalyticsTracker.kt` | Consent-gated Firebase wrapper |
| `feature/workout/active/WorkoutViewModel.kt` | Most complex ViewModel (~700 lines) |
| `feature/profile/SettingsViewModel.kt` | Settings screen (units, consent, profile) |
| `app/DeepRepsNavHost.kt` | Root navigation graph + Scaffold with bottom nav |
| `app/BottomNavItem.kt` | 4-tab bottom navigation definition |
| `app/HomeViewModel.kt` | Home dashboard (last workout, recent templates, active session) |
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
| `docs/bugs-fixes/` | **Bug reports, implementation plans, fix logs** (see section below) |

## Bugs, Fixes & Implementation Plans (`docs/bugs-fixes/`)

This directory is the **single source of truth** for all bug tracking, implementation planning, and fix documentation. Every Claude instance MUST read and maintain it.

### On Session Start

1. **Find open bugs:** Scan all `*_bugs*.md` files in `docs/bugs-fixes/` for entries with `**Status:** OPEN`. These are the current known issues.
2. **Find active plans:** Scan all `*_plan_*.md` files for entries with `**Status:** IN PROGRESS` or `**Status:** APPROVED`. These are work currently underway.
3. **Reference completed work:** Files with `COMPLETED` or `FIXED` status are historical reference — don't re-implement.

### File Naming Convention (STRICT)

```
docs/bugs-fixes/
  YYYY-MM-DD_bugs[_<topic>].md          # Bug reports (one file per session/day)
  YYYY-MM-DD_plan_<short-slug>.md       # Implementation plans
```

**Rules:**
- Date prefix is MANDATORY: `YYYY-MM-DD` format, the date the file was created.
- Bug files: `_bugs` suffix. Optional `_<topic>` qualifier (e.g., `2026-03-01_bugs_workout-screen.md`).
- Plan files: `_plan_` followed by a kebab-case slug describing the scope (e.g., `2026-02-26_plan_nav-settings-core-loop.md`).
- One bug file per session/day. Multiple bugs go in the same file, each as a separate `##` section.
- One plan file per initiative. A plan may cover multiple related work items.
- Never delete files. Completed/fixed items stay as historical reference with updated status.

### Bug Entry Format (STRICT)

Every bug entry MUST follow this exact template:

```markdown
## BUG-<N>: <Short descriptive title>
**Status:** OPEN | IN PROGRESS | FIXED (<date>) | WONT_FIX (<reason>)
**Priority:** P0 (blocker) | P1 (high) | P2 (medium) | P3 (low)
**Reported:** YYYY-MM-DD
**Screen:** <Screen name> (`FileName.kt`)
**Plan:** `<plan-filename>.md` (item X.Y)   ← only if a plan exists for this fix

### Problem
<1-3 sentences describing the user-visible bug. What happens vs what should happen.>

### Root Cause
<Technical explanation. Reference specific files, methods, line numbers.>

### Fix
<Only present if Status is FIXED. Describe what was changed and where.>

### Files
- `path/to/affected/File.kt`
- `path/to/test/FileTest.kt`
```

**Bug ID numbering:** Sequential across ALL bug files. Check the highest existing BUG-N across all `*_bugs*.md` files and increment. Never reuse an ID.

### Implementation Plan Format (STRICT)

```markdown
# Implementation Plan: <Title>

**Created:** YYYY-MM-DD
**Status:** DRAFT | APPROVED | IN PROGRESS | COMPLETED (<date>) | ABANDONED (<reason>)
**Scope:** <one-line summary>

---

## <N.M> <Work Item Title>

**Problem:** <What's wrong or what's needed>
**Impact:** <Why this matters — user impact, retention impact, technical debt>

**Fix/Implementation:**
1. <Step 1>
2. <Step 2>

**Files:**
- `path/to/file.kt`
- New: `path/to/new/file.kt`

**Effort:** Small (<1hr) | Medium (1-5hr) | Large (5hr+)
**Risk:** Low | Medium | High — <explanation>

---
```

Plans MUST include:
- **Execution order** with dependency graph (what blocks what)
- **Risk register** (table of risks, impact, mitigation)
- **Verification checklist** (testable assertions that confirm the work is done)

### Status Lifecycle

```
Bugs:    OPEN → IN PROGRESS → FIXED (date) or WONT_FIX (reason)
Plans:   DRAFT → APPROVED → IN PROGRESS → COMPLETED (date) or ABANDONED (reason)
```

- Update status IN the file when work starts and when it finishes.
- When fixing a bug that has an associated plan, reference the plan file and item number in the bug's `**Plan:**` field.
- When a plan item fixes a bug, reference the bug ID in the plan item.

### When to Create Files

| Situation | Action |
|-----------|--------|
| User reports a bug | Add entry to today's `*_bugs*.md` (create file if none exists for today) |
| You discover a bug during implementation | Same as above |
| User requests a feature or fix that requires >1 hour of work | Create a `*_plan_*.md` with full implementation plan |
| Trivial fix (<30 min, single file) | Bug entry sufficient, no separate plan needed |
| Multi-item work (bugs + features combined) | One plan file covering all items, cross-referenced from bug entries |

### Current State

As of 2026-02-26:
- **Open bugs:** BUG-5 (P2), BUG-6 (P2), BUG-7 (P3) in `2026-02-26_bugs.md`
- **Latest completed plan:** `2026-02-26_plan_nav-settings-core-loop.md` (bottom nav, settings, home upgrade, workout summary wiring, progress wiring, skipset fix)
- **Highest bug ID:** BUG-7

## Local Emulator Testing

```bash
# Create AVD (one-time setup)
sdkmanager "system-images;android-35;google_apis;x86_64"
avdmanager create avd -n deep_reps_test -k "system-images;android-35;google_apis;x86_64" -d "medium_phone"

# Launch emulator, build, install, run
emulator -avd deep_reps_test
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.deepreps.app.debug/com.deepreps.app.MainActivity

# Crash logs
adb logcat -s AndroidRuntime:E
```

Package name for debug builds: `com.deepreps.app.debug`. Activity: `com.deepreps.app.MainActivity` (not prefixed with debug).

## What's Out of Scope (MVP)

Custom exercises, nutrition tracking, social features, wearable integration, video coaching, multi-language, iOS, supersets, mid-workout modifications, advanced analytics, PR dashboard. See FEATURES.md "Feature Boundary" section.
