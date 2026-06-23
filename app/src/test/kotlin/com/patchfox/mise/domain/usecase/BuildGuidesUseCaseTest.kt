package com.patchfox.mise.domain.usecase

import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookPhase
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.Macro
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.MisePhase
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.RecipeType
import com.patchfox.mise.domain.model.RecipeYield
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.ui.theme.RecipeColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/*
 * Executable spec for the app-side Shared-lane derivation (BuildMiseGuidesUseCase /
 * BuildCookGuidesUseCase). Ported from handoffv2/reference-kotlin/CookGuidesTest.kt to
 * the real cook-card v3 domain.model types. Pure domain (no JSON / Android), so it runs
 * under plain JUnit5 Jupiter. The fixture mirrors the Caribbean sample: three recipes
 * share rice, but only the two that cook PLAIN rice merge — coconut "rice & peas" stays
 * on its own lane.
 */
class BuildGuidesUseCaseTest {

    // --- tiny builders: construct real domain.model types with empty defaults for
    //     irrelevant fields, focusing on misePhase.bowls + cookPhase.steps. ---

    private fun ing(
        id: String,
        name: String,
        q: Double?,
        unit: String?,
        prep: String? = null,
        comb: Boolean = true,
    ) = Ingredient(
        ingredientId = id,
        name = name,
        quantity = q,
        quantityDisplay = q?.let { if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString() },
        unit = unit,
        preparation = prep,
        combinable = comb,
    )

    private fun bowl(id: String, name: String, vararg ings: Ingredient) =
        MiseBowl(id = BowlId(id), name = name, ingredients = ings.toList(), steps = emptyList())

    private fun step(
        id: String,
        bg: Boolean,
        prose: String,
        vararg ings: Ingredient,
        final: Boolean = false,
    ) = CookStep(
        id = StepId(id),
        backgroundable = bg,
        label = null,
        clockTime = null,
        steps = listOf(prose),
        ingredients = ings.toList(),
        handsOff = emptyList(),
        isFinalStep = final,
        timer = null,
    )

    private fun recipe(
        id: String,
        emoji: String,
        type: RecipeType,
        bowls: List<MiseBowl>,
        steps: List<CookStep>,
    ) = Recipe(
        id = RecipeId(id),
        name = id,
        type = type,
        emoji = emoji,
        color = RecipeColor.BLUE,
        description = "",
        source = null,
        yield = RecipeYield(null, null, null),
        perServing = Macro(0.0, 0.0, 0.0, 0.0),
        timing = null,
        equipment = emptyList(),
        assembleAndStore = emptyList(),
        prepSchedule = emptyList(),
        misePhase = MisePhase(durationMinutes = null, description = null, bowls = bowls, standalones = emptyList()),
        cookPhase = CookPhase(durationMinutes = null, description = null, steps = steps),
        macroBreakdown = emptyList(),
        reheat = null,
        deviationsFromPlan = emptyList(),
    )

    // chicken — rice & peas (white-rice + kidney-beans); jerk finishing bowl
    private val chicken = recipe(
        "chicken", "🍗", RecipeType.MAIN,
        bowls = listOf(
            bowl(
                "a-b1", "Jerk finishing",
                ing("lime", "lime", 1.0, "each", "juiced"),
                ing("allspice", "ground allspice", 2.0, "tsp"),
                ing("salt", "salt", null, "pinch", comb = false), // combinable=false → never pools
            ),
        ),
        steps = listOf(
            step(
                "a-c1", bg = true, "Rice & peas: simmer coconut + beans, add rice",
                ing("white-rice", "white rice", 340.0, "g", "dry, rinsed"),
                ing("kidney-beans", "kidney beans", 1.0, "can", "drained"),
            ),
            step("a-c2", bg = false, "Air-fry chicken 16-18 min"),
            step("a-c3", bg = false, "Slice; portion", final = true),
        ),
    )

    // salmon — PLAIN white rice; mango salsa + dry rub
    private val salmon = recipe(
        "salmon", "🐟", RecipeType.MAIN,
        bowls = listOf(
            bowl(
                "b-b1", "Mango salsa",
                ing("mango", "mango", 2.0, "each", "diced"),
                ing("lime", "lime", 1.0, "each", "juiced"),
                ing("cilantro", "cilantro", 20.0, "g", "chopped"),
            ),
            bowl(
                "b-b2", "Dry jerk rub",
                ing("allspice", "ground allspice", 2.0, "tsp"),
                ing("cayenne", "cayenne", 0.5, "tsp"),
            ),
        ),
        steps = listOf(
            step(
                "b-c1", bg = true, "Start the white rice",
                ing("white-rice", "white rice", 280.0, "g", "dry, rinsed"),
            ),
            step("b-c2", bg = false, "Air-fry salmon 8-9 min"),
            step("b-c3", bg = false, "Portion", final = true),
        ),
    )

    // stew — also PLAIN white rice
    private val stew = recipe(
        "stew", "🫘", RecipeType.MAIN,
        bowls = listOf(bowl("c-b1", "Stew veg", ing("yellow-onion", "yellow onion", 1.0, "each", "diced"))),
        steps = listOf(
            step(
                "c-c1", bg = true, "Start the white rice",
                ing("white-rice", "white rice", 200.0, "g", "dry, rinsed"),
            ),
            step("c-c2", bg = false, "Simmer stew; portion", final = true),
        ),
    )

    // dessert — NO cooking (single assemble/chill step, not backgroundable)
    private val pudding = recipe(
        "pudding", "🍮", RecipeType.DESSERT,
        bowls = listOf(bowl("d-b1", "Pudding base", ing("whole-milk", "whole milk", 800.0, "g", null))),
        steps = listOf(
            step("d-c1", bg = false, "Cover and refrigerate overnight; portion into 7 cups", final = true),
        ),
    )

    private val plan = listOf(chicken, salmon, stew, pudding)

    // ===================== COOK =====================

    @Test
    fun `plain rice merges across the two recipes that cook it`() {
        val (_, shared) = BuildCookGuidesUseCase()(plan)
        assertEquals(1, shared.size, "salmon + stew plain rice should hoist to ONE shared cook step")
        assertEquals(listOf(RecipeId("salmon"), RecipeId("stew")), shared[0].recipeIds)
        // the shared batch sums the dry amounts (280 + 200)
        val rice = shared[0].ingredients.first { it.ingredientId == "white-rice" }
        assertEquals(480.0, rice.quantity)
    }

    @Test
    fun `coconut rice and peas does NOT merge with plain rice`() {
        val (perRecipe, shared) = BuildCookGuidesUseCase()(plan)
        // not in shared
        assertTrue(shared.none { s -> RecipeId("chicken") in s.recipeIds })
        // still on the chicken lane (all 3 of its steps remain)
        val chick = perRecipe.first { it.recipe.id == RecipeId("chicken") }
        assertEquals(3, chick.steps.size)
        assertTrue(chick.steps.any { it.id == StepId("a-c1") }, "rice & peas stays on chicken's own lane")
    }

    @Test
    fun `hoisted steps are removed from the per-recipe lanes`() {
        val (perRecipe, _) = BuildCookGuidesUseCase()(plan)
        val salmonLane = perRecipe.first { it.recipe.id == RecipeId("salmon") }
        assertTrue(salmonLane.steps.none { it.id == StepId("b-c1") }, "shared rice step removed from salmon's lane")
        assertEquals(2, salmonLane.steps.size)
    }

    @Test
    fun `no-cook recipe yields a trivial lane and never shares`() {
        val (perRecipe, shared) = BuildCookGuidesUseCase()(plan)
        val dessert = perRecipe.first { it.recipe.id == RecipeId("pudding") }
        assertEquals(1, dessert.steps.size)
        assertTrue(shared.none { RecipeId("pudding") in it.recipeIds })
    }

    @Test
    fun `shared cook steps that differ only by preparation get distinct ids`() {
        // Two recipe pairs both cook white-rice, but with DIFFERENT preparation, so the
        // hoist signature differs → TWO distinct shared steps. Their synthetic ids must
        // be distinct (the id includes preparation), or their started-timer Room rows collide.
        fun riceRecipe(id: String, prep: String) = recipe(
            id, "🍚", RecipeType.MAIN,
            bowls = emptyList(),
            steps = listOf(
                step("$id-c1", bg = true, "Start the rice", ing("white-rice", "white rice", 200.0, "g", prep)),
                step("$id-c2", bg = false, "Plate", final = true),
            ),
        )
        val pool = listOf(
            riceRecipe("plainA", "dry, rinsed"),
            riceRecipe("plainB", "dry, rinsed"),
            riceRecipe("toastedA", "toasted"),
            riceRecipe("toastedB", "toasted"),
        )
        val (_, shared) = BuildCookGuidesUseCase()(pool)
        assertEquals(2, shared.size, "the plain-rice pair and the toasted-rice pair hoist to TWO distinct shared steps")
        assertEquals(
            shared.size,
            shared.map { it.id }.distinct().size,
            "shared step ids must be unique — preparation is part of the id",
        )
    }

    // ===================== MISE (per-recipe; no shared lane) =====================

    @Test
    fun `mise is per-recipe — every recipe keeps all of its own bowls intact`() {
        val guides = BuildMiseGuidesUseCase()(plan)
        assertEquals(plan.size, guides.size, "one mise guide per recipe")

        // chicken keeps its whole jerk-finishing bowl (lime + allspice + salt) —
        // nothing is hoisted into a shared lane.
        val chick = guides.first { it.recipe.id == RecipeId("chicken") }
        val jerk = chick.bowls.first { it.id == BowlId("a-b1") }.ingredients.map { it.ingredientId }
        assertEquals(setOf("lime", "allspice", "salt"), jerk.toSet())

        // salmon keeps lime in its own salsa bowl even though chicken also uses lime —
        // mise bowls are physically per-recipe and are never pooled.
        val salm = guides.first { it.recipe.id == RecipeId("salmon") }
        val salsa = salm.bowls.first { it.id == BowlId("b-b1") }.ingredients.map { it.ingredientId }
        assertEquals(setOf("mango", "lime", "cilantro"), salsa.toSet())
    }
}
