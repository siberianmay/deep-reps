# Deep Reps -- Product Strategy

**Document owner:** Product Owner / CEO
**Last updated:** 2026-02-11
**Status:** Draft v1.0

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Competitive Analysis](#2-competitive-analysis)
3. [Target Audience Segmentation](#3-target-audience-segmentation)
4. [Business Model](#4-business-model)
5. [MVP Scope Definition](#5-mvp-scope-definition)
6. [KPI Framework](#6-kpi-framework)
7. [Risk Assessment](#7-risk-assessment)
8. [Go-to-Market Timeline](#8-go-to-market-timeline)

---

## 1. Executive Summary

### What Deep Reps Is

Deep Reps is an Android-native strength training tracker that combines a CSCS-curated exercise library with AI-powered session planning via the Gemini API. The core loop: select muscle groups, pick exercises, receive a personalized plan (warm-up sets, working sets, weights, reps), log what actually happens, and track progress over time. The app is offline-first -- every feature works without connectivity except AI plan generation, which degrades gracefully to cached or baseline plans.

### Target Market

The global fitness app market is valued at approximately $13.9 billion in 2026, growing at 13-14% CAGR. The strength training segment (exercise and weight tracking) accounts for over 53% of fitness app revenue. Deep Reps targets the Android segment specifically -- Android holds roughly 48% of fitness app platform share but is underserved by the strongest workout trackers, most of which were designed iOS-first and ported to Android as an afterthought.

### Value Proposition

**For intermediate lifters who are frustrated with generic workout apps:** Deep Reps provides AI-generated session plans grounded in real exercise science (CSCS-validated exercise library, progressive overload logic, experience-level-aware baselines) with a workout logging experience designed for the gym floor -- large touch targets, minimal taps, offline reliability.

Three differentiators versus the incumbent field:

1. **AI plan generation with training history context.** Not generic templates. Not "pick a program." The AI sees your last 3-5 sessions for each muscle group and proposes weights, reps, and warm-up progressions accordingly.
2. **Exercise science authority.** Every exercise, muscle group mapping, form cue, and progression model is validated by a CSCS. The library is curated, not crowdsourced.
3. **Android-first quality.** Not a React Native port. Not a cross-platform compromise. Native Material Design 3, optimized for the Android ecosystem.

### Strategic Bet

The incumbents (Strong, Hevy, JEFIT) are loggers. They record what you did. Fitbod generates plans but is locked into its own algorithm with no transparency. Deep Reps bets that the next generation of workout apps must be **intelligent and transparent** -- AI proposes, the user decides, and the AI learns from what actually happened. The plan is a suggestion, not a constraint.

---

## 2. Competitive Analysis

### 2.1 Competitor Profiles

#### Strong

| Attribute | Detail |
|-----------|--------|
| **Users** | 3M+ |
| **Monthly downloads** | ~200K |
| **Monthly revenue** | ~$600K |
| **Pricing** | Free (limited to 3 custom exercises) / $29.99/yr / $99.99 lifetime |
| **Platform** | iOS-first, Android secondary |
| **Core strength** | Speed, reliability, 12 years of refinement. Best pure logger on the market. Clean, distraction-free UX. |
| **Core weakness** | No AI features. No workout generation. No plan suggestions. Interface feels dated in 2026. Free tier is aggressively limited (3 custom exercises). Android experience is inferior to iOS. |
| **Positioning** | "The logbook for lifters who already know what they're doing." |

#### Hevy

| Attribute | Detail |
|-----------|--------|
| **Users** | 10M+ |
| **Monthly downloads** | ~300K |
| **Monthly revenue** | ~$500K |
| **Pricing** | Free (generous) / Pro $8.99/mo or $59.99/yr / ~$80 lifetime |
| **Platform** | Cross-platform, slightly iOS-favored |
| **Core strength** | Most generous free tier in the market. Social/community features (feed, followers). Fast growth. Good UX that borrows the best from Strong but modernizes it. Hevy Coach as a B2B2C channel. |
| **Core weakness** | Social features add noise for serious lifters. No AI plan generation. Revenue per user is low because the free tier gives away too much. Analytics are basic compared to JEFIT. |
| **Positioning** | "The social workout tracker -- log, share, compete." |

#### JEFIT

| Attribute | Detail |
|-----------|--------|
| **Users** | 13M+ (20M+ downloads) |
| **Monthly downloads** | Mature, slower growth |
| **Monthly revenue** | Not publicly disclosed |
| **Pricing** | Free (ads) / Elite $12.99/mo or $69.99/yr |
| **Platform** | Cross-platform |
| **Core strength** | Largest exercise library (1,400+). AI-powered NSPI progressive overload tracking. Community challenges. Smartwatch integration. Long track record (since 2010). |
| **Core weakness** | Bloated UX. The app tries to do everything and the interface suffers for it. Exercise library size comes at the cost of curation quality. Elite subscription is expensive. The "community" features feel bolted on. |
| **Positioning** | "The comprehensive bodybuilding companion." |

#### Fitbod

| Attribute | Detail |
|-----------|--------|
| **Users** | 5M+ downloads |
| **Monthly downloads** | Moderate |
| **Monthly revenue** | Not publicly disclosed |
| **Pricing** | Free trial (3 workouts) / $15.99/mo or $95.99/yr / $359.99 lifetime |
| **Platform** | iOS-first, Android secondary |
| **Core strength** | Best AI workout generation in the market today. 1,600+ exercise library. Adapts to equipment, goals, and fatigue. Recovery-aware programming. Strong brand in the "intelligent training" niche. |
| **Core weakness** | Expensive. No meaningful free tier (3 workouts then paywall). Algorithm is a black box -- users have no visibility into why the AI chose specific weights or exercises. Android experience is weaker. Targets all fitness, not strength-specific. |
| **Positioning** | "Your AI personal trainer." |

#### Gymshark Training

| Attribute | Detail |
|-----------|--------|
| **Users** | Large (brand-driven), exact numbers undisclosed |
| **Monthly downloads** | High (brand halo effect) |
| **Monthly revenue** | $0 (free app, monetized through apparel) |
| **Pricing** | 100% free |
| **Platform** | iOS and Android |
| **Core strength** | Completely free. Backed by a massive brand (Gymshark). 1,000+ workouts led by Gymshark athletes. Good for beginners who want guided programs. |
| **Core weakness** | Not a serious tracking tool. No progressive overload logic. No AI personalization based on training history. Exists to sell Gymshark apparel, not to be the best workout tracker. Limited analytics. No set/rep/weight logging sophistication. |
| **Positioning** | "Free workouts from the Gymshark community." |

### 2.2 Positioning Matrix

```
                    AI/Intelligence
                         HIGH
                          |
                  Fitbod  |  Deep Reps (target)
                          |
    Logger ---------------+---------------- Planner
                          |
              Strong      |  JEFIT
              Hevy        |  Gymshark Training
                          |
                         LOW
```

**Horizontal axis:** Logger (records what you did) vs. Planner (tells you what to do).
**Vertical axis:** Low AI/intelligence vs. High AI/intelligence.

Deep Reps targets the upper-right quadrant: high intelligence, plan-forward but user-controlled. Fitbod is the only current occupant of this quadrant, and it is expensive, iOS-first, and opaque in its algorithm. The opportunity is clear.

### 2.3 Competitive Gaps Deep Reps Exploits

| Gap | Incumbents | Deep Reps Approach |
|-----|------------|--------------------|
| AI plan generation on Android | Fitbod is iOS-first; Strong/Hevy have none | Android-native, Gemini-powered, history-aware plans |
| Exercise science credibility | Most libraries are crowdsourced or developer-assembled | CSCS-curated and validated library with biomechanics data |
| Plan transparency | Fitbod's algorithm is a black box | Plan is a visible suggestion. User sees what the AI proposed and can modify freely. AI learns from actuals. |
| Offline reliability | Most AI features require connectivity | Offline-first architecture. Cached plans and experience-level baselines as fallback. |
| 100% free app | Strong locks features after 3 workouts; Fitbod charges $96/year | Everything free, no restrictions (see Section 4) |

---

## 3. Target Audience Segmentation

### Segment 1: "The Dedicated Intermediate" (Primary)

| Attribute | Detail |
|-----------|--------|
| **Demographics** | Age 22-35, male-skewing (65/35 M/F), urban/suburban, employed, $40K-$80K income. Android user (by platform choice or cost). |
| **Psychographics** | Trains 3-5x/week consistently. Has been lifting 6-24 months. Understands the basics (progressive overload, compound movements) but doesn't program their own training. Follows fitness content on YouTube/Instagram. Values efficiency -- wants to get in, train smart, get out. |
| **Pain points** | Currently uses spreadsheets, notes apps, or Strong's free tier. Knows they should be progressing but doesn't have a structured plan. Finds existing apps either too simple (just a logger) or too expensive/opaque (Fitbod). Wants guidance without giving up control. |
| **Value driver** | Wants structured progression and AI-driven plans. Currently uses spreadsheets or Strong's limited free tier. Deep Reps gives them everything for free — AI plans, templates, progress tracking — with no paywall. |
| **Size estimate** | ~40% of total addressable users. This is the core growth engine. |

### Segment 2: "The Structured Beginner" (Secondary)

| Attribute | Detail |
|-----------|--------|
| **Demographics** | Age 18-28, fairly even gender split (55/45 M/F), students and early-career professionals. Android user (often by budget). First or second year of gym membership. |
| **Psychographics** | Motivated but overwhelmed. Watches form videos but doesn't know how to structure a workout. May have tried Gymshark Training or generic YouTube programs. Wants to "do it right" and avoid injury. Cares about learning, not just logging. |
| **Pain points** | Doesn't know what weight to start with. Doesn't know how many sets to do. Afraid of doing exercises wrong. Existing apps assume you already know what you're doing. Fitbod would solve this but costs $96/year -- too much for a beginner unsure if they'll stick with it. |
| **Value driver** | AI-generated plans with guardrails remove the "what should I do?" paralysis. Deep Reps gives beginners the same plan quality Fitbod charges $96/year for — completely free. High churn risk in first 30 days but high retention value if kept past 90 days. |
| **Size estimate** | ~35% of total addressable users. High volume, lower initial engagement, but the pipeline for Segment 1. |

### Segment 3: "The Self-Coached Advanced Lifter" (Tertiary)

| Attribute | Detail |
|-----------|--------|
| **Demographics** | Age 25-40, heavily male (80/20 M/F), higher income ($60K-$120K). May own both iOS and Android devices. 2+ years of consistent training. |
| **Psychographics** | Programs their own training or follows a coach. Knows exactly what they're doing. Uses a workout tracker primarily as a data repository -- logging, progress charts, 1RM tracking, volume analytics. May export data to spreadsheets. Values data integrity above all else. |
| **Pain points** | Strong is the closest fit but feels stale. JEFIT is bloated. Wants deep analytics (estimated 1RM trends, weekly volume per muscle group, training frequency heatmaps) without the noise. Doesn't need AI to tell them what to do but may appreciate AI as a "second opinion" or for deload week suggestions. |
| **Value driver** | Deep analytics (1RM trends, volume tracking, frequency heatmaps) and data integrity. These users evaluate tools on data quality, not price. Deep Reps earns their loyalty by being the best free data tool in the space. |
| **Size estimate** | ~25% of total addressable users. Smallest segment but strongest word-of-mouth influence and highest engagement per user. |

---

## 4. Business Model

**Deep Reps is a 100% free app.** No subscriptions, no ads, no paywalls, no freemium tiers, no in-app purchases.

Every feature — AI plan generation, full exercise library, unlimited templates, progress analytics, personal records — is available to every user from day one.

### 4.1 Rationale

- **Removes conversion friction.** No paywall means no drop-off at the paywall. Every install gets the full product. This maximizes retention and word-of-mouth.
- **Competitive positioning.** Fitbod charges $96/year. Strong locks features after 3 workouts. JEFIT shows ads. Deep Reps is the serious, free alternative. "Free" is itself a differentiator in a market where every competitor monetizes aggressively.
- **Passion project.** This is a founder-funded tool, not a revenue business at launch. The goal is product-market fit and a loyal user base, not immediate revenue.
- **No ads, ever.** Ads in a workout logging app are a UX disaster. Sweaty hands, focus on the workout, time between sets — an interstitial ad destroys trust.

### 4.2 Sustainability

Operating costs scale with usage (primarily Gemini API):
- 1K DAU: ~$150/month
- 10K DAU: ~$940/month (Gemini API: ~$600)
- 100K DAU: ~$8,340/month (Gemini API: ~$6,000)

The founder must commit to funding operating costs through product-market fit validation. If the app achieves strong retention (D30 > 15%) and meaningful scale (50K+ DAU), monetization options to explore at 12+ months include: sponsorships, equipment brand partnerships, optional premium analytics, or B2B licensing.

### 4.3 What This Changes

- No paywall screens in the app
- No trial/subscription logic in the codebase
- No billing infrastructure needed
- No conversion funnel analytics (subscription events removed from analytics plan)
- Marketing emphasizes "free" as a key differentiator
- All features available to all users — no feature gating

---

## 5. MVP Scope Definition

### 5.1 RICE Scoring

RICE = (Reach x Impact x Confidence) / Effort

Scale: Reach (users/quarter), Impact (0.25 minimal, 0.5 low, 1 medium, 2 high, 3 massive), Confidence (0-100%), Effort (person-weeks).

| Feature | Reach | Impact | Confidence | Effort (pw) | RICE Score | Phase |
|---------|-------|--------|------------|-------------|------------|-------|
| Workout logging (sets, reps, weight) | 10,000 | 3 | 95% | 6 | 4,750 | **MVP** |
| Exercise library (browse + detail views) | 10,000 | 2 | 90% | 4 | 4,500 | **MVP** |
| Muscle group selection + exercise picker | 10,000 | 2 | 90% | 3 | 6,000 | **MVP** |
| AI plan generation (Gemini) | 8,000 | 3 | 80% | 8 | 2,400 | **MVP** |
| Onboarding (experience level, units, profile) | 10,000 | 2 | 95% | 2 | 9,500 | **MVP** |
| Basic progress tracking (weight per exercise over time) | 7,000 | 2 | 85% | 4 | 2,975 | **MVP** |
| Rest timer | 8,000 | 1 | 90% | 2 | 3,600 | **MVP** |
| Workout templates (save/load) | 6,000 | 1 | 85% | 3 | 1,700 | **MVP** |
| Workout complete summary | 7,000 | 1 | 80% | 3 | 1,867 | **MVP** |
| Auto-ordered exercise sequencing | 7,000 | 0.5 | 80% | 2 | 1,400 | **MVP** |
| Offline fallback (cached/baseline plans) | 5,000 | 2 | 75% | 4 | 1,875 | **MVP** |
| Per-exercise notes | 5,000 | 0.5 | 90% | 1 | 2,250 | **MVP** |
| Data persistence / crash recovery | 10,000 | 3 | 95% | 3 | 9,500 | **MVP** |
| Advanced analytics (1RM, volume, frequency) | 4,000 | 2 | 70% | 6 | 933 | **Phase 2** |
| Personal records detection + dashboard | 5,000 | 2 | 75% | 4 | 1,875 | **Phase 2** |
| Supersets / circuits | 3,000 | 1 | 80% | 4 | 600 | **Phase 2** |
| Mid-workout exercise add/remove/reorder | 4,000 | 1 | 85% | 3 | 1,133 | **Phase 2** |
| Strength milestones (vs. standards) | 3,000 | 1 | 70% | 3 | 700 | **Phase 2** |
| Data export (CSV) | 2,000 | 0.5 | 90% | 2 | 450 | **Phase 2** |
| Bodyweight trend tracking | 3,000 | 0.5 | 80% | 2 | 600 | **Phase 2** |
| Cloud sync | 5,000 | 2 | 60% | 12 | 500 | **Phase 3** |
| Wearable integration (Wear OS) | 2,000 | 1 | 50% | 10 | 100 | **Phase 3** |
| Custom exercises | 3,000 | 1 | 70% | 5 | 420 | **Phase 3** |
| Multi-language support | 4,000 | 1 | 60% | 8 | 300 | **Phase 3** |
| iOS port | 8,000 | 2 | 50% | 30 | 267 | **Phase 3** |

### 5.2 MVP Feature Set (Launch Cut Line)

**In MVP (13 features):**

1. Onboarding (experience level selection, unit preference, optional profile)
2. Muscle group selection screen
3. Exercise picker per selected group (with detail views)
4. Exercise library with CSCS-curated detail cards (name, description, anatomy diagram, pros, tips, equipment, isolation level)
5. Auto-ordered exercise sequencing (compounds first)
6. AI plan generation via Gemini (warm-up + working sets with weights/reps)
7. Active workout logging (set-by-set: weight, reps, set type, done checkbox)
8. Rest timer (configurable, notification on expire)
9. Per-exercise notes
10. Workout complete summary (duration, volume, tonnage)
11. Workout templates (save/load, unlimited)
12. Basic progress tracking (weight progression chart per exercise, session history list)
13. Data persistence with crash recovery (auto-save per completed set)
14. Offline fallback (cached plan, baseline plan, manual entry)

**Deferred to Phase 2 (post-launch, months 1-3):**

- Advanced analytics (1RM trends, volume load charts, weekly volume per group, training frequency)
- Personal records detection and dashboard
- Supersets and circuits
- Mid-workout exercise modification (add/remove/reorder)
- Strength milestones relative to experience level
- Data export (CSV)
- Bodyweight trend tracking

**Deferred to Phase 3 (months 4-12+):**

- Cloud sync across devices
- Wearable integration (Wear OS)
- Custom exercise creation
- Multi-language support
- iOS port

### 5.3 MVP Rationale

The MVP must deliver the complete core loop: **select -> plan -> log -> review**. Anything that breaks this loop is MVP. Anything that enhances it is Phase 2+.

The critical insight: AI plan generation is MVP, not Phase 2. Without it, Deep Reps launches as "another workout logger" in a market with Strong (12 years of polish) and Hevy (10M users). The AI plan is the differentiator. Ship it on day one or don't ship at all.

---

## 6. KPI Framework

### 6.1 North Star Metric

**Workouts completed per week per active user.**

Not DAU. Not downloads. Not revenue. A completed workout means the user went through the full loop: selected exercises, received (or skipped) a plan, logged at least one set, and hit "finish." This metric directly correlates with retention, perceived value, and word-of-mouth.

**Target:** 2.5 workouts/week/active user at steady state (12 months).

### 6.2 Metric Targets

#### Acquisition

| Metric | Launch (Month 1) | 3 Months | 6 Months | 12 Months |
|--------|-------------------|----------|----------|-----------|
| Monthly downloads | 5,000 | 15,000 | 40,000 | 100,000 |
| Cumulative installs | 5,000 | 35,000 | 120,000 | 500,000 |
| Organic vs. paid split | 80/20 | 60/40 | 50/50 | 40/60 |
| CAC (paid) | $2.50 | $2.00 | $1.80 | $1.50 |

#### Activation

| Metric | Launch | 3 Months | 6 Months | 12 Months |
|--------|--------|----------|----------|-----------|
| Onboarding completion rate | 60% | 70% | 75% | 80% |
| First workout completed (within 48h of install) | 30% | 40% | 45% | 50% |
| First AI plan generated (within 48h) | 25% | 35% | 40% | 45% |

#### Retention

| Metric | Launch | 3 Months | 6 Months | 12 Months |
|--------|--------|----------|----------|-----------|
| D1 retention | 25% | 30% | 35% | 40% |
| D7 retention | 12% | 16% | 20% | 22% |
| D30 retention | 5% | 8% | 10% | 12% |
| Weekly active users (WAU) | 1,500 | 8,000 | 25,000 | 80,000 |
| Workouts/week/active user | 1.5 | 2.0 | 2.3 | 2.5 |

Industry context: Average D1 retention for fitness apps is 20-25%. Average D30 is 4-6%. The 12-month targets above represent top-quartile performance.

#### Engagement Quality

| Metric | Launch | 3 Months | 6 Months | 12 Months |
|--------|--------|----------|----------|-----------|
| AI plan usage rate (% of workouts) | 40% | 55% | 65% | 70% |
| Plans modified before starting | 50% | 40% | 35% | 30% |
| Sets logged per workout (avg) | 12 | 14 | 16 | 16 |
| Template save rate | 10% | 20% | 25% | 30% |
| App store rating (rolling 30-day) | 4.0 | 4.2 | 4.3 | 4.5 |

### 6.3 Leading Indicators (Weekly Dashboard)

These are monitored weekly to catch problems before they manifest in lagging metrics:

| Indicator | Signal |
|-----------|--------|
| AI plan generation requests / day | Proxy for engagement with core differentiator |
| Plans modified before starting workout | If >60%, the AI quality needs work |
| Sets logged per workout | Dropping = users are shortcutting or abandoning mid-workout |
| Crash-free session rate | Must stay above 99.5%. Below 99% is a P0. |
| App store rating (rolling 30-day) | Below 4.0 triggers immediate investigation |
| Funnel drop-off: group select -> exercise select -> plan generate -> workout start | Identifies UX bottlenecks in the core loop |

---

## 7. Risk Assessment

### Top 10 Risks

| # | Risk | Probability | Impact | Severity | Mitigation |
|---|------|-------------|--------|----------|------------|
| 1 | **Gemini API quality degrades or pricing changes unfavorably** | Medium (40%) | High | Critical | Provider interface abstraction (already planned). Pre-negotiate API pricing tiers. Maintain working integration with at least one fallback LLM (e.g., Claude, GPT-4o-mini). Budget 15% above projected API costs. |
| 2 | **D30 retention below 5% after 3 months** | Medium (45%) | High | Critical | Invest in onboarding optimization from day one. Implement push notification re-engagement (post-rest-day reminders, PR celebrations). Run retention cohort analysis weekly. If the core loop isn't retaining, investigate AI plan quality, onboarding friction, and workout UX before assuming the product concept is wrong. |
| 3 | **AI-generated plans are inaccurate or unsafe** | Medium (35%) | Very High | Critical | CSCS validates all prompt templates and reviews sample outputs before launch. Implement guardrails: max weight jump per session (e.g., no more than 10% increase), mandatory warm-up sets, volume ceiling per muscle group. Automated testing of plan outputs against safety rules. |
| 4 | **Hevy or Strong ships AI plan generation** | High (60%) | Medium | High | Speed to market matters. Deep Reps must launch with AI plans before incumbents add them. If they do ship AI, our CSCS-validated quality and Android-native UX are the moats, not the AI itself. Incumbents bolting on AI to an existing app will likely compromise UX. |
| 5 | **Android fragmentation causes device-specific bugs** | High (55%) | Medium | High | Firebase Test Lab for automated testing across 20+ device/OS combinations. Define a support floor (Android 10+, covering ~95% of active devices). QA engineer runs manual tests on 5 representative devices (budget, mid-range, flagship, Samsung, Pixel). |
| 6 | **Gemini API costs exceed budget at scale** | Medium (40%) | High | High | Monitor cost-per-plan-generation weekly. If costs exceed $0.01/plan, investigate prompt optimization, response caching, or switching to a smaller model for simple plans. Set hard spending caps per month. At 100K DAU, API costs could reach $6K/month — founder must budget accordingly. |
| 7 | **Users don't engage with AI plans (prefer manual logging)** | Medium (35%) | Medium | Medium | Monitor AI plan usage rate. If <30% of workouts use AI plans at month 3, investigate: are plans inaccurate? Are users unaware? Is the UX friction too high? A/B test plan presentation (inline vs. separate screen). |
| 8 | **CSCS hire is delayed or poor fit** | Low (20%) | Very High | High | Begin recruiting immediately. The CSCS is a blocker for the exercise library, which is a blocker for everything else. Have a shortlist of 3 candidates. Consider contract engagement if full-time hire takes too long. |
| 9 | **Gemini API latency makes plan generation feel slow** | Medium (40%) | Medium | Medium | Show loading state with exercise-specific tips during generation. Cache plans aggressively -- if the user runs the same exercises as last time, serve modified cached plan instantly. Target <3 second plan generation P95. If Gemini can't hit this, switch to a faster model for plan generation specifically. |
| 10 | **Google Play Store ranking is invisible at launch** | High (65%) | Medium | Medium | ASO strategy from day one: keyword research (workout tracker, gym log, strength training, AI workout), screenshot A/B testing, localized listing. Seed 50+ legitimate reviews from beta testers before public launch. Budget for Google Ads App Campaigns at $3K/month from launch. |

### Risk Heat Map

```
              Low Impact    Medium Impact    High Impact    Very High Impact
High Prob.  |             | #4, #5, #10     |              |
Med Prob.   |             | #7, #9          | #1, #2, #6   | #3
Low Prob.   |             |                 | #8           |
```

---

## 8. Go-to-Market Timeline

### Phase 0: Foundation (Now -- Week 8)

| Week | Milestone | Owner | Deliverable |
|------|-----------|-------|-------------|
| 1-2 | Finalize team hiring for MVP phase (7 roles) | CEO | Signed offers for CSCS, Lead Android Dev, Mid Android Dev, UI/UX Designer, Backend Dev, QA |
| 1-2 | Competitive deep-dive: install and use Strong, Hevy, JEFIT, Fitbod for 2 weeks | CEO + CSCS | Competitive UX audit document with screenshots and friction points |
| 2-4 | CSCS builds exercise library v1 (7 muscle groups, ~80-100 exercises) | CSCS | Exercise database spreadsheet: name, group, equipment, isolation level, description, tips, pros, primary/secondary muscles |
| 2-4 | Architecture design: tech stack selection, data model, offline-first strategy | Lead Android Dev | Architecture Decision Record (ADR) document |
| 3-6 | UI/UX design: wireframes for entire core loop | UI/UX Designer | Figma wireframes: onboarding, group select, exercise pick, plan view, workout logging, summary, progress |
| 4-6 | AI prompt engineering: design and test Gemini prompts with CSCS | CSCS + Lead Dev | Prompt templates with sample inputs/outputs, safety guardrail rules |
| 6-8 | UI/UX design: high-fidelity mockups and design system | UI/UX Designer | Figma high-fidelity designs, Material Design 3 component library, dark theme |
| 8 | **Gate: Go/No-Go for development start** | CEO | Exercise library complete, architecture approved, designs approved, AI prompts validated |

### Phase 1: MVP Development (Weeks 9-22, ~14 weeks)

| Week | Milestone | Owner |
|------|-----------|-------|
| 9-10 | Project scaffolding: CI/CD, repo structure, dependency injection, Room database setup | Lead Android Dev |
| 9-12 | Exercise library implementation (database, browse UI, detail views) | Mid Android Dev |
| 10-14 | Workout logging core (set entry, weight/reps, timer, auto-save) | Lead Android Dev |
| 11-14 | Onboarding flow + user profile | Mid Android Dev |
| 13-16 | AI plan generation integration (Gemini API, provider interface, offline fallback) | Lead Android Dev + Backend Dev |
| 14-16 | Muscle group selection + exercise picker + auto-ordering | Mid Android Dev |
| 15-18 | Workout templates (save/load) | Mid Android Dev |
| 16-18 | Basic progress tracking (weight chart per exercise, session history) | Lead Android Dev |
| 17-19 | Workout complete summary screen | Mid Android Dev |
| 18-20 | Per-exercise notes, crash recovery, data persistence hardening | Lead Android Dev |
| 18-22 | QA: full regression testing, device fragmentation testing, edge cases | QA Engineer |
| 20-22 | Bug fixes, performance optimization, polish | All Dev |
| 22 | **Gate: Internal alpha complete. Feature-complete MVP.** | CEO |

### Phase 2: Beta & Pre-Launch (Weeks 23-28, ~6 weeks)

| Week | Milestone | Owner |
|------|-----------|-------|
| 23 | Closed beta launch (Google Play internal testing track, 50-100 testers) | CEO + QA |
| 23-24 | Analytics instrumentation (event taxonomy, Mixpanel/Amplitude setup) | Data Analyst + Lead Dev |
| 23-26 | Beta feedback collection and triage | CEO + QA |
| 24-26 | ASO preparation: store listing, screenshots, video, keyword optimization | Growth & Content Marketing |
| 25-27 | Critical bug fixes from beta feedback | Dev team |
| 26-28 | Open beta (Google Play open testing track, target 500-1,000 testers) | Growth & Content Marketing |
| 27-28 | Final polish, accessibility pass, and performance optimization | Lead Android Dev |
| 28 | **Gate: Launch readiness review. App store rating >= 4.0, crash-free rate >= 99.5%, core loop completion rate >= 60%.** | CEO |

### Phase 3: Public Launch (Week 29)

| Day | Action | Owner |
|-----|--------|-------|
| Launch Day | Google Play production release (staged rollout: 10% -> 50% -> 100% over 3 days) | DevOps + Lead Dev |
| Launch Day | Press outreach: fitness tech blogs, Android publications | Growth & Content Marketing |
| Launch Day | Social media announcement + influencer seeding (5-10 micro-influencers, 10K-100K followers) | Growth & Content Marketing |
| Launch Week | Google Ads App Campaigns live ($3K/month budget) | Growth & Content Marketing |
| Launch Week | Daily monitoring: crash rates, store reviews, funnel metrics, server load | All |

### Phase 4: Post-Launch Growth (Weeks 30-52)

| Timeframe | Focus | Key Actions |
|-----------|-------|-------------|
| **Month 1 (Weeks 30-33)** | Stabilize and fix | Fix launch bugs. Respond to every app store review. Monitor D1/D7 retention. Optimize onboarding based on funnel data. |
| **Month 2 (Weeks 34-37)** | Retention push | Ship Phase 2 features: personal records detection, advanced analytics (1RM trends). A/B test push notification re-engagement. Optimize AI plan quality based on logged actual-vs-plan data. |
| **Month 3 (Weeks 38-42)** | Feature expansion | Ship supersets/circuits. Ship data export. Deep retention cohort analysis. A/B test onboarding variants. Optimize AI plan quality based on actual-vs-plan data from 3 months of usage. |
| **Months 4-6 (Weeks 43-52)** | Scale | Increase organic acquisition efforts (content marketing, community). Ship strength milestones, bodyweight tracking. Begin cloud sync architecture. Evaluate iOS port feasibility based on product-market fit signals (D30 > 15%, 50K+ DAU). Hire UX Researcher for retention deep-dive. |

### Key Dates Summary

| Milestone | Target Date | Week |
|-----------|-------------|------|
| Team hired and onboarded | Week 4 | March 2026 |
| Development start | Week 9 | April 2026 |
| Internal alpha (feature-complete) | Week 22 | July 2026 |
| Closed beta | Week 23 | July 2026 |
| Open beta | Week 26 | August 2026 |
| Public launch | Week 29 | September 2026 |
| 10K cumulative installs | Week 35 | October 2026 |
| D30 retention > 10% | Week 41 | December 2026 |
| 100K cumulative installs | Week 52 | March 2027 |

---

## Appendix: Sources & References

### Competitive Intelligence

- [Strong App Official](https://www.strong.app/)
- [Strong App Review 2026 -- PRPath](https://www.prpath.app/blog/strong-app-review-2026.html)
- [Strong vs Hevy Comparison 2026 -- GymGod](https://gymgod.app/blog/strong-vs-hevy)
- [Hevy Pricing](https://hevy.com/pricing)
- [Hevy App Review 2026 -- PRPath](https://www.prpath.app/blog/hevy-app-review-2026.html)
- [JEFIT Official](https://www.jefit.com/)
- [Fitbod Review 2026 -- Fitness Drum](https://fitnessdrum.com/fitbod-review/)
- [Fitbod Pricing](https://dr-muscle.com/fitbod-cost/)
- [Gymshark Training App -- Tom's Guide](https://www.tomsguide.com/wellness/fitness/gymshark-training-app-review-effective-workouts-for-free)

### Market Data

- [Fitness App Market Size & Share -- Grand View Research](https://www.grandviewresearch.com/industry-analysis/fitness-app-market)
- [Fitness App Revenue and Usage Statistics 2026 -- Business of Apps](https://www.businessofapps.com/data/fitness-app-market/)
- [2026 Fitness App Market Statistics -- Wellness Creatives](https://www.wellnesscreatives.com/fitness-app-market/)

### Retention & Subscription Benchmarks

- [App Retention Benchmarks 2026 -- Enable3](https://enable3.io/blog/app-retention-benchmarks-2025)
- [Retention Metrics for Fitness Apps -- Lucid](https://www.lucid.now/blog/retention-metrics-for-fitness-apps-industry-insights/)
- [Health & Fitness App Benchmarks 2026 -- Business of Apps](https://www.businessofapps.com/data/health-fitness-app-benchmarks/)
- [State of Subscription Apps 2025 -- RevenueCat](https://www.revenuecat.com/state-of-subscription-apps-2025/)
- [Fitness Apps Are Highly Monetizable -- Athletech News](https://athletechnews.com/fitness-apps-monetizable-winner-take-all-or-most/)
