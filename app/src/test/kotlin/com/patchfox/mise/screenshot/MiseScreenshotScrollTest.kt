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
import com.patchfox.mise.ui.component.BottomNav
import com.patchfox.mise.ui.nav.MiseDestination
import com.patchfox.mise.ui.screen.recipe.RecipeDetailPhone
import com.patchfox.mise.ui.theme.MiseTheme
import com.patchfox.mise.ui.theme.MiseTokens
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Screenshots that need scroll interaction, so they require a Compose test rule.
 * Kept separate from [MiseScreenshotPhoneTest] because the rule applies to every
 * test in its class and would blank out the rule-less `captureRoboImage` snapshots.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = PHONE_QUALIFIERS, application = Application::class)
class MiseScreenshotScrollTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val card = loadSampleCookCard()

    /**
     * Regression guard for the dot-leader wrapping fix: ingredient amounts must
     * wrap within their own column instead of starving the name column and
     * ballooning the row. The salmon recipe has the longest ingredient list in the
     * sample card (its mise spice mix plus cook-step ingredients), so it both
     * exercises a variety of amount strings and is long enough to require a scroll.
     * The list is scrolled until the final "salmon portions" row is in view so it
     * is captured.
     */
    @Test
    fun recipeDetailLongAmount() {
        val recipe = card.recipes.first { it.id.value == "jerk-salmon-mango-salsa" }
        composeRule.setContent {
            MiseTheme(textStyles = ScreenshotTextStyles) {
                Box(modifier = Modifier.fillMaxSize().background(MiseTokens.colors.bg)) {
                    Box(modifier = Modifier.fillMaxSize().padding(bottom = 86.dp)) {
                        RecipeDetailPhone(recipe = recipe, onBack = {}, onOpenInstructions = {})
                    }
                    Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                        BottomNav(selected = MiseDestination.Recipes, onSelect = {})
                    }
                }
            }
        }
        // Scroll the ingredients list down so the trailing ingredient rows are captured.
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("salmon portions", substring = true))
        composeRule.onRoot().captureRoboImage("src/test/screenshots/phone-recipe-detail-long-amount.png")
    }
}
