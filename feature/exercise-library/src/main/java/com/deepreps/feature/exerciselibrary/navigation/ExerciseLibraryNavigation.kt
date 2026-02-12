package com.deepreps.feature.exerciselibrary.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.deepreps.feature.exerciselibrary.ExerciseDetailScreen
import com.deepreps.feature.exerciselibrary.ExerciseListScreen
import com.deepreps.feature.exerciselibrary.ExerciseSelectionScreen

/**
 * Navigation constants and graph builder extensions for the exercise library feature.
 *
 * Route conventions per architecture.md Section 8.2:
 * - Route names use snake_case
 * - Arguments are embedded in the route pattern
 */
object ExerciseLibraryNavigation {

    const val EXERCISE_LIST_ROUTE = "exercise_list"
    const val EXERCISE_DETAIL_ROUTE = "exercise_detail/{exerciseId}"
    const val EXERCISE_SELECTION_ROUTE = "exercise_selection"

    /** Navigation argument key for exercise ID. */
    const val EXERCISE_ID_ARG = "exerciseId"

    /** Build the route string for navigating to exercise detail with a specific ID. */
    fun exerciseDetailRoute(exerciseId: Long): String = "exercise_detail/$exerciseId"
}

/**
 * Adds the exercise list screen to the navigation graph.
 */
fun NavGraphBuilder.exerciseListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (exerciseId: Long) -> Unit,
) {
    composable(route = ExerciseLibraryNavigation.EXERCISE_LIST_ROUTE) {
        ExerciseListScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
}

/**
 * Adds the exercise detail screen to the navigation graph.
 */
fun NavGraphBuilder.exerciseDetailScreen(
    onNavigateBack: () -> Unit,
) {
    composable(
        route = ExerciseLibraryNavigation.EXERCISE_DETAIL_ROUTE,
        arguments = listOf(
            navArgument(ExerciseLibraryNavigation.EXERCISE_ID_ARG) {
                type = NavType.LongType
            },
        ),
    ) {
        ExerciseDetailScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Adds the exercise selection screen to the navigation graph.
 */
fun NavGraphBuilder.exerciseSelectionScreen(
    onNavigateBack: () -> Unit,
    onSelectionConfirmed: (Set<Long>) -> Unit,
    onNavigateToDetail: (exerciseId: Long) -> Unit,
) {
    composable(route = ExerciseLibraryNavigation.EXERCISE_SELECTION_ROUTE) {
        ExerciseSelectionScreen(
            onNavigateBack = onNavigateBack,
            onSelectionConfirmed = onSelectionConfirmed,
            onNavigateToDetail = onNavigateToDetail,
        )
    }
}

/**
 * NavController extension for navigating to the exercise list screen.
 */
fun NavController.navigateToExerciseList() {
    navigate(ExerciseLibraryNavigation.EXERCISE_LIST_ROUTE)
}

/**
 * NavController extension for navigating to the exercise detail screen.
 */
fun NavController.navigateToExerciseDetail(exerciseId: Long) {
    navigate(ExerciseLibraryNavigation.exerciseDetailRoute(exerciseId))
}

/**
 * NavController extension for navigating to the exercise selection screen.
 */
fun NavController.navigateToExerciseSelection() {
    navigate(ExerciseLibraryNavigation.EXERCISE_SELECTION_ROUTE)
}
