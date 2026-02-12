package com.deepreps.app

import android.os.Bundle
import android.text.format.DateUtils
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.deepreps.core.domain.repository.OnboardingRepository
import com.deepreps.core.ui.theme.DeepRepsTheme
import com.deepreps.feature.workout.active.components.ResumeOrDiscardDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity host for the Deep Reps Compose navigation graph.
 *
 * On cold start:
 * 1. Checks onboarding_completed flag to determine start destination.
 * 2. Runs session recovery check (cleanup stale, detect crashed/abandoned).
 * 3. Shows "Resume or Discard?" dialog if a recoverable session is found.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var onboardingRepository: OnboardingRepository

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isOnboardingCompleted = onboardingRepository.isOnboardingCompleted()

        setContent {
            DeepRepsTheme {
                val mainState by mainViewModel.state.collectAsStateWithLifecycle()

                // Show recovery dialog if a recoverable session was found
                val recoverableSession = mainState.recoverableSession
                if (recoverableSession != null) {
                    val startedAtFormatted = DateUtils.getRelativeTimeSpanString(
                        recoverableSession.startedAt,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                    ).toString()

                    ResumeOrDiscardDialog(
                        startedAtFormatted = startedAtFormatted,
                        completedSets = 0, // Will be populated when we have the data
                        totalSets = 0,
                        onResume = mainViewModel::onResumeSession,
                        onDiscard = mainViewModel::onDiscardSession,
                    )
                }

                DeepRepsNavHost(
                    isOnboardingCompleted = isOnboardingCompleted,
                    navigateToWorkoutSessionId = mainState.navigateToWorkout,
                    onWorkoutNavigationConsumed = mainViewModel::onNavigationConsumed,
                )
            }
        }
    }
}
