package com.patchfox.mise.ui.screen.cook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.state.MiseCheckRepository
import com.patchfox.mise.data.state.StartedTimerRepository
import com.patchfox.mise.data.state.StepCompletionRepository
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.phase2
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import com.patchfox.mise.timer.TimerController
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.state.CookSessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ActiveTimerUi(
    val id: String,
    val title: String,
    val stepId: StepId?,
    val durationSeconds: Int,
    val remainingSeconds: Int,
    val recipeEmoji: String?,
    val recipeColor: com.patchfox.mise.ui.theme.RecipeColor?,
)

data class CookUiState(
    val loading: Boolean = true,
    val card: CookCard? = null,
    val selectedPhase: PhaseTab = PhaseTab.Phase2,
    val currentStepIndex: Int = 0,
    val activeTimers: List<ActiveTimerUi> = emptyList(),
    val startedTimerStepIds: Set<StepId> = emptySet(),
    val miseChecks: Set<BowlId> = emptySet(),
    val stepCompletions: Set<StepId> = emptySet(),
)

@HiltViewModel
class CookViewModel @Inject constructor(
    getCard: GetCurrentCookCardUseCase,
    private val miseCheck: MiseCheckRepository,
    private val stepCompletion: StepCompletionRepository,
    private val startedTimer: StartedTimerRepository,
    private val timerController: TimerController,
    private val cookSession: CookSessionState,
) : ViewModel() {

    private val selectedPhase = MutableStateFlow(PhaseTab.Phase2)
    private val currentStepIndex = MutableStateFlow(0)
    private var hasAutoAdvancedFromPhase1 = false

    init {
        // Mirror the selected phase into the app-wide session state so the
        // MiseAppViewModel can decide whether to surface the off-Phase-2 timer
        // alert dialog. The CookScreen composable separately toggles
        // CookSessionState.setCookScreenVisible via DisposableEffect.
        viewModelScope.launch {
            selectedPhase.collect { cookSession.setPhase(it) }
        }
    }

    fun setCookScreenVisible(visible: Boolean) {
        cookSession.setCookScreenVisible(visible)
    }

    fun extendTimer(timerId: String) {
        viewModelScope.launch { timerController.extendByOneMinute(timerId) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<CookUiState> = getCard()
        .flatMapLatest { load ->
            val card = (load as? CookCardLoadState.Success)?.card
                ?: (load as? CookCardLoadState.Error)?.cached
            if (card == null) {
                flowOf(CookUiState(loading = load is CookCardLoadState.Loading))
            } else {
                combine(
                    miseCheck.observe(card.id),
                    stepCompletion.observe(card.id),
                    startedTimer.observe(card.id),
                    timerController.timers,
                    selectedPhase,
                    currentStepIndex,
                ) { values ->
                    @Suppress("UNCHECKED_CAST")
                    val checks = values[0] as Set<BowlId>
                    @Suppress("UNCHECKED_CAST")
                    val completions = values[1] as Set<StepId>
                    @Suppress("UNCHECKED_CAST")
                    val started = values[2] as Set<StepId>
                    @Suppress("UNCHECKED_CAST")
                    val timers = values[3] as List<TimerController.RuntimeTimer>
                    val phase = values[4] as PhaseTab
                    val idx = values[5] as Int

                    val stepCount = card.phase2()?.steps?.size ?: 0
                    // Allow index == stepCount to represent the post-cook "congrats" card.
                    val upper = if (stepCount > 0) stepCount else 0
                    CookUiState(
                        loading = false,
                        card = card,
                        selectedPhase = phase,
                        currentStepIndex = idx.coerceIn(0, upper),
                        miseChecks = checks,
                        stepCompletions = completions,
                        startedTimerStepIds = started,
                        activeTimers = timers.map { t ->
                            ActiveTimerUi(
                                id = t.id,
                                title = t.title,
                                stepId = t.stepId?.let(::StepId),
                                durationSeconds = t.durationSeconds,
                                remainingSeconds = t.remainingSeconds,
                                recipeEmoji = t.recipeEmoji,
                                recipeColor = t.recipeColor,
                            )
                        },
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CookUiState())

    fun setPhase(phase: PhaseTab) { selectedPhase.value = phase }

    /**
     * Called from Phase 1 when the "all bowls checked" state changes. Auto-advances
     * to Phase 2 only the FIRST time per session — re-entering Phase 1 with all
     * bowls already checked must not bounce the user away again.
     */
    fun onPhase1AllDoneChanged(allDone: Boolean) {
        if (allDone && !hasAutoAdvancedFromPhase1) {
            hasAutoAdvancedFromPhase1 = true
            selectedPhase.value = PhaseTab.Phase2
        }
    }

    fun jumpToStep(index: Int) { currentStepIndex.value = index }

    fun advanceStep(delta: Int) {
        val steps = state.value.card?.phase2()?.steps ?: return
        // Allow one extra index past the last step to surface the congrats card.
        currentStepIndex.value = (currentStepIndex.value + delta).coerceIn(0, steps.size)
    }

    fun startTimerForCurrentStep() {
        val card = state.value.card ?: return
        val step = card.phase2()?.steps?.getOrNull(state.value.currentStepIndex) ?: return
        startTimer(step)
    }

    fun startTimer(step: CookStep) {
        val card = state.value.card ?: return
        val timer = step.timer ?: return
        viewModelScope.launch {
            startedTimer.markStarted(card.id, step.id)
            timerController.start(
                cookCardId = card.id,
                stepId = step.id,
                title = timer.title,
                durationSeconds = timer.durationSeconds,
                recipeEmoji = step.recipeEmoji,
                recipeColor = step.recipeColor,
            )
        }
    }

    fun dismissTimer(timerId: String) {
        viewModelScope.launch { timerController.dismiss(timerId) }
    }

    fun toggleMiseCheck(bowlId: BowlId, checked: Boolean) {
        val card = state.value.card ?: return
        viewModelScope.launch { miseCheck.toggle(card.id, bowlId, checked) }
    }

    fun toggleStepCompletion(id: StepId, completed: Boolean) {
        val card = state.value.card ?: return
        viewModelScope.launch { stepCompletion.toggle(card.id, id, completed) }
    }
}
