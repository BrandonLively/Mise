package com.patchfox.mise.domain.cook

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertNotNull

/*
 * Executable spec for the app-side Shared-lane derivation. Pure domain (no JSON/Android),
 * so it runs under plain kotlin.test / JUnit. The fixture mirrors the Caribbean sample:
 * three recipes share rice, but only the two that cook PLAIN rice merge — coconut
 * "rice & peas" stays on its own lane.
 */
class CookGuidesTest {

    // --- tiny builders ---
    private fun ing(id: String, name: String, q: Double?, unit: String?, prep: String? = null, comb: Boolean = true) =
        Ingredient(id, name, q, unit, prep, comb)

    private fun bowl(id: String, name: String, vararg ings: Ingredient) = MiseBowl(id, name, ings.toList(), emptyList())

    private fun step(id: String, bg: Boolean, prose: String, vararg ings: Ingredient, final: Boolean = false) =
        CookStep(id, bg, listOf(prose), ings.toList(), null, emptyList(), final)

    private fun recipe(id: String, emoji: String, type: String, bowls: List<MiseBowl>, steps: List<CookStep>) =
        Recipe(id, id, emoji, type, MisePhase(bowls, emptyList()), CookPhase(steps))

    // chicken — rice & peas (white-rice + kidney-beans); jerk finishing bowl
    private val chicken = recipe(
        "chicken", "🍗", "main",
        bowls = listOf(bowl("a-b1", "Jerk finishing",
            ing("lime", "lime", 1.0, "each", "juiced"),
            ing("allspice", "ground allspice", 2.0, "tsp"),
            ing("salt", "salt", null, "pinch", comb = false))),   // combinable=false → never pools
        steps = listOf(
            step("a-c1", bg = true, "Rice & peas: simmer coconut + beans, add rice",
                ing("white-rice", "white rice", 340.0, "g", "dry, rinsed"),
                ing("kidney-beans", "kidney beans", 1.0, "can", "drained")),
            step("a-c2", bg = false, "Air-fry chicken 16-18 min"),
            step("a-c3", bg = false, "Slice; portion", final = true)),
    )

    // salmon — PLAIN white rice; mango salsa + dry rub
    private val salmon = recipe(
        "salmon", "🐟", "main",
        bowls = listOf(
            bowl("b-b1", "Mango salsa",
                ing("mango", "mango", 2.0, "each", "diced"),
                ing("lime", "lime", 1.0, "each", "juiced"),
                ing("cilantro", "cilantro", 20.0, "g", "chopped")),
            bowl("b-b2", "Dry jerk rub",
                ing("allspice", "ground allspice", 2.0, "tsp"),
                ing("cayenne", "cayenne", 0.5, "tsp"))),
        steps = listOf(
            step("b-c1", bg = true, "Start the white rice",
                ing("white-rice", "white rice", 280.0, "g", "dry, rinsed")),
            step("b-c2", bg = false, "Air-fry salmon 8-9 min"),
            step("b-c3", bg = false, "Portion", final = true)),
    )

    // stew — also PLAIN white rice
    private val stew = recipe(
        "stew", "🫘", "main",
        bowls = listOf(bowl("c-b1", "Stew veg", ing("yellow-onion", "yellow onion", 1.0, "each", "diced"))),
        steps = listOf(
            step("c-c1", bg = true, "Start the white rice",
                ing("white-rice", "white rice", 200.0, "g", "dry, rinsed")),
            step("c-c2", bg = false, "Simmer stew; portion", final = true)),
    )

    // dessert — NO cooking (single assemble/chill step, not backgroundable)
    private val pudding = recipe(
        "pudding", "🍮", "dessert",
        bowls = listOf(bowl("d-b1", "Pudding base", ing("whole-milk", "whole milk", 800.0, "g", null))),
        steps = listOf(step("d-c1", bg = false, "Cover and refrigerate overnight; portion into 7 cups", final = true)),
    )

    private val plan = listOf(chicken, salmon, stew, pudding)

    // ===================== COOK =====================

    @Test fun `plain rice merges across the two recipes that cook it`() {
        val (_, shared) = BuildCookGuidesUseCase()(plan)
        assertEquals(1, shared.size, "salmon + stew plain rice should hoist to ONE shared cook step")
        assertEquals(listOf("salmon", "stew"), shared[0].recipeIds)
        // the shared batch sums the dry amounts (280 + 200)
        val rice = shared[0].ingredients.first { it.ingredientId == "white-rice" }
        assertEquals(480.0, rice.quantity)
    }

    @Test fun `coconut rice and peas does NOT merge with plain rice`() {
        val (perRecipe, shared) = BuildCookGuidesUseCase()(plan)
        // not in shared
        assertTrue(shared.none { s -> "chicken" in s.recipeIds })
        // still on the chicken lane (all 3 of its steps remain)
        val chick = perRecipe.first { it.recipe.id == "chicken" }
        assertEquals(3, chick.steps.size)
        assertTrue(chick.steps.any { it.id == "a-c1" }, "rice & peas stays on chicken's own lane")
    }

    @Test fun `hoisted steps are removed from the per-recipe lanes`() {
        val (perRecipe, _) = BuildCookGuidesUseCase()(plan)
        val salmonLane = perRecipe.first { it.recipe.id == "salmon" }
        assertTrue(salmonLane.steps.none { it.id == "b-c1" }, "shared rice step removed from salmon's lane")
        assertEquals(2, salmonLane.steps.size)
    }

    @Test fun `no-cook recipe yields a trivial lane and never shares`() {
        val (perRecipe, shared) = BuildCookGuidesUseCase()(plan)
        val dessert = perRecipe.first { it.recipe.id == "pudding" }
        assertEquals(1, dessert.steps.size)
        assertTrue(shared.none { "pudding" in it.recipeIds })
    }

    // ===================== MISE =====================

    @Test fun `shared mise merges combinable ingredients across recipes and sums quantities`() {
        val (_, sharedBowls) = BuildMiseGuidesUseCase()(plan)
        assertEquals(1, sharedBowls.size)
        val shared = sharedBowls[0].ingredients.associateBy { it.ingredientId }

        val lime = assertNotNull(shared["lime"], "lime (juiced) is in chicken + salmon → shared")
        assertEquals(2.0, lime.quantity)                       // 1 + 1
        assertEquals(setOf("chicken", "salmon"), lime.recipeIds.toSet())

        val allspice = assertNotNull(shared["allspice"], "allspice is in chicken + salmon → shared")
        assertEquals(4.0, allspice.quantity)                   // 2 + 2

        // single-recipe + combinable=false items never pool
        assertNull(shared["mango"], "mango is only in salmon → not shared")
        assertNull(shared["cayenne"], "cayenne is only in salmon → not shared")
        assertNull(shared["salt"], "salt is combinable=false → never pools")
    }

    @Test fun `per-recipe mise keeps non-shared and combinable-false ingredients`() {
        val (perRecipe, _) = BuildMiseGuidesUseCase()(plan)

        val chick = perRecipe.first { it.recipe.id == "chicken" }
        val jerk = chick.bowls.first { it.id == "a-b1" }.ingredients.map { it.ingredientId }
        assertTrue("salt" in jerk, "combinable=false salt stays on the chicken lane")
        assertTrue("lime" !in jerk && "allspice" !in jerk, "shared items moved out of the per-recipe bowl")

        val salm = perRecipe.first { it.recipe.id == "salmon" }
        val salsa = salm.bowls.first { it.id == "b-b1" }.ingredients.map { it.ingredientId }
        assertEquals(setOf("mango", "cilantro"), salsa.toSet(), "lime moved to shared; mango+cilantro stay")
    }
}
