package com.patchfox.mise.voice

/** A recognized hands-free voice command. */
enum class VoiceCommand {
    /** Previous cook step. */
    Back,
    /** Next cook step. */
    Forward,
    /** Start the current step's timer, if it has one. */
    StartTimer,
    /** Dismiss every timer that has finished. */
    StopTimers,
}

/**
 * UI-facing state of the voice listener, surfaced on the Cook screen's mic
 * indicator so the user knows whether they were heard without looking closely.
 */
sealed interface VoiceState {
    /** Voice control is off — disabled by the user or microphone permission missing. */
    data object Off : VoiceState

    /** Listening, waiting for the "go" activator word. */
    data object Idle : VoiceState

    /** Heard "go" — armed and waiting for a direction word. */
    data object Listening : VoiceState

    /** A command was just recognized and acted on — a brief confirmation. */
    data class Heard(val command: VoiceCommand) : VoiceState
}

/**
 * Voice state + the enable toggle, passed from the Cook screen down to the mic
 * indicator. [onToggle] turns voice on/off (and triggers the permission request
 * on first use).
 */
data class VoiceUi(
    val state: VoiceState,
    val enabled: Boolean,
    val onToggle: () -> Unit,
) {
    companion object {
        /** Inert value for previews and screenshot tests. */
        val OFF = VoiceUi(VoiceState.Off, enabled = false, onToggle = {})
    }
}

