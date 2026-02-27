# Implementation Plan: Data Export (CSV) & Import (JSON)

**Created:** 2026-02-26
**Status:** COMPLETED (2026-02-26)
**Scope:** CSV export for human-readable data + JSON import for data restoration across builds

---

## 1.1 Data Export Use Case (JSON + CSV)

**Problem:** No way to export workout data. User is about to start real-world testing and needs data portability across debug builds.

**Implementation:**

Create `ExportDataUseCase` in `:core:domain` (interface) with implementation in `:core:data`:

**What to export (all tables with user data):**
- `user_profile` (single row — settings, rep ranges, preferences)
- `workout_sessions` (all sessions regardless of status)
- `workout_exercises` (all exercises linked to sessions)
- `workout_sets` (all sets)
- `templates` (user-created templates)
- `template_exercises` (exercises in templates)
- `personal_records` (PRs)
- `body_weight_entries` (weight log)

**What NOT to export (static/ephemeral):**
- `exercises` (pre-populated, 78 entries — already in the app)
- `muscle_groups` (pre-populated, 7 entries)
- `exercise_muscles` (junction table, pre-populated)
- `cached_ai_plans` (ephemeral cache)

**JSON format (for import):**
```json
{
  "version": 1,
  "exportedAt": "2026-02-26T20:00:00Z",
  "dbVersion": 3,
  "userProfile": { ... },
  "workoutSessions": [ ... ],
  "workoutExercises": [ ... ],
  "workoutSets": [ ... ],
  "templates": [ ... ],
  "templateExercises": [ ... ],
  "personalRecords": [ ... ],
  "bodyWeightEntries": [ ... ]
}
```

**CSV format (for human readability):**
One CSV file per logical group:
- `workouts.csv` — sessions with exercises and sets flattened (one row per set)
- `templates.csv` — template list with exercise names
- `personal_records.csv` — PR log
- `body_weight.csv` — weight tracking log

All zipped into a single `.zip` file alongside the JSON.

**Export output:** A `.zip` file containing:
```
deep-reps-export-2026-02-26/
  backup.json          (full data, machine-readable, for import)
  workouts.csv         (human-readable workout log)
  templates.csv        (human-readable templates)
  personal_records.csv (human-readable PRs)
  body_weight.csv      (human-readable weight log)
```

**Files:**
- New: `core/domain/src/main/java/com/deepreps/core/domain/usecase/ExportDataUseCase.kt` (interface)
- New: `core/data/src/main/java/com/deepreps/core/data/export/DataExporter.kt` (implementation)
- New: `core/data/src/main/java/com/deepreps/core/data/export/CsvFormatter.kt` (CSV generation)

**Effort:** Medium (2-3 hr)

---

## 1.2 Data Import Use Case (JSON)

**Problem:** Need to restore exported data into a fresh install or new build.

**Implementation:**

Create `ImportDataUseCase` in `:core:domain` (interface) with implementation in `:core:data`:

1. Read `backup.json` from the zip (or standalone JSON file)
2. Validate `version` and `dbVersion` fields
3. Inside a Room transaction:
   a. Clear all user data tables (CASCADE will handle children)
   b. Insert user_profile
   c. Insert workout_sessions
   d. Insert workout_exercises (with correct session_id FKs)
   e. Insert workout_sets (with correct workout_exercise_id FKs)
   f. Insert templates
   g. Insert template_exercises
   h. Insert personal_records
   i. Insert body_weight_entries
4. Return success/failure with count of imported items

**ID handling:** Export stores original IDs. Import uses `@Insert(onConflict = REPLACE)` or clears + inserts with original IDs. Since we clear first, original IDs should work.

**Files:**
- New: `core/domain/src/main/java/com/deepreps/core/domain/usecase/ImportDataUseCase.kt` (interface)
- New: `core/data/src/main/java/com/deepreps/core/data/export/DataImporter.kt` (implementation)

**Effort:** Medium (2-3 hr)

---

## 1.3 Settings UI for Export/Import

**Problem:** Need UI entry points in Settings.

**Implementation:**

Add "Data" section to SettingsScreen with two rows:
- "Export Data" — triggers export, opens share sheet with the zip file
- "Import Data" — opens file picker for .json or .zip file, confirmation dialog, then imports

**Export flow:**
1. User taps "Export Data"
2. Show loading indicator
3. Generate zip in app's cache directory
4. Open Android share sheet (ACTION_SEND) with the zip file URI via FileProvider
5. Show success toast with item counts

**Import flow:**
1. User taps "Import Data"
2. Open file picker (ACTION_OPEN_DOCUMENT, mime: application/json or application/zip)
3. Show confirmation dialog: "This will replace all existing data. Continue?"
4. On confirm: show loading, run import, show result toast
5. Restart relevant ViewModels (or just show "Restart app for changes to take effect")

**Files:**
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsScreen.kt` (add Data section)
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsIntent.kt` (add ExportData, ImportData intents)
- `feature/profile/src/main/java/com/deepreps/feature/profile/SettingsViewModel.kt` (handle export/import)

**Effort:** Medium (1-2 hr)

---

## Execution Order

```
1.1 Export use case (2-3 hr) — can start immediately
1.2 Import use case (2-3 hr) — can run parallel with 1.1
1.3 Settings UI (1-2 hr) — blocked by 1.1 and 1.2
```

Total: ~5-6 hours

---

## Verification Checklist

- [ ] Export produces a valid zip with backup.json + CSV files
- [ ] backup.json contains all user data tables
- [ ] CSV files are human-readable with headers
- [ ] Import reads backup.json and restores all data
- [ ] Import clears existing data before inserting
- [ ] Import handles missing optional tables gracefully
- [ ] Export → fresh install → import → all data visible
- [ ] Share sheet opens with the zip file
- [ ] File picker accepts .json and .zip files
- [ ] Confirmation dialog before destructive import
- [ ] Build passes: assembleDebug, testDebugUnitTest, detekt, lintDebug
