package com.patchfox.mise.timer

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.SystemClock
import androidx.core.content.getSystemService
import com.patchfox.mise.data.local.PendingAlarmDao
import com.patchfox.mise.data.local.PendingAlarmEntity
import com.patchfox.mise.data.state.StartedTimerRepository
import com.patchfox.mise.domain.model.CookCardId
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.ui.theme.RecipeColor
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Singleton
class TimerController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val pendingAlarmDao: PendingAlarmDao,
    private val startedTimerRepository: StartedTimerRepository,
) {
    data class RuntimeTimer(
        val id: String,
        val cookCardId: CookCardId,
        val stepId: String?,
        val title: String,
        val durationSeconds: Int,
        val remainingSeconds: Int,
        val expiresAtRealtimeMs: Long,
        val recipeEmoji: String?,
        val recipeColor: RecipeColor?,
    )

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _timers = MutableStateFlow<List<RuntimeTimer>>(emptyList())
    val timers: StateFlow<List<RuntimeTimer>> = _timers.asStateFlow()

    private var ringtone: Ringtone? = null
    private var hadDoneTimers = false

    init {
        scope.launch { restoreFromDisk() }
        scope.launch { runTick() }
    }

    suspend fun start(
        cookCardId: CookCardId,
        stepId: StepId,
        title: String,
        durationSeconds: Int,
        recipeEmoji: String?,
        recipeColor: RecipeColor?,
    ): String {
        val timerId = "timer-" + UUID.randomUUID().toString()
        val expiresAt = SystemClock.elapsedRealtime() + durationSeconds * 1000L
        val timer = RuntimeTimer(
            id = timerId,
            cookCardId = cookCardId,
            stepId = stepId.value,
            title = title,
            durationSeconds = durationSeconds,
            remainingSeconds = durationSeconds,
            expiresAtRealtimeMs = expiresAt,
            recipeEmoji = recipeEmoji,
            recipeColor = recipeColor,
        )

        pendingAlarmDao.upsert(
            PendingAlarmEntity(
                timerId = timerId,
                cookCardId = cookCardId.value,
                stepId = stepId.value,
                title = title,
                durationSeconds = durationSeconds,
                expiresAtRealtimeMs = expiresAt,
                recipeColor = recipeColor?.name,
            ),
        )

        scheduleAlarm(timer)
        _timers.update { it + timer }
        TimerForegroundService.ensureRunning(context, _timers.value.size)
        return timerId
    }

    suspend fun dismiss(timerId: String) {
        val timer = _timers.value.firstOrNull { it.id == timerId } ?: return
        cancelAlarm(timerId)
        pendingAlarmDao.delete(timerId)
        if (timer.stepId != null) {
            startedTimerRepository.clear(timer.cookCardId, StepId(timer.stepId))
        }
        _timers.update { list -> list.filterNot { it.id == timerId } }
        syncAlarmSound()
        TimerForegroundService.maybeStop(context, _timers.value.size)
    }

    /**
     * Tack on one extra minute. Cooking ranges ("15–17 minutes") mean the user
     * often wants a quick extension without re-arming a brand-new timer. Tapping
     * a done chip lands here; the existing chip stays, just with 60 fresh seconds.
     */
    suspend fun extendByOneMinute(timerId: String) {
        val timer = _timers.value.firstOrNull { it.id == timerId } ?: return
        cancelAlarm(timerId)
        val now = SystemClock.elapsedRealtime()
        // If the timer is still counting down, append the minute to its current
        // expiry. If it already expired, anchor the new minute to "now" so the
        // user sees a fresh 60s, not a minute partially eaten by elapsed time.
        val baseExpiry = if (timer.remainingSeconds > 0) timer.expiresAtRealtimeMs else now
        val newExpiresAt = baseExpiry + 60_000L
        val newRemaining = ((newExpiresAt - now) / 1000L).toInt().coerceAtLeast(0)
        val updated = timer.copy(
            expiresAtRealtimeMs = newExpiresAt,
            remainingSeconds = newRemaining,
        )
        pendingAlarmDao.upsert(
            PendingAlarmEntity(
                timerId = timer.id,
                cookCardId = timer.cookCardId.value,
                stepId = timer.stepId ?: "",
                title = timer.title,
                durationSeconds = timer.durationSeconds,
                expiresAtRealtimeMs = newExpiresAt,
                recipeColor = timer.recipeColor?.name,
            ),
        )
        scheduleAlarm(updated)
        _timers.update { list -> list.map { if (it.id == timerId) updated else it } }
        syncAlarmSound()
    }

    /** Mirrors the alarm-sound state to whether any timer is currently at 0. */
    private fun syncAlarmSound() {
        val anyDone = _timers.value.any { it.remainingSeconds == 0 }
        if (anyDone && !hadDoneTimers) startAlarmSound()
        else if (!anyDone && hadDoneTimers) stopAlarmSound()
        hadDoneTimers = anyDone
    }

    private fun startAlarmSound() {
        if (ringtone?.isPlaying == true) return
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) ?: return
        val r = RingtoneManager.getRingtone(context, uri) ?: return
        r.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            r.isLooping = true
        }
        ringtone = r
        runCatching { r.play() }
    }

    private fun stopAlarmSound() {
        ringtone?.runCatching { stop() }
        ringtone = null
    }

    private fun scheduleAlarm(timer: RuntimeTimer) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val intent = Intent(context, TimerAlarmReceiver::class.java).apply {
            putExtra(TimerChannels.EXTRA_TIMER_ID, timer.id)
            putExtra(TimerChannels.EXTRA_TIMER_TITLE, timer.title)
            putExtra(TimerChannels.EXTRA_STEP_ID, timer.stepId)
            putExtra(TimerChannels.EXTRA_RECIPE_COLOR, timer.recipeColor?.name)
        }
        val pi = PendingIntent.getBroadcast(
            context, timer.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, timer.expiresAtRealtimeMs, pi)
            } else {
                am.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, timer.expiresAtRealtimeMs, pi)
            }
        } catch (_: SecurityException) {
            am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timer.expiresAtRealtimeMs, pi)
        }
    }

    private fun cancelAlarm(timerId: String) {
        val am = context.getSystemService<AlarmManager>() ?: return
        val intent = Intent(context, TimerAlarmReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context, timerId.hashCode(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pi != null) am.cancel(pi)
    }

    private suspend fun restoreFromDisk() {
        val rows = pendingAlarmDao.getAll()
        val now = SystemClock.elapsedRealtime()
        val restored = rows.mapNotNull { row ->
            val remainingMs = row.expiresAtRealtimeMs - now
            val remainingSeconds = (remainingMs / 1000).toInt()
            RuntimeTimer(
                id = row.timerId,
                cookCardId = CookCardId(row.cookCardId),
                stepId = row.stepId,
                title = row.title,
                durationSeconds = row.durationSeconds,
                remainingSeconds = remainingSeconds.coerceAtLeast(0),
                expiresAtRealtimeMs = row.expiresAtRealtimeMs,
                recipeEmoji = null,
                recipeColor = row.recipeColor?.let { runCatching { RecipeColor.valueOf(it) }.getOrNull() },
            ).also { restoredTimer ->
                // Re-arm if still in the future; otherwise leave as "done" until dismissed.
                if (remainingMs > 0) scheduleAlarm(restoredTimer)
            }
        }
        if (restored.isNotEmpty()) {
            _timers.value = restored
            TimerForegroundService.ensureRunning(context, restored.size)
        }
    }

    private suspend fun runTick() {
        while (scope.isActive) {
            delay(1000L)
            val now = SystemClock.elapsedRealtime()
            _timers.update { list ->
                list.map { t ->
                    val rem = ((t.expiresAtRealtimeMs - now) / 1000L).toInt().coerceAtLeast(0)
                    if (rem == t.remainingSeconds) t else t.copy(remainingSeconds = rem)
                }
            }
            syncAlarmSound()
        }
    }
}
