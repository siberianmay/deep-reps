# Deep Reps — Implementation Plan Overview

**Created:** 2026-02-11
**Owner:** Project Manager
**Status:** Draft for approval

---

## Document Map

| File | Contents |
|------|----------|
| `00-overview.md` | This file. Master plan, critical path, team allocation, risk ordering |
| `01-phase-0-foundation.md` | Pre-development (Weeks 1-8): hiring, exercise library, design, AI prompts |
| `02-phase-1-mvp.md` | MVP Development (Weeks 9-22): 15 epics, ~80 tasks, full dependency graph |
| `03-phase-2-beta.md` | Beta & Pre-Launch (Weeks 23-28): testing, ASO, polish |
| `04-phase-3-launch.md` | Public Launch (Week 29): staged rollout, monitoring |
| `05-phase-4-growth.md` | Post-Launch Growth (Weeks 30-52): Phase 2 features, retention push |

---

## Timeline Summary

```
PHASE 0       PHASE 1 (MVP DEV)              PH 2 (BETA)   PH3  PHASE 4
Weeks 1-8     Weeks 9-22                     Weeks 23-28   W29  Weeks 30-52
|------------|--------------------------------|------------|--|-----------------|
Foundation    Scaffolding → Features → QA     Beta→Polish  🚀  Growth & Iterate
```

---

## MVP Feature Set (14 items)

Per `product-strategy.md` Section 5.2. All must ship by Week 22 (internal alpha gate).

| # | Feature | RICE | Critical Path? |
|---|---------|------|---------------|
| 1 | Onboarding (consent, experience level, units, profile) | 9,500 | Yes — blocks user profile for AI |
| 2 | Muscle group selection screen | 6,000 | Yes — entry point to core loop |
| 3 | Exercise picker per group (with detail views) | 4,500 | Yes — requires exercise DB |
| 4 | Exercise library (CSCS-curated detail cards) | 4,500 | Yes — blocks everything downstream |
| 5 | Auto-ordered exercise sequencing | 1,400 | No — enhances flow, not blocking |
| 6 | AI plan generation via Gemini | 2,400 | Yes — core differentiator |
| 7 | Active workout logging (sets, weight, reps, timer, auto-save) | 4,750 | Yes — the primary screen |
| 8 | Rest timer (configurable, notification) | 3,600 | No — enhances workout UX |
| 9 | Per-exercise notes | 2,250 | No — simple addition |
| 10 | Workout complete summary | 1,867 | No — enhances loop closure |
| 11 | Workout templates (save/load) | 1,700 | No — convenience feature |
| 12 | Basic progress tracking (weight chart, session history) | 2,975 | No — post-core-loop |
| 13 | Data persistence with crash recovery | 9,500 | Yes — non-negotiable for data integrity |
| 14 | Offline fallback (cached/baseline plans) | 1,875 | Yes — required for offline-first promise |

---

## Critical Path

The critical path is the longest dependency chain. If anything on this chain slips, the launch date slips.

```
Exercise DB (.db file) ──→ Exercise Library UI ──→ Exercise Picker ──→ Workout Setup
                                                                          │
                                                                          ▼
Room Schema + Entities ──→ DAOs + Repositories ──→ Use Cases ──→ AI Plan Generation
                                                       │              │
                                                       ▼              ▼
                     Onboarding ──→ User Profile ──→ Active Workout Logging
                                                       │
                                                       ▼
                                              Crash Recovery + Data Persistence
                                                       │
                                                       ▼
                                              Workout Summary ──→ Progress Tracking
```

**Critical path items (delays here delay launch):**
1. Pre-populated exercise database (`.db` file from CSCS data) — blocks all exercise UI
2. Room schema (all entities + DAOs) — blocks all data flow
3. Core domain use cases (state machine, plan generation, set completion) — blocks feature work
4. Active workout logging screen — most complex UI, highest risk
5. AI plan generation + fallback chain — core differentiator, network-dependent
6. Crash recovery and data persistence hardening — non-negotiable for launch

---

## Team Allocation (Phase 1: Weeks 9-22)

### Available Roles

| Role | Abbrev | Primary Focus |
|------|--------|---------------|
| Lead Android Dev | **Lead** | Architecture, scaffolding, complex features (workout, AI, data layer) |
| Mid-Senior Android Dev | **Mid** | Feature implementation (exercise library, onboarding, templates, UI) |
| QA Engineer | **QA** | Continuous testing from Week 14, full regression Weeks 18-22 |
| DevOps Engineer | **DevOps** | CI/CD setup Week 9-10, then on-call for pipeline issues |
| Data Analyst | **DA** | Analytics instrumentation Weeks 20-22 |
| CSCS | **CSCS** | Consultation: validate exercise DB, review AI prompt outputs, safety guardrails |
| UI/UX Designer | **Design** | Design support: component refinement, edge case screens, QA visual review |

### Parallel Workstreams

The two developers work on independent module tracks that converge at integration points.

```
Week 9-10:  Lead: Scaffolding + build-logic + Room schema
            Mid:  Core UI components + theme + design tokens

Week 11-13: Lead: Data layer (repos, mappers, DAOs) + workout state machine
            Mid:  Exercise library UI + onboarding flow

Week 13-16: Lead: AI plan generation + Gemini integration + fallback chain
            Mid:  Muscle group selector + exercise picker + auto-ordering

Week 15-18: Lead: Active workout logging + foreground service + rest timer
            Mid:  Templates + workout summary + per-exercise notes

Week 18-20: Lead: Crash recovery + data persistence hardening + process death
            Mid:  Basic progress tracking + charts

Week 20-22: Lead: Performance optimization + analytics (with DA)
            Mid:  Bug fixes + polish + edge cases
            QA:   Full regression + device matrix testing
```

---

## Risk-Ordered Build Sequence

Features are built in order of technical risk, not RICE score. High-risk items go first so problems surface early when there's time to adjust.

| Priority | Feature | Risk Level | Rationale |
|----------|---------|-----------|-----------|
| 1 | Room schema + pre-populated DB | High | Foundation for everything. Schema mistakes are expensive to fix post-data. |
| 2 | Workout state machine + data persistence | High | Most complex domain logic. Process death, crash recovery, concurrent writes. |
| 3 | AI plan generation + fallback chain | High | Network dependency, LLM output parsing, prompt engineering iteration. |
| 4 | Active workout logging screen | High | Most complex UI. Foreground service, real-time timer, sweaty-hand UX. |
| 5 | Exercise library + picker | Medium | Standard CRUD UI, but blocks workout setup flow. |
| 6 | Onboarding | Low | Simple flow, but blocks user profile for AI context. |
| 7 | Rest timer | Medium | Foreground service integration, background behavior, Doze mode. |
| 8 | Templates | Low | Standard CRUD, no technical risk. |
| 9 | Auto-ordering | Low | Pure domain logic, well-specified algorithm. |
| 10 | Progress tracking | Medium | Chart rendering, complex queries, time-range filtering. |
| 11 | Workout summary | Low | Derived from existing data. |
| 12 | Per-exercise notes | Low | Single text field per exercise. |
| 13 | Offline fallback | Medium | Already designed in fallback chain; needs integration testing. |
| 14 | Analytics instrumentation | Low | Firebase SDK integration, event firing. |

---

## Cut Line (Emergency Scope Reduction)

If the team is behind schedule at Week 18, deprioritize in this order:

1. **Auto-ordered exercise sequencing** → Users manually order (acceptable UX)
2. **Per-exercise notes** → Defer to Phase 2 (low engagement impact)
3. **Workout templates** → Defer to Phase 2 (users re-select exercises manually)
4. **Basic progress tracking** → Defer to Phase 2 (users still have workout history list)
5. **Workout complete summary** → Show minimal completion screen (duration + "done")

**Never cut:**
- AI plan generation (the differentiator — without it we're just another logger)
- Crash recovery / data persistence (one data loss bug = permanent trust destruction)
- Offline fallback (the offline-first promise)
- Exercise library (no exercises = no app)

---

## QA Integration Strategy

**Continuous testing, not big-bang.** QA starts testing features as they reach "feature-complete" status.

| Period | QA Activity |
|--------|-------------|
| Weeks 9-13 | Write test plans. Set up device matrix. Configure Firebase Test Lab. |
| Weeks 14-16 | Begin testing completed features (exercise library, onboarding). File bugs. |
| Weeks 16-18 | Test workout logging, AI plan flow, templates as they complete. |
| Weeks 18-20 | Full integration testing: end-to-end core loop. Process death testing. |
| Weeks 20-22 | Full regression. Device fragmentation matrix. Performance testing. Edge cases. |

---

## Phase 0 → Phase 1 Handoff Gates

### MUST be complete before development starts (Week 9):
- [ ] Architecture design document approved (architecture.md — done)
- [ ] Exercise library spreadsheet from CSCS (all 78 exercises with full metadata)
- [ ] Wireframes for core loop (group select → exercise pick → plan → workout → summary)
- [ ] Design tokens finalized (colors, typography, spacing — design-system.md)
- [ ] CI/CD pipeline scaffolded (GitHub Actions, at minimum lint + build)

### CAN start development while Phase 0 finishes:
- High-fidelity mockups (wireframes sufficient for data layer + initial UI work)
- Anatomy diagrams (not needed until exercise detail view implementation)
- AI prompt templates (not needed until AI integration at Week 13)
- UX validation sessions (results inform polish, not core architecture)

---

## Next Steps

1. Review and approve this overview
2. Read detailed phase files (`01` through `05`)
3. Assign owners to each epic
4. Set up project tracking (GitHub Projects or equivalent)
5. Begin Phase 0 execution
