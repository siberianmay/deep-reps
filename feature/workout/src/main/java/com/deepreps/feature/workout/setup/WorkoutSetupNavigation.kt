package com.deepreps.feature.workout.setup

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

/**
 * Navigation constants and graph builder extensions for the workout setup flow.
 *
 * Flow: MuscleGroupSelector -> ExercisePicker -> ExerciseOrder -> PlanReview
 * Alternative: TemplateList -> ExerciseOrder -> PlanReview
 */
object WorkoutSetupNavigation {

    const val MUSCLE_GROUP_SELECTOR_ROUTE = "workout_setup_muscle_groups"
    const val EXERCISE_IDS_ARG = "exerciseIds"
    const val TEMPLATE_ID_ARG = "templateId"
    const val EXERCISE_ORDER_ROUTE =
        "workout_setup_exercise_order/{$EXERCISE_IDS_ARG}?$TEMPLATE_ID_ARG={$TEMPLATE_ID_ARG}"

    fun createExerciseOrderRoute(exerciseIds: Collection<Long>): String =
        "workout_setup_exercise_order/${exerciseIds.joinToString(",")}"

    fun createExerciseOrderRouteFromTemplate(templateId: Long): String =
        "workout_setup_exercise_order/_?$TEMPLATE_ID_ARG=$templateId"
}

/**
 * Adds the muscle group selector screen to the navigation graph.
 */
fun NavGraphBuilder.muscleGroupSelectorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToExerciseSelection: (groupIds: List<Long>) -> Unit,
    @Suppress("UnusedParameter") viewModel: () -> WorkoutSetupViewModel = {
        throw IllegalStateException("Provide shared ViewModel")
    },
) {
    composable(route = WorkoutSetupNavigation.MUSCLE_GROUP_SELECTOR_ROUTE) {
        val vm: WorkoutSetupViewModel = hiltViewModel()
        val state by vm.state.collectAsStateWithLifecycle()

        MuscleGroupSelectorScreen(
            selectedGroups = state.selectedGroups,
            onToggleGroup = { vm.onIntent(WorkoutSetupIntent.ToggleGroup(it)) },
            onNextClicked = { onNavigateToExerciseSelection(vm.getSelectedGroupIds()) },
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * Adds the exercise order screen to the navigation graph.
 */
fun NavGraphBuilder.exerciseOrderScreen(
    onNavigateBack: () -> Unit,
    onGeneratePlan: (exerciseIds: List<Long>) -> Unit,
) {
    composable(
        route = WorkoutSetupNavigation.EXERCISE_ORDER_ROUTE,
        arguments = listOf(
            navArgument(WorkoutSetupNavigation.EXERCISE_IDS_ARG) {
                type = NavType.StringType
            },
            navArgument(WorkoutSetupNavigation.TEMPLATE_ID_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) {
        val vm: WorkoutSetupViewModel = hiltViewModel()
        val state by vm.state.collectAsStateWithLifecycle()

        ExerciseOrderScreen(
            exercises = state.selectedExercises,
            isFromTemplate = state.isFromTemplate,
            templateName = state.templateName,
            onMoveExercise = { from, to ->
                vm.onIntent(WorkoutSetupIntent.MoveExercise(from, to))
            },
            onGeneratePlan = {
                val exerciseIds = state.selectedExercises.map { it.exerciseId }
                onGeneratePlan(exerciseIds)
            },
            onNavigateBack = onNavigateBack,
        )
    }
}

/**
 * NavController extension for navigating to the muscle group selector.
 */
fun NavController.navigateToMuscleGroupSelector() {
    navigate(WorkoutSetupNavigation.MUSCLE_GROUP_SELECTOR_ROUTE)
}

/**
 * NavController extension for navigating to the exercise order screen.
 */
fun NavController.navigateToExerciseOrder(exerciseIds: Collection<Long>) {
    navigate(WorkoutSetupNavigation.createExerciseOrderRoute(exerciseIds))
}

/**
 * NavController extension for navigating to exercise order from a template.
 */
fun NavController.navigateToExerciseOrderFromTemplate(templateId: Long) {
    navigate(WorkoutSetupNavigation.createExerciseOrderRouteFromTemplate(templateId))
}
