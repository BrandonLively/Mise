package com.patchfox.mise.screenshot

import android.app.Application
import com.patchfox.mise.domain.usecase.BuildCookGuidesUseCase
import com.patchfox.mise.domain.usecase.BuildMiseGuidesUseCase
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.screen.cook.CookContent
import com.patchfox.mise.ui.screen.cook.CookLaneCallbacks
import com.patchfox.mise.ui.screen.cook.CookUiState
import com.patchfox.mise.ui.screen.home.HomePhone
import com.patchfox.mise.ui.screen.home.HomeUiState
import com.patchfox.mise.ui.screen.login.LoginContent
import com.patchfox.mise.ui.screen.login.LoginUiState
import com.patchfox.mise.ui.screen.recipe.RecipeDetailPhone
import com.patchfox.mise.ui.screen.recipe.RecipesContent
import com.patchfox.mise.ui.state.CookStage
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
            onOpenStage = {},
            onOpenRecipe = {},
        )
    }

    @Test
    fun cook() = capturePhoneScreen("phone-cook", MiseDestination.Cook) {
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
    fun mise() = capturePhoneScreen("phone-mise", MiseDestination.Cook) {
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
    fun recipes() = capturePhoneScreen("phone-recipes", MiseDestination.Recipes) {
        RecipesContent(current = card, previous = emptyList(), onOpenRecipe = { _, _ -> })
    }

    @Test
    fun recipeDetail() = capturePhoneScreen("phone-recipe-detail", MiseDestination.Recipes) {
        RecipeDetailPhone(recipe = card.recipes.first(), onBack = {}, onOpenInstructions = {})
    }
}
