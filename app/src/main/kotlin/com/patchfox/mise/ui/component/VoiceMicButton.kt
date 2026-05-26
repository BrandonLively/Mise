package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.voice.VoiceCommand
import com.patchfox.mise.voice.VoiceState
import com.patchfox.mise.voice.VoiceUi

/**
 * Compact mic toggle + status pill for the Cook screen. Tapping it turns voice
 * navigation on/off; the label reflects the current [VoiceState] so the user
 * knows whether they were heard without studying the screen.
 */
@Composable
fun VoiceMicButton(voiceUi: VoiceUi, modifier: Modifier = Modifier) {
    val state = voiceUi.state
    val tint = when (state) {
        VoiceState.Off -> MiseTokens.colors.ink3
        VoiceState.Idle -> MiseTokens.colors.ink2
        VoiceState.Listening, is VoiceState.Heard -> MiseTokens.colors.accent
    }
    val label = when (state) {
        VoiceState.Off -> "Voice"
        VoiceState.Idle -> "Say “go”"
        VoiceState.Listening -> "Listening…"
        is VoiceState.Heard -> when (state.command) {
            VoiceCommand.Back -> "← Back"
            VoiceCommand.Forward -> "Next →"
            VoiceCommand.StartTimer -> "Start timer"
            VoiceCommand.StopTimers -> "Stop timer"
        }
    }
    val background = when (state) {
        VoiceState.Listening, is VoiceState.Heard -> MiseTokens.colors.accentWash
        else -> MiseTokens.colors.surface2
    }
    val shape = RoundedCornerShape(999.dp)

    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable { voiceUi.onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (state == VoiceState.Off) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = "Voice navigation",
            tint = tint,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(5.dp))
        Text(label, style = MiseTokens.text.micro, color = tint)
    }
}
