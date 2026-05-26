package com.patchfox.mise.ui.component

import com.patchfox.mise.domain.model.InstructionIngredient

/**
 * Units that join their quantity with no space — abbreviated weight/volume.
 * Everything else (tsp, Tbsp, cup, clove, pinch, etc.) takes a space.
 */
private val NO_SPACE_UNITS = setOf("g", "kg", "mg", "ml", "l", "oz", "lb", "fl oz")

/**
 * Renders one [InstructionIngredient] using the recipe-card convention:
 * `{quantity}{join}{unit} {name}, {preparation}`.
 *
 * Edge cases (see cook-card v2 handoff doc):
 * - `quantity:null, unit:"pinch"` → `"pinch salt"` (unit alone is the measurement)
 * - `quantity:"4", unit:null` → `"4 eggs"` (whole count, no measurement type)
 * - `quantity:null, unit:null` → `"black pepper"` (preparation carries any modifier)
 * - `preparation:"to taste"` → suffixed as `", to taste"`
 */
fun formatIngredient(ingredient: InstructionIngredient): String {
    val measurement = buildMeasurement(ingredient.quantity, ingredient.unit)
    val head = if (measurement.isEmpty()) ingredient.name else "$measurement ${ingredient.name}"
    val prep = ingredient.preparation?.takeIf { it.isNotBlank() }
    return if (prep != null) "$head, $prep" else head
}

private fun buildMeasurement(quantity: String?, unit: String?): String {
    val q = quantity?.takeIf { it.isNotBlank() }
    val u = unit?.takeIf { it.isNotBlank() }
    return when {
        q == null && u == null -> ""
        q == null -> u!!
        u == null -> q
        u in NO_SPACE_UNITS -> "$q$u"
        else -> "$q $u"
    }
}
