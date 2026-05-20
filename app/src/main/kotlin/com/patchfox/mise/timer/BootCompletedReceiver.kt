package com.patchfox.mise.timer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * After a reboot, [TimerController] reconstructs alarms from the pending_alarm table the
 * first time it's instantiated (when Hilt resolves it). This receiver exists purely to
 * make sure the application class wakes up after BOOT_COMPLETED, which triggers Hilt
 * graph init and the TimerController's restore-from-disk pass.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject lateinit var timerController: TimerController

    override fun onReceive(context: Context, intent: Intent) {
        // Injecting timerController is enough — its init block runs the disk restore.
        // No further action needed here.
    }
}
