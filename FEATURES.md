# Deep Reps — Feature Specification

This document describes **what** the app does. It does not describe how it's built.

---

## 1. Muscle Groups & Exercise Library

### 1.1 Muscle Groups

Seven top-level groups. Each contains a curated, fixed exercise library (no user-created exercises at launch).

| Group | Includes |
|-------|----------|
| **Legs** | Quads, hamstrings, glutes, calves |
| **Lower Back** | Erector spinae, spinal stabilizers (deadlift variations, hyperextensions, good mornings, etc.) |
| **Chest** | Upper, mid, lower pec |
| **Back** | Lats, rhomboids, rear delts (rows, pull-ups, pulldowns, etc.) |
| **Shoulders** | Anterior, lateral, posterior delts, traps |
| **Arms** | Biceps, triceps, forearms — treated as a single selectable group |
| **Core** | Abs, obliques, transverse abdominis |

**Compound exercise rule:** Each exercise belongs to one primary group only. Deadlift → Lower Back. Bench press → Chest. Barbell row → Back. No duplicates across groups. The CSCS determines the primary group assignment for every exercise.

### 1.2 Exercise Detail View

Each exercise has a detail card containing:

- **Name** and short description (1-2 sentences on what the exercise is)
- **2D anatomical diagram** with highlighted primary and secondary muscles (primary = filled/bold, secondary = lighter shade)
- **Pros / key benefits** (e.g., "high lat activation with full stretch at bottom")
- **Tips / cues** (e.g., "retract scapulae before pulling")
- **Equipment required** (barbell, dumbbell, cable, machine, bodyweight, etc.)
- **Isolation level** tag: compound / isolation (used for auto-ordering — see 2.3)

The exercise library is fixed at launch. Custom exercises are a potential future addition — not planned or designed now.

---

## 2. Workout Setup Flow

### 2.1 Group Selection

User selects one or more muscle groups to train today. Multi-select. No limit on how many groups can be combined.

### 2.2 Exercise Selection

After group selection, the app shows the exercise list for each selected group. The user taps to select which exercises they want to include in today's workout.

Each exercise in the list shows: name, equipment tag, isolation level tag. Tapping opens the full detail view (1.2). A checkbox or toggle marks it as selected.

### 2.3 Auto-Ordered Workout

Once exercises are selected, the app proposes an exercise order based on:

1. Compound exercises first, isolation exercises last
2. Within compounds: larger muscle groups before smaller (e.g., squats before lunges)
3. Within isolation: CSCS-defined sensible ordering

The user can **reorder freely** by dragging. The app's proposal is a default, not a constraint.

### 2.4 Reusable Templates

After completing a workout (or during exercise selection), the user can save the current exercise selection + order as a named template (e.g., "Monday Push Day", "Heavy Legs").

When starting a new workout, the user can either:
- Pick groups → pick exercises (standard flow)
- Load a saved template (skips directly to plan generation)

Templates store: exercise list, exercise order, and template name. They do **not** store weights/reps — those come from the AI plan each session.

---

## 3. AI-Powered Plan Generation

### 3.1 Plan Proposal

After exercises are selected and ordered, the app generates a session plan using Gemini API. The plan includes, for each exercise:

- **Warm-up sets** (preparatory approaches): weight × reps for each
- **Working sets**: weight × reps × number of sets

Example plan for Bench Press:
```
Warm-up:  20kg × 12,  40kg × 8,  60kg × 5
Working:  80kg × 8 × 4 sets
```

### 3.2 Context Fed to the AI

The prompt includes (within token limits — prioritize recent, relevant data):

- **Training history** for the selected muscle group(s): last 3-5 sessions, including exercises performed, weights, reps, and sets actually completed
- **User profile** (if filled): age, body weight, training experience level
- **Exercise list** for this session and their order
- **Goal context** derived from experience level (beginner = form/volume focus, advanced = progressive overload focus)

The prompt is carefully designed by the CSCS + developer together. It must produce structured, parseable output (JSON), not free-text.

**Developer note:** The LLM integration must be abstracted behind a provider interface. Gemini is the initial implementation. Switching to another API (OpenAI, Claude, local model, etc.) must not require changes outside the provider layer.

### 3.3 Cold Start: Experience-Based Baseline Plans

When no training history exists (new user, or first time performing an exercise), the AI has no data to work from. Instead, the app uses one of three baseline profiles:

| Level | Label | Description |
|-------|-------|-------------|
| **1** | Total Beginner | 0-6 months of gym experience. Conservative weights, higher reps (10-15), focus on movement patterns. Baseline weights derived from body weight ratios (e.g., bench press ≈ 0.3-0.5× BW). |
| **2** | Intermediate | 6-18 months. Moderate weights, standard hypertrophy rep ranges (8-12). Baseline weights derived from intermediate strength standards. |
| **3** | Advanced | 18+ months. Heavier loads, varied rep ranges (3-6 for strength, 8-12 for hypertrophy). Baseline weights derived from advanced strength standards. |

The user selects their experience level during onboarding. This level feeds into the AI prompt as context and determines the baseline plan when history is absent.

As the user logs workouts, real data progressively replaces baseline assumptions.

### 3.4 Plan is a Suggestion, Not a Constraint

The proposed plan is fully editable at any point:

- **Before starting:** User can modify any weight, rep count, or set count in the plan
- **During the workout:** User logs what actually happens — if the plan says 100kg × 8 but the user does 90kg × 6, they log 90 × 6. No friction, no "are you sure?" prompts
- **The logged data (actuals) is what gets saved**, not the plan. The plan is ephemeral; the log is permanent.

---

## 4. Active Workout Logging

### 4.1 Workout Screen

The primary workout interface. This is the most critical UX screen in the app — it's used mid-exercise with sweaty hands, time pressure, and distraction.

For each exercise in the workout, the user sees:
- Exercise name
- Planned sets (from AI plan or manual)
- A row per set: **weight field**, **reps field**, **set type indicator** (warm-up / working), **done checkbox**

Completing a set = tapping done after entering weight and reps. Minimal taps. Large touch targets.

### 4.2 Set Logging

Each set is logged with:
- **Weight** (in user-selected unit — kg or lbs)
- **Reps completed**
- **Set type**: warm-up or working set

The user can:
- Edit any set's weight/reps before or after marking it done
- Add extra sets beyond what the plan proposed
- Delete/skip sets
- Reorder sets if needed

### 4.3 Mid-Workout Modifications

During an active workout, the user can:
- **Add an exercise** not in the original selection (opens a quick exercise picker)
- **Remove/skip an exercise** entirely
- **Reorder remaining exercises**

### 4.4 Supersets and Circuits

The user can group 2+ exercises into a superset or circuit. When exercises are grouped:
- They are visually linked (indented, bracketed, or color-coded)
- The rest timer runs after the full group is completed, not after each individual exercise
- Logging works the same per-set, but the UI presents the grouped exercises together

Grouping is optional and user-initiated (e.g., long-press two exercises → "Create superset").

### 4.5 Rest Timer

A configurable rest timer runs between sets:
- Default rest times differ by set type (e.g., warm-up: 60s, working: 90-120s)
- User can adjust the default per exercise or globally
- Timer shows a visible countdown on the workout screen
- Notification/vibration when rest period ends (works when app is backgrounded)
- User can skip the timer at any time

### 4.6 Per-Exercise Notes

Each exercise in the workout has an optional free-text notes field. Examples:
- "Left shoulder felt tight"
- "Grip slipped on last rep, try straps next time"
- "Used Smith machine, regular rack was taken"

Notes are saved with the workout log and visible in training history.

### 4.7 Data Persistence

- Workout data auto-saves after every completed set
- If the app crashes or is killed by the OS, **at most the last in-progress set may be lost** — all previously completed sets are preserved
- On next app launch after a crash, the user is prompted to resume the interrupted workout or discard it

### 4.8 Workout Complete Summary

When the user finishes the workout, a summary screen shows:
- Total workout duration
- Exercises completed (count)
- Total volume (sets × reps × weight) per muscle group
- Total tonnage for the session
- Personal records hit during this session (highlighted)
- Comparison to previous session for the same muscle group(s): volume up/down, weight PRs

---

## 5. Progress Tracking

Metrics and time ranges defined by professional training standards. The CSCS has final authority on what's shown and how it's calculated.

### 5.1 Per-Exercise Metrics

- **Estimated 1RM progression** over time (calculated from logged sets using standard formulas like Epley or Brzycki)
- **Top set weight** progression (heaviest working set per session)
- **Volume load** per exercise per session (sets × reps × weight)
- **Rep PRs at given weights** (e.g., "PR: 8 reps at 100kg" — previously best was 6)

### 5.2 Per-Muscle-Group Metrics

- **Weekly volume** (total working sets per group per week) — a key hypertrophy driver
- **Total tonnage** per group per session and per week
- **Training frequency** (sessions per week hitting this group)

### 5.3 Overall Metrics

- **Training consistency** (sessions per week over time)
- **Bodyweight trend** (if tracked in profile)
- **Strength milestones** relative to experience level (e.g., "Bench press: Intermediate level" based on body weight ratios and strength standards)

### 5.4 Time Ranges

Charts and metrics viewable across:
- **Last 4 weeks** — current training block view
- **Last 12 weeks** — mesocycle view
- **Last 6 months** — long-term trend
- **All time** — full history

### 5.5 Personal Records

The app automatically detects and highlights PRs:
- Weight PR (heaviest set for an exercise, ever or within a rep range)
- Rep PR (most reps at a given weight)
- Estimated 1RM PR
- Volume PR (highest single-session volume for a muscle group)

PRs are surfaced in the workout summary (4.8) and in the progress view.

---

## 6. User Profile

### 6.1 Required at Onboarding

- **Experience level**: Total Beginner / Intermediate / Advanced (drives baseline plans — see 3.3)
- **Preferred unit**: kg or lbs

### 6.2 Optional Profile Fields

All optional. Not required to use the app. Used to improve AI plan quality when available.

- Age
- Height
- Body weight (can be updated over time — tracked as a series, not a single value)
- Gender (relevant for strength standard calculations)

The profile is deliberately minimal. No social features, no profile photos, no bio. This is a training tool, not a social network.

---

## 7. Offline & Sync (Architecture Decision Pending)

The app must work fully offline. Gyms have unreliable connectivity. Every feature described above must function without an internet connection, **except** AI plan generation (3.1), which requires an API call.

**Fallback when offline:** If the AI plan cannot be generated (no connectivity), the app offers:
- The last plan used for the same exercise set (if available)
- The baseline plan for the user's experience level (if no history)
- Manual entry (user sets their own weights/reps with no AI suggestion)

Whether data syncs to the cloud or stays local-only is a team decision (see TEAM.md). The feature set does not depend on this choice.

---

## Feature Boundary — Explicitly Out of Scope

These are **not planned, not designed, and should not be built** unless revisited in a future planning cycle:

| Feature | Reason |
|---------|--------|
| Custom exercise creation | Potential future addition. Exercise library is curated and fixed at launch. |
| Nutrition / calorie tracking | Separate domain. Would require the nutritionist role from TEAM.md. |
| Social features / sharing | Not a social app. Revisit post-PMF if retention data suggests community value. |
| Wearable integration | Desirable but not MVP. Revisit after core workout logging is solid. |
| Video/audio coaching during exercises | Content production dependency. Out of scope for launch. |
| Multi-language support | English only at launch. |
| iOS version | Android only until product-market fit is confirmed. |
