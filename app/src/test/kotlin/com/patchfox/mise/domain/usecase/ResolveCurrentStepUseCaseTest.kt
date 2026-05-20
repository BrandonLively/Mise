package com.patchfox.mise.domain.usecase

import com.patchfox.mise.data.cookcard.CookCardParser
import com.patchfox.mise.domain.mapper.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ResolveCurrentStepUseCaseTest {

    private val useCase = ResolveCurrentStepUseCase()
    private val card = CookCardParser.parse(
        javaClass.classLoader!!.getResourceAsStream("sample-cook-card.json")!!
            .bufferedReader().use { it.readText() },
    ).toDomain()

    @Test
    fun `before first step returns 0`() {
        assertEquals(0, useCase(card, -1))
    }

    @Test
    fun `at minute 0 returns first step`() {
        assertEquals(0, useCase(card, 0))
    }

    @Test
    fun `at minute 25 returns step at index 10 (step 11 fires at clock 0_25)`() {
        // Step 11 is clockMinutes=25 — first step where clockMinutes <= 25 hitting at 25 → that step
        val idx = useCase(card, 25)
        assertEquals(25, card.cookPlan.phases.filterIsInstance<com.patchfox.mise.domain.model.Phase.Phase2>().first().steps[idx].clockMinutes)
    }

    @Test
    fun `past last step returns last index`() {
        val phase2 = card.cookPlan.phases.filterIsInstance<com.patchfox.mise.domain.model.Phase.Phase2>().first()
        assertEquals(phase2.steps.lastIndex, useCase(card, 999))
    }
}
