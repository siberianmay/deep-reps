package com.deepreps.feature.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Root composable for the onboarding flow.
 *
 * Hosts 4 screens with animated transitions:
 * - Screen 0: Privacy & Consent
 * - Screen 1: Welcome
 * - Screen 2: Experience Level
 * - Screen 3: Unit Preference + Optional Profile
 */
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is OnboardingSideEffect.NavigateToMain -> onOnboardingComplete()
                is OnboardingSideEffect.ShowError -> {
                    // Error is shown via state; snackbar could be added later.
                }
            }
        }
    }

    BackHandler(enabled = state.currentStep > 0) {
        viewModel.onIntent(OnboardingIntent.PreviousStep)
    }

    OnboardingContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
internal fun OnboardingContent(
    state: OnboardingUiState,
    onIntent: (OnboardingIntent) -> Unit,
) {
    val colors = DeepRepsTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceLowest),
    ) {
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                val direction = if (targetState > initialState) 1 else -1
                (slideInHorizontally { it * direction } + fadeIn())
                    .togetherWith(slideOutHorizontally { -it * direction } + fadeOut())
            },
            label = "onboarding_screen_transition",
        ) { step ->
            when (step) {
                0 -> ConsentStepScreen(
                    analyticsConsent = state.analyticsConsent,
                    crashlyticsConsent = state.crashlyticsConsent,
                    onIntent = onIntent,
                )
                1 -> WelcomeStepScreen(
                    onIntent = onIntent,
                )
                2 -> ExperienceLevelStepScreen(
                    selectedLevel = state.experienceLevel,
                    onIntent = onIntent,
                )
                3 -> UnitAndProfileStepScreen(
                    state = state,
                    onIntent = onIntent,
                )
            }
        }
    }
}
