package com.deepreps.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.deepreps.app.MainActivity
import com.deepreps.app.R
import com.deepreps.core.data.timer.RestTimerManager
import com.deepreps.core.data.timer.RestTimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps the workout alive when the app is backgrounded.
 *
 * Responsibilities:
 * - Persistent notification showing elapsed workout time
 * - Rest timer countdown in notification when active
 * - Quick actions: Pause, Resume (via PendingIntent)
 * - Keeps the process alive so timers remain accurate even in Doze
 *
 * Lifecycle:
 * - Started when a workout session transitions to Active
 * - Stopped when the session is Completed, Discarded, or the user kills the service
 *
 * The service does NOT own any workout state. It reads from [RestTimerManager] (singleton)
 * and receives elapsed time updates via extras in the start command.
 */
@AndroidEntryPoint
class WorkoutForegroundService : Service() {

    @Inject
    lateinit var restTimerManager: RestTimerManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var notificationUpdateJob: Job? = null
    private var startedAtElapsedRealtime: Long = 0L
    private var pausedDurationSeconds: Long = 0L
    private var isPaused: Boolean = false

    companion object {
        const val CHANNEL_ID = "deep_reps_workout_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.deepreps.action.START_WORKOUT"
        const val ACTION_PAUSE = "com.deepreps.action.PAUSE_WORKOUT"
        const val ACTION_RESUME = "com.deepreps.action.RESUME_WORKOUT"
        const val ACTION_STOP = "com.deepreps.action.STOP_WORKOUT"

        const val EXTRA_STARTED_AT = "extra_started_at"
        const val EXTRA_PAUSED_DURATION = "extra_paused_duration"

        fun startService(context: Context, startedAtMillis: Long, pausedDurationSeconds: Long) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_STARTED_AT, startedAtMillis)
                putExtra(EXTRA_PAUSED_DURATION, pausedDurationSeconds)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, WorkoutForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val startedAtMillis = intent.getLongExtra(EXTRA_STARTED_AT, System.currentTimeMillis())
                pausedDurationSeconds = intent.getLongExtra(EXTRA_PAUSED_DURATION, 0L)
                isPaused = false

                // Anchor elapsed time
                val elapsedSoFar = (System.currentTimeMillis() - startedAtMillis) / 1000 - pausedDurationSeconds
                startedAtElapsedRealtime = SystemClock.elapsedRealtime() - (elapsedSoFar * 1000)

                startForeground(NOTIFICATION_ID, buildNotification(elapsedSoFar.coerceAtLeast(0), null))
                startNotificationUpdates()
            }

            ACTION_PAUSE -> {
                isPaused = true
                notificationUpdateJob?.cancel()
                // Update notification to show paused state
                updateNotification("Paused", null)
            }

            ACTION_RESUME -> {
                isPaused = false
                startNotificationUpdates()
            }

            ACTION_STOP -> {
                stopNotificationUpdates()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    // --- Notification ---

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Active Workout",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "Shows workout timer and rest countdown"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(
        elapsedSeconds: Long,
        restTimerState: RestTimerState?,
    ): Notification {
        val contentText = buildNotificationText(elapsedSeconds, restTimerState)

        // Tap notification to return to the app
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingContent = PendingIntent.getActivity(
            this,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // Pause action
        val pauseIntent = Intent(this, WorkoutForegroundService::class.java).apply {
            action = if (isPaused) ACTION_RESUME else ACTION_PAUSE
        }
        val pausePending = PendingIntent.getService(
            this,
            1,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val pauseActionLabel = if (isPaused) "Resume" else "Pause"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Workout in progress")
            .setContentText(contentText)
            .setContentIntent(pendingContent)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .addAction(0, pauseActionLabel, pausePending)
            .build()
    }

    private fun buildNotificationText(
        elapsedSeconds: Long,
        restTimerState: RestTimerState?,
    ): String {
        val timeStr = formatElapsed(elapsedSeconds)
        return if (restTimerState != null && restTimerState.isActive) {
            val restStr = "${restTimerState.remainingSeconds}s rest"
            "$timeStr | $restStr"
        } else {
            timeStr
        }
    }

    @Suppress("UnusedParameter")
    private fun updateNotification(text: String, restTimerState: RestTimerState?) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("Workout in progress")
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_WORKOUT)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun startNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = serviceScope.launch {
            while (isActive && !isPaused) {
                val elapsedMillis = SystemClock.elapsedRealtime() - startedAtElapsedRealtime
                val elapsedSec = (elapsedMillis / 1000) - pausedDurationSeconds
                val restState = restTimerManager.state.value

                val notification = buildNotification(
                    elapsedSeconds = elapsedSec.coerceAtLeast(0),
                    restTimerState = if (restState.isActive) restState else null,
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(NOTIFICATION_ID, notification)

                delay(1_000L)
            }
        }
    }

    private fun stopNotificationUpdates() {
        notificationUpdateJob?.cancel()
        notificationUpdateJob = null
    }

    private fun formatElapsed(totalSeconds: Long): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }
}
