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

    @Test
    fun `v2 InstructionStep with structured ingredients parses`() {
        val card = CookCardParser.parse(V2_MINI_CARD).toDomain()
        val bowl = card.cookPlan.phases.filterIsInstance<Phase.Phase1>().first().bowls.first()
        assertEquals(2, bowl.instruction.size)

        val first = bowl.instruction[0]
        assertEquals("Combine the following directly in the saucepan off heat:", first.prefix)
        val ingredients = first.ingredients
        assertNotNull(ingredients)
        assertEquals(3, ingredients!!.size)
        assertEquals("whole milk", ingredients[0].name)
        assertEquals("1,220", ingredients[0].quantity)
        assertEquals("g", ingredients[0].unit)
        assertEquals("pinch", ingredients[2].unit)
        assertEquals(null, ingredients[2].quantity)

        val second = bowl.instruction[1]
        assertEquals("Whisk well to dissolve cocoa and cornstarch", second.prefix)
        assertEquals(null, second.ingredients)
    }
}

// language=JSON
private const val V2_MINI_CARD = """
{
  "schemaVersion": "2.0.0",
  "meta": {
    "id": "test-card",
    "theme": "Test",
    "cookDate": "2026-05-31",
    "household": { "people": 2, "daysCovered": 7 },
    "servingsPerMain": 14,
    "dailyTarget": { "calories": 2000, "proteinFloorGrams": 100 },
    "dailyActual": { "calories": 2000, "proteinGrams": 100, "carbsGrams": 200, "fatGrams": 80 }
  },
  "schedule": { "prep": [], "cook": [], "firstMeal": [] },
  "recipes": [],
  "cookPlan": {
    "phases": [
      {
        "id": "phase-1",
        "label": "Mise",
        "bowls": [
          {
            "id": "bowl-1",
            "bowlNumber": 1,
            "name": "Pudding Base",
            "forRecipeId": ["test-recipe"],
            "ingredients": [],
            "instruction": [
              {
                "instructionPrefix": "Combine the following directly in the saucepan off heat:",
                "ingredients": [
                  { "name": "whole milk", "quantity": "1,220", "unit": "g", "preparation": null },
                  { "name": "unsweetened cocoa", "quantity": "38", "unit": "g", "preparation": null },
                  { "name": "salt", "quantity": null, "unit": "pinch", "preparation": null }
                ]
              },
              {
                "instructionPrefix": "Whisk well to dissolve cocoa and cornstarch",
                "ingredients": null
              }
            ]
          }
        ]
      }
    ]
  }
}
"""

