package com.deepreps.feature.workout.active.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.deepreps.core.ui.theme.DeepRepsTheme

/**
 * Dialog shown on app startup when a previously active workout session is detected.
 *
 * This handles three scenarios:
 * - Process death: Android killed the app while backgrounded. Session data intact in Room.
 * - App crash: App crashed mid-workout. Session data intact in Room up to last completed set.
 * - User abandoned: User force-closed app or phone died. Session data intact.
 *
 * The user must explicitly choose to resume or discard. There is no auto-discard.
 *
 * @param startedAtFormatted Human-readable timestamp when the session started.
 * @param completedSets Number of sets that were completed before interruption.
 * @param totalSets Total planned sets in the session.
 * @param onResume User chose to resume the workout.
 * @param onDiscard User chose to discard the workout. Shows a secondary confirmation.
 */
@Composable
fun ResumeOrDiscardDialog(
    startedAtFormatted: String,
    completedSets: Int,
    totalSets: Int,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
) {
    val colors = DeepRepsTheme.colors

    val progressText = remember(completedSets, totalSets) {
        if (totalSets > 0) {
            "$completedSets/$totalSets sets completed"
        } else {
            "No sets completed"
        }
    }

    AlertDialog(
        onDismissRequest = { /* Non-dismissable -- user must choose */ },
        title = {
            Text(text = "Unfinished Workout")
        },
        text = {
            Text(
                text = "You have an unfinished workout from $startedAtFormatted. " +
                    "$progressText. Would you like to resume or discard it?",
            )
        },
        confirmButton = {
            TextButton(onClick = onResume) {
                Text(
                    text = "Resume",
                    color = colors.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = "Discard",
                    color = colors.statusError,
                )
            }
        },
    )
}
