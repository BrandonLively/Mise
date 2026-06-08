package com.patchfox.mise.screenshot

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.ui.component.BottomNav
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
import com.patchfox.mise.ui.theme.MiseTheme
import com.patchfox.mise.ui.theme.MiseTokens
import org.junit.Rule
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

    @get:Rule
    val composeRule = createComposeRule()

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
        RecipesContent(current = card, previous = emptyList(), onOpenRecipe = { _, _ -> })
    }

    @Test
    fun recipeDetail() = capturePhoneScreen("phone-recipe-detail", MiseDestination.Recipes) {
        RecipeDetailPhone(recipe = card.recipes.first(), onBack = {}, onOpenInstructions = {})
    }

    /**
     * Documents a known text-wrapping defect: a very long ingredient amount on the
     * right of the dot-leader row overruns instead of wrapping or eliding. The first
     * ingredient's batch quantity is replaced with an oversized string to surface it.
     * The list is scrolled into view so the broken row is captured.
     */
    @Test
    fun recipeDetailLongAmount() {
        val recipe = card.recipes.first()
        val longAmountRecipe = recipe.copy(
            ingredients = recipe.ingredients.mapIndexed { index, ingredient ->
                if (index == 0) {
                    ingredient.copy(
                        batchQuantity = "2 1/2 cups plus 3 tablespoons, divided — about 312 g sifted",
                    )
                } else {
                    ingredient
                }
            },
        )
        composeRule.setContent {
            MiseTheme(textStyles = ScreenshotTextStyles) {
                Box(modifier = Modifier.fillMaxSize().background(MiseTokens.colors.bg)) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 86.dp)) {
                        RecipeDetailPhone(recipe = longAmountRecipe, onBack = {}, onOpenInstructions = {})
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        BottomNav(selected = MiseDestination.Recipes, onSelect = {})
                    }
                }
            }
        }
        // Scroll the ingredients list into view so the broken first row is captured.
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("tablespoons", substring = true))
        composeRule.onRoot().captureRoboImage("src/test/screenshots/phone-recipe-detail-long-amount.png")
    }
}
