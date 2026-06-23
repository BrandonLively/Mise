package com.patchfox.mise.ui.screen.cook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.state.CompletedRecipeRepository
import com.patchfox.mise.data.state.CookStepIndexRepository
import com.patchfox.mise.data.state.LaneVisibilityRepository
import com.patchfox.mise.data.state.MiseCheckRepository
import com.patchfox.mise.data.state.StartedTimerRepository
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.StepTimer
import com.patchfox.mise.domain.usecase.BuildCookGuidesUseCase
import com.patchfox.mise.domain.usecase.BuildMiseGuidesUseCase
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import com.patchfox.mise.domain.usecase.RecipeCookGuide
import com.patchfox.mise.domain.usecase.RecipeMiseGuide
import com.patchfox.mise.domain.usecase.SharedCookStep
import com.patchfox.mise.timer.TimerController
import com.patchfox.mise.ui.state.CookSessionState
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.RecipeColor
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

/** A lane is one recipe column, or the app-derived shared column. */
@JvmInline
value class LaneId(val value: String) {
    companion object {
        val SHARED = LaneId("shared")
    }
}

/** Persistence key for a cook lane's step index — a recipeId, or the shared sentinel. */
private const val SHARED_LANE_KEY = "__shared__"

data class ActiveTimerUi(
    val id: String,
    val title: String,
    val stepId: StepId?,
    val durationSeconds: Int,
    val remainingSeconds: Int,
    val recipeEmoji: String?,
    val recipeColor: RecipeColor?,
)

data class CookUiState(
    val loading: Boolean = true,
    val card: CookCard? = null,
    val stage: CookStage = CookStage.COOK,
    // app-derived lanes
    val miseGuides: List<RecipeMiseGuide> = emptyList(),
    val cookGuides: List<RecipeCookGuide> = emptyList(),
    val sharedCookSteps: List<SharedCookStep> = emptyList(),
    // lane visibility — INDEPENDENT per stage; a present laneId = explicitly hidden
    val hiddenLanesByStage: Map<CookStage, Set<String>> = emptyMap(),
    // cook progress
    val stepIndexByRecipe: Map<String, Int> = emptyMap(),
    val sharedStepIndex: Int = 0,
    val completedRecipes: Set<RecipeId> = emptySet(),
    // mise progress
    val checkedBowls: Set<BowlId> = emptySet(),
    // timers (command bar)
    val activeTimers: List<ActiveTimerUi> = emptyList(),
    val startedTimerStepIds: Set<StepId> = emptySet(),
) {
    fun isHidden(stage: CookStage, laneId: LaneId): Boolean =
        hiddenLanesByStage[stage]?.contains(laneId.value) == true

    fun recipeStepIndex(recipeId: RecipeId): Int = stepIndexByRecipe[recipeId.value] ?: 0

    /** Stable per-recipe letter (A, B, C…) from the plan order — used for bowl labels (A1, A2…). */
    fun recipeLetter(recipeId: RecipeId): String {
        val idx = card?.recipes?.indexOfFirst { it.id == recipeId } ?: -1
        return when {
            idx < 0 -> "?"
            idx < 26 -> ('A' + idx).toString()
            else -> (idx + 1).toString()
        }
    }
}

@HiltViewModel
class CookViewModel @Inject constructor(
    getCard: GetCurrentCookCardUseCase,
    private val miseCheck: MiseCheckRepository,
    private val startedTimer: StartedTimerRepository,
    private val laneVisibility: LaneVisibilityRepository,
    private val cookStepIndex: CookStepIndexRepository,
    private val completedRecipe: CompletedRecipeRepository,
    private val timerController: TimerController,
    private val cookSession: CookSessionState,
) : ViewModel() {

    private val buildMiseGuides = BuildMiseGuidesUseCase()
    private val buildCookGuides = BuildCookGuidesUseCase()

    private val stage = MutableStateFlow(CookStage.COOK)

    init {
        // Mirror the active stage into the app session so MiseAppViewModel can
        // suppress the done-timer dialog while the command bar shows it inline.
        viewModelScope.launch { stage.collect { cookSession.setStage(it) } }
    }

    fun setCookScreenVisible(visible: Boolean) = cookSession.setCookScreenVisible(visible)

    fun extendTimer(timerId: String) {
        viewModelScope.launch { timerController.extendByOneMinute(timerId) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<CookUiState> = getCard()
        .flatMapLatest { load ->
            val card = (load as? CookCardLoadState.Success)?.card
                ?: (load as? CookCardLoadState.Error)?.cached
            if (card == null) {
                flowOf(CookUiState(loading = load is CookCardLoadState.Loading, card = null))
            } else {
                val miseGuides = buildMiseGuides(card.recipes)
                val (cookGuides, sharedCookSteps) = buildCookGuides(card.recipes)
                combine(
                    miseCheck.observe(card.id),
                    startedTimer.observe(card.id),
                    timerController.timers,
                    laneVisibility.observe(card.id),
                    cookStepIndex.observe(card.id),
                    completedRecipe.observe(card.id),
                    stage,
                ) { values ->
                    @Suppress("UNCHECKED_CAST")
                    val checkedBowls = values[0] as Set<BowlId>
                    @Suppress("UNCHECKED_CAST")
                    val started = values[1] as Set<StepId>
                    @Suppress("UNCHECKED_CAST")
                    val timers = values[2] as List<TimerController.RuntimeTimer>
                    @Suppress("UNCHECKED_CAST")
                    val hiddenByStageName = values[3] as Map<String, Set<String>>
                    @Suppress("UNCHECKED_CAST")
                    val indexMap = values[4] as Map<String, Int>
                    @Suppress("UNCHECKED_CAST")
                    val completed = values[5] as Set<RecipeId>
                    val stg = values[6] as CookStage

                    CookUiState(
                        loading = false,
                        card = card,
                        stage = stg,
                        miseGuides = miseGuides,
                        cookGuides = cookGuides,
                        sharedCookSteps = sharedCookSteps,
                        hiddenLanesByStage = hiddenByStageName.mapKeys { (k, _) ->
                            runCatching { CookStage.valueOf(k) }.getOrDefault(CookStage.COOK)
                        },
                        stepIndexByRecipe = indexMap.filterKeys { it != SHARED_LANE_KEY },
                        sharedStepIndex = indexMap[SHARED_LANE_KEY] ?: 0,
                        completedRecipes = completed,
                        checkedBowls = checkedBowls,
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

    // --- intents -----------------------------------------------------------

    fun setStage(stage: CookStage) { this.stage.value = stage }

    /** Show/hide a lane for the given stage (independent per stage). */
    fun toggleLane(stage: CookStage, laneId: LaneId) {
        val card = state.value.card ?: return
        val nowHidden = !state.value.isHidden(stage, laneId)
        viewModelScope.launch {
            laneVisibility.setHidden(card.id, stage.name, laneId.value, nowHidden)
        }
    }

    /**
     * Advance a recipe's cook lane. Going past the last step marks the recipe
     * complete and hides its cook lane (its chip stays, to re-open).
     */
    fun advanceRecipe(recipeId: RecipeId, delta: Int) {
        val card = state.value.card ?: return
        val steps = state.value.cookGuides.firstOrNull { it.recipe.id == recipeId }?.steps ?: return
        val next = state.value.recipeStepIndex(recipeId) + delta
        viewModelScope.launch {
            if (next >= steps.size) {
                completedRecipe.toggle(card.id, recipeId, true)
                laneVisibility.setHidden(card.id, CookStage.COOK.name, recipeId.value, true)
            } else {
                cookStepIndex.set(card.id, recipeId.value, next.coerceAtLeast(0))
            }
        }
    }

    /** Re-open a finished recipe: clear completion and un-hide its cook lane. */
    fun reopenRecipe(recipeId: RecipeId) {
        val card = state.value.card ?: return
        viewModelScope.launch {
            completedRecipe.toggle(card.id, recipeId, false)
            laneVisibility.setHidden(card.id, CookStage.COOK.name, recipeId.value, false)
        }
    }

    fun advanceShared(delta: Int) {
        val card = state.value.card ?: return
        val size = state.value.sharedCookSteps.size
        if (size == 0) return
        val next = (state.value.sharedStepIndex + delta).coerceIn(0, size - 1)
        viewModelScope.launch { cookStepIndex.set(card.id, SHARED_LANE_KEY, next) }
    }

    /** Deep-link: jump to the step with [stepId] (real per-recipe step, or a shared step). */
    fun jumpToStep(stepId: String) {
        val card = state.value.card ?: return
        val s = state.value
        s.cookGuides.forEach { g ->
            val idx = g.steps.indexOfFirst { it.id.value == stepId }
            if (idx >= 0) {
                stage.value = CookStage.COOK
                viewModelScope.launch {
                    cookStepIndex.set(card.id, g.recipe.id.value, idx)
                    laneVisibility.setHidden(card.id, CookStage.COOK.name, g.recipe.id.value, false)
                }
                return
            }
        }
        val sharedIdx = s.sharedCookSteps.indexOfFirst { it.id == stepId }
        if (sharedIdx >= 0) {
            stage.value = CookStage.COOK
            viewModelScope.launch { cookStepIndex.set(card.id, SHARED_LANE_KEY, sharedIdx) }
        }
    }

    fun toggleBowl(bowlId: BowlId, checked: Boolean) {
        val card = state.value.card ?: return
        viewModelScope.launch { miseCheck.toggle(card.id, bowlId, checked) }
    }

    /** Start a step's timer; the owning recipe supplies the color/emoji (v3 steps carry neither). */
    fun startStepTimer(step: CookStep, recipeColor: RecipeColor?, recipeEmoji: String?) {
        val card = state.value.card ?: return
        val timer: StepTimer = step.timer ?: return
        viewModelScope.launch {
            startedTimer.markStarted(card.id, step.id)
            timerController.start(
                cookCardId = card.id,
                stepId = step.id,
                title = timer.title,
                durationSeconds = timer.durationSeconds,
                recipeEmoji = recipeEmoji,
                recipeColor = recipeColor,
            )
        }
    }

    /** Start the timer for an app-derived shared cook step (synthetic id, neutral color). */
    fun startSharedTimer(shared: SharedCookStep) {
        val card = state.value.card ?: return
        val timer = shared.timer ?: return
        val stepId = StepId(shared.id)
        viewModelScope.launch {
            startedTimer.markStarted(card.id, stepId)
            timerController.start(
                cookCardId = card.id,
                stepId = stepId,
                title = timer.title,
                durationSeconds = timer.durationSeconds,
                recipeEmoji = null,
                recipeColor = null,
            )
        }
    }

    fun dismissTimer(timerId: String) {
        viewModelScope.launch { timerController.dismiss(timerId) }
    }
}
