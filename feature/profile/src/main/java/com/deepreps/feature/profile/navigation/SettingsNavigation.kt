package com.deepreps.feature.profile.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.deepreps.feature.profile.SettingsScreen

/**
 * Navigation constants and graph builder extensions for the profile/settings feature.
 *
 * Route conventions per architecture.md Section 8.2:
 * - Route names use snake_case
 */
object SettingsNavigation {
    const val SETTINGS_ROUTE = "profile_settings"
}

/**
 * Adds the settings screen to the navigation graph.
 */
fun NavGraphBuilder.settingsScreen() {
    composable(route = SettingsNavigation.SETTINGS_ROUTE) {
        SettingsScreen()
    }
}

/**
 * NavController extension for navigating to the settings screen.
 */
fun NavController.navigateToSettings() {
    navigate(SettingsNavigation.SETTINGS_ROUTE)
}
