package com.patchfox.mise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.auth.AuthRepository
import com.patchfox.mise.timer.TimerController
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.state.CookSessionState
import com.patchfox.mise.ui.theme.RecipeColor
import com.patchfox.mise.voice.VoiceCommand
import com.patchfox.mise.voice.VoiceCommandController
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUiState(val signedIn: Boolean)

/**
 * Lightweight DTO for the off-Phase-2 done-timer alert dialog at app scope.
 */
data class DoneTimerAlert(
    val id: String,
    val title: String,
    val recipeColor: RecipeColor?,
    val stepId: String?,
    val recipeEmoji: String?,
)

@HiltViewModel
class MiseAppViewModel @Inject constructor(
    auth: AuthRepository,
    private val timerController: TimerController,
    cookSession: CookSessionState,
    voiceController: VoiceCommandController,
) : ViewModel() {

    init {
        // "stop timer" must silence a ringing alarm from any screen, so it's
        // routed here at app scope rather than in CookViewModel (which only
        // exists while the Cook screen is composed).
        viewModelScope.launch {
            voiceController.commands.collect { command ->
                if (command == VoiceCommand.StopTimers) timerController.dismissAllDone()
            }
        }
    }

    val authState: StateFlow<AuthUiState> = auth.currentUser
        .map { AuthUiState(signedIn = it != null) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthUiState(signedIn = auth.currentUser.value != null))

    /**
     * Done timers that the user is NOT already looking at. The Cook Phase 2 strip
     * already shows the chip in its "done" state inline, so we suppress the
     * dialog there; everywhere else (Cook Phase 0/1, Schedule, Recipes, Summary)
     * the dialog surfaces so the user can decide what to do.
     */
    val doneTimerAlerts: StateFlow<List<DoneTimerAlert>> = combine(
        timerController.timers,
        cookSession.isCookScreenVisible,
        cookSession.phase,
    ) { timers, visible, phase ->
        val onPhase2 = visible && phase == PhaseTab.Phase2
        if (onPhase2) {
            emptyList()
        } else {
            timers.filter { it.remainingSeconds == 0 }.map { t ->
                DoneTimerAlert(
                    id = t.id,
                    title = t.title,
                    recipeColor = t.recipeColor,
                    stepId = t.stepId,
                    recipeEmoji = t.recipeEmoji,
                )
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun dismissTimer(id: String) {
        viewModelScope.launch { timerController.dismiss(id) }
    }

    fun extendTimer(id: String) {
        viewModelScope.launch { timerController.extendByOneMinute(id) }
    }
}
