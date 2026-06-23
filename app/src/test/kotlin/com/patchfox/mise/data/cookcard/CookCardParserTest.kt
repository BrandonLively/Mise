package com.patchfox.mise.data.cookcard

import com.patchfox.mise.domain.mapper.toDomain
import com.patchfox.mise.domain.model.RecipeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * v3 parser/mapper spec. Loads the on-disk v3 fixtures, parses them through
 * [CookCardParser.parse] + [toDomain], and asserts the shape survives the round trip.
 * Kept robust on purpose — exact step counts / numbers may drift, so we assert on
 * structure and invariants rather than brittle totals.
 */
class CookCardParserTest {

    private fun load(resource: String): String =
        this::class.java.getResource(resource)!!.readText()

    private fun caribbean() = CookCardParser.parse(load("/sample-cook-card.json")).toDomain()

    private fun minimal() = CookCardParser.parse(load("/cook-card-v3-minimal.json")).toDomain()

    @Test
    fun `caribbean sample parses theme and four recipes`() {
        val card = caribbean()
        assertEquals("Caribbean / Jerk", card.theme)
        assertEquals(4, card.recipes.size)
    }

    @Test
    fun `minimal fixture parses two recipes`() {
        val card = minimal()
        assertEquals(2, card.recipes.size)
        val ids = card.recipes.map { it.id.value }.toSet()
        assertTrue("jerk-chicken-rice-peas" in ids)
        assertTrue("jerk-salmon-mango-salsa" in ids)
    }

    @Test
    fun `caribbean sample has a dessert recipe`() {
        val card = caribbean()
        assertTrue(
            card.recipes.any { it.type == RecipeType.DESSERT },
            "expected at least one DESSERT recipe in the caribbean card",
        )
    }

    @Test
    fun `every caribbean recipe has at least one cook step`() {
        val card = caribbean()
        card.recipes.forEach { r ->
            assertTrue(
                r.cookPhase.steps.isNotEmpty(),
                "recipe ${r.id.value} should have a non-empty cookPhase.steps",
            )
        }
    }

    @Test
    fun `the white-rice backgroundable steps exist`() {
        val card = caribbean()
        val backgroundableRiceSteps = card.recipes.flatMap { it.cookPhase.steps }
            .filter { step -> step.backgroundable && step.ingredients.any { it.ingredientId == "white-rice" } }
        assertTrue(
            backgroundableRiceSteps.size >= 2,
            "expected multiple backgroundable white-rice steps, got ${backgroundableRiceSteps.size}",
        )
    }

    @Test
    fun `ingredient quantity is parsed when numeric and left null for messy strings`() {
        val card = caribbean()
        val allIngredients = card.recipes.flatMap { r ->
            r.cookPhase.steps.flatMap { it.ingredients } + r.misePhase.bowls.flatMap { it.ingredients }
        }

        // At least one cleanly-numeric quantity (e.g. white rice "340"/280) parses to a Double.
        assertTrue(
            allIngredients.any { it.quantity != null },
            "expected at least one ingredient with a non-null parsed quantity",
        )

        // At least one messy authored amount ("1 can", "½ tsp", "~120") keeps a display string
        // but cannot be parsed to a clean number → quantity is null, quantityDisplay is set.
        assertTrue(
            allIngredients.any { it.quantity == null && !it.quantityDisplay.isNullOrBlank() },
            "expected a messy-string ingredient with null quantity but a non-null quantityDisplay",
        )
    }

    @Test
    fun `a backgroundable step carries a populated timer`() {
        val card = caribbean()
        val timed = card.recipes.flatMap { it.cookPhase.steps }
            .firstOrNull { it.backgroundable && it.timer != null }
        assertNotNull(timed, "expected a backgroundable step with a timer")
        val timer = timed!!.timer!!
        assertTrue(timer.title.isNotBlank(), "timer.title should be populated")
        assertTrue(timer.durationSeconds > 0, "timer.durationSeconds should be populated")
    }
}
