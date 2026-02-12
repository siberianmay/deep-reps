package com.deepreps.feature.workout.active

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

/**
 * Navigation graph registration and NavController extensions for the active workout screen.
 *
 * Route: "workout_active/{session_id}"
 * Deep link: "deepreps://workout/active" (resumes current active session)
 *
 * The sessionId is passed via SavedStateHandle to the ViewModel.
 */
object WorkoutActiveNavigation {

    const val SESSION_ID_ARG = "session_id"
    const val ROUTE = "workout_active/{$SESSION_ID_ARG}"

    fun createRoute(sessionId: Long): String = "workout_active/$sessionId"
}

/**
 * Registers the active workout screen in the navigation graph.
 */
fun NavGraphBuilder.activeWorkoutScreen(
    onNavigateToSummary: (Long) -> Unit,
    onNavigateBack: () -> Unit,
) {
    composable(
        route = WorkoutActiveNavigation.ROUTE,
        arguments = listOf(
            navArgument(WorkoutActiveNavigation.SESSION_ID_ARG) {
                type = NavType.LongType
            },
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "deepreps://workout/active" },
        ),
    ) {
        val viewModel: WorkoutViewModel = hiltViewModel()

        WorkoutScreen(
            viewModel = viewModel,
            onNavigateToSummary = onNavigateToSummary,
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Navigate to the active workout screen with a given session ID.
 */
fun NavController.navigateToActiveWorkout(sessionId: Long) {
    navigate(WorkoutActiveNavigation.createRoute(sessionId))
}
