# Deep Reps -- Design System

Version 1.1 | Pre-development Specification
Owned by: Senior UI/UX Designer (Role #4)

**CRITICAL: Deep Reps is a FREE app.** No subscription UI, no paywall screens, no Pro tier gating, no trial prompts. All features are available to all users.

---

## Table of Contents

1. [Design Philosophy](#1-design-philosophy)
2. [Design Tokens](#2-design-tokens)
3. [Component Library](#3-component-library)
4. [Screen Specifications](#4-screen-specifications)
5. [Interaction Specifications](#5-interaction-specifications)
6. [Navigation Architecture](#6-navigation-architecture)
7. [Accessibility](#7-accessibility)
8. [Responsive Layout](#8-responsive-layout)

---

## 1. Design Philosophy

### 1.1 Core Principles

1. **Speed over aesthetics.** Every interaction during an active workout must complete in 2 taps or fewer. Visual polish never comes at the cost of input speed.
2. **Glanceability.** Data must be readable from arm's length (phone on a bench or floor). Large type, high contrast, minimal visual noise.
3. **One-handed operation.** All primary workout actions must be reachable with the thumb on a 6.1" display held in one hand. Critical controls sit in the bottom 60% of the screen.
4. **Forgiveness.** Sweaty hands hit wrong targets. Every destructive action (delete set, discard workout) requires confirmation. Every edit is reversible.
5. **Offline confidence.** The UI never shows loading spinners for locally stored data. Online-dependent features (AI plan generation) clearly indicate network dependency before the user commits to the action.

### 1.2 Material Design 3 Alignment

Deep Reps is built on Material Design 3 (Material You) with the following stance:

- **Dynamic color is disabled.** The app uses a fixed brand palette. Wallpaper-derived theming creates unpredictable contrast in gym lighting conditions (dim, fluorescent, mixed). A controlled palette ensures readability.
- **Material 3 components are used as the base.** Buttons, cards, text fields, navigation bars, sheets, and dialogs follow M3 specs unless this document explicitly overrides them.
- **Shape system** uses M3's shape scale with custom overrides for key components (SetRow gets sharper corners for density; cards get medium rounding for visual softness).

### 1.3 Dark Theme Primary

Dark theme is the **default and primary** design surface. Rationale:

- Gym environments are often dimly lit. A bright screen is physically uncomfortable and socially conspicuous.
- Dark backgrounds reduce battery drain on OLED panels, which dominate Android flagships.
- High-contrast data on dark surfaces is the established visual language of fitness apps (Strong, Hevy, Fitbod). Users expect it.

Light theme is supported as an accessibility option but is secondary. All design work begins in dark theme; light theme is derived, not designed independently.

### 1.4 Visual Identity

- **Brand character:** Disciplined, dense, functional. Not playful, not minimalist-to-the-point-of-empty. Closer to a cockpit instrument panel than a meditation app.
- **Typography voice:** Bold headings, medium-weight body text. No thin/light weights in workout screens -- they disappear under gym lighting.
- **Color voice:** A single vibrant accent color (electric blue) against neutral dark surfaces. Secondary accents are used sparingly for status (green = complete, amber = warning, red = destructive).
- **Iconography:** Material Symbols Rounded, weight grade 400, optical size 24. Custom icons only for muscle group illustrations and exercise diagrams.

---

## 2. Design Tokens

### 2.1 Color Palette

#### 2.1.1 Dark Theme (Primary)

| Token | Hex | Usage |
|-------|-----|-------|
| `surface-lowest` | `#0A0A0F` | App background, system bars |
| `surface-low` | `#121218` | Card backgrounds, bottom sheet |
| `surface-medium` | `#1A1A22` | Elevated cards, active exercise container |
| `surface-high` | `#24242E` | Input fields, dialog backgrounds |
| `surface-highest` | `#2E2E3A` | Hover/focus states, tooltips |
| `on-surface-primary` | `#EAEAF0` | Primary text (headings, values) |
| `on-surface-secondary` | `#A0A0B0` | Secondary text (labels, hints) |
| `on-surface-tertiary` | `#6A6A7A` | Disabled text, placeholders |
| `accent-primary` | `#4D8DF7` | Primary actions, active states, links |
| `accent-primary-variant` | `#3A6FCC` | Pressed state for primary accent |
| `accent-primary-container` | `#1A2A4D` | Chip backgrounds, selection highlights |
| `accent-secondary` | `#7B61FF` | Superset indicators, secondary highlights |
| `status-success` | `#34C759` | Completed sets, PRs, positive deltas |
| `status-success-container` | `#0D3318` | PR badge background |
| `status-warning` | `#FFB830` | Rest timer active, caution states |
| `status-warning-container` | `#3D2E00` | Warning badge background |
| `status-error` | `#FF4A4A` | Destructive actions, negative deltas, errors |
| `status-error-container` | `#3D0A0A` | Error badge background |
| `border-subtle` | `#2A2A36` | Dividers, card borders |
| `border-focus` | `#4D8DF7` | Focus ring (matches accent-primary) |
| `warm-up-set` | `#FF9F43` | Warm-up set type indicator |
| `working-set` | `#4D8DF7` | Working set type indicator |
| `overlay-scrim` | `#000000B3` | Modal overlay (70% opacity black) |

#### 2.1.2 Light Theme (Secondary)

| Token | Hex | Usage |
|-------|-----|-------|
| `surface-lowest` | `#FFFFFF` | App background |
| `surface-low` | `#F5F5FA` | Card backgrounds |
| `surface-medium` | `#EDEDF4` | Elevated cards |
| `surface-high` | `#E4E4EE` | Input fields |
| `surface-highest` | `#DCDCE8` | Hover/focus states |
| `on-surface-primary` | `#121218` | Primary text |
| `on-surface-secondary` | `#52525E` | Secondary text |
| `on-surface-tertiary` | `#8A8A96` | Disabled text |
| `accent-primary` | `#2563EB` | Primary actions |
| `accent-primary-variant` | `#1D4ED8` | Pressed state |
| `accent-primary-container` | `#DBEAFE` | Chip backgrounds |
| `accent-secondary` | `#6D4AFF` | Superset indicators |
| `status-success` | `#16A34A` | Completed sets, PRs |
| `status-success-container` | `#DCFCE7` | PR badge background |
| `status-warning` | `#D97706` | Rest timer, caution |
| `status-warning-container` | `#FEF3C7` | Warning background |
| `status-error` | `#DC2626` | Destructive actions |
| `status-error-container` | `#FEE2E2` | Error background |
| `border-subtle` | `#D4D4DE` | Dividers |
| `border-focus` | `#2563EB` | Focus ring |
| `warm-up-set` | `#EA8C00` | Warm-up indicator |
| `working-set` | `#2563EB` | Working set indicator |
| `overlay-scrim` | `#00000066` | Modal overlay (40% opacity) |

#### 2.1.3 Muscle Group Colors

Each muscle group has a dedicated accent used in selectors, charts, and tags.

| Group | Dark Theme Hex | Light Theme Hex |
|-------|---------------|-----------------|
| Legs | `#FF6B6B` | `#DC2626` |
| Lower Back | `#FFA94D` | `#D97706` |
| Chest | `#4D8DF7` | `#2563EB` |
| Back | `#51CF66` | `#16A34A` |
| Shoulders | `#CC5DE8` | `#9333EA` |
| Arms | `#FFD43B` | `#CA8A04` |
| Core | `#22D3EE` | `#0891B2` |

### 2.2 Typography

Font family: **Inter** (variable weight). Fallback: `Roboto, system-ui, sans-serif`.

Inter is chosen over Roboto for its tabular (monospaced) numerals, which are critical for weight/rep columns alignment. Roboto's proportional figures cause column misalignment in set logging tables.

| Token | Size (sp) | Weight | Line Height | Letter Spacing | Usage |
|-------|-----------|--------|-------------|----------------|-------|
| `display-large` | 34sp | 700 (Bold) | 40sp | -0.5sp | Workout summary hero numbers |
| `display-medium` | 28sp | 700 (Bold) | 34sp | -0.25sp | Screen titles, rest timer countdown |
| `display-small` | 24sp | 600 (SemiBold) | 30sp | 0sp | Section headers |
| `headline-large` | 22sp | 600 (SemiBold) | 28sp | 0sp | Exercise names in active workout |
| `headline-medium` | 20sp | 600 (SemiBold) | 26sp | 0sp | Card titles |
| `headline-small` | 18sp | 600 (SemiBold) | 24sp | 0sp | Subsection titles |
| `body-large` | 16sp | 500 (Medium) | 24sp | 0.15sp | Primary body text, set values |
| `body-medium` | 14sp | 400 (Regular) | 20sp | 0.25sp | Secondary body text, labels |
| `body-small` | 12sp | 400 (Regular) | 16sp | 0.4sp | Captions, timestamps, metadata |
| `label-large` | 14sp | 600 (SemiBold) | 20sp | 0.1sp | Button text, tab labels |
| `label-medium` | 12sp | 600 (SemiBold) | 16sp | 0.5sp | Chip text, tags |
| `label-small` | 10sp | 500 (Medium) | 14sp | 0.5sp | Overline text, badge labels |
| `number-large` | 48sp | 700 (Bold) | 52sp | -1.0sp | Timer digits |
| `number-medium` | 24sp | 600 (SemiBold) | 28sp | 0sp | Weight/rep input fields |
| `number-small` | 16sp | 600 (SemiBold) | 20sp | 0sp | Set row values |

All number tokens use Inter's tabular numeral feature (`font-feature-settings: "tnum"`).

### 2.3 Spacing Scale

Based on a 4dp grid. All spacing values are multiples of 4dp.

| Token | Value (dp) | Usage |
|-------|-----------|-------|
| `space-0` | 0dp | No spacing |
| `space-1` | 4dp | Tight inline spacing (icon-to-text) |
| `space-2` | 8dp | Default inline spacing, list item internal padding |
| `space-3` | 12dp | Card internal padding (compact) |
| `space-4` | 16dp | Card internal padding (standard), screen horizontal margin |
| `space-5` | 20dp | Section spacing within a card |
| `space-6` | 24dp | Between cards/sections |
| `space-7` | 32dp | Major section dividers |
| `space-8` | 40dp | Screen top/bottom safe areas |
| `space-9` | 48dp | Large visual breaks |
| `space-10` | 64dp | Hero section spacing |

### 2.4 Border Radius

| Token | Value (dp) | Usage |
|-------|-----------|-------|
| `radius-none` | 0dp | Square edges (progress bar fills) |
| `radius-xs` | 4dp | Set row containers, inline tags |
| `radius-sm` | 8dp | Input fields, chips |
| `radius-md` | 12dp | Cards, buttons |
| `radius-lg` | 16dp | Bottom sheets, dialogs |
| `radius-xl` | 24dp | FABs, pill-shaped elements |
| `radius-full` | 9999dp | Circular elements (avatars, badges) |

### 2.5 Elevation

Using M3 tonal elevation (surface tint) in dark theme rather than shadow-based elevation, since shadows are invisible on dark surfaces.

| Token | Level | Surface Tint | Shadow (Light Theme) | Usage |
|-------|-------|-------------|---------------------|-------|
| `elevation-0` | Level 0 | None | None | Flat content on background |
| `elevation-1` | Level 1 | +4% primary | 1dp shadow | Cards at rest |
| `elevation-2` | Level 2 | +8% primary | 3dp shadow | Raised cards, FAB at rest |
| `elevation-3` | Level 3 | +11% primary | 6dp shadow | Bottom navigation, app bars |
| `elevation-4` | Level 4 | +14% primary | 8dp shadow | FAB pressed, dragged items |
| `elevation-5` | Level 5 | +16% primary | 12dp shadow | Dialogs, modals |

### 2.6 Touch Targets

| Token | Minimum Size | Usage |
|-------|-------------|-------|
| `target-minimum` | 48dp x 48dp | Absolute minimum per M3 spec |
| `target-standard` | 56dp x 56dp | Default for all interactive elements in workout screens |
| `target-large` | 64dp x 64dp | Set done checkbox, weight/rep steppers during active workout |
| `target-number-input` | 72dp x 56dp | Weight and rep input fields (wider for digit entry) |

All touch targets include at minimum 8dp padding between adjacent targets to prevent mis-taps. During active workout screens, this minimum increases to 12dp.

### 2.7 Motion Tokens

| Token | Duration | Easing | Usage |
|-------|----------|--------|-------|
| `motion-instant` | 100ms | `EmphasizedDecelerate` | Checkbox toggle, button press feedback |
| `motion-fast` | 200ms | `EmphasizedDecelerate` | Chip selection, small state changes |
| `motion-standard` | 300ms | `Emphasized` | Card expand/collapse, screen transitions |
| `motion-slow` | 450ms | `Emphasized` | Bottom sheet open/close, page transitions |
| `motion-deliberate` | 600ms | `EmphasizedAccelerate` | Rest timer completion animation |

---

## 3. Component Library

### 3.1 SetRow

The atomic unit of workout logging. This is the most frequently interacted-with component in the entire app.

**Layout:**

```
[Set #] [Type Indicator] [Weight Input] [x] [Reps Input] [Done Checkbox]
```

**Dimensions:**
- Total height: 64dp (includes 4dp vertical padding top + bottom)
- Set number column: 32dp wide, centered
- Type indicator: 8dp wide color bar on the left edge (warm-up = `#FF9F43`, working = `#4D8DF7`)
- Weight input: 80dp wide, 48dp tall, right-aligned text
- "x" separator: 16dp wide, centered, `on-surface-tertiary` color
- Reps input: 64dp wide, 48dp tall, right-aligned text
- Done checkbox: 56dp x 56dp touch target (24dp icon within)
- Horizontal padding between elements: 8dp

**States:**
| State | Appearance |
|-------|------------|
| Planned (not started) | `surface-high` background, `on-surface-secondary` text, empty checkbox outline |
| In progress (focused) | `accent-primary-container` background, `on-surface-primary` text, `border-focus` ring on active input |
| Completed | `surface-low` background, `status-success` filled checkbox, text at 85% opacity |
| Skipped | `surface-low` background, strikethrough text, `on-surface-tertiary` color |
| PR achieved | `status-success-container` background, gold star icon (`#FFD43B`) next to set number |

**Interaction:** Tapping the weight or reps field opens the number input (see 5.4). Tapping the done checkbox marks the set complete and auto-starts the rest timer. Long-press on a completed set opens an edit menu (Edit, Delete, Add Set Below).

### 3.2 ExerciseCard

Container for all sets of a single exercise during active workout.

**Layout:**

```
+------------------------------------------+
| [Collapse Toggle]  Exercise Name    [...] |
| Equipment Tag  Difficulty |  Planned: 4x8 @ 80kg |
| Warm-ups: 3 recommended  |                       |
|------------------------------------------|
| SetRow 1                                  |
| SetRow 2                                  |
| SetRow 3                                  |
| SetRow 4                                  |
|------------------------------------------|
| [+ Add Set]              [Notes icon]     |
+------------------------------------------+
```

**Dimensions:**
- Width: Full screen width minus 16dp horizontal margin (8dp each side)
- Corner radius: `radius-md` (12dp)
- Internal padding: 16dp
- Header height: 56dp
- Gap between header and set rows: 8dp
- Gap between set rows: 4dp
- Footer height: 48dp
- Margin between ExerciseCards: 12dp

**States:**
| State | Appearance |
|-------|------------|
| Upcoming | `surface-low` background, full opacity, collapsed (only header visible) |
| Active (current exercise) | `surface-medium` background, expanded showing all sets, `accent-primary` left border (3dp) |
| Completed | `surface-low` background, 70% opacity, collapsed with summary line ("4/4 sets, 320kg volume") |
| Superset member **(Phase 2)** | `accent-secondary` left border (3dp), connected to sibling by a vertical bracket line |

**Header details:**
- Exercise name: `headline-large` (22sp, SemiBold)
- Equipment tag: `label-medium` chip with `surface-highest` background
- Difficulty tag: `label-small` chip, color-coded — Beginner: `status-success`, Intermediate: `status-warning`, Advanced: `status-error`
- Warm-up indicator: `label-small`, `on-surface-tertiary` — shows recommended warm-up count per exercise type (e.g., "3 warm-up sets" for heavy compounds, "1 warm-up set" for isolations, "0" for bodyweight isolations). Source: `exercise-science.md` Section 8.5. If any AI-planned warm-up sets are not completed when user starts working sets, show non-blocking confirmation: "You have incomplete warm-up sets. Continue anyway?"
- Overflow menu (...): 48dp touch target, contains Reorder, Skip Exercise, Add to Superset **(Phase 2)**, View Detail
- Planned summary: `body-medium`, `on-surface-secondary`

### 3.3 MuscleGroupSelector

Grid of 7 selectable muscle group cards for workout setup.

**Layout:** 2-column grid with the 7th item spanning full width at the bottom.

**Per-item dimensions:**
- Width: (screen width - 48dp) / 2 (16dp margin left + right + 16dp gap)
- Height: 100dp
- Corner radius: `radius-md` (12dp)
- Internal padding: 12dp

**Per-item content:**
- Muscle group icon: 40dp x 40dp, custom illustration (simplified anatomical outline)
- Group name: `label-large` (14sp, SemiBold), below icon, centered
- Exercise count badge: `label-small` (10sp), top-right corner, `on-surface-tertiary`

**States:**
| State | Appearance |
|-------|------------|
| Unselected | `surface-low` background, `on-surface-secondary` icon tint, `border-subtle` 1dp border |
| Selected | Muscle group color as background at 15% opacity, muscle group color as border (2dp), icon tinted to muscle group color, checkmark badge top-right |
| Pressed | Scale to 96%, 100ms transition |
| Disabled | Not applicable -- all groups are always available |

### 3.4 RestTimer

Persistent overlay during rest periods between sets.

**Layout:**

```
+------------------------------------------+
|          REST                             |
|         1:32                              |
|     [circular progress ring]              |
|                                           |
|   [Skip]              [+30s]             |
+------------------------------------------+
```

**Dimensions:**
- Container: Bottom sheet, 280dp tall, full width, `radius-lg` top corners
- Circular progress ring: 160dp diameter, 8dp stroke width
- Timer digits: `number-large` (48sp, Bold), centered within ring
- "REST" label: `label-large` (14sp, SemiBold), `on-surface-secondary`, 16dp above ring
- Skip button: 56dp height, `radius-xl` (24dp), secondary style (outlined)
- +30s button: 56dp height, `radius-xl` (24dp), secondary style (outlined)
- Buttons placed 24dp from bottom, 16dp horizontal margin

**States:**
| State | Appearance |
|-------|------------|
| Counting down | `status-warning` ring color, digits in `on-surface-primary` |
| Last 10 seconds | Ring color pulses between `status-warning` and `status-error`, vibration pattern: 200ms pulse every second |
| Time's up | `status-error` ring fill, "GO" replaces digits, device vibrates 500ms, notification if backgrounded |
| Extended (+30s) | Ring resets with added time, smooth animation |
| Skipped | Sheet dismisses with `motion-standard` slide down |

### 3.5 ProgressChart

Line/bar chart for displaying training metrics over time.

**Layout:**

```
+------------------------------------------+
| Metric Title              [Time Range v]  |
|                                           |
|   Y-axis |                       *        |
|   labels |              *    *            |
|          |         *                      |
|          |    *                            |
|          |*                                |
|          +-----------------------------   |
|             X-axis date labels            |
|                                           |
| Current: 95kg  |  Peak: 100kg  |  +5kg   |
+------------------------------------------+
```

**Dimensions:**
- Container: Full width card, `radius-md` (12dp), `surface-low` background
- Internal padding: 16dp
- Chart area height: 200dp (portrait), 160dp (landscape)
- Y-axis label width: 48dp
- X-axis label height: 24dp
- Data point touch target: 32dp diameter (visual dot: 8dp)
- Line stroke: 2dp, color = muscle group color or `accent-primary`
- Grid lines: 1dp, `border-subtle`, dashed
- Summary row height: 40dp

**Time range selector:** Dropdown or segmented control with options: 4W, 12W, 6M, All. Default: 12W.

**States:**
| State | Appearance |
|-------|------------|
| Loading | Skeleton shimmer (animated gradient on `surface-medium`) |
| Data present | Chart rendered with animated draw-in (`motion-slow`) |
| No data | Centered illustration + "Complete a workout to see progress" message |
| Point selected | Enlarged dot (12dp), tooltip showing exact value + date, vertical crosshair line |
| PR data point | Gold dot (`#FFD43B`), star icon above point |

### 3.6 WorkoutSummary

Post-workout summary card displayed after completing a session.

**Layout:**

```
+------------------------------------------+
|          WORKOUT COMPLETE                 |
|             01:12:34                      |
|                                           |
| +------+ +------+ +------+               |
| | 5    | | 24   | | 4,800|               |
| | exer.| | sets | | kg   |               |
| +------+ +------+ +------+               |
|                                           |
| --- Volume by Muscle Group ---            |
| Chest    ||||||||||||||||     2,400kg     |
| Shoulders||||||||||           1,200kg     |
| Arms     ||||||              1,200kg     |
|                                           |
| --- Personal Records ---                  |
| [star] Bench Press: 100kg x 8 (NEW PR!)  |
| [star] OHP: Est 1RM 72kg (NEW PR!)       |
|                                           |
| vs Last Session: +12% volume              |
|                                           |
| [Save as Template]    [Done]              |
+------------------------------------------+
```

**Dimensions:**
- Full screen, scrollable
- Hero duration: `display-large` (34sp), centered, `on-surface-primary`
- Stat cards: 3-column row, each card 100dp tall, `radius-md`, `surface-low` background
- Stat value: `display-small` (24sp, SemiBold)
- Stat label: `body-small` (12sp), `on-surface-secondary`
- Volume bars: 8dp tall, rounded ends, muscle group color
- PR section: `status-success-container` background, `radius-sm`
- Comparison delta: `status-success` for positive, `status-error` for negative
- Bottom buttons: 56dp height, full width, 8dp gap

### 3.7 TemplateCard

Card representing a saved workout template in the template manager or home screen.

**Layout:**

```
+------------------------------------------+
| Template Name                       [...] |
| Last used: 3 days ago                     |
|                                           |
| [Chest] [Shoulders] [Arms]     5 exerc.  |
| Bench, OHP, Lateral Raise, ...           |
+------------------------------------------+
```

**Dimensions:**
- Width: Full screen width minus 32dp margin
- Minimum height: 96dp
- Corner radius: `radius-md` (12dp)
- Internal padding: 16dp
- Background: `surface-low`
- Border: 1dp `border-subtle`

**Content:**
- Template name: `headline-medium` (20sp, SemiBold)
- Last used: `body-small` (12sp), `on-surface-tertiary`
- Muscle group chips: `label-medium` (12sp), each with its muscle group color at 15% opacity background and muscle group color text
- Exercise count: `body-medium` (14sp), `on-surface-secondary`, right-aligned
- Exercise preview: `body-small` (12sp), `on-surface-tertiary`, single line, ellipsized

**States:**
| State | Appearance |
|-------|------------|
| Default | As described above |
| Pressed | Scale to 98%, background shifts to `surface-medium` |
| Long-pressed | Elevated to `elevation-4`, reveals delete/edit/duplicate actions |

### 3.8 NavigationBar

Bottom navigation bar. Persistent across all main screens.

**Layout:**

```
+----------+----------+----------+----------+
|  [icon]  |  [icon]  |  [icon]  |  [icon]  |
|   Home   | Workout  | Progress | Profile  |
+----------+----------+----------+----------+
```

**Dimensions:**
- Total height: 80dp (includes 12dp bottom safe area for gesture navigation)
- Usable height: 68dp
- Background: `surface-low` with `elevation-3`
- Icon size: 24dp
- Label: `label-small` (10sp, Medium)
- Per-item touch target: (screen width / 4) x 68dp (minimum 48dp wide)
- Active indicator: pill-shaped, 64dp x 32dp, `accent-primary-container` background
- Active icon tint: `accent-primary`
- Inactive icon tint: `on-surface-secondary`
- Gap between icon and label: 4dp

**States:**
| State | Appearance |
|-------|------------|
| Active | Icon filled variant, `accent-primary` tint, label in `accent-primary`, indicator pill visible |
| Inactive | Icon outlined variant, `on-surface-secondary` tint, label in `on-surface-secondary` |
| Badge (e.g., active workout) | 8dp red dot on Workout tab icon, top-right |

**Behavior:** Hidden during active workout (full-screen immersive). Shown on all other screens. Animates out with `motion-fast` when entering workout, animates in with `motion-standard` when exiting.

---

## 4. Screen Specifications

### 4.1 Onboarding (4 screens)

#### Screen 0: Privacy & Consent

- Title: "Your Data, Your Choice" -- `display-small` (24sp), 32dp from top
- Body text: "Deep Reps stores all workout data locally on your device. We collect anonymous usage analytics to improve the app." -- `body-medium`, `on-surface-secondary`
- Privacy policy link: Underlined `body-medium` text, `accent-primary` color, opens in-app browser
- Consent toggles (M3 Switch components, each 56dp row):
  - "Analytics (crash reports & usage data)" -- Default: OFF. `body-large` label, `on-surface-primary`
  - "Performance monitoring" -- Default: OFF. `body-large` label
- Subtitle below toggles: "You can change these anytime in Settings" -- `body-small`, `on-surface-tertiary`
- [Continue] button: Bottom-pinned, 56dp, `accent-primary` fill, always enabled (consent is optional)
- **Implementation note:** Consent state persisted to `EncryptedSharedPreferences` (not Room) via `ConsentManager` in `:core:data`. Firebase Analytics collection remains disabled until the user toggles consent ON. See `architecture.md` Section 4.9.

#### Screen 1: Welcome

- Full-screen illustration: branded gym silhouette, centered
- App logo + name: `display-medium` (28sp), centered, 120dp from top
- Tagline: "AI-powered strength tracking" -- `body-large` (16sp), `on-surface-secondary`, 8dp below name
- [Get Started] button: Full width (minus 32dp margin), 56dp height, `accent-primary` fill, `radius-md`
- Background: `surface-lowest`

#### Screen 2: Experience Level

- Title: "What's your training experience?" -- `display-small` (24sp), 32dp from top
- Subtitle: "This helps us generate your first workout plan" -- `body-medium`, `on-surface-secondary`
- 3 selectable cards, vertical stack, 16dp gap:
  - Each card: full width (minus 32dp), 88dp height, `radius-md`
  - Level icon (left, 40dp), Level name (`headline-small`), Description (`body-small`, 2 lines max)
  - Card 1: "Total Beginner" / "0-6 months of gym experience"
  - Card 2: "Intermediate" / "6-18 months, comfortable with main lifts"
  - Card 3: "Advanced" / "18+ months, structured programming"
- Selection state: `accent-primary` border (2dp), `accent-primary-container` background
- [Continue] button: Bottom-pinned, 56dp, full width minus 32dp, disabled until selection made

#### Screen 3: Unit Preference

- Title: "Preferred weight unit" -- `display-small` (24sp)
- Two large toggle buttons, side by side:
  - Each: (screen width - 48dp) / 2 wide, 72dp tall, `radius-md`
  - "kg" and "lbs" -- `headline-large` (22sp) centered
  - Selected: `accent-primary` fill, white text. Unselected: `surface-high`, `on-surface-secondary`
- Optional profile fields below (collapsible section "Optional: improve AI accuracy"):
  - Age: number input, `body-large`
  - Body weight: number input with unit label
  - Height: number input with unit label
  - Gender: segmented control (Male / Female / Prefer not to say)
- [Start Training] button: Bottom-pinned, 56dp, `accent-primary`

### 4.2 Home Screen

**Purpose:** Launch point. Quick access to start workout or load template.

**Layout (top to bottom):**

1. **Top app bar (64dp):** "Deep Reps" left-aligned (`headline-medium`), profile avatar button (40dp circle) right-aligned
2. **Active workout banner (conditional, 72dp):** If a workout is in progress, shows "Workout in progress -- 34:12" with [Resume] button. `status-warning-container` background, full width.
3. **Quick start section (200dp):**
   - "Start Workout" heading: `display-small`
   - Primary CTA: "Select Muscle Groups" -- large card, 120dp tall, `accent-primary` background, muscle group icons in a row, `radius-md`
   - Or: "From Template" -- secondary card, 72dp tall, `surface-medium`, `radius-md`
4. **Recent templates (scrollable horizontal list):**
   - Section title: "Recent Templates" -- `headline-small`, with "See All" link right-aligned
   - TemplateCard components, 200dp wide, horizontal scroll with 12dp gaps, 16dp left padding
5. **Last workout summary (compact):**
   - Section title: "Last Workout" -- `headline-small`
   - Compact summary card: date, duration, exercise count, total volume, one-line PR callout if any
   - 96dp tall, full width, `surface-low`, `radius-md`

**Scroll behavior:** Entire content scrollable. Top app bar stays fixed.

### 4.3 Muscle Group Selection Screen

**Purpose:** User picks which muscle groups to train.

**Layout:**
- Top app bar: Back arrow (48dp touch target) + "Select Muscle Groups" title
- MuscleGroupSelector grid (see 3.3): 2 columns, 7 items, 16dp grid gap
- Grid top margin: 16dp below app bar
- Bottom action area: Fixed at bottom, 80dp tall, `surface-low` background with top border
  - Selected count: "3 groups selected" -- `body-large`, left-aligned
  - [Next] button: 56dp height, 120dp width, `accent-primary`, right-aligned, `radius-md`
  - Button disabled state: `surface-highest` background, `on-surface-tertiary` text, when 0 groups selected

### 4.4 Exercise Selection Screen

**Purpose:** User picks exercises from the selected muscle groups.

**Layout:**
- Top app bar: Back arrow + "Select Exercises" + selected count badge
- Tabbed interface: One tab per selected muscle group, scrollable tab row if >3 tabs
  - Tab height: 48dp
  - Active tab: `accent-primary` text + bottom indicator (3dp, `accent-primary`)
  - Inactive tab: `on-surface-secondary` text
- Exercise list (per tab):
  - Each item: 72dp height, full width
  - Checkbox (left, 48dp touch target) + Exercise name (`body-large`) + Equipment chip (`label-medium`) + Isolation chip (`label-medium`)
  - Tapping the row (not checkbox) opens Exercise Detail (4.5) as a bottom sheet
  - Divider: 1dp `border-subtle` between items
- Bottom action area: Same pattern as 4.3
  - "8 exercises selected" + [Generate Plan] button
  - [Generate Plan] disabled if 0 exercises selected

### 4.5 Exercise Detail Screen

**Presented as:** Bottom sheet (peek height 60%, full expansion to 92% of screen).

**Layout:**
- Drag handle: 32dp x 4dp, centered, `on-surface-tertiary`, 8dp from top
- Exercise name: `display-small` (24sp), 16dp horizontal padding
- Tags row: Row of chips, 8dp below name:
  - Equipment chip: `label-medium`, `surface-highest` background
  - Movement type chip: "Compound" or "Isolation", `label-medium`, `surface-highest` background
  - Difficulty chip: `label-medium`, color-coded — Beginner: `status-success` tint, Intermediate: `status-warning` tint, Advanced: `status-error` tint
- Anatomical diagram: 200dp tall, centered, custom 2D illustration
  - Primary muscles: filled with muscle group color at 80% opacity
  - Secondary muscles: filled at 30% opacity — driven by `secondaryMuscles` JSON column on `ExerciseEntity` (sub-muscle level, e.g., "Anterior delts, triceps"), not group-level data
  - Body outline: `on-surface-tertiary` stroke, 1.5dp
- Pros / Key Benefits section:
  - Section title: `headline-small` (18sp)
  - Bullet list: `body-medium` (14sp), `on-surface-primary`, 4dp between items
- Tips / Cues section:
  - Same layout as benefits
  - Icon: lightbulb, `status-warning` tint
- [Add to Workout] / [Remove from Workout] button: Full width, 56dp, bottom-pinned with 16dp padding

### 4.6 Workout Plan Review Screen

**Purpose:** Shows AI-generated plan before starting. User can edit and reorder.

**Layout:**
- Top app bar: Back arrow + "Review Plan" + [Regenerate] icon button (48dp)
- Status banner (conditional): "Generated by AI" chip or "Offline -- using last plan" chip, `radius-xl`, 8dp below app bar
- Exercise list (reorderable):
  - Each exercise block:
    - Exercise name: `headline-medium` (20sp)
    - Drag handle (left): 24dp icon, 48dp touch target
    - Warm-up sets: grouped, `warm-up-set` type indicator
    - Working sets: grouped, `working-set` type indicator
    - Each set: `body-large`, "60kg x 12" format
    - [Edit] icon per set (pencil, 48dp touch target)
  - 12dp gap between exercise blocks
  - Divider between exercises
- Bottom action area:
  - [Start Workout] button: Full width minus 32dp, 56dp, `accent-primary`, `radius-md`
  - 16dp bottom padding + safe area

### 4.7 Active Workout Screen (Critical)

This is the most important screen in the app. Every design decision here prioritizes speed, readability, and error tolerance.

**Layout:**

```
+------------------------------------------+
| [Pause]  Workout Timer: 34:12     [End]  |  <- Sticky header, 56dp
|------------------------------------------|
|                                           |
| ExerciseCard 1 (Active)                   |  <- Expanded
|   SetRow 1 (completed)                    |
|   SetRow 2 (completed)                    |
|   SetRow 3 (in progress)  <-- focused     |
|   SetRow 4 (planned)                      |
|   [+ Add Set]               [Notes]       |
|                                           |
| ExerciseCard 2 (Upcoming)                 |  <- Collapsed
| ExerciseCard 3 (Upcoming)                 |  <- Collapsed
|                                           |
|------ RestTimer (when active) ------------|
| [+ Add Exercise]                          |  <- FAB or bottom action
+------------------------------------------+
```

**Sticky header (56dp):**
- Background: `surface-lowest`
- [Pause] icon button: left-aligned, 48dp, `on-surface-secondary`
- Workout timer: center, `headline-medium` (20sp, SemiBold), `on-surface-primary`. Counts up from 00:00.
- [End Workout] text button: right-aligned, `status-error` text, 48dp touch target

**Exercise list:**
- Vertical scroll, the current (active) exercise is auto-scrolled to top
- Auto-advance: when all sets of current exercise are marked done, the next exercise auto-expands and scrolls into view
- ExerciseCard components as defined in 3.2
- **(Phase 2)** Superset groups are visually bracketed with `accent-secondary` left bar spanning all grouped exercises

**Floating action:**
- **(Phase 2)** [+ Add Exercise] FAB: 56dp diameter, `accent-primary` fill, positioned 16dp from right edge, 16dp above bottom nav (or above rest timer when active)
- When rest timer is active, FAB repositions to 16dp above the rest timer sheet

**Critical UX details:**
- Number inputs (weight, reps) use a custom stepper: +/- buttons (64dp each) flanking a direct-entry text field. Tapping the field opens a numpad-only keyboard.
- The active set row has a subtle pulsing border animation (`accent-primary` at 40-70% opacity, 2s cycle) to indicate where the user should log.
- Completing the last set of the last exercise shows a "Finish Workout?" confirmation dialog.
- The screen requests `KEEP_SCREEN_ON` flag to prevent display timeout during workouts.

### 4.8 Workout Summary Screen

Post-workout. Detailed in component 3.6 (WorkoutSummary).

**Additional screen-level specs:**
- Background: `surface-lowest`
- Confetti animation on PR achievements: 1.5s burst, colored particles matching muscle group colors
- Scroll: Full content scrollable
- Share is out of scope (no social features)
- Navigation: [Done] navigates to Home. Saving as template navigates to template naming dialog, then Home.
- Back button behavior: Same as [Done] -- navigates to Home. No "are you sure?" because the workout is already saved.

### 4.9 Progress Dashboard Screen

**Purpose:** Central hub for training metrics and charts.

**MVP scope:** Weight-per-exercise chart (ProgressChart component) + session history list. The full advanced dashboard below is the target; items marked **(Phase 2)** are deferred.

**Layout:**
- Top app bar: "Progress" title, [Filter] icon button for time range
- Time range selector: Segmented control (4W | 12W | 6M | All), 40dp tall, full width minus 32dp, `radius-sm` segments
- Overview cards row (horizontal scroll):
  - "Weekly Volume" card: total working sets this week, trend arrow
  - "Training Streak" card: consecutive weeks with 3+ sessions
  - "PRs This Month" card: count of new PRs **(Phase 2 — PR detection is Phase 2)**
  - Each card: 120dp wide, 80dp tall, `surface-low`, `radius-md`
- Muscle group volume chart **(Phase 2)**: Stacked bar chart, one bar per week, colored by muscle group
  - Chart height: 200dp
  - Legend: horizontal row of muscle group color swatches + names below chart
- Strength progression section:
  - Title: "Strength Trends"
  - Exercise selector: Horizontal scrolling chips (exercise names), `radius-xl`
  - ProgressChart component (3.5) for selected exercise
  - Default metric: Top Set weight **(MVP)** / Estimated 1RM **(Phase 2)**
  - Toggle: Top Set | Volume **(MVP)** | Est. 1RM **(Phase 2)**
- Training consistency heatmap **(Phase 2)**:
  - GitHub-style contribution grid, 52 weeks (or selected range)
  - Cell size: 12dp x 12dp, 2dp gap
  - Color intensity: 0 sessions = `surface-medium`, 1 = 25% `accent-primary`, 2 = 50%, 3 = 75%, 4+ = 100%

### 4.10 Exercise History Screen

**Purpose:** Full history for a single exercise.

**Accessed from:** Exercise Detail (4.5) or Progress Dashboard drill-down.

**Layout:**
- Top app bar: Exercise name + back arrow
- Summary stats row:
  - Est. 1RM: `display-small`
  - All-time best set: `body-large`
  - Total sessions: `body-large`
  - Arranged in a 3-column grid, 80dp tall per cell
- ProgressChart component: Est. 1RM over time, 200dp tall
- Session history list (reverse chronological):
  - Each session entry:
    - Date: `body-medium`, `on-surface-secondary`
    - Sets performed: compact display "4x8 @ 80kg" or expanded per-set view on tap
    - Notes (if any): `body-small`, italic, `on-surface-tertiary`
    - PR badge if applicable
  - Entry height: 64dp collapsed, expandable
  - Dividers: 1dp `border-subtle`
  - Infinite scroll / pagination (20 sessions per page)

### 4.11 Template Manager Screen

**Purpose:** View, edit, delete, and organize saved templates.

**Layout:**
- Top app bar: "Templates" + [+ New Template] icon button
- Template list: Vertical list of TemplateCard components (3.7)
  - 12dp gap between cards
  - Swipe-right to reveal delete action (red background, trash icon)
  - Long press to enter multi-select mode
- Empty state:
  - Centered illustration
  - "No templates yet" -- `headline-small`
  - "Save a workout as a template to reuse it" -- `body-medium`, `on-surface-secondary`
  - [Create Template] button: outlined, 56dp, `accent-primary` border

### 4.12 Profile / Settings Screen

**Layout:**
- Top app bar: "Profile & Settings"
- Profile section:
  - Experience level: Segmented control (Beginner | Intermediate | Advanced)
  - Weight unit: Toggle (kg | lbs)
  - Optional fields in expandable section: Age, Body weight, Height, Gender
  - Body weight history: Small inline chart (80dp tall) if multiple entries exist **(Phase 2)**
- Settings sections (grouped in M3 list style):
  - **Rest Timer Defaults:**
    - Warm-up rest: Slider, 30s-180s, default 60s, 15s steps
    - Working set rest: Slider, 30s-300s, default varies by experience level (Beginner: 90s, Intermediate: 120s, Advanced: 150s), 15s steps
    - **Note:** The settings screen default adapts to the user's experience level per `exercise-science.md` Section 4. During active workouts, the rest timer priority chain is: (1) AI plan's per-exercise `rest_seconds`, (2) user per-exercise override, (3) this global default, (4) CSCS baseline by experience level + exercise type.
  - **Notifications:**
    - Rest timer notification: Toggle (default on)
    - Rest timer vibration: Toggle (default on)
  - **Data:**
    - Export workout data (CSV)
    - Clear all data (destructive, double confirmation)
  - **About:**
    - App version
    - Licenses
    - Privacy Policy link
- Each settings group: `headline-small` title, `surface-low` card background, `radius-md`
- Each setting item: 56dp min height, label left (`body-large`), control right

### 4.13 Superset Creation Screen **(Phase 2)**

**NOTE:** Supersets are deferred to Phase 2 per product-strategy.md Section 5.2. This screen is included for design continuity but will not be built in MVP.

**Presented as:** Dialog/bottom sheet overlay on Active Workout screen.

**Trigger:** Long-press an exercise, then select "Create Superset", or drag one exercise onto another.

**Layout:**
- Title: "Create Superset" -- `headline-medium`
- Available exercises list (from current workout, not yet in a superset):
  - Checkbox + exercise name per row
  - 56dp row height
  - Pre-selected: the exercise that triggered the action
- Reorder handles for selected exercises (defines execution order within superset)
- Preview: "Superset: Bench Press + Dumbbell Fly" -- `body-medium`
- [Cancel] and [Create] buttons: 56dp, side by side, 8dp gap
  - [Create] disabled until 2+ exercises selected

**After creation:**
- Exercises are visually grouped in the Active Workout screen
- A `accent-secondary` vertical bar connects them
- Rest timer only starts after the last exercise in the superset group is completed
- "Superset" label badge appears above the grouped exercises: `label-small`, `accent-secondary` text

---

## 5. Interaction Specifications

### 5.1 Drag-and-Drop Reordering

**Used in:** Workout Plan Review (4.6), Active Workout (4.7), Superset Creation (4.13, Phase 2), Template editing.

**Initiation:** Long-press on drag handle icon (300ms hold duration). Haptic feedback (light vibrate, 50ms) on activation.

**During drag:**
- Dragged item elevates to `elevation-4` with scale to 102%
- Background opacity: 95% (slight transparency)
- Shadow: 8dp in light theme, tonal elevation in dark theme
- Other items animate to make space with `motion-fast` (200ms) transitions
- Drop zone indicators: 2dp `accent-primary` horizontal line appears between potential drop positions
- Scroll: If dragged within 80dp of screen top/bottom edge, list auto-scrolls at 4dp/frame

**Drop:**
- Item settles to new position with `motion-standard` (300ms)
- Haptic feedback: medium vibrate (100ms)
- If dropped outside valid zone, item returns to original position with `motion-slow` (450ms)

### 5.2 Swipe Gestures

**Swipe-to-delete (Template cards, sets):**
- Direction: Right-to-left swipe
- Threshold: 40% of item width to trigger
- Background reveal: `status-error` background with white trash icon, right-aligned
- If threshold reached and released: item slides out fully, undo snackbar appears for 5 seconds at bottom
- If below threshold: item snaps back with `motion-fast`
- Velocity-based: Fast swipe (>800dp/s) triggers regardless of distance

**Swipe-to-complete (SetRow):**
- Direction: Left-to-right swipe
- Threshold: 50% of row width
- Background reveal: `status-success` background with white checkmark icon, left-aligned
- Equivalent to tapping the Done checkbox

### 5.3 Long-Press Actions

| Element | Long-Press Duration | Action |
|---------|-------------------|--------|
| ExerciseCard header | 300ms | Context menu: Reorder, Skip, Superset, View Detail |
| SetRow (completed) | 300ms | Context menu: Edit, Delete, Insert Set Above/Below |
| TemplateCard | 300ms | Enter multi-select mode (delete/duplicate multiple) |
| MuscleGroupSelector item | 500ms | Preview: tooltip showing exercise count and list |
| Any number field | 300ms | Select all text for replacement |

Haptic feedback: Light vibrate (50ms) on all long-press activations.

### 5.4 Number Input

**Custom number input for weight and rep fields during active workout.**

**Mode 1: Stepper (default)**
```
[ - ]  80  [ + ]
```
- [-] and [+] buttons: 64dp x 56dp, `surface-high` background, `radius-sm`
- Value display: `number-medium` (24sp), centered between buttons
- Weight step: 2.5kg / 5lbs (configurable)
- Rep step: 1
- Long-press on +/-: Begins rapid increment after 500ms hold, accelerating from 1 step/300ms to 1 step/100ms

**Mode 2: Direct entry (tap on value)**
- Opens number keyboard (Android `inputType="numberDecimal"`)
- Field pre-selects all text for easy replacement
- Confirm: Tap outside field, press Enter, or tap Done on keyboard
- Cancel: Press Back -- reverts to previous value

**Mode 3: Quick-fill from history**
- If the same exercise was performed in a recent session, a row of "quick fill" chips appears above the input:
  - "Last: 80kg" -- `label-medium` chip, `surface-highest` background
  - Tapping a chip fills the value instantly

### 5.5 Animations

| Animation | Trigger | Duration | Specification |
|-----------|---------|----------|---------------|
| Set completion | Tap done checkbox | 200ms | Checkbox fills with `status-success`, row background fades to completed state. Checkmark icon scales from 0 to 1 with overshoot (1.1x peak at 150ms). |
| Exercise transition | All sets done | 400ms | Completed exercise collapses (height animation), next exercise expands and scrolls up. Cross-fade content. |
| Rest timer appear | Set completed | 300ms | Bottom sheet slides up from bottom with `motion-standard` easing. |
| PR celebration | PR detected on set complete | 1500ms | Gold star icon scales in with bounce. PR badge slides in from right. If workout-level PR, subtle confetti burst (20 particles, gravity fall). |
| Workout complete | Tap End Workout (confirmed) | 600ms | Screen cross-fades to summary. Duration counter does a final "tick" animation. |
| Page transition | Navigate between screens | 300ms | M3 shared axis transition (forward: fade + slide right 30dp, back: fade + slide left 30dp). |
| Card press | Touch down on card | 100ms | Scale to 98%, elevation drops by 1 level. On release: scale back to 100% in 150ms. |
| Skeleton loading | Data loading | Continuous | Gradient shimmer from left to right, 1.5s cycle, on `surface-medium` blocks matching content layout shape. |
| Number change | Weight/rep value change | 150ms | Old value slides up and fades out, new value slides up from below and fades in. |

---

## 6. Navigation Architecture

### 6.1 Bottom Tab Structure

| Tab | Icon (Outlined) | Icon (Filled) | Destination |
|-----|-----------------|---------------|-------------|
| Home | `home_outlined` | `home_filled` | Home Screen (4.2) |
| Workout | `fitness_center_outlined` | `fitness_center_filled` | Active Workout (4.7) or Workout Setup entry |
| Progress | `insights_outlined` | `insights_filled` | Progress Dashboard (4.9) |
| Profile | `person_outlined` | `person_filled` | Profile/Settings (4.12) |

### 6.2 Screen Flow Diagram

```
Onboarding (first launch only)
  |
  v
Home -----> Muscle Group Selection --> Exercise Selection --> Workout Plan Review --> Active Workout --> Workout Summary --> Home
  |                                        |                                                              |
  |                                        +--- Exercise Detail (sheet)                                   +--- Save Template
  |
  +-------> Load Template -----------> Workout Plan Review --> Active Workout --> ...
  |
  +-------> Resume Active Workout ---> Active Workout --> ...

Progress --> Exercise History --> Exercise Detail (sheet)
  |
  +-------> Drill-down charts (in-screen transitions)

Profile --> Settings subsections (in-screen)
  |
  +-------> Export data
  +-------> Template Manager --> Template Edit

Active Workout --> Add Exercise (sheet) --> Exercise Selection (sheet)
  |
  +-------> Superset Creation (dialog)
  +-------> Exercise Detail (sheet, read-only during workout)
```

### 6.3 Back Stack Behavior

| From | Back Action | Result |
|------|------------|--------|
| Home | System back | Exit app (with confirmation if workout active) |
| Muscle Group Selection | Back | Home |
| Exercise Selection | Back | Muscle Group Selection (selections preserved) |
| Workout Plan Review | Back | Exercise Selection (selections preserved) |
| Active Workout | Back | "Discard workout?" confirmation dialog. Confirm = Home, Cancel = stay |
| Active Workout (paused) | Back | Same as active -- confirmation required |
| Workout Summary | Back | Home (cannot go back to active workout -- it's finished) |
| Progress Dashboard | Back | Home |
| Exercise History | Back | Progress Dashboard |
| Exercise Detail (sheet) | Back or swipe down | Dismiss sheet, return to underlying screen |
| Profile/Settings | Back | Home |
| Template Manager | Back | Profile/Settings or Home (depends on entry point) |
| Superset Creation (dialog) | Back | Dismiss dialog, return to Active Workout |

### 6.4 Deep Links

| URI Pattern | Destination | Use Case |
|-------------|------------|----------|
| `deepreps://workout/active` | Active Workout | Resume from notification |
| `deepreps://workout/summary/{id}` | Workout Summary | Post-workout notification |
| `deepreps://progress/{exerciseId}` | Exercise History | Progress notification |
| `deepreps://template/{templateId}/start` | Plan Review (loaded template) | Quick start shortcut |
| `deepreps://home` | Home | Default entry point |

Deep links that target Active Workout when no workout is active redirect to Home.

---

## 7. Accessibility

### 7.1 WCAG AA Compliance

Deep Reps targets WCAG 2.1 Level AA conformance. The following requirements are non-negotiable.

### 7.2 Color Contrast

| Element Type | Minimum Contrast Ratio | Verification |
|-------------|----------------------|--------------|
| Normal text (< 18sp) on background | 4.5:1 | All `on-surface-*` tokens against corresponding `surface-*` tokens |
| Large text (>= 18sp bold or >= 24sp) on background | 3:1 | Headings, display text |
| UI components (icons, borders, controls) | 3:1 | Against adjacent surfaces |
| Graphical objects (chart lines, data points) | 3:1 | Against chart background |

**Verified contrast ratios (dark theme critical pairs):**

| Foreground | Background | Ratio | Pass? |
|-----------|-----------|-------|-------|
| `#EAEAF0` on-surface-primary | `#0A0A0F` surface-lowest | 17.2:1 | Yes |
| `#EAEAF0` on-surface-primary | `#1A1A22` surface-medium | 13.1:1 | Yes |
| `#A0A0B0` on-surface-secondary | `#0A0A0F` surface-lowest | 8.4:1 | Yes |
| `#A0A0B0` on-surface-secondary | `#1A1A22` surface-medium | 6.4:1 | Yes |
| `#6A6A7A` on-surface-tertiary | `#0A0A0F` surface-lowest | 3.8:1 | Yes (large text only) |
| `#4D8DF7` accent-primary | `#0A0A0F` surface-lowest | 5.9:1 | Yes |
| `#4D8DF7` accent-primary | `#1A1A22` surface-medium | 4.5:1 | Yes |
| `#34C759` status-success | `#0A0A0F` surface-lowest | 8.2:1 | Yes |
| `#FF4A4A` status-error | `#0A0A0F` surface-lowest | 5.1:1 | Yes |
| `#FFB830` status-warning | `#0A0A0F` surface-lowest | 9.5:1 | Yes |

**Color must never be the sole indicator of state.** All states use a combination of color + icon + text label. For example, a completed set uses green color AND a filled checkmark AND the text "Done."

### 7.3 Screen Reader Support

**Content descriptions (TalkBack):**

| Component | Announcement |
|-----------|-------------|
| SetRow (planned) | "Set {n}, warm-up/working, {weight} kilograms, {reps} reps, not completed" |
| SetRow (completed) | "Set {n}, warm-up/working, {weight} kilograms, {reps} reps, completed" |
| SetRow (PR) | "Set {n}, {weight} kilograms, {reps} reps, completed, personal record" |
| ExerciseCard | "Exercise: {name}, {completed}/{total} sets completed" |
| MuscleGroupSelector item | "{group name}, {n} exercises, {selected/not selected}" |
| RestTimer | "Rest timer, {minutes} minutes {seconds} seconds remaining" |
| ProgressChart | "Progress chart for {exercise/group}, showing {metric} over {time range}. Current value: {value}. Peak: {value}" |
| NavigationBar item | "{tab name} tab, {selected/not selected}" |
| Done checkbox | "Mark set {n} as complete, double tap to activate" |
| Number stepper | "{field name}, current value {n}, double tap to edit, swipe up to increase, swipe down to decrease" |

**Focus order:** Top-to-bottom, left-to-right within each screen. During Active Workout, focus starts on the first incomplete set of the active exercise.

**Custom actions (TalkBack):**
- SetRow: "Mark complete", "Edit set", "Delete set" as accessible actions
- ExerciseCard: "Skip exercise", "Add set", "Open notes" as accessible actions
- Drag-and-drop: Accessible via "Move up" / "Move down" custom actions (replaces visual drag)

### 7.4 Font Scaling

The app supports Android system font scaling from 85% to 200%.

| Scale | Behavior |
|-------|----------|
| 85%-100% | Standard layout, no modifications |
| 100%-130% | Text scales, layouts adjust padding. SetRow may wrap weight/reps to a second line. |
| 130%-160% | SetRow switches to stacked layout (weight above reps). Chart labels truncate with ellipsis. |
| 160%-200% | Navigation bar labels hidden (icons only). Card content becomes single-column. Horizontal scrolling lists become vertical. |

**Non-scaling elements:**
- Icons (fixed dp sizes)
- Touch targets (never shrink below 48dp)
- Spacing tokens (fixed dp)
- Chart data points and lines

**Testing requirement:** All screens must be visually tested at 100%, 150%, and 200% font scale.

### 7.5 Motion and Reduced Motion

When the Android system setting "Remove animations" is enabled:

- All animations complete instantly (0ms duration)
- Confetti particle effects are replaced with a static PR badge
- Skeleton loading shimmer is replaced with a static placeholder
- Page transitions become instant cuts (no slide/fade)
- Rest timer countdown remains numerical only (no ring animation)

### 7.6 Touch Accommodation

- All interactive elements meet the 48dp minimum touch target (56dp standard in workout screens)
- Adjacent touch targets have minimum 8dp separation (12dp in workout screens)
- No gesture-only interactions -- every swipe/drag action has a tap-accessible alternative:
  - Swipe-to-delete: also available via long-press context menu
  - Swipe-to-complete: also available via done checkbox tap
  - Drag-to-reorder: also available via "Move Up" / "Move Down" buttons in context menu

---

## 8. Responsive Layout

### 8.1 Portrait (Primary, < 600dp width)

This is the primary and most-tested orientation. All specifications in sections 3-4 assume portrait on a standard phone (360dp-412dp width range).

**Grid system:**
- 16dp horizontal margins (8dp each side for workout cards)
- Maximum content width: screen width - 32dp
- Column system: Single column for workout flow, 2-column grid for Muscle Group Selector

### 8.2 Landscape (Phone, < 600dp height available)

Landscape on a phone is a constrained environment. Workout logging in landscape is awkward; the design accommodates it but does not optimize for it.

**Modifications:**
- NavigationBar moves to a NavigationRail on the left edge: 72dp wide, icons vertically stacked
- Active Workout screen: ExerciseCard uses a 2-column layout
  - Left column (55% width): Exercise name, set list, add set button
  - Right column (45% width): Rest timer (compact, inline), notes
- SetRow becomes more compact: 52dp height, smaller spacing between elements
- Rest timer: Inline display above the set list instead of bottom sheet (120dp tall, horizontal layout)
- Charts: Chart height reduces to 140dp, x-axis labels show every other label
- Workout Summary: Stat cards flow into 2 rows of 3 instead of scrolling

### 8.3 Tablet (600dp-840dp width)

**Modifications:**
- Maximum content width: 600dp, centered on screen
- Horizontal margins increase to 24dp (32dp on larger tablets)
- NavigationRail replaces NavigationBar: 80dp wide, left-aligned, with labels
- Muscle Group Selector: 3-column grid (instead of 2)
- Exercise Selection: 2-column list (side-by-side exercise items)
- Active Workout: Exercise list in left panel (60% width), currently active exercise detail + rest timer in right panel (40% width)
- Workout Plan Review: Side-by-side exercise list (left) and editable plan detail (right)
- Progress Dashboard: 2-column layout for chart + stats sections
- Exercise Detail: Opens as a side panel (400dp wide) instead of bottom sheet
- WorkoutSummary: Grid layout for stat cards (3 columns, no scrolling)

### 8.4 Large Tablet (> 840dp width)

**Modifications:**
- Maximum content width: 840dp, centered
- All tablet modifications from 8.3 apply
- NavigationRail expands to 120dp with labels always visible
- Active Workout: True split-screen -- exercise list (left, 400dp), active exercise with full set detail + rest timer (right, remaining width)
- Progress Dashboard: 3-column layout -- overview stats, charts, and history list visible simultaneously
- Muscle Group Selector: 4-column grid with larger cards (120dp height)

### 8.5 Foldable Devices

**Fold-aware behavior (tested on Samsung Galaxy Z Fold series):**

**Folded (outer screen, ~360dp width):**
- Standard phone portrait layout
- All components fit without modification
- NavigationBar at bottom

**Unfolded (inner screen, ~585dp width for Fold5):**
- Treated as small tablet (8.3 rules apply)
- NavigationRail on left
- Content centered within inner screen

**Tabletop posture (partially folded, top half visible):**
- Active Workout adapts: Exercise list on top half, set input + rest timer on bottom half (below the fold)
- Rest timer optimized for the bottom panel: larger digits, larger buttons
- This posture is ideal for propping the phone on a gym bench

**Flex mode hinge avoidance:**
- 24dp clear zone on either side of the hinge line
- No interactive elements placed directly on the fold axis
- Content reflows to avoid text splitting across the fold

### 8.6 Window Size Classes (M3)

| Width | Class | Layout Strategy |
|-------|-------|----------------|
| < 600dp | Compact | Single column, bottom nav, full-width cards |
| 600dp - 840dp | Medium | Navigation rail, wider margins, 2-column where applicable |
| > 840dp | Expanded | Navigation rail (wide), multi-pane layouts, max content width 840dp |

All breakpoints use M3 window size class definitions. Layouts adapt using Jetpack Compose `WindowSizeClass` API.

---

## Appendix A: Component Inventory

| Component | Section | Reused In Screens |
|-----------|---------|-------------------|
| SetRow | 3.1 | Active Workout, Plan Review, Exercise History |
| ExerciseCard | 3.2 | Active Workout |
| MuscleGroupSelector | 3.3 | Muscle Group Selection, Template Edit |
| RestTimer | 3.4 | Active Workout |
| ProgressChart | 3.5 | Progress Dashboard, Exercise History |
| WorkoutSummary | 3.6 | Workout Summary |
| TemplateCard | 3.7 | Home, Template Manager |
| NavigationBar | 3.8 | All screens (except Active Workout, Onboarding) |

## Appendix B: Icon Reference

| Icon | Material Symbol Name | Usage |
|------|---------------------|-------|
| Home | `home` | Navigation tab |
| Workout | `fitness_center` | Navigation tab |
| Progress | `insights` | Navigation tab |
| Profile | `person` | Navigation tab |
| Back | `arrow_back` | App bar navigation |
| Close | `close` | Sheet/dialog dismiss |
| More | `more_vert` | Overflow menus |
| Drag handle | `drag_indicator` | Reorder handles |
| Add | `add` | Add set, add exercise |
| Delete | `delete` | Remove items |
| Edit | `edit` | Edit set, edit template |
| Check | `check_circle` | Set completion |
| Timer | `timer` | Rest timer |
| Star | `star` | Personal records |
| Notes | `sticky_note_2` | Per-exercise notes |
| Skip | `skip_next` | Skip rest timer |
| Pause | `pause` | Pause workout |
| Play | `play_arrow` | Resume workout |
| Stop | `stop` | End workout |
| Refresh | `refresh` | Regenerate AI plan |
| Superset | `link` | Superset indicator |
| Calendar | `calendar_today` | Date references |
| Trophy | `emoji_events` | Milestone achievements |
| Info | `info` | Exercise detail trigger |

## Appendix C: Design-to-Development Handoff Checklist

For each screen/component before implementation:

- [ ] All states documented (default, active, completed, error, empty, loading, disabled)
- [ ] Touch target sizes verified (minimum 48dp, 56dp for workout screens)
- [ ] Dark and light theme tokens mapped
- [ ] Content descriptions written for TalkBack
- [ ] Font scaling tested at 100%, 150%, 200%
- [ ] Landscape layout specified
- [ ] Tablet layout specified (if different from phone)
- [ ] Animation specs documented (duration, easing, property)
- [ ] Edge cases documented (empty state, error state, offline state)
- [ ] Contrast ratios verified against WCAG AA
