package com.patchfox.mise.ui.component

import com.patchfox.mise.domain.model.Ingredient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Spec for [formatIngredient] over the v3 [Ingredient]. Renders the human
 * [Ingredient.quantityDisplay] (not a numeric quantity), so messy authored amounts
 * surface verbatim. Formatting rules:
 *  - abbreviated weight/volume units join with NO space ("340g salmon", "1fl oz ...")
 *  - spelled-out units take a space ("2 tsp allspice")
 *  - null quantityDisplay + unit alone is the measurement ("pinch salt")
 *  - null quantityDisplay + null unit → just the name (preparation carries modifiers)
 *  - non-blank preparation is suffixed as ", {prep}"
 */
class InstructionFormattingTest {

    private fun ing(
        quantityDisplay: String?,
        unit: String?,
        preparation: String?,
        name: String,
    ) = Ingredient(
        ingredientId = name.replace(' ', '-'),
        name = name,
        quantity = null,
        quantityDisplay = quantityDisplay,
        unit = unit,
        preparation = preparation,
    )

    @Test
    fun `weight unit joins with no space`() {
        val out = formatIngredient(ing(quantityDisplay = "1,220", unit = "g", preparation = null, name = "whole milk"))
        assertEquals("1,220g whole milk", out)
    }

    @Test
    fun `spelled-out unit joins with a space`() {
        val out = formatIngredient(ing(quantityDisplay = "2", unit = "tsp", preparation = null, name = "cinnamon"))
        assertEquals("2 tsp cinnamon", out)
    }

    @Test
    fun `fractional quantity is preserved`() {
        val out = formatIngredient(
            ing(quantityDisplay = "¼", unit = "tsp", preparation = null, name = "ancho chili powder"),
        )
        assertEquals("¼ tsp ancho chili powder", out)
    }

    @Test
    fun `unit alone with null quantity renders pinch salt`() {
        val out = formatIngredient(ing(quantityDisplay = null, unit = "pinch", preparation = null, name = "salt"))
        assertEquals("pinch salt", out)
    }

    @Test
    fun `whole count without unit renders 4 eggs with preparation comma suffix`() {
        val out = formatIngredient(
            ing(quantityDisplay = "4", unit = null, preparation = "large grade A", name = "eggs"),
        )
        assertEquals("4 eggs, large grade A", out)
    }

    @Test
    fun `no quantity no unit just preparation`() {
        val out = formatIngredient(
            ing(quantityDisplay = null, unit = null, preparation = "to taste", name = "black pepper"),
        )
        assertEquals("black pepper, to taste", out)
    }

    @Test
    fun `garlic clove with grated preparation`() {
        val out = formatIngredient(
            ing(quantityDisplay = "1", unit = null, preparation = "grated", name = "garlic clove"),
        )
        assertEquals("1 garlic clove, grated", out)
    }

    @Test
    fun `derived measurement renders with comma-suffix preparation`() {
        val out = formatIngredient(
            ing(quantityDisplay = "12", unit = "g", preparation = "from ½ lime", name = "lime juice"),
        )
        assertEquals("12g lime juice, from ½ lime", out)
    }

    @Test
    fun `blank preparation is dropped`() {
        val out = formatIngredient(ing(quantityDisplay = "135", unit = "g", preparation = "", name = "sugar"))
        assertEquals("135g sugar", out)
    }

    @Test
    fun `fl oz treated as no-space weight-like unit`() {
        val out = formatIngredient(
            ing(quantityDisplay = "1", unit = "fl oz", preparation = null, name = "vanilla extract"),
        )
        assertEquals("1fl oz vanilla extract", out)
    }

    @Test
    fun `null quantityDisplay and unit renders just the name`() {
        val out = formatIngredient(ing(quantityDisplay = null, unit = null, preparation = null, name = "salmon"))
        assertEquals("salmon", out)
    }
}
