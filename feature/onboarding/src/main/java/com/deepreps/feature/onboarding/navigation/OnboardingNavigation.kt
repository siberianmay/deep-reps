package com.deepreps.feature.onboarding.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.deepreps.feature.onboarding.OnboardingScreen

/**
 * Navigation constants and graph builder extensions for the onboarding feature.
 *
 * Route conventions per architecture.md Section 8.2: snake_case route names.
 */
object OnboardingNavigation {

    const val ONBOARDING_ROUTE = "onboarding"
}

/**
 * Adds the onboarding screen to the navigation graph.
 *
 * @param onOnboardingComplete Called when the user completes onboarding.
 *   The host should navigate to the main app and clear the onboarding back stack.
 */
fun NavGraphBuilder.onboardingScreen(
    onOnboardingComplete: () -> Unit,
) {
    composable(route = OnboardingNavigation.ONBOARDING_ROUTE) {
        OnboardingScreen(
            onOnboardingComplete = onOnboardingComplete,
        )
    }
}

/**
 * NavController extension for navigating to the onboarding screen.
 */
fun NavController.navigateToOnboarding() {
    navigate(OnboardingNavigation.ONBOARDING_ROUTE) {
        popUpTo(graph.startDestinationId) { inclusive = true }
    }
}
