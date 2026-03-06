package com.deepreps.core.ui.component

import com.deepreps.core.domain.model.enums.MuscleGroup

/**
 * Maps SVG path IDs from the anatomy diagram to muscle groups.
 * Generated from spatial + bounding-box analysis of resources/anatomy_template.svg.
 *
 * The SVG contains 189 paths in a 1333x1333 viewBox showing front and back
 * anatomical views side by side.
 *
 * Paths with bounding boxes exceeding 200x400 or 40K sq. units are classified
 * as body outlines (base), not individual muscles.
 */
@Suppress("MaxLineLength")
internal val MUSCLE_GROUP_PATHS: Map<MuscleGroup, Set<String>> = mapOf(
    MuscleGroup.CHEST to setOf(
        "path55", "path56", "path65", "path162", "path165",
    ),
    MuscleGroup.SHOULDERS to setOf(
        "path54", "path76", "path77", "path78", "path79", "path80", "path81",
        "path82", "path83", "path88", "path89", "path90", "path91", "path92",
        "path93", "path94", "path95", "path157", "path159", "path160",
        "path192", "path193", "path197", "path198", "path234", "path237",
    ),
    MuscleGroup.ARMS to setOf(
        "path66", "path68", "path69", "path70", "path71", "path73", "path74",
        "path75", "path84", "path85", "path86", "path87", "path97", "path101",
        "path108", "path109", "path110", "path111", "path113",
        "path115", "path116", "path117", "path118", "path119",
        "path120", "path121", "path122", "path124", "path148", "path149",
        "path150", "path151", "path163", "path166", "path168",
        "path169", "path170", "path177", "path178", "path194", "path195",
        "path199", "path201", "path209", "path210",
        "path212", "path213", "path214", "path235",
    ),
    MuscleGroup.CORE to setOf(
        "path58", "path59", "path62", "path230",
        "path172", "path173", "path174", "path175", "path176",
    ),
    MuscleGroup.LEGS to setOf(
        "path53", "path123", "path125", "path127", "path128",
        "path129", "path130", "path131", "path132", "path133", "path134",
        "path135", "path137", "path138", "path139", "path140",
        "path141", "path143", "path144", "path145", "path147",
        "path153", "path154", "path155", "path156", "path179",
        "path180", "path181", "path182", "path183", "path185",
        "path186", "path187", "path188", "path190", "path215", "path216",
        "path217", "path218", "path219", "path220", "path221",
        "path223", "path224", "path225", "path226", "path228",
    ),
    MuscleGroup.BACK to setOf(
        "path61", "path63", "path64", "path67", "path72", "path164",
    ),
    MuscleGroup.LOWER_BACK to setOf(
        "path60", "path231", "path171",
    ),
)

/** Path IDs that form the base body outline (always rendered, never highlighted). */
internal val BASE_PATHS: Set<String> = setOf(
    // Full body outlines
    "path49", "path50", "path51", "path52",
    // Left hand / forearm extremities
    "path96", "path98", "path99", "path100", "path102", "path103",
    "path104", "path105", "path106", "path107",
    // Right hand / forearm extremities
    "path200", "path202", "path203", "path204", "path205", "path206", "path207",
    // Feet
    "path142", "path189", "path191", "path227", "path229",
    // Ground shadow / lower extremities
    "path232", "path233",
    // Oversized paths moved from muscle groups (body outlines/shading):
    "path57", "path161", // from chest
    "path158", "path196", // from shoulders
    "path112", "path114", "path167", "path208", "path211", "path236", // from arms
    "path126", "path136", "path146", "path152", "path184", "path222", // from legs
)
