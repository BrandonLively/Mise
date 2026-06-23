package com.patchfox.mise.ui.screen.cook

import app.cash.turbine.test
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.state.CompletedRecipeRepository
import com.patchfox.mise.data.state.CookStepIndexRepository
import com.patchfox.mise.data.state.LaneVisibilityRepository
import com.patchfox.mise.data.state.MiseCheckRepository
import com.patchfox.mise.data.state.StartedTimerRepository
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookCardId
import com.patchfox.mise.domain.model.CookPhase
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.DailyTarget
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.Macro
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.MisePhase
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.RecipeType
import com.patchfox.mise.domain.model.RecipeYield
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.StepTimer
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import com.patchfox.mise.timer.TimerController
import com.patchfox.mise.ui.state.CookSessionState
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.RecipeColor
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CookViewModelTest {

    // --- constructor deps -------------------------------------------------
    private val getCard: GetCurrentCookCardUseCase = mockk(relaxed = true)
    private val miseCheck: MiseCheckRepository = mockk(relaxed = true)
    private val startedTimer: StartedTimerRepository = mockk(relaxed = true)
    private val laneVisibility: LaneVisibilityRepository = mockk(relaxed = true)
    private val cookStepIndex: CookStepIndexRepository = mockk(relaxed = true)
    private val completedRecipe: CompletedRecipeRepository = mockk(relaxed = true)
    private val timerController: TimerController = mockk(relaxed = true)
    private val cookSession: CookSessionState = mockk(relaxed = true)

    // --- fixture ----------------------------------------------------------

    private fun ing(
        id: String,
        name: String = id,
        q: Double? = null,
        unit: String? = null,
        prep: String? = null,
    ) = Ingredient(
        ingredientId = id,
        name = name,
        quantity = q,
        quantityDisplay = q?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
        unit = unit,
        preparation = prep,
        combinable = true,
    )

    private fun step(
        id: String,
        bg: Boolean,
        prose: String,
        timer: StepTimer? = null,
        final: Boolean = false,
        vararg ings: Ingredient,
    ) = CookStep(
        id = StepId(id),
        backgroundable = bg,
        label = null,
        clockTime = null,
        steps = listOf(prose),
        ingredients = ings.toList(),
        handsOff = emptyList(),
        isFinalStep = final,
        timer = timer,
    )

    private fun recipe(
        id: String,
        emoji: String,
        type: RecipeType,
        bowls: List<MiseBowl>,
        steps: List<CookStep>,
    ) = Recipe(
        id = RecipeId(id),
        name = id,
        type = type,
        emoji = emoji,
        color = RecipeColor.BLUE,
        description = "",
        source = null,
        yield = RecipeYield(null, null, null),
        perServing = Macro(0.0, 0.0, 0.0, 0.0),
        timing = null,
        equipment = emptyList(),
        assembleAndStore = emptyList(),
        prepSchedule = emptyList(),
        misePhase = MisePhase(durationMinutes = null, description = null, bowls = bowls, standalones = emptyList()),
        cookPhase = CookPhase(durationMinutes = null, description = null, steps = steps),
        macroBreakdown = emptyList(),
        reheat = null,
        deviationsFromPlan = emptyList(),
    )

    private val cardId = CookCardId("test-card")

    /**
     * One MAIN recipe "r1" with two NON-backgroundable cook steps. Because nothing is
     * backgroundable there is no shared cook lane, so BuildCookGuidesUseCase yields a
     * single RecipeCookGuide with both steps. The second step carries a timer.
     */
    private val card: CookCard = CookCard(
        id = cardId,
        theme = "Test",
        cookDate = LocalDate(2026, 6, 23),
        servingsPerMain = 4,
        servingsPerDessert = null,
        dailyTarget = DailyTarget(0.0, 0.0, null, null),
        recipes = listOf(
            recipe(
                id = "r1",
                emoji = "🍞", // 🍞
                type = RecipeType.MAIN,
                bowls = listOf(MiseBowl(id = BowlId("b1"), name = "Bowl 1", ingredients = emptyList(), steps = emptyList())),
                steps = listOf(
                    step("r1-s1", bg = false, "Mix the dough"),
                    step(
                        "r1-s2",
                        bg = false,
                        "Bake the loaf",
                        timer = StepTimer("Bake", 600, null, "10 min"),
                        final = true,
                    ),
                ),
            ),
        ),
    )

    // --- harness ----------------------------------------------------------

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())

        every { getCard() } returns flowOf(CookCardLoadState.Success(card))
        every { miseCheck.observe(any()) } returns flowOf(emptySet())
        every { startedTimer.observe(any()) } returns flowOf(emptySet())
        every { laneVisibility.observe(any()) } returns flowOf(emptyMap())
        every { cookStepIndex.observe(any()) } returns flowOf(emptyMap())
        every { completedRecipe.observe(any()) } returns flowOf(emptySet())
        every { timerController.timers } returns MutableStateFlow(emptyList())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun vm() = CookViewModel(
        getCard = getCard,
        miseCheck = miseCheck,
        startedTimer = startedTimer,
        laneVisibility = laneVisibility,
        cookStepIndex = cookStepIndex,
        completedRecipe = completedRecipe,
        timerController = timerController,
        cookSession = cookSession,
    )

    // --- tests ------------------------------------------------------------

    @Test
    fun `setStage mirrors to the cook session`() = runTest {
        val vm = vm()
        vm.state.test {
            awaitItem() // initial empty state (loading)
            vm.setStage(CookStage.MISE)
            advanceUntilIdle()
            // first state with the loaded card has stage COOK; the MISE update follows.
            val mise = expectMostRecentItem()
            assertEquals(CookStage.MISE, mise.stage)
            cancelAndIgnoreRemainingEvents()
        }
        verify { cookSession.setStage(CookStage.MISE) }
    }

    @Test
    fun `toggleBowl toggles the mise check`() = runTest {
        val vm = vm()
        vm.state.test {
            awaitItem()
            advanceUntilIdle() // let the card load so state.value.card is non-null
            vm.toggleBowl(BowlId("b1"), true)
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { miseCheck.toggle(cardId, BowlId("b1"), true) }
    }

    @Test
    fun `advanceRecipe before the last step persists the next index`() = runTest {
        val vm = vm()
        vm.state.test {
            awaitItem()
            advanceUntilIdle()
            vm.advanceRecipe(RecipeId("r1"), 1)
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        // current index is 0 (emptyMap), so next is 1 — before the last step (size 2).
        coVerify { cookStepIndex.set(cardId, "r1", 1) }
    }

    @Test
    fun `advanceRecipe past the last step completes and hides the lane`() = runTest {
        // Current index is the last step (lastIndex 1) → advancing finishes the recipe.
        every { cookStepIndex.observe(any()) } returns flowOf(mapOf("r1" to 1))

        val vm = vm()
        vm.state.test {
            awaitItem()
            advanceUntilIdle()
            vm.advanceRecipe(RecipeId("r1"), 1)
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { completedRecipe.toggle(cardId, RecipeId("r1"), true) }
        coVerify { laneVisibility.setHidden(cardId, "COOK", "r1", true) }
    }

    @Test
    fun `startStepTimer starts the timer and marks it started`() = runTest {
        val timerStep = card.recipes.first().cookPhase.steps[1] // r1-s2, has the timer

        val vm = vm()
        vm.state.test {
            awaitItem()
            advanceUntilIdle()
            vm.startStepTimer(timerStep, RecipeColor.BLUE, "🍞")
            advanceUntilIdle()
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { startedTimer.markStarted(cardId, timerStep.id) }
        coVerify { timerController.start(cardId, timerStep.id, "Bake", 600, "🍞", RecipeColor.BLUE) }
    }
}
