package com.deepreps.feature.aiplan

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Navigation graph registration and NavController extensions for the plan review screen.
 *
 * Route: "plan_review/{exercise_ids}" where exercise_ids is a comma-separated string of Longs.
 *
 * The PlanReviewScreen creates its own hiltViewModel and collects side effects internally.
 * This file handles route definition, argument parsing, and callback wiring only.
 */
object PlanReviewNavigation {

    const val EXERCISE_IDS_ARG = "exercise_ids"
    const val TEMPLATE_ID_ARG = "template_id"
    const val ROUTE = "plan_review/{$EXERCISE_IDS_ARG}?$TEMPLATE_ID_ARG={$TEMPLATE_ID_ARG}"

    fun createRoute(exerciseIds: List<Long>, templateId: Long? = null): String {
        val base = "plan_review/${exerciseIds.joinToString(",")}"
        return if (templateId != null) "$base?$TEMPLATE_ID_ARG=$templateId" else base
    }
}

/**
 * Registers the plan review screen in the navigation graph.
 *
 * @param onNavigateToWorkout Called when the user confirms a plan and a session is created.
 * @param onNavigateBack Called when the user presses back from the plan review screen.
 */
fun NavGraphBuilder.planReviewScreen(
    onNavigateToWorkout: (sessionId: Long) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(
        route = PlanReviewNavigation.ROUTE,
        arguments = listOf(
            navArgument(PlanReviewNavigation.EXERCISE_IDS_ARG) {
                type = NavType.StringType
            },
            navArgument(PlanReviewNavigation.TEMPLATE_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val exerciseIdsString = backStackEntry.arguments
            ?.getString(PlanReviewNavigation.EXERCISE_IDS_ARG)
            .orEmpty()

        val exerciseIds = exerciseIdsString
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }

        PlanReviewScreen(
            exerciseIds = exerciseIds,
            onNavigateToWorkout = onNavigateToWorkout,
            onBack = onNavigateBack,
        )
    }
}

/**
 * Navigate to the plan review screen with the given exercise IDs.
 */
fun NavController.navigateToPlanReview(exerciseIds: List<Long>, templateId: Long? = null) {
    navigate(PlanReviewNavigation.createRoute(exerciseIds, templateId))
}
