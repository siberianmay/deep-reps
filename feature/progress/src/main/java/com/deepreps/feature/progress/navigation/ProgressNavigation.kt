package com.deepreps.feature.progress.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.deepreps.feature.progress.ExerciseProgressScreen
import com.deepreps.feature.progress.ProgressDashboardScreen
import com.deepreps.feature.progress.SessionDetailScreen

/**
 * Navigation constants and graph builder extensions for the progress feature.
 *
 * Route conventions per architecture.md Section 8.2:
 * - Route names use snake_case
 * - Arguments are embedded in the route pattern
 */
object ProgressNavigation {

    const val PROGRESS_DASHBOARD_ROUTE = "progress_dashboard"
    const val EXERCISE_PROGRESS_ROUTE = "exercise_progress/{exerciseId}"
    const val SESSION_DETAIL_ROUTE = "session_detail/{sessionId}"

    const val EXERCISE_ID_ARG = "exerciseId"
    const val SESSION_ID_ARG = "sessionId"

    /** Build the route string for navigating to exercise progress. */
    fun exerciseProgressRoute(exerciseId: Long): String = "exercise_progress/$exerciseId"

    /** Build the route string for navigating to session detail. */
    fun sessionDetailRoute(sessionId: Long): String = "session_detail/$sessionId"
}

/**
 * Adds the progress dashboard screen to the navigation graph.
 */
fun NavGraphBuilder.progressDashboardScreen(
    onNavigateToSessionDetail: (sessionId: Long) -> Unit,
    onNavigateToExerciseProgress: (exerciseId: Long) -> Unit,
) {
    composable(route = ProgressNavigation.PROGRESS_DASHBOARD_ROUTE) {
        ProgressDashboardScreen(
            onNavigateToSessionDetail = onNavigateToSessionDetail,
            onNavigateToExerciseProgress = onNavigateToExerciseProgress,
        )
    }
}

/**
 * Adds the exercise progress (weight chart) screen to the navigation graph.
 */
fun NavGraphBuilder.exerciseProgressScreen(
    onNavigateBack: () -> Unit,
) {
    composable(
        route = ProgressNavigation.EXERCISE_PROGRESS_ROUTE,
        arguments = listOf(
            navArgument(ProgressNavigation.EXERCISE_ID_ARG) {
                type = NavType.LongType
            },
        ),
    ) {
        ExerciseProgressScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Adds the session detail screen to the navigation graph.
 */
fun NavGraphBuilder.sessionDetailScreen(
    onNavigateBack: () -> Unit,
) {
    composable(
        route = ProgressNavigation.SESSION_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(ProgressNavigation.SESSION_ID_ARG) {
                type = NavType.LongType
            },
        ),
    ) {
        SessionDetailScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * NavController extension for navigating to the progress dashboard.
 */
fun NavController.navigateToProgressDashboard() {
    navigate(ProgressNavigation.PROGRESS_DASHBOARD_ROUTE)
}

/**
 * NavController extension for navigating to exercise progress.
 */
fun NavController.navigateToExerciseProgress(exerciseId: Long) {
    navigate(ProgressNavigation.exerciseProgressRoute(exerciseId))
}

/**
 * NavController extension for navigating to session detail.
 */
fun NavController.navigateToSessionDetail(sessionId: Long) {
    navigate(ProgressNavigation.sessionDetailRoute(sessionId))
}
