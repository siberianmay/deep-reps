package com.deepreps.feature.workout.summary

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Navigation constants and graph builder extensions for the workout summary screen.
 *
 * Route conventions per architecture.md Section 8.2:
 * - Route names use snake_case
 * - Arguments are embedded in the route pattern
 */
object WorkoutSummaryNavigation {

    const val SESSION_ID_ARG = "sessionId"
    const val ROUTE = "workout_summary/{$SESSION_ID_ARG}"

    fun createRoute(sessionId: Long): String = "workout_summary/$sessionId"
}

/**
 * Registers the workout summary screen in the navigation graph.
 */
fun NavGraphBuilder.workoutSummaryScreen(
    onDismiss: () -> Unit,
    onNavigateToCreateTemplate: (exerciseIds: List<Long>) -> Unit,
) {
    composable(
        route = WorkoutSummaryNavigation.ROUTE,
        arguments = listOf(
            navArgument(WorkoutSummaryNavigation.SESSION_ID_ARG) {
                type = NavType.LongType
            },
        ),
    ) {
        WorkoutSummarySheet(
            onDismiss = onDismiss,
            onNavigateToCreateTemplate = onNavigateToCreateTemplate,
        )
    }
}

/**
 * Navigate to the workout summary screen.
 */
fun NavController.navigateToWorkoutSummary(sessionId: Long) {
    navigate(WorkoutSummaryNavigation.createRoute(sessionId))
}
