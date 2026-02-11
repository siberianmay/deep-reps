# AI Prompt Test Fixtures — Deep Reps

**Authority:** CSCS + Lead Android Developer
**Purpose:** Comprehensive test fixtures for AI workout plan generation (Epic 0.6.3)
**Status:** Ready for implementation

This document provides 10 diverse test fixtures representing the range of user profiles the Gemini AI will encounter. Each fixture includes input profiles, expected outputs matching the JSON schema, and validation notes explaining which safety rules and logic should activate.

---

## Fixture 1: Beginner Male — No Training History (Cold Start)

### Input Profile

```json
{
  "experience_level": 1,
  "gender": "male",
  "age": 25,
  "body_weight_kg": 75.0,
  "selected_muscle_groups": ["Chest"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 45,
  "training_history": []
}
```

### Selected Exercises

1. `chest_barbell_bench_press` (Barbell Bench Press)
2. `chest_incline_dumbbell_press` (Incline Dumbbell Press)
3. `chest_dumbbell_fly` (Dumbbell Fly)
4. `chest_machine_chest_press` (Machine Chest Press)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "chest_barbell_bench_press",
      "warmup_sets": [
        { "weight": 20, "reps": 12, "set_number": 1 },
        { "weight": 30, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 30, "reps": 10, "set_number": 1 },
        { "weight": 30, "reps": 10, "set_number": 2 },
        { "weight": 30, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 90,
      "notes": "Baseline: 0.40 x 75kg = 30kg. Focus on form: bar touches mid-chest, elbows 45 degrees."
    },
    {
      "exercise_id": "chest_incline_dumbbell_press",
      "warmup_sets": [
        { "weight": 5, "reps": 10, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 10, "reps": 12, "set_number": 1 },
        { "weight": 10, "reps": 12, "set_number": 2 },
        { "weight": 10, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.15 x 75kg = 11.25kg per hand, rounded down to 10kg. 30-45 degree bench angle."
    },
    {
      "exercise_id": "chest_dumbbell_fly",
      "warmup_sets": [
        { "weight": 2.5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 5, "reps": 12, "set_number": 1 },
        { "weight": 5, "reps": 12, "set_number": 2 },
        { "weight": 5, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Baseline: 0.08 x 75kg = 6kg per hand, rounded down to 5kg. Slight bend at elbow throughout."
    },
    {
      "exercise_id": "chest_machine_chest_press",
      "warmup_sets": [
        { "weight": 15, "reps": 10, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 25, "reps": 12, "set_number": 1 },
        { "weight": 25, "reps": 12, "set_number": 2 },
        { "weight": 25, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.35 x 75kg = 26.25kg, rounded down to 25kg. Adjust seat so handles align with mid-chest."
    }
  ],
  "session_summary": {
    "total_working_sets": 12,
    "estimated_duration_minutes": 42,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Baseline tables used:** Section 4.1 male beginner ratios (0.40 BW bench, 0.15 BW per hand incline DB, 0.08 BW per hand fly, 0.35 BW machine press)
- **Weight rounding:** All weights rounded DOWN to nearest 2.5kg (Section 8.7)
- **Warm-up sets:** 2 for barbell compound, 1 for dumbbell and machine (Section 8.5)
- **Rest periods:** 90s heavy compound, 75s moderate compound, 60s isolation (Section 8.8)
- **Volume check:** 12 working sets within beginner range (12-16 total sets, Section 8.2)
- **No history:** Cold start exception applies — no 10% jump constraint active (Section 8.1)

---

## Fixture 2: Intermediate Female — With Training History

### Input Profile

```json
{
  "experience_level": 2,
  "gender": "female",
  "age": 32,
  "body_weight_kg": 62.0,
  "selected_muscle_groups": ["Back", "Arms"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 60,
  "training_history": [
    {
      "exercise_id": "back_lat_pulldown",
      "sessions": [
        {
          "date": "2026-02-04",
          "sets": [
            { "weight": 35, "reps": 10, "set_type": "working" },
            { "weight": 35, "reps": 10, "set_type": "working" },
            { "weight": 35, "reps": 9, "set_type": "working" }
          ]
        }
      ]
    },
    {
      "exercise_id": "back_seated_cable_row",
      "sessions": [
        {
          "date": "2026-02-04",
          "sets": [
            { "weight": 30, "reps": 10, "set_type": "working" },
            { "weight": 30, "reps": 10, "set_type": "working" },
            { "weight": 30, "reps": 9, "set_type": "working" }
          ]
        }
      ]
    }
  ]
}
```

### Selected Exercises

1. `back_lat_pulldown` (Lat Pulldown)
2. `back_seated_cable_row` (Seated Cable Row)
3. `back_face_pull` (Face Pull)
4. `arms_barbell_curl` (Barbell Curl)
5. `arms_tricep_pushdown` (Tricep Pushdown)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "back_lat_pulldown",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 30, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 37.5, "reps": 10, "set_number": 1 },
        { "weight": 37.5, "reps": 10, "set_number": 2 },
        { "weight": 37.5, "reps": 10, "set_number": 3 },
        { "weight": 37.5, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 105,
      "notes": "Last session: 35kg x 10, 10, 9. Progressive overload +2.5kg. Aim to complete all 4 sets at 10 reps."
    },
    {
      "exercise_id": "back_seated_cable_row",
      "warmup_sets": [
        { "weight": 15, "reps": 10, "set_number": 1 },
        { "weight": 25, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 32.5, "reps": 10, "set_number": 1 },
        { "weight": 32.5, "reps": 10, "set_number": 2 },
        { "weight": 32.5, "reps": 10, "set_number": 3 },
        { "weight": 32.5, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 105,
      "notes": "Last session: 30kg x 10, 10, 9. Progressive overload +2.5kg. Focus on scapular retraction."
    },
    {
      "exercise_id": "back_face_pull",
      "warmup_sets": [
        { "weight": 5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 7.5, "reps": 12, "set_number": 1 },
        { "weight": 7.5, "reps": 12, "set_number": 2 },
        { "weight": 7.5, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.07 x 62kg = 4.34kg, rounded up to 7.5kg for conservative load. Pull rope to face height, external rotation at end."
    },
    {
      "exercise_id": "arms_barbell_curl",
      "warmup_sets": [
        { "weight": 5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 10, "reps": 12, "set_number": 1 },
        { "weight": 10, "reps": 12, "set_number": 2 },
        { "weight": 10, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.08 x 62kg = 4.96kg, rounded to 5kg EZ bar + weight = 10kg total. No swinging — strict form."
    },
    {
      "exercise_id": "arms_tricep_pushdown",
      "warmup_sets": [
        { "weight": 10, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 15, "reps": 12, "set_number": 1 },
        { "weight": 15, "reps": 12, "set_number": 2 },
        { "weight": 15, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.10 x 62kg = 6.2kg, rounded to 15kg machine stack for conservative load. Elbows tucked, no leaning."
    }
  ],
  "session_summary": {
    "total_working_sets": 17,
    "estimated_duration_minutes": 58,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Progression logic:** Last working weight +2.5kg (within 10% limit: 35kg -> 37.5kg = 7.1%, Section 8.1)
- **Cross-group fatigue:** Back rowing already loads biceps — arms isolation volume kept moderate (3 sets per arm exercise)
- **Intermediate volume:** 17 working sets within 16-20 range (Section 8.2)
- **Rest periods:** 105s for moderate compounds, 75s for isolation (Section 8.8 intermediate defaults)
- **Warm-up sets:** 2 for compounds, 1 for isolation (Section 8.5)

---

## Fixture 3: Advanced Male — Age 62 (Age Modifier Active)

### Input Profile

```json
{
  "experience_level": 3,
  "gender": "male",
  "age": 62,
  "body_weight_kg": 88.0,
  "selected_muscle_groups": ["Legs"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 75,
  "training_history": [
    {
      "exercise_id": "legs_barbell_back_squat",
      "sessions": [
        {
          "date": "2026-02-03",
          "sets": [
            { "weight": 110, "reps": 6, "set_type": "working" },
            { "weight": 110, "reps": 6, "set_type": "working" },
            { "weight": 110, "reps": 5, "set_type": "working" },
            { "weight": 110, "reps": 5, "set_type": "working" }
          ]
        }
      ]
    }
  ]
}
```

### Selected Exercises

1. `legs_barbell_back_squat` (Barbell Back Squat)
2. `legs_leg_press` (Leg Press)
3. `legs_lying_leg_curl` (Lying Leg Curl)
4. `legs_standing_calf_raise` (Standing Calf Raise)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "legs_barbell_back_squat",
      "warmup_sets": [
        { "weight": 20, "reps": 12, "set_number": 1 },
        { "weight": 45, "reps": 8, "set_number": 2 },
        { "weight": 65, "reps": 6, "set_number": 3 },
        { "weight": 85, "reps": 3, "set_number": 4 },
        { "weight": 100, "reps": 1, "set_number": 5 }
      ],
      "working_sets": [
        { "weight": 110, "reps": 6, "set_number": 1 },
        { "weight": 110, "reps": 6, "set_number": 2 },
        { "weight": 110, "reps": 6, "set_number": 3 },
        { "weight": 110, "reps": 6, "set_number": 4 }
      ],
      "rest_seconds": 225,
      "notes": "Age 60+ modifier: +2 warm-up sets (5 total), +45s rest (180s -> 225s), intensity capped at 90% working max. Weight maintained due to incomplete reps last session."
    },
    {
      "exercise_id": "legs_leg_press",
      "warmup_sets": [
        { "weight": 100, "reps": 10, "set_number": 1 },
        { "weight": 150, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 200, "reps": 10, "set_number": 1 },
        { "weight": 200, "reps": 10, "set_number": 2 },
        { "weight": 200, "reps": 10, "set_number": 3 },
        { "weight": 200, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 165,
      "notes": "Baseline: 2.50 x 88kg = 220kg, reduced -10% for age 60+ = 198kg, rounded to 200kg. Machine preferred over free weights for safety."
    },
    {
      "exercise_id": "legs_lying_leg_curl",
      "warmup_sets": [
        { "weight": 25, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 55, "reps": 10, "set_number": 1 },
        { "weight": 55, "reps": 10, "set_number": 2 },
        { "weight": 55, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: 0.75 x 88kg = 66kg, reduced -20% volume = 3 sets instead of 4, rounded to 55kg."
    },
    {
      "exercise_id": "legs_standing_calf_raise",
      "warmup_sets": [
        { "weight": 50, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 100, "reps": 12, "set_number": 1 },
        { "weight": 100, "reps": 12, "set_number": 2 },
        { "weight": 100, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: 1.25 x 88kg = 110kg, reduced -10% = 99kg, rounded to 100kg."
    }
  ],
  "session_summary": {
    "total_working_sets": 14,
    "estimated_duration_minutes": 72,
    "volume_check": "ok - volume reduced 20% per age 60+ guidelines"
  }
}
```

### Validation Notes

- **Age 60+ modifiers active (Section 8.6):**
  - Intensity reduced by 10% (applied to leg press baseline)
  - +2 warm-up sets for heavy compounds (squat has 5 total)
  - +45s rest periods (180s -> 225s for squat)
  - Volume reduced 20% (14 working sets vs typical 18-20 for advanced)
  - Machine exercises preferred (leg press over hack squat)
- **No weight increase on squat:** Last session incomplete (5, 5 reps on sets 3-4), so weight maintained
- **Warm-up protocol:** 5-step ramp for squat per age 60+ requirements (Section 8.5)

---

## Fixture 4: Beginner Female — Home Gym (Minimal Equipment)

### Input Profile

```json
{
  "experience_level": 1,
  "gender": "female",
  "age": 28,
  "body_weight_kg": 58.0,
  "selected_muscle_groups": ["Legs", "Core"],
  "available_equipment": "minimal_home",
  "session_duration_target_minutes": 40,
  "training_history": []
}
```

### Selected Exercises

1. `legs_dumbbell_walking_lunges` (Walking Lunges)
2. `legs_dumbbell_bulgarian_split_squat` (Bulgarian Split Squat)
3. `core_bodyweight_plank` (Plank)
4. `core_bodyweight_russian_twist` (Russian Twist)
5. `core_bodyweight_dead_bug` (Dead Bug)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "legs_dumbbell_walking_lunges",
      "warmup_sets": [
        { "weight": 0, "reps": 10, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 5, "reps": 12, "set_number": 1 },
        { "weight": 5, "reps": 12, "set_number": 2 },
        { "weight": 5, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.08 x 58kg = 4.64kg per hand, rounded to 5kg. Alternate legs each rep, upright torso."
    },
    {
      "exercise_id": "legs_dumbbell_bulgarian_split_squat",
      "warmup_sets": [
        { "weight": 0, "reps": 8, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 5, "reps": 10, "set_number": 1 },
        { "weight": 5, "reps": 10, "set_number": 2 },
        { "weight": 5, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.08 x 58kg = 4.64kg per hand, rounded to 5kg. Back foot elevated on bench/chair 30-40cm high."
    },
    {
      "exercise_id": "core_bodyweight_plank",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 20, "set_number": 1 },
        { "weight": 0, "reps": 20, "set_number": 2 },
        { "weight": 0, "reps": 20, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Time-based: 20 seconds hold per set. Maintain neutral spine, avoid hip sag."
    },
    {
      "exercise_id": "core_bodyweight_russian_twist",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 12, "set_number": 1 },
        { "weight": 0, "reps": 12, "set_number": 2 },
        { "weight": 0, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Bodyweight only. Count each side as 1 rep (24 total twists per set). Keep feet elevated for progression."
    },
    {
      "exercise_id": "core_bodyweight_dead_bug",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 8, "set_number": 1 },
        { "weight": 0, "reps": 8, "set_number": 2 },
        { "weight": 0, "reps": 8, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Reps per side (8 per side = 16 total reps per set). Lower back stays flat on floor throughout."
    }
  ],
  "session_summary": {
    "total_working_sets": 15,
    "estimated_duration_minutes": 38,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Minimal equipment constraint:** Only dumbbell and bodyweight exercises selected
- **Bodyweight progression:** Core exercises use time (plank) or reps (Russian twist, dead bug) — no weight progression available (Section 3.6)
- **Warm-up sets:** 1 for moderate compounds, 0 for bodyweight core (Section 8.5)
- **Female beginner baselines:** 0.08 BW per hand for lunges/split squats, 20s plank, 8 reps/side dead bug (Section 4.1)
- **Volume:** 15 working sets at upper end of beginner range (12-16 total sets, Section 8.2)

---

## Fixture 5: Intermediate Male — 3 Muscle Groups (Cross-Group Fatigue)

### Input Profile

```json
{
  "experience_level": 2,
  "gender": "male",
  "age": 35,
  "body_weight_kg": 82.0,
  "selected_muscle_groups": ["Chest", "Shoulders", "Arms"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 70,
  "training_history": []
}
```

### Selected Exercises

1. `chest_barbell_bench_press` (Barbell Bench Press)
2. `chest_incline_dumbbell_press` (Incline Dumbbell Press)
3. `shoulders_dumbbell_shoulder_press` (Dumbbell Shoulder Press)
4. `shoulders_lateral_raise` (Lateral Raise)
5. `arms_close_grip_bench_press` (Close-Grip Bench Press)
6. `arms_barbell_curl` (Barbell Curl)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "chest_barbell_bench_press",
      "warmup_sets": [
        { "weight": 20, "reps": 12, "set_number": 1 },
        { "weight": 40, "reps": 8, "set_number": 2 },
        { "weight": 55, "reps": 4, "set_number": 3 }
      ],
      "working_sets": [
        { "weight": 70, "reps": 10, "set_number": 1 },
        { "weight": 70, "reps": 10, "set_number": 2 },
        { "weight": 70, "reps": 10, "set_number": 3 },
        { "weight": 70, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: 0.85 x 82kg = 69.7kg, rounded to 70kg. Intermediate male. Full ROM, bar touches chest."
    },
    {
      "exercise_id": "chest_incline_dumbbell_press",
      "warmup_sets": [
        { "weight": 10, "reps": 10, "set_number": 1 },
        { "weight": 17.5, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 22.5, "reps": 10, "set_number": 1 },
        { "weight": 22.5, "reps": 10, "set_number": 2 },
        { "weight": 22.5, "reps": 10, "set_number": 3 },
        { "weight": 22.5, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 105,
      "notes": "Baseline: 0.28 x 82kg = 22.96kg per hand, rounded down to 22.5kg. 30-45 degree bench."
    },
    {
      "exercise_id": "shoulders_dumbbell_shoulder_press",
      "warmup_sets": [
        { "weight": 7.5, "reps": 10, "set_number": 1 },
        { "weight": 15, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 20, "reps": 10, "set_number": 2 },
        { "weight": 20, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 105,
      "notes": "Baseline: 0.25 x 82kg = 20.5kg per hand, rounded down to 20kg. CROSS-GROUP WARNING: Chest presses already loaded anterior delts. Volume reduced to 3 sets instead of 4."
    },
    {
      "exercise_id": "shoulders_lateral_raise",
      "warmup_sets": [
        { "weight": 2.5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 7.5, "reps": 12, "set_number": 1 },
        { "weight": 7.5, "reps": 12, "set_number": 2 },
        { "weight": 7.5, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.09 x 82kg = 7.38kg per hand, rounded down to 7.5kg. Isolation — no cross-group overlap."
    },
    {
      "exercise_id": "arms_close_grip_bench_press",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 40, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 55, "reps": 10, "set_number": 1 },
        { "weight": 55, "reps": 10, "set_number": 2 },
        { "weight": 55, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 105,
      "notes": "Baseline: 0.67 x 82kg = 54.94kg, rounded to 55kg. CROSS-GROUP WARNING: Chest + shoulder presses already loaded triceps. Volume reduced to 3 sets instead of 4."
    },
    {
      "exercise_id": "arms_barbell_curl",
      "warmup_sets": [
        { "weight": 10, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 20, "reps": 10, "set_number": 2 },
        { "weight": 20, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.25 x 82kg = 20.5kg, rounded down to 20kg. Biceps not pre-fatigued — standard volume."
    }
  ],
  "session_summary": {
    "total_working_sets": 20,
    "estimated_duration_minutes": 68,
    "volume_check": "ok - cross-group fatigue adjustments applied"
  }
}
```

### Validation Notes

- **Cross-group fatigue warning active (Section 2.2):**
  - Chest bench press loads anterior delts -> shoulder press volume reduced (4 sets -> 3 sets)
  - Chest + shoulder presses load triceps -> close-grip bench volume reduced (4 sets -> 3 sets)
  - Biceps not pre-fatigued by other groups -> full volume maintained
- **Intermediate baselines used:** Section 4.2 male intermediate ratios
- **Volume:** 20 working sets at ceiling of intermediate range (16-20 total sets, Section 8.2)
- **No training history:** Cold start, baseline tables applied

---

## Fixture 6: Advanced Female — With Block Periodization Context

### Input Profile

```json
{
  "experience_level": 3,
  "gender": "female",
  "age": 40,
  "body_weight_kg": 65.0,
  "selected_muscle_groups": ["Lower Back"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 60,
  "training_history": [
    {
      "exercise_id": "lower_back_conventional_deadlift",
      "sessions": [
        {
          "date": "2026-02-08",
          "sets": [
            { "weight": 75, "reps": 5, "set_type": "working" },
            { "weight": 75, "reps": 5, "set_type": "working" },
            { "weight": 75, "reps": 5, "set_type": "working" },
            { "weight": 75, "reps": 5, "set_type": "working" }
          ]
        },
        {
          "date": "2026-02-05",
          "sets": [
            { "weight": 72.5, "reps": 5, "set_type": "working" },
            { "weight": 72.5, "reps": 5, "set_type": "working" },
            { "weight": 72.5, "reps": 5, "set_type": "working" }
          ]
        }
      ]
    }
  ],
  "periodization_context": {
    "model": "block",
    "current_phase": "intensification",
    "week_number": 2,
    "weeks_since_deload": 5
  }
}
```

### Selected Exercises

1. `lower_back_conventional_deadlift` (Conventional Deadlift)
2. `lower_back_rack_pull` (Rack Pull)
3. `lower_back_back_extension` (Back Extension 45-degree)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "lower_back_conventional_deadlift",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 40, "reps": 6, "set_number": 2 },
        { "weight": 55, "reps": 3, "set_number": 3 },
        { "weight": 67.5, "reps": 1, "set_number": 4 }
      ],
      "working_sets": [
        { "weight": 77.5, "reps": 5, "set_number": 1 },
        { "weight": 77.5, "reps": 5, "set_number": 2 },
        { "weight": 77.5, "reps": 5, "set_number": 3 },
        { "weight": 77.5, "reps": 5, "set_number": 4 },
        { "weight": 77.5, "reps": 5, "set_number": 5 }
      ],
      "rest_seconds": 180,
      "notes": "Block periodization: Intensification phase week 2. Last session: 75kg x 5 x 4 completed. Progressive overload +2.5kg (3.3%). 5 working sets per intensification protocol."
    },
    {
      "exercise_id": "lower_back_rack_pull",
      "warmup_sets": [
        { "weight": 40, "reps": 8, "set_number": 1 },
        { "weight": 70, "reps": 4, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 95, "reps": 5, "set_number": 1 },
        { "weight": 95, "reps": 5, "set_number": 2 },
        { "weight": 95, "reps": 5, "set_number": 3 },
        { "weight": 95, "reps": 5, "set_number": 4 }
      ],
      "rest_seconds": 180,
      "notes": "Baseline: 1.45 x 65kg = 94.25kg, rounded to 95kg. Advanced female. Rack pull typically 15-20% above deadlift working weight."
    },
    {
      "exercise_id": "lower_back_back_extension",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 15, "reps": 10, "set_number": 1 },
        { "weight": 15, "reps": 10, "set_number": 2 },
        { "weight": 15, "reps": 10, "set_number": 3 },
        { "weight": 15, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: BW + 15kg for advanced female. Bodyweight isolation — no warm-up required. Controlled descent, avoid hyperextension at top."
    }
  ],
  "session_summary": {
    "total_working_sets": 13,
    "estimated_duration_minutes": 58,
    "volume_check": "ok - intensification phase volume (moderate: 12-16 sets/group/week)"
  }
}
```

### Validation Notes

- **Block periodization context:** Intensification phase, week 2 (Section 3.3)
  - Rep range: 4-6 reps (strength focus)
  - Working sets: 5 for primary compound (deadlift), 4 for secondary compounds
  - Volume moderate (13 sets vs 20+ in accumulation phase)
- **Progression:** +2.5kg from last session (75kg -> 77.5kg = 3.3%, within 10% limit)
- **Weeks since deload:** 5 weeks — approaching deload recommendation (every 4-5 weeks for advanced, Section 3.4)
- **Rest periods:** 180s for heavy compounds (advanced default, Section 8.8)
- **Advanced female baselines:** Section 4.3 (1.25 BW deadlift, 1.45 BW rack pull, BW+15kg back extension)

---

## Fixture 7: Beginner Male — Age 17 (Under-18 Modifier Active)

### Input Profile

```json
{
  "experience_level": 1,
  "gender": "male",
  "age": 17,
  "body_weight_kg": 70.0,
  "selected_muscle_groups": ["Legs", "Chest"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 60,
  "training_history": []
}
```

### Selected Exercises

1. `legs_barbell_back_squat` (Barbell Back Squat)
2. `legs_leg_extension` (Leg Extension)
3. `chest_barbell_bench_press` (Barbell Bench Press)
4. `chest_dumbbell_fly` (Dumbbell Fly)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "legs_barbell_back_squat",
      "warmup_sets": [
        { "weight": 20, "reps": 12, "set_number": 1 },
        { "weight": 30, "reps": 8, "set_number": 2 },
        { "weight": 35, "reps": 5, "set_number": 3 }
      ],
      "working_sets": [
        { "weight": 35, "reps": 12, "set_number": 1 },
        { "weight": 35, "reps": 12, "set_number": 2 },
        { "weight": 35, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 90,
      "notes": "AGE MODIFIER (under 18): Intensity capped at 85% estimated 1RM. Baseline 0.50 x 70kg = 35kg. Extra warm-up set added. Focus on movement quality — no singles or max-effort lifts."
    },
    {
      "exercise_id": "legs_leg_extension",
      "warmup_sets": [
        { "weight": 15, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 25, "reps": 12, "set_number": 1 },
        { "weight": 25, "reps": 12, "set_number": 2 },
        { "weight": 25, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Baseline: 0.35 x 70kg = 24.5kg, rounded to 25kg. Controlled eccentric, full ROM."
    },
    {
      "exercise_id": "chest_barbell_bench_press",
      "warmup_sets": [
        { "weight": 20, "reps": 12, "set_number": 1 },
        { "weight": 25, "reps": 8, "set_number": 2 },
        { "weight": 27.5, "reps": 5, "set_number": 3 }
      ],
      "working_sets": [
        { "weight": 27.5, "reps": 10, "set_number": 1 },
        { "weight": 27.5, "reps": 10, "set_number": 2 },
        { "weight": 27.5, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 90,
      "notes": "AGE MODIFIER (under 18): Extra warm-up set. Baseline 0.40 x 70kg = 28kg, rounded down to 27.5kg. Spotter required — no solo heavy lifts."
    },
    {
      "exercise_id": "chest_dumbbell_fly",
      "warmup_sets": [
        { "weight": 2.5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 5, "reps": 12, "set_number": 1 },
        { "weight": 5, "reps": 12, "set_number": 2 },
        { "weight": 5, "reps": 12, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Baseline: 0.08 x 70kg = 5.6kg per hand, rounded down to 5kg. Slight elbow bend throughout."
    }
  ],
  "session_summary": {
    "total_working_sets": 12,
    "estimated_duration_minutes": 55,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Under-18 age modifier active (Section 8.6):**
  - Intensity capped at 85% estimated 1RM (no near-max lifts)
  - No singles (1-rep sets) allowed
  - +1 extra warm-up set for heavy compounds (3 total instead of 2)
  - Focus on movement quality over load progression
- **Beginner volume:** 12 working sets (Section 8.2)
- **Baselines:** Section 4.1 male beginner
- **Safety emphasis:** Notes highlight spotter requirements and form focus

---

## Fixture 8: Intermediate Male — Single Muscle Group (Shoulders Only)

### Input Profile

```json
{
  "experience_level": 2,
  "gender": "male",
  "age": 29,
  "body_weight_kg": 78.0,
  "selected_muscle_groups": ["Shoulders"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 50,
  "training_history": []
}
```

### Selected Exercises

1. `shoulders_overhead_press_barbell` (Overhead Press)
2. `shoulders_dumbbell_shoulder_press` (Dumbbell Shoulder Press)
3. `shoulders_lateral_raise` (Lateral Raise)
4. `shoulders_barbell_shrug` (Barbell Shrug)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "shoulders_overhead_press_barbell",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 30, "reps": 6, "set_number": 2 },
        { "weight": 35, "reps": 3, "set_number": 3 }
      ],
      "working_sets": [
        { "weight": 42.5, "reps": 10, "set_number": 1 },
        { "weight": 42.5, "reps": 10, "set_number": 2 },
        { "weight": 42.5, "reps": 10, "set_number": 3 },
        { "weight": 42.5, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: 0.55 x 78kg = 42.9kg, rounded down to 42.5kg. Intermediate male. Strict press — no leg drive."
    },
    {
      "exercise_id": "shoulders_dumbbell_shoulder_press",
      "warmup_sets": [
        { "weight": 10, "reps": 10, "set_number": 1 },
        { "weight": 15, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 20, "reps": 10, "set_number": 2 },
        { "weight": 20, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 105,
      "notes": "Baseline: 0.25 x 78kg = 19.5kg per hand, rounded to 20kg. Volume reduced (3 sets vs 4) — overhead press already loaded anterior delts."
    },
    {
      "exercise_id": "shoulders_lateral_raise",
      "warmup_sets": [
        { "weight": 2.5, "reps": 12, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 7.5, "reps": 12, "set_number": 1 },
        { "weight": 7.5, "reps": 12, "set_number": 2 },
        { "weight": 7.5, "reps": 12, "set_number": 3 },
        { "weight": 7.5, "reps": 12, "set_number": 4 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 0.09 x 78kg = 7.02kg per hand, rounded to 7.5kg. Slight forward lean, raise to shoulder height."
    },
    {
      "exercise_id": "shoulders_barbell_shrug",
      "warmup_sets": [
        { "weight": 40, "reps": 10, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 80, "reps": 12, "set_number": 1 },
        { "weight": 80, "reps": 12, "set_number": 2 },
        { "weight": 80, "reps": 12, "set_number": 3 },
        { "weight": 80, "reps": 12, "set_number": 4 }
      ],
      "rest_seconds": 75,
      "notes": "Baseline: 1.05 x 78kg = 81.9kg, rounded down to 80kg. Upper traps isolation. No rolling motion."
    }
  ],
  "session_summary": {
    "total_working_sets": 15,
    "estimated_duration_minutes": 48,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Single muscle group:** Shoulders only — internal fatigue management (OHP loads anterior delts, so DB press volume reduced)
- **Intermediate baselines:** Section 4.2 male intermediate
- **Volume:** 15 working sets within intermediate range for single-group session
- **Exercise ordering:** Compound (OHP) first, then moderate compound (DB press), then isolation (lateral raise, shrug)

---

## Fixture 9: Advanced Male — Contraindicated Exercise Handling (Good Morning)

### Input Profile

```json
{
  "experience_level": 3,
  "gender": "male",
  "age": 38,
  "body_weight_kg": 90.0,
  "selected_muscle_groups": ["Lower Back"],
  "available_equipment": "full_gym",
  "session_duration_target_minutes": 60,
  "training_history": [
    {
      "exercise_id": "lower_back_conventional_deadlift",
      "sessions": [
        {
          "date": "2026-02-08",
          "sets": [
            { "weight": 145, "reps": 5, "set_type": "working" },
            { "weight": 145, "reps": 5, "set_type": "working" },
            { "weight": 145, "reps": 5, "set_type": "working" }
          ]
        }
      ]
    },
    {
      "exercise_id": "legs_barbell_back_squat",
      "sessions": [
        {
          "date": "2026-02-06",
          "sets": [
            { "weight": 120, "reps": 6, "set_type": "working" },
            { "weight": 120, "reps": 6, "set_type": "working" },
            { "weight": 120, "reps": 6, "set_type": "working" }
          ]
        }
      ]
    }
  ]
}
```

### Selected Exercises

1. `lower_back_conventional_deadlift` (Conventional Deadlift)
2. `lower_back_barbell_good_morning` (Good Morning)
3. `lower_back_back_extension` (Back Extension 45-degree)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "lower_back_conventional_deadlift",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 60, "reps": 6, "set_number": 2 },
        { "weight": 100, "reps": 3, "set_number": 3 },
        { "weight": 130, "reps": 1, "set_number": 4 }
      ],
      "working_sets": [
        { "weight": 147.5, "reps": 5, "set_number": 1 },
        { "weight": 147.5, "reps": 5, "set_number": 2 },
        { "weight": 147.5, "reps": 5, "set_number": 3 },
        { "weight": 147.5, "reps": 5, "set_number": 4 },
        { "weight": 147.5, "reps": 5, "set_number": 5 }
      ],
      "rest_seconds": 180,
      "notes": "Last session: 145kg x 5 x 3 completed. Progressive overload +2.5kg (1.7%). 5 working sets per advanced protocol."
    },
    {
      "exercise_id": "lower_back_barbell_good_morning",
      "warmup_sets": [
        { "weight": 20, "reps": 10, "set_number": 1 },
        { "weight": 40, "reps": 6, "set_number": 2 }
      ],
      "working_sets": [
        { "weight": 70, "reps": 8, "set_number": 1 },
        { "weight": 70, "reps": 8, "set_number": 2 },
        { "weight": 70, "reps": 8, "set_number": 3 }
      ],
      "rest_seconds": 120,
      "notes": "CONTRAINDICATION CONSTRAINT: Good Morning max weight = 60% of squat working weight. Squat history: 120kg. Max Good Morning: 72kg. Prescribed 70kg (97% of max). ADVANCED ONLY — never appears in beginner plans. Maintain neutral spine throughout."
    },
    {
      "exercise_id": "lower_back_back_extension",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 25, "reps": 10, "set_number": 1 },
        { "weight": 25, "reps": 10, "set_number": 2 },
        { "weight": 25, "reps": 10, "set_number": 3 },
        { "weight": 25, "reps": 10, "set_number": 4 }
      ],
      "rest_seconds": 120,
      "notes": "Baseline: BW + 25kg for advanced male. Controlled descent, avoid hyperextension at top."
    }
  ],
  "session_summary": {
    "total_working_sets": 12,
    "estimated_duration_minutes": 57,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Good Morning contraindication enforced (Section 8.4):**
  - Max weight = 60% of squat working weight (120kg squat -> 72kg max Good Morning)
  - AI prescribed 70kg (within constraint)
  - Marked Advanced — would be EXCLUDED if user was beginner or intermediate
  - Safety note included in plan
- **Deadlift progression:** +2.5kg from last session (145kg -> 147.5kg), within 10% limit
- **Advanced volume:** 12 working sets — appropriate for high-intensity lower back session
- **Exercise-specific gating:** Good Morning only appears because user is experience level 3

---

## Fixture 10: Beginner Gender-Not-Provided — Bodyweight Only

### Input Profile

```json
{
  "experience_level": 1,
  "gender": null,
  "age": 22,
  "body_weight_kg": 68.0,
  "selected_muscle_groups": ["Chest", "Core"],
  "available_equipment": "bodyweight_only",
  "session_duration_target_minutes": 35,
  "training_history": []
}
```

### Selected Exercises

1. `chest_bodyweight_push_up` (Push-Up)
2. `core_bodyweight_plank` (Plank)
3. `core_bodyweight_bicycle_crunch` (Bicycle Crunch)
4. `core_bodyweight_side_plank` (Side Plank)

### Expected Output

```json
{
  "exercise_plans": [
    {
      "exercise_id": "chest_bodyweight_push_up",
      "warmup_sets": [
        { "weight": 0, "reps": 8, "set_number": 1 }
      ],
      "working_sets": [
        { "weight": 0, "reps": 10, "set_number": 1 },
        { "weight": 0, "reps": 10, "set_number": 2 },
        { "weight": 0, "reps": 10, "set_number": 3 }
      ],
      "rest_seconds": 75,
      "notes": "Bodyweight compound. Gender not provided — using conservative rep target (10 reps, between male 12 and female 8 for beginners). If unable to complete, use incline push-ups."
    },
    {
      "exercise_id": "core_bodyweight_plank",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 22, "set_number": 1 },
        { "weight": 0, "reps": 22, "set_number": 2 },
        { "weight": 0, "reps": 22, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Time-based: 22 seconds hold per set. Gender not provided — using midpoint between male 30s and female 20s, reduced 15% = 21.25s, rounded to 22s."
    },
    {
      "exercise_id": "core_bodyweight_bicycle_crunch",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 13, "set_number": 1 },
        { "weight": 0, "reps": 13, "set_number": 2 },
        { "weight": 0, "reps": 13, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Reps per side (13 per side = 26 total twists). Gender not provided — midpoint between male 15 and female 12, reduced 15%."
    },
    {
      "exercise_id": "core_bodyweight_side_plank",
      "warmup_sets": [],
      "working_sets": [
        { "weight": 0, "reps": 17, "set_number": 1 },
        { "weight": 0, "reps": 17, "set_number": 2 },
        { "weight": 0, "reps": 17, "set_number": 3 }
      ],
      "rest_seconds": 60,
      "notes": "Time-based: 17 seconds hold per side. Gender not provided — midpoint between male 20s and female 15s, reduced 15%."
    }
  ],
  "session_summary": {
    "total_working_sets": 12,
    "estimated_duration_minutes": 32,
    "volume_check": "ok"
  }
}
```

### Validation Notes

- **Gender not provided (Section 4.1 guidance):**
  - Baseline formula: male ratio reduced by 15%
  - For bodyweight exercises: use midpoint between male and female targets, then reduce 15%
- **Bodyweight-only constraint:** No equipment exercises selected
- **Bodyweight progression:** Rep-based for push-up and crunches, time-based for planks (Section 3.6)
- **No warm-up sets for bodyweight core:** Per Section 8.5
- **Conservative targets:** AI applies safety margin when gender unknown

---

## Summary Table: Fixture Coverage

| # | Experience | Gender | Age | Equipment | Groups | Key Features |
|---|------------|--------|-----|-----------|--------|--------------|
| 1 | Beginner | Male | 25 | Full gym | 1 (Chest) | Cold start, baseline tables, no history |
| 2 | Intermediate | Female | 32 | Full gym | 2 (Back, Arms) | Training history, progressive overload, cross-group fatigue |
| 3 | Advanced | Male | 62 | Full gym | 1 (Legs) | Age 60+ modifier, +2 warm-ups, volume reduction |
| 4 | Beginner | Female | 28 | Home gym | 2 (Legs, Core) | Minimal equipment, bodyweight progression |
| 5 | Intermediate | Male | 35 | Full gym | 3 (Chest, Shoulders, Arms) | Cross-group fatigue warning, volume reduction |
| 6 | Advanced | Female | 40 | Full gym | 1 (Lower Back) | Block periodization, intensification phase |
| 7 | Beginner | Male | 17 | Full gym | 2 (Legs, Chest) | Under-18 modifier, +1 warm-up, 85% intensity cap |
| 8 | Intermediate | Male | 29 | Full gym | 1 (Shoulders) | Single muscle group, internal fatigue management |
| 9 | Advanced | Male | 38 | Full gym | 1 (Lower Back) | Good Morning contraindication, exercise-specific gating |
| 10 | Beginner | Not provided | 22 | Bodyweight | 2 (Chest, Core) | Gender unknown heuristic, bodyweight only |

---

## Implementation Notes for Epic 0.6.3

1. **GeminiPromptBuilder must inject all context** shown in these fixtures (experience level, body weight, age, gender, training history, periodization context, equipment constraints).

2. **GeminiResponseParser must validate** that returned JSON matches the schema in architecture.md Section 4.3 and exercise-science.md Section 5.3.

3. **Safety constraint violations** (e.g., weight jumps >10%, volume exceeding MRV, advanced exercises for beginners) should trigger a retry with an explicit constraint reminder appended to the prompt.

4. **Test implementation strategy:**
   - Unit tests: Pass each fixture input to `GeminiPromptBuilder.build()`, verify prompt contains all required sections
   - Integration tests: Mock Gemini API responses with these expected outputs, verify parsing succeeds
   - E2E tests: Send real requests to Gemini API with these inputs, validate response structure (not exact values, which will vary)

5. **Edge case coverage:**
   - Cold start (Fixtures 1, 4, 5, 7, 8, 10)
   - Training history present (Fixtures 2, 3, 6, 9)
   - Age modifiers (Fixtures 3, 7)
   - Gender not provided (Fixture 10)
   - Cross-group fatigue (Fixtures 2, 5, 8)
   - Contraindicated exercises (Fixture 9)
   - Block periodization (Fixture 6)
   - Bodyweight-only (Fixture 10)
   - Minimal equipment (Fixture 4)

6. **Validation layer (PlanValidationUseCase) must check:**
   - All weights rounded correctly (Section 8.7)
   - Volume within ceilings (Section 8.2)
   - Warm-up sets present for compounds (Section 8.5)
   - Weight jumps within limits (Section 8.1)
   - Age-adjusted parameters applied (Section 8.6)
   - Contraindication constraints respected (Section 8.4)

---

**Files referenced:**
- `docs/exercise-science.md` — Sections 4, 5.3, 5.4, 8
- `docs/architecture.md` — Section 4.3
