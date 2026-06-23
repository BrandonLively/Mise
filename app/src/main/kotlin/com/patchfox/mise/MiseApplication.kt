package com.patchfox.mise

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.FirebaseApp
import com.patchfox.mise.timer.TimerChannels
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MiseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        registerNotificationChannels()
    }

    private fun registerNotificationChannels() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val alarmAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val expired = NotificationChannel(
            TimerChannels.TIMER_EXPIRED,
            getString(R.string.timer_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.timer_channel_description)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 600, 200, 600, 200, 600)
            setSound(alarmSound, alarmAttrs)
            setBypassDnd(false)
        }

        val running = NotificationChannel(
            TimerChannels.TIMER_RUNNING,
            getString(R.string.timer_running_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.timer_running_channel_description)
            enableVibration(false)
            setSound(null, null)
        }

        val prep = NotificationChannel(
            TimerChannels.PREP_REMINDER,
            "Prep reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Reminders for do-ahead prep (marinate, thaw, ripen, etc.)"
            enableVibration(true)
        }

        nm.createNotificationChannels(listOf(expired, running, prep))
    }
}
