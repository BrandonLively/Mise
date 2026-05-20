package com.patchfox.mise.screenshot

import android.app.Application
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.screen.cook.CookActions
import com.patchfox.mise.ui.screen.cook.CookPhone
import com.patchfox.mise.ui.screen.cook.CookUiState
import com.patchfox.mise.ui.screen.home.HomePhone
import com.patchfox.mise.ui.screen.home.HomeUiState
import com.patchfox.mise.ui.screen.login.LoginContent
import com.patchfox.mise.ui.screen.login.LoginUiState
import com.patchfox.mise.ui.screen.recipe.RecipeDetailPhone
import com.patchfox.mise.ui.screen.recipe.RecipesContent
import com.patchfox.mise.ui.screen.summary.SummaryPhone
import com.patchfox.mise.ui.screen.summary.SummaryUiState
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Phone-portrait screenshots of every screen. Goldens live in
 * `app/src/test/screenshots/` — `./gradlew recordRoborazziDebug` (re)generates,
 * `./gradlew verifyRoborazziDebug` checks against them.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PHONE_QUALIFIERS, application = Application::class)
class MiseScreenshotPhoneTest {

    private val card = loadSampleCookCard()

    @Test
    fun login() = captureScreen("phone-login") {
        LoginContent(state = LoginUiState(), onSignIn = {})
    }

    @Test
    fun schedule() = capturePhoneScreen("phone-schedule", MiseDestination.Home) {
        HomePhone(
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
    fun cook() = capturePhoneScreen("phone-cook", MiseDestination.Cook) {
        CookPhone(
            state = CookUiState(loading = false, card = card, selectedPhase = PhaseTab.Phase2),
            actions = CookActions.NONE,
            onViewSummary = {},
        )
    }

    @Test
    fun summary() = capturePhoneScreen("phone-summary", MiseDestination.Summary) {
        SummaryPhone(
            state = SummaryUiState(card = card, days = 7, loading = false),
            onSetDays = {},
            onSetWeight = { _, _, _ -> },
        )
    }

    @Test
    fun recipes() = capturePhoneScreen("phone-recipes", MiseDestination.Recipes) {
        RecipesContent(recipes = card.recipes, onOpenRecipe = {})
    }

    @Test
    fun recipeDetail() = capturePhoneScreen("phone-recipe-detail", MiseDestination.Recipes) {
        RecipeDetailPhone(recipe = card.recipes.first(), onBack = {})
    }
}
