package com.example.motivationcalendarapi.notifications

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat

class WorkoutTimerNotificationService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var startTimeMillis: Long = 0L
    private var elapsedOnPauseSeconds: Int = 0
    private var running: Boolean = false

    private val tick = object : Runnable {
        override fun run() {
            if (running) {
                val elapsed = ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(0)
                startForeground(
                    NotificationType.WORKOUT_ACTIVE.id,
                    NotificationHelper.buildWorkoutActiveNotification(applicationContext, elapsed)
                )
            }
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        handler.post(tick)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START, ACTION_RESUME -> {
                elapsedOnPauseSeconds = intent.getIntExtra(EXTRA_ELAPSED_SECONDS, elapsedOnPauseSeconds).coerceAtLeast(0)
                startTimeMillis = System.currentTimeMillis() - elapsedOnPauseSeconds * 1000L
                running = true
                startForeground(
                    NotificationType.WORKOUT_ACTIVE.id,
                    NotificationHelper.buildWorkoutActiveNotification(this, elapsedOnPauseSeconds)
                )
            }
            ACTION_PAUSE -> {
                elapsedOnPauseSeconds = intent.getIntExtra(EXTRA_ELAPSED_SECONDS, currentElapsedSeconds()).coerceAtLeast(0)
                running = false
                startForeground(
                    NotificationType.WORKOUT_ACTIVE.id,
                    NotificationHelper.buildWorkoutActiveNotification(this, elapsedOnPauseSeconds)
                )
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun currentElapsedSeconds(): Int {
        return if (running && startTimeMillis > 0L) {
            ((System.currentTimeMillis() - startTimeMillis) / 1000L).toInt().coerceAtLeast(0)
        } else {
            elapsedOnPauseSeconds
        }
    }

    companion object {
        private const val ACTION_START = "com.example.motivationcalendarapi.notifications.WORKOUT_START"
        private const val ACTION_RESUME = "com.example.motivationcalendarapi.notifications.WORKOUT_RESUME"
        private const val ACTION_PAUSE = "com.example.motivationcalendarapi.notifications.WORKOUT_PAUSE"
        private const val ACTION_STOP = "com.example.motivationcalendarapi.notifications.WORKOUT_STOP"
        private const val EXTRA_ELAPSED_SECONDS = "extra_elapsed_seconds"

        fun start(context: Context, elapsedSeconds: Int, running: Boolean) {
            val action = if (running) ACTION_START else ACTION_PAUSE
            val intent = Intent(context.applicationContext, WorkoutTimerNotificationService::class.java)
                .setAction(action)
                .putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun update(context: Context, elapsedSeconds: Int, running: Boolean) {
            val action = if (running) ACTION_RESUME else ACTION_PAUSE
            val intent = Intent(context.applicationContext, WorkoutTimerNotificationService::class.java)
                .setAction(action)
                .putExtra(EXTRA_ELAPSED_SECONDS, elapsedSeconds)
            ContextCompat.startForegroundService(context.applicationContext, intent)
        }

        fun stop(context: Context) {
            context.applicationContext.startService(
                Intent(context.applicationContext, WorkoutTimerNotificationService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
