package com.patchfox.mise.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.patchfox.mise.MainActivity
import com.patchfox.mise.R
import com.patchfox.mise.timer.TimerChannels

/** Posts a do-ahead prep reminder notification when its scheduled alarm fires. */
class PrepReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(TimerChannels.EXTRA_PREP_TITLE) ?: "Prep reminder"
        val text = intent.getStringExtra(TimerChannels.EXTRA_PREP_TEXT).orEmpty()

        val open = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pi = PendingIntent.getActivity(
            context, title.hashCode(), open,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(context, TimerChannels.PREP_REMINDER)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pi)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(("prep-$title").hashCode(), notification)
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted — drop silently.
        }
    }
}
