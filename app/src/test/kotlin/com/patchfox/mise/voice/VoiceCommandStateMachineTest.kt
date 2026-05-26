package com.patchfox.mise.voice

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class VoiceCommandStateMachineTest {

    @Test
    fun `go back in one phrase emits Back`() {
        assertEquals(VoiceCommand.Back, VoiceCommandStateMachine().onResult("go back", nowMs = 0))
    }

    @Test
    fun `go forward and go next both emit Forward`() {
        assertEquals(VoiceCommand.Forward, VoiceCommandStateMachine().onResult("go forward", 0))
        assertEquals(VoiceCommand.Forward, VoiceCommandStateMachine().onResult("go next", 0))
    }

    @Test
    fun `activator then command as separate phrases within the window`() {
        val sm = VoiceCommandStateMachine(windowMs = 4_000)
        assertNull(sm.onResult("go", nowMs = 1_000))
        assertEquals(VoiceCommand.Back, sm.onResult("back", nowMs = 3_000))
    }

    @Test
    fun `command after the window expires is ignored`() {
        val sm = VoiceCommandStateMachine(windowMs = 4_000)
        sm.onResult("go", nowMs = 0)
        assertNull(sm.onResult("back", nowMs = 5_000))
    }

    @Test
    fun `command without an activator is ignored`() {
        assertNull(VoiceCommandStateMachine().onResult("back", nowMs = 0))
        assertNull(VoiceCommandStateMachine().onResult("forward", nowMs = 0))
    }

    @Test
    fun `isArmed reflects the activator window`() {
        val sm = VoiceCommandStateMachine(windowMs = 4_000)
        assertFalse(sm.isArmed(nowMs = 0))
        sm.onResult("go", nowMs = 1_000)
        assertTrue(sm.isArmed(nowMs = 2_000))
        assertFalse(sm.isArmed(nowMs = 6_000))
    }

    @Test
    fun `a recognized command consumes the armed window`() {
        val sm = VoiceCommandStateMachine(windowMs = 4_000)
        sm.onResult("go", nowMs = 0)
        assertEquals(VoiceCommand.Back, sm.onResult("back", nowMs = 1_000))
        // Window consumed — a second command without a fresh "go" does nothing.
        assertNull(sm.onResult("back", nowMs = 2_000))
    }

    @Test
    fun `reset clears a pending window`() {
        val sm = VoiceCommandStateMachine(windowMs = 4_000)
        sm.onResult("go", nowMs = 0)
        sm.reset()
        assertNull(sm.onResult("back", nowMs = 1_000))
    }

    @Test
    fun `unknown tokens are ignored`() {
        val sm = VoiceCommandStateMachine()
        assertNull(sm.onResult("the quick brown fox", nowMs = 0))
        assertEquals(VoiceCommand.Forward, sm.onResult("um go forward please", nowMs = 0))
    }

    @Test
    fun `start timer and stop timer fire without the activator`() {
        assertEquals(VoiceCommand.StartTimer, VoiceCommandStateMachine().onResult("start timer", 0))
        assertEquals(VoiceCommand.StopTimers, VoiceCommandStateMachine().onResult("stop timer", 0))
    }

    @Test
    fun `start or stop without timer is ignored`() {
        assertNull(VoiceCommandStateMachine().onResult("start", nowMs = 0))
        assertNull(VoiceCommandStateMachine().onResult("stop", nowMs = 0))
        assertNull(VoiceCommandStateMachine().onResult("timer", nowMs = 0))
    }
}
