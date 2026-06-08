package com.patchfox.mise.screenshot

import android.app.Application
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.screen.cook.CookActions
import com.patchfox.mise.ui.screen.cook.CookTablet
import com.patchfox.mise.ui.screen.cook.CookUiState
import com.patchfox.mise.ui.screen.home.HomeTablet
import com.patchfox.mise.ui.screen.home.HomeUiState
import com.patchfox.mise.ui.screen.login.LoginContent
import com.patchfox.mise.ui.screen.login.LoginUiState
import com.patchfox.mise.ui.screen.recipe.RecipeDetailTablet
import com.patchfox.mise.ui.screen.recipe.RecipesContent
import com.patchfox.mise.ui.screen.summary.SummaryTablet
import com.patchfox.mise.ui.screen.summary.SummaryUiState
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
            onOpenPhase = {},
            onOpenRecipe = {},
        )
    }

    @Test
    fun cook() = captureTabletScreen("tablet-cook", MiseDestination.Cook) {
        CookTablet(
            state = CookUiState(loading = false, card = card, selectedPhase = PhaseTab.Phase2),
            actions = CookActions.NONE,
        )
    }

    @Test
    fun summary() = captureTabletScreen("tablet-summary", MiseDestination.Summary) {
        SummaryTablet(
            state = SummaryUiState(card = card, days = 7, loading = false),
            onSetDays = {},
            onSetWeight = { _, _, _ -> },
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
