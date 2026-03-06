package com.deepreps.feature.templates.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.deepreps.feature.templates.CreateTemplateScreen
import com.deepreps.feature.templates.TemplateListScreen

/**
 * Navigation constants and graph builder extensions for the templates feature.
 *
 * Route conventions per architecture.md Section 8.2:
 * - Route names use snake_case
 * - Arguments are embedded in the route pattern
 */
object TemplateNavigation {

    const val TEMPLATE_LIST_ROUTE = "template_list"
    const val CREATE_TEMPLATE_ROUTE = "create_template"
    const val EDIT_TEMPLATE_ROUTE = "edit_template/{templateId}"

    const val TEMPLATE_ID_ARG = "templateId"
    const val EXERCISE_IDS_ARG = "exerciseIds"

    /** Build the route string for navigating to edit a specific template. */
    fun editTemplateRoute(templateId: Long): String = "edit_template/$templateId"

    /** Build the route for creating a template pre-populated from workout exercise IDs. */
    fun createFromWorkoutRoute(exerciseIds: List<Long>): String {
        val idsParam = exerciseIds.joinToString(",")
        return "create_template?$EXERCISE_IDS_ARG=$idsParam"
    }
}

/**
 * Adds the template list screen to the navigation graph.
 */
fun NavGraphBuilder.templateListScreen(
    onNavigateToWorkoutSetup: (templateId: Long) -> Unit,
    onNavigateToCreateTemplate: () -> Unit,
    onNavigateToEditTemplate: (templateId: Long) -> Unit,
) {
    composable(route = TemplateNavigation.TEMPLATE_LIST_ROUTE) {
        TemplateListScreen(
            onNavigateToWorkoutSetup = onNavigateToWorkoutSetup,
            onNavigateToCreateTemplate = onNavigateToCreateTemplate,
            onNavigateToEditTemplate = onNavigateToEditTemplate,
        )
    }
}

/**
 * Adds the create template screen to the navigation graph.
 *
 * Supports optional exerciseIds query parameter for the "Save as Template" flow
 * from workout summary. Also supports returning exercise selections from picker.
 *
 * @param exerciseResultKey The SavedStateHandle key for exercise picker results.
 */
fun NavGraphBuilder.createTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: (message: String) -> Unit,
    onAddExercises: (existingExerciseIds: List<Long>) -> Unit,
    exerciseResultKey: String = "",
) {
    composable(
        route = "${TemplateNavigation.CREATE_TEMPLATE_ROUTE}" +
            "?${TemplateNavigation.EXERCISE_IDS_ARG}" +
            "={${TemplateNavigation.EXERCISE_IDS_ARG}}",
        arguments = listOf(
            navArgument(TemplateNavigation.EXERCISE_IDS_ARG) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        CreateTemplateScreen(
            onNavigateBack = onNavigateBack,
            onTemplateSaved = onTemplateSaved,
            onAddExercises = onAddExercises,
            selectedExerciseResult = {
                if (exerciseResultKey.isNotEmpty()) {
                    backStackEntry.savedStateHandle.get<LongArray>(exerciseResultKey)
                } else {
                    null
                }
            },
            clearExerciseResult = {
                if (exerciseResultKey.isNotEmpty()) {
                    backStackEntry.savedStateHandle.remove<LongArray>(exerciseResultKey)
                }
            },
        )
    }
}

/**
 * Adds the edit template screen to the navigation graph.
 *
 * @param exerciseResultKey The SavedStateHandle key for exercise picker results.
 */
fun NavGraphBuilder.editTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: (message: String) -> Unit,
    onAddExercises: (existingExerciseIds: List<Long>) -> Unit,
    exerciseResultKey: String = "",
) {
    composable(
        route = TemplateNavigation.EDIT_TEMPLATE_ROUTE,
        arguments = listOf(
            navArgument(TemplateNavigation.TEMPLATE_ID_ARG) {
                type = NavType.LongType
            },
        ),
    ) { backStackEntry ->
        CreateTemplateScreen(
            onNavigateBack = onNavigateBack,
            onTemplateSaved = onTemplateSaved,
            onAddExercises = onAddExercises,
            selectedExerciseResult = {
                if (exerciseResultKey.isNotEmpty()) {
                    backStackEntry.savedStateHandle.get<LongArray>(exerciseResultKey)
                } else {
                    null
                }
            },
            clearExerciseResult = {
                if (exerciseResultKey.isNotEmpty()) {
                    backStackEntry.savedStateHandle.remove<LongArray>(exerciseResultKey)
                }
            },
        )
    }
}

/**
 * NavController extension for navigating to the template list.
 */
fun NavController.navigateToTemplateList() {
    navigate(TemplateNavigation.TEMPLATE_LIST_ROUTE)
}

/**
 * NavController extension for navigating to create a new template.
 */
fun NavController.navigateToCreateTemplate() {
    navigate(TemplateNavigation.CREATE_TEMPLATE_ROUTE)
}

/**
 * NavController extension for navigating to create a template from workout exercises.
 */
fun NavController.navigateToCreateTemplateFromWorkout(exerciseIds: List<Long>) {
    navigate(TemplateNavigation.createFromWorkoutRoute(exerciseIds))
}

/**
 * NavController extension for navigating to edit an existing template.
 */
fun NavController.navigateToEditTemplate(templateId: Long) {
    navigate(TemplateNavigation.editTemplateRoute(templateId))
}
