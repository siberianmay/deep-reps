# Exercise Metadata Supplement

**Purpose:** Provides the 7 missing fields for all 78 exercises. This data supplements the exercise tables in `exercise-science.md` Section 1.
**Authority:** CSCS
**Status:** Ready for review and database seeding

**Fields provided per exercise:**
- `stableId` — Unique stable identifier per Appendix A convention
- `description` — 1-2 sentence biomechanical description
- `tips` — 1-4 form cues (array)
- `pros` — 1-3 key benefits (array)
- `order_priority` — Integer for auto-ordering (lower = earlier in workout, within group)
- `superset_tags` — Compatibility tags for superset suggestions
- `auto_program_min_level` — Minimum experience level for AI auto-inclusion (1=beginner, 2=intermediate, 3=advanced, 99=never)

---

## 1. Legs (12 exercises)

### Barbell Back Squat
- **stableId:** `legs_barbell_back_squat`
- **description:** The foundational lower body compound movement. Quadriceps are the prime mover through full range of motion, with significant glute and core engagement.
- **tips:** ["Bar on upper traps, not neck", "Drive knees out over toes", "Break at hips and knees simultaneously", "Chest up, neutral spine throughout"]
- **pros:** ["Highest lower body muscle recruitment", "Functional strength carryover", "Core stability development"]
- **order_priority:** 1
- **superset_tags:** ["quad_dominant", "heavy_compound", "barbell"]
- **auto_program_min_level:** 1

### Barbell Front Squat
- **stableId:** `legs_barbell_front_squat`
- **description:** Front-loaded squat variation emphasizing quad recruitment and demanding significant upper back and core stability. More vertical torso than back squat.
- **tips:** ["Elbows high, bar rests on front delts", "Vertical torso -- do not lean forward", "Core braced to prevent forward collapse", "Can use cross-grip if wrist mobility limited"]
- **pros:** ["Greater quad isolation than back squat", "Reduced spinal compression", "Core stabilization challenge"]
- **order_priority:** 2
- **superset_tags:** ["quad_dominant", "heavy_compound", "barbell"]
- **auto_program_min_level:** 2

### Leg Press
- **stableId:** `legs_machine_leg_press`
- **description:** Machine-based compound allowing heavy loading with reduced stabilization demand. Targets quads, glutes, and hamstrings.
- **tips:** ["Feet shoulder-width, mid-platform", "Lower to 90 degrees knee flexion", "Do not allow lower back to round off the pad", "Press through full foot, not just toes"]
- **pros:** ["Beginner-friendly heavy loading", "Reduced technique barrier vs. squats", "Adjustable foot position for emphasis shifts"]
- **order_priority:** 3
- **superset_tags:** ["quad_dominant", "moderate_compound", "machine"]
- **auto_program_min_level:** 1

### Romanian Deadlift (RDL)
- **stableId:** `legs_barbell_rdl`
- **description:** Hip hinge with soft knees isolating hamstrings as the prime mover. Glutes and lower back provide synergistic support.
- **tips:** ["Soft knee bend -- not a stiff leg", "Push hips back, not down", "Bar tracks close to shins and thighs", "Stretch hamstrings at bottom, contract glutes at top"]
- **pros:** ["Hamstring hypertrophy emphasis", "Hip hinge pattern reinforcement", "Minimal lower back fatigue vs. conventional deadlift"]
- **order_priority:** 4
- **superset_tags:** ["hip_hinge", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Walking Lunges
- **stableId:** `legs_dumbbell_walking_lunge`
- **description:** Unilateral lower body compound. High glute activation and balance demand.
- **tips:** ["Step forward into lunge, do not step back", "Back knee hovers just above floor", "Torso upright throughout", "Push through front heel to stand"]
- **pros:** ["Unilateral balance and stability", "Glute activation higher than bilateral squats", "Functional movement pattern"]
- **order_priority:** 5
- **superset_tags:** ["quad_dominant", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 1

### Bulgarian Split Squat
- **stableId:** `legs_dumbbell_bulgarian_split_squat`
- **description:** Elevated rear foot split squat creating extreme quad and glute demand on the front leg. Unilateral balance challenge.
- **tips:** ["Rear foot on bench 12-18 inches high", "Front shin vertical at bottom", "Do not allow front knee to collapse inward", "Torso slight forward lean is acceptable"]
- **pros:** ["Unilateral strength development", "Lower spinal load than bilateral squats", "Glute hypertrophy stimulus"]
- **order_priority:** 6
- **superset_tags:** ["quad_dominant", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 2

### Hack Squat
- **stableId:** `legs_machine_hack_squat`
- **description:** Machine squat with angled back support isolating quads with minimal lower back involvement.
- **tips:** ["Shoulders under pads, back flat against support", "Feet shoulder-width, mid-platform", "Lower until thighs parallel to platform", "Drive through heels"]
- **pros:** ["Quad isolation without spinal loading", "Beginner-friendly fixed path", "Safe near-failure training"]
- **order_priority:** 7
- **superset_tags:** ["quad_dominant", "moderate_compound", "machine"]
- **auto_program_min_level:** 1

### Leg Extension
- **stableId:** `legs_machine_leg_extension`
- **description:** Single-joint knee extension isolating the quadriceps. No hip or lower back involvement.
- **tips:** ["Adjust seat so knees align with machine pivot", "Controlled eccentric -- do not drop weight", "Full extension with 1-second squeeze", "Do not hyperextend knee"]
- **pros:** ["Pure quad isolation", "Safe for knee rehabilitation (when loaded appropriately)", "Easy to track progressive overload"]
- **order_priority:** 8
- **superset_tags:** ["quad_dominant", "isolation", "machine"]
- **auto_program_min_level:** 1

### Lying Leg Curl
- **stableId:** `legs_machine_lying_leg_curl`
- **description:** Prone hamstring isolation with glute synergy. Single-joint knee flexion.
- **tips:** ["Hips stay down on pad -- do not lift", "Curl heels to glutes", "Control the eccentric phase", "Feet neutral or slightly plantarflexed"]
- **pros:** ["Hamstring hypertrophy isolation", "Reduced lower back involvement vs. RDL", "Safe to train near failure"]
- **order_priority:** 9
- **superset_tags:** ["hip_hinge", "isolation", "machine"]
- **auto_program_min_level:** 1

### Seated Leg Curl
- **stableId:** `legs_machine_seated_leg_curl`
- **description:** Seated hamstring isolation with reduced glute involvement compared to lying variation.
- **tips:** ["Adjust thigh pad to secure legs", "Pull heels under seat", "1-second contraction at peak", "Avoid rocking torso forward"]
- **pros:** ["Hamstring isolation without hip extension", "Comfortable setup for high-rep sets", "Lower back stays neutral"]
- **order_priority:** 10
- **superset_tags:** ["hip_hinge", "isolation", "machine"]
- **auto_program_min_level:** 1

### Standing Calf Raise
- **stableId:** `legs_machine_standing_calf_raise`
- **description:** Straight-leg calf raise emphasizing gastrocnemius (visible calf muscle).
- **tips:** ["Full stretch at bottom -- heels below toes", "Drive through balls of feet", "1-second contraction at top", "Knees locked throughout"]
- **pros:** ["Gastrocnemius hypertrophy", "High load capacity", "Simple execution"]
- **order_priority:** 11
- **superset_tags:** ["calf", "isolation", "machine"]
- **auto_program_min_level:** 1

### Seated Calf Raise
- **stableId:** `legs_machine_seated_calf_raise`
- **description:** Bent-knee calf raise emphasizing soleus (deeper calf muscle under gastrocnemius).
- **tips:** ["Knees bent 90 degrees", "Full stretch at bottom", "Controlled tempo -- no bouncing", "Toes pointed straight ahead"]
- **pros:** ["Soleus isolation", "Complements standing calf raises", "Lower weight allows higher reps"]
- **order_priority:** 12
- **superset_tags:** ["calf", "isolation", "machine"]
- **auto_program_min_level:** 1

---

## 2. Lower Back (10 exercises)

### Conventional Deadlift
- **stableId:** `lower_back_barbell_conventional_deadlift`
- **description:** The king of posterior chain compounds. Erector spinae are the primary stabilizer and force transmitter -- the movement fails when the lower back rounds.
- **tips:** ["Bar over mid-foot, shins vertical", "Pull slack out before initiating lift", "Neutral spine -- no rounding or hyperextension", "Drive through floor with legs, lock hips and knees simultaneously"]
- **pros:** ["Highest total-body muscle recruitment", "Posterior chain strength and hypertrophy", "Functional carryover to all lifting"]
- **order_priority:** 1
- **superset_tags:** ["hip_hinge", "heavy_compound", "barbell"]
- **auto_program_min_level:** 2

### Sumo Deadlift
- **stableId:** `lower_back_barbell_sumo_deadlift`
- **description:** Wide-stance deadlift variation with greater quad and adductor involvement. Reduced lower back shear vs. conventional.
- **tips:** ["Stance 1.5x shoulder width, toes out 30-45 degrees", "Grip inside knees", "Vertical torso -- more upright than conventional", "Pull knees out as you drive up"]
- **pros:** ["Reduced lumbar stress vs. conventional", "Greater quad recruitment", "Shorter range of motion favors leverages for some lifters"]
- **order_priority:** 2
- **superset_tags:** ["hip_hinge", "heavy_compound", "barbell"]
- **auto_program_min_level:** 2

### Trap Bar Deadlift
- **stableId:** `lower_back_trapbar_deadlift`
- **description:** Neutral-grip deadlift with more upright torso and greater quad involvement. Easier to learn than barbell deadlift.
- **tips:** ["Stand in center of trap bar", "Vertical shins, neutral spine", "Drive through floor with legs", "Grip handles at sides, not forward"]
- **pros:** ["Beginner-friendly deadlift alternative", "Reduced lumbar shear force", "Allows heavier loading safely"]
- **order_priority:** 3
- **superset_tags:** ["hip_hinge", "heavy_compound", "trap_bar"]
- **auto_program_min_level:** 1

### Rack Pull
- **stableId:** `lower_back_barbell_rack_pull`
- **description:** Partial-range deadlift starting from pins or blocks. Emphasizes upper back and traps with heavy loads.
- **tips:** ["Set pins at or below knee height", "Same setup as deadlift from top position", "No bounce off pins -- dead stop each rep", "Focus on upper back retraction at lockout"]
- **pros:** ["Overload training for deadlift lockout", "Trap and upper back hypertrophy", "Builds grip strength with supra-maximal loads"]
- **order_priority:** 4
- **superset_tags:** ["hip_hinge", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Good Morning
- **stableId:** `lower_back_barbell_good_morning`
- **description:** Barbell hip hinge with high lower back and hamstring demand. Requires excellent spinal control. Advanced only.
- **tips:** ["Bar on upper traps (squat position)", "Soft knee bend throughout", "Push hips back, maintain neutral spine", "Do not go below parallel if lower back rounds"]
- **pros:** ["Erector spinae hypertrophy", "Reinforces hip hinge pattern", "Hamstring stretch under load"]
- **order_priority:** 5
- **superset_tags:** ["hip_hinge", "moderate_compound", "barbell"]
- **auto_program_min_level:** 3

### Back Extension (45-degree)
- **stableId:** `lower_back_bodyweight_back_extension`
- **description:** Hyperextension bench exercise targeting erector spinae isometrically and concentrically. Beginner-friendly lower back isolation.
- **tips:** ["Hips on pad, ankles secured", "Hands behind head or crossed on chest", "Lower torso to 45 degrees, not 90", "Extend to neutral, do not hyperextend"]
- **pros:** ["Lower back endurance and stability", "Beginner-safe with bodyweight", "Can add weight as progression"]
- **order_priority:** 6
- **superset_tags:** ["hip_hinge", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Reverse Hyperextension
- **stableId:** `lower_back_machine_reverse_hyperextension`
- **description:** Machine-based hip extension with reduced spinal compression. Glutes and hamstrings as primary movers.
- **tips:** ["Torso on pad, legs hang off edge", "Swing legs up to parallel", "Squeeze glutes at top", "Control descent -- no momentum"]
- **pros:** ["Decompression effect on spine", "Glute and hamstring activation without spinal load", "Rehabilitation-friendly"]
- **order_priority:** 7
- **superset_tags:** ["hip_hinge", "isolation", "machine"]
- **auto_program_min_level:** 2

### Barbell Hip Thrust
- **stableId:** `lower_back_barbell_hip_thrust`
- **description:** Supine hip extension with maximal glute recruitment. Barbell rests on hips. Lower back stabilization demand justifies placement in this group.
- **tips:** ["Upper back on bench edge, feet flat", "Bar over hip crease (use pad)", "Drive hips to full extension", "Squeeze glutes at top, 1-second hold"]
- **pros:** ["Highest glute activation of any exercise", "Lower back spinal stability training", "Safe heavy loading"]
- **order_priority:** 8
- **superset_tags:** ["hip_hinge", "moderate_compound", "barbell"]
- **auto_program_min_level:** 1

### Cable Pull-Through
- **stableId:** `lower_back_cable_pull_through`
- **description:** Cable hip hinge teaching proper deadlift mechanics. Beginner-friendly with constant tension.
- **tips:** ["Stand facing away from cable, rope between legs", "Push hips back to stretch hamstrings", "Drive hips forward to stand", "Do not pull with arms"]
- **pros:** ["Hip hinge pattern reinforcement", "Lower back and glute activation without heavy load", "Constant cable tension"]
- **order_priority:** 9
- **superset_tags:** ["hip_hinge", "moderate_compound", "cable"]
- **auto_program_min_level:** 1

### Deficit Deadlift
- **stableId:** `lower_back_barbell_deficit_deadlift`
- **description:** Deadlift standing on a 1-3 inch platform. Increases range of motion and lumbar demand. Advanced only.
- **tips:** ["Stand on 1-2 inch platform", "All conventional deadlift cues apply", "Greater hamstring stretch at bottom", "Reduce load 10-15% vs. conventional"]
- **pros:** ["Increased range of motion training", "Off-the-floor strength development", "Addresses weak points in deadlift"]
- **order_priority:** 10
- **superset_tags:** ["hip_hinge", "heavy_compound", "barbell"]
- **auto_program_min_level:** 3

---

## 3. Chest (11 exercises)

### Barbell Bench Press
- **stableId:** `chest_barbell_bench_press`
- **description:** The foundational horizontal pressing movement. Pectoralis major (sternal head) as prime mover with anterior delt and tricep synergy.
- **tips:** ["Feet flat, shoulder blades retracted", "Bar to mid-chest, not neck", "Elbows 45-degree angle to torso", "Press in slight arc toward face"]
- **pros:** ["Highest chest and tricep recruitment", "Benchmarks strength progression clearly", "Allows heaviest loading safely"]
- **order_priority:** 1
- **superset_tags:** ["push_horizontal", "heavy_compound", "barbell"]
- **auto_program_min_level:** 1

### Incline Barbell Bench Press
- **stableId:** `chest_barbell_incline_bench_press`
- **description:** Barbell press on 30-45 degree incline emphasizing upper (clavicular) chest fibers.
- **tips:** ["30-45 degree bench angle", "Bar to upper chest", "Elbows slightly wider than flat bench", "Do not arch excessively"]
- **pros:** ["Upper chest emphasis", "Anterior delt development", "Balances chest development"]
- **order_priority:** 2
- **superset_tags:** ["push_horizontal", "heavy_compound", "barbell"]
- **auto_program_min_level:** 1

### Dumbbell Bench Press
- **stableId:** `chest_dumbbell_bench_press`
- **description:** Dumbbell variation of flat bench press allowing greater range of motion and unilateral stability demand.
- **tips:** ["Dumbbells start at chest level", "Press up and slightly inward", "Lower until stretch felt in chest", "Keep wrists neutral"]
- **pros:** ["Greater chest stretch than barbell", "Unilateral balance requirement", "Safer to train to failure (can drop dumbbells)"]
- **order_priority:** 3
- **superset_tags:** ["push_horizontal", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 1

### Incline Dumbbell Press
- **stableId:** `chest_dumbbell_incline_dumbbell_press`
- **description:** Dumbbell press on incline emphasizing upper chest. Greater range of motion vs. incline barbell.
- **tips:** ["30-45 degree incline", "Dumbbells at upper chest", "Press straight up, not inward as much as flat press", "Full stretch at bottom"]
- **pros:** ["Upper chest isolation", "Unilateral stability challenge", "Anterior delt development"]
- **order_priority:** 4
- **superset_tags:** ["push_horizontal", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 1

### Decline Barbell Bench Press
- **stableId:** `chest_barbell_decline_bench_press`
- **description:** Barbell press on 15-30 degree decline emphasizing lower (costal) chest fibers.
- **tips:** ["15-30 degree decline, ankles secured", "Bar to lower chest", "Shorter range of motion than flat bench", "Blood pressure spike possible -- stop if dizzy"]
- **pros:** ["Lower chest emphasis", "Reduced shoulder stress vs. flat bench", "Tricep involvement"]
- **order_priority:** 5
- **superset_tags:** ["push_horizontal", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Dumbbell Fly
- **stableId:** `chest_dumbbell_fly`
- **description:** Single-joint horizontal adduction isolating pectoralis major with minimal tricep involvement.
- **tips:** ["Slight elbow bend maintained throughout", "Arc motion -- do not press", "Lower until stretch in chest, not shoulder pain", "Squeeze pecs at top"]
- **pros:** ["Pure chest isolation", "Stretch-mediated hypertrophy", "Anterior delt secondary stimulus"]
- **order_priority:** 6
- **superset_tags:** ["push_horizontal", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Cable Crossover
- **stableId:** `chest_cable_crossover`
- **description:** Cable-based horizontal adduction with constant tension throughout range. Highly versatile angle adjustment.
- **tips:** ["Cables set high, step forward for tension", "Slight forward lean", "Bring hands together in front of chest", "Control eccentric -- do not let cables snap back"]
- **pros:** ["Constant tension throughout rep", "Adjustable angle for upper/mid/lower chest", "Safe to train to failure"]
- **order_priority:** 7
- **superset_tags:** ["push_horizontal", "isolation", "cable"]
- **auto_program_min_level:** 1

### Machine Chest Press
- **stableId:** `chest_machine_machine_chest_press`
- **description:** Seated chest press on fixed path. Beginner-friendly heavy loading without stabilization demand.
- **tips:** ["Adjust seat so handles at mid-chest", "Press straight forward", "Full range of motion without lockout", "Controlled eccentric"]
- **pros:** ["Beginner-safe heavy loading", "Reduced technique barrier", "Easy progressive overload tracking"]
- **order_priority:** 8
- **superset_tags:** ["push_horizontal", "moderate_compound", "machine"]
- **auto_program_min_level:** 1

### Pec Deck / Machine Fly
- **stableId:** `chest_machine_pec_deck`
- **description:** Machine-based horizontal adduction isolating chest with guided path.
- **tips:** ["Adjust seat so handles at chest height", "Bring handles together in front of chest", "Squeeze and hold 1 second", "Do not hyperextend shoulders on eccentric"]
- **pros:** ["Pure chest isolation", "Safe near-failure training", "Constant tension"]
- **order_priority:** 9
- **superset_tags:** ["push_horizontal", "isolation", "machine"]
- **auto_program_min_level:** 1

### Push-Up
- **stableId:** `chest_bodyweight_push_up`
- **description:** Bodyweight horizontal press engaging chest, anterior delts, triceps, and core stabilizers. Highly versatile for home/travel.
- **tips:** ["Hands slightly wider than shoulders", "Plank position -- neutral spine", "Lower chest to floor", "Press through full range"]
- **pros:** ["No equipment required", "Core stability integration", "Scalable difficulty (incline/decline/weighted)"]
- **order_priority:** 10
- **superset_tags:** ["push_horizontal", "moderate_compound", "bodyweight"]
- **auto_program_min_level:** 1

### Dips (Chest-Focused)
- **stableId:** `chest_bodyweight_dips_chest_focused`
- **description:** Bodyweight vertical press with torso leaned forward emphasizing lower chest and triceps.
- **tips:** ["Lean torso forward 30 degrees", "Elbows flared slightly wider than tricep dips", "Lower until stretch in chest", "Press up without locking elbows"]
- **pros:** ["Lower chest and tricep overload", "Bodyweight strength benchmark", "Can add weight for progression"]
- **order_priority:** 11
- **superset_tags:** ["push_horizontal", "moderate_compound", "bodyweight"]
- **auto_program_min_level:** 2

---

## 4. Back (11 exercises)

### Barbell Bent-Over Row
- **stableId:** `back_barbell_bent_over_row`
- **description:** Hip-hinge barbell row with torso at 45-degree angle. Pulls from below knee to lower chest.
- **tips:** ["Keep knees slightly bent and hips back", "Pull to lower chest, not waist", "Maintain neutral spine throughout", "Drive elbows back, not just hands"]
- **pros:** ["Builds lat width and mid-back thickness", "High carryover to deadlift lockout strength", "Trains hip hinge pattern under load"]
- **order_priority:** 3
- **superset_tags:** ["pull_horizontal", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Pull-Up
- **stableId:** `back_bodyweight_pull_up`
- **description:** Overhand grip vertical pull from dead hang to chin over bar. Lat-dominant bodyweight compound.
- **tips:** ["Start from dead hang with arms fully extended", "Pull chest to bar, not chin only", "Engage lats before bending elbows", "Control the descent -- do not drop"]
- **pros:** ["Excellent lat width and thickness builder", "Scalable via band assistance or added weight", "Minimal equipment required"]
- **order_priority:** 2
- **superset_tags:** ["pull_vertical", "moderate_compound", "bodyweight"]
- **auto_program_min_level:** 2

### Lat Pulldown
- **stableId:** `back_cable_lat_pulldown`
- **description:** Seated vertical pull on cable machine. Accessible alternative to pull-ups for beginners. Prime mover: lats with bicep and rear delt synergy.
- **tips:** ["Pull bar to upper chest, not behind neck", "Lean back slightly (10-15 degrees)", "Focus on pulling elbows down, not hands", "Full stretch at top with scapular elevation"]
- **pros:** ["Easier to learn than pull-ups", "Allows precise load control", "Effective for lat width development"]
- **order_priority:** 1
- **superset_tags:** ["pull_vertical", "moderate_compound", "cable"]
- **auto_program_min_level:** 1

### Seated Cable Row
- **stableId:** `back_cable_seated_row`
- **description:** Horizontal cable pull to torso. Targets mid-back (rhomboids, mid-traps) and lats.
- **tips:** ["Sit upright with chest out", "Initiate pull by retracting scapulae", "Pull to lower chest or upper abdomen", "Do not rock torso -- isolate upper back"]
- **pros:** ["Builds mid-back thickness", "Minimal lower back fatigue compared to barbell rows", "Excellent for scapular retraction strength"]
- **order_priority:** 1
- **superset_tags:** ["pull_horizontal", "moderate_compound", "cable"]
- **auto_program_min_level:** 1

### Dumbbell Single-Arm Row
- **stableId:** `back_dumbbell_single_arm_row`
- **description:** Unilateral horizontal pull with one hand on bench for support. Allows extended range of motion and core stabilization demand.
- **tips:** ["Keep bench-side arm locked for stability", "Pull dumbbell to hip, not chest", "Rotate torso slightly at top for maximal lat contraction", "Avoid excessive hip rotation"]
- **pros:** ["Corrects left-right strength imbalances", "Greater range of motion than barbell row", "Trains anti-rotational core stability"]
- **order_priority:** 2
- **superset_tags:** ["pull_horizontal", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 1

### T-Bar Row
- **stableId:** `back_barbell_t_bar_row`
- **description:** Barbell anchored at one end, pulled to chest from standing hip-hinge position.
- **tips:** ["Straddle the bar with feet shoulder-width", "Pull bar to sternum, not waist", "Keep elbows close to torso", "Maintain neutral spine throughout"]
- **pros:** ["Easier to learn than bent-over row", "Allows heavy loading with lower injury risk", "Builds mid-back thickness effectively"]
- **order_priority:** 3
- **superset_tags:** ["pull_horizontal", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Chest-Supported Row
- **stableId:** `back_machine_chest_supported_row`
- **description:** Machine row with chest pad eliminating lower back involvement. Isolates lats, rhomboids, and rear delts without spinal loading.
- **tips:** ["Adjust pad so chest is fully supported", "Pull handles to ribs, not chest", "Squeeze scapulae together at peak contraction", "Do not use momentum -- control the eccentric"]
- **pros:** ["Zero lower back fatigue -- ideal after deadlifts or squats", "Allows heavy loading safely", "Excellent for hypertrophy with high reps"]
- **order_priority:** 1
- **superset_tags:** ["pull_horizontal", "moderate_compound", "machine"]
- **auto_program_min_level:** 1

### Chin-Up
- **stableId:** `back_bodyweight_chin_up`
- **description:** Supinated (underhand) grip vertical pull from dead hang. Higher bicep recruitment than pull-ups due to grip orientation.
- **tips:** ["Use full supinated grip (palms facing you)", "Pull chest to bar, not just chin over", "Engage biceps and lats simultaneously", "Lower with control to full arm extension"]
- **pros:** ["Builds bicep strength alongside lats", "Easier for beginners than pull-ups due to bicep involvement", "Scalable via band assistance or added weight"]
- **order_priority:** 2
- **superset_tags:** ["pull_vertical", "moderate_compound", "bodyweight"]
- **auto_program_min_level:** 2

### Straight-Arm Pulldown
- **stableId:** `back_cable_straight_arm_pulldown`
- **description:** Cable pulldown with elbows locked. Isolates lats by removing bicep contribution. Movement is pure shoulder extension.
- **tips:** ["Keep elbows locked throughout (slight bend acceptable)", "Pull bar from overhead to thighs in arc motion", "Lean forward slightly at waist", "Focus on contracting lats, not pulling with arms"]
- **pros:** ["Isolates lats without bicep fatigue", "Excellent for mind-muscle connection", "Useful finisher after heavy rows/pull-ups"]
- **order_priority:** 10
- **superset_tags:** ["pull_vertical", "isolation", "cable"]
- **auto_program_min_level:** 1

### Face Pull
- **stableId:** `back_cable_face_pull`
- **description:** Cable pull to face with rope attachment. Targets rear delts, mid-traps, and rotator cuff. Critical for shoulder health.
- **tips:** ["Pull rope to eye level, not chest", "Externally rotate shoulders at peak (hands apart)", "Keep elbows high throughout", "Use light-moderate weight -- this is not a heavy row"]
- **pros:** ["Strengthens rear delts and rotator cuff", "Improves shoulder health and posture", "Corrects anterior-dominant pressing imbalances"]
- **order_priority:** 11
- **superset_tags:** ["delt_rear", "isolation", "cable"]
- **auto_program_min_level:** 1

### Cable Lat Pullover
- **stableId:** `back_cable_lat_pullover`
- **description:** Cable pullover from overhead to waist with arms extended. Isolates lats through shoulder extension.
- **tips:** ["Stand facing away from cable stack", "Pull cable from overhead to thighs in arc", "Maintain slight elbow bend throughout", "Lean forward at hips for greater lat stretch"]
- **pros:** ["Isolates lats with minimal bicep involvement", "Excellent lat stretch at top position", "Trains shoulder extension strength"]
- **order_priority:** 12
- **superset_tags:** ["pull_vertical", "isolation", "cable"]
- **auto_program_min_level:** 2

---

## 5. Shoulders (11 exercises)

### Overhead Press (Barbell)
- **stableId:** `shoulders_barbell_overhead_press`
- **description:** Standing barbell press from shoulders to lockout overhead. Trains anterior and lateral delts with high core demand.
- **tips:** ["Stand with feet shoulder-width, core braced", "Press bar in straight vertical line past face", "Lock elbows fully at top", "Do not lean back excessively -- maintain neutral spine"]
- **pros:** ["Builds total shoulder strength and size", "High carryover to athletic performance", "Trains core stability under load"]
- **order_priority:** 1
- **superset_tags:** ["push_vertical", "heavy_compound", "barbell"]
- **auto_program_min_level:** 2

### Dumbbell Shoulder Press
- **stableId:** `shoulders_dumbbell_shoulder_press`
- **description:** Seated or standing dumbbell press. Allows greater range of motion than barbell and trains unilateral stability.
- **tips:** ["Start with dumbbells at shoulder height, palms forward", "Press dumbbells up and slightly inward", "Avoid clanging dumbbells at top", "Lower with control to shoulder level"]
- **pros:** ["Greater range of motion than barbell", "Corrects left-right imbalances", "Easier on wrists and shoulders for some lifters"]
- **order_priority:** 1
- **superset_tags:** ["push_vertical", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 1

### Landmine Press
- **stableId:** `shoulders_barbell_landmine_press`
- **description:** Barbell anchored at floor, pressed at angle overhead. Reduces shoulder stress compared to vertical pressing.
- **tips:** ["Stand facing away from anchor point", "Press bar from shoulder to overhead at 45-degree angle", "Keep core tight throughout", "Do not overextend lower back at top"]
- **pros:** ["Lower shoulder impingement risk than vertical press", "Trains core anti-rotation (single-arm variation)", "Ideal for those with shoulder mobility limitations"]
- **order_priority:** 2
- **superset_tags:** ["push_vertical", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Arnold Press
- **stableId:** `shoulders_dumbbell_arnold_press`
- **description:** Dumbbell press with rotation from supinated to pronated grip. Emphasizes anterior and lateral delts through extended ROM.
- **tips:** ["Start with palms facing you, dumbbells at shoulder height", "Press while rotating palms forward", "Finish with palms facing forward, arms locked", "Reverse rotation on descent"]
- **pros:** ["Extended range of motion hits all three delt heads", "Builds shoulder mobility and strength simultaneously", "Popularized by Arnold Schwarzenegger for hypertrophy"]
- **order_priority:** 2
- **superset_tags:** ["push_vertical", "moderate_compound", "dumbbell"]
- **auto_program_min_level:** 2

### Barbell Upright Row
- **stableId:** `shoulders_barbell_upright_row`
- **description:** Barbell pull from thighs to chest. Targets lateral delts and traps. Moderate impingement risk -- pull to chest, not chin.
- **tips:** ["Grip slightly wider than shoulder width", "Pull to chest height only (not chin)", "Lead with elbows, not hands", "Stop immediately if shoulder pain occurs"]
- **pros:** ["Builds lateral delts and upper traps", "Allows heavy loading for strength", "Effective for shoulder mass"]
- **order_priority:** 3
- **superset_tags:** ["delt_lateral", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Lateral Raise
- **stableId:** `shoulders_dumbbell_lateral_raise`
- **description:** Dumbbell raise to side with arms extended. Isolates lateral deltoid. Primary exercise for shoulder width.
- **tips:** ["Slight forward lean, dumbbells at sides", "Raise dumbbells to shoulder height (not higher)", "Lead with elbows, not hands", "Control the descent -- do not drop"]
- **pros:** ["Best isolation exercise for lateral delt width", "Low injury risk when performed correctly", "Easily adjusted for drop sets and high-rep finishers"]
- **order_priority:** 5
- **superset_tags:** ["delt_lateral", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Cable Lateral Raise
- **stableId:** `shoulders_cable_lateral_raise`
- **description:** Cable lateral raise with D-handle. Provides constant tension throughout range of motion.
- **tips:** ["Stand perpendicular to cable stack", "Raise handle to shoulder height", "Maintain tension at bottom (do not rest)", "Control the negative -- resist cable tension"]
- **pros:** ["Constant tension on lateral delts", "No dead zone at bottom like dumbbells", "Allows precise load increments"]
- **order_priority:** 6
- **superset_tags:** ["delt_lateral", "isolation", "cable"]
- **auto_program_min_level:** 1

### Front Raise
- **stableId:** `shoulders_dumbbell_front_raise`
- **description:** Dumbbell raise to front with arms extended. Targets anterior deltoid.
- **tips:** ["Start with dumbbells at thighs", "Raise to shoulder height, not overhead", "Use controlled tempo -- no swinging", "Palms can face down or toward each other"]
- **pros:** ["Isolates anterior deltoid", "Useful for bodybuilding-style shoulder training", "Can be performed alternating or simultaneously"]
- **order_priority:** 7
- **superset_tags:** ["push_vertical", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Reverse Fly (Dumbbell)
- **stableId:** `shoulders_dumbbell_reverse_fly`
- **description:** Bent-over fly targeting posterior deltoid. Performed with torso parallel to floor, dumbbells raised to sides.
- **tips:** ["Hinge at hips, torso near parallel to floor", "Raise dumbbells to sides with slight elbow bend", "Squeeze shoulder blades together at top", "Keep neck neutral -- do not look up"]
- **pros:** ["Isolates posterior deltoid and rhomboids", "Improves shoulder health and posture", "Corrects imbalances from pressing-heavy programs"]
- **order_priority:** 8
- **superset_tags:** ["delt_rear", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Barbell Shrug
- **stableId:** `shoulders_barbell_shrug`
- **description:** Barbell held at thighs, shoulders elevated maximally. Isolates upper trapezius.
- **tips:** ["Hold bar at thighs with arms straight", "Elevate shoulders straight up (not roll)", "Pause at top for 1-2 seconds", "Lower with control"]
- **pros:** ["Builds upper trap mass and strength", "Simple movement with low injury risk", "Allows very heavy loading"]
- **order_priority:** 9
- **superset_tags:** ["delt_lateral", "isolation", "barbell"]
- **auto_program_min_level:** 1

### Dumbbell Shrug
- **stableId:** `shoulders_dumbbell_shrug`
- **description:** Dumbbell version of shrug. Dumbbells at sides, shoulders elevated maximally. Greater range of motion than barbell.
- **tips:** ["Hold dumbbells at sides with arms straight", "Elevate shoulders as high as possible", "Do not roll shoulders -- straight up and down", "Pause at peak contraction"]
- **pros:** ["Greater range of motion than barbell", "Easier grip for heavy loads", "Corrects left-right imbalances"]
- **order_priority:** 10
- **superset_tags:** ["delt_lateral", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

---

## 6. Arms (12 exercises)

### Close-Grip Bench Press
- **stableId:** `arms_barbell_close_grip_bench_press`
- **description:** Barbell bench press with hands inside shoulder width. Shifts emphasis from chest to triceps. Only compound exercise in Arms group.
- **tips:** ["Grip barbell hands shoulder-width or slightly narrower", "Lower bar to lower chest", "Keep elbows closer to torso than regular bench", "Lock out fully at top"]
- **pros:** ["Allows heaviest loading for triceps", "Builds tricep strength with chest synergy", "Excellent for powerlifting lockout strength"]
- **order_priority:** 1
- **superset_tags:** ["arm_push", "moderate_compound", "barbell"]
- **auto_program_min_level:** 2

### Tricep Pushdown (Cable)
- **stableId:** `arms_cable_tricep_pushdown`
- **description:** Cable pushdown with rope, V-bar, or straight bar. Most popular tricep isolation exercise.
- **tips:** ["Stand facing cable stack, elbows at sides", "Push attachment down to full elbow extension", "Keep elbows stationary -- do not flare", "Control the return -- resist cable tension"]
- **pros:** ["Easy to learn and execute", "Constant tension throughout movement", "Low injury risk"]
- **order_priority:** 3
- **superset_tags:** ["arm_push", "isolation", "cable"]
- **auto_program_min_level:** 1

### Skull Crusher (Lying Tricep Extension)
- **stableId:** `arms_ez_bar_skull_crusher`
- **description:** Lying tricep extension with EZ bar lowered to forehead. Isolates triceps through elbow extension. High elbow stress -- use EZ bar.
- **tips:** ["Lie on bench, bar held above chest with arms vertical", "Lower bar to forehead by bending elbows only", "Keep upper arms stationary (perpendicular to floor)", "Press back to lockout"]
- **pros:** ["Excellent tricep isolation and stretch", "Builds tricep long head mass", "Allows moderate-heavy loading"]
- **order_priority:** 4
- **superset_tags:** ["arm_push", "isolation", "ez_bar"]
- **auto_program_min_level:** 2

### Overhead Tricep Extension
- **stableId:** `arms_dumbbell_overhead_tricep_extension`
- **description:** Dumbbell held overhead, lowered behind head via elbow flexion. Maximally stretches tricep long head.
- **tips:** ["Hold single dumbbell overhead with both hands", "Lower behind head by bending elbows", "Keep elbows pointing forward (do not flare)", "Extend to lockout overhead"]
- **pros:** ["Maximally stretches tricep long head", "Can be performed seated or standing", "Builds tricep mass effectively"]
- **order_priority:** 4
- **superset_tags:** ["arm_push", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Barbell Curl
- **stableId:** `arms_barbell_curl`
- **description:** Standing barbell curl from thighs to shoulders. Classic bicep mass builder. Allows heaviest loading of all curl variations.
- **tips:** ["Stand with feet shoulder-width, elbows at sides", "Curl bar to shoulders without moving elbows forward", "Do not swing hips -- isolate biceps", "Lower with control to full elbow extension"]
- **pros:** ["Allows heaviest weight of any bicep exercise", "Builds bicep mass and strength effectively", "Simple movement pattern"]
- **order_priority:** 5
- **superset_tags:** ["arm_pull", "isolation", "barbell"]
- **auto_program_min_level:** 1

### Dumbbell Curl
- **stableId:** `arms_dumbbell_curl`
- **description:** Standing or seated dumbbell curl. Can be performed simultaneously or alternating. Allows supination throughout range of motion.
- **tips:** ["Start with arms fully extended, palms forward or neutral", "Curl dumbbells to shoulders", "Supinate wrists as you curl (rotate palms up)", "Keep elbows stationary"]
- **pros:** ["Corrects left-right imbalances", "Allows full supination for bicep peak contraction", "Can be performed seated to eliminate momentum"]
- **order_priority:** 5
- **superset_tags:** ["arm_pull", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Hammer Curl
- **stableId:** `arms_dumbbell_hammer_curl`
- **description:** Dumbbell curl with neutral grip (palms facing each other). Targets brachialis and brachioradialis more than standard curls.
- **tips:** ["Hold dumbbells with palms facing each other throughout", "Curl to shoulders without rotating wrists", "Keep elbows tight to torso", "Lower with control"]
- **pros:** ["Builds brachialis for arm thickness", "Trains forearm strength simultaneously", "Easier on wrists than supinated curls"]
- **order_priority:** 6
- **superset_tags:** ["arm_pull", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Cable Curl
- **stableId:** `arms_cable_curl`
- **description:** Cable curl with straight bar or EZ bar attachment. Provides constant tension throughout range of motion.
- **tips:** ["Stand facing cable stack, bar at thighs", "Curl bar to shoulders keeping elbows stationary", "Maintain tension at bottom (do not rest)", "Control the negative against cable resistance"]
- **pros:** ["Constant tension on biceps (no dead zones)", "Allows precise load adjustments", "Excellent for high-rep pump work"]
- **order_priority:** 6
- **superset_tags:** ["arm_pull", "isolation", "cable"]
- **auto_program_min_level:** 1

### Preacher Curl
- **stableId:** `arms_ez_bar_preacher_curl`
- **description:** EZ bar curl with upper arms supported on preacher bench. Isolates biceps by eliminating shoulder involvement.
- **tips:** ["Position arms on pad with armpits at top edge", "Lower bar to near-full extension (do not hyperextend)", "Curl to top without lifting elbows off pad", "Use EZ bar to reduce wrist strain"]
- **pros:** ["Strict isolation with no momentum", "Emphasizes bicep short head and peak", "Reduces cheating and improves mind-muscle connection"]
- **order_priority:** 7
- **superset_tags:** ["arm_pull", "isolation", "ez_bar"]
- **auto_program_min_level:** 1

### Incline Dumbbell Curl
- **stableId:** `arms_dumbbell_incline_curl`
- **description:** Dumbbell curl performed on incline bench (45-60 degrees). Extends shoulders behind torso, maximally stretching bicep long head.
- **tips:** ["Set bench to 45-60 degree incline", "Let arms hang straight down with shoulders extended", "Curl without moving upper arms forward", "Emphasize the stretch at bottom"]
- **pros:** ["Maximally stretches bicep long head", "Eliminates momentum -- strict isolation", "Builds bicep peak"]
- **order_priority:** 8
- **superset_tags:** ["arm_pull", "isolation", "dumbbell"]
- **auto_program_min_level:** 2

### Concentration Curl
- **stableId:** `arms_dumbbell_concentration_curl`
- **description:** Seated single-arm curl with elbow braced against inner thigh. Strict isolation with no momentum.
- **tips:** ["Sit on bench, brace elbow against inner thigh", "Curl dumbbell to shoulder with full supination", "Keep upper arm stationary throughout", "Squeeze bicep at peak contraction"]
- **pros:** ["Strictest bicep isolation -- no cheating possible", "Emphasizes bicep peak", "Excellent for mind-muscle connection"]
- **order_priority:** 8
- **superset_tags:** ["arm_pull", "isolation", "dumbbell"]
- **auto_program_min_level:** 1

### Wrist Curl
- **stableId:** `arms_barbell_wrist_curl`
- **description:** Barbell wrist flexion with forearms supported on bench. Isolates forearm flexors.
- **tips:** ["Sit with forearms on bench, wrists hanging off edge", "Curl bar up via wrist flexion only", "Lower with control to full wrist extension", "Use light weight -- forearms fatigue quickly"]
- **pros:** ["Builds forearm size and grip strength", "Improves grip endurance for deadlifts and rows", "Simple and safe movement"]
- **order_priority:** 12
- **superset_tags:** ["forearm", "isolation", "barbell"]
- **auto_program_min_level:** 1

---

## 7. Core (11 exercises)

### Plank
- **stableId:** `core_bodyweight_plank`
- **description:** Front plank on forearms and toes. Isometric hold. Trains rectus abdominis and transverse abdominis in anti-extension pattern.
- **tips:** ["Maintain straight line from head to heels", "Engage glutes and brace core", "Do not let hips sag or pike", "Breathe normally -- do not hold breath"]
- **pros:** ["Safest core exercise for spine (isometric, no flexion)", "Builds anterior core endurance", "Accessible to all levels"]
- **order_priority:** 1
- **superset_tags:** ["core_anti_flexion", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Dead Bug
- **stableId:** `core_bodyweight_dead_bug`
- **description:** Lying on back, alternating arm and leg extension while maintaining lower back contact with floor. Anti-extension and stabilization exercise.
- **tips:** ["Lie on back, arms vertical, knees bent at 90 degrees", "Extend opposite arm and leg while keeping lower back flat", "Do not let lower back arch off floor", "Move slowly and deliberately"]
- **pros:** ["Safest core exercise (spine neutral)", "Trains core stabilization and coordination", "Excellent for beginners and rehab"]
- **order_priority:** 1
- **superset_tags:** ["core_anti_flexion", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Side Plank
- **stableId:** `core_bodyweight_side_plank`
- **description:** Plank on one forearm and side of foot. Isometric hold. Trains obliques and transverse abdominis in anti-lateral flexion pattern.
- **tips:** ["Lie on side, prop up on forearm, feet stacked", "Maintain straight line from head to feet", "Do not let hips sag", "Engage obliques and glutes throughout"]
- **pros:** ["Best oblique isolation exercise (isometric)", "Improves lateral core stability", "Low injury risk"]
- **order_priority:** 2
- **superset_tags:** ["core_rotation", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Ab Wheel Rollout
- **stableId:** `core_bodyweight_ab_wheel_rollout`
- **description:** Ab wheel rollout from knees or toes. Extreme anti-extension demand. Advanced core exercise requiring high baseline strength.
- **tips:** ["Start on knees (beginners) or toes (advanced)", "Roll wheel forward while maintaining posterior pelvic tilt", "Do not let lower back arch", "Roll out only as far as you can maintain neutral spine"]
- **pros:** ["Builds extreme core strength", "Trains lats, shoulders, and hip flexors simultaneously", "Functional carryover to athletic performance"]
- **order_priority:** 2
- **superset_tags:** ["core_anti_flexion", "compound", "bodyweight"]
- **auto_program_min_level:** 3

### Hanging Leg Raise
- **stableId:** `core_bodyweight_hanging_leg_raise`
- **description:** Hang from pull-up bar, raise legs to horizontal or higher. Trains rectus abdominis and hip flexors.
- **tips:** ["Hang from bar with overhand grip", "Raise legs to horizontal (or knees to chest for regression)", "Control the descent -- do not swing", "Engage core before initiating movement"]
- **pros:** ["Builds lower ab and hip flexor strength", "Trains grip endurance simultaneously", "Scalable from knee raises to toes-to-bar"]
- **order_priority:** 3
- **superset_tags:** ["core_flexion", "compound", "bodyweight"]
- **auto_program_min_level:** 2

### Dragon Flag
- **stableId:** `core_bodyweight_dragon_flag`
- **description:** Lying on bench, grip behind head, raise entire body to vertical via core and hip flexion. Extreme difficulty. Requires advanced baseline strength.
- **tips:** ["Lie on bench, grip bench behind head", "Raise legs and torso to vertical (shoulders remain on bench)", "Lower with extreme control -- do not collapse", "Only perform if you can complete 3+ strict reps"]
- **pros:** ["Extreme core and hip flexor strength builder", "Impressive display of control and strength", "Trains entire anterior chain"]
- **order_priority:** 3
- **superset_tags:** ["core_flexion", "compound", "bodyweight"]
- **auto_program_min_level:** 99

### Cable Crunch
- **stableId:** `core_cable_crunch`
- **description:** Kneeling cable crunch with rope attachment. Spinal flexion under load. Isolates rectus abdominis.
- **tips:** ["Kneel facing cable stack, rope behind head", "Crunch down by flexing spine (not hips)", "Keep hips stationary throughout", "Control the eccentric -- resist cable tension"]
- **pros:** ["Allows progressive overload via cable stack", "Isolates abs with minimal hip flexor involvement", "Easy to learn and execute"]
- **order_priority:** 4
- **superset_tags:** ["core_flexion", "isolation", "cable"]
- **auto_program_min_level:** 1

### Pallof Press
- **stableId:** `core_cable_pallof_press`
- **description:** Cable anti-rotation press. Stand perpendicular to cable, press handle away from chest while resisting rotation.
- **tips:** ["Stand perpendicular to cable stack, handle at chest", "Press handle straight out without rotating torso", "Hold for 2-3 seconds, then return to chest", "Keep core braced throughout"]
- **pros:** ["Best anti-rotation core exercise", "Functional carryover to sports and daily life", "Low injury risk"]
- **order_priority:** 5
- **superset_tags:** ["core_rotation", "isolation", "cable"]
- **auto_program_min_level:** 2

### Decline Sit-Up
- **stableId:** `core_bodyweight_decline_sit_up`
- **description:** Sit-up performed on decline bench. Increases resistance compared to flat sit-ups.
- **tips:** ["Set bench to 15-30 degree decline", "Anchor feet at top of bench", "Sit up to vertical, lower with control", "Do not pull on neck -- hands behind head lightly"]
- **pros:** ["Progressive overload via bench angle", "Builds ab strength and endurance", "Can be weighted with plate on chest"]
- **order_priority:** 5
- **superset_tags:** ["core_flexion", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Bicycle Crunch
- **stableId:** `core_bodyweight_bicycle_crunch`
- **description:** Alternating elbow-to-knee crunch with legs cycling. Trains rectus abdominis and obliques dynamically.
- **tips:** ["Lie on back, hands behind head", "Bring opposite elbow to opposite knee while extending other leg", "Rotate torso, do not just move arms", "Maintain controlled tempo -- quality over speed"]
- **pros:** ["High oblique activation", "No equipment needed", "Combines flexion and rotation"]
- **order_priority:** 6
- **superset_tags:** ["core_flexion", "isolation", "bodyweight"]
- **auto_program_min_level:** 1

### Russian Twist
- **stableId:** `core_bodyweight_russian_twist`
- **description:** Seated rotation with feet elevated. Trains obliques and transverse abdominis.
- **tips:** ["Sit with knees bent, feet elevated off floor", "Rotate torso side to side, touching floor beside hips", "Keep core engaged throughout", "Use light weight or bodyweight only"]
- **pros:** ["Builds rotational core strength", "Trains obliques effectively", "Can be performed with or without weight"]
- **order_priority:** 7
- **superset_tags:** ["core_rotation", "isolation", "bodyweight"]
- **auto_program_min_level:** 1
