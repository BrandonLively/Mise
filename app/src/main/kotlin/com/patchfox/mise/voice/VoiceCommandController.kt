package com.patchfox.mise.voice

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import com.patchfox.mise.timer.TimerController
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.state.CookSessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService

private const val TAG = "VoiceCommand"

/**
 * Owns the offline (Vosk) speech recognizer for hands-free voice control.
 *
 * The listening lifecycle is self-managed: the mic stays open while voice is
 * [enabled] and RECORD_AUDIO is granted AND the user is either on Cook Phase 2
 * OR a timer alarm is currently ringing. The alarm clause lets "stop timer"
 * silence an alarm from any screen, not only Cook. The mic is closed otherwise.
 *
 * The Vosk [Model] is loaded once and kept for the process lifetime — only the
 * recognizer and microphone are released when listening stops.
 */
@Singleton
class VoiceCommandController @Inject constructor(
    @ApplicationContext private val context: Context,
    cookSession: CookSessionState,
    timerController: TimerController,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val stateMachine = VoiceCommandStateMachine()
    private val prefs = context.getSharedPreferences("voice_prefs", Context.MODE_PRIVATE)

    private val _state = MutableStateFlow<VoiceState>(VoiceState.Off)
    val state: StateFlow<VoiceState> = _state.asStateFlow()

    private val _commands = MutableSharedFlow<VoiceCommand>(extraBufferCapacity = 8)
    val commands: SharedFlow<VoiceCommand> = _commands.asSharedFlow()

    private val _enabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    /** User's opt-in preference for voice control — persisted across launches. */
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null
    private var armedTimeoutJob: Job? = null
    private var heardClearJob: Job? = null

    init {
        // Self-managed listening lifecycle. The mic stays open while voice is
        // enabled and permitted AND the user is either on Cook Phase 2 or has
        // a ringing alarm — the alarm clause lets "stop timer" silence an
        // alarm from any screen, not only Cook.
        scope.launch {
            combine(
                _enabled,
                cookSession.isCookScreenVisible,
                cookSession.phase,
                timerController.timers,
            ) { enabled, cookVisible, phase, timers ->
                val onCookPhase2 = cookVisible && phase == PhaseTab.Phase2
                val alarmRinging = timers.any { it.remainingSeconds == 0 }
                enabled && hasMicPermission() && (onCookPhase2 || alarmRinging)
            }
                .distinctUntilChanged()
                .collect { shouldListen -> if (shouldListen) start() else stop() }
        }
    }

    private fun hasMicPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    fun setEnabled(value: Boolean) {
        _enabled.value = value
        prefs.edit().putBoolean(KEY_ENABLED, value).apply()
    }

    /** Begins listening. Gating (enable / permission / context) is enforced by [init]. */
    private fun start() {
        if (speechService != null) return
        scope.launch {
            val loadedModel = model ?: withContext(Dispatchers.IO) {
                runCatching { Model(ensureModelDir().absolutePath) }
                    .onFailure { Log.e(TAG, "Vosk model load failed", it) }
                    .getOrNull()
            }?.also { model = it }

            if (loadedModel == null) {
                _state.value = VoiceState.Off
                return@launch
            }
            if (speechService != null) return@launch // started/stopped while loading

            val ok = runCatching {
                val rec = Recognizer(loadedModel, SAMPLE_RATE, GRAMMAR)
                recognizer = rec
                speechService = SpeechService(rec, SAMPLE_RATE).also {
                    it.startListening(recognitionListener)
                }
            }.onFailure { Log.e(TAG, "Could not start the recognizer", it) }.isSuccess

            _state.value = if (ok) VoiceState.Idle else VoiceState.Off
        }
    }

    /** Stops listening and releases the microphone. The loaded model is kept. */
    private fun stop() {
        armedTimeoutJob?.cancel()
        heardClearJob?.cancel()
        speechService?.let { runCatching { it.stop(); it.shutdown() } }
        speechService = null
        recognizer?.let { runCatching { it.close() } }
        recognizer = null
        stateMachine.reset()
        _state.value = VoiceState.Off
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onPartialResult(hypothesis: String?) = Unit
        override fun onResult(hypothesis: String?) = handleHypothesis(hypothesis)
        override fun onFinalResult(hypothesis: String?) = handleHypothesis(hypothesis)
        override fun onError(e: Exception?) {
            Log.e(TAG, "Recognition error", e)
        }
        override fun onTimeout() = Unit
    }

    private fun handleHypothesis(hypothesis: String?) {
        val text = hypothesis
            ?.let { runCatching { JSONObject(it).optString("text") }.getOrNull() }
            ?.trim()
            .orEmpty()
        if (text.isEmpty()) return

        val now = SystemClock.elapsedRealtime()
        val command = stateMachine.onResult(text, now)
        when {
            command != null -> {
                armedTimeoutJob?.cancel()
                _commands.tryEmit(command)
                _state.value = VoiceState.Heard(command)
                heardClearJob?.cancel()
                heardClearJob = scope.launch {
                    delay(HEARD_DISPLAY_MS)
                    if (speechService != null) _state.value = VoiceState.Idle
                }
            }
            stateMachine.isArmed(now) -> {
                _state.value = VoiceState.Listening
                armedTimeoutJob?.cancel()
                armedTimeoutJob = scope.launch {
                    delay(VoiceCommandStateMachine.DEFAULT_WINDOW_MS)
                    if (speechService != null && !stateMachine.isArmed(SystemClock.elapsedRealtime())) {
                        _state.value = VoiceState.Idle
                    }
                }
            }
        }
    }

    /** Copies the bundled model out of assets into internal storage once. */
    private fun ensureModelDir(): File {
        val dest = File(context.filesDir, "vosk-model")
        if (File(dest, "am/final.mdl").exists()) return dest
        copyAsset(ASSET_MODEL_DIR, dest)
        return dest
    }

    private fun copyAsset(assetPath: String, dest: File) {
        val children = context.assets.list(assetPath).orEmpty()
        if (children.isEmpty()) {
            dest.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                dest.outputStream().use { input.copyTo(it) }
            }
        } else {
            dest.mkdirs()
            for (child in children) copyAsset("$assetPath/$child", File(dest, child))
        }
    }

    private companion object {
        const val SAMPLE_RATE = 16_000.0f
        const val ASSET_MODEL_DIR = "vosk-model-en"
        const val KEY_ENABLED = "voice_enabled"
        const val HEARD_DISPLAY_MS = 1_200L

        /** Restricted grammar — the recognizer only ever decides between these. */
        const val GRAMMAR =
            """["go", "back", "forward", "next", "start", "stop", "timer", "[unk]"]"""
    }
}
