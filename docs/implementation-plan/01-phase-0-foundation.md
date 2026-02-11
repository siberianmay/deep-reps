# Phase 0: Foundation (Weeks 1-8)

Pre-development preparation. No code is written. All outputs feed into Phase 1.

---

## Epic 0.1: Team Hiring & Onboarding

**Owner:** CEO / Product Owner
**Duration:** Weeks 1-4
**Blocks:** All Phase 1 work

| # | Task | Owner | Week | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|-------------------|
| 0.1.1 | Finalize job descriptions for 7 MVP roles | CEO | 1 | JDs posted | All 7 roles listed in TEAM.md have JDs |
| 0.1.2 | Interview and hire CSCS | CEO | 1-3 | Signed offer | NSCA-CSCS certified, 8+ years coaching |
| 0.1.3 | Interview and hire Lead Android Dev | CEO | 1-3 | Signed offer | 8+ years Android, Compose + MVI experience |
| 0.1.4 | Interview and hire Mid-Senior Android Dev | CEO | 2-4 | Signed offer | 4+ years Android, Kotlin + Compose |
| 0.1.5 | Interview and hire UI/UX Designer | CEO | 2-4 | Signed offer | MD3 fluency, fitness app portfolio |
| 0.1.6 | Interview and hire QA Engineer | CEO | 3-4 | Signed offer | Android device testing experience |
| 0.1.7 | Onboard all hires: repo access, tools, docs review | CEO | 4 | Team ready | All team members have reviewed CLAUDE.md, FEATURES.md, TEAM.md |

**Risk:** CSCS hire is the #1 blocker. Exercise library cannot start without them. Mitigation: begin CSCS recruiting immediately; have 3 candidates in pipeline.

---

## Epic 0.2: Competitive Deep-Dive

**Owner:** CEO + CSCS
**Duration:** Weeks 1-3
**Blocks:** Design decisions, feature prioritization

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.2.1 | Install and use Strong, Hevy, JEFIT, Fitbod for 2 weeks | CEO + CSCS | 1-2 | Raw notes on each app |
| 0.2.2 | Document competitive UX audit | CEO | 2-3 | `docs/competitive-audit.md` — friction points, screenshots, strengths/weaknesses |
| 0.2.3 | Identify 5 specific UX improvements Deep Reps will make | CEO + Design | 3 | List in competitive audit doc |

---

## Epic 0.3: Exercise Library Creation

**Owner:** CSCS
**Duration:** Weeks 2-6
**Blocks:** Pre-populated DB file, exercise detail UI, AI prompt testing

| # | Task | Owner | Week | Deliverable | Acceptance Criteria |
|---|------|-------|------|-------------|-------------------|
| 0.3.1 | Define all 78 exercises across 7 muscle groups | CSCS | 2-3 | Spreadsheet with all fields per `exercise-science.md` schema | All fields populated: stableId, name, primaryGroup, secondaryMuscles, equipment, movementType, difficulty, description, tips (max 4), pros (max 3), orderPriority, supersetTags, autoProgramMinLevel |
| 0.3.2 | Define muscle group taxonomy and sub-muscle breakdowns | CSCS | 2-3 | Section 2 of exercise-science.md validated | All 7 groups with sub-muscles documented |
| 0.3.3 | Define cross-group activation map | CSCS | 3 | Section 2.2 of exercise-science.md | Overlap rules for AI prompt |
| 0.3.4 | Define baseline plan tables (BW ratios by experience level) | CSCS | 3-4 | Section 4 of exercise-science.md | Tables for all 78 exercises x 3 levels |
| 0.3.5 | Define progression models (linear, DUP, block) | CSCS | 3-4 | Section 3 of exercise-science.md | Rules for each experience level |
| 0.3.6 | Define safety guardrails (max weight jump, MRV, age modifiers) | CSCS | 4 | Section 8 of exercise-science.md | All 9 safety rules specified |
| 0.3.7 | Define auto-ordering algorithm rules | CSCS | 4-5 | Section 6 of exercise-science.md | Priority values for all 78 exercises |
| 0.3.8 | Review and approve exercise library spreadsheet | CEO + CSCS | 5-6 | Approved spreadsheet | Sign-off from CSCS and CEO |

---

## Epic 0.4: Architecture & Tech Stack

**Owner:** Lead Android Dev
**Duration:** Weeks 2-4
**Blocks:** All development work

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.4.1 | Tech stack selection and rationale | Lead | 2 | architecture.md Sections 1.1-1.14 (done) |
| 0.4.2 | Module dependency graph design | Lead | 2-3 | architecture.md Section 2 (done) |
| 0.4.3 | Room database schema design (all entities, relationships, indices) | Lead | 3 | architecture.md Section 3 (done) |
| 0.4.4 | AI provider interface design | Lead | 3-4 | architecture.md Section 4 (done) |
| 0.4.5 | State management design (workout state machine) | Lead | 3-4 | architecture.md Section 5 (done) |
| 0.4.6 | Performance requirements definition | Lead | 4 | architecture.md Section 6 (done) |
| 0.4.7 | Security considerations | Lead | 4 | architecture.md Section 7 (done) |
| 0.4.8 | Architecture review with team | Lead + CEO | 4 | Architecture approved for implementation |

**Status:** All 0.4.x tasks are COMPLETE. architecture.md is approved.

---

## Epic 0.5: UI/UX Design

**Owner:** UI/UX Designer
**Duration:** Weeks 3-8
**Blocks:** Feature UI implementation (but data layer work can start without it)

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.5.1 | Wireframes: onboarding flow (4 screens) | Design | 3-4 | Figma wireframes |
| 0.5.2 | Wireframes: muscle group selection → exercise picker | Design | 3-4 | Figma wireframes |
| 0.5.3 | Wireframes: AI plan review screen | Design | 4 | Figma wireframes |
| 0.5.4 | Wireframes: active workout logging screen | Design | 4-5 | Figma wireframes |
| 0.5.5 | Wireframes: rest timer overlay | Design | 4-5 | Figma wireframes |
| 0.5.6 | Wireframes: workout summary screen | Design | 5 | Figma wireframes |
| 0.5.7 | Wireframes: exercise detail card | Design | 5 | Figma wireframes |
| 0.5.8 | Wireframes: progress dashboard + charts | Design | 5-6 | Figma wireframes |
| 0.5.9 | Wireframes: templates list + create | Design | 5-6 | Figma wireframes |
| 0.5.10 | Wireframes: profile + settings | Design | 6 | Figma wireframes |
| 0.5.11 | Design system finalization (tokens, components) | Design | 6-7 | design-system.md finalized, Figma component library |
| 0.5.12 | High-fidelity mockups: all screens (dark theme) | Design | 6-8 | Figma hi-fi designs |
| 0.5.13 | Light theme derivation | Design | 7-8 | Figma light theme variants |
| 0.5.14 | Interaction specs: set completion, rest timer, drag-reorder | Design | 7-8 | Annotated Figma prototypes |

---

## Epic 0.6: AI Prompt Engineering

**Owner:** CSCS + Lead Android Dev
**Duration:** Weeks 4-8
**Blocks:** AI plan generation feature (but not until Week 13)

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.6.1 | Draft initial Gemini prompt template | Lead + CSCS | 4-5 | Prompt text with placeholders |
| 0.6.2 | Define JSON output schema | Lead | 5 | Schema in architecture.md Section 4.3 (done) |
| 0.6.3 | Test prompts with 10 sample inputs (variety of user profiles) | Lead + CSCS | 5-6 | Sample outputs validated by CSCS |
| 0.6.4 | Validate safety guardrails in AI outputs | CSCS | 6-7 | All 9 safety constraints verified in outputs |
| 0.6.5 | Iterate prompt based on CSCS feedback | Lead + CSCS | 7 | Finalized prompt version (v2.0) |
| 0.6.6 | Document prompt version and test results | Lead | 8 | Prompt test log |

---

## Epic 0.7: Anatomy Diagram Production

**Owner:** UI/UX Designer
**Duration:** Weeks 4-8
**Budget:** $2,000-$4,000
**Blocks:** Exercise detail view implementation

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.7.1 | Source illustrator or licensed SVG library | Design | 4 | 3 candidate options with pricing |
| 0.7.2 | Commission/license 78 exercise anatomy diagrams | Design | 4-5 | Contract signed |
| 0.7.3 | Receive and review first batch (20 diagrams) | Design + CSCS | 5-6 | Anatomical accuracy validated |
| 0.7.4 | Receive and review remaining 58 diagrams | Design + CSCS | 6-8 | All 78 diagrams delivered |
| 0.7.5 | Optimize SVGs for mobile (<50KB each) | Design | 7-8 | Optimized assets ready for bundling |

---

## Epic 0.8: UX Validation

**Owner:** CEO (abbreviated, no UX Researcher until post-launch)
**Duration:** Weeks 4-8

| # | Task | Owner | Week | Deliverable |
|---|------|-------|------|-------------|
| 0.8.1 | Recruit 5-8 gym contacts for moderated usability sessions | CEO | 4-5 | Participant list |
| 0.8.2 | Conduct wireframe walkthroughs (core loop) | CEO | 6-7 | Session notes |
| 0.8.3 | Synthesize findings and adjust wireframes | CEO + Design | 7-8 | Updated wireframes |

---

## Phase 0 Gate (Week 8)

**Go/No-Go for development start.** ALL of the following must be true:

- [ ] Exercise library spreadsheet complete and approved (78 exercises, all fields)
- [ ] Architecture document approved
- [ ] Wireframes complete for all core loop screens
- [ ] Design system tokens finalized
- [ ] AI prompt templates tested with sample outputs
- [ ] Anatomy diagrams delivered (or at least 60 of 78, rest in progress)
- [ ] CI/CD pipeline scaffolded (at minimum: lint + unit test + build)
- [ ] All MVP-phase team members onboarded

**Decision maker:** CEO / Product Owner
