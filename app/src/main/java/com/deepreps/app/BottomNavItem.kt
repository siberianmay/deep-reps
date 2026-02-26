package com.deepreps.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.deepreps.feature.exerciselibrary.navigation.ExerciseLibraryNavigation
import com.deepreps.feature.progress.navigation.ProgressNavigation

/**
 * Defines the four top-level destinations shown in the bottom navigation bar.
 *
 * Each entry carries its route, display label, and icon pair (selected/unselected).
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    Home(
        route = HOME_ROUTE,
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
    ),
    Library(
        route = ExerciseLibraryNavigation.EXERCISE_LIST_ROUTE,
        label = "Library",
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
    ),
    Progress(
        route = ProgressNavigation.PROGRESS_DASHBOARD_ROUTE,
        label = "Progress",
        selectedIcon = Icons.AutoMirrored.Filled.ShowChart,
        unselectedIcon = Icons.AutoMirrored.Outlined.ShowChart,
    ),
    Profile(
        route = PROFILE_SETTINGS_ROUTE,
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
    ),
}

internal const val HOME_ROUTE = "home"
internal const val PROFILE_SETTINGS_ROUTE = "profile_settings"
