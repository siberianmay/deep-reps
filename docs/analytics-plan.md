# Deep Reps -- Analytics Plan

This document defines the analytics strategy for Deep Reps. It covers event taxonomy, KPI dashboards, experimentation framework, cohort analysis, data pipeline architecture, privacy compliance, and reporting cadence.

---

## 1. Analytics Event Taxonomy

### 1.1 Naming Convention

All events follow `object_action` format in `snake_case`. Properties use `snake_case`. All events carry these global properties:

| Property | Type | Description |
|----------|------|-------------|
| `user_id` | string | Anonymous hashed identifier |
| `session_id` | string | App session identifier |
| `timestamp` | ISO 8601 | UTC event time |
| `app_version` | string | Semantic version of the app |
| `os_version` | string | Android OS version |
| `device_model` | string | Device manufacturer and model |
| `experience_level` | enum | `beginner`, `intermediate`, `advanced` |

### 1.2 Onboarding Events (1-6)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 1 | `app_first_open` | User opens app for the first time after install | `install_source`, `referrer` | `{ "event": "app_first_open", "install_source": "google_play", "referrer": "utm_campaign_jan25" }` |
| 2 | `onboarding_started` | Onboarding flow first screen loads | `screen_name` | `{ "event": "onboarding_started", "screen_name": "experience_level_select" }` |
| 3 | `onboarding_experience_selected` | User selects experience level | `level` | `{ "event": "onboarding_experience_selected", "level": "intermediate" }` |
| 4 | `onboarding_unit_selected` | User selects preferred unit | `unit` | `{ "event": "onboarding_unit_selected", "unit": "kg" }` |
| 5 | `onboarding_profile_completed` | User fills optional profile fields | `fields_filled` (array of field names), `fields_count` | `{ "event": "onboarding_profile_completed", "fields_filled": ["age", "body_weight"], "fields_count": 2 }` |
| 6 | `onboarding_finished` | Onboarding flow completes | `duration_seconds`, `fields_filled_count` | `{ "event": "onboarding_finished", "duration_seconds": 45, "fields_filled_count": 3 }` |

### 1.3 Workout Setup Events (7-16)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 7 | `workout_setup_started` | User enters workout setup flow | `entry_method` (`groups` or `template`) | `{ "event": "workout_setup_started", "entry_method": "groups" }` |
| 8 | `muscle_group_selected` | User selects a muscle group | `group_name`, `groups_selected_count` | `{ "event": "muscle_group_selected", "group_name": "chest", "groups_selected_count": 2 }` |
| 9 | `muscle_group_deselected` | User removes a muscle group | `group_name`, `groups_selected_count` | `{ "event": "muscle_group_deselected", "group_name": "arms", "groups_selected_count": 1 }` |
| 10 | `exercise_selected` | User selects an exercise | `exercise_name`, `muscle_group`, `equipment_type`, `isolation_level`, `exercises_selected_count` | `{ "event": "exercise_selected", "exercise_name": "bench_press", "muscle_group": "chest", "equipment_type": "barbell", "isolation_level": "compound", "exercises_selected_count": 4 }` |
| 11 | `exercise_deselected` | User removes an exercise | `exercise_name`, `muscle_group` | `{ "event": "exercise_deselected", "exercise_name": "cable_fly", "muscle_group": "chest" }` |
| 12 | `exercise_detail_viewed` | User opens exercise detail card | `exercise_name`, `muscle_group`, `view_duration_ms` | `{ "event": "exercise_detail_viewed", "exercise_name": "deadlift", "muscle_group": "lower_back", "view_duration_ms": 3200 }` |
| 13 | `exercise_order_changed` | User manually reorders an exercise | `exercise_name`, `from_position`, `to_position` | `{ "event": "exercise_order_changed", "exercise_name": "squat", "from_position": 3, "to_position": 1 }` |
| 14 | `template_loaded` | User loads a saved template | `template_name`, `exercise_count` | `{ "event": "template_loaded", "template_name": "Monday Push Day", "exercise_count": 5 }` |
| 15 | `template_saved` | User saves current selection as template | `template_name`, `exercise_count`, `save_context` (`post_workout` or `during_setup`) | `{ "event": "template_saved", "template_name": "Heavy Legs", "exercise_count": 6, "save_context": "post_workout" }` |
| 16 | `template_deleted` | User deletes a saved template | `template_name` | `{ "event": "template_deleted", "template_name": "Old Routine" }` |

### 1.4 AI Plan Events (17-22)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 17 | `ai_plan_requested` | App sends request to Gemini API | `exercise_count`, `muscle_groups` (array), `has_history`, `experience_level` | `{ "event": "ai_plan_requested", "exercise_count": 5, "muscle_groups": ["chest", "shoulders"], "has_history": true, "experience_level": "intermediate" }` |
| 18 | `ai_plan_received` | AI plan response successfully parsed | `latency_ms`, `exercise_count`, `total_sets_proposed` | `{ "event": "ai_plan_received", "latency_ms": 1850, "exercise_count": 5, "total_sets_proposed": 28 }` |
| 19 | `ai_plan_failed` | API call fails or response unparseable | `error_type` (`network`, `timeout`, `parse_error`, `api_error`), `error_message` | `{ "event": "ai_plan_failed", "error_type": "network", "error_message": "connection_timeout" }` |
| 20 | `ai_plan_fallback_used` | Offline fallback plan used | `fallback_type` (`last_plan`, `baseline`, `manual`) | `{ "event": "ai_plan_fallback_used", "fallback_type": "last_plan" }` |
| 21 | `ai_plan_modified` | User edits the AI-proposed plan before starting | `modifications_count`, `fields_modified` (array: `weight`, `reps`, `sets`) | `{ "event": "ai_plan_modified", "modifications_count": 3, "fields_modified": ["weight", "reps"] }` |
| 22 | `ai_plan_accepted` | User starts workout with the plan (modified or not) | `was_modified`, `modifications_count` | `{ "event": "ai_plan_accepted", "was_modified": true, "modifications_count": 2 }` |

### 1.5 Active Workout Events (23-37)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 23 | `workout_started` | User begins active workout | `exercise_count`, `muscle_groups` (array), `plan_source` (`ai`, `fallback`, `manual`) | `{ "event": "workout_started", "exercise_count": 6, "muscle_groups": ["legs"], "plan_source": "ai" }` |
| 24 | `set_completed` | User marks a set as done | `exercise_name`, `set_number`, `set_type` (`warmup` or `working`), `weight`, `reps`, `unit`, `matched_plan` (bool) | `{ "event": "set_completed", "exercise_name": "squat", "set_number": 3, "set_type": "working", "weight": 100, "reps": 8, "unit": "kg", "matched_plan": false }` |
| 25 | `set_edited` | User modifies a completed set | `exercise_name`, `set_number`, `field_changed` (`weight`, `reps`, `set_type`) | `{ "event": "set_edited", "exercise_name": "squat", "set_number": 2, "field_changed": "weight" }` |
| 26 | `set_added` | User adds an extra set beyond plan | `exercise_name`, `total_sets_for_exercise` | `{ "event": "set_added", "exercise_name": "bench_press", "total_sets_for_exercise": 6 }` |
| 27 | `set_deleted` | User deletes/skips a set | `exercise_name`, `set_number`, `set_type` | `{ "event": "set_deleted", "exercise_name": "lateral_raise", "set_number": 4, "set_type": "working" }` |
| 28 | `exercise_added_midworkout` | User adds an exercise during workout | `exercise_name`, `muscle_group` | `{ "event": "exercise_added_midworkout", "exercise_name": "face_pull", "muscle_group": "shoulders" }` |
| 29 | `exercise_removed_midworkout` | User removes/skips an exercise | `exercise_name`, `muscle_group`, `sets_completed_before_skip` | `{ "event": "exercise_removed_midworkout", "exercise_name": "leg_curl", "muscle_group": "legs", "sets_completed_before_skip": 0 }` |
| 30 | `superset_created` | User groups exercises into superset/circuit | `exercise_names` (array), `group_size` | `{ "event": "superset_created", "exercise_names": ["bicep_curl", "tricep_pushdown"], "group_size": 2 }` |
| 31 | `rest_timer_started` | Rest timer begins | `exercise_name`, `duration_seconds`, `set_type` | `{ "event": "rest_timer_started", "exercise_name": "squat", "duration_seconds": 120, "set_type": "working" }` |
| 32 | `rest_timer_skipped` | User skips rest timer | `exercise_name`, `time_remaining_seconds` | `{ "event": "rest_timer_skipped", "exercise_name": "squat", "time_remaining_seconds": 45 }` |
| 33 | `rest_timer_completed` | Timer expires naturally | `exercise_name`, `duration_seconds` | `{ "event": "rest_timer_completed", "exercise_name": "squat", "duration_seconds": 120 }` |
| 34 | `exercise_note_added` | User adds a note to an exercise | `exercise_name`, `note_length_chars` | `{ "event": "exercise_note_added", "exercise_name": "bench_press", "note_length_chars": 42 }` |
| 35 | `workout_completed` | User finishes the workout | `duration_minutes`, `exercises_completed`, `total_sets`, `total_volume_kg`, `personal_records_count`, `muscle_groups` (array) | `{ "event": "workout_completed", "duration_minutes": 62, "exercises_completed": 6, "total_sets": 24, "total_volume_kg": 8400, "personal_records_count": 2, "muscle_groups": ["chest", "shoulders"] }` |
| 36 | `workout_abandoned` | User exits without completing | `duration_minutes`, `exercises_completed`, `total_exercises_planned`, `sets_completed` | `{ "event": "workout_abandoned", "duration_minutes": 18, "exercises_completed": 2, "total_exercises_planned": 6, "sets_completed": 8 }` |
| 37 | `workout_resumed` | User resumes after crash/background kill | `interruption_duration_minutes`, `sets_recovered` | `{ "event": "workout_resumed", "interruption_duration_minutes": 3, "sets_recovered": 12 }` |

### 1.6 Progress Tracking Events (38-42)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 38 | `progress_viewed` | User opens progress/stats screen | `view_type` (`exercise`, `muscle_group`, `overall`), `time_range` (`4w`, `12w`, `6m`, `all`) | `{ "event": "progress_viewed", "view_type": "exercise", "time_range": "12w" }` |
| 39 | `progress_exercise_drilldown` | User drills into specific exercise stats | `exercise_name`, `metric_type` (`e1rm`, `top_set`, `volume`, `rep_pr`) | `{ "event": "progress_exercise_drilldown", "exercise_name": "deadlift", "metric_type": "e1rm" }` |
| 40 | `personal_record_achieved` | System detects a new PR | `exercise_name`, `pr_type` (`weight`, `rep`, `e1rm`, `volume`), `previous_value`, `new_value`, `unit` | `{ "event": "personal_record_achieved", "exercise_name": "bench_press", "pr_type": "weight", "previous_value": 95, "new_value": 100, "unit": "kg" }` |
| 41 | `workout_summary_viewed` | User views post-workout summary | `duration_on_screen_seconds`, `shared` (bool) | `{ "event": "workout_summary_viewed", "duration_on_screen_seconds": 15, "shared": false }` |
| 42 | `bodyweight_logged` | User updates body weight in profile | `weight`, `unit`, `previous_weight` | `{ "event": "bodyweight_logged", "weight": 82.5, "unit": "kg", "previous_weight": 83.0 }` |

### 1.7 Profile & Settings Events (43-47)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 43 | `profile_updated` | User updates profile fields | `fields_changed` (array), `experience_level_changed` (bool) | `{ "event": "profile_updated", "fields_changed": ["body_weight", "age"], "experience_level_changed": false }` |
| 44 | `settings_changed` | User modifies a setting | `setting_name`, `old_value`, `new_value` | `{ "event": "settings_changed", "setting_name": "default_rest_timer_working", "old_value": 90, "new_value": 120 }` |
| 45 | `unit_switched` | User changes between kg and lbs | `from_unit`, `to_unit` | `{ "event": "unit_switched", "from_unit": "kg", "to_unit": "lbs" }` |
| 46 | `rest_timer_default_changed` | User adjusts default rest timer | `set_type`, `old_duration_seconds`, `new_duration_seconds` | `{ "event": "rest_timer_default_changed", "set_type": "working", "old_duration_seconds": 90, "new_duration_seconds": 150 }` |
| 47 | `data_export_requested` | User requests data export (GDPR) | `format` | `{ "event": "data_export_requested", "format": "json" }` |

### 1.8 App Lifecycle Events (48-50)

| # | Event Name | Trigger | Properties | Example Payload |
|---|-----------|---------|------------|-----------------|
| 48 | `app_session_start` | App brought to foreground | `time_since_last_session_hours`, `has_active_workout` | `{ "event": "app_session_start", "time_since_last_session_hours": 22.5, "has_active_workout": false }` |
| 49 | `app_session_end` | App backgrounded or closed | `session_duration_seconds`, `screens_viewed` (array) | `{ "event": "app_session_end", "session_duration_seconds": 3840, "screens_viewed": ["setup", "workout", "summary"] }` |
| 50 | `app_crash_detected` | App detects previous unclean shutdown | `had_active_workout`, `sets_at_risk` | `{ "event": "app_crash_detected", "had_active_workout": true, "sets_at_risk": 1 }` |

---

## 2. KPI Dashboard Design

### 2.1 Acquisition Metrics

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **Daily Installs** | Count of `app_first_open` events per day | Analytics | Track trend |
| **Install Source Breakdown** | Distribution of `install_source` from `app_first_open` | Analytics | Identify top channels |
| **Cost Per Install (CPI)** | Ad spend / installs per channel per day | Ad platforms + Analytics | < $1.50 |
| **Organic vs Paid Ratio** | Organic installs / total installs | Analytics + Ad platforms | > 60% organic at maturity |
| **Play Store Conversion Rate** | Store listing visitors / installs | Google Play Console | > 30% |

### 2.2 Activation Metrics

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **Onboarding Completion Rate** | Users who fire `onboarding_finished` / users who fire `onboarding_started` | Analytics | > 85% |
| **Time to First Workout** | Time between `app_first_open` and first `workout_completed` | Analytics | < 48 hours |
| **First Workout Completion Rate** | Users with at least one `workout_completed` / users with `app_first_open` within 7 days | Analytics | > 40% |
| **Activation Rate** | Users who complete 2+ workouts within 14 days of install / total installs | Analytics | > 25% |
| **Onboarding Drop-off by Step** | Funnel from `onboarding_started` through each step to `onboarding_finished` | Analytics | Identify worst step |

### 2.3 Engagement Metrics

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **DAU** | Distinct users with at least one `app_session_start` per day | Analytics | Track trend |
| **WAU** | Distinct users with at least one `app_session_start` in a 7-day window | Analytics | Track trend |
| **MAU** | Distinct users with at least one `app_session_start` in a 30-day window | Analytics | Track trend |
| **DAU/MAU Ratio (Stickiness)** | DAU / MAU | Derived | > 0.25 |
| **Workouts Per User Per Week** | Count of `workout_completed` per user per 7-day window | Analytics | > 2.5 |
| **Avg Workout Duration** | Mean `duration_minutes` from `workout_completed` | Analytics | 45-75 min |
| **Avg Sets Per Workout** | Mean `total_sets` from `workout_completed` | Analytics | 18-30 |
| **Session Duration** | Mean `session_duration_seconds` from `app_session_end` | Analytics | > 30 min |
| **AI Plan Acceptance Rate** | `ai_plan_accepted` where `was_modified` = false / total `ai_plan_accepted` | Analytics | > 60% |
| **Workout Abandonment Rate** | `workout_abandoned` / (`workout_completed` + `workout_abandoned`) | Analytics | < 10% |

### 2.4 Retention Metrics

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **D1 Retention** | Users returning on day 1 after install / installs on day 0 | Analytics | > 45% |
| **D7 Retention** | Users returning on day 7 after install / installs on day 0 | Analytics | > 25% |
| **D30 Retention** | Users returning on day 30 after install / installs on day 0 | Analytics | > 15% |
| **D90 Retention** | Users returning on day 90 after install / installs on day 0 | Analytics | > 10% |
| **Weekly Retention (Rolling)** | Users active in week N who are also active in week N+1 | Analytics | > 65% |
| **Workout Streak** | Consecutive weeks with at least 1 `workout_completed` | Analytics | Median > 4 weeks |
| **Resurrection Rate** | Previously churned users (inactive 30+ days) who return / total churned | Analytics | Track trend |

### 2.5 Revenue Metrics (Post-Monetization)

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **Trial Start Rate** | Users who start free trial / eligible users shown paywall | Analytics + Billing | > 15% |
| **Trial-to-Paid Conversion** | Users who convert after trial / users who started trial | Billing | > 40% |
| **MRR** | Monthly recurring revenue | Billing | Track trend |
| **ARPU** | Revenue / MAU | Billing + Analytics | Track trend |
| **ARPPU** | Revenue / paying users | Billing | Track trend |
| **LTV (180-day)** | Projected revenue per user over 180 days | Billing + Analytics | > 3x CPI |
| **Churn Rate (Monthly)** | Subscribers who cancel in month N / subscribers at start of month N | Billing | < 8% |
| **Refund Rate** | Refund requests / total transactions | Google Play Console | < 5% |

### 2.6 Feature Adoption Metrics

| Metric | Definition | Data Source | Target |
|--------|-----------|-------------|--------|
| **AI Plan Usage Rate** | Workouts using AI plan (`plan_source` = `ai`) / total workouts | Analytics | > 70% |
| **Template Usage Rate** | Workouts started via template (`entry_method` = `template`) / total workouts | Analytics | > 30% after 30 days |
| **Superset Usage Rate** | Workouts with at least one `superset_created` / total workouts | Analytics | Track trend |
| **Rest Timer Usage** | Workouts with at least one `rest_timer_started` / total workouts | Analytics | > 50% |
| **Notes Usage Rate** | Workouts with at least one `exercise_note_added` / total workouts | Analytics | Track trend |
| **Progress Screen Views/Week** | `progress_viewed` events per user per week | Analytics | > 1.5 |
| **Bodyweight Tracking Adoption** | Users with at least one `bodyweight_logged` / MAU | Analytics | > 20% |
| **Exercise Detail View Rate** | Users who view at least one `exercise_detail_viewed` per session / total setup sessions | Analytics | > 40% |

---

## 3. A/B Test Framework

### 3.1 Tool Recommendation

**Primary tool: Firebase A/B Testing + Remote Config**

Rationale:
- Native Android integration with no additional SDK
- Built-in statistical engine with Bayesian analysis
- Ties directly to Firebase Analytics events
- Free tier sufficient for initial scale
- Supports server-side and client-side feature flags via Remote Config

**Secondary (post-scale): Amplitude Experiment or Statsig**

Move to a dedicated experimentation platform when test velocity exceeds 5 concurrent experiments or when the Firebase statistical engine is insufficient for more advanced sequential testing designs.

### 3.2 Statistical Standards

- **Minimum detectable effect (MDE):** 5% relative change for primary metric
- **Significance level (alpha):** 0.05
- **Statistical power:** 0.80
- **Assignment unit:** User-level (not session-level)
- **Minimum runtime:** 14 days (covers full weekly workout cycles x2)
- **Guardrail metrics:** Crash rate, workout abandonment rate, session duration

### 3.3 Experiment Backlog

#### Experiment 1: Onboarding Length

| Field | Value |
|-------|-------|
| **Hypothesis** | Reducing onboarding to 2 steps (experience level + unit only) will increase onboarding completion rate without harming first-workout completion. |
| **Control** | Current full onboarding (experience level, unit, optional profile fields) |
| **Variant** | 2-step onboarding; optional profile fields deferred to settings |
| **Primary Metric** | Onboarding completion rate |
| **Secondary Metrics** | Time to first workout, 7-day first workout completion rate |
| **Sample Size** | ~3,200 per arm (5% MDE on 85% baseline, alpha=0.05, power=0.80) |

#### Experiment 2: AI Plan Presentation

| Field | Value |
|-------|-------|
| **Hypothesis** | Showing a plain-language rationale under each AI-proposed set (e.g., "5% increase from last session") will increase AI plan acceptance rate. |
| **Control** | AI plan displayed as weight x reps x sets only |
| **Variant** | AI plan with one-line rationale per exercise |
| **Primary Metric** | AI plan acceptance rate (unmodified) |
| **Secondary Metrics** | `ai_plan_modified` count, workout completion rate |
| **Sample Size** | ~3,800 per arm (5% MDE on 60% baseline) |

#### Experiment 3: Default Rest Timer Duration

| Field | Value |
|-------|-------|
| **Hypothesis** | Increasing default working-set rest timer from 90s to 120s will reduce rest timer skip rate, indicating better alignment with user behavior. |
| **Control** | 90s default rest timer for working sets |
| **Variant** | 120s default rest timer for working sets |
| **Primary Metric** | Rest timer skip rate (`rest_timer_skipped` / `rest_timer_started`) |
| **Secondary Metrics** | Avg workout duration, sets per workout |
| **Sample Size** | ~4,000 per arm (5% MDE on 40% baseline skip rate) |

#### Experiment 4: Workout Summary Depth

| Field | Value |
|-------|-------|
| **Hypothesis** | A richer post-workout summary (including muscle group heatmap and historical comparison chart) will increase D7 retention by encouraging progress awareness. |
| **Control** | Current text-based summary (duration, volume, PRs) |
| **Variant** | Summary with visual muscle heatmap + 4-week volume trend chart |
| **Primary Metric** | D7 retention |
| **Secondary Metrics** | `workout_summary_viewed` duration, `progress_viewed` frequency |
| **Sample Size** | ~12,500 per arm (5% MDE on 25% D7 retention baseline) |

#### Experiment 5: Template Nudge After First Workout

| Field | Value |
|-------|-------|
| **Hypothesis** | Prompting users to save their first workout as a template will increase template adoption and reduce friction for the second workout. |
| **Control** | No prompt; template save is discoverable in UI |
| **Variant** | Modal prompt after first `workout_completed`: "Save this as a template for next time?" |
| **Primary Metric** | Template usage rate in second workout |
| **Secondary Metrics** | Time to second workout, D7 retention |
| **Sample Size** | ~2,200 per arm (10% MDE on 15% baseline template usage, first-time users only) |

#### Experiment 6: Exercise Order Auto-Sort Algorithm

| Field | Value |
|-------|-------|
| **Hypothesis** | A personalized auto-sort (based on user's historical exercise order preferences) will reduce manual reorders compared to the default CSCS-defined sort. |
| **Control** | CSCS-defined default order (compounds first, large-to-small) |
| **Variant** | Personalized order based on user's most frequent ordering from past 5 sessions |
| **Primary Metric** | Manual reorder rate (`exercise_order_changed` / workouts) |
| **Secondary Metrics** | Workout setup time, workout abandonment rate |
| **Sample Size** | ~5,000 per arm (5% MDE on 30% baseline reorder rate); requires users with 5+ workout history |

#### Experiment 7: Cold Start Baseline Plan Aggressiveness

| Field | Value |
|-------|-------|
| **Hypothesis** | Slightly more conservative baseline weights for beginners (0.25x BW instead of 0.3x BW for bench press) will reduce plan modification rate and increase confidence. |
| **Control** | Current baseline ratios (0.3-0.5x BW for bench) |
| **Variant** | 15% lower baseline ratios across all exercises for beginners |
| **Primary Metric** | Plan modification rate for first workout (beginner cohort) |
| **Secondary Metrics** | Workout completion rate, `ai_plan_modified` count |
| **Sample Size** | ~2,600 per arm (8% MDE on 55% baseline modification rate); beginner cohort only |

#### Experiment 8: Progress Notification Cadence

| Field | Value |
|-------|-------|
| **Hypothesis** | Sending a weekly progress summary push notification will increase weekly retention and progress screen views. |
| **Control** | No progress notifications |
| **Variant A** | Weekly push notification with volume summary |
| **Variant B** | Weekly push notification with PR highlights only |
| **Primary Metric** | Weekly retention (week-over-week) |
| **Secondary Metrics** | `progress_viewed` frequency, notification open rate, uninstall rate |
| **Sample Size** | ~8,500 per arm (3% MDE on 65% weekly retention; 3 arms) |

#### Experiment 9: Mid-Workout Exercise Addition UX

| Field | Value |
|-------|-------|
| **Hypothesis** | A floating "+" button on the workout screen will increase mid-workout exercise additions compared to the current menu-based approach, indicating better discoverability. |
| **Control** | Exercise addition via overflow menu |
| **Variant** | Floating action button for adding exercises |
| **Primary Metric** | Mid-workout exercise addition rate (`exercise_added_midworkout` / workouts) |
| **Secondary Metrics** | Workout abandonment rate, total exercises per workout |
| **Sample Size** | ~6,400 per arm (5% MDE on 10% baseline addition rate) |

#### Experiment 10: Paywall Timing

| Field | Value |
|-------|-------|
| **Hypothesis** | Showing the paywall after the 3rd completed workout (instead of the 5th) will increase trial start rate without harming D30 retention. |
| **Control** | Paywall shown after 5th workout |
| **Variant** | Paywall shown after 3rd workout |
| **Primary Metric** | Trial start rate |
| **Guardrail Metric** | D30 retention (must not decrease by > 2%) |
| **Secondary Metrics** | Trial-to-paid conversion, uninstall rate within 48h of paywall |
| **Sample Size** | ~4,400 per arm (5% MDE on 15% trial start baseline) |

---

## 4. Cohort Analysis Plan

### 4.1 Cohort Definitions

| Cohort Dimension | Segments | Purpose |
|-----------------|----------|---------|
| **Install Week** | ISO week of `app_first_open` | Standard retention curves; compare cohort quality over time |
| **Experience Level** | `beginner`, `intermediate`, `advanced` | Understand if retention/engagement differs by training maturity |
| **Acquisition Channel** | `organic`, `google_ads`, `meta_ads`, `referral`, `influencer` | Measure channel quality beyond install volume |
| **First Workout Week** | ISO week of first `workout_completed` | Separate install timing from activation timing |
| **Plan Source** | `ai`, `fallback`, `manual` (from first workout) | Measure whether AI plan exposure drives better outcomes |
| **Template Adopter** | Users who save or load a template within 30 days vs. never | Measure template feature correlation with retention |
| **Workout Frequency Tier** | `light` (1/wk), `moderate` (2-3/wk), `heavy` (4+/wk) over first 30 days | Segment engagement levels for targeted interventions |

### 4.2 Cohort Comparisons

| Comparison | Method | Expected Insight |
|-----------|--------|-----------------|
| Install-week cohort retention curves (D1/D7/D30/D90) | Retention heatmap, week-over-week overlay | Detect if product changes or marketing shifts improve or degrade new-user quality |
| Beginner vs Intermediate vs Advanced retention | Segmented retention curves | Identify if a specific experience level churns faster; inform onboarding/plan calibration |
| AI-plan users vs Manual users | Matched cohort comparison (control for experience level) | Quantify the retention lift from AI plan usage |
| Template adopters vs Non-adopters | Survival analysis from first template event | Measure whether templates are a leading indicator of retention or just a power-user signal |
| Paid channel vs Organic cohorts | LTV curves segmented by channel | Determine true channel ROI beyond CPI |

### 4.3 Churn Prediction Model

**Objective:** Identify users at risk of churning within the next 14 days.

**Features (inputs):**

| Feature | Type | Description |
|---------|------|-------------|
| `days_since_last_workout` | numeric | Calendar days since most recent `workout_completed` |
| `workouts_last_14d` | numeric | Count of completed workouts in trailing 14 days |
| `workouts_last_14d_delta` | numeric | Change vs. prior 14-day period |
| `avg_session_duration_trend` | numeric | Slope of session duration over last 5 sessions |
| `ai_plan_modification_rate` | numeric | Fraction of plans modified over last 5 workouts |
| `workout_abandonment_rate` | numeric | Fraction of abandoned workouts over lifetime |
| `progress_views_last_7d` | numeric | Count of `progress_viewed` events in trailing 7 days |
| `personal_records_last_30d` | numeric | Count of PRs achieved in trailing 30 days |
| `experience_level` | categorical | User's self-reported level |
| `days_since_install` | numeric | Account age |
| `template_user` | boolean | Whether user has ever used templates |

**Model approach:**
- Start with logistic regression for interpretability
- Graduate to gradient-boosted trees (LightGBM) when data volume exceeds 10K users
- Retrain monthly on rolling 90-day windows
- Output: probability score 0-1, threshold at 0.7 for "at-risk" classification

**Interventions for at-risk users:**
- Push notification with personalized workout suggestion
- In-app message highlighting recent progress or approaching PR
- Email with training consistency streak status (if email captured)

### 4.4 Segmentation Model

**Primary segments:**

| Segment | Definition | Estimated % | Strategy |
|---------|-----------|-------------|----------|
| **Power Users** | 4+ workouts/week, use AI plans, track progress | ~10% | Upsell premium, gather feedback, encourage referrals |
| **Consistent Trainers** | 2-3 workouts/week, moderate feature usage | ~25% | Maintain engagement, introduce underused features |
| **Casual Users** | 1 workout/week or less, basic feature usage | ~30% | Re-engagement campaigns, simplify UX, reduce friction |
| **Dormant** | No workout in 14+ days, still installed | ~25% | Win-back push notifications, "we miss you" campaigns |
| **Churned** | No session in 30+ days | ~10% | Email win-back, app update notifications |

Segments are recalculated weekly. Users can transition between segments. Segment transitions are tracked as a leading indicator of overall product health.

---

## 5. Data Pipeline Architecture

### 5.1 Collection Layer

**Recommended: Firebase Analytics (Google Analytics for Firebase)**

Rationale:
- Zero cost at any scale for event collection
- Native Android SDK with offline event buffering (critical for gym environments)
- Automatic session tracking, user properties, and crash correlation
- BigQuery export for raw event-level data (free daily export)
- Tight integration with Firebase A/B Testing and Remote Config

Firebase Analytics handles event collection on-device, queues events when offline, and batch-uploads when connectivity returns. This aligns with the offline-first requirement.

**Supplementary: Firebase Crashlytics** for crash reporting, correlated with analytics events via `session_id`.

### 5.2 Data Warehouse

**Recommended: Google BigQuery**

Rationale:
- Free automatic daily export from Firebase Analytics
- Serverless, no infrastructure to manage
- Cost-effective at early scale (first 1 TB/month queries free)
- SQL interface familiar to analysts
- Integrates with every BI tool

**Schema structure:**
- `analytics_events` -- raw Firebase event export (daily partitioned, event-date clustered)
- `dim_users` -- user dimension table built from profile events and first-touch attribution
- `dim_exercises` -- exercise metadata from the app's exercise library
- `fact_workouts` -- transformed workout-level aggregations (one row per completed/abandoned workout)
- `fact_sets` -- transformed set-level data (one row per `set_completed`)
- `agg_daily_kpis` -- pre-aggregated daily KPI table for dashboard performance
- `agg_cohort_retention` -- pre-computed retention curves by cohort dimension

### 5.3 ETL / Transformation

**Recommended: dbt (data build tool) on BigQuery**

Rationale:
- SQL-based transformations, version-controlled in Git
- Automated testing and documentation
- Incremental models for cost-efficient processing
- Open source; hosted option (dbt Cloud) available if needed

**Pipeline schedule:**
- Raw Firebase export lands in BigQuery daily at ~midnight UTC (automatic)
- dbt runs at 02:00 UTC daily via Cloud Scheduler or GitHub Actions
- Transforms raw events into dimensional models and aggregation tables
- Tests run on every dbt execution (schema tests, row-count checks, freshness checks)

**Transformation flow:**
```
Firebase Analytics --> BigQuery (raw events, auto-exported daily)
                          |
                          v
                    dbt transformations
                          |
               +----------+----------+
               |          |          |
           dim_users  fact_workouts  agg_daily_kpis
               |          |          |
               v          v          v
                    BI Dashboard
```

### 5.4 Dashboard / BI Layer

**Recommended: Metabase (self-hosted) or Looker Studio (free tier)**

Phase 1 (launch): **Looker Studio**
- Free, connects directly to BigQuery
- Sufficient for initial dashboards
- Shareable with the entire team via Google accounts
- Limitation: limited interactivity, no alerting

Phase 2 (post-traction): **Metabase (self-hosted on Cloud Run)**
- Open-source, richer interactivity
- SQL-native, supports parameterized queries
- Embedding, alerting, and subscriptions
- Self-hosted cost: ~$30-50/month on Cloud Run

**Dashboard structure:**
1. **Executive Dashboard** -- DAU/WAU/MAU, retention, revenue, install trend (Looker Studio)
2. **Acquisition Dashboard** -- Install sources, CPI, organic ratio, Play Store metrics (Looker Studio)
3. **Engagement Dashboard** -- Workouts/user/week, session duration, feature adoption (Metabase)
4. **Retention Dashboard** -- Cohort heatmaps, D1/D7/D30/D90, churn prediction scores (Metabase)
5. **Revenue Dashboard** -- MRR, ARPU, trial conversion, LTV (Metabase)
6. **Experiment Dashboard** -- Active experiments, results, guardrail metrics (Firebase console + Metabase)

### 5.5 Alerting

| Alert | Condition | Channel | Recipient |
|-------|----------|---------|-----------|
| DAU drop | DAU decreases > 15% day-over-day | Slack | Product Owner, Data Analyst |
| Crash spike | Crash-free rate drops below 99% | Slack + PagerDuty | Lead Dev, DevOps |
| AI plan failure rate | `ai_plan_failed` > 10% of `ai_plan_requested` in 1-hour window | Slack | Lead Dev |
| Workout abandonment spike | Abandonment rate > 20% over 24h | Slack | Product Owner, Data Analyst |
| Trial conversion drop | Trial-to-paid conversion drops > 20% week-over-week | Email | Product Owner, Growth Marketing |

---

## 6. Privacy & Compliance

### 6.1 GDPR Compliance

Deep Reps processes personal data (workout history, body metrics, device info). Even without EU-specific targeting at launch, GDPR-grade compliance is the baseline standard.

**Lawful basis for processing:**
- **Contract performance** (Article 6(1)(b)): Processing workout data is necessary to deliver the core service
- **Legitimate interest** (Article 6(1)(f)): Analytics for product improvement, with user right to opt out
- **Consent** (Article 6(1)(a)): Required for optional data (age, gender, body weight) and marketing communications

**Data subject rights (implemented in-app):**
- **Right of access (Art. 15):** `data_export_requested` event triggers export of all user data in JSON format
- **Right to erasure (Art. 17):** Account deletion removes all personal data from local storage and backend within 30 days
- **Right to data portability (Art. 20):** Export includes workout history, profile data, and templates in machine-readable format
- **Right to restrict processing (Art. 18):** User can opt out of analytics while retaining full app functionality

### 6.2 Data Retention Policies

| Data Category | Retention Period | Justification |
|--------------|-----------------|---------------|
| Raw analytics events (BigQuery) | 14 months | Sufficient for year-over-year comparison with 2-month buffer |
| Aggregated KPI tables | Indefinite | No PII in aggregated data |
| User profile data | Until account deletion + 30 days | Service delivery + grace period for accidental deletion |
| Workout history (on-device) | Until user deletes or uninstalls | Core service data owned by user |
| Workout history (backend, if synced) | Until account deletion + 30 days | Service delivery |
| Crash reports | 90 days | Sufficient for debugging; contains device metadata |
| A/B test assignment logs | 6 months after experiment conclusion | Post-analysis and audit trail |

Automated deletion jobs run weekly in BigQuery to enforce retention windows.

### 6.3 Consent Framework

**First launch consent flow:**
1. Privacy policy presented (required read/scroll before proceeding)
2. Analytics consent toggle: "Help improve Deep Reps by sharing anonymous usage data"
   - Default: **OFF** (opt-in model for EU compliance)
   - If declined: Firebase Analytics collection disabled; only crash reporting retained (legitimate interest for app stability)
3. Optional profile data consent: Separate toggle per optional field

**Consent state tracked as user property:**
- `analytics_consent`: `granted` | `denied`
- `profile_data_consent`: `granted` | `denied`

Consent can be changed at any time in Settings. Changes take effect immediately.

### 6.4 Anonymization & Pseudonymization

| Measure | Implementation |
|---------|---------------|
| **User ID** | Firebase-generated anonymous ID; no PII in the identifier |
| **IP Address** | Firebase Analytics does not log IP addresses by default; IP-based geolocation (country level) stored as a property, IP itself discarded |
| **Device ID** | Android Advertising ID used only with consent; falls back to Firebase Instance ID |
| **Event properties** | No free-text user input (e.g., exercise notes) sent to analytics; only structured properties |
| **BigQuery access** | Role-based access via IAM; only Data Analyst and Product Owner have query access |
| **Dashboard access** | Read-only roles for team members; no raw-data access for non-analyst roles |
| **Data in transit** | TLS 1.2+ enforced for all analytics and API traffic |
| **Data at rest** | BigQuery default encryption (AES-256); no additional key management needed at this scale |

### 6.5 Google Play Data Safety Section

The following disclosures are required for the Play Store listing:

| Data Type | Collected | Shared | Purpose |
|-----------|----------|--------|---------|
| App activity (screens viewed, feature usage) | Yes (with consent) | No | Analytics, product improvement |
| App info and performance (crash logs, diagnostics) | Yes | No | App stability |
| Device info (model, OS version) | Yes (with consent) | No | Compatibility analysis |
| Health & fitness (workout data, body weight) | Yes | No | Core app functionality |
| Personal info (age, gender) | Yes (optional, with consent) | No | AI plan personalization |

---

## 7. Reporting Cadence

### 7.1 Daily Report (Automated, Slack)

**Audience:** Product Owner, Lead Dev, Data Analyst
**Delivery:** Automated Slack message at 09:00 local time
**Content:**

- DAU (absolute + day-over-day change)
- New installs (absolute + source breakdown)
- Workouts completed (absolute + per-user average)
- Workout abandonment rate
- AI plan failure rate
- Crash-free rate
- Any triggered alerts from prior 24h

Format: Single Slack message with inline numbers. No charts. Designed to scan in 15 seconds.

### 7.2 Weekly Report (Monday)

**Audience:** Full team
**Delivery:** Slack channel post + linked Looker Studio/Metabase dashboard
**Content:**

- **Acquisition:** WAU, new installs by source, CPI by channel, organic ratio
- **Activation:** Onboarding completion rate, first-workout completion rate (7-day window), time to first workout (median)
- **Engagement:** Workouts/user/week, avg workout duration, avg sets/workout, feature adoption rates (AI plan, templates, rest timer, progress views)
- **Retention:** D1/D7 retention for last week's install cohort, weekly rolling retention, resurrection count
- **Revenue (post-monetization):** Weekly MRR, trial starts, trial-to-paid conversion, refund count
- **Product health:** Crash-free rate, AI plan latency (p50/p95), top 3 crash signatures
- **Experiment status:** Active experiments with interim results (directional only, no decisions)
- **Top insight:** One actionable finding from the week's data, with recommended action

### 7.3 Monthly Report (First Monday of Month)

**Audience:** Product Owner, Growth Marketing, Data Analyst
**Delivery:** Written document (Google Doc or Notion page) + team meeting presentation
**Content:**

- **Executive summary:** 3-5 bullet points covering the month's story
- **Cohort retention analysis:** Month's install cohort D7/D30 compared to prior 3 months
- **Acquisition deep-dive:** Channel performance, CPI trends, organic growth rate, Play Store conversion rate changes
- **Engagement trends:** 4-week rolling averages for all engagement KPIs, feature adoption changes
- **Churn analysis:** Churn rate by segment, top churn predictors from model, intervention effectiveness
- **Experiment results:** Completed experiments with statistical results and shipping recommendations
- **Revenue analysis (post-monetization):** MRR growth, LTV by cohort, ARPU trend, paywall funnel analysis
- **Segmentation update:** Segment sizes, migration between segments, notable movements
- **Next month priorities:** Data-informed recommendations for product and growth teams

### 7.4 Quarterly Business Review (QBR)

**Audience:** Full team + stakeholders/investors (if applicable)
**Delivery:** Slide deck + recorded presentation
**Content:**

- **Quarter summary:** Key metrics vs. targets set at quarter start
- **Retention deep-dive:** 90-day retention curves for the quarter's cohorts, comparison to industry benchmarks (fitness app median D30 retention ~12-15%)
- **LTV analysis:** Updated LTV model with actuals vs. projections
- **Channel efficiency:** Full-funnel unit economics by acquisition channel (CPI -> activation rate -> LTV)
- **Product-market fit signals:** DAU/MAU trend, NPS (if collected), qualitative user feedback themes
- **Experimentation review:** All experiments run, win rate, cumulative metric impact
- **Churn deep-dive:** Root cause analysis of top churn drivers with user research correlation
- **Competitive landscape:** How Deep Reps metrics compare to publicly available benchmarks from competitors
- **Next quarter targets:** Specific KPI targets with assumptions and risk factors
- **Data infrastructure health:** Pipeline reliability, data freshness SLA adherence, outstanding data quality issues

---

## Appendix A: Implementation Priority

| Priority | Items | Timeline |
|----------|-------|----------|
| **P0 (Pre-launch)** | Events 1-6 (onboarding), 23/35/36 (workout start/complete/abandon), 48-50 (app lifecycle), Firebase setup, BigQuery export, consent framework | Before first beta user |
| **P1 (Launch)** | Events 7-22 (setup + AI), 24-34/37 (active workout detail), daily + weekly reports, Looker Studio executive dashboard | Launch week |
| **P2 (Post-launch week 2-4)** | Events 38-47 (progress + profile), retention dashboard, cohort analysis, first A/B test | Weeks 2-4 |
| **P3 (Post-launch month 2+)** | Churn prediction model, segmentation, Metabase migration, experiment velocity ramp-up, revenue dashboards | Month 2+ |

## Appendix B: Event Validation Checklist

Before shipping any new event:

- [ ] Event name follows `object_action` convention
- [ ] All properties have defined types and allowed values
- [ ] Event fires in correct sequence (e.g., `workout_started` before `set_completed`)
- [ ] Event does not contain PII or free-text user input
- [ ] Event is tested on a physical device in offline and online modes
- [ ] Event appears in BigQuery debug view within 24 hours
- [ ] Event is documented in the team's event dictionary (this document)
- [ ] Dashboard query referencing this event has been verified
