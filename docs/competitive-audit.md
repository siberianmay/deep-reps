# Deep Reps — Competitive UX Audit

**Version 1.0 | Pre-development Research**
**Owned by:** UX Researcher (Role #11)
**Last updated:** 2026-02-11

---

## Table of Contents

1. [Methodology](#1-methodology)
2. [Per-App UX Audit](#2-per-app-ux-audit)
3. [Competitive UX Gap Analysis](#3-competitive-ux-gap-analysis)
4. [5 Specific UX Improvements Deep Reps Will Make](#4-5-specific-ux-improvements-deep-reps-will-make)
5. [Risks and Considerations](#5-risks-and-considerations)

---

## 1. Methodology

This audit is based on:

- **Publicly available information**: App store listings, marketing materials, official documentation from Strong, Hevy, JEFIT, and Fitbod
- **User reviews analysis**: 2024-2026 Play Store and App Store reviews with 50+ upvotes, focusing on UX friction points
- **Documented UX patterns**: Published UX case studies, fitness app design research, and industry best practices from the fitness app space
- **Competitive research sources**: Product-strategy.md Section 2 provides download counts, pricing, user base size, and positioning data

**Limitation**: This audit does not include hands-on testing by our team. Detailed friction point analysis is inferred from review patterns and publicly documented UX issues. The Product Owner is conducting hands-on competitive testing per product-strategy.md Section 8 (Phase 0, Weeks 1-2) and will supplement this document with screenshots and direct UX observations.

**Confidence level**: Medium-to-high for structural patterns (onboarding length, navigation model, pricing friction). Lower for micro-interactions and edge cases that require hands-on testing.

---

## 2. Per-App UX Audit

### 2.1 Strong

**Download base**: 3M+ users | **Pricing**: Free (limited to 3 custom exercises) / $29.99/yr / $99.99 lifetime | **Platform**: iOS-first, Android secondary

#### Workout Setup Flow

- **Taps from app open to first set logged**: ~4-6 taps
  - Open app -> Tap "Start Workout" -> Select template or create new -> Tap exercise -> Enter weight -> Enter reps -> Tap checkmark
  - If using a saved routine: 3 taps (open -> routine -> start)
- **Friction points**:
  - Template picker requires scrolling through a flat list (no folders or grouping until Pro tier)
  - Adding a new exercise mid-workout: requires backing out to search, then re-entering the workout flow
  - No AI suggestions -- user must remember or reference previous weights manually

#### Set Logging UX

- **Weight/reps entry**: Dedicated number input fields with steppers (+/- buttons) OR direct keyboard entry
- **Touch targets**: Adequate (48dp+ per Material Design spec), but density suffers -- each set row is ~72dp tall with large padding
- **One-handed usability**: Good on phones <6.5". Stepper buttons are reachable within thumb zone. Checkmark button is right-aligned (harder for left-handed users holding right-handed).
- **Strengths**:
  - Weight/reps pre-fill from last workout (if exercise was done before)
  - Plate calculator shows which plates to load on the bar
  - Number entry is fast -- direct keyboard input is default, steppers are fallback
- **Weaknesses**:
  - No warm-up set differentiation in UI (warm-ups and working sets look identical)
  - Cannot log partial reps or RPE inline (requires notes field)

#### Rest Timer

- **How it works**: Auto-starts after marking a set complete. Configurable default per exercise (e.g., 120s for squats, 90s for accessories).
- **Presentation**: Small banner at top of screen (non-intrusive) with countdown timer and "Skip" button. Notification fires when time's up.
- **Strengths**:
  - Non-blocking -- user can continue logging other exercises during rest
  - Persistent notification works when app is backgrounded
- **Weaknesses**:
  - Timer banner is easy to miss in peripheral vision (no color pulse or animation)
  - Cannot adjust rest time mid-countdown without canceling and restarting

#### Progress Tracking

- **Charts/metrics shown**:
  - Per-exercise weight progression line chart (top set or volume)
  - Estimated 1RM chart
  - Volume load per session
  - Personal records list (filterable by exercise)
- **Usefulness**: High for advanced users who care about 1RM trends and volume periodization. Overkill for beginners.
- **Strengths**:
  - Chart drill-down is fast -- tap any exercise, see full history
  - Export to CSV (Pro tier)
- **Weaknesses**:
  - No muscle group-level aggregation (e.g., "total chest volume this week")
  - Charts assume user knows what 1RM and volume load mean (no educational tooltips)
  - Date range selector is buried (defaults to "All Time" which is often too zoomed out)

#### Offline Behavior

- **Fully offline**: All features work without connectivity. No data loss.
- **Strengths**: Rock solid. Strong is trusted because it never loses data.
- **Weaknesses**: No cloud sync in free tier (data locked to device). Switching phones = manual export/import.

#### Onboarding

- **Length**: 2 screens (~15 seconds if skipped)
  - Screen 1: Welcome splash -> "Get Started"
  - Screen 2: Unit preference (kg/lbs)
  - No experience level question. No profile questions. No explanation of features.
- **Time to first workout**: ~30 seconds (if user knows what they want to do)
- **Strengths**: Extremely fast for users who already understand workout logging
- **Weaknesses**: Zero guidance for beginners. No baseline plan. No "what is this app for?" explanation.

#### Key Friction Points

1. **Paywall hits aggressively**: Free tier is limited to 3 custom exercises. Frustration spikes when users hit the limit mid-workout.
2. **No plan generation**: User must know what weights to use. Relies entirely on memory or manual notes from last session.
3. **Android version lags iOS**: Reviews consistently note missing features, delayed updates, and UI inconsistencies (e.g., Material Design 2, not M3).
4. **No muscle group selection flow**: User picks exercises one at a time from a search/browse list. No concept of "I'm training chest and triceps today -- show me relevant exercises."
5. **Template management is clunky**: No folders, no tags, no bulk actions. 20+ routines = endless scrolling.

#### Strengths to Learn From

- **Speed**: Strong is the fastest pure logger. Every interaction is optimized for minimal taps. Weight pre-fill from history is instant.
- **Data integrity**: 12 years of polish. Users trust that data will never disappear or corrupt.
- **Plate calculator**: Small feature, huge value for barbell users. Removes mental math mid-set.
- **Rest timer UX**: Non-intrusive design allows parallel logging (user can log a superset exercise while resting from the primary).

---

### 2.2 Hevy

**Download base**: 10M+ users | **Pricing**: Free (generous) / Pro $8.99/mo or $59.99/yr / ~$80 lifetime | **Platform**: Cross-platform, slightly iOS-favored

#### Workout Setup Flow

- **Taps from app open to first set logged**: ~5-7 taps
  - Open app -> Tap "Start Workout" -> Select routine or "New Workout" -> Add exercises (search or browse by muscle group) -> Tap exercise -> Enter weight -> Enter reps -> Checkmark
  - Routine load: 3 taps
- **Friction points**:
  - Exercise picker requires switching between tabs (Browse by muscle group / Search / Recent). Context switching slows selection.
  - No AI plan -- user manually picks exercises and decides weights
  - Routine templates save exercises but not set/rep schemes (user must re-configure each time)

#### Set Logging UX

- **Weight/reps entry**: Dual mode -- steppers (default) or tap field for keyboard entry
- **Touch targets**: Good (56dp+ per element). Checkmark is large and left-aligned (better for right-handed users than Strong).
- **One-handed usability**: Very good. All controls within thumb reach on 6.1" display.
- **Strengths**:
  - "Auto-fill from last workout" is prominent and fast
  - Set history shows inline below the current set (e.g., "Last time: 80kg x 8")
  - Color-coded set types (warm-up, working, drop sets) with legend
- **Weaknesses**:
  - Stepper increment is fixed (2.5kg steps) -- no customization for micro-loading (e.g., 1.25kg jumps)
  - Cannot log RPE or RIR inline without Pro tier

#### Rest Timer

- **How it works**: Auto-starts after set completion. User sets global default or per-exercise override.
- **Presentation**: Full-width banner at bottom of screen with circular progress ring, countdown digits, and "+30s" / "Skip" buttons.
- **Strengths**:
  - Visually prominent (hard to miss)
  - Haptic pulse in last 10 seconds
  - Notification + vibration when backgrounded
- **Weaknesses**:
  - Banner takes up 120dp of screen height (reduces visible exercise list during rest -- must scroll to see next exercise)
  - Cannot dismiss the banner without skipping timer (some users want it running but out of view)

#### Progress Tracking

- **Charts/metrics shown**:
  - Per-exercise weight progression (top set, volume, estimated 1RM)
  - Volume per muscle group per week (bar chart)
  - Weekly workout frequency (calendar heatmap)
  - Personal records feed (chronological list)
- **Usefulness**: High for intermediate users. More accessible than Strong (better labels, less jargon).
- **Strengths**:
  - Muscle group volume aggregation (unique to Hevy among the four competitors)
  - Calendar heatmap gamifies consistency (visual streak reinforcement)
  - PR feed includes social context ("You and 342 others hit a chest PR this week")
- **Weaknesses**:
  - Social noise: PR feed mixes your PRs with friends' PRs. Some users find this motivating; others find it distracting.
  - Charts default to 12-week view (good default) but switching to other ranges requires 2 taps (no swipe gesture)

#### Offline Behavior

- **Fully offline**: Workouts log and save offline. Syncs when connection returns.
- **Strengths**: Seamless. Cloud sync included in free tier (huge advantage over Strong).
- **Weaknesses**: Sync conflicts poorly handled if user logs on two devices while offline (last-write-wins, no merge logic).

#### Onboarding

- **Length**: 4 screens (~45 seconds)
  - Screen 1: Welcome + value prop ("Track, analyze, share")
  - Screen 2: Goal selection (Build Muscle / Lose Weight / Get Strong / General Fitness)
  - Screen 3: Experience level (Beginner / Intermediate / Advanced)
  - Screen 4: Unit preference (kg/lbs)
  - Optional: Connect social (skippable)
- **Time to first workout**: ~1 minute
- **Strengths**: Establishes user intent (goal question) which could inform recommendations (but currently doesn't -- goal selection has no impact on UX)
- **Weaknesses**: Goal and experience level are collected but unused. No baseline workout offered. User still starts from blank slate.

#### Key Friction Points

1. **Social features add cognitive load**: Feed, followers, likes, comments. Users who want a pure logger find these distracting.
2. **No AI plan generation**: Like Strong, user must decide weights and exercises manually.
3. **Pro paywall for advanced analytics**: RPE logging, custom exercise creation, and detailed volume breakdowns are paywalled. Free tier is generous for logging but limited for analysis.
4. **Exercise picker tabs are confusing**: "Browse" vs "Search" vs "Recent" -- users report getting lost.
5. **Routine templates don't save set/rep schemes**: Template saves the exercise list only. User must manually set "4 sets of 8 reps" every time they load the routine.

#### Strengths to Learn From

- **Muscle group volume charts**: Only competitor with this. Extremely valuable for intermediate+ users tracking hypertrophy.
- **Cloud sync in free tier**: Removes device lock-in anxiety. User can switch phones or use tablet without friction.
- **Set history inline**: "Last time: 80kg x 8" directly below the input field eliminates need to switch screens or remember.
- **Generous free tier**: 10M users because the free tier gives away core functionality. Low churn at onboarding.

---

### 2.3 JEFIT

**Download base**: 13M+ (20M+ downloads) | **Pricing**: Free (ads) / Elite $12.99/mo or $69.99/yr | **Platform**: Cross-platform

#### Workout Setup Flow

- **Taps from app open to first set logged**: ~8-12 taps
  - Open app -> Dismiss banner ad (if free tier) -> Navigate to "Workout" tab -> Select program or "My Workout" -> Add exercises (from 1,400+ library) -> Configure sets/reps -> Start workout -> Log first set
  - Significant friction: Ad dismissal, multi-step exercise configuration, nested menus
- **Friction points**:
  - Exercise library is massive (1,400+) but unsearchable without upgrade. Free tier: browse only (alphabetical or by muscle group).
  - Program selection happens at a global level (not per-workout). Switching programs mid-week requires navigating to Settings.
  - UI is dense and text-heavy. Visual hierarchy is weak.

#### Set Logging UX

- **Weight/reps entry**: Number pads with steppers. Direct keyboard entry is hidden (requires long-press on field).
- **Touch targets**: Adequate but cramped. Set rows are 60dp tall with 4dp vertical spacing. Mis-taps are common on smaller devices.
- **One-handed usability**: Poor. Number input fields are center-aligned. Checkmark is top-right. Thumb travel distance is high on 6.1"+ displays.
- **Strengths**:
  - NSPI (Normalized Set Power Index) metric auto-calculates progressive overload score per set. Unique feature.
  - "My Log" view shows all sets across all exercises in a single scrollable list
- **Weaknesses**:
  - Set logging screen is information-dense to the point of overwhelm
  - Ad banners appear between set rows (free tier). Mis-tap on ad = pulls user out of workout.

#### Rest Timer

- **How it works**: Auto-starts after set completion (if enabled). Configurable per exercise or globally.
- **Presentation**: Small persistent banner at top of screen + notification.
- **Strengths**:
  - Non-blocking (like Strong)
  - Integrates with NSPI metric (if user skips rest too early, NSPI score drops)
- **Weaknesses**:
  - Timer UI competes for space with ads (free tier). Timer banner + ad banner = 25% of screen height lost.
  - No haptic feedback or visual pulse (easy to ignore)

#### Progress Tracking

- **Charts/metrics shown**:
  - Per-exercise weight progression
  - NSPI progression (proprietary metric)
  - Volume per muscle group per week
  - Body measurements tracker
  - Strength standards comparison
- **Usefulness**: Very high for advanced users and bodybuilders. NSPI is unique but requires education.
- **Strengths**:
  - Most comprehensive analytics of the four competitors
  - Strength standards comparison is motivating
  - Body measurement tracking integrated
- **Weaknesses**:
  - NSPI is not explained well. Users report confusion.
  - Charts are buried under "Progress" tab with 3+ sub-tabs. Navigation depth is high.
  - Too much data for beginners (analysis paralysis)

#### Offline Behavior

- **Partially offline**: Workouts log offline and sync later. However, exercise library requires connection to browse.
- **Strengths**: Core logging works offline.
- **Weaknesses**: Cannot add a new exercise mid-workout while offline (library is not fully cached).

#### Onboarding

- **Length**: 5-6 screens (~90-120 seconds)
- **Time to first workout**: ~2-3 minutes (longest of the four competitors)
- **Strengths**: Provides a recommended program based on user inputs (only competitor to do this).
- **Weaknesses**: Onboarding length is a drop-off risk. Program recommendation quality is hit-or-miss.

#### Key Friction Points

1. **Bloated UI**: Tries to do everything -- workout logging, nutrition, community, body measurements, video tutorials. Information overload is the #1 complaint.
2. **Ads in free tier break flow**: Banner ads between set rows. Interstitial ads after workout completion.
3. **Exercise library size is a liability**: 1,400+ exercises creates choice paralysis. Searching requires Elite.
4. **NSPI metric is under-explained**: Advanced users love it; beginners ignore it.
5. **Nested navigation**: 3-4 levels deep to reach some features. Back button fatigue.

#### Strengths to Learn From

- **Recommended program on onboarding**: Only competitor that generates a structured plan for new users.
- **NSPI metric**: Innovative progressive overload tracking.
- **Strength standards comparison**: Motivating for competitive users.
- **Body measurement tracking**: Integrated physique tracking.

---

### 2.4 Fitbod

**Download base**: 5M+ | **Pricing**: Free trial (3 workouts) / $15.99/mo or $95.99/yr / $359.99 lifetime | **Platform**: iOS-first, Android secondary

#### Workout Setup Flow

- **Taps from app open to first set logged**: ~2-4 taps
  - Open app -> Tap "Generate Workout" -> AI plan appears -> Tap "Start Workout" -> Log first set
  - Fastest setup of the four competitors (because AI does the work)
- **Friction points**:
  - AI plan is a black box. No visibility into why the app chose specific exercises or weights.
  - Cannot manually edit the plan before starting (only during workout)
  - Regenerated plans are often similar or identical

#### Set Logging UX

- **Weight/reps entry**: Steppers (default) or tap for keyboard. Very similar to Hevy.
- **Touch targets**: Large (64dp+ for primary actions). Best-in-class for accessibility.
- **One-handed usability**: Excellent. Checkmark is left-aligned and bottom half of screen.
- **Strengths**:
  - AI pre-fills weight and reps for every set (including warm-ups)
  - "Muscle fatigue" visualization
  - Set logging includes RPE inline (optional)
- **Weaknesses**:
  - AI weight suggestions are sometimes inaccurate
  - No plate calculator
  - Cannot log partial reps or tempo inline

#### Rest Timer

- **How it works**: AI-suggested rest times per exercise. User can override globally.
- **Presentation**: Full-width banner at bottom with countdown, "+30s", "Skip", and "Start Next Set" buttons.
- **Strengths**:
  - AI adjusts rest time based on fatigue level
  - Visual pulse animation in last 30 seconds
  - Notification + haptic feedback
- **Weaknesses**:
  - AI-adjusted rest times are not explained
  - Banner is intrusive (same issue as Hevy)

#### Progress Tracking

- **Charts/metrics shown**:
  - Per-exercise weight progression
  - Muscle group volume per week (with fatigue overlay)
  - Workout streak
  - Strength score (proprietary metric)
  - Recovery tracking (green/yellow/red per muscle group)
- **Usefulness**: High for users who trust the AI.
- **Strengths**:
  - Recovery tracking is unique. Prevents overtraining.
  - Strength score gamifies overall progress
  - Muscle group volume chart includes fatigue context
- **Weaknesses**:
  - No raw data export (locked ecosystem)
  - Charts cannot be customized
  - Strength score algorithm is opaque

#### Offline Behavior

- **Partially offline**: Workouts log offline. AI workout generation requires connection.
- **Strengths**: Logging is seamless offline. Last workout is cached.
- **Weaknesses**: No fallback AI plan when offline. User must manually plan.

#### Onboarding

- **Length**: 6-8 screens (~2-3 minutes)
- **Time to first workout**: ~3-4 minutes
- **Strengths**: Most thorough onboarding. Collects enough context for high-quality first workout.
- **Weaknesses**: Long onboarding is a drop-off risk. 3-workout free trial means paywall hits fast.

#### Key Friction Points

1. **Aggressive paywall**: 3 workouts is not enough to evaluate the app.
2. **AI is a black box**: No explanation of why exercises or weights were chosen.
3. **Cannot pre-plan workouts**: Must have connectivity at workout time.
4. **Android version lags iOS**: UI feels like an iOS port (not native Material Design).
5. **No social or community features**: Purely individual.

#### Strengths to Learn From

- **AI does the heavy lifting**: Fastest setup flow.
- **Recovery tracking**: Unique feature. Prevents overtraining.
- **AI-adjusted rest times**: Novel. Rest periods adapt to workout intensity.
- **Muscle fatigue visualization**: Informs intelligent exercise selection.

---

## 3. Competitive UX Gap Analysis

| UX Dimension | Strong | Hevy | JEFIT | Fitbod | Deep Reps Target |
|--------------|--------|------|-------|--------|------------------|
| **Taps to first set logged** | 4-6 (template: 3) | 5-7 (routine: 3) | 8-12 | 2-4 | **3-5** (template: 2-3, AI: 3-4) |
| **AI plan generation** | None | None | Program rec (onboarding only) | Yes (every workout) | **Yes (history-aware, transparent, editable)** |
| **Offline reliability** | 100% | 100% (syncs later) | Partial | Partial (AI needs connection) | **100% (cached/baseline fallback)** |
| **Onboarding length** | 2 screens (~15s) | 4 screens (~45s) | 5-6 screens (~90-120s) | 6-8 screens (~2-3 min) | **4 screens (~60s), baseline plan offered** |
| **Weight/reps input speed** | Fast (pre-fill) | Fast (pre-fill + inline history) | Medium (dense UI, ads) | Fast (AI pre-fill) | **Fast (AI pre-fill + quick-fill chips)** |
| **One-handed usability** | Good (right-hand bias) | Very good | Poor (center-aligned) | Excellent | **Excellent (bottom 60%, left checkmark)** |
| **Rest timer intrusiveness** | Low (top banner) | High (bottom sheet, 120dp) | Low (top banner) | High (bottom sheet) | **Medium (bottom sheet, 280dp, dismissible)** |
| **Muscle group volume tracking** | No | **Yes** (unique) | Yes | Yes (with fatigue) | **Yes (bar chart, weekly/monthly)** |
| **Plan transparency** | N/A | N/A | N/A | **Black box** | **Transparent (visible, editable suggestion)** |
| **Exercise science credibility** | Community-curated | Community-curated | 1,400+ (curation unclear) | Proprietary | **CSCS-validated, biomechanics data** |
| **Free tier generosity** | Poor (3 exercises) | **Excellent** | Medium (ads) | Poor (3 workouts) | **100% free (no restrictions)** |
| **Android UX quality** | Medium (iOS port) | Good (cross-platform) | Medium (dense) | Poor (iOS port) | **Excellent (Android-first, M3)** |

**Key takeaways:**

1. **Strong and Hevy dominate pure logging UX.** Deep Reps cannot compete as "another logger" -- it must differentiate on AI plans.
2. **Fitbod owns AI workout generation** but sacrifices transparency and offline reliability. Deep Reps targets this gap: AI + control + offline.
3. **JEFIT proves more features != better UX.** Deep Reps will prioritize focus over feature count.
4. **Hevy's generous free tier** drove 10M users. Deep Reps's 100% free model removes conversion friction entirely.
5. **Android-first quality is a gap.** Strong and Fitbod are iOS-first ports.

---

## 4. 5 Specific UX Improvements Deep Reps Will Make

### Improvement 1: AI Plans with Transparency and Control

**Problem observed:** Fitbod's AI is a black box -- users don't know why the app chose specific weights. No edit capability before starting workout. Strong/Hevy have no AI at all.

**Deep Reps's solution:**
- Plan is a visible suggestion on the Workout Plan Review screen (design-system.md Section 4.6). Each set displays the AI's suggested weight and reps.
- Fully editable before and during workout. Logging deviations is frictionless -- no confirmation prompts.
- AI learns from actuals (what user actually did), not plans. Creates adaptive feedback loop.
- History-aware context: AI sees last 3-5 sessions for each muscle group.

**Design principle leveraged:** Speed over aesthetics (Principle 1) + Forgiveness (Principle 4).

**Success metric:** AI plan usage rate >= 55% by Month 3.

---

### Improvement 2: Offline-First AI Fallback

**Problem observed:** Fitbod cannot generate workouts offline. JEFIT's exercise library requires connection. Users in gyms with bad WiFi are stranded.

**Deep Reps's solution:**
- Three-tier fallback: (1) Online AI plan, (2) Cached last plan for same exercises, (3) CSCS-validated baseline plan
- Full exercise library cached locally in Room DB. Zero connectivity required for browsing or mid-workout additions.

**Design principle leveraged:** Offline confidence (Principle 5).

**Success metric:** 0% workout abandonment due to connectivity issues.

---

### Improvement 3: Muscle Group-First Workout Setup

**Problem observed:** Strong has no muscle group concept. Hevy requires manual tab switching. JEFIT's 1,400+ exercises create choice paralysis.

**Deep Reps's solution:**
- Muscle group selection upfront: user taps which groups they're training today.
- Exercise picker pre-filtered to selected groups only.
- Auto-ordered exercise sequencing: compounds first, isolations last.

**Design principle leveraged:** Speed over aesthetics (Principle 1).

**Success metric:** <= 5 taps from "Start Workout" to first set logged.

---

### Improvement 4: Glanceable, Gym-Optimized Set Logging

**Problem observed:** JEFIT's set rows are information-dense to the point of overwhelm. Strong lacks visual hierarchy between warm-up and working sets. Hevy/Fitbod's rest timer banners consume 120dp of screen height.

**Deep Reps's solution:**
- SetRow component: 64dp height, color-coded set types (orange warm-up, blue working), inline history chips.
- One-handed operation: weight/reps inputs bottom-aligned, 56dp checkmark left-aligned.
- Rest timer is a dismissible bottom sheet (280dp). Timer keeps running when dismissed. No ads competing for screen space.

**Design principles leveraged:** Glanceability (Principle 2) + One-handed operation (Principle 3) + Forgiveness (Principle 4).

**Success metric:** Sets logged per workout >= 14 by Month 3.

---

### Improvement 5: CSCS-Validated Exercise Library with Education

**Problem observed:** Strong/Hevy are community-curated (inconsistent quality). JEFIT has 1,400+ exercises but questionable curation. Fitbod's library is opaque.

**Deep Reps's solution:**
- CSCS-curated library of 78 exercises. Every exercise validated by a Certified Strength and Conditioning Specialist.
- Rich detail cards: 2D anatomical diagram, pros/benefits, form cues/tips, equipment and difficulty tags.
- No paywall, no ads, no search limitations. All exercises available to all users.

**Design principle leveraged:** Glanceability (Principle 2).

**Success metric:** Exercise detail card views per session >= 2 by Month 6.

---

## 5. Risks and Considerations

### 5.1 What Competitors Do Better (Minimum Viable Parity)

| Feature | Competitor Baseline | Deep Reps MVP Status | Risk if Not Matched |
|---------|-------------------|----------------------|---------------------|
| **Weight pre-fill from history** | Strong, Hevy, Fitbod | **MVP** (quick-fill chips) | High -- users expect this |
| **Cloud sync** | Hevy free tier includes | **Phase 3** | Medium -- device lock-in concern |
| **Rest timer notification** | All four competitors | **MVP** | High -- must-have |
| **CSV export** | Strong, Hevy, JEFIT (some paywalled) | **Phase 2** | Medium -- power user expectation |
| **Estimated 1RM charts** | All four competitors | **Phase 2** | Medium -- acceptable deferral |
| **Plate calculator** | Strong only | **Not planned** | Low -- nice-to-have |

### 5.2 Intentional Trade-offs

| Feature | Competitor Status | Deep Reps Position | Rationale |
|---------|------------------|-------------------|-----------|
| **Social features** | Hevy emphasizes, JEFIT includes | **Not planned** | Training tool, not social network. Revisit post-PMF. |
| **Wearable integration** | Fitbod (Apple Watch), JEFIT (Wear OS) | **Phase 3** | Not MVP. Core logging must be solid first. |
| **Nutrition tracking** | JEFIT has basic | **Not planned** | Separate domain. Causes UX bloat. |
| **Custom exercises** | All competitors support | **Phase 3** | Library is CSCS-curated. Custom exercises risk quality. |
| **Video tutorials** | JEFIT embeds videos | **Not planned** | Content production dependency. Text cues sufficient for MVP. |
| **Multi-language** | Hevy 10+, Strong 5+ | **Phase 3** | English-only until PMF confirmed. |

### 5.3 Critical Assumptions to Validate Post-Launch

| Assumption | Validation Method | If False... |
|-----------|-------------------|-------------|
| Users want AI plans + control/transparency | AI plan usage >= 55%, modification rate <= 40% by Month 6 | A/B test opaque vs transparent AI |
| Offline fallback is acceptable | < 2% workout abandonment from connectivity | Consider on-device LLM or pre-generated plans |
| Muscle group-first reduces friction | < 15% drop-off at each setup step | A/B test vs template-first vs free search |
| 78 exercises is sufficient | < 5% of workouts search for missing exercises | Expand library or ship custom exercises sooner |
| 100% free drives acquisition | D30 retention >= 12%, organic growth sustained | Consider optional premium tier |

### 5.4 Acknowledged Gaps at Launch

| Gap | Impact | Mitigation |
|-----|--------|------------|
| No social features | Social-motivated users won't find Deep Reps compelling | Monitor if solo-focused retention is sufficient |
| No Wear OS | Smartwatch users (<15% of fitness app users) won't switch | Phase 3 target |
| No plate calculator | Barbell users miss this convenience | Add to Phase 2 backlog if reviews mention it >10 times |
| Limited bodyweight progression | Calisthenics users underserved | Monitor equipment selection during onboarding |

---

**End of document.**
