@file:Suppress("MaxLineLength", "LongMethod", "LargeClass")

package com.deepreps.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room callback that seeds the database on first creation with:
 * - 7 muscle groups
 * - 78 exercises with all metadata
 * - exercise_muscles junction table entries (primary + secondary)
 *
 * All data sourced from exercise-science.md and exercise-metadata-supplement.md.
 * This callback is additive-only -- it never deletes existing data.
 */
internal class PrepopulateCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.beginTransaction()
        try {
            insertMuscleGroups(db)
            insertExercises(db)
            insertExerciseMuscles(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ===========================================================================================
    // Muscle Groups
    // ===========================================================================================

    private fun insertMuscleGroups(db: SupportSQLiteDatabase) {
        val groups = listOf(
            Triple(1L, "Legs", 1),
            Triple(2L, "Lower Back", 2),
            Triple(3L, "Chest", 3),
            Triple(4L, "Back", 4),
            Triple(5L, "Shoulders", 5),
            Triple(6L, "Arms", 6),
            Triple(7L, "Core", 7),
        )
        for ((id, name, order) in groups) {
            db.execSQL(
                "INSERT INTO muscle_groups (id, name, display_order) VALUES (?, ?, ?)",
                arrayOf(id, name, order),
            )
        }
    }

    // ===========================================================================================
    // Exercises
    // ===========================================================================================

    private fun insertExercises(db: SupportSQLiteDatabase) {
        insertLegsExercises(db)
        insertLowerBackExercises(db)
        insertChestExercises(db)
        insertBackExercises(db)
        insertShouldersExercises(db)
        insertArmsExercises(db)
        insertCoreExercises(db)
    }

    @Suppress("LongParameterList")
    private fun insertExercise(
        db: SupportSQLiteDatabase,
        id: Long,
        stableId: String,
        name: String,
        description: String,
        equipment: String,
        movementType: String,
        difficulty: String,
        primaryGroupId: Long,
        secondaryMuscles: String,
        tips: String,
        pros: String,
        displayOrder: Int,
        orderPriority: Int,
        supersetTags: String,
        autoProgramMinLevel: Int,
    ) {
        db.execSQL(
            """INSERT INTO exercises (
                id, stable_id, name, description, equipment, movement_type, difficulty,
                primary_group_id, secondary_muscles, tips, pros, display_order,
                order_priority, superset_tags, auto_program_min_level
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)""",
            arrayOf(
                id, stableId, name, description, equipment, movementType, difficulty,
                primaryGroupId, secondaryMuscles, tips, pros, displayOrder,
                orderPriority, supersetTags, autoProgramMinLevel,
            ),
        )
    }

    // --- Legs (Group 1, IDs 1-12) ---

    private fun insertLegsExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 1, "legs_barbell_back_squat", "Barbell Back Squat",
            "The foundational lower body compound movement. Quadriceps are the prime mover through full range of motion, with significant glute and core engagement.",
            "barbell", "compound", "beginner", 1,
            """["Glutes","lower back","core"]""",
            """["Bar on upper traps, not neck","Drive knees out over toes","Break at hips and knees simultaneously","Chest up, neutral spine throughout"]""",
            """["Highest lower body muscle recruitment","Functional strength carryover","Core stability development"]""",
            1, 1, """["quad_dominant","heavy_compound","barbell"]""", 1
        )

        insertExercise(
            db, 2, "legs_barbell_front_squat", "Barbell Front Squat",
            "Front-loaded squat variation emphasizing quad recruitment and demanding significant upper back and core stability. More vertical torso than back squat.",
            "barbell", "compound", "intermediate", 1,
            """["Core","upper back","glutes"]""",
            """["Elbows high, bar rests on front delts","Vertical torso -- do not lean forward","Core braced to prevent forward collapse","Can use cross-grip if wrist mobility limited"]""",
            """["Greater quad isolation than back squat","Reduced spinal compression","Core stabilization challenge"]""",
            2, 2, """["quad_dominant","heavy_compound","barbell"]""", 2
        )

        insertExercise(
            db, 3, "legs_machine_leg_press", "Leg Press",
            "Machine-based compound allowing heavy loading with reduced stabilization demand. Targets quads, glutes, and hamstrings.",
            "machine", "compound", "beginner", 1,
            """["Glutes","hamstrings"]""",
            """["Feet shoulder-width, mid-platform","Lower to 90 degrees knee flexion","Do not allow lower back to round off the pad","Press through full foot, not just toes"]""",
            """["Beginner-friendly heavy loading","Reduced technique barrier vs. squats","Adjustable foot position for emphasis shifts"]""",
            3, 3, """["quad_dominant","moderate_compound","machine"]""", 1
        )

        insertExercise(
            db, 4, "legs_barbell_rdl", "Romanian Deadlift (RDL)",
            "Hip hinge with soft knees isolating hamstrings as the prime mover. Glutes and lower back provide synergistic support.",
            "barbell", "compound", "intermediate", 1,
            """["Lower back","glutes"]""",
            """["Soft knee bend -- not a stiff leg","Push hips back, not down","Bar tracks close to shins and thighs","Stretch hamstrings at bottom, contract glutes at top"]""",
            """["Hamstring hypertrophy emphasis","Hip hinge pattern reinforcement","Minimal lower back fatigue vs. conventional deadlift"]""",
            4, 4, """["hip_hinge","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 5, "legs_dumbbell_walking_lunge", "Walking Lunges",
            "Unilateral lower body compound. High glute activation and balance demand.",
            "dumbbell", "compound", "beginner", 1,
            """["Glutes","core","calves"]""",
            """["Step forward into lunge, do not step back","Back knee hovers just above floor","Torso upright throughout","Push through front heel to stand"]""",
            """["Unilateral balance and stability","Glute activation higher than bilateral squats","Functional movement pattern"]""",
            5, 5, """["quad_dominant","moderate_compound","dumbbell"]""", 1
        )

        insertExercise(
            db, 6, "legs_dumbbell_bulgarian_split_squat", "Bulgarian Split Squat",
            "Elevated rear foot split squat creating extreme quad and glute demand on the front leg. Unilateral balance challenge.",
            "dumbbell", "compound", "intermediate", 1,
            """["Glutes","core"]""",
            """["Rear foot on bench 12-18 inches high","Front shin vertical at bottom","Do not allow front knee to collapse inward","Torso slight forward lean is acceptable"]""",
            """["Unilateral strength development","Lower spinal load than bilateral squats","Glute hypertrophy stimulus"]""",
            6, 6, """["quad_dominant","moderate_compound","dumbbell"]""", 2
        )

        insertExercise(
            db, 7, "legs_machine_hack_squat", "Hack Squat",
            "Machine squat with angled back support isolating quads with minimal lower back involvement.",
            "machine", "compound", "beginner", 1,
            """["Glutes"]""",
            """["Shoulders under pads, back flat against support","Feet shoulder-width, mid-platform","Lower until thighs parallel to platform","Drive through heels"]""",
            """["Quad isolation without spinal loading","Beginner-friendly fixed path","Safe near-failure training"]""",
            7, 7, """["quad_dominant","moderate_compound","machine"]""", 1
        )

        insertExercise(
            db, 8, "legs_machine_leg_extension", "Leg Extension",
            "Single-joint knee extension isolating the quadriceps. No hip or lower back involvement.",
            "machine", "isolation", "beginner", 1,
            """[]""",
            """["Adjust seat so knees align with machine pivot","Controlled eccentric -- do not drop weight","Full extension with 1-second squeeze","Do not hyperextend knee"]""",
            """["Pure quad isolation","Safe for knee rehabilitation (when loaded appropriately)","Easy to track progressive overload"]""",
            8, 8, """["quad_dominant","isolation","machine"]""", 1
        )

        insertExercise(
            db, 9, "legs_machine_lying_leg_curl", "Lying Leg Curl",
            "Prone hamstring isolation with glute synergy. Single-joint knee flexion.",
            "machine", "isolation", "beginner", 1,
            """["Glutes"]""",
            """["Hips stay down on pad -- do not lift","Curl heels to glutes","Control the eccentric phase","Feet neutral or slightly plantarflexed"]""",
            """["Hamstring hypertrophy isolation","Reduced lower back involvement vs. RDL","Safe to train near failure"]""",
            9, 9, """["hip_hinge","isolation","machine"]""", 1
        )

        insertExercise(
            db, 10, "legs_machine_seated_leg_curl", "Seated Leg Curl",
            "Seated hamstring isolation with reduced glute involvement compared to lying variation.",
            "machine", "isolation", "beginner", 1,
            """[]""",
            """["Adjust thigh pad to secure legs","Pull heels under seat","1-second contraction at peak","Avoid rocking torso forward"]""",
            """["Hamstring isolation without hip extension","Comfortable setup for high-rep sets","Lower back stays neutral"]""",
            10, 10, """["hip_hinge","isolation","machine"]""", 1
        )

        insertExercise(
            db, 11, "legs_machine_standing_calf_raise", "Standing Calf Raise",
            "Straight-leg calf raise emphasizing gastrocnemius (visible calf muscle).",
            "machine", "isolation", "beginner", 1,
            """[]""",
            """["Full stretch at bottom -- heels below toes","Drive through balls of feet","1-second contraction at top","Knees locked throughout"]""",
            """["Gastrocnemius hypertrophy","High load capacity","Simple execution"]""",
            11, 11, """["calf","isolation","machine"]""", 1
        )

        insertExercise(
            db, 12, "legs_machine_seated_calf_raise", "Seated Calf Raise",
            "Bent-knee calf raise emphasizing soleus (deeper calf muscle under gastrocnemius).",
            "machine", "isolation", "beginner", 1,
            """[]""",
            """["Knees bent 90 degrees","Full stretch at bottom","Controlled tempo -- no bouncing","Toes pointed straight ahead"]""",
            """["Soleus isolation","Complements standing calf raises","Lower weight allows higher reps"]""",
            12, 12, """["calf","isolation","machine"]""", 1
        )
    }

    // --- Lower Back (Group 2, IDs 13-22) ---

    private fun insertLowerBackExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 13, "lower_back_barbell_conventional_deadlift", "Conventional Deadlift",
            "The king of posterior chain compounds. Erector spinae are the primary stabilizer and force transmitter -- the movement fails when the lower back rounds.",
            "barbell", "compound", "intermediate", 2,
            """["Glutes","hamstrings","traps","forearms"]""",
            """["Bar over mid-foot, shins vertical","Pull slack out before initiating lift","Neutral spine -- no rounding or hyperextension","Drive through floor with legs, lock hips and knees simultaneously"]""",
            """["Highest total-body muscle recruitment","Posterior chain strength and hypertrophy","Functional carryover to all lifting"]""",
            1, 1, """["hip_hinge","heavy_compound","barbell"]""", 2
        )

        insertExercise(
            db, 14, "lower_back_barbell_sumo_deadlift", "Sumo Deadlift",
            "Wide-stance deadlift variation with greater quad and adductor involvement. Reduced lower back shear vs. conventional.",
            "barbell", "compound", "intermediate", 2,
            """["Glutes","quads","adductors"]""",
            """["Stance 1.5x shoulder width, toes out 30-45 degrees","Grip inside knees","Vertical torso -- more upright than conventional","Pull knees out as you drive up"]""",
            """["Reduced lumbar stress vs. conventional","Greater quad recruitment","Shorter range of motion favors leverages for some lifters"]""",
            2, 2, """["hip_hinge","heavy_compound","barbell"]""", 2
        )

        insertExercise(
            db, 15, "lower_back_trapbar_deadlift", "Trap Bar Deadlift",
            "Neutral-grip deadlift with more upright torso and greater quad involvement. Easier to learn than barbell deadlift.",
            "trap_bar", "compound", "beginner", 2,
            """["Glutes","quads","traps"]""",
            """["Stand in center of trap bar","Vertical shins, neutral spine","Drive through floor with legs","Grip handles at sides, not forward"]""",
            """["Beginner-friendly deadlift alternative","Reduced lumbar shear force","Allows heavier loading safely"]""",
            3, 3, """["hip_hinge","heavy_compound","trap_bar"]""", 1
        )

        insertExercise(
            db, 16, "lower_back_barbell_rack_pull", "Rack Pull",
            "Partial-range deadlift starting from pins or blocks. Emphasizes upper back and traps with heavy loads.",
            "barbell", "compound", "intermediate", 2,
            """["Traps","glutes","forearms"]""",
            """["Set pins at or below knee height","Same setup as deadlift from top position","No bounce off pins -- dead stop each rep","Focus on upper back retraction at lockout"]""",
            """["Overload training for deadlift lockout","Trap and upper back hypertrophy","Builds grip strength with supra-maximal loads"]""",
            4, 4, """["hip_hinge","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 17, "lower_back_barbell_good_morning", "Good Morning",
            "Barbell hip hinge with high lower back and hamstring demand. Requires excellent spinal control. Advanced only.",
            "barbell", "compound", "advanced", 2,
            """["Hamstrings","glutes"]""",
            """["Bar on upper traps (squat position)","Soft knee bend throughout","Push hips back, maintain neutral spine","Do not go below parallel if lower back rounds"]""",
            """["Erector spinae hypertrophy","Reinforces hip hinge pattern","Hamstring stretch under load"]""",
            5, 5, """["hip_hinge","moderate_compound","barbell"]""", 3
        )

        insertExercise(
            db, 18, "lower_back_bodyweight_back_extension", "Back Extension (45-degree)",
            "Hyperextension bench exercise targeting erector spinae isometrically and concentrically. Beginner-friendly lower back isolation.",
            "bodyweight", "isolation", "beginner", 2,
            """["Glutes","hamstrings"]""",
            """["Hips on pad, ankles secured","Hands behind head or crossed on chest","Lower torso to 45 degrees, not 90","Extend to neutral, do not hyperextend"]""",
            """["Lower back endurance and stability","Beginner-safe with bodyweight","Can add weight as progression"]""",
            6, 6, """["hip_hinge","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 19, "lower_back_machine_reverse_hyperextension", "Reverse Hyperextension",
            "Machine-based hip extension with reduced spinal compression. Glutes and hamstrings as primary movers.",
            "machine", "isolation", "intermediate", 2,
            """["Glutes","hamstrings"]""",
            """["Torso on pad, legs hang off edge","Swing legs up to parallel","Squeeze glutes at top","Control descent -- no momentum"]""",
            """["Decompression effect on spine","Glute and hamstring activation without spinal load","Rehabilitation-friendly"]""",
            7, 7, """["hip_hinge","isolation","machine"]""", 2
        )

        insertExercise(
            db, 20, "lower_back_barbell_hip_thrust", "Barbell Hip Thrust",
            "Supine hip extension with maximal glute recruitment. Barbell rests on hips. Lower back stabilization demand justifies placement in this group.",
            "barbell", "compound", "beginner", 2,
            """["Glutes","hamstrings"]""",
            """["Upper back on bench edge, feet flat","Bar over hip crease (use pad)","Drive hips to full extension","Squeeze glutes at top, 1-second hold"]""",
            """["Highest glute activation of any exercise","Lower back spinal stability training","Safe heavy loading"]""",
            8, 8, """["hip_hinge","moderate_compound","barbell"]""", 1
        )

        insertExercise(
            db, 21, "lower_back_cable_pull_through", "Cable Pull-Through",
            "Cable hip hinge teaching proper deadlift mechanics. Beginner-friendly with constant tension.",
            "cable", "compound", "beginner", 2,
            """["Glutes","hamstrings"]""",
            """["Stand facing away from cable, rope between legs","Push hips back to stretch hamstrings","Drive hips forward to stand","Do not pull with arms"]""",
            """["Hip hinge pattern reinforcement","Lower back and glute activation without heavy load","Constant cable tension"]""",
            9, 9, """["hip_hinge","moderate_compound","cable"]""", 1
        )

        insertExercise(
            db, 22, "lower_back_barbell_deficit_deadlift", "Deficit Deadlift",
            "Deadlift standing on a 1-3 inch platform. Increases range of motion and lumbar demand. Advanced only.",
            "barbell", "compound", "advanced", 2,
            """["Glutes","hamstrings","quads"]""",
            """["Stand on 1-2 inch platform","All conventional deadlift cues apply","Greater hamstring stretch at bottom","Reduce load 10-15% vs. conventional"]""",
            """["Increased range of motion training","Off-the-floor strength development","Addresses weak points in deadlift"]""",
            10, 10, """["hip_hinge","heavy_compound","barbell"]""", 3
        )
    }

    // --- Chest (Group 3, IDs 23-33) ---

    private fun insertChestExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 23, "chest_barbell_bench_press", "Barbell Bench Press",
            "The foundational horizontal pressing movement. Pectoralis major (sternal head) as prime mover with anterior delt and tricep synergy.",
            "barbell", "compound", "beginner", 3,
            """["Anterior delts","triceps"]""",
            """["Feet flat, shoulder blades retracted","Bar to mid-chest, not neck","Elbows 45-degree angle to torso","Press in slight arc toward face"]""",
            """["Highest chest and tricep recruitment","Benchmarks strength progression clearly","Allows heaviest loading safely"]""",
            1, 1, """["push_horizontal","heavy_compound","barbell"]""", 1
        )

        insertExercise(
            db, 24, "chest_barbell_incline_bench_press", "Incline Barbell Bench Press",
            "Barbell press on 30-45 degree incline emphasizing upper (clavicular) chest fibers.",
            "barbell", "compound", "beginner", 3,
            """["Anterior delts","triceps","upper chest"]""",
            """["30-45 degree bench angle","Bar to upper chest","Elbows slightly wider than flat bench","Do not arch excessively"]""",
            """["Upper chest emphasis","Anterior delt development","Balances chest development"]""",
            2, 2, """["push_horizontal","heavy_compound","barbell"]""", 1
        )

        insertExercise(
            db, 25, "chest_dumbbell_bench_press", "Dumbbell Bench Press",
            "Dumbbell variation of flat bench press allowing greater range of motion and unilateral stability demand.",
            "dumbbell", "compound", "beginner", 3,
            """["Anterior delts","triceps"]""",
            """["Dumbbells start at chest level","Press up and slightly inward","Lower until stretch felt in chest","Keep wrists neutral"]""",
            """["Greater chest stretch than barbell","Unilateral balance requirement","Safer to train to failure (can drop dumbbells)"]""",
            3, 3, """["push_horizontal","moderate_compound","dumbbell"]""", 1
        )

        insertExercise(
            db, 26, "chest_dumbbell_incline_dumbbell_press", "Incline Dumbbell Press",
            "Dumbbell press on incline emphasizing upper chest. Greater range of motion vs. incline barbell.",
            "dumbbell", "compound", "beginner", 3,
            """["Anterior delts","triceps","upper chest"]""",
            """["30-45 degree incline","Dumbbells at upper chest","Press straight up, not inward as much as flat press","Full stretch at bottom"]""",
            """["Upper chest isolation","Unilateral stability challenge","Anterior delt development"]""",
            4, 4, """["push_horizontal","moderate_compound","dumbbell"]""", 1
        )

        insertExercise(
            db, 27, "chest_barbell_decline_bench_press", "Decline Barbell Bench Press",
            "Barbell press on 15-30 degree decline emphasizing lower (costal) chest fibers.",
            "barbell", "compound", "intermediate", 3,
            """["Triceps","anterior delts"]""",
            """["15-30 degree decline, ankles secured","Bar to lower chest","Shorter range of motion than flat bench","Blood pressure spike possible -- stop if dizzy"]""",
            """["Lower chest emphasis","Reduced shoulder stress vs. flat bench","Tricep involvement"]""",
            5, 5, """["push_horizontal","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 28, "chest_dumbbell_fly", "Dumbbell Fly",
            "Single-joint horizontal adduction isolating pectoralis major with minimal tricep involvement.",
            "dumbbell", "isolation", "beginner", 3,
            """["Anterior delts"]""",
            """["Slight elbow bend maintained throughout","Arc motion -- do not press","Lower until stretch in chest, not shoulder pain","Squeeze pecs at top"]""",
            """["Pure chest isolation","Stretch-mediated hypertrophy","Anterior delt secondary stimulus"]""",
            6, 6, """["push_horizontal","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 29, "chest_cable_crossover", "Cable Crossover",
            "Cable-based horizontal adduction with constant tension throughout range. Highly versatile angle adjustment.",
            "cable", "isolation", "beginner", 3,
            """["Anterior delts"]""",
            """["Cables set high, step forward for tension","Slight forward lean","Bring hands together in front of chest","Control eccentric -- do not let cables snap back"]""",
            """["Constant tension throughout rep","Adjustable angle for upper/mid/lower chest","Safe to train to failure"]""",
            7, 7, """["push_horizontal","isolation","cable"]""", 1
        )

        insertExercise(
            db, 30, "chest_machine_machine_chest_press", "Machine Chest Press",
            "Seated chest press on fixed path. Beginner-friendly heavy loading without stabilization demand.",
            "machine", "compound", "beginner", 3,
            """["Anterior delts","triceps"]""",
            """["Adjust seat so handles at mid-chest","Press straight forward","Full range of motion without lockout","Controlled eccentric"]""",
            """["Beginner-safe heavy loading","Reduced technique barrier","Easy progressive overload tracking"]""",
            8, 8, """["push_horizontal","moderate_compound","machine"]""", 1
        )

        insertExercise(
            db, 31, "chest_machine_pec_deck", "Pec Deck / Machine Fly",
            "Machine-based horizontal adduction isolating chest with guided path.",
            "machine", "isolation", "beginner", 3,
            """[]""",
            """["Adjust seat so handles at chest height","Bring handles together in front of chest","Squeeze and hold 1 second","Do not hyperextend shoulders on eccentric"]""",
            """["Pure chest isolation","Safe near-failure training","Constant tension"]""",
            9, 9, """["push_horizontal","isolation","machine"]""", 1
        )

        insertExercise(
            db, 32, "chest_bodyweight_push_up", "Push-Up",
            "Bodyweight horizontal press engaging chest, anterior delts, triceps, and core stabilizers. Highly versatile for home/travel.",
            "bodyweight", "compound", "beginner", 3,
            """["Anterior delts","triceps","core"]""",
            """["Hands slightly wider than shoulders","Plank position -- neutral spine","Lower chest to floor","Press through full range"]""",
            """["No equipment required","Core stability integration","Scalable difficulty (incline/decline/weighted)"]""",
            10, 10, """["push_horizontal","moderate_compound","bodyweight"]""", 1
        )

        insertExercise(
            db, 33, "chest_bodyweight_dips_chest_focused", "Dips (Chest-Focused)",
            "Bodyweight vertical press with torso leaned forward emphasizing lower chest and triceps.",
            "bodyweight", "compound", "intermediate", 3,
            """["Triceps","anterior delts"]""",
            """["Lean torso forward 30 degrees","Elbows flared slightly wider than tricep dips","Lower until stretch in chest","Press up without locking elbows"]""",
            """["Lower chest and tricep overload","Bodyweight strength benchmark","Can add weight for progression"]""",
            11, 11, """["push_horizontal","moderate_compound","bodyweight"]""", 2
        )
    }

    // --- Back (Group 4, IDs 34-44) ---

    private fun insertBackExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 34, "back_barbell_bent_over_row", "Barbell Bent-Over Row",
            "Hip-hinge barbell row with torso at 45-degree angle. Pulls from below knee to lower chest.",
            "barbell", "compound", "intermediate", 4,
            """["Rear delts","biceps","lower back"]""",
            """["Keep knees slightly bent and hips back","Pull to lower chest, not waist","Maintain neutral spine throughout","Drive elbows back, not just hands"]""",
            """["Builds lat width and mid-back thickness","High carryover to deadlift lockout strength","Trains hip hinge pattern under load"]""",
            1, 3, """["pull_horizontal","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 35, "back_bodyweight_pull_up", "Pull-Up",
            "Overhand grip vertical pull from dead hang to chin over bar. Lat-dominant bodyweight compound.",
            "bodyweight", "compound", "intermediate", 4,
            """["Biceps","core","rear delts"]""",
            """["Start from dead hang with arms fully extended","Pull chest to bar, not chin only","Engage lats before bending elbows","Control the descent -- do not drop"]""",
            """["Excellent lat width and thickness builder","Scalable via band assistance or added weight","Minimal equipment required"]""",
            2, 2, """["pull_vertical","moderate_compound","bodyweight"]""", 2
        )

        insertExercise(
            db, 36, "back_cable_lat_pulldown", "Lat Pulldown",
            "Seated vertical pull on cable machine. Accessible alternative to pull-ups for beginners. Prime mover: lats with bicep and rear delt synergy.",
            "cable", "compound", "beginner", 4,
            """["Biceps","rear delts"]""",
            """["Pull bar to upper chest, not behind neck","Lean back slightly (10-15 degrees)","Focus on pulling elbows down, not hands","Full stretch at top with scapular elevation"]""",
            """["Easier to learn than pull-ups","Allows precise load control","Effective for lat width development"]""",
            3, 1, """["pull_vertical","moderate_compound","cable"]""", 1
        )

        insertExercise(
            db, 37, "back_cable_seated_row", "Seated Cable Row",
            "Horizontal cable pull to torso. Targets mid-back (rhomboids, mid-traps) and lats.",
            "cable", "compound", "beginner", 4,
            """["Rhomboids","biceps","rear delts"]""",
            """["Sit upright with chest out","Initiate pull by retracting scapulae","Pull to lower chest or upper abdomen","Do not rock torso -- isolate upper back"]""",
            """["Builds mid-back thickness","Minimal lower back fatigue compared to barbell rows","Excellent for scapular retraction strength"]""",
            4, 1, """["pull_horizontal","moderate_compound","cable"]""", 1
        )

        insertExercise(
            db, 38, "back_dumbbell_single_arm_row", "Dumbbell Single-Arm Row",
            "Unilateral horizontal pull with one hand on bench for support. Allows extended range of motion and core stabilization demand.",
            "dumbbell", "compound", "beginner", 4,
            """["Biceps","rear delts","core"]""",
            """["Keep bench-side arm locked for stability","Pull dumbbell to hip, not chest","Rotate torso slightly at top for maximal lat contraction","Avoid excessive hip rotation"]""",
            """["Corrects left-right strength imbalances","Greater range of motion than barbell row","Trains anti-rotational core stability"]""",
            5, 2, """["pull_horizontal","moderate_compound","dumbbell"]""", 1
        )

        insertExercise(
            db, 39, "back_barbell_t_bar_row", "T-Bar Row",
            "Barbell anchored at one end, pulled to chest from standing hip-hinge position.",
            "barbell", "compound", "intermediate", 4,
            """["Biceps","rear delts","lower back"]""",
            """["Straddle the bar with feet shoulder-width","Pull bar to sternum, not waist","Keep elbows close to torso","Maintain neutral spine throughout"]""",
            """["Easier to learn than bent-over row","Allows heavy loading with lower injury risk","Builds mid-back thickness effectively"]""",
            6, 3, """["pull_horizontal","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 40, "back_machine_chest_supported_row", "Chest-Supported Row",
            "Machine row with chest pad eliminating lower back involvement. Isolates lats, rhomboids, and rear delts without spinal loading.",
            "machine", "compound", "beginner", 4,
            """["Rear delts","biceps"]""",
            """["Adjust pad so chest is fully supported","Pull handles to ribs, not chest","Squeeze scapulae together at peak contraction","Do not use momentum -- control the eccentric"]""",
            """["Zero lower back fatigue -- ideal after deadlifts or squats","Allows heavy loading safely","Excellent for hypertrophy with high reps"]""",
            7, 1, """["pull_horizontal","moderate_compound","machine"]""", 1
        )

        insertExercise(
            db, 41, "back_bodyweight_chin_up", "Chin-Up",
            "Supinated (underhand) grip vertical pull from dead hang. Higher bicep recruitment than pull-ups due to grip orientation.",
            "bodyweight", "compound", "intermediate", 4,
            """["Biceps","lats","core"]""",
            """["Use full supinated grip (palms facing you)","Pull chest to bar, not just chin over","Engage biceps and lats simultaneously","Lower with control to full arm extension"]""",
            """["Builds bicep strength alongside lats","Easier for beginners than pull-ups due to bicep involvement","Scalable via band assistance or added weight"]""",
            8, 2, """["pull_vertical","moderate_compound","bodyweight"]""", 2
        )

        insertExercise(
            db, 42, "back_cable_straight_arm_pulldown", "Straight-Arm Pulldown",
            "Cable pulldown with elbows locked. Isolates lats by removing bicep contribution. Movement is pure shoulder extension.",
            "cable", "isolation", "beginner", 4,
            """["Triceps (long head)","core"]""",
            """["Keep elbows locked throughout (slight bend acceptable)","Pull bar from overhead to thighs in arc motion","Lean forward slightly at waist","Focus on contracting lats, not pulling with arms"]""",
            """["Isolates lats without bicep fatigue","Excellent for mind-muscle connection","Useful finisher after heavy rows/pull-ups"]""",
            9, 10, """["pull_vertical","isolation","cable"]""", 1
        )

        insertExercise(
            db, 43, "back_cable_face_pull", "Face Pull",
            "Cable pull to face with rope attachment. Targets rear delts, mid-traps, and rotator cuff. Critical for shoulder health.",
            "cable", "isolation", "beginner", 4,
            """["Rear delts","rotator cuff","traps"]""",
            """["Pull rope to eye level, not chest","Externally rotate shoulders at peak (hands apart)","Keep elbows high throughout","Use light-moderate weight -- this is not a heavy row"]""",
            """["Strengthens rear delts and rotator cuff","Improves shoulder health and posture","Corrects anterior-dominant pressing imbalances"]""",
            10, 11, """["delt_rear","isolation","cable"]""", 1
        )

        insertExercise(
            db, 44, "back_cable_lat_pullover", "Cable Lat Pullover",
            "Cable pullover from overhead to waist with arms extended. Isolates lats through shoulder extension.",
            "cable", "isolation", "intermediate", 4,
            """["Triceps (long head)","core"]""",
            """["Stand facing away from cable stack","Pull cable from overhead to thighs in arc","Maintain slight elbow bend throughout","Lean forward at hips for greater lat stretch"]""",
            """["Isolates lats with minimal bicep involvement","Excellent lat stretch at top position","Trains shoulder extension strength"]""",
            11, 12, """["pull_vertical","isolation","cable"]""", 2
        )
    }

    // --- Shoulders (Group 5, IDs 45-55) ---

    private fun insertShouldersExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 45, "shoulders_barbell_overhead_press", "Overhead Press (Barbell)",
            "Standing barbell press from shoulders to lockout overhead. Trains anterior and lateral delts with high core demand.",
            "barbell", "compound", "intermediate", 5,
            """["Triceps","upper chest","core"]""",
            """["Stand with feet shoulder-width, core braced","Press bar in straight vertical line past face","Lock elbows fully at top","Do not lean back excessively -- maintain neutral spine"]""",
            """["Builds total shoulder strength and size","High carryover to athletic performance","Trains core stability under load"]""",
            1, 1, """["push_vertical","heavy_compound","barbell"]""", 2
        )

        insertExercise(
            db, 46, "shoulders_dumbbell_shoulder_press", "Dumbbell Shoulder Press",
            "Seated or standing dumbbell press. Allows greater range of motion than barbell and trains unilateral stability.",
            "dumbbell", "compound", "beginner", 5,
            """["Triceps","upper chest"]""",
            """["Start with dumbbells at shoulder height, palms forward","Press dumbbells up and slightly inward","Avoid clanging dumbbells at top","Lower with control to shoulder level"]""",
            """["Greater range of motion than barbell","Corrects left-right imbalances","Easier on wrists and shoulders for some lifters"]""",
            2, 1, """["push_vertical","moderate_compound","dumbbell"]""", 1
        )

        insertExercise(
            db, 47, "shoulders_dumbbell_arnold_press", "Arnold Press",
            "Dumbbell press with rotation from supinated to pronated grip. Emphasizes anterior and lateral delts through extended ROM.",
            "dumbbell", "compound", "intermediate", 5,
            """["Triceps","upper chest"]""",
            """["Start with palms facing you, dumbbells at shoulder height","Press while rotating palms forward","Finish with palms facing forward, arms locked","Reverse rotation on descent"]""",
            """["Extended range of motion hits all three delt heads","Builds shoulder mobility and strength simultaneously","Popularized by Arnold Schwarzenegger for hypertrophy"]""",
            3, 2, """["push_vertical","moderate_compound","dumbbell"]""", 2
        )

        insertExercise(
            db, 48, "shoulders_dumbbell_lateral_raise", "Lateral Raise",
            "Dumbbell raise to side with arms extended. Isolates lateral deltoid. Primary exercise for shoulder width.",
            "dumbbell", "isolation", "beginner", 5,
            """["Traps (upper)"]""",
            """["Slight forward lean, dumbbells at sides","Raise dumbbells to shoulder height (not higher)","Lead with elbows, not hands","Control the descent -- do not drop"]""",
            """["Best isolation exercise for lateral delt width","Low injury risk when performed correctly","Easily adjusted for drop sets and high-rep finishers"]""",
            4, 5, """["delt_lateral","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 49, "shoulders_cable_lateral_raise", "Cable Lateral Raise",
            "Cable lateral raise with D-handle. Provides constant tension throughout range of motion.",
            "cable", "isolation", "beginner", 5,
            """["Traps (upper)"]""",
            """["Stand perpendicular to cable stack","Raise handle to shoulder height","Maintain tension at bottom (do not rest)","Control the negative -- resist cable tension"]""",
            """["Constant tension on lateral delts","No dead zone at bottom like dumbbells","Allows precise load increments"]""",
            5, 6, """["delt_lateral","isolation","cable"]""", 1
        )

        insertExercise(
            db, 50, "shoulders_dumbbell_front_raise", "Front Raise",
            "Dumbbell raise to front with arms extended. Targets anterior deltoid.",
            "dumbbell", "isolation", "beginner", 5,
            """["Upper chest"]""",
            """["Start with dumbbells at thighs","Raise to shoulder height, not overhead","Use controlled tempo -- no swinging","Palms can face down or toward each other"]""",
            """["Isolates anterior deltoid","Useful for bodybuilding-style shoulder training","Can be performed alternating or simultaneously"]""",
            6, 7, """["push_vertical","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 51, "shoulders_dumbbell_reverse_fly", "Reverse Fly (Dumbbell)",
            "Bent-over fly targeting posterior deltoid. Performed with torso parallel to floor, dumbbells raised to sides.",
            "dumbbell", "isolation", "beginner", 5,
            """["Rhomboids","traps"]""",
            """["Hinge at hips, torso near parallel to floor","Raise dumbbells to sides with slight elbow bend","Squeeze shoulder blades together at top","Keep neck neutral -- do not look up"]""",
            """["Isolates posterior deltoid and rhomboids","Improves shoulder health and posture","Corrects imbalances from pressing-heavy programs"]""",
            7, 8, """["delt_rear","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 52, "shoulders_barbell_upright_row", "Barbell Upright Row",
            "Barbell pull from thighs to chest. Targets lateral delts and traps. Moderate impingement risk -- pull to chest, not chin.",
            "barbell", "compound", "intermediate", 5,
            """["Traps","biceps"]""",
            """["Grip slightly wider than shoulder width","Pull to chest height only (not chin)","Lead with elbows, not hands","Stop immediately if shoulder pain occurs"]""",
            """["Builds lateral delts and upper traps","Allows heavy loading for strength","Effective for shoulder mass"]""",
            8, 3, """["delt_lateral","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 53, "shoulders_barbell_shrug", "Barbell Shrug",
            "Barbell held at thighs, shoulders elevated maximally. Isolates upper trapezius.",
            "barbell", "isolation", "beginner", 5,
            """[]""",
            """["Hold bar at thighs with arms straight","Elevate shoulders straight up (not roll)","Pause at top for 1-2 seconds","Lower with control"]""",
            """["Builds upper trap mass and strength","Simple movement with low injury risk","Allows very heavy loading"]""",
            9, 9, """["delt_lateral","isolation","barbell"]""", 1
        )

        insertExercise(
            db, 54, "shoulders_dumbbell_shrug", "Dumbbell Shrug",
            "Dumbbell version of shrug. Dumbbells at sides, shoulders elevated maximally. Greater range of motion than barbell.",
            "dumbbell", "isolation", "beginner", 5,
            """[]""",
            """["Hold dumbbells at sides with arms straight","Elevate shoulders as high as possible","Do not roll shoulders -- straight up and down","Pause at peak contraction"]""",
            """["Greater range of motion than barbell","Easier grip for heavy loads","Corrects left-right imbalances"]""",
            10, 10, """["delt_lateral","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 55, "shoulders_barbell_landmine_press", "Landmine Press",
            "Barbell anchored at floor, pressed at angle overhead. Reduces shoulder stress compared to vertical pressing.",
            "barbell", "compound", "intermediate", 5,
            """["Triceps","upper chest","core"]""",
            """["Stand facing away from anchor point","Press bar from shoulder to overhead at 45-degree angle","Keep core tight throughout","Do not overextend lower back at top"]""",
            """["Lower shoulder impingement risk than vertical press","Trains core anti-rotation (single-arm variation)","Ideal for those with shoulder mobility limitations"]""",
            11, 2, """["push_vertical","moderate_compound","barbell"]""", 2
        )
    }

    // --- Arms (Group 6, IDs 56-67) ---

    private fun insertArmsExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 56, "arms_barbell_close_grip_bench_press", "Close-Grip Bench Press",
            "Barbell bench press with hands inside shoulder width. Shifts emphasis from chest to triceps. Only compound exercise in Arms group.",
            "barbell", "compound", "intermediate", 6,
            """["Chest","anterior delts"]""",
            """["Grip barbell hands shoulder-width or slightly narrower","Lower bar to lower chest","Keep elbows closer to torso than regular bench","Lock out fully at top"]""",
            """["Allows heaviest loading for triceps","Builds tricep strength with chest synergy","Excellent for powerlifting lockout strength"]""",
            1, 1, """["arm_push","moderate_compound","barbell"]""", 2
        )

        insertExercise(
            db, 57, "arms_barbell_curl", "Barbell Curl",
            "Standing barbell curl from thighs to shoulders. Classic bicep mass builder. Allows heaviest loading of all curl variations.",
            "barbell", "isolation", "beginner", 6,
            """["Forearms","brachialis"]""",
            """["Stand with feet shoulder-width, elbows at sides","Curl bar to shoulders without moving elbows forward","Do not swing hips -- isolate biceps","Lower with control to full elbow extension"]""",
            """["Allows heaviest weight of any bicep exercise","Builds bicep mass and strength effectively","Simple movement pattern"]""",
            2, 5, """["arm_pull","isolation","barbell"]""", 1
        )

        insertExercise(
            db, 58, "arms_dumbbell_curl", "Dumbbell Curl",
            "Standing or seated dumbbell curl. Can be performed simultaneously or alternating. Allows supination throughout range of motion.",
            "dumbbell", "isolation", "beginner", 6,
            """["Forearms","brachialis"]""",
            """["Start with arms fully extended, palms forward or neutral","Curl dumbbells to shoulders","Supinate wrists as you curl (rotate palms up)","Keep elbows stationary"]""",
            """["Corrects left-right imbalances","Allows full supination for bicep peak contraction","Can be performed seated to eliminate momentum"]""",
            3, 5, """["arm_pull","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 59, "arms_dumbbell_hammer_curl", "Hammer Curl",
            "Dumbbell curl with neutral grip (palms facing each other). Targets brachialis and brachioradialis more than standard curls.",
            "dumbbell", "isolation", "beginner", 6,
            """["Brachialis","forearms"]""",
            """["Hold dumbbells with palms facing each other throughout","Curl to shoulders without rotating wrists","Keep elbows tight to torso","Lower with control"]""",
            """["Builds brachialis for arm thickness","Trains forearm strength simultaneously","Easier on wrists than supinated curls"]""",
            4, 6, """["arm_pull","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 60, "arms_ez_bar_preacher_curl", "Preacher Curl",
            "EZ bar curl with upper arms supported on preacher bench. Isolates biceps by eliminating shoulder involvement.",
            "ez_bar", "isolation", "beginner", 6,
            """["Brachialis"]""",
            """["Position arms on pad with armpits at top edge","Lower bar to near-full extension (do not hyperextend)","Curl to top without lifting elbows off pad","Use EZ bar to reduce wrist strain"]""",
            """["Strict isolation with no momentum","Emphasizes bicep short head and peak","Reduces cheating and improves mind-muscle connection"]""",
            5, 7, """["arm_pull","isolation","ez_bar"]""", 1
        )

        insertExercise(
            db, 61, "arms_dumbbell_incline_curl", "Incline Dumbbell Curl",
            "Dumbbell curl performed on incline bench (45-60 degrees). Extends shoulders behind torso, maximally stretching bicep long head.",
            "dumbbell", "isolation", "intermediate", 6,
            """["Forearms"]""",
            """["Set bench to 45-60 degree incline","Let arms hang straight down with shoulders extended","Curl without moving upper arms forward","Emphasize the stretch at bottom"]""",
            """["Maximally stretches bicep long head","Eliminates momentum -- strict isolation","Builds bicep peak"]""",
            6, 8, """["arm_pull","isolation","dumbbell"]""", 2
        )

        insertExercise(
            db, 62, "arms_cable_curl", "Cable Curl",
            "Cable curl with straight bar or EZ bar attachment. Provides constant tension throughout range of motion.",
            "cable", "isolation", "beginner", 6,
            """["Forearms"]""",
            """["Stand facing cable stack, bar at thighs","Curl bar to shoulders keeping elbows stationary","Maintain tension at bottom (do not rest)","Control the negative against cable resistance"]""",
            """["Constant tension on biceps (no dead zones)","Allows precise load adjustments","Excellent for high-rep pump work"]""",
            7, 6, """["arm_pull","isolation","cable"]""", 1
        )

        insertExercise(
            db, 63, "arms_ez_bar_skull_crusher", "Skull Crusher (Lying Tricep Extension)",
            "Lying tricep extension with EZ bar lowered to forehead. Isolates triceps through elbow extension. High elbow stress -- use EZ bar.",
            "ez_bar", "isolation", "intermediate", 6,
            """[]""",
            """["Lie on bench, bar held above chest with arms vertical","Lower bar to forehead by bending elbows only","Keep upper arms stationary (perpendicular to floor)","Press back to lockout"]""",
            """["Excellent tricep isolation and stretch","Builds tricep long head mass","Allows moderate-heavy loading"]""",
            8, 4, """["arm_push","isolation","ez_bar"]""", 2
        )

        insertExercise(
            db, 64, "arms_cable_tricep_pushdown", "Tricep Pushdown (Cable)",
            "Cable pushdown with rope, V-bar, or straight bar. Most popular tricep isolation exercise.",
            "cable", "isolation", "beginner", 6,
            """[]""",
            """["Stand facing cable stack, elbows at sides","Push attachment down to full elbow extension","Keep elbows stationary -- do not flare","Control the return -- resist cable tension"]""",
            """["Easy to learn and execute","Constant tension throughout movement","Low injury risk"]""",
            9, 3, """["arm_push","isolation","cable"]""", 1
        )

        insertExercise(
            db, 65, "arms_dumbbell_overhead_tricep_extension", "Overhead Tricep Extension",
            "Dumbbell held overhead, lowered behind head via elbow flexion. Maximally stretches tricep long head.",
            "dumbbell", "isolation", "beginner", 6,
            """[]""",
            """["Hold single dumbbell overhead with both hands","Lower behind head by bending elbows","Keep elbows pointing forward (do not flare)","Extend to lockout overhead"]""",
            """["Maximally stretches tricep long head","Can be performed seated or standing","Builds tricep mass effectively"]""",
            10, 4, """["arm_push","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 66, "arms_dumbbell_concentration_curl", "Concentration Curl",
            "Seated single-arm curl with elbow braced against inner thigh. Strict isolation with no momentum.",
            "dumbbell", "isolation", "beginner", 6,
            """["Brachialis"]""",
            """["Sit on bench, brace elbow against inner thigh","Curl dumbbell to shoulder with full supination","Keep upper arm stationary throughout","Squeeze bicep at peak contraction"]""",
            """["Strictest bicep isolation -- no cheating possible","Emphasizes bicep peak","Excellent for mind-muscle connection"]""",
            11, 8, """["arm_pull","isolation","dumbbell"]""", 1
        )

        insertExercise(
            db, 67, "arms_barbell_wrist_curl", "Wrist Curl",
            "Barbell wrist flexion with forearms supported on bench. Isolates forearm flexors.",
            "barbell", "isolation", "beginner", 6,
            """[]""",
            """["Sit with forearms on bench, wrists hanging off edge","Curl bar up via wrist flexion only","Lower with control to full wrist extension","Use light weight -- forearms fatigue quickly"]""",
            """["Builds forearm size and grip strength","Improves grip endurance for deadlifts and rows","Simple and safe movement"]""",
            12, 12, """["forearm","isolation","barbell"]""", 1
        )
    }

    // --- Core (Group 7, IDs 68-78) ---

    private fun insertCoreExercises(db: SupportSQLiteDatabase) {
        insertExercise(
            db, 68, "core_bodyweight_plank", "Plank",
            "Front plank on forearms and toes. Isometric hold. Trains rectus abdominis and transverse abdominis in anti-extension pattern.",
            "bodyweight", "isolation", "beginner", 7,
            """["Lower back","shoulders"]""",
            """["Maintain straight line from head to heels","Engage glutes and brace core","Do not let hips sag or pike","Breathe normally -- do not hold breath"]""",
            """["Safest core exercise for spine (isometric, no flexion)","Builds anterior core endurance","Accessible to all levels"]""",
            1, 1, """["core_anti_flexion","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 69, "core_bodyweight_ab_wheel_rollout", "Ab Wheel Rollout",
            "Ab wheel rollout from knees or toes. Extreme anti-extension demand. Advanced core exercise requiring high baseline strength.",
            "bodyweight", "compound", "advanced", 7,
            """["Lats","shoulders","hip flexors"]""",
            """["Start on knees (beginners) or toes (advanced)","Roll wheel forward while maintaining posterior pelvic tilt","Do not let lower back arch","Roll out only as far as you can maintain neutral spine"]""",
            """["Builds extreme core strength","Trains lats, shoulders, and hip flexors simultaneously","Functional carryover to athletic performance"]""",
            2, 2, """["core_anti_flexion","compound","bodyweight"]""", 3
        )

        insertExercise(
            db, 70, "core_bodyweight_hanging_leg_raise", "Hanging Leg Raise",
            "Hang from pull-up bar, raise legs to horizontal or higher. Trains rectus abdominis and hip flexors.",
            "bodyweight", "compound", "intermediate", 7,
            """["Hip flexors","forearms"]""",
            """["Hang from bar with overhand grip","Raise legs to horizontal (or knees to chest for regression)","Control the descent -- do not swing","Engage core before initiating movement"]""",
            """["Builds lower ab and hip flexor strength","Trains grip endurance simultaneously","Scalable from knee raises to toes-to-bar"]""",
            3, 3, """["core_flexion","compound","bodyweight"]""", 2
        )

        insertExercise(
            db, 71, "core_cable_crunch", "Cable Crunch",
            "Kneeling cable crunch with rope attachment. Spinal flexion under load. Isolates rectus abdominis.",
            "cable", "isolation", "beginner", 7,
            """[]""",
            """["Kneel facing cable stack, rope behind head","Crunch down by flexing spine (not hips)","Keep hips stationary throughout","Control the eccentric -- resist cable tension"]""",
            """["Allows progressive overload via cable stack","Isolates abs with minimal hip flexor involvement","Easy to learn and execute"]""",
            4, 4, """["core_flexion","isolation","cable"]""", 1
        )

        insertExercise(
            db, 72, "core_bodyweight_russian_twist", "Russian Twist",
            "Seated rotation with feet elevated. Trains obliques and transverse abdominis.",
            "bodyweight", "isolation", "beginner", 7,
            """["Obliques","hip flexors"]""",
            """["Sit with knees bent, feet elevated off floor","Rotate torso side to side, touching floor beside hips","Keep core engaged throughout","Use light weight or bodyweight only"]""",
            """["Builds rotational core strength","Trains obliques effectively","Can be performed with or without weight"]""",
            5, 7, """["core_rotation","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 73, "core_bodyweight_bicycle_crunch", "Bicycle Crunch",
            "Alternating elbow-to-knee crunch with legs cycling. Trains rectus abdominis and obliques dynamically.",
            "bodyweight", "isolation", "beginner", 7,
            """["Obliques","hip flexors"]""",
            """["Lie on back, hands behind head","Bring opposite elbow to opposite knee while extending other leg","Rotate torso, do not just move arms","Maintain controlled tempo -- quality over speed"]""",
            """["High oblique activation","No equipment needed","Combines flexion and rotation"]""",
            6, 6, """["core_flexion","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 74, "core_bodyweight_dead_bug", "Dead Bug",
            "Lying on back, alternating arm and leg extension while maintaining lower back contact with floor. Anti-extension and stabilization exercise.",
            "bodyweight", "isolation", "beginner", 7,
            """["Lower back","hip flexors"]""",
            """["Lie on back, arms vertical, knees bent at 90 degrees","Extend opposite arm and leg while keeping lower back flat","Do not let lower back arch off floor","Move slowly and deliberately"]""",
            """["Safest core exercise (spine neutral)","Trains core stabilization and coordination","Excellent for beginners and rehab"]""",
            7, 1, """["core_anti_flexion","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 75, "core_cable_pallof_press", "Pallof Press",
            "Cable anti-rotation press. Stand perpendicular to cable, press handle away from chest while resisting rotation.",
            "cable", "isolation", "intermediate", 7,
            """["Obliques","shoulders"]""",
            """["Stand perpendicular to cable stack, handle at chest","Press handle straight out without rotating torso","Hold for 2-3 seconds, then return to chest","Keep core braced throughout"]""",
            """["Best anti-rotation core exercise","Functional carryover to sports and daily life","Low injury risk"]""",
            8, 5, """["core_rotation","isolation","cable"]""", 2
        )

        insertExercise(
            db, 76, "core_bodyweight_decline_sit_up", "Decline Sit-Up",
            "Sit-up performed on decline bench. Increases resistance compared to flat sit-ups.",
            "bodyweight", "isolation", "beginner", 7,
            """["Hip flexors"]""",
            """["Set bench to 15-30 degree decline","Anchor feet at top of bench","Sit up to vertical, lower with control","Do not pull on neck -- hands behind head lightly"]""",
            """["Progressive overload via bench angle","Builds ab strength and endurance","Can be weighted with plate on chest"]""",
            9, 5, """["core_flexion","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 77, "core_bodyweight_side_plank", "Side Plank",
            "Plank on one forearm and side of foot. Isometric hold. Trains obliques and transverse abdominis in anti-lateral flexion pattern.",
            "bodyweight", "isolation", "beginner", 7,
            """["Obliques","glutes"]""",
            """["Lie on side, prop up on forearm, feet stacked","Maintain straight line from head to feet","Do not let hips sag","Engage obliques and glutes throughout"]""",
            """["Best oblique isolation exercise (isometric)","Improves lateral core stability","Low injury risk"]""",
            10, 2, """["core_rotation","isolation","bodyweight"]""", 1
        )

        insertExercise(
            db, 78, "core_bodyweight_dragon_flag", "Dragon Flag",
            "Lying on bench, grip behind head, raise entire body to vertical via core and hip flexion. Extreme difficulty. Requires advanced baseline strength.",
            "bodyweight", "compound", "advanced", 7,
            """["Hip flexors","lats"]""",
            """["Lie on bench, grip bench behind head","Raise legs and torso to vertical (shoulders remain on bench)","Lower with extreme control -- do not collapse","Only perform if you can complete 3+ strict reps"]""",
            """["Extreme core and hip flexor strength builder","Impressive display of control and strength","Trains entire anterior chain"]""",
            11, 3, """["core_flexion","compound","bodyweight"]""", 99
        )
    }

    // ===========================================================================================
    // Exercise-Muscle Junction Table
    // ===========================================================================================

    private fun insertExerciseMuscles(db: SupportSQLiteDatabase) {
        // Helper: insert a primary + secondary muscle link
        fun link(exerciseId: Long, muscleGroupId: Long, isPrimary: Boolean) {
            db.execSQL(
                "INSERT INTO exercise_muscles (exercise_id, muscle_group_id, is_primary) VALUES (?, ?, ?)",
                arrayOf(exerciseId, muscleGroupId, if (isPrimary) 1 else 0),
            )
        }

        // Group IDs: 1=Legs, 2=Lower Back, 3=Chest, 4=Back, 5=Shoulders, 6=Arms, 7=Core

        // --- Legs (IDs 1-12) ---
        link(1, 1, true) // Barbell Back Squat -> Legs (primary)
        link(1, 2, false) // -> Lower Back (secondary)
        link(1, 7, false) // -> Core (secondary)

        link(2, 1, true) // Barbell Front Squat -> Legs
        link(2, 4, false) // -> Back (upper back secondary)
        link(2, 7, false) // -> Core

        link(3, 1, true) // Leg Press -> Legs

        link(4, 1, true) // RDL -> Legs
        link(4, 2, false) // -> Lower Back

        link(5, 1, true) // Walking Lunges -> Legs
        link(5, 7, false) // -> Core

        link(6, 1, true) // Bulgarian Split Squat -> Legs
        link(6, 7, false) // -> Core

        link(7, 1, true) // Hack Squat -> Legs

        link(8, 1, true) // Leg Extension -> Legs (only)

        link(9, 1, true) // Lying Leg Curl -> Legs (only)

        link(10, 1, true) // Seated Leg Curl -> Legs (only)

        link(11, 1, true) // Standing Calf Raise -> Legs (only)

        link(12, 1, true) // Seated Calf Raise -> Legs (only)

        // --- Lower Back (IDs 13-22) ---
        link(13, 2, true) // Conventional Deadlift -> Lower Back
        link(13, 1, false) // -> Legs (glutes, hamstrings)
        link(13, 4, false) // -> Back (traps)
        link(13, 6, false) // -> Arms (forearms/grip)

        link(14, 2, true) // Sumo Deadlift -> Lower Back
        link(14, 1, false) // -> Legs (quads, adductors)

        link(15, 2, true) // Trap Bar Deadlift -> Lower Back
        link(15, 1, false) // -> Legs (quads)
        link(15, 4, false) // -> Back (traps)

        link(16, 2, true) // Rack Pull -> Lower Back
        link(16, 4, false) // -> Back (traps)
        link(16, 6, false) // -> Arms (forearms/grip)

        link(17, 2, true) // Good Morning -> Lower Back
        link(17, 1, false) // -> Legs (hamstrings, glutes)

        link(18, 2, true) // Back Extension -> Lower Back
        link(18, 1, false) // -> Legs (glutes, hamstrings)

        link(19, 2, true) // Reverse Hyperextension -> Lower Back
        link(19, 1, false) // -> Legs (glutes, hamstrings)

        link(20, 2, true) // Barbell Hip Thrust -> Lower Back
        link(20, 1, false) // -> Legs (glutes, hamstrings)

        link(21, 2, true) // Cable Pull-Through -> Lower Back
        link(21, 1, false) // -> Legs (glutes, hamstrings)

        link(22, 2, true) // Deficit Deadlift -> Lower Back
        link(22, 1, false) // -> Legs (glutes, hamstrings, quads)

        // --- Chest (IDs 23-33) ---
        link(23, 3, true) // Barbell Bench Press -> Chest
        link(23, 5, false) // -> Shoulders (anterior delts)
        link(23, 6, false) // -> Arms (triceps)

        link(24, 3, true) // Incline Barbell Bench Press -> Chest
        link(24, 5, false) // -> Shoulders
        link(24, 6, false) // -> Arms (triceps)

        link(25, 3, true) // Dumbbell Bench Press -> Chest
        link(25, 5, false) // -> Shoulders
        link(25, 6, false) // -> Arms (triceps)

        link(26, 3, true) // Incline Dumbbell Press -> Chest
        link(26, 5, false) // -> Shoulders
        link(26, 6, false) // -> Arms (triceps)

        link(27, 3, true) // Decline Barbell Bench Press -> Chest
        link(27, 5, false) // -> Shoulders
        link(27, 6, false) // -> Arms (triceps)

        link(28, 3, true) // Dumbbell Fly -> Chest
        link(28, 5, false) // -> Shoulders (anterior delts)

        link(29, 3, true) // Cable Crossover -> Chest
        link(29, 5, false) // -> Shoulders (anterior delts)

        link(30, 3, true) // Machine Chest Press -> Chest
        link(30, 5, false) // -> Shoulders
        link(30, 6, false) // -> Arms (triceps)

        link(31, 3, true) // Pec Deck -> Chest (only)

        link(32, 3, true) // Push-Up -> Chest
        link(32, 5, false) // -> Shoulders
        link(32, 6, false) // -> Arms (triceps)
        link(32, 7, false) // -> Core

        link(33, 3, true) // Dips (Chest-Focused) -> Chest
        link(33, 5, false) // -> Shoulders
        link(33, 6, false) // -> Arms (triceps)

        // --- Back (IDs 34-44) ---
        link(34, 4, true) // Barbell Bent-Over Row -> Back
        link(34, 5, false) // -> Shoulders (rear delts)
        link(34, 6, false) // -> Arms (biceps)
        link(34, 2, false) // -> Lower Back

        link(35, 4, true) // Pull-Up -> Back
        link(35, 6, false) // -> Arms (biceps)
        link(35, 7, false) // -> Core

        link(36, 4, true) // Lat Pulldown -> Back
        link(36, 6, false) // -> Arms (biceps)

        link(37, 4, true) // Seated Cable Row -> Back
        link(37, 6, false) // -> Arms (biceps)

        link(38, 4, true) // Dumbbell Single-Arm Row -> Back
        link(38, 6, false) // -> Arms (biceps)
        link(38, 7, false) // -> Core

        link(39, 4, true) // T-Bar Row -> Back
        link(39, 6, false) // -> Arms (biceps)
        link(39, 2, false) // -> Lower Back

        link(40, 4, true) // Chest-Supported Row -> Back
        link(40, 6, false) // -> Arms (biceps)

        link(41, 4, true) // Chin-Up -> Back
        link(41, 6, false) // -> Arms (biceps)
        link(41, 7, false) // -> Core

        link(42, 4, true) // Straight-Arm Pulldown -> Back
        link(42, 7, false) // -> Core

        link(43, 4, true) // Face Pull -> Back
        link(43, 5, false) // -> Shoulders (rear delts)

        link(44, 4, true) // Cable Lat Pullover -> Back
        link(44, 7, false) // -> Core

        // --- Shoulders (IDs 45-55) ---
        link(45, 5, true) // Overhead Press -> Shoulders
        link(45, 6, false) // -> Arms (triceps)
        link(45, 3, false) // -> Chest (upper)
        link(45, 7, false) // -> Core

        link(46, 5, true) // Dumbbell Shoulder Press -> Shoulders
        link(46, 6, false) // -> Arms (triceps)

        link(47, 5, true) // Arnold Press -> Shoulders
        link(47, 6, false) // -> Arms (triceps)

        link(48, 5, true) // Lateral Raise -> Shoulders (only)

        link(49, 5, true) // Cable Lateral Raise -> Shoulders (only)

        link(50, 5, true) // Front Raise -> Shoulders (only)

        link(51, 5, true) // Reverse Fly -> Shoulders (only)

        link(52, 5, true) // Barbell Upright Row -> Shoulders
        link(52, 6, false) // -> Arms (biceps)

        link(53, 5, true) // Barbell Shrug -> Shoulders (only)

        link(54, 5, true) // Dumbbell Shrug -> Shoulders (only)

        link(55, 5, true) // Landmine Press -> Shoulders
        link(55, 6, false) // -> Arms (triceps)
        link(55, 3, false) // -> Chest (upper)
        link(55, 7, false) // -> Core

        // --- Arms (IDs 56-67) ---
        link(56, 6, true) // Close-Grip Bench Press -> Arms
        link(56, 3, false) // -> Chest
        link(56, 5, false) // -> Shoulders (anterior delts)

        link(57, 6, true) // Barbell Curl -> Arms (only)

        link(58, 6, true) // Dumbbell Curl -> Arms (only)

        link(59, 6, true) // Hammer Curl -> Arms (only)

        link(60, 6, true) // Preacher Curl -> Arms (only)

        link(61, 6, true) // Incline Dumbbell Curl -> Arms (only)

        link(62, 6, true) // Cable Curl -> Arms (only)

        link(63, 6, true) // Skull Crusher -> Arms (only)

        link(64, 6, true) // Tricep Pushdown -> Arms (only)

        link(65, 6, true) // Overhead Tricep Extension -> Arms (only)

        link(66, 6, true) // Concentration Curl -> Arms (only)

        link(67, 6, true) // Wrist Curl -> Arms (only)

        // --- Core (IDs 68-78) ---
        link(68, 7, true) // Plank -> Core
        link(68, 2, false) // -> Lower Back

        link(69, 7, true) // Ab Wheel Rollout -> Core
        link(69, 5, false) // -> Shoulders

        link(70, 7, true) // Hanging Leg Raise -> Core
        link(70, 6, false) // -> Arms (forearms/grip)

        link(71, 7, true) // Cable Crunch -> Core (only)

        link(72, 7, true) // Russian Twist -> Core (only)

        link(73, 7, true) // Bicycle Crunch -> Core (only)

        link(74, 7, true) // Dead Bug -> Core
        link(74, 2, false) // -> Lower Back

        link(75, 7, true) // Pallof Press -> Core

        link(76, 7, true) // Decline Sit-Up -> Core (only)

        link(77, 7, true) // Side Plank -> Core (only)

        link(78, 7, true) // Dragon Flag -> Core (only)
    }
}
