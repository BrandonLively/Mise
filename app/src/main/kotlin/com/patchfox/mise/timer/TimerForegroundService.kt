package com.patchfox.mise.timer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.patchfox.mise.MainActivity
import com.patchfox.mise.R

class TimerForegroundService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val activeCount = intent?.getIntExtra(EXTRA_ACTIVE_COUNT, 1) ?: 1
        startForegroundCompat(activeCount)
        return START_STICKY
    }

    private fun startForegroundCompat(activeCount: Int) {
        val openApp = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification: Notification = NotificationCompat.Builder(this, TimerChannels.TIMER_RUNNING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("mise · $activeCount timer${if (activeCount == 1) "" else "s"} running")
            .setContentText("Tap to return to the cook flow.")
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )
    }

    override fun onDestroy() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 47
        private const val EXTRA_ACTIVE_COUNT = "active_count"

        fun ensureRunning(context: Context, activeCount: Int) {
            if (activeCount <= 0) return
            val intent = Intent(context, TimerForegroundService::class.java).apply {
                putExtra(EXTRA_ACTIVE_COUNT, activeCount)
            }
            context.startForegroundService(intent)
        }

        fun maybeStop(context: Context, activeCount: Int) {
            if (activeCount > 0) return
            context.stopService(Intent(context, TimerForegroundService::class.java))
        }
    }
}
