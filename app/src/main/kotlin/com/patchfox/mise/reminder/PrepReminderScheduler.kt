package com.patchfox.mise.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.edit
import androidx.core.content.getSystemService
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.timer.TimerChannels
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * Schedules a local notification for every do-ahead prep step on the current card
 * (marinade, thaw, ripen, …), derived from each `recipe.prepSchedule` offset relative to
 * `cookDate`. Fire times are clamped into a 10:00–20:00 window so the user isn't pinged at
 * inconvenient hours. Reuses AlarmManager (RTC_WAKEUP) like the cook timers.
 *
 * Idempotent: each [schedule] cancels everything previously scheduled, then re-arms the
 * future ones. (Alarms don't survive a reboot — they re-arm the next time the app opens.)
 */
@Singleton
class PrepReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val prefs = context.getSharedPreferences("prep_reminders", Context.MODE_PRIVATE)

    fun schedule(card: CookCard) {
        val am = context.getSystemService<AlarmManager>() ?: return

        // Cancel everything from a previous schedule pass.
        prefs.getStringSet(KEY_CODES, emptySet()).orEmpty().forEach { code ->
            code.toIntOrNull()?.let { cancel(am, it) }
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val newCodes = mutableSetOf<String>()
        card.recipes.forEach { recipe ->
            recipe.prepSchedule.forEachIndexed { idx, step ->
                val triggerAt = triggerMillis(card.cookDate, step.offset) ?: return@forEachIndexed
                if (triggerAt <= now) return@forEachIndexed
                val code = "${card.id.value}|${recipe.id.value}|$idx".hashCode()
                val title = "${recipe.emoji} ${step.label}".trim()
                val text = step.tasks.joinToString(" ").ifBlank { step.label }
                scheduleAlarm(am, code, triggerAt, title, text)
                newCodes += code.toString()
            }
        }
        prefs.edit { putStringSet(KEY_CODES, newCodes) }
        Log.d(TAG, "Scheduled ${newCodes.size} prep reminder(s) for card ${card.id.value}")
    }

    private fun scheduleAlarm(am: AlarmManager, requestCode: Int, triggerAt: Long, title: String, text: String) {
        val intent = Intent(context, PrepReminderReceiver::class.java).apply {
            putExtra(TimerChannels.EXTRA_PREP_TITLE, title)
            putExtra(TimerChannels.EXTRA_PREP_TEXT, text)
        }
        val pi = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            am.set(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancel(am: AlarmManager, requestCode: Int) {
        val pi = PendingIntent.getBroadcast(
            context, requestCode, Intent(context, PrepReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pi != null) am.cancel(pi)
    }

    /**
     * Fire time (epoch ms) for an [offset] relative to the cook day, clamped into the
     * [WINDOW_START_HOUR]..[WINDOW_END_HOUR] window. Returns null for unrecognized offsets.
     * T-Xh offsets are measured back from an assumed [COOK_START_HOUR] cook start.
     */
    private fun triggerMillis(cookDate: LocalDate, offset: String): Long? {
        val tz = TimeZone.currentSystemDefault()
        val o = offset.trim().lowercase()
        val base: LocalDateTime = when {
            o.startsWith("night") -> cookDate.minus(1, DateTimeUnit.DAY).atTime(19, 0)
            o.startsWith("morning") -> cookDate.atTime(9, 0)
            o.startsWith("same") -> cookDate.atTime(12, 0)
            o.startsWith("t-") -> {
                val hours = o.removePrefix("t-").removeSuffix("h").trim().toIntOrNull() ?: return null
                cookDate.atTime(COOK_START_HOUR, 0).toInstant(tz)
                    .minus(hours.toLong(), DateTimeUnit.HOUR)
                    .toLocalDateTime(tz)
            }
            else -> return null
        }
        return clampToWindow(base).toInstant(tz).toEpochMilliseconds()
    }

    private fun clampToWindow(dt: LocalDateTime): LocalDateTime = when {
        dt.hour < WINDOW_START_HOUR -> LocalDateTime(dt.year, dt.monthNumber, dt.dayOfMonth, WINDOW_START_HOUR, 0)
        dt.hour >= WINDOW_END_HOUR -> LocalDateTime(dt.year, dt.monthNumber, dt.dayOfMonth, WINDOW_END_HOUR, 0)
        else -> dt
    }

    private companion object {
        const val TAG = "PrepReminders"
        const val KEY_CODES = "scheduled_codes"
        const val COOK_START_HOUR = 10
        const val WINDOW_START_HOUR = 10
        const val WINDOW_END_HOUR = 20
    }
}
