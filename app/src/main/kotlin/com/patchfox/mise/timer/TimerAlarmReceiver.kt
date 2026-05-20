package com.patchfox.mise.timer

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.patchfox.mise.MainActivity
import com.patchfox.mise.R

class TimerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra(TimerChannels.EXTRA_TIMER_ID) ?: return
        val title = intent.getStringExtra(TimerChannels.EXTRA_TIMER_TITLE) ?: "Timer"
        val stepId = intent.getStringExtra(TimerChannels.EXTRA_STEP_ID)

        val deepLink = Intent(context, MainActivity::class.java).apply {
            action = "com.patchfox.mise.OPEN_TIMER_STEP"
            putExtra(TimerChannels.EXTRA_TIMER_ID, timerId)
            putExtra(TimerChannels.EXTRA_STEP_ID, stepId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, timerId.hashCode(), deepLink,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, TimerChannels.TIMER_EXPIRED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer done")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(timerId.hashCode(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted — silently swallow; the in-app foreground path
            // still plays an alarm sound when the chip flips to "done".
        }
    }
}
