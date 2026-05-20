package com.patchfox.mise.data.cookcard

import com.patchfox.mise.domain.mapper.toDomain
import com.patchfox.mise.domain.model.Phase
import com.patchfox.mise.ui.theme.RecipeColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CookCardParserTest {

    private fun loadSample(): String =
        javaClass.classLoader!!.getResourceAsStream("sample-cook-card.json")!!
            .bufferedReader().use { it.readText() }

    @Test
    fun `parses theme and recipe count from sample cook card`() {
        val card = CookCardParser.parse(loadSample()).toDomain()
        assertEquals("Latin / Tex-Mex", card.theme)
        assertEquals(4, card.recipes.size)
    }

    @Test
    fun `phase 2 has 22 steps`() {
        val card = CookCardParser.parse(loadSample()).toDomain()
        val phase2 = card.cookPlan.phases.filterIsInstance<Phase.Phase2>().firstOrNull()
        assertNotNull(phase2)
        assertEquals(22, phase2!!.steps.size)
    }

    @Test
    fun `phase 1 has 9 mise bowls`() {
        val card = CookCardParser.parse(loadSample()).toDomain()
        val phase1 = card.cookPlan.phases.filterIsInstance<Phase.Phase1>().firstOrNull()
        assertNotNull(phase1)
        assertEquals(9, phase1!!.bowls.size)
    }

    @Test
    fun `chicken recipe color is BLUE`() {
        val card = CookCardParser.parse(loadSample()).toDomain()
        val chicken = card.recipes.first { it.id.value == "chicken-burrito-bowl" }
        assertEquals(RecipeColor.BLUE, chicken.color)
    }

    @Test
    fun `step 11 has a timer with 720 second lower bound`() {
        val card = CookCardParser.parse(loadSample()).toDomain()
        val phase2 = card.cookPlan.phases.filterIsInstance<Phase.Phase2>().first()
        val step11 = phase2.steps.first { it.stepNumber == 11 }
        assertEquals(720, step11.timer?.durationSeconds)
        assertEquals(900, step11.timer?.maxDurationSeconds)
    }
}
