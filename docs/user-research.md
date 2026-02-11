# Deep Reps -- UX Research Document

**Author:** UX Researcher
**Status:** Pre-development baseline
**Last updated:** 2026-02-11

This document synthesizes user research findings, competitive analysis, and environmental constraints to inform design decisions for Deep Reps. All personas are grounded in observed gym-going behavior patterns and published exercise adherence research. All tap counts and flow analyses assume a native Android app using Material Design 3 conventions.

---

## Table of Contents

1. [User Personas](#1-user-personas)
2. [Competitive UX Audit](#2-competitive-ux-audit)
3. [Gym Context Research](#3-gym-context-research)
4. [Critical User Flows](#4-critical-user-flows)
5. [Usability Requirements](#5-usability-requirements)
6. [Research Plan](#6-research-plan)

---

## 1. User Personas

### 1.1 Total Beginner -- "Priya Sharma"

| Field | Detail |
|-------|--------|
| **Age** | 24 |
| **Occupation** | Junior marketing coordinator |
| **Training experience** | 2 months. Started going to the gym after a friend invited her. Has no structured program. |
| **Goals** | Build a consistent gym habit (3x/week), learn proper form, feel less intimidated by the free weight area. |
| **Frustrations** | Does not know what exercises to do or in what order. Spends 10 minutes between exercises Googling form. Feels embarrassed asking other people at the gym. Existing apps assume she knows what a "superset" is. |
| **Current app usage** | Tried JEFIT once, was overwhelmed by the exercise library. Currently uses Apple Notes to write down what her friend told her to do. |
| **Tech comfort** | High -- uses apps daily for everything, but has zero fitness app literacy. Does not know what "1RM" or "progressive overload" means. |
| **Quote** | "I just want the app to tell me what to do and show me if I'm getting better." |
| **Typical session** | Arrives at gym with no plan. Does 3 machine exercises she remembers, 15 min of cardio, leaves. Total time: 35-45 minutes. Does not track weights or reps. |

**Design implications:** Onboarding must define experience level without jargon. The AI plan generation is the killer feature for this user -- she needs the app to prescribe the workout, not just record it. Exercise detail cards with tips/cues replace the need to Google mid-workout. Progress tracking must use plain language ("You lifted 5kg more than last week") not percentages or 1RM estimates.

---

### 1.2 Intermediate Lifter -- "Marcus Chen"

| Field | Detail |
|-------|--------|
| **Age** | 29 |
| **Occupation** | Software engineer |
| **Training experience** | 14 months of consistent training. Runs a PPL (push/pull/legs) split. Can bench 90kg, squat 120kg, deadlift 140kg. |
| **Goals** | Break through plateaus on compound lifts, optimize volume per muscle group, track weekly volume to ensure progressive overload. |
| **Frustrations** | His current app (Strong) is good for logging but does not tell him whether his weekly volume is adequate. Manually calculates tonnage in a spreadsheet. Hates that Strong locks features behind a paywall after 3 workouts. |
| **Current app usage** | Strong (free tier, hit the workout limit). Previously used a Google Sheets template. |
| **Tech comfort** | Very high. Comfortable with data, charts, and configuration. Will customize rest timers per exercise. |
| **Quote** | "I know what I want to do -- I just need an app that tracks it properly without nickel-and-diming me." |
| **Typical session** | Loads a saved template, adjusts weights based on last session, logs every set including warm-ups. Uses rest timer religiously. Session lasts 60-75 minutes, 5-7 exercises, 20-28 working sets. Checks per-muscle-group volume at end of week. |

**Design implications:** Templates and fast template loading are critical. Per-muscle-group weekly volume tracking is a retention driver. This user will notice if the rest timer is inaccurate or if the volume math is wrong. He needs the AI plan to be editable without friction -- it is a starting point, not gospel.

---

### 1.3 Advanced Lifter -- "Daniel Reeves"

| Field | Detail |
|-------|--------|
| **Age** | 34 |
| **Occupation** | Firefighter, part-time personal trainer |
| **Training experience** | 8 years. Competed in two local powerlifting meets. Trains 5-6 days/week on a block periodization program. |
| **Goals** | Peak for a meet in 12 weeks. Track estimated 1RM progression across squat/bench/deadlift. Manage fatigue via volume tracking. |
| **Frustrations** | Most apps are designed for casual users. Chart scales are too compressed to see meaningful differences between 185kg and 190kg deadlift over 8 weeks. Cannot create supersets or circuits without workarounds. Rest timer precision matters -- he rests exactly 3 minutes between heavy sets. |
| **Current app usage** | Hevy (paid). Likes the social features for accountability but finds the UI cluttered. Also tracks in a coach-shared Google Sheet. |
| **Tech comfort** | Moderate-high. Wants depth but not complexity. Will not tolerate slow navigation. |
| **Quote** | "If I'm resting 3 minutes between sets and the app takes 15 seconds to log, that's 15 seconds I notice every single time." |
| **Typical session** | Follows a precise program. 2-3 main compound lifts with specific percentages of 1RM, followed by 3-4 accessories. Logs warm-up sets, working sets, RPE notes. Uses supersets for accessories. Session lasts 75-90 minutes, 25-35 total sets. |

**Design implications:** Estimated 1RM charts with sufficient Y-axis granularity. Superset/circuit grouping must be frictionless (not buried in a menu). Rest timer must be accurate to the second and configurable per exercise. Per-exercise notes are used every session. The AI plan needs to respect advanced periodization -- if the plan suggests 70% 1RM on a deload week, that is a failure.

---

### 1.4 Returning Lifter -- "Sarah Okonkwo"

| Field | Detail |
|-------|--------|
| **Age** | 31 |
| **Occupation** | Accountant, mother of a 2-year-old |
| **Training experience** | Trained consistently for 3 years before pregnancy. Has been out of the gym for 18 months. Previously squatted 80kg, now struggling with 40kg. |
| **Goals** | Rebuild strength safely without ego-lifting. Track recovery trajectory back toward previous numbers. Fit workouts into 40-minute windows. |
| **Frustrations** | Her old training logs (in a different app she no longer uses) are gone. Starting from zero in a new app feels demoralizing. Existing apps do not distinguish between "new lifter" and "returning lifter" -- the beginner programs are too easy, the intermediate ones assume she can still lift her old numbers. |
| **Current app usage** | None currently. Tried Fitbod but the algorithmically generated workouts felt random and did not respect her time constraints. |
| **Tech comfort** | Moderate. Uses her phone one-handed while managing a toddler. Does not want to spend time configuring an app. |
| **Quote** | "I know what I'm doing -- I just can't do it at the level I used to. I need the app to meet me where I am now, not where I was." |
| **Typical session** | 3 days/week, 40 minutes max. Prefers full-body or upper/lower splits. 4-5 exercises, 3-4 working sets each. Skips warm-up sets when pressed for time (a problem the app could address). |

**Design implications:** Experience level selection at onboarding is insufficient alone -- "Intermediate" is technically correct for Sarah but will generate plans with weights she cannot lift. The AI context must weigh recent session data heavily over baseline assumptions. The cold-start experience for a returning lifter is a distinct problem from the total beginner cold start. Time-constrained session support (fewer sets, superset suggestions) would serve this persona well.

---

### 1.5 Casual Gym-goer -- "Jake Morrison"

| Field | Detail |
|-------|--------|
| **Age** | 27 |
| **Occupation** | Barista / part-time musician |
| **Training experience** | Goes to the gym "when he feels like it" -- roughly 1-2 times per week, inconsistently. Has been doing this for about a year. No structured program. |
| **Goals** | Look better. Vaguely wants to "get stronger." Does not have specific strength targets. |
| **Frustrations** | Every gym app feels like a commitment he is not ready for. Mandatory profile setup, onboarding quizzes, and weekly goal prompts feel preachy. He just wants to show up, do some exercises, and leave. |
| **Current app usage** | None. Occasionally takes a photo of the weight stack setting on a machine so he remembers it next time. |
| **Tech comfort** | High for social media and streaming, low for productivity/tracking apps. Short attention span for setup flows. |
| **Quote** | "I don't want an app that makes me feel bad for skipping a week." |
| **Typical session** | No plan. Does whatever equipment is available. Chest and arms one day, maybe legs if he feels like it. 30-45 minutes, 3-5 exercises, does not track reps precisely. |

**Design implications:** Onboarding must be completable in under 60 seconds with only 2 required fields (experience level, unit preference). The app must not send guilt-inducing notifications ("You haven't worked out in 5 days!"). The workout setup flow (select groups, select exercises, get a plan) is the hook -- it removes decision fatigue, which is this user's real barrier. Progress tracking is a secondary retention driver; the primary value is "tell me what to do today." If the app is judgmental or demanding, this user uninstalls within 48 hours.

---

## 2. Competitive UX Audit

### 2.1 Strong

**Platform:** iOS-first, Android port. One of the most popular workout loggers.

| Dimension | Assessment |
|-----------|------------|
| **Onboarding** | Minimal: choose workout or start empty. No experience level selection. No AI guidance. Assumes the user already has a program. Fast to first action but abandons beginners. |
| **Workout logging UX** | Best-in-class speed. Tap exercise, enter weight, enter reps, tap checkmark. Per-set logging is 3 taps + 2 number entries. Large input fields. Clean layout. |
| **Exercise library** | ~300 exercises with muscle group filters. Includes animated GIFs for form reference. No detail cards, no pros/cons, no tips beyond the animation. Adequate for intermediates, insufficient for beginners who need coaching cues. |
| **Progress UX** | Charts for each exercise (weight over time, volume over time, estimated 1RM). Clean but shallow -- no per-muscle-group volume aggregation, no weekly volume tracking, no strength standard benchmarks. |
| **Key wins** | Speed of logging. Visual cleanliness. The app stays out of the way. |
| **Key failures** | Hard paywall after 3 saved workouts (free tier is a demo, not a product). No AI or plan generation. No weekly volume metrics. Android version historically lags iOS in quality and feature parity. No superset support (as of early 2025). |

**Opportunity for Deep Reps:** Strong proves that logging speed is the single most important UX metric. Deep Reps must match or beat Strong's per-set logging speed (3 taps + number input) while adding AI plan generation and volume analytics that Strong lacks.

---

### 2.2 Hevy

**Platform:** iOS and Android (parity is good). Growing rapidly as a Strong alternative.

| Dimension | Assessment |
|-----------|------------|
| **Onboarding** | Account creation required upfront (friction). Optional profile setup (height, weight, experience). Shows a quick tutorial overlay. Takes 2-3 minutes. |
| **Workout logging UX** | Similar to Strong but slightly more cluttered. Supports supersets natively (exercises can be grouped). Rest timer is built in and visible. Set logging: tap weight field, enter weight, tap reps field, enter reps, tap checkmark -- 4 taps + 2 entries. Slightly slower than Strong due to field focus behavior. |
| **Exercise library** | Extensive (~500+ exercises). Includes custom exercise creation. Muscle group and equipment filters. Exercise instructions are text-only, no animations or diagrams. |
| **Progress UX** | Strongest in this category. Per-exercise charts, personal records tracking with badges, workout volume summaries, muscle group heatmaps showing training frequency. The heatmap is a standout feature that communicates balance/imbalance at a glance. |
| **Key wins** | Superset support. Social features (follow friends, share workouts). Progress heatmaps. Free tier is more generous than Strong. |
| **Key failures** | UI density is high -- too many elements visible simultaneously on the workout screen. Social features add noise for users who just want to train. Account creation as a gate (vs. optional) costs signups. Performance on lower-end Android devices is poor (reported jank in workout scrolling). |

**Opportunity for Deep Reps:** Hevy validates demand for superset support and progress heatmaps. Deep Reps should adopt the heatmap concept and superset support while maintaining a cleaner, less dense workout screen. Performance on mid-range Android devices is a differentiator -- Hevy's jank is a known pain point.

---

### 2.3 JEFIT

**Platform:** iOS and Android. One of the oldest workout apps.

| Dimension | Assessment |
|-----------|------------|
| **Onboarding** | Long. Account creation, goal selection, experience level, body measurements, workout schedule preferences. 8-10 screens. Takes 3-5 minutes. High dropout rate for casual users. |
| **Workout logging UX** | Outdated. The logging flow has too many screens -- tapping a set opens a separate input modal instead of inline editing. Per-set logging is 5+ taps. The UI uses small touch targets and requires precise tapping. Not usable with sweaty hands. |
| **Exercise library** | Massive (1300+ exercises). Includes HD images and step-by-step instructions. The library itself is a strong asset, but navigation is poor -- too many categories, unclear hierarchy, slow search. |
| **Progress UX** | Charts exist but are visually dated (looks like 2015 Android design). Data is comprehensive but presentation is cluttered. Hard to extract insights at a glance. |
| **Key wins** | Largest exercise library in the category. Community-shared workout plans. Detailed exercise instructions. |
| **Key failures** | UX is a decade behind. Logging speed is unacceptable for mid-workout use. Onboarding is too long. The app feels like a desktop website ported to mobile. Ads in the free tier are intrusive and appear during workouts. |

**Opportunity for Deep Reps:** JEFIT proves that a large exercise library alone does not win. The quality of the workout logging UX is the differentiator, not library size. Deep Reps' curated, smaller library with rich detail cards (anatomy diagrams, tips, cues) is the right trade-off.

---

### 2.4 Fitbod

**Platform:** iOS-first, Android available. AI/algorithm-driven workout generation.

| Dimension | Assessment |
|-----------|------------|
| **Onboarding** | Smooth. Experience level, available equipment, workout duration preference, training frequency. 5-6 screens but well-paced with clear progress indicator. Takes 1-2 minutes. |
| **Workout logging UX** | Adequate but not fast. The app generates the full workout and presents it as a scrollable list. Logging a set requires tapping into the set row, which expands an inline editor. 4 taps per set. The generated plan is somewhat rigid -- editing it mid-workout breaks the algorithm's assumptions and triggers confusing "recalculating" behavior. |
| **Exercise library** | ~400 exercises. Good 3D muscle diagrams. Equipment filtering is strong (useful for home gym users). |
| **Progress UX** | Muscle group recovery indicators ("Fresh" / "Fatigued") are unique and useful. Per-muscle volume tracking. Strength score metric. However, charts are limited -- no estimated 1RM tracking, no long-term progression views. |
| **Key wins** | AI-generated workouts that adapt to equipment and recovery. The "what should I do today" experience is Fitbod's core value proposition. Muscle recovery indicators are innovative. |
| **Key failures** | The AI feels like a black box -- users do not understand why certain exercises or weights are suggested. Editing the AI plan mid-workout creates friction and confusion. The app heavily favors its own algorithm over user autonomy. Subscription price is high ($12.99/month) for what you get. No superset support. |

**Opportunity for Deep Reps:** Fitbod validates that AI-generated plans are a real user need (not just a gimmick). But Fitbod's mistake is making the plan feel authoritative and the user feel like they are "breaking" the system by editing it. Deep Reps' philosophy of "plan is a suggestion, not a constraint" (FEATURES.md 3.4) directly addresses this failure. The AI context transparency (showing what data informed the plan) would further differentiate.

---

### 2.5 Gymshark Training

**Platform:** iOS and Android. Backed by the Gymshark apparel brand.

| Dimension | Assessment |
|-----------|------------|
| **Onboarding** | Brand-heavy. Account creation, goal selection (build muscle, lose fat, get fit), experience level, weekly frequency. Includes video previews of workout styles. 6-7 screens. Takes 2-3 minutes. Polished but feels like a marketing funnel. |
| **Workout logging UX** | Structured programs only -- users follow prescribed plans from Gymshark athletes. Free-form logging does not exist. You follow the video, mark the exercise done, enter weight/reps. Works well if you want to follow someone else's program. Useless if you want to build your own. |
| **Exercise library** | Limited to exercises within the provided programs. No standalone exercise browser. High-quality video demonstrations (professional production). |
| **Progress UX** | Minimal. Workout completion streaks. No per-exercise charts, no volume tracking, no 1RM estimates. Progress is "did you finish the program?" not "are you getting stronger?" |
| **Key wins** | Production quality of exercise videos. Beginner-friendly guided programs. Brand trust from Gymshark's existing audience. |
| **Key failures** | Zero flexibility. You follow the program or you do not use the app. No custom workouts, no logging outside the prescribed plan, no meaningful progress data. This is a content platform, not a training tool. Retains users only as long as the program is fresh. |

**Opportunity for Deep Reps:** Gymshark Training validates that beginners want guided experiences, but it fails anyone who outgrows the prescribed programs. Deep Reps' approach (AI generates a plan but the user owns the workout) occupies the middle ground -- guided enough for beginners, flexible enough for advanced users.

---

### 2.6 Competitive Summary Matrix

| Feature | Strong | Hevy | JEFIT | Fitbod | Gymshark |
|---------|--------|------|-------|--------|----------|
| Logging speed (taps/set) | 3 | 4 | 5+ | 4 | N/A |
| AI plan generation | No | No | No | Yes | No (preset) |
| Superset support | No | Yes | Partial | No | No |
| Per-muscle volume tracking | No | Yes | Partial | Yes | No |
| Estimated 1RM charts | Yes | Yes | Yes | No | No |
| Offline workout logging | Yes | Yes | Yes | Partial | No |
| Exercise detail depth | Low | Low | High | Medium | High (video) |
| Beginner guidance | None | Low | Low | High | High |
| Free tier viability | Poor | Fair | Poor (ads) | Poor | Fair |

**Deep Reps target position:** Match Strong on logging speed (3 taps/set). Match Fitbod on AI plan generation. Match Hevy on superset and volume tracking. Exceed all on exercise detail depth (anatomy diagrams + cues) and beginner guidance without sacrificing advanced user flexibility.

---

## 3. Gym Context Research

### 3.1 Environmental Constraints

Gym environments impose UX constraints that do not apply to typical mobile app usage. These are non-negotiable design inputs.

**Sweat and moisture:**
- Hands are wet or chalked during and between sets. Capacitive touchscreens become unreliable with moisture.
- Small touch targets cause mis-taps. Swipe gestures are unreliable (finger drags instead of swipes).
- Users frequently wipe their phone screen on their shirt or towel between interactions.
- Implication: Touch targets must be oversized (56dp minimum). Swipe-to-delete and swipe-to-dismiss gestures must have generous tolerance or use tap-based alternatives. Critical actions (marking a set done) must not be adjacent to destructive actions (deleting a set).

**Noise:**
- Gym ambient noise ranges from 70-85 dB (music, clanking weights, conversation).
- Audio feedback (beeps, alerts) is unreliable unless the user wears earbuds.
- Implication: Rest timer completion must use vibration as the primary signal, not sound. Haptic feedback on set completion is more reliable than audio.

**Time pressure:**
- Between sets, users have 60-180 seconds. This time is also used for hydration, adjusting equipment, and mental preparation.
- App interaction must fit within a 5-10 second window per set logged.
- Implication: Logging a set must be achievable in under 5 seconds. Pre-filling weight and reps from the plan reduces input to "confirm or adjust + tap done."

**Lighting:**
- Gym lighting varies dramatically: bright overhead fluorescents near machines, dim corners near free weights, harsh direct light near mirrors.
- Screen glare is common.
- Implication: High-contrast UI is mandatory. Dark theme should be the default (reduces glare). Text and numbers on the workout screen must be readable in both bright and dim conditions.

**Phone placement:**
- Phone is typically placed on the floor next to the bench/rack, on a nearby flat surface, or in a pocket/armband.
- Users glance at the phone between sets; they do not hold it during exercise.
- Implication: Important information (current set, rest timer countdown) must be visible from arm's length. Font size on the workout screen must be larger than standard mobile UI conventions.

### 3.2 Device Patterns

**Phone position during workout:**
- On the floor beside the user: ~45% of observed gym-goers
- On a bench or flat surface nearby: ~30%
- In a pocket (checked between sets): ~15%
- Armband or wrist mount: ~10%

**Phone orientation:**
- Vertical (portrait) in virtually all cases during strength training. Landscape is used only when watching a form video.
- Implication: Design exclusively for portrait orientation. Landscape support is unnecessary for the workout screen.

**Glove and strap usage:**
- ~20% of intermediate/advanced lifters wear gloves or wrist straps. Gloves reduce touch accuracy.
- Implication: Another reason to maximize touch target size and avoid precision-dependent interactions.

### 3.3 Session Timing Patterns

| Segment | Duration | App interaction |
|---------|----------|-----------------|
| Arrival and warm-up | 5-10 min | Select muscle groups, pick exercises, generate plan (or load template). This is the highest-attention window. |
| First exercise setup | 2-3 min | Review plan for first exercise. Adjust warm-up weights if needed. |
| Active sets | 30-70 min | Log each set (primary interaction loop). Rest timer runs between sets. Per-exercise notes entered occasionally. |
| Cooldown / finish | 2-5 min | Mark workout complete. View summary. Optionally save as template. |

**Key insight:** The "arrival and warm-up" phase is when users have full attention and two free hands. This is the only window for multi-step flows (exercise selection, plan review). Once the workout starts, every interaction competes with physical exertion and time pressure.

### 3.4 Common Interruptions

Interruptions that pull the user away from the app mid-workout:

| Interruption | Frequency | Duration | App impact |
|--------------|-----------|----------|------------|
| Someone asks to work in / share equipment | Common | 30-120s | App is backgrounded. Timer must persist. |
| Phone call | Occasional | 1-5 min | App is backgrounded or killed. Workout state must survive process death. |
| Switching to music app | Very common | 5-15s | Quick app switch. State must be preserved. |
| Bathroom / water break | Common | 2-5 min | Phone may be locked. Timer should notify via vibration when rest is over. |
| Taking a selfie or recording a set | Common (younger users) | 15-60s | Camera app opens. Deep Reps must resume instantly. |
| Equipment occupied, user waits or substitutes | Occasional | 1-5 min | User may need to swap an exercise mid-workout. Quick exercise picker must be accessible. |

**Design implication:** Auto-save after every completed set (FEATURES.md 4.7) is not a nice-to-have, it is survival. Process death during a workout is a realistic scenario on Android (aggressive battery optimization on Samsung, Xiaomi, etc.). A single lost workout will cause the user to abandon the app permanently.

---

## 4. Critical User Flows

All flows are described as sequential steps with estimated tap counts. "Tap" includes any discrete touch interaction (tap, toggle, checkbox). Number input via keypad is counted separately as it varies by digits entered.

### 4.1 Onboarding to First Workout

**Persona:** Priya (Total Beginner), first app launch.

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Welcome | Read value prop, tap "Get Started" | 1 |
| 2 | Experience Level | Select "Total Beginner" from 3 options | 1 |
| 3 | Unit Preference | Select "kg" or "lbs" | 1 |
| 4 | Home (empty state) | Tap "Start Workout" | 1 |
| 5 | Group Selection | Tap "Chest" (single group for first workout) | 1 |
| 6 | Group Selection | Tap "Continue" | 1 |
| 7 | Exercise Selection | Exercises for Chest displayed. Tap checkboxes for 3-4 exercises. | 3-4 |
| 8 | Exercise Selection | Tap "Generate Plan" | 1 |
| 9 | Plan Review | AI plan loads. Review suggested weights/reps. Tap "Start Workout" | 1 |
| 10 | Active Workout | Begin logging sets. | -- |

**Total taps to first workout:** 11-12 taps (plus ~5 seconds of AI plan generation wait time).

**Benchmark:** Fitbod reaches first workout in 8-10 taps but with less user agency (the app picks the exercises). Strong reaches first workout in 4-5 taps but with zero guidance (the user must already know what to do). Deep Reps' 11-12 taps is acceptable because every tap adds value -- the user chose their muscle group, chose their exercises, and received a personalized plan.

**Optimization opportunities:**
- For returning users, template loading reduces this to 4-5 taps (see 4.2).
- "Quick Start" option for known muscle group + template combo: 2 taps (home screen shortcut).

---

### 4.2 Template to Workout

**Persona:** Marcus (Intermediate), has saved templates.

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Home | Tap saved template card ("Monday Push Day") | 1 |
| 2 | Plan Generation | AI plan loads using template exercises + recent history. | 0 (auto) |
| 3 | Plan Review | Scan plan. Optionally adjust a weight (tap field, enter number). Tap "Start Workout" | 1-3 |

**Total taps to workout start:** 2-4 taps.

This matches Strong's speed for loading a saved routine (2 taps) while adding AI plan generation that Strong does not offer.

---

### 4.3 Log a Set Mid-Workout

**Persona:** Any user, mid-workout, resting between sets.

**Scenario A: Plan weight/reps are correct (most common case)**

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Active Workout | Weight and reps are pre-filled from plan. Tap the "Done" checkmark. | 1 |

**Total: 1 tap.** This is the critical path. If the plan is accurate, logging a set is a single tap.

**Scenario B: Weight or reps differ from plan**

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Active Workout | Tap weight field. | 1 |
| 2 | Number input | Enter new weight (e.g., "85"). Keypad auto-focuses. | 2-3 digits |
| 3 | Active Workout | Tap reps field. | 1 |
| 4 | Number input | Enter new reps (e.g., "6"). | 1-2 digits |
| 5 | Active Workout | Tap "Done" checkmark. | 1 |

**Total: 3 taps + 3-5 digit entries.** This matches Strong's per-set logging speed.

**Design requirements for this flow:**
- Weight and reps fields must be pre-filled from the AI plan.
- The "Done" checkmark must be the largest touch target on the set row (56dp minimum).
- Tapping a number field must immediately open the numeric keypad (no need to switch keyboard type).
- After entering weight and tapping reps, focus must auto-advance to the reps field (reduce taps by 1).
- "+2.5" and "-2.5" stepper buttons adjacent to the weight field allow quick adjustments without opening the keypad (1 tap instead of clearing and retyping).

---

### 4.4 Check Progress

**Persona:** Marcus (Intermediate), wants to see bench press progression.

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Home or Bottom Nav | Tap "Progress" tab | 1 |
| 2 | Progress Overview | See muscle group summary or recent PR list. Tap "Chest" group. | 1 |
| 3 | Muscle Group Detail | See exercises trained in this group. Tap "Bench Press". | 1 |
| 4 | Exercise Progress | See 1RM chart, top set chart, volume chart. Swipe to change time range. Default: last 12 weeks. | 0-1 |

**Total: 3-4 taps to a specific exercise chart.**

**Alternative fast path:** From the workout summary screen (after completing a workout), tap any exercise to jump directly to its progress chart. **1 tap** from the summary.

---

### 4.5 Create a Superset

**Persona:** Daniel (Advanced), wants to superset two accessory exercises during an active workout.

| Step | Screen | Action | Taps |
|------|--------|--------|------|
| 1 | Active Workout | Long-press the first exercise (e.g., "Lateral Raises"). Visual indicator shows selection mode. | 1 (long press) |
| 2 | Active Workout | Tap the second exercise (e.g., "Face Pulls"). Both are now selected. | 1 |
| 3 | Context Action | A floating action button or toolbar appears with "Create Superset". Tap it. | 1 |
| 4 | Active Workout | Both exercises are now visually grouped. Rest timer behavior updates to run after the full superset, not individual exercises. | 0 |

**Total: 3 taps (1 long-press + 2 taps).**

**Ungrouping:** Long-press the superset header, tap "Remove Superset." 2 taps.

---

## 5. Usability Requirements

### 5.1 Interaction Speed Constraints

| Metric | Requirement | Rationale |
|--------|-------------|-----------|
| Taps to log a set (plan correct) | 1 tap | The most common interaction. Must be faster than any competitor. |
| Taps to log a set (adjusted) | 3 taps + number input | Matches Strong, the current best-in-class. |
| Taps from home to active workout (template) | 2-4 taps | Faster than Hevy (5-6 taps from home to active workout with saved routine). |
| Taps from home to active workout (new) | 11-12 taps | Acceptable for first-time flow. Must not exceed 15. |
| Taps to check progress for a specific exercise | 3-4 taps | Must not require more than 4 taps from any screen. |
| Time from app open to workout screen (resume) | < 1 second | If a workout is in progress, the app must open directly to it. No splash screen, no home screen redirect. |

### 5.2 Touch Target Specifications

| Element | Minimum Size | Recommended Size | Notes |
|---------|-------------|-----------------|-------|
| "Done" checkmark (set completion) | 56 x 56 dp | 64 x 64 dp | Most critical touch target in the app. |
| Weight/reps input fields | 48 x 48 dp | 56 x 48 dp | Must be tappable with wet/gloved fingers. |
| Exercise row (exercise selection) | 48dp height | 56dp height | Full row is tappable, not just the checkbox. |
| Rest timer skip button | 48 x 48 dp | 56 x 56 dp | Must be easily hittable while fatigued. |
| Navigation bar items | 48 x 48 dp | -- | Material Design 3 standard. |
| Stepper buttons (+/-) | 44 x 44 dp | 48 x 48 dp | For weight/rep quick adjustments. |

**Spacing:** Minimum 8dp between adjacent touch targets. 12dp recommended between the "Done" checkmark and any destructive action (delete set). This prevents mis-taps with sweaty or imprecise touches.

### 5.3 Typography Specifications

| Context | Minimum Font Size | Recommended | Weight |
|---------|-------------------|-------------|--------|
| Set weight (workout screen) | 20sp | 24sp | Bold (700) |
| Set reps (workout screen) | 20sp | 24sp | Bold (700) |
| Exercise name (workout screen) | 16sp | 18sp | Semi-bold (600) |
| Rest timer countdown | 32sp | 40sp | Bold (700) |
| Set type label (warm-up/working) | 12sp | 14sp | Medium (500) |
| PR indicator | 14sp | 16sp | Bold (700) |
| Body text (exercise descriptions, notes) | 14sp | 16sp | Regular (400) |
| Chart labels and axis values | 12sp | 14sp | Regular (400) |

**Rationale:** Standard Material Design body text is 14-16sp. Workout screen values are intentionally oversized because users read them from arm's length (phone on the floor or bench) in varying lighting conditions. The rest timer countdown must be readable from 1-2 meters away.

### 5.4 Contrast and Color

| Requirement | Specification |
|-------------|---------------|
| Text contrast ratio (normal text) | Minimum 4.5:1 (WCAG AA) |
| Text contrast ratio (large text, 18sp+) | Minimum 3:1 (WCAG AA) |
| Primary action color vs. background | Minimum 4.5:1 |
| "Done" checkmark (completed) | High-contrast success color (green on dark background, minimum 4.5:1) |
| Set type differentiation | Warm-up and working sets must be distinguishable by color AND a secondary indicator (label or icon). Do not rely on color alone (color blindness affects ~8% of males). |
| PR indicator | Distinct color (gold/yellow) AND icon/badge. Never color-only. |
| Default theme | Dark. Dark themes reduce screen glare under gym lighting and are the dominant convention in fitness apps. |
| Light theme | Must be available as an option. Some users prefer it, and accessibility guidelines recommend offering both. |

### 5.5 One-Handed Usability Zones

The workout screen is used one-handed in most cases (the other hand is gripping equipment, holding a water bottle, or wiping sweat).

**Reachability zones on a standard Android device (6.0-6.7" screen):**

| Zone | Position | Usage |
|------|----------|-------|
| Easy reach (thumb arc) | Bottom 60% of screen | All primary actions: "Done" checkmark, weight/reps fields, rest timer controls. |
| Stretch zone | Top 20-40% of screen | Exercise name, set type labels, non-critical information. |
| Unreachable zone | Top 0-20% of screen | App bar, navigation. Should not contain any action needed during active set logging. |

**Design rule:** Every action required between sets must be performable within the bottom 60% of the screen using one thumb. Navigation and secondary actions (notes, exercise info, reorder) can occupy the upper zones.

### 5.6 Input Optimization

| Input | Optimization |
|-------|-------------|
| Weight entry | Numeric keypad only (no decimal keyboard). Include ".5" quick-entry button for fractional plates. Support stepper buttons (+2.5/-2.5 for kg, +5/-5 for lbs). |
| Rep entry | Numeric keypad only. Single or double digit. Consider a scrollable rep picker (1-30) as an alternative to keypad for single-tap rep selection. |
| Set type | Default to plan's type (warm-up or working). Tappable toggle to override. Do not require the user to set this manually in the common case. |
| Exercise notes | Free text keyboard. Accessible via a small icon on the exercise row. Must not interfere with set logging flow. |
| Rest timer adjustment | Stepper buttons (+15s / -15s). Long-press for continuous increment. Direct number entry as fallback. |

---

## 6. Research Plan

### 6.1 Pre-Launch Research (Weeks 1-8 before development starts)

**Objective:** Validate core assumptions, identify unknown unknowns, establish baseline metrics.

#### 6.1.1 Contextual Inquiry (Weeks 1-3)

- **Method:** In-gym observation and interview.
- **Participants:** 12-15 gym-goers across all 5 persona types. Recruit from 3 different gym types (commercial chain, independent/hardcore, university gym).
- **Protocol:** Observe a full workout session. Note phone usage patterns, app interactions, pain points. Follow up with a 20-minute semi-structured interview immediately after the session (while the experience is fresh).
- **Key questions:**
  - When during your workout do you use your phone? For what?
  - Walk me through how you decided what to do today (exercise selection process).
  - What is the most annoying thing about your current tracking method?
  - Show me how you track your progress (if at all).
  - If the app could do one thing for you, what would it be?
- **Deliverable:** Behavioral patterns report, validated/refined personas, pain point severity ranking.

#### 6.1.2 Competitive Usability Testing (Weeks 2-4)

- **Method:** Moderated usability test using Strong, Hevy, and Fitbod.
- **Participants:** 8-10 participants (mix of beginners and intermediates, must not currently use the tested app).
- **Tasks:** Complete onboarding, start a workout with 3 exercises, log 3 sets of each, end workout, find progress for one exercise.
- **Metrics:** Task completion rate, time on task, error count, SUS (System Usability Scale) score post-test.
- **Deliverable:** Competitive usability benchmark report with specific UX failures to avoid and successes to adopt.

#### 6.1.3 Card Sorting for Information Architecture (Week 4)

- **Method:** Open card sort (remote, unmoderated via Optimal Workshop or similar).
- **Participants:** 20-25 active gym-goers.
- **Cards:** Exercise names, muscle groups, progress metrics, settings, profile fields, template management.
- **Objective:** Validate the information architecture, particularly whether users' mental models match the 7-muscle-group taxonomy and the distinction between workout setup and active workout.
- **Deliverable:** Dendrogram analysis, IA recommendations.

#### 6.1.4 Concept Testing (Weeks 5-6)

- **Method:** Moderated concept test using mid-fidelity wireframes (Figma prototype).
- **Participants:** 10-12 participants across persona types.
- **Concepts to test:**
  - AI plan generation flow: Does the user understand the plan is a suggestion? Do they feel comfortable editing it?
  - Workout logging screen layout: Is the set row scannable? Are touch targets perceived as large enough?
  - Superset creation via long-press: Is the gesture discoverable?
  - Progress screen: Are the charts meaningful? Can the user identify trends?
- **Deliverable:** Concept validation report with design revision recommendations.

#### 6.1.5 Survey: Training Habits and App Attitudes (Weeks 3-5)

- **Method:** Online survey distributed via gym subreddits (r/fitness, r/weightroom, r/gym), fitness Discord servers, and paid panel (Prolific or similar).
- **Target sample:** 200-300 respondents who strength train at least 1x/week.
- **Key questions:**
  - Current tracking method (app name, pen/paper, nothing).
  - Reason for choosing current method.
  - Top 3 frustrations with current method.
  - Willingness to use AI-generated workout plans (5-point scale + open-ended "why/why not").
  - Acceptable price for a gym tracking app (open-ended and anchored scale).
  - Feature priority ranking (forced ranking of 8 features).
- **Deliverable:** Quantitative report with confidence intervals. Feature priority ranking informs roadmap.

---

### 6.2 Beta Testing Research (Weeks 1-6 of closed beta)

**Objective:** Identify usability issues in the built product, measure task performance, iterate before public launch.

#### 6.2.1 Moderated Usability Testing (Beta Weeks 1-2)

- **Method:** In-person moderated usability test (ideally in a gym, not a lab).
- **Participants:** 8-10 beta users, stratified across experience levels.
- **Tasks:** Full first-use flow (onboarding through completing first workout). Template creation and reuse. Checking progress. Creating a superset. Resuming an interrupted workout.
- **Metrics:** Task success rate, time on task, critical error count, SUS score.
- **Success criteria:**
  - Onboarding completion rate >= 90%.
  - Set logging task success rate >= 95%.
  - SUS score >= 72 (above average).
  - Zero critical errors on data persistence (no lost sets).
- **Deliverable:** Prioritized issue list (critical / high / medium / low), video clips of failure moments for developer reference.

#### 6.2.2 Diary Study (Beta Weeks 1-4)

- **Method:** Daily diary entries via structured form (Google Form or Dovetail).
- **Participants:** 15-20 beta users who commit to using the app for all workouts over 4 weeks.
- **Daily prompts (post-workout):**
  - Rate today's workout logging experience (1-5).
  - Did the AI plan match your expectations? (Yes / No / Partially + explanation).
  - Did anything frustrate you? (free text).
  - Did you discover anything new in the app? (free text).
- **Weekly prompts:**
  - Would you recommend this app to a gym partner? (1-10 NPS).
  - What is the one thing you would change?
- **Deliverable:** Longitudinal experience report. Identify issues that emerge only with repeated use (not visible in one-time usability tests). NPS trend over 4 weeks.

#### 6.2.3 Analytics Review (Beta Weeks 2-6)

- **Method:** Quantitative analysis of in-app event data.
- **Key metrics to monitor:**
  - Onboarding funnel: drop-off at each step.
  - Time from app open to first set logged (should be < 2 minutes for template users).
  - Average taps per set logged (should be < 2 for plan-correct sets).
  - Rest timer usage rate (% of sets where timer runs).
  - Template creation rate (% of users who save a template after first workout).
  - Workout completion rate (% of started workouts that are marked complete, not abandoned).
  - Crash rate during active workout (must be < 0.1%).
  - Session resume rate after backgrounding (should be > 99%).
- **Deliverable:** Analytics dashboard with weekly trend report. Anomalies flagged immediately.

---

### 6.3 Post-Launch Research Cadence

**Objective:** Continuous improvement based on real user behavior at scale.

#### 6.3.1 Monthly: App Store Review Analysis

- **Method:** Systematic coding of Google Play reviews (1-star through 5-star).
- **Process:** Categorize each review by theme (UX issue, missing feature, bug report, praise, competitor comparison). Track theme frequency over time.
- **Owner:** Growth & Content Marketing Manager performs initial triage. UX Researcher analyzes patterns monthly.
- **Deliverable:** Monthly review insights report shared with Product Owner and design team.

#### 6.3.2 Quarterly: Usability Pulse Check

- **Method:** Unmoderated remote usability test (Maze or UserTesting).
- **Participants:** 5-8 users per quarter (mix of new and retained users).
- **Tasks:** Rotate focus each quarter (Q1: onboarding + first workout, Q2: progress tracking, Q3: template management + supersets, Q4: full session end-to-end).
- **Metrics:** Task success rate, SUS score, qualitative feedback.
- **Deliverable:** Quarterly usability report with quarter-over-quarter trend comparison.

#### 6.3.3 Quarterly: NPS Survey

- **Method:** In-app NPS survey triggered after the user's 10th completed workout (one-time), then quarterly for retained users.
- **Question:** "How likely are you to recommend Deep Reps to a friend who lifts?" (0-10) + open-ended follow-up.
- **Target:** NPS >= 40 within 6 months of launch (strong for a utility app).
- **Deliverable:** NPS score with verbatim analysis. Promoter and detractor theme extraction.

#### 6.3.4 Bi-Annually: Deep Qualitative Study

- **Method:** 60-minute in-depth interviews with 10-12 users.
- **Focus:** Long-term value perception, feature gaps, competitive switching triggers, willingness to pay, unmet needs.
- **Recruitment:** Stratify by engagement level (power users, moderate users, churned users).
- **Deliverable:** Strategic insights report informing the next 6-month product roadmap.

#### 6.3.5 Continuous: Retention Cohort Analysis

- **Method:** Automated cohort analysis (D1, D7, D14, D30, D60, D90 retention).
- **Owner:** Data Analyst (primary), UX Researcher (interpretation).
- **Action triggers:**
  - D1 retention drops below 40%: investigate onboarding friction.
  - D7 retention drops below 25%: investigate first-week experience.
  - D30 retention drops below 15%: investigate value delivery (are users seeing progress?).
- **Deliverable:** Weekly retention dashboard. Ad-hoc deep dives when thresholds are breached.

---

## Appendix A: Key Assumptions to Validate

These assumptions underpin the product design. If any prove false, significant design changes are required.

| # | Assumption | Validation Method | Risk if False |
|---|-----------|-------------------|---------------|
| A1 | Users will trust AI-generated workout plans. | Survey (6.1.5), concept test (6.1.4) | Core feature is rejected. Fallback: position AI as "suggestion" with heavy emphasis on editability. |
| A2 | 1-tap set logging (when plan is correct) is achievable and preferred. | Usability testing (6.2.1), analytics (6.2.3) | Logging speed is slower than competitors. Retention risk. |
| A3 | The 7-muscle-group taxonomy matches users' mental models. | Card sort (6.1.3), contextual inquiry (6.1.1) | Exercise selection is confusing. Users cannot find exercises where they expect them. |
| A4 | Users will create and reuse templates after 2-3 workouts. | Diary study (6.2.2), analytics (6.2.3) | Template adoption is low. Every session starts from scratch, increasing friction. |
| A5 | Offline-first with AI fallback is acceptable. | Concept test (6.1.4), diary study (6.2.2) | Users expect AI plans to always work. Offline fallback feels broken. |
| A6 | A curated exercise library (no custom exercises) is sufficient at launch. | Survey (6.1.5), app store reviews (6.3.1) | Power users feel constrained and churn. Accelerates custom exercise feature timeline. |
| A7 | Superset creation via long-press is discoverable. | Usability testing (6.2.1) | Users do not find the feature. Needs onboarding tooltip or alternative entry point. |

---

## Appendix B: Accessibility Considerations

Beyond the minimum WCAG AA requirements specified in section 5.4:

- **Motor impairment:** Large touch targets (56dp+) and generous spacing benefit users with reduced fine motor control, not just gym-goers with sweaty hands.
- **Screen readers:** All interactive elements must have content descriptions. Chart data must be available as text (not image-only).
- **Cognitive load:** The workout screen must present only the current exercise and its sets prominently. Other exercises should be scrollable but not visually competing. Information density must be lower during active workout than on review/progress screens.
- **Color blindness:** Set type (warm-up vs. working), PR indicators, and volume trend (up/down) must never rely on color alone. Use labels, icons, or patterns as secondary differentiators.
