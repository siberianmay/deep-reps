package com.deepreps.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.deepreps.feature.exerciselibrary.navigation.exerciseDetailScreen
import com.deepreps.feature.exerciselibrary.navigation.exerciseListScreen
import com.deepreps.feature.exerciselibrary.navigation.exerciseSelectionScreen
import com.deepreps.feature.exerciselibrary.navigation.navigateToExerciseDetail
import com.deepreps.feature.exerciselibrary.navigation.navigateToExerciseSelection
import com.deepreps.feature.onboarding.navigation.OnboardingNavigation
import com.deepreps.feature.onboarding.navigation.onboardingScreen
import com.deepreps.feature.workout.active.activeWorkoutScreen
import com.deepreps.feature.workout.active.navigateToActiveWorkout
import com.deepreps.feature.workout.setup.WorkoutSetupNavigation
import com.deepreps.feature.workout.setup.exerciseOrderScreen
import com.deepreps.feature.workout.setup.muscleGroupSelectorScreen
import com.deepreps.feature.workout.setup.navigateToExerciseOrder
import com.deepreps.feature.workout.setup.navigateToMuscleGroupSelector

/**
 * Root navigation host for Deep Reps.
 *
 * Determines start destination based on onboarding completion state.
 * If onboarding not completed: starts at onboarding flow.
 * If completed: starts at main (home placeholder for now).
 *
 * @param navigateToWorkoutSessionId If non-null, immediately navigate to the active
 *        workout screen to resume this session. Set by the recovery dialog.
 * @param onWorkoutNavigationConsumed Callback to clear the navigation trigger after consuming.
 */
@Composable
fun DeepRepsNavHost(
    isOnboardingCompleted: Boolean,
    navigateToWorkoutSessionId: Long? = null,
    onWorkoutNavigationConsumed: () -> Unit = {},
) {
    val navController = rememberNavController()

    val startDestination = if (isOnboardingCompleted) {
        HOME_ROUTE
    } else {
        OnboardingNavigation.ONBOARDING_ROUTE
    }

    // Handle navigation to active workout from recovery dialog
    LaunchedEffect(navigateToWorkoutSessionId) {
        if (navigateToWorkoutSessionId != null) {
            navController.navigateToActiveWorkout()
            onWorkoutNavigationConsumed()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // Onboarding
        onboardingScreen(
            onOnboardingComplete = {
                navController.navigate(HOME_ROUTE) {
                    popUpTo(OnboardingNavigation.ONBOARDING_ROUTE) { inclusive = true }
                }
            },
        )

        // Home (placeholder until home feature is built)
        composable(route = HOME_ROUTE) {
            // TODO: Replace with actual HomeScreen composable
            HomePlaceholder(
                onStartWorkout = { navController.navigateToMuscleGroupSelector() },
            )
        }

        // Workout Setup
        muscleGroupSelectorScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToExerciseSelection = {
                navController.navigateToExerciseSelection()
            },
        )

        exerciseSelectionScreen(
            onNavigateBack = { navController.popBackStack() },
            onSelectionConfirmed = { exerciseIds ->
                // Navigate to exercise order screen
                // The ViewModel will receive the exercise IDs
                navController.navigateToExerciseOrder()
            },
            onNavigateToDetail = { exerciseId ->
                navController.navigateToExerciseDetail(exerciseId)
            },
        )

        exerciseOrderScreen(
            onNavigateBack = { navController.popBackStack() },
            onGeneratePlan = { exerciseIds ->
                // TODO: Navigate to PlanReview screen (Epic 6)
                // For now, pop back to home
                navController.popBackStack(HOME_ROUTE, inclusive = false)
            },
        )

        // Active Workout
        activeWorkoutScreen(
            onNavigateToSummary = { sessionId ->
                // TODO: Navigate to WorkoutSummary screen (Epic 11)
                navController.popBackStack(HOME_ROUTE, inclusive = false)
            },
            onNavigateBack = { navController.popBackStack() },
        )

        // Exercise Library
        exerciseListScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetail = { exerciseId ->
                navController.navigateToExerciseDetail(exerciseId)
            },
        )

        exerciseDetailScreen(
            onNavigateBack = { navController.popBackStack() },
        )
    }
}

private const val HOME_ROUTE = "home"
