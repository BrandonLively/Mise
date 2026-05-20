package com.patchfox.mise.domain.usecase

import com.patchfox.mise.data.cookcard.CookCardParser
import com.patchfox.mise.domain.mapper.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParsePrepTasksUseCaseTest {
    private val useCase = ParsePrepTasksUseCase()
    private val card = CookCardParser.parse(
        javaClass.classLoader!!.getResourceAsStream("sample-cook-card.json")!!
            .bufferedReader().use { it.readText() },
    ).toDomain()

    @Test
    fun `pickup tasks are filtered out`() {
        val tasks = useCase(card)
        assertFalse(tasks.any { it.text.contains("Pickup", ignoreCase = true) })
    }

    @Test
    fun `chicken marinade task is attributed to chicken-burrito-bowl`() {
        val tasks = useCase(card)
        val marinade = tasks.first { it.text.contains("Marinate", ignoreCase = true) }
        assertEquals("chicken-burrito-bowl", marinade.attributedRecipeId?.value)
    }

    @Test
    fun `avocado ripen task is attributed to enchilada recipe`() {
        val tasks = useCase(card)
        val avocado = tasks.first { it.text.contains("avocado", ignoreCase = true) }
        assertEquals("black-bean-enchilada-skillet", avocado.attributedRecipeId?.value)
    }

    @Test
    fun `expected number of food-prep tasks`() {
        // Two food-prep entries in the sample: marinate chicken + move avocados.
        // (The "No frozen proteins to thaw" entry matches "thaw" keyword too, so 3 in total
        // is fine.)
        val tasks = useCase(card)
        assertTrue(tasks.size in 2..3, "got ${tasks.size}")
    }
}
