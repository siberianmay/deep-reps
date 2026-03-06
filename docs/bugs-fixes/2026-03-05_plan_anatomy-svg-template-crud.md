# Implementation Plan: Anatomy SVG Replacement + Template CRUD

**Created:** 2026-03-05
**Status:** IN PROGRESS (anatomy SVG pipeline complete, awaiting better body SVG source)
**Scope:** Replace rectangle-man Canvas anatomy diagram with SVG-based multi-layer VectorDrawable; wire exercise picker into template create/edit flow

### Current State (2026-03-06)

**Anatomy SVG pipeline: COMPLETE.** The full toolchain works end-to-end:
- `scripts/analyze_anatomy_svg.py` — parses SVG, computes bounding boxes, classifies paths
- `scripts/generate_anatomy_drawables.py` — generates per-muscle-group VectorDrawable XML with pre-transformed coordinates
- `AnatomyDiagram.kt` composable — stacks VectorDrawable layers with per-group color tinting
- `AnatomyPathMapping.kt` — maps path IDs to muscle groups (used for reference, not rendering)

**Blocking issue:** The current SVG source (`resources/anatomy_template.svg`) has poor path structure — many paths span the entire body rather than individual muscles. Bounding-box filtering moved 16 oversized paths to the base group, but the remaining mapping is still approximate. **Need a cleaner SVG with well-isolated muscle group paths.** User will source a better body schema.

**Key technical lessons learned:**
- AAPT2 silently replaces pathData >32K chars with `"STRING_TOO_LARGE"` — pre-transform coordinates to shrink values
- SVG first `m` is absolute per spec — must convert to `M` in pre-transform
- After `m→M` conversion, subsequent implicit pairs must be `l` (relative lineto)
- Bounding-box filtering (>200w or >400h or >40K area) is essential for path classification

---

## Issue #4: Anatomy SVG Replacement

### 4.1 Pre-process SVG into Multi-Layer VectorDrawables

**Problem:** Current `AnatomyDiagram.kt` (582 lines) draws a crude rectangle-man using Canvas primitives. A proper SVG (`resources/anatomy_template.svg`, 998 lines) exists but has no semantic muscle group IDs — just ~200 paths labeled `path49`..`path250+`.

**Impact:** The rectangle-man is ugly and undermines app credibility. Users expect a real anatomy visualization.

**Implementation:**
1. Open `resources/anatomy_template.svg` in Inkscape
2. Visually identify paths belonging to each muscle group region:
   - CHEST: pectoralis major paths (upper, mid, lower)
   - SHOULDERS: anterior deltoid, lateral deltoid paths
   - ARMS: biceps, forearms (anterior) paths
   - CORE: rectus abdominis, external obliques paths
   - LEGS: quads, adductors, hip flexors paths
   - BACK: lateral lat silhouette paths (visible from front)
   - LOWER_BACK: (minimal on front view — skip for MVP)
   - BASE: head, neck, hands, feet, outline, non-muscle anatomy
3. Group paths into Inkscape layers by muscle group
4. Export each layer as separate SVG
5. Convert each SVG to Android VectorDrawable XML using Android Studio's SVG import tool
6. Place files in `core/ui/src/main/res/drawable/`:
   - `ic_anatomy_base.xml` — outline, head, hands, feet (always rendered)
   - `ic_anatomy_chest.xml` — chest paths only
   - `ic_anatomy_shoulders.xml` — shoulder paths only
   - `ic_anatomy_arms.xml` — arm paths only
   - `ic_anatomy_core.xml` — core paths only
   - `ic_anatomy_legs.xml` — leg paths only
   - `ic_anatomy_back.xml` — lat silhouette paths only

**Files:**
- `resources/anatomy_template.svg` (source)
- New: `core/ui/src/main/res/drawable/ic_anatomy_*.xml` (7-8 files)

**Effort:** Medium (SVG splitting is manual but one-time)
**Risk:** Medium — some SVG paths may span multiple body regions or have complex fills that don't convert cleanly to VectorDrawable. Mitigation: simplify paths in Inkscape before export.

---

### 4.2 Create New AnatomyDiagram Composable in core:ui

**Problem:** Current `AnatomyDiagram` lives in `feature:exercise-library` with a bad API (`exerciseId: Long, primaryGroupId: Long`) and Canvas-based rendering. Needs replacement with a composable that stacks VectorDrawable layers with per-group tinting.

**Impact:** Enables primary + secondary muscle highlighting on any screen.

**Implementation:**
1. Create `core/ui/src/main/java/.../component/AnatomyDiagram.kt`
2. New API:
   ```kotlin
   enum class HighlightLevel { PRIMARY, SECONDARY }

   @Composable
   fun AnatomyDiagram(
       muscleHighlights: Map<MuscleGroup, HighlightLevel>,
       modifier: Modifier = Modifier,
   )
   ```
3. Rendering approach: `Box` with stacked `Image` composables:
   - Base layer: always rendered with neutral gray (`#2E2E3A`) tint
   - One layer per muscle group: rendered only when highlighted
   - Primary: group accent color at 85% alpha via `ColorFilter.tint()`
   - Secondary: group accent color at 30% alpha via `ColorFilter.tint()`
4. Add `Modifier.graphicsLayer { }` on the Box for compositing
5. Height: 280dp, width: `fillMaxWidth()`, `ContentScale.Fit`
6. Minimal inline legend below diagram:
   - `[filled dot] Primary  [dim dot] Secondary`
   - Only shown when at least one secondary highlight exists
   - 8dp dots, `body-small` labels, left-aligned
7. Muscle group label below legend: centered, `label-medium`, primary group color

**Files:**
- New: `core/ui/src/main/java/com/deepreps/core/ui/component/AnatomyDiagram.kt`
- Dependencies: `MuscleGroup` from `core:domain`, theme colors from `core:ui`

**Effort:** Medium
**Risk:** Low — standard Compose Image stacking, no third-party deps

---

### 4.3 Update ExerciseDetailScreen to Use New AnatomyDiagram

**Problem:** `ExerciseDetailScreen` currently calls the old `AnatomyDiagram(exerciseId, primaryGroupId)`. Needs to pass primary + secondary muscle groups.

**Impact:** Exercise detail now shows both primary (bright) and secondary (dim) muscles.

**Implementation:**
1. Update `ExerciseDetailViewModel` to expose `muscleHighlights: Map<MuscleGroup, HighlightLevel>` in UI state
   - Query `exercise_muscles` junction table for the exercise
   - Map primary groups to `HighlightLevel.PRIMARY`, secondary to `HighlightLevel.SECONDARY`
2. Update `ExerciseDetailUiState` to include `muscleHighlights` field
3. Update `ExerciseDetailScreen` to pass `muscleHighlights` to new `AnatomyDiagram`
4. Remove import of old `AnatomyDiagram` from `feature:exercise-library:components`
5. Delete old `feature/exercise-library/.../components/AnatomyDiagram.kt` (582 lines)
6. Add `core:ui` already in deps via feature plugin — no new dependency needed

**Files:**
- `feature/exercise-library/src/main/java/.../ExerciseDetailScreen.kt`
- `feature/exercise-library/src/main/java/.../ExerciseDetailViewModel.kt`
- `feature/exercise-library/src/main/java/.../ExerciseDetailUiState.kt`
- Delete: `feature/exercise-library/src/main/java/.../components/AnatomyDiagram.kt`

**Effort:** Small
**Risk:** Low

---

### 4.4 Future: Back View (Phase 2)

**Note:** The current SVG is front-facing only. BACK and LOWER_BACK muscles are primarily posterior and won't highlight well on the front view. A back-facing SVG asset needs to be sourced and processed identically. The `AnatomyDiagram` API is designed to support a front/back toggle when the asset is available — just add a second set of VectorDrawables and a toggle parameter.

Not blocking MVP. BACK group will show lat silhouette on front view. LOWER_BACK will show minimal/no highlighting on front view — acceptable for now.

---

## Issue #2: Template CRUD — Wire Exercise Picker

### 2.1 Add "Add Exercise" Button to CreateTemplateScreen

**Problem:** `CreateTemplateScreen` has a name field, muscle group chips, and exercise list — but NO WAY to add exercises. The "Add Exercise" button is completely missing.

**Impact:** Template creation is non-functional without this.

**Implementation:**
1. Wrap `CreateTemplateContent` in a `Scaffold` with `bottomBar`
2. Bottom bar: 80dp height, `surface-low` background, `border-subtle` top border
3. Full-width `DeepRepsButton` (Primary variant) with text "Add Exercise" and `Icons.Filled.Add` leading icon
4. 16dp horizontal padding inside the bar
5. `LazyColumn` gets additional bottom `contentPadding` of 80dp to avoid overlap
6. Add `onAddExercises: () -> Unit` callback parameter to `CreateTemplateScreen`
7. Button click triggers `CreateTemplateIntent.NavigateToExerciseSelection`
8. ViewModel emits `CreateTemplateSideEffect.NavigateToExerciseSelection(existingExerciseIds: List<Long>)`
9. Screen composable calls `onAddExercises()` when side effect fires
10. Improve empty state: use `EmptyState` component with fitness icon, "No exercises yet" title, "Tap 'Add Exercise' to build your template" message

**Files:**
- `feature/templates/.../CreateTemplateScreen.kt`
- `feature/templates/.../CreateTemplateIntent.kt` — add `NavigateToExerciseSelection`
- `feature/templates/.../CreateTemplateSideEffect.kt` — add `NavigateToExerciseSelection(existingIds: List<Long>)`
- `feature/templates/.../CreateTemplateViewModel.kt` — handle intent, emit side effect

**Effort:** Small
**Risk:** Low

---

### 2.2 Wire Exercise Selection Result Back to CreateTemplate

**Problem:** After navigating to `ExerciseSelectionScreen`, selected exercise IDs need to get back to `CreateTemplateViewModel`.

**Impact:** Completes the add-exercise flow.

**Implementation:**
1. Use `SavedStateHandle` navigation result pattern (official Jetpack approach):
   - `ExerciseSelectionScreen` sets result on `previousBackStackEntry.savedStateHandle`
   - `CreateTemplateScreen` observes result from its own `savedStateHandle`
2. Add new route `exercise_selection_for_template` in `ExerciseLibraryNavigation.kt`:
   - Route: `exercise_selection_for_template?preSelected={preSelected}`
   - `preSelected` is a comma-separated string of exercise IDs already in template
   - On confirm: sets `LongArray` result on `previousBackStackEntry.savedStateHandle` and pops back
3. Add `AddExercises(exerciseIds: List<Long>)` intent to `CreateTemplateIntent`
   - Appends newly-selected exercises to existing list (preserves order of existing exercises)
   - Skips duplicates (exercises already in the template)
   - Looks up exercise names from `ExerciseRepository`
4. Wire in `DeepRepsNavHost.kt`:
   - `createTemplateScreen` gets `onAddExercises` callback → navigates to selection route
   - New `exerciseSelectionForResult` NavGraphBuilder extension
   - Result observation in the composable registration

**Decision note:** Using "append new, skip duplicates" rather than "replace entire list" to preserve user's exercise ordering within the template.

**Files:**
- `feature/exercise-library/.../navigation/ExerciseLibraryNavigation.kt` — new route
- `feature/templates/.../CreateTemplateIntent.kt` — add `AddExercises`
- `feature/templates/.../CreateTemplateViewModel.kt` — handle `AddExercises`
- `feature/templates/.../navigation/TemplateNavigation.kt` — add `onAddExercises` param
- `app/src/main/java/.../DeepRepsNavHost.kt` — wire routes and result passing

**Effort:** Medium
**Risk:** Medium — SavedStateHandle result pattern requires careful lifecycle handling (clear result after consuming). Mitigation: follow official Google docs pattern exactly.

---

### 2.3 Add Edit Action to Template List

**Problem:** `TemplateListScreen` has Rename and Delete in context menu, but no Edit (open template in editor). The `NavigateToEditTemplate` side effect exists but nothing triggers it.

**Impact:** Users can't modify exercise composition of existing templates.

**Implementation:**
1. Add `EditTemplate(templateId: Long)` to `TemplateListIntent`
2. Handle in `TemplateListViewModel`: emit `NavigateToEditTemplate(templateId)` side effect
3. Add "Edit" item to the dropdown menu in `TemplateItemWithContextMenu`:
   - Position: first item (above Rename, above Delete)
   - Icon: `Icons.Default.Edit`, text: "Edit"
4. Add three-dot `more_vert` IconButton to `TemplateCard` for discoverability:
   - 48dp touch target, 24dp icon, top-right of card
   - Tint: `on-surface-tertiary`
   - Triggers same dropdown as long-press
   - Requires adding `onMenuClick: () -> Unit` parameter to `TemplateCard`

**Files:**
- `feature/templates/.../TemplateListIntent.kt` — add `EditTemplate`
- `feature/templates/.../TemplateListViewModel.kt` — handle intent
- `feature/templates/.../TemplateListScreen.kt` — add Edit to dropdown
- `feature/templates/.../components/TemplateCard.kt` — add `more_vert` button

**Effort:** Small
**Risk:** Low

---

### 2.4 Pre-select Existing Exercises in Picker

**Problem:** When editing a template with existing exercises and opening the picker, those exercises should appear pre-checked.

**Impact:** Prevents confusion and duplicate additions.

**Implementation:**
1. Pass `preSelected` as a comma-separated ID string in the nav argument
2. `ExerciseSelectionViewModel` parses `preSelected` from `SavedStateHandle` and initializes `selectedExerciseIds` set
3. Pre-checked exercises show as checked in the list but are not re-added on confirm

**Files:**
- `feature/exercise-library/.../ExerciseSelectionViewModel.kt` — parse preSelected arg
- `feature/exercise-library/.../navigation/ExerciseLibraryNavigation.kt` — route arg

**Effort:** Small
**Risk:** Low

---

## Execution Order & Dependencies

```
4.1 (SVG splitting) ──> 4.2 (new composable) ──> 4.3 (wire to detail screen)
                                                        │
2.1 (Add Exercise button) ──> 2.2 (wire result) ──> 2.3 (edit action) ──> 2.4 (pre-select)
```

- Issue #4 and #2 are independent — can be worked in parallel
- Within #4: 4.1 blocks 4.2 blocks 4.3
- Within #2: 2.1 can start immediately; 2.2 depends on 2.1; 2.3 is independent; 2.4 depends on 2.2

## Risk Register

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| SVG paths don't map cleanly to muscle groups | High | Medium | Simplify paths in Inkscape; some groups may have fewer paths |
| VectorDrawable conversion loses detail | Medium | Low | Use Android Studio SVG import; manual cleanup if needed |
| SavedStateHandle result lost on process death | Medium | Low | Use `getLiveData` (survives process death); clear after consumption |
| SVG too complex for VectorDrawable format | High | Low | Simplify paths; remove gradients/effects; use flat fills only |
| Front-only view disappoints for BACK/LOWER_BACK exercises | Medium | High | Accepted for MVP; back view is Phase 2 |

## Verification Checklist

### Issue #4
- [ ] 7+ VectorDrawable XML files in `core/ui/src/main/res/drawable/`
- [ ] New `AnatomyDiagram` composable in `core:ui` accepts `Map<MuscleGroup, HighlightLevel>`
- [ ] ExerciseDetailScreen shows primary muscles highlighted at 85% alpha
- [ ] ExerciseDetailScreen shows secondary muscles highlighted at 30% alpha
- [ ] Unhighlighted body parts visible in neutral gray
- [ ] Legend shows "Primary / Secondary" when secondaries exist
- [ ] Old Canvas-based AnatomyDiagram.kt deleted
- [ ] Build passes (assembleDebug, detekt, lint, unit tests)

### Issue #2
- [ ] CreateTemplateScreen has "Add Exercise" sticky bottom button
- [ ] Tapping "Add Exercise" navigates to exercise selection picker
- [ ] Selected exercises appear in template after confirming picker
- [ ] Already-added exercises shown as pre-checked in picker
- [ ] Duplicate exercises are not added twice
- [ ] Template list context menu has "Edit" option
- [ ] Template cards have three-dot menu button for discoverability
- [ ] Edit action opens CreateTemplateScreen with template loaded
- [ ] Empty state shows helpful message with icon
- [ ] Build passes (assembleDebug, detekt, lint, unit tests)
