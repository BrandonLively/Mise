package com.patchfox.mise.ui.component

import com.patchfox.mise.domain.model.InstructionIngredient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InstructionFormattingTest {

    @Test
    fun `weight unit joins with no space`() {
        val out = formatIngredient(
            InstructionIngredient(name = "whole milk", quantity = "1,220", unit = "g", preparation = null),
        )
        assertEquals("1,220g whole milk", out)
    }

    @Test
    fun `spelled-out unit joins with a space`() {
        val out = formatIngredient(
            InstructionIngredient(name = "cinnamon", quantity = "2", unit = "tsp", preparation = null),
        )
        assertEquals("2 tsp cinnamon", out)
    }

    @Test
    fun `fractional quantity is preserved`() {
        val out = formatIngredient(
            InstructionIngredient(name = "ancho chili powder", quantity = "¼", unit = "tsp", preparation = null),
        )
        assertEquals("¼ tsp ancho chili powder", out)
    }

    @Test
    fun `unit alone with null quantity renders pinch salt`() {
        val out = formatIngredient(
            InstructionIngredient(name = "salt", quantity = null, unit = "pinch", preparation = null),
        )
        assertEquals("pinch salt", out)
    }

    @Test
    fun `whole count without unit renders 4 eggs with preparation comma suffix`() {
        val out = formatIngredient(
            InstructionIngredient(name = "eggs", quantity = "4", unit = null, preparation = "large grade A"),
        )
        assertEquals("4 eggs, large grade A", out)
    }

    @Test
    fun `no quantity no unit just preparation`() {
        val out = formatIngredient(
            InstructionIngredient(name = "black pepper", quantity = null, unit = null, preparation = "to taste"),
        )
        assertEquals("black pepper, to taste", out)
    }

    @Test
    fun `garlic clove with grated preparation`() {
        val out = formatIngredient(
            InstructionIngredient(name = "garlic clove", quantity = "1", unit = null, preparation = "grated"),
        )
        assertEquals("1 garlic clove, grated", out)
    }

    @Test
    fun `derived measurement renders with comma-suffix preparation`() {
        val out = formatIngredient(
            InstructionIngredient(name = "lime juice", quantity = "12", unit = "g", preparation = "from ½ lime"),
        )
        assertEquals("12g lime juice, from ½ lime", out)
    }

    @Test
    fun `blank preparation is dropped`() {
        val out = formatIngredient(
            InstructionIngredient(name = "sugar", quantity = "135", unit = "g", preparation = ""),
        )
        assertEquals("135g sugar", out)
    }

    @Test
    fun `fl oz treated as no-space weight-like unit`() {
        val out = formatIngredient(
            InstructionIngredient(name = "vanilla extract", quantity = "1", unit = "fl oz", preparation = null),
        )
        assertEquals("1fl oz vanilla extract", out)
    }
}
