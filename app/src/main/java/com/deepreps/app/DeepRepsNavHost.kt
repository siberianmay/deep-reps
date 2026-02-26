package com.deepreps.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.aiplan.navigateToPlanReview
import com.deepreps.feature.aiplan.planReviewScreen
import com.deepreps.feature.exerciselibrary.navigation.exerciseDetailScreen
import com.deepreps.feature.exerciselibrary.navigation.exerciseListScreen
import com.deepreps.feature.exerciselibrary.navigation.exerciseSelectionScreen
import com.deepreps.feature.exerciselibrary.navigation.navigateToExerciseDetail
import com.deepreps.feature.exerciselibrary.navigation.navigateToExerciseSelection
import com.deepreps.feature.onboarding.navigation.OnboardingNavigation
import com.deepreps.feature.onboarding.navigation.onboardingScreen
import com.deepreps.feature.profile.navigation.settingsScreen
import com.deepreps.feature.progress.navigation.exerciseProgressScreen
import com.deepreps.feature.progress.navigation.navigateToExerciseProgress
import com.deepreps.feature.progress.navigation.navigateToSessionDetail
import com.deepreps.feature.progress.navigation.progressDashboardScreen
import com.deepreps.feature.progress.navigation.sessionDetailScreen
import com.deepreps.feature.templates.navigation.createTemplateScreen
import com.deepreps.feature.templates.navigation.editTemplateScreen
import com.deepreps.feature.templates.navigation.navigateToCreateTemplate
import com.deepreps.feature.templates.navigation.navigateToCreateTemplateFromWorkout
import com.deepreps.feature.templates.navigation.navigateToEditTemplate
import com.deepreps.feature.templates.navigation.navigateToTemplateList
import com.deepreps.feature.templates.navigation.templateListScreen
import com.deepreps.feature.workout.active.activeWorkoutScreen
import com.deepreps.feature.workout.active.navigateToActiveWorkout
import com.deepreps.feature.workout.setup.exerciseOrderScreen
import com.deepreps.feature.workout.setup.muscleGroupSelectorScreen
import com.deepreps.feature.workout.setup.navigateToExerciseOrder
import com.deepreps.feature.workout.setup.navigateToExerciseOrderFromTemplate
import com.deepreps.feature.workout.setup.navigateToMuscleGroupSelector
import com.deepreps.feature.workout.summary.navigateToWorkoutSummary
import com.deepreps.feature.workout.summary.workoutSummaryScreen

/**
 * Routes where the bottom navigation bar should be visible.
 *
 * The bar shows on the four top-level tab destinations plus their
 * immediate drill-in sub-screens. All other routes (onboarding,
 * workout setup flow, active workout, etc.) hide the bar.
 */
private val bottomBarVisibleRoutes = setOf(
    HOME_ROUTE,
    "exercise_list",
    "exercise_detail/{exerciseId}",
    "progress_dashboard",
    "exercise_progress/{exerciseId}",
    "session_detail/{sessionId}",
    PROFILE_SETTINGS_ROUTE,
    "template_list",
)

/**
 * Root navigation host for Deep Reps.
 *
 * Wraps the [NavHost] in a [Scaffold] with a Material 3 [NavigationBar].
 * The bottom bar is conditionally shown based on the current route.
 *
 * @param isOnboardingCompleted Determines whether to start at home or onboarding.
 * @param navigateToWorkoutSessionId If non-null, immediately navigate to the active
 *        workout screen to resume this session. Set by the recovery dialog.
 * @param onWorkoutNavigationConsumed Callback to clear the navigation trigger after consuming.
 */
@Suppress("LongMethod")
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
            navController.navigateToActiveWorkout(navigateToWorkoutSessionId)
            onWorkoutNavigationConsumed()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute by remember {
        derivedStateOf { navBackStackEntry?.destination?.route }
    }
    val shouldShowBottomBar by remember {
        derivedStateOf { currentRoute in bottomBarVisibleRoutes }
    }

    val colors = DeepRepsTheme.colors

    Scaffold(
        containerColor = colors.surfaceLowest,
        bottomBar = {
            AnimatedVisibility(
                visible = shouldShowBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                DeepRepsBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
        ) {
            // Onboarding
            onboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(OnboardingNavigation.ONBOARDING_ROUTE) { inclusive = true }
                    }
                },
            )

            // Home dashboard
            composable(route = HOME_ROUTE) {
                HomeScreen(
                    onStartWorkout = { navController.navigateToMuscleGroupSelector() },
                    onFromTemplate = { navController.navigateToTemplateList() },
                    onResumeWorkout = { sessionId ->
                        navController.navigateToActiveWorkout(sessionId)
                    },
                    onSessionDetail = { sessionId ->
                        navController.navigateToSessionDetail(sessionId)
                    },
                    onTemplateSelected = { templateId ->
                        navController.navigateToExerciseOrderFromTemplate(templateId)
                    },
                )
            }

            // Templates
            templateListScreen(
                onNavigateToWorkoutSetup = { templateId ->
                    navController.navigateToExerciseOrderFromTemplate(templateId)
                },
                onNavigateToCreateTemplate = { navController.navigateToCreateTemplate() },
                onNavigateToEditTemplate = { templateId ->
                    navController.navigateToEditTemplate(templateId)
                },
            )

            createTemplateScreen(
                onNavigateBack = { navController.popBackStack() },
                onTemplateSaved = { navController.popBackStack() },
            )

            editTemplateScreen(
                onNavigateBack = { navController.popBackStack() },
                onTemplateSaved = { navController.popBackStack() },
            )

            // Workout Setup
            muscleGroupSelectorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToExerciseSelection = { groupIds ->
                    navController.navigateToExerciseSelection(groupIds)
                },
            )

            exerciseSelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onSelectionConfirmed = { exerciseIds ->
                    navController.navigateToExerciseOrder(exerciseIds)
                },
                onNavigateToDetail = { exerciseId ->
                    navController.navigateToExerciseDetail(exerciseId)
                },
            )

            exerciseOrderScreen(
                onNavigateBack = { navController.popBackStack() },
                onGeneratePlan = { exerciseIds ->
                    navController.navigateToPlanReview(exerciseIds)
                },
            )

            // Plan Review
            planReviewScreen(
                onNavigateToWorkout = { sessionId ->
                    navController.navigateToActiveWorkout(sessionId)
                },
                onNavigateBack = { navController.popBackStack() },
            )

            // Active Workout
            activeWorkoutScreen(
                onNavigateToSummary = { sessionId ->
                    navController.navigateToWorkoutSummary(sessionId) {
                        popUpTo(HOME_ROUTE) { inclusive = false }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )

            // Workout Summary
            workoutSummaryScreen(
                onDismiss = {
                    navController.popBackStack(HOME_ROUTE, inclusive = false)
                },
                onNavigateToCreateTemplate = { exerciseIds ->
                    navController.navigateToCreateTemplateFromWorkout(exerciseIds)
                },
            )

            // Exercise Library (tab destination)
            exerciseListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetail = { exerciseId ->
                    navController.navigateToExerciseDetail(exerciseId)
                },
            )

            exerciseDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )

            // Progress (tab destination)
            progressDashboardScreen(
                onNavigateToSessionDetail = { sessionId ->
                    navController.navigateToSessionDetail(sessionId)
                },
                onNavigateToExerciseProgress = { exerciseId ->
                    navController.navigateToExerciseProgress(exerciseId)
                },
            )

            exerciseProgressScreen(
                onNavigateBack = { navController.popBackStack() },
            )

            sessionDetailScreen(
                onNavigateBack = { navController.popBackStack() },
            )

            // Profile / Settings
            settingsScreen()
        }
    }
}

/**
 * Material 3 bottom navigation bar for the four top-level tabs.
 *
 * @param currentRoute The currently active navigation route.
 * @param onTabSelected Callback invoked when the user taps a tab.
 */
@Composable
private fun DeepRepsBottomBar(
    currentRoute: String?,
    onTabSelected: (BottomNavItem) -> Unit,
) {
    val colors = DeepRepsTheme.colors

    NavigationBar(
        containerColor = colors.surfaceLow,
        contentColor = colors.onSurfacePrimary,
    ) {
        BottomNavItem.entries.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                    )
                },
                label = {
                    Text(text = item.label)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accentPrimary,
                    selectedTextColor = colors.accentPrimary,
                    unselectedIconColor = colors.onSurfaceTertiary,
                    unselectedTextColor = colors.onSurfaceTertiary,
                    indicatorColor = colors.accentPrimaryContainer,
                ),
            )
        }
    }
}
