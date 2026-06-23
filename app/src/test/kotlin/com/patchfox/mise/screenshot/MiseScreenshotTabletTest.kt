package com.patchfox.mise.screenshot

import android.app.Application
import com.patchfox.mise.domain.usecase.BuildCookGuidesUseCase
import com.patchfox.mise.domain.usecase.BuildMiseGuidesUseCase
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.screen.cook.CookContent
import com.patchfox.mise.ui.screen.cook.CookLaneCallbacks
import com.patchfox.mise.ui.screen.cook.CookUiState
import com.patchfox.mise.ui.screen.home.HomeTablet
import com.patchfox.mise.ui.screen.home.HomeUiState
import com.patchfox.mise.ui.screen.login.LoginContent
import com.patchfox.mise.ui.screen.login.LoginUiState
import com.patchfox.mise.ui.screen.recipe.RecipeDetailTablet
import com.patchfox.mise.ui.screen.recipe.RecipesContent
import com.patchfox.mise.ui.state.CookStage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tablet-landscape screenshots of every screen. Login and Recipes have no
 * dedicated tablet layout — their single composable is rendered at tablet size.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = TABLET_QUALIFIERS, application = Application::class)
class MiseScreenshotTabletTest {

    private val card = loadSampleCookCard()

    @Test
    fun login() = captureScreen("tablet-login") {
        LoginContent(state = LoginUiState(), onSignIn = {})
    }

    @Test
    fun schedule() = captureTabletScreen("tablet-schedule", MiseDestination.Home) {
        HomeTablet(
            state = HomeUiState(
                loading = false,
                card = card,
                prepTasks = ParsePrepTasksUseCase().invoke(card),
            ),
            onTogglePrep = { _, _ -> },
            onWalkPlan = {},
            onOpenStage = {},
            onOpenRecipe = {},
        )
    }

    @Test
    fun cook() = captureTabletScreen("tablet-cook", MiseDestination.Cook) {
        val miseGuides = BuildMiseGuidesUseCase()(card.recipes)
        val (cookGuides, sharedCookSteps) = BuildCookGuidesUseCase()(card.recipes)
        CookContent(
            state = CookUiState(
                loading = false,
                card = card,
                stage = CookStage.COOK,
                miseGuides = miseGuides,
                cookGuides = cookGuides,
                sharedCookSteps = sharedCookSteps,
            ),
            onSetStage = {},
            onToggleLane = {},
            onReopenRecipe = {},
            onDismissTimer = {},
            laneCallbacks = CookLaneCallbacks.NONE,
        )
    }

    @Test
    fun mise() = captureTabletScreen("tablet-mise", MiseDestination.Cook) {
        val miseGuides = BuildMiseGuidesUseCase()(card.recipes)
        val (cookGuides, sharedCookSteps) = BuildCookGuidesUseCase()(card.recipes)
        CookContent(
            state = CookUiState(
                loading = false,
                card = card,
                stage = CookStage.MISE,
                miseGuides = miseGuides,
                cookGuides = cookGuides,
                sharedCookSteps = sharedCookSteps,
            ),
            onSetStage = {},
            onToggleLane = {},
            onReopenRecipe = {},
            onDismissTimer = {},
            laneCallbacks = CookLaneCallbacks.NONE,
        )
    }

    @Test
    fun recipes() = captureTabletScreen("tablet-recipes", MiseDestination.Recipes) {
        RecipesContent(current = card, previous = emptyList(), onOpenRecipe = { _, _ -> })
    }

    @Test
    fun recipeDetail() = captureTabletScreen("tablet-recipe-detail", MiseDestination.Recipes) {
        RecipeDetailTablet(
            recipes = card.recipes,
            initial = card.recipes.first(),
            onBack = {},
            onOpenInstructions = {},
        )
    }
}
