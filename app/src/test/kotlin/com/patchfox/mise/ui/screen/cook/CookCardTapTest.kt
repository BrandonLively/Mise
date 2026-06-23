package com.patchfox.mise.ui.screen.cook

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.StepTimer
import com.patchfox.mise.ui.theme.MiseTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Tap-target behavior for the lane cards: the WHOLE card checks off (mise) / advances
 * (cook) a step. The in-card "Start timer" button must keep its own tap (the child
 * consumes it) and NOT also advance the step — that's the gesture-correctness risk a
 * static golden can't catch.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], application = Application::class)
class CookCardTapTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun tapping_a_mise_bowl_card_toggles_checked() {
        var toggledTo: Boolean? = null
        val bowl = MiseBowl(
            id = BowlId("b1"),
            name = "Pico de gallo",
            ingredients = listOf(Ingredient("tomato", "Roma tomato", 250.0, "250", "g", "diced")),
            steps = emptyList(),
        )
        rule.setContent {
            MiseTheme {
                MiseBowlCard(label = "A1", bowl = bowl, checked = false, onToggle = { toggledTo = it })
            }
        }
        rule.onNodeWithText("Pico de gallo", useUnmergedTree = true).performClick()
        assertEquals(true, toggledTo)
    }

    @Test
    fun tapping_a_cook_step_card_advances() {
        var advanced = false
        val step = CookStep(
            id = StepId("s1"), backgroundable = false, label = null, clockTime = null,
            steps = listOf("Sear the salmon skin-side down"), ingredients = emptyList(),
            handsOff = emptyList(), isFinalStep = false, timer = null,
        )
        rule.setContent {
            MiseTheme {
                StepCard(step = step, started = false, onStartTimer = {}, onClick = { advanced = true })
            }
        }
        rule.onNodeWithText("Sear the salmon skin-side down", useUnmergedTree = true).performClick()
        assertTrue(advanced)
    }

    @Test
    fun tapping_start_timer_starts_the_timer_and_does_not_advance() {
        var advanced = false
        var timerStarted = false
        val step = CookStep(
            id = StepId("s1"), backgroundable = true, label = null, clockTime = null,
            steps = listOf("Start the rice"), ingredients = emptyList(), handsOff = emptyList(),
            isFinalStep = false, timer = StepTimer("Rice", 1500, null, "~25 min"),
        )
        rule.setContent {
            MiseTheme {
                StepCard(
                    step = step,
                    started = false,
                    onStartTimer = { timerStarted = true },
                    onClick = { advanced = true },
                )
            }
        }
        rule.onNodeWithText("Start timer", substring = true, useUnmergedTree = true).performClick()
        assertTrue("the start-timer button must fire", timerStarted)
        assertFalse("tapping the start-timer button must NOT advance the step", advanced)
    }
}
