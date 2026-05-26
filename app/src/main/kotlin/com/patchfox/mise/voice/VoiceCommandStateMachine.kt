package com.patchfox.mise.voice

/**
 * Pure, deterministic activator/command logic for voice control.
 *
 * Navigation: the word "go" arms a command window; a direction word ("back",
 * "forward", or "next") spoken within that window emits a [VoiceCommand].
 * Handles "go back" as one phrase or as "go" / "back" across two phrases.
 *
 * Timers: "start timer" and "stop timer" are self-contained two-word phrases —
 * distinctive enough not to need the "go" activator — and fire immediately.
 *
 * Time is passed in explicitly rather than read from a clock, so the logic is
 * fully unit-testable.
 */
class VoiceCommandStateMachine(
    private val windowMs: Long = DEFAULT_WINDOW_MS,
) {
    /** Monotonic timestamp until which a command will be accepted; 0 = not armed. */
    private var armedUntil: Long = 0L

    /** True while a "go" activator is still waiting for a direction word. */
    fun isArmed(nowMs: Long): Boolean = nowMs < armedUntil

    /**
     * Feeds one recognized phrase (space-separated tokens from the restricted
     * grammar). Returns the first command the phrase completed, otherwise null.
     * Tokens outside the grammar are ignored.
     */
    fun onResult(text: String, nowMs: Long): VoiceCommand? {
        var result: VoiceCommand? = null
        var prev: String? = null
        for (raw in text.lowercase().split(' ')) {
            val token = raw.trim()
            when (token) {
                "go" -> armedUntil = nowMs + windowMs
                "back" -> if (nowMs < armedUntil) {
                    armedUntil = 0L
                    if (result == null) result = VoiceCommand.Back
                }
                "forward", "next" -> if (nowMs < armedUntil) {
                    armedUntil = 0L
                    if (result == null) result = VoiceCommand.Forward
                }
                "timer" -> when (prev) {
                    "start" -> if (result == null) result = VoiceCommand.StartTimer
                    "stop" -> if (result == null) result = VoiceCommand.StopTimers
                }
            }
            if (token.isNotEmpty()) prev = token
        }
        return result
    }

    /** Drops any pending armed window — call when listening stops. */
    fun reset() {
        armedUntil = 0L
    }

    companion object {
        const val DEFAULT_WINDOW_MS = 4_000L
    }
}
