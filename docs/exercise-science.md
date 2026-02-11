# Exercise Science Reference — Deep Reps

Authority: Certified Strength & Conditioning Specialist (CSCS) / Head of Fitness
Status: Canonical reference for exercise database, progression logic, AI prompt design, and safety systems.

This document governs all training logic in the app. No exercise, progression model, or AI-generated plan ships without conforming to the specifications below.

---

## Table of Contents

1. [Exercise Database Design](#1-exercise-database-design)
2. [Muscle Group Taxonomy](#2-muscle-group-taxonomy)
3. [Progression Models](#3-progression-models)
4. [Baseline Plans by Experience Level](#4-baseline-plans-by-experience-level)
5. [AI Prompt Design Guidelines](#5-ai-prompt-design-guidelines)
6. [Auto-Ordering Algorithm](#6-auto-ordering-algorithm)
7. [1RM Estimation Formulas](#7-1rm-estimation-formulas)
8. [Safety Guardrails](#8-safety-guardrails)

---

## 1. Exercise Database Design

Every exercise in the app is assigned to exactly one primary muscle group. No duplicates across groups. The primary group assignment reflects the muscle that is the **prime mover** under standard execution of the lift — not the muscle that "also works."

### Data Schema Per Exercise

| Field | Type | Description |
|-------|------|-------------|
| `id` | string | Unique identifier (e.g., `legs_barbell_squat`) |
| `name` | string | Display name |
| `primary_group` | enum | One of the 7 muscle groups |
| `secondary_muscles` | string[] | Muscles significantly recruited but not the prime mover |
| `equipment` | enum | `barbell`, `dumbbell`, `cable`, `machine`, `bodyweight`, `kettlebell`, `band`, `ez_bar`, `trap_bar` |
| `movement_type` | enum | `compound` or `isolation` |
| `difficulty` | enum | `beginner`, `intermediate`, `advanced` |
| `description` | string | 1-2 sentence description of the movement |
| `tips` | string[] | Form cues (max 4) |
| `pros` | string[] | Key benefits (max 3) |
| `order_priority` | int | Used by auto-ordering algorithm (lower = earlier in workout) |
| `superset_tags` | string[] | Compatibility tags for superset suggestions |

### 1.1 Legs

Primary movers: quadriceps, hamstrings, glutes, calves.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Barbell Back Squat | Glutes, lower back, core | Barbell | Compound | Beginner |
| 2 | Barbell Front Squat | Core, upper back, glutes | Barbell | Compound | Intermediate |
| 3 | Leg Press | Glutes, hamstrings | Machine | Compound | Beginner |
| 4 | Romanian Deadlift (RDL) | Lower back, glutes | Barbell | Compound | Intermediate |
| 5 | Walking Lunges | Glutes, core, calves | Dumbbell | Compound | Beginner |
| 6 | Bulgarian Split Squat | Glutes, core | Dumbbell | Compound | Intermediate |
| 7 | Hack Squat | Glutes | Machine | Compound | Beginner |
| 8 | Leg Extension | — | Machine | Isolation | Beginner |
| 9 | Lying Leg Curl | Glutes | Machine | Isolation | Beginner |
| 10 | Seated Leg Curl | — | Machine | Isolation | Beginner |
| 11 | Standing Calf Raise | — | Machine | Isolation | Beginner |
| 12 | Seated Calf Raise | — | Machine | Isolation | Beginner |

**Notes:**
- Barbell Back Squat is the primary leg compound. It is assigned to Legs, not Lower Back, because the quadriceps are the prime mover through the full range of motion.
- RDL is assigned to Legs (hamstring-dominant) rather than Lower Back. The hip hinge with a knee-soft position makes the hamstrings the prime mover. Conventional Deadlift is assigned to Lower Back (see section 1.2).
- Walking Lunges default to dumbbells. Barbell lunges are a valid variation but not a separate exercise entry.

### 1.2 Lower Back

Primary movers: erector spinae, spinal stabilizers, posterior chain as a unit.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Conventional Deadlift | Glutes, hamstrings, traps, forearms | Barbell | Compound | Intermediate |
| 2 | Sumo Deadlift | Glutes, quads, adductors | Barbell | Compound | Intermediate |
| 3 | Trap Bar Deadlift | Glutes, quads, traps | Trap Bar | Compound | Beginner |
| 4 | Rack Pull | Traps, glutes, forearms | Barbell | Compound | Intermediate |
| 5 | Good Morning | Hamstrings, glutes | Barbell | Compound | Advanced |
| 6 | Back Extension (45-degree) | Glutes, hamstrings | Bodyweight | Isolation | Beginner |
| 7 | Reverse Hyperextension | Glutes, hamstrings | Machine | Isolation | Intermediate |
| 8 | Barbell Hip Thrust | Glutes, hamstrings | Barbell | Compound | Beginner |
| 9 | Cable Pull-Through | Glutes, hamstrings | Cable | Compound | Beginner |
| 10 | Deficit Deadlift | Glutes, hamstrings, quads | Barbell | Compound | Advanced |

**Notes:**
- Conventional Deadlift is placed here because the erector spinae works as the primary stabilizer and force transmitter. The movement fails when the lower back rounds — it is the limiting factor.
- Barbell Hip Thrust is assigned here despite significant glute involvement. The spinal stabilization demand and posterior chain integration justify this placement over Legs.
- Good Morning is marked Advanced due to high spinal loading demand and form sensitivity. It should never appear in beginner auto-generated plans.

### 1.3 Chest

Primary movers: pectoralis major (upper/clavicular head, sternal head, lower/costal fibers).

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Barbell Bench Press | Anterior delts, triceps | Barbell | Compound | Beginner |
| 2 | Incline Barbell Bench Press | Anterior delts, triceps, upper chest | Barbell | Compound | Beginner |
| 3 | Dumbbell Bench Press | Anterior delts, triceps | Dumbbell | Compound | Beginner |
| 4 | Incline Dumbbell Press | Anterior delts, triceps, upper chest | Dumbbell | Compound | Beginner |
| 5 | Decline Barbell Bench Press | Triceps, anterior delts | Barbell | Compound | Intermediate |
| 6 | Dumbbell Fly | Anterior delts | Dumbbell | Isolation | Beginner |
| 7 | Cable Crossover | Anterior delts | Cable | Isolation | Beginner |
| 8 | Machine Chest Press | Anterior delts, triceps | Machine | Compound | Beginner |
| 9 | Pec Deck / Machine Fly | — | Machine | Isolation | Beginner |
| 10 | Push-Up | Anterior delts, triceps, core | Bodyweight | Compound | Beginner |
| 11 | Dips (Chest-Focused) | Triceps, anterior delts | Bodyweight | Compound | Intermediate |

**Notes:**
- Dips are assigned to Chest with a "chest-focused" qualifier (torso leaned forward, wider grip). The Arms group does not contain dips — that would create a duplicate. Tricep-dominant dip is not a separate exercise; form cues explain body position adjustments.
- Push-Up is included for home/travel scenarios and warm-up utility. Marked as Beginner compound.

### 1.4 Back

Primary movers: latissimus dorsi, rhomboids, rear deltoids, teres major/minor.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Barbell Bent-Over Row | Rear delts, biceps, lower back | Barbell | Compound | Intermediate |
| 2 | Pull-Up | Biceps, core, rear delts | Bodyweight | Compound | Intermediate |
| 3 | Lat Pulldown | Biceps, rear delts | Cable | Compound | Beginner |
| 4 | Seated Cable Row | Rhomboids, biceps, rear delts | Cable | Compound | Beginner |
| 5 | Dumbbell Single-Arm Row | Biceps, rear delts, core | Dumbbell | Compound | Beginner |
| 6 | T-Bar Row | Biceps, rear delts, lower back | Barbell | Compound | Intermediate |
| 7 | Chest-Supported Row | Rear delts, biceps | Machine | Compound | Beginner |
| 8 | Chin-Up | Biceps, lats, core | Bodyweight | Compound | Intermediate |
| 9 | Straight-Arm Pulldown | Triceps (long head), core | Cable | Isolation | Beginner |
| 10 | Face Pull | Rear delts, rotator cuff, traps | Cable | Isolation | Beginner |
| 11 | Cable Lat Pullover | Triceps (long head), core | Cable | Isolation | Intermediate |

**Notes:**
- Pull-Up and Chin-Up are separate entries. The supinated grip of the Chin-Up shifts bicep recruitment significantly — they are not interchangeable.
- Face Pull is assigned to Back, not Shoulders, because the rear delt and mid-trap are the prime movers. The lateral and anterior delts are minimally involved.
- Barbell Bent-Over Row is Intermediate (not Beginner) due to lower back positioning demands.

### 1.5 Shoulders

Primary movers: anterior deltoid, lateral deltoid, posterior deltoid, trapezius.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Overhead Press (Barbell) | Triceps, upper chest, core | Barbell | Compound | Intermediate |
| 2 | Dumbbell Shoulder Press | Triceps, upper chest | Dumbbell | Compound | Beginner |
| 3 | Arnold Press | Triceps, upper chest | Dumbbell | Compound | Intermediate |
| 4 | Lateral Raise | Traps (upper) | Dumbbell | Isolation | Beginner |
| 5 | Cable Lateral Raise | Traps (upper) | Cable | Isolation | Beginner |
| 6 | Front Raise | Upper chest | Dumbbell | Isolation | Beginner |
| 7 | Reverse Fly (Dumbbell) | Rhomboids, traps | Dumbbell | Isolation | Beginner |
| 8 | Barbell Upright Row | Traps, biceps | Barbell | Compound | Intermediate |
| 9 | Barbell Shrug | — | Barbell | Isolation | Beginner |
| 10 | Dumbbell Shrug | — | Dumbbell | Isolation | Beginner |
| 11 | Landmine Press | Triceps, upper chest, core | Barbell | Compound | Intermediate |

**Notes:**
- Overhead Press (standing, barbell) is assigned to Shoulders even though it involves the entire kinetic chain. The deltoids are the prime movers.
- Barbell Upright Row is marked Intermediate due to impingement risk at high ROM. The AI should include a safety note recommending pulling only to chest height, not chin height.
- Shrugs are classified as Isolation — they involve a single joint action (scapular elevation) despite using heavy loads.

### 1.6 Arms

Primary movers: biceps brachii, brachialis, triceps brachii, forearm flexors/extensors.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Barbell Curl | Forearms, brachialis | Barbell | Isolation | Beginner |
| 2 | Dumbbell Curl | Forearms, brachialis | Dumbbell | Isolation | Beginner |
| 3 | Hammer Curl | Brachialis, forearms | Dumbbell | Isolation | Beginner |
| 4 | Preacher Curl | Brachialis | EZ Bar | Isolation | Beginner |
| 5 | Incline Dumbbell Curl | Forearms | Dumbbell | Isolation | Intermediate |
| 6 | Cable Curl | Forearms | Cable | Isolation | Beginner |
| 7 | Close-Grip Bench Press | Chest, anterior delts | Barbell | Compound | Intermediate |
| 8 | Skull Crusher (Lying Tricep Extension) | — | EZ Bar | Isolation | Intermediate |
| 9 | Tricep Pushdown (Cable) | — | Cable | Isolation | Beginner |
| 10 | Overhead Tricep Extension | — | Dumbbell | Isolation | Beginner |
| 11 | Concentration Curl | Brachialis | Dumbbell | Isolation | Beginner |
| 12 | Wrist Curl | — | Barbell | Isolation | Beginner |

**Notes:**
- Close-Grip Bench Press is the only compound in this group. It is assigned to Arms (not Chest) because the narrow grip shifts the prime mover to the triceps.
- All curls are classified as Isolation. While standing barbell curls technically allow some body English, the target movement is single-joint elbow flexion.
- Wrist Curl is included for forearm development. It is low-priority in auto-ordering.

### 1.7 Core

Primary movers: rectus abdominis, obliques (internal/external), transverse abdominis.

| # | Exercise | Secondary Muscles | Equipment | Type | Difficulty |
|---|----------|-------------------|-----------|------|------------|
| 1 | Plank | Lower back, shoulders | Bodyweight | Isolation | Beginner |
| 2 | Ab Wheel Rollout | Lats, shoulders, hip flexors | Bodyweight | Compound | Advanced |
| 3 | Hanging Leg Raise | Hip flexors, forearms | Bodyweight | Compound | Intermediate |
| 4 | Cable Crunch | — | Cable | Isolation | Beginner |
| 5 | Russian Twist | Obliques, hip flexors | Bodyweight | Isolation | Beginner |
| 6 | Bicycle Crunch | Obliques, hip flexors | Bodyweight | Isolation | Beginner |
| 7 | Dead Bug | Lower back, hip flexors | Bodyweight | Isolation | Beginner |
| 8 | Pallof Press | Obliques, shoulders | Cable | Isolation | Intermediate |
| 9 | Decline Sit-Up | Hip flexors | Bodyweight | Isolation | Beginner |
| 10 | Side Plank | Obliques, glutes | Bodyweight | Isolation | Beginner |
| 11 | Dragon Flag | Hip flexors, lats | Bodyweight | Compound | Advanced |

**Notes:**
- Ab Wheel Rollout and Dragon Flag are marked Advanced. They require significant baseline core strength and shoulder stability. They must not appear in beginner plans.
- Plank is classified as Isolation despite being an isometric full-body exercise — the training stimulus is overwhelmingly on the anterior core.
- Hanging Leg Raise is Compound because it involves multi-joint hip flexion and significant grip/shoulder engagement.

### Exercise Count Summary

| Group | Exercise Count |
|-------|---------------|
| Legs | 12 |
| Lower Back | 10 |
| Chest | 11 |
| Back | 11 |
| Shoulders | 11 |
| Arms | 12 |
| Core | 11 |
| **Total** | **78** |

---

## 2. Muscle Group Taxonomy

### 2.1 Group Definitions and Sub-Muscle Breakdown

#### Legs

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Quadriceps (anterior) | Rectus femoris, vastus lateralis, vastus medialis, vastus intermedius | Knee extension, hip flexion (rectus femoris) | Squat, Leg Press, Leg Extension |
| Hamstrings (posterior) | Biceps femoris, semitendinosus, semimembranosus | Knee flexion, hip extension | RDL, Lying Leg Curl, Seated Leg Curl |
| Glutes | Gluteus maximus, gluteus medius, gluteus minimus | Hip extension, abduction, external rotation | Squat, Lunges, Bulgarian Split Squat |
| Calves | Gastrocnemius, soleus | Ankle plantarflexion | Standing Calf Raise, Seated Calf Raise |

**Activation patterns:**
- Squat-pattern movements (knee-dominant): quads as primary, glutes as synergist, hamstrings as stabilizer.
- Hinge-pattern movements (hip-dominant): hamstrings and glutes as primary, erectors as stabilizer.
- Calf exercises are single-joint and isolated from the knee-hip complex.

#### Lower Back

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Erector spinae | Iliocostalis, longissimus, spinalis | Spinal extension, anti-flexion | Deadlift, Good Morning, Back Extension |
| Multifidus | Multifidus | Segmental spinal stabilization | Deadlift, all hip hinge variations |
| Quadratus lumborum | Quadratus lumborum | Lateral flexion, spinal stabilization | Deadlift, Rack Pull |

**Activation patterns:**
- Deadlift-pattern movements create isometric and concentric erector spinae loading throughout the pull.
- The lower back functions primarily as a stabilizer in most compound lifts (squats, rows). When it is the prime mover (back extension, good morning), spinal loads are highest and form precision is critical.

#### Chest

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Upper chest | Clavicular head of pectoralis major | Shoulder flexion, horizontal adduction | Incline Barbell Press, Incline Dumbbell Press |
| Mid chest | Sternal head of pectoralis major | Horizontal adduction, shoulder extension from flexed position | Flat Bench Press, Dumbbell Fly |
| Lower chest | Costal fibers of pectoralis major | Shoulder extension, horizontal adduction from elevated arm | Decline Bench Press, Dips |

**Activation patterns:**
- Bench angle determines relative fiber recruitment: 30-45 degrees = clavicular emphasis; flat = sternal emphasis; 15-30 degree decline = costal emphasis.
- All pressing movements recruit anterior deltoid and triceps as synergists. The wider the grip relative to shoulder width, the greater the pec stretch and the less tricep involvement.

#### Back

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Lats | Latissimus dorsi | Shoulder extension, adduction, internal rotation | Pull-Up, Lat Pulldown, Barbell Row |
| Rhomboids | Rhomboid major, rhomboid minor | Scapular retraction, downward rotation | Seated Cable Row, Face Pull |
| Mid/lower traps | Trapezius (middle, lower fibers) | Scapular retraction, depression | Rows, Face Pull |
| Rear delts | Posterior deltoid | Shoulder horizontal abduction, extension | Face Pull, Reverse Fly |
| Teres major/minor | Teres major, teres minor | Shoulder extension, external rotation (minor) | Pull-Up, Row variations |

**Activation patterns:**
- Vertical pulling (pulldowns, pull-ups): lats as primary, biceps and teres as synergists.
- Horizontal pulling (rows): rhomboids, mid-traps, rear delts, and lats all contribute. Row angle determines relative lat vs. rhomboid emphasis — more upright = more traps/rhomboids, more bent-over = more lats.
- Grip width and orientation: wide overhand = lat-dominant; narrow neutral/supinated = biceps and lower lats.

#### Shoulders

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Anterior delt | Anterior deltoid | Shoulder flexion, horizontal adduction | Overhead Press, Front Raise |
| Lateral delt | Lateral deltoid | Shoulder abduction | Lateral Raise, Upright Row |
| Posterior delt | Posterior deltoid | Shoulder horizontal abduction, extension | Reverse Fly (assigned to Shoulders), Face Pull (assigned to Back) |
| Upper traps | Upper trapezius | Scapular elevation | Shrugs, Upright Row |

**Activation patterns:**
- Overhead pressing primarily loads the anterior delt with significant tricep synergy. The lateral delt contributes to abduction through the press arc.
- Lateral raises isolate the lateral delt most effectively at 15-45 degrees of shoulder abduction with a slight forward lean.
- The posterior delt is shared between the Back and Shoulders groups. Face Pull is assigned to Back; Reverse Fly is assigned to Shoulders. This is an intentional split to avoid redundancy while ensuring posterior delt coverage regardless of which group the user trains.

#### Arms

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Biceps (long head) | Biceps brachii, caput longum | Elbow flexion, forearm supination | Incline Curl, Barbell Curl |
| Biceps (short head) | Biceps brachii, caput breve | Elbow flexion, forearm supination | Preacher Curl, Concentration Curl |
| Brachialis | Brachialis | Elbow flexion (strongest flexor) | Hammer Curl, Reverse Curl |
| Triceps (long head) | Triceps brachii, caput longum | Elbow extension, shoulder extension | Overhead Tricep Extension, Skull Crusher |
| Triceps (lateral head) | Triceps brachii, caput laterale | Elbow extension | Tricep Pushdown, Close-Grip Bench |
| Triceps (medial head) | Triceps brachii, caput mediale | Elbow extension (all movements) | All tricep exercises (always active) |
| Forearms | Wrist flexors/extensors, brachioradialis | Wrist flexion/extension, grip | Wrist Curl, Hammer Curl |

**Activation patterns:**
- Bicep long head is preferentially stretched when the shoulder is extended (incline curls). Short head is preferentially loaded when the shoulder is flexed (preacher curls).
- Tricep long head is the only biarticular head — it crosses both the elbow and shoulder joints. Overhead extensions maximally stretch it.
- Forearms are trained directly (wrist curls) and indirectly (heavy gripping during deadlifts, rows, curls).

#### Core

| Sub-Muscle | Anatomical Name | Function | Key Exercises |
|------------|-----------------|----------|---------------|
| Rectus abdominis | Rectus abdominis | Spinal flexion, anti-extension | Cable Crunch, Hanging Leg Raise, Dragon Flag |
| External obliques | Obliquus externus abdominis | Rotation, lateral flexion, compression | Russian Twist, Bicycle Crunch |
| Internal obliques | Obliquus internus abdominis | Rotation (opposite direction), compression | Pallof Press, Side Plank |
| Transverse abdominis | Transversus abdominis | Intra-abdominal pressure, spinal stability | Plank, Dead Bug, all bracing movements |

**Activation patterns:**
- Flexion-based movements (crunches, sit-ups) primarily load the rectus abdominis concentrically.
- Anti-extension exercises (plank, ab wheel) load the rectus abdominis and transverse abdominis isometrically — generally safer for the spine under Mcgill's recommendations.
- Anti-rotation exercises (Pallof Press) primarily load the obliques and transverse abdominis isometrically.
- The core functions as a stabilizer in virtually every compound lift. Direct core training supplements, but does not replace, the stabilization demand of heavy squats and deadlifts.

### 2.2 Cross-Group Activation Map

This table shows significant secondary activation across groups. The AI uses this to avoid redundant volume when multiple groups are trained in the same session.

| Primary Group | Exercises That Also Significantly Load |
|---------------|----------------------------------------|
| Legs (squats) | Lower Back (erectors), Core (bracing) |
| Lower Back (deadlifts) | Legs (glutes, hamstrings), Back (traps), Arms (grip) |
| Chest (bench press) | Shoulders (anterior delt), Arms (triceps) |
| Back (rows) | Arms (biceps), Shoulders (rear delts), Lower Back (isometric) |
| Shoulders (overhead press) | Arms (triceps), Chest (upper) |
| Arms | Minimal cross-group activation (isolation-dominant group) |
| Core | Minimal cross-group activation (isolation-dominant group) |

**Implication for AI:** When a user selects both Chest and Arms, the AI should reduce tricep isolation volume because the pressing movements already provide substantial tricep stimulus. Similarly, Chest + Shoulders should reduce anterior delt isolation volume.

---

## 3. Progression Models

### 3.1 Linear Progression (Beginners: 0-6 months)

**Who:** Users with experience level = 1 (Total Beginner).

**Mechanism:** Add weight every session if the previous session's target reps were completed for all working sets.

**Protocol:**

| Variable | Rule |
|----------|------|
| Weight increase per session | Upper body: 1.25-2.5 kg (2.5-5 lbs). Lower body: 2.5-5 kg (5-10 lbs). |
| Condition to increase | All working sets completed at target reps |
| Failure protocol | If target reps not met for 2+ sets, keep weight the same next session |
| Stall protocol | If weight is unchanged for 3 consecutive sessions, reduce by 10% and rebuild |
| Rep range | 8-12 for compounds, 10-15 for isolation |
| Working sets | 3 sets per exercise |
| Deload trigger | After 4-6 consecutive weeks without a stall (proactive) or after 2 stalls on the same exercise (reactive) |

**Example progression (Barbell Bench Press, beginner):**
```
Session 1:  40 kg x 10, 10, 10  --> all sets complete, increase
Session 2:  42.5 kg x 10, 10, 9 --> missed 1 rep, keep weight
Session 3:  42.5 kg x 10, 10, 10 --> all sets complete, increase
Session 4:  45 kg x 10, 9, 8    --> missed 2+ sets, keep weight
```

### 3.2 Daily Undulating Periodization — DUP (Intermediates: 6-18 months)

**Who:** Users with experience level = 2 (Intermediate).

**Mechanism:** Vary rep ranges and intensity within a training week. Each session targeting the same muscle group uses a different rep/intensity scheme.

**Protocol:**

| Day Type | Reps | Intensity (% est. 1RM) | Sets | Focus |
|----------|------|------------------------|------|-------|
| Hypertrophy | 8-12 | 65-75% | 3-4 | Volume accumulation |
| Strength | 4-6 | 80-87.5% | 4-5 | Neuromuscular adaptation |
| Power/Technique | 6-8 | 75-82.5% | 3-4 | Movement quality at moderate load |

**Progression rules:**
- Increase load when the upper end of the rep range is hit for all working sets at a given intensity day type.
- Weight increases: 1.25-2.5 kg for upper body, 2.5 kg for lower body.
- If a user trains the same group only once per week, the AI cycles through day types across weeks (week 1 = hypertrophy, week 2 = strength, week 3 = power).

**Example week (Barbell Back Squat, intermediate, 2x/week legs):**
```
Monday (Hypertrophy):   100 kg x 10, 10, 10, 9   (4 sets)
Thursday (Strength):    120 kg x 5, 5, 5, 4, 4    (5 sets)
```

### 3.3 Block Periodization (Advanced: 18+ months)

**Who:** Users with experience level = 3 (Advanced).

**Mechanism:** Organize training into focused 3-4 week blocks (mesocycles), each with a specific emphasis. Blocks progress from accumulation to intensification to realization.

**Block structure:**

| Block | Duration | Rep Range | Intensity | Volume | Goal |
|-------|----------|-----------|-----------|--------|------|
| Accumulation | 3-4 weeks | 8-12 | 65-75% | High (16-20 sets/group/week) | Work capacity, hypertrophy |
| Intensification | 3-4 weeks | 4-6 | 80-90% | Moderate (12-16 sets/group/week) | Strength, neural drive |
| Realization/Peak | 1-2 weeks | 1-3 | 90-97.5% | Low (8-12 sets/group/week) | Peaking, test maxes |
| Deload | 1 week | 8-10 | 50-60% | Low (8-10 sets/group/week) | Recovery |

**Progression rules:**
- Within each block, increase load by 1-2.5% per week if target reps are met.
- Volume (total sets) stays constant or increases slightly within a block, then drops at block transition.
- The AI tracks which block phase the user is in based on training history patterns. If history is insufficient, it defaults to Accumulation.

**Implementation note:** Block periodization requires the AI to maintain state across multiple weeks. The prompt must include not just the last 3-5 sessions, but a summary of the current mesocycle phase and week number. This is the most context-heavy prompt scenario.

### 3.4 Deload Protocols

A deload is a planned reduction in training stress to allow recovery and prevent overtraining.

| Trigger | Protocol |
|---------|----------|
| **Scheduled (proactive)** | Every 4th week for intermediates, every 4th-6th week for advanced, every 6-8 weeks for beginners |
| **Performance-triggered (reactive)** | 2+ consecutive sessions with regression (fewer reps or lower weight than previous session at same exercise) |
| **User-requested** | User can manually request a deload week at any time |

**Deload parameters:**

| Variable | Deload Value |
|----------|-------------|
| Volume (total sets) | Reduce by 40-50% (cut sets, not exercises) |
| Intensity (weight) | Reduce to 50-65% of recent working weights |
| RPE target | 5-6 (should feel easy) |
| Duration | 1 week (5-7 days) |

**What NOT to change during deload:**
- Exercise selection should remain the same (maintain movement patterns).
- Training frequency should remain the same (maintain habit).
- Only volume and intensity decrease.

### 3.5 Progressive Overload Mechanics

Progressive overload is the fundamental driver of adaptation. The app tracks and promotes overload through these vectors (in priority order):

1. **Load (weight):** Primary driver. Increase weight when rep targets are met.
2. **Volume (sets x reps):** Secondary driver. Add sets before adding weight if the user is struggling to increase load.
3. **Reps at a fixed weight:** Tertiary driver. If 80 kg x 8 last session, aim for 80 kg x 9 this session.
4. **Density (same work in less time):** Not explicitly tracked in v1, but rest timer data enables future implementation.

The AI should prefer load-based overload for compounds and rep-based overload for isolation exercises.

---

## 4. Baseline Plans by Experience Level

These tables provide the starting point for AI plan generation when no training history exists. All weights are expressed as multipliers of body weight (BW). The AI uses these ratios to calculate absolute weights based on the user's profile.

### 4.1 Beginner (Level 1: 0-6 months)

**Training parameters:**
- Rep range: 10-15 (compounds), 12-15 (isolation)
- Working sets: 3 per exercise
- Rest periods: 60-90s (compounds), 45-60s (isolation)
- Session volume: 12-16 total working sets
- Warm-up sets: 2 per compound (empty bar + 50% working weight)

**Body weight ratio baselines (working weight for sets of 10-12):**

| Exercise | Male | Female |
|----------|------|--------|
| Barbell Back Squat | 0.50 x BW | 0.35 x BW |
| Conventional Deadlift | 0.60 x BW | 0.40 x BW |
| Barbell Bench Press | 0.40 x BW | 0.20 x BW |
| Overhead Press | 0.25 x BW | 0.15 x BW |
| Barbell Bent-Over Row | 0.35 x BW | 0.20 x BW |
| Barbell Curl | 0.15 x BW | 0.08 x BW |
| Leg Press | 0.80 x BW | 0.60 x BW |
| Lat Pulldown | 0.35 x BW | 0.25 x BW |

**When gender is not provided:** Use the male ratios reduced by 15%. This is an imperfect heuristic — the app should prompt for gender as an optional field to improve accuracy.

**Example:** Male beginner, 80 kg body weight, Barbell Bench Press:
- Working weight: 0.40 x 80 = 32 kg (round to nearest 2.5 kg = 32.5 kg)
- Warm-up: 20 kg x 12, 25 kg x 8
- Working: 32.5 kg x 12 x 3 sets

### 4.2 Intermediate (Level 2: 6-18 months)

**Training parameters:**
- Rep range: 8-12 (compounds), 10-15 (isolation)
- Working sets: 3-4 per exercise
- Rest periods: 90-120s (compounds), 60-90s (isolation)
- Session volume: 16-20 total working sets
- Warm-up sets: 2-3 per compound (progressive ramp to working weight)

**Body weight ratio baselines (working weight for sets of 8-10):**

| Exercise | Male | Female |
|----------|------|--------|
| Barbell Back Squat | 1.00 x BW | 0.70 x BW |
| Conventional Deadlift | 1.25 x BW | 0.85 x BW |
| Barbell Bench Press | 0.75 x BW | 0.40 x BW |
| Overhead Press | 0.45 x BW | 0.25 x BW |
| Barbell Bent-Over Row | 0.65 x BW | 0.40 x BW |
| Barbell Curl | 0.25 x BW | 0.13 x BW |
| Leg Press | 1.50 x BW | 1.10 x BW |
| Lat Pulldown | 0.55 x BW | 0.40 x BW |

### 4.3 Advanced (Level 3: 18+ months)

**Training parameters:**
- Rep range: 3-6 (strength compounds), 8-12 (hypertrophy compounds), 10-15 (isolation)
- Working sets: 4-5 per exercise
- Rest periods: 2-3 min (heavy compounds), 90-120s (moderate compounds), 60-90s (isolation)
- Session volume: 20-25 total working sets
- Warm-up sets: 3-4 per compound (progressive ramp)

**Body weight ratio baselines (working weight for sets of 5-6):**

| Exercise | Male | Female |
|----------|------|--------|
| Barbell Back Squat | 1.50 x BW | 1.10 x BW |
| Conventional Deadlift | 1.75 x BW | 1.25 x BW |
| Barbell Bench Press | 1.25 x BW | 0.65 x BW |
| Overhead Press | 0.70 x BW | 0.40 x BW |
| Barbell Bent-Over Row | 0.90 x BW | 0.55 x BW |
| Barbell Curl | 0.35 x BW | 0.18 x BW |
| Leg Press | 2.50 x BW | 1.80 x BW |
| Lat Pulldown | 0.75 x BW | 0.55 x BW |

### 4.4 Volume Recommendations by Group (Weekly Working Sets)

These are per-muscle-group weekly targets, not per-session.

| Muscle Group | Beginner | Intermediate | Advanced |
|--------------|----------|--------------|----------|
| Legs (quads) | 8-10 | 12-16 | 16-20 |
| Legs (hamstrings) | 6-8 | 10-12 | 12-16 |
| Lower Back | 4-6 | 6-10 | 10-14 |
| Chest | 8-10 | 12-16 | 16-20 |
| Back | 8-10 | 14-18 | 18-22 |
| Shoulders | 6-8 | 10-14 | 14-18 |
| Arms (biceps) | 4-6 | 8-12 | 12-16 |
| Arms (triceps) | 4-6 | 8-12 | 12-16 |
| Core | 4-6 | 6-10 | 8-12 |

**Note:** These values are total direct working sets. Indirect volume (e.g., bicep work from rows) is NOT counted toward these targets. The upper end of each range is the Maximum Recoverable Volume (MRV) ceiling — exceeding it risks overtraining without proportional benefit.

### 4.5 Strength Milestones (Estimated 1RM Relative to Body Weight)

Used for progress tracking (section 5.3 of FEATURES.md) and experience level validation.

| Exercise | Beginner (1RM) | Intermediate (1RM) | Advanced (1RM) |
|----------|----------------|---------------------|----------------|
| Barbell Back Squat (M) | 0.75 x BW | 1.50 x BW | 2.00 x BW |
| Barbell Back Squat (F) | 0.50 x BW | 1.00 x BW | 1.50 x BW |
| Bench Press (M) | 0.60 x BW | 1.10 x BW | 1.50 x BW |
| Bench Press (F) | 0.30 x BW | 0.60 x BW | 1.00 x BW |
| Deadlift (M) | 1.00 x BW | 1.75 x BW | 2.50 x BW |
| Deadlift (F) | 0.65 x BW | 1.25 x BW | 1.75 x BW |
| Overhead Press (M) | 0.40 x BW | 0.70 x BW | 1.00 x BW |
| Overhead Press (F) | 0.25 x BW | 0.45 x BW | 0.65 x BW |

---

## 5. AI Prompt Design Guidelines

### 5.1 Context Required for Plan Generation

The AI prompt must include the following context, in priority order (if token limits are hit, drop from the bottom):

1. **Exercise list and order** for the current session (mandatory — without this, no plan is possible)
2. **User experience level** (1/2/3) (mandatory)
3. **User body weight** and **preferred unit** (mandatory for baseline calculations; if missing, omit BW-ratio calculations and use conservative absolute values)
4. **Training history** for selected exercises: last 3-5 sessions, including:
   - Date
   - Exercise name
   - Sets performed (weight x reps, warm-up vs. working)
   - Any notes logged
5. **User age** (if available — affects recovery recommendations)
6. **User gender** (if available — affects BW ratio baselines)
7. **Current mesocycle phase** (if applicable — advanced users in block periodization)
8. **Cross-group fatigue signal**: if the user is training groups with heavy overlap (e.g., Chest + Shoulders + Arms), flag this to the AI so it can reduce redundant volume

### 5.2 Safety Guardrails in the Prompt

The prompt MUST include these constraints as system-level instructions to the LLM:

```
SAFETY CONSTRAINTS (non-negotiable):
1. Never suggest a working weight more than 10% above the user's heaviest logged set for that exercise.
2. If no history exists, use the baseline tables for the user's experience level.
3. Never exceed the Maximum Recoverable Volume (MRV) ceiling for any muscle group.
4. For exercises marked "advanced" difficulty, only include them if user experience level >= 2.
5. Always include at least 1 warm-up set for every compound exercise.
6. Never program more than 2 exercises at RPE 9-10 in a single session.
7. If user age > 50, reduce max intensity by 5% and increase warm-up sets by 1.
8. Round all weights to the nearest 2.5 kg (or 5 lbs).
```

### 5.3 Required JSON Output Structure

The AI must return a valid JSON response. The app parses this directly — free-text responses are rejected and trigger a retry.

```json
{
  "session_plan": {
    "generated_at": "2026-02-11T10:30:00Z",
    "experience_level": 2,
    "body_weight_kg": 80,
    "exercises": [
      {
        "exercise_id": "chest_barbell_bench_press",
        "exercise_name": "Barbell Bench Press",
        "order": 1,
        "warmup_sets": [
          { "weight_kg": 20, "reps": 12 },
          { "weight_kg": 40, "reps": 8 },
          { "weight_kg": 55, "reps": 5 }
        ],
        "working_sets": [
          { "weight_kg": 70, "reps": 8, "set_number": 1 },
          { "weight_kg": 70, "reps": 8, "set_number": 2 },
          { "weight_kg": 70, "reps": 8, "set_number": 3 },
          { "weight_kg": 70, "reps": 8, "set_number": 4 }
        ],
        "rest_seconds": 120,
        "notes": "Increase by 2.5 kg next session if all sets completed."
      }
    ],
    "session_summary": {
      "total_working_sets": 16,
      "estimated_duration_minutes": 55,
      "primary_groups": ["chest"],
      "volume_check": {
        "chest": { "sets": 12, "status": "within_range" },
        "arms_triceps": { "sets": 4, "status": "within_range" }
      }
    }
  }
}
```

### 5.4 Example Prompt Template

```
You are a certified strength and conditioning specialist generating a workout plan.

USER PROFILE:
- Experience level: {{experience_level}} (1=beginner, 2=intermediate, 3=advanced)
- Body weight: {{body_weight_kg}} kg
- Age: {{age | "not provided"}}
- Gender: {{gender | "not provided"}}
- Preferred unit: {{unit}}

EXERCISES FOR THIS SESSION (in order):
{{#each exercises}}
{{order}}. {{name}} ({{equipment}}, {{movement_type}}, difficulty: {{difficulty}})
{{/each}}

TRAINING HISTORY (last {{history_sessions}} sessions for relevant exercises):
{{#each history}}
Session: {{date}}
{{#each sets}}
  {{exercise_name}}: {{weight_kg}} kg x {{reps}} ({{set_type}})
{{/each}}
{{/each}}

{{#if history_empty}}
NO TRAINING HISTORY AVAILABLE. Use baseline tables:
- Beginner working weight = {{baseline_ratios}} x body weight
- Rep range: {{rep_range}}
- Working sets: {{set_count}} per exercise
{{/if}}

PROGRESSION CONTEXT:
- Current periodization model: {{periodization_model}}
- Last session performance trend: {{trend}} (improving/stalled/regressing)
- Weeks since last deload: {{weeks_since_deload}}
{{#if deload_recommended}}
- DELOAD RECOMMENDED: Reduce volume by 40-50% and intensity to 50-65%.
{{/if}}

CROSS-GROUP FATIGUE:
{{#if overlap_warning}}
- WARNING: User is training overlapping groups ({{overlapping_groups}}).
  Reduce isolation volume for shared muscles ({{shared_muscles}}).
{{/if}}

SAFETY CONSTRAINTS (non-negotiable):
1. Never suggest a working weight more than 10% above the user's heaviest logged set.
2. If no history, use baseline tables for experience level {{experience_level}}.
3. Never exceed MRV ceilings: {{mrv_limits}}.
4. Only include "advanced" exercises if experience level >= 2.
5. Include at least 1 warm-up set per compound.
6. Max 2 exercises at RPE 9-10 per session.
7. Round weights to nearest 2.5 kg.
{{#if age_over_50}}
8. User is over 50: reduce max intensity by 5%, add 1 extra warm-up set.
{{/if}}

OUTPUT FORMAT:
Return a valid JSON object matching the session_plan schema. Do not include any text outside the JSON.
```

### 5.5 Prompt Versioning

Every prompt template must be versioned (e.g., `v1.0`, `v1.1`). The version is logged with every generated plan. This enables:
- A/B testing of prompt variations.
- Debugging when plans are poor.
- Rollback to a previous prompt version.

Changes to the prompt template require CSCS sign-off.

---

## 6. Auto-Ordering Algorithm

When the user selects exercises but has not manually reordered them, the app proposes an exercise order. This is a default — the user can freely reorder.

### 6.1 Ordering Rules (Priority Order)

**Rule 1: Compound before isolation.**
All compound exercises appear before all isolation exercises.

**Rule 2: Within compounds — large muscle group before small.**
The "size" of a muscle group determines its position. Order:

| Priority | Muscle Group | Rationale |
|----------|-------------|-----------|
| 1 | Legs (squat-pattern) | Largest muscle mass, highest systemic demand |
| 2 | Lower Back (deadlift-pattern) | Second-highest systemic demand |
| 3 | Back (rowing/pulling) | Large muscle group, high neural demand |
| 4 | Chest (pressing) | Large muscle group |
| 5 | Shoulders (overhead pressing) | Medium muscle group |
| 6 | Arms (close-grip press) | Small muscle group, only compound is CGBP |
| 7 | Core (compound core) | Stability role — train last to avoid fatigue that compromises other lifts |

**Rule 3: Within isolation — follow the same large-to-small order.**
After all compounds, isolation exercises follow the same group priority.

**Rule 4: Within same group and same type — by difficulty descending.**
Advanced exercises before intermediate before beginner within the same category. Rationale: harder exercises demand more neural freshness.

**Rule 5: Core exercises always last.**
Regardless of compound/isolation status, core exercises are placed at the end of the workout. Fatiguing the core before heavy compounds (squats, deadlifts, overhead press) is a safety risk.

### 6.2 Algorithm Pseudocode

```
function orderExercises(selectedExercises):
    groupPriority = {
        "legs": 1, "lower_back": 2, "back": 3,
        "chest": 4, "shoulders": 5, "arms": 6, "core": 7
    }
    difficultyPriority = {
        "advanced": 1, "intermediate": 2, "beginner": 3
    }

    // Separate core exercises
    coreExercises = filter(selectedExercises, e => e.primary_group == "core")
    nonCoreExercises = filter(selectedExercises, e => e.primary_group != "core")

    // Split non-core into compound and isolation
    compounds = filter(nonCoreExercises, e => e.movement_type == "compound")
    isolations = filter(nonCoreExercises, e => e.movement_type == "isolation")

    // Sort compounds: by group priority, then difficulty
    sort(compounds, by: [
        groupPriority[e.primary_group] ASC,
        difficultyPriority[e.difficulty] ASC
    ])

    // Sort isolations: by group priority, then difficulty
    sort(isolations, by: [
        groupPriority[e.primary_group] ASC,
        difficultyPriority[e.difficulty] ASC
    ])

    // Sort core: compounds first, then isolation, then by difficulty
    sort(coreExercises, by: [
        e.movement_type == "compound" ? 0 : 1 ASC,
        difficultyPriority[e.difficulty] ASC
    ])

    return concat(compounds, isolations, coreExercises)
```

### 6.3 Superset Compatibility

Two exercises can be suggested as a superset if:
- They target **antagonist** muscle groups (e.g., bicep curl + tricep pushdown).
- They target **unrelated** muscle groups (e.g., lateral raise + leg curl).
- Neither is a heavy compound at RPE 8+ (supersets compromise recovery between heavy sets).

**Compatible superset pairs (examples):**

| Exercise A | Exercise B | Pairing Type |
|-----------|-----------|--------------|
| Barbell Curl | Tricep Pushdown | Antagonist |
| Lateral Raise | Leg Curl | Unrelated |
| Dumbbell Fly | Face Pull | Antagonist (chest/rear delt) |
| Leg Extension | Lying Leg Curl | Antagonist (quad/hamstring) |
| Bicep Curl | Lateral Raise | Unrelated |
| Cable Crunch | Calf Raise | Unrelated |

**Incompatible pairs (never superset):**
- Two exercises for the same muscle group (e.g., barbell curl + dumbbell curl) — this is a drop set or extended set, not a superset.
- Two heavy compounds (e.g., squat + deadlift) — systemic fatigue makes this dangerous and counterproductive.
- Any exercise paired with a heavy deadlift or squat variation.

---

## 7. 1RM Estimation Formulas

### 7.1 Epley Formula

```
estimated_1RM = weight * (1 + reps / 30)
```

- Most widely used.
- Accurate for rep ranges 1-10.
- Overestimates at high rep counts (15+).

### 7.2 Brzycki Formula

```
estimated_1RM = weight * (36 / (37 - reps))
```

- Slightly more conservative than Epley at moderate rep ranges (6-10).
- Converges with Epley at low reps (1-5).
- Undefined at 37 reps (division by zero) — cap input at 36 reps.

### 7.3 Comparison at Common Rep Ranges

For a 100 kg lift:

| Reps | Epley 1RM | Brzycki 1RM | Difference |
|------|-----------|-------------|------------|
| 1 | 103.3 kg | 103.6 kg | 0.3% |
| 3 | 110.0 kg | 110.6 kg | 0.5% |
| 5 | 116.7 kg | 118.8 kg | 1.8% |
| 8 | 126.7 kg | 124.1 kg | 2.1% |
| 10 | 133.3 kg | 133.3 kg | 0.0% |
| 12 | 140.0 kg | 144.0 kg | 2.8% |
| 15 | 150.0 kg | 163.6 kg | 8.3% |

### 7.4 Recommendation for Deep Reps

**Use the Epley formula as the primary estimator.** Rationale:
- It is the most commonly cited in peer-reviewed literature.
- It produces reasonable estimates across the 1-12 rep range that covers the vast majority of working sets.
- It is simpler to compute and explain to users.

**Accuracy constraints:**
- Only calculate estimated 1RM from sets of 1-10 reps. Sets of 11+ reps produce increasingly unreliable estimates.
- Only use working sets for 1RM calculation. Warm-up sets are excluded.
- Display estimated 1RM with a confidence indicator: "high" (1-5 reps), "moderate" (6-10 reps), "low" (11+ reps, shown but flagged).

### 7.5 PR Detection Logic

A new Personal Record (PR) is detected when any of the following conditions are met:

**Weight PR:**
```
current_set.weight > max(all_previous_working_sets.weight)
WHERE exercise_id = current_set.exercise_id
```

**Rep PR at Weight:**
```
current_set.reps > max(all_previous_working_sets.reps)
WHERE exercise_id = current_set.exercise_id
AND weight >= current_set.weight
```

**Estimated 1RM PR:**
```
epley(current_set.weight, current_set.reps) > max(epley(s.weight, s.reps) for s in all_previous_working_sets)
WHERE exercise_id = current_set.exercise_id
AND current_set.reps <= 10  // only count reliable estimates
```

**Volume PR (per session, per muscle group):**
```
sum(weight * reps for all working_sets in current_session WHERE primary_group = G)
  > max(session_volumes for all previous sessions WHERE primary_group = G)
```

**PR notification rules:**
- PRs are detected in real-time during a workout (after each set is marked complete).
- PR notification should be visually prominent but not modal — do not interrupt the workout flow with a dialog.
- PRs are confirmed only after the workout is saved. If the user discards the workout, PRs are not recorded.
- Historical PR recalculation: if the user edits a past workout entry (e.g., corrects a weight typo), PR history must be recalculated for affected exercises.

---

## 8. Safety Guardrails

### 8.1 Maximum Weight Jump Per Session

The AI and the validation layer must enforce maximum weight increases between sessions.

| Exercise Type | Max Jump (absolute) | Max Jump (relative to last working weight) |
|--------------|--------------------|--------------------------------------------|
| Barbell compound (squat, bench, deadlift, OHP, row) | 10 kg / 22 lbs | 10% |
| Dumbbell compound | 5 kg / 11 lbs per hand | 10% |
| Machine compound | 10 kg / 22 lbs | 15% |
| Isolation (any) | 5 kg / 11 lbs | 15% |
| Bodyweight | N/A (progression via reps/added weight) | — |

**Enforcement:** Whichever limit is reached first (absolute or relative) applies. If the AI suggests a weight that exceeds either limit, the plan is rejected and regenerated with a constraint reminder.

**Cold start exception:** When no history exists and the user's first session is generated from baseline tables, these limits do not apply (there is no previous session to compare against).

### 8.2 Volume Ceilings (Per Session)

| Variable | Beginner | Intermediate | Advanced | Hard Maximum (all levels) |
|----------|----------|--------------|----------|---------------------------|
| Total working sets per session | 12-16 | 16-20 | 20-25 | 30 |
| Working sets per muscle group per session | 6-8 | 8-12 | 10-14 | 16 |
| Working sets per exercise | 3 | 3-4 | 4-5 | 6 |
| Total exercises per session | 4-6 | 5-8 | 6-10 | 12 |

**The hard maximum column is an absolute ceiling.** Even if the user manually adds exercises beyond these limits, the app should display a warning (not a block — the user has final authority, but the system must inform them).

### 8.3 Fatigue and Overtraining Warnings

The app monitors cumulative training stress and surfaces warnings when patterns suggest overtraining risk.

**Warning triggers:**

| Condition | Warning Message | Severity |
|-----------|----------------|----------|
| Weekly volume exceeds MRV ceiling for any group for 2+ consecutive weeks | "Your {{group}} volume has exceeded recommended limits for 2 weeks. Consider a deload." | Medium |
| Performance regression on a compound for 3+ consecutive sessions | "Your {{exercise}} has decreased in weight or reps for 3 sessions. This may indicate insufficient recovery." | High |
| Training frequency > 6 sessions/week for 2+ consecutive weeks | "You've trained 6+ days/week for over 2 weeks. Rest days are essential for adaptation." | High |
| Same muscle group trained 4+ times in a 7-day window | "You've hit {{group}} 4 times in the past week. Most individuals recover optimally training each group 2-3x/week." | Medium |
| Session duration exceeds 120 minutes | "Your session has exceeded 2 hours. Cortisol elevation beyond this point may impair recovery." | Low |

**Warning behavior:**
- Warnings are non-blocking. They are informational.
- Warnings appear on the workout summary screen and in the progress view.
- The user can dismiss warnings. Dismissed warnings for the same condition do not reappear for 7 days.

### 8.4 Contraindications and Exercise-Specific Safety Notes

Certain exercises carry elevated injury risk under specific conditions. The AI prompt and exercise detail view must include these notes.

| Exercise | Risk | Guardrail |
|----------|------|-----------|
| Barbell Upright Row | Shoulder impingement at high ROM | AI must include note: "Pull to chest height only. Stop if shoulder pain occurs." Do not program above moderate weight for beginners. |
| Good Morning | Lumbar shear force | Marked Advanced. Never appears in beginner plans. AI limits to 60% of squat working weight. |
| Behind-the-Neck Press | Shoulder impingement, rotator cuff strain | **Not included in exercise database.** Deliberately excluded. |
| Barbell Skull Crusher | Elbow stress | Marked Intermediate. AI recommends EZ bar over straight bar. Limits to 3 working sets max. |
| Deficit Deadlift | Increased lumbar flexion demand | Marked Advanced. Only programmed if conventional deadlift history shows good performance (no regression pattern). |
| Dragon Flag | Extreme core/hip flexor demand | Marked Advanced. Never auto-programmed. Only appears if user manually selects it. |
| Ab Wheel Rollout | Lower back hyperextension risk | Marked Advanced. AI includes note: "Maintain posterior pelvic tilt throughout. If lower back arches, reduce range of motion." |
| Decline Barbell Bench Press | Blood pressure spike in decline position, reduced shoulder stability | Marked Intermediate. Not programmed for users with reported hypertension (future profile field). |

### 8.5 Warm-Up Set Requirements

| Exercise Type | Minimum Warm-Up Sets | Protocol |
|--------------|---------------------|----------|
| Heavy compound (squat, deadlift, bench, OHP) | 3 | Empty bar x 10-12, 50% working weight x 6-8, 75% working weight x 3-4 |
| Moderate compound (row, leg press, lunges) | 2 | 50% working weight x 8-10, 75% working weight x 4-6 |
| Light compound (push-up, dips, pull-up) | 1 | Bodyweight x 8-10 (or band-assisted) |
| Isolation | 1 | 50% working weight x 10-12 |
| Bodyweight isolation (plank, side plank) | 0 | Not required |

**The AI must never generate a plan with zero warm-up sets for any compound exercise.** This is a hard constraint in the prompt.

### 8.6 Age-Adjusted Safety Modifiers

When user age is provided:

| Age Range | Modifier |
|-----------|----------|
| Under 18 | Cap intensity at 85% estimated 1RM. No singles (1-rep sets). Add 1 extra warm-up set. |
| 18-40 | No modification (standard protocols). |
| 41-50 | Reduce max intensity by 2.5%. Recommend longer rest periods (+15s). |
| 51-60 | Reduce max intensity by 5%. Add 1 warm-up set. Increase rest periods by 30s. Reduce max weekly volume by 10%. |
| 60+ | Reduce max intensity by 10%. Add 2 warm-up sets. Increase rest periods by 45s. Reduce max weekly volume by 20%. Prefer machine exercises over free weights for compounds. |

**These modifiers are defaults.** They can be overridden by the user's actual performance history. If a 55-year-old user consistently performs at advanced-level strength standards, the age modifiers should attenuate based on demonstrated capacity.

### 8.7 Weight Rounding Rules

All suggested weights must be rounded to practically loadable values:

| Equipment | Rounding Increment |
|-----------|--------------------|
| Barbell | 2.5 kg / 5 lbs (standard plate increments) |
| Dumbbell | 2.5 kg / 5 lbs (typical dumbbell increments) |
| Cable/Machine | 5 kg / 10 lbs (typical weight stack increments) |
| EZ Bar | 2.5 kg / 5 lbs |

**Always round down, not up.** When in doubt, prescribe the lighter load. The user can always add weight; starting too heavy risks form breakdown and injury.

---

## Appendix A: Exercise ID Convention

Format: `{group}_{equipment}_{exercise_name_snake_case}`

Examples:
- `legs_barbell_back_squat`
- `chest_dumbbell_fly`
- `arms_cable_curl`
- `core_bodyweight_plank`
- `lower_back_barbell_conventional_deadlift`

IDs must be stable across app versions. Once assigned, an exercise ID is never changed or reused.

## Appendix B: Unit Conversion Reference

| From | To | Factor |
|------|----|--------|
| kg | lbs | x 2.20462 |
| lbs | kg | x 0.45359 |

The app stores all weights internally in kilograms. Display conversion to pounds is applied at the UI layer only. AI prompts always use kilograms. The response is converted to the user's preferred unit for display.

## Appendix C: References

- Schoenfeld, B.J. (2010). The mechanisms of muscle hypertrophy and their application to resistance training. *Journal of Strength and Conditioning Research*, 24(10), 2857-2872.
- Epley, B. (1985). Poundage chart. *Boyd Epley Workout*. University of Nebraska-Lincoln.
- Brzycki, M. (1993). Strength testing — predicting a one-rep max from reps-to-fatigue. *Journal of Physical Education, Recreation & Dance*, 64(1), 88-90.
- Helms, E.R., Cronin, J., Storey, A., & Zourdos, M.C. (2016). Application of the repetition in reserve-based rating of perceived exertion scale for resistance training. *Strength and Conditioning Journal*, 38(4), 42-49.
- Israetel, M., Hoffmann, J., & Smith, C.W. (2015). *Scientific Principles of Strength Training*. Renaissance Periodization.
- McGill, S.M. (2010). Core training: Evidence translating to better performance and injury prevention. *Strength and Conditioning Journal*, 32(3), 33-46.
- NSCA. (2016). *Essentials of Strength Training and Conditioning* (4th ed.). Human Kinetics.
- Zourdos, M.C., et al. (2016). Novel resistance training-specific rating of perceived exertion scale measuring repetitions in reserve. *Journal of Strength and Conditioning Research*, 30(1), 267-275.
