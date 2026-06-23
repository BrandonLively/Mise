package com.patchfox.mise.domain.usecase

import com.patchfox.mise.data.dto.tidy
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.StepTimer

/**
 * App-side derivation of the SHARED lanes from self-contained v3 recipes — the new
 * v3 logic that replaces author-time shared-mise consolidation. Pure Kotlin (no Android),
 * keyed off canonical ingredientId so it works for ANY user-selected recipe set
 * (including custom cross-week plans). Spec: handoffv2/reference-kotlin/CookGuidesTest.kt.
 */

/** A recipe's mise lane: its own bowls (with shared ingredients removed) + standalones. */
data class RecipeMiseGuide(val recipe: Recipe, val bowls: List<MiseBowl>)

/** A recipe's cook lane: its own ordered steps (with hoisted shared steps removed). */
data class RecipeCookGuide(val recipe: Recipe, val steps: List<CookStep>)

/** An app-derived shared cook step (e.g. "cook the rice") covering multiple recipes. */
data class SharedCookStep(
    val id: String,
    val title: String,
    val recipeIds: List<RecipeId>,
    val steps: List<String>,
    val ingredients: List<Ingredient>,
    val timer: StepTimer?,
    val runtime: String?,
)

// ---------------------------------------------------------------------------
// MISE: strictly per-recipe — there is NO shared mise lane
// ---------------------------------------------------------------------------

class BuildMiseGuidesUseCase {

    /**
     * Mise is intentionally PER-RECIPE: each recipe keeps ALL of its own bowls even
     * when another recipe preps the same ingredients, because the bowls are physically
     * portioned for that recipe's cook steps. There is deliberately no shared mise lane
     * — only the cook phase hoists shared backgroundable work (see [BuildCookGuidesUseCase]).
     */
    operator fun invoke(recipes: List<Recipe>): List<RecipeMiseGuide> =
        recipes.map { RecipeMiseGuide(it, it.misePhase.bowls) }
}

// ---------------------------------------------------------------------------
// COOK: per-recipe lanes + an app-derived shared cook lane (backgroundable hoist)
// ---------------------------------------------------------------------------

class BuildCookGuidesUseCase {

    private data class Occ(val recipeId: RecipeId, val step: CookStep)

    /** A backgroundable step's "what am I cooking" signature: the sorted set of its
     * ingredients' (ingredientId, preparation). Plain white-rice != rice & peas. */
    private fun signature(step: CookStep): List<Pair<String, String?>> =
        step.ingredients
            .map { it.ingredientId to it.preparation }
            .sortedBy { it.first + (it.second ?: "") }

    /** @return (per-recipe cook guides with shared steps removed, shared steps). */
    operator fun invoke(recipes: List<Recipe>): Pair<List<RecipeCookGuide>, List<SharedCookStep>> {
        val byKey = LinkedHashMap<List<Pair<String, String?>>, MutableList<Occ>>()
        for (r in recipes) for (s in r.cookPhase.steps) {
            if (!s.backgroundable) continue
            val sig = signature(s)
            if (sig.isEmpty()) continue            // no ingredients → can't safely hoist; leave per-recipe
            byKey.getOrPut(sig) { mutableListOf() }.add(Occ(r.id, s))
        }

        val sharedSigs = byKey.filterValues { occ -> occ.map { it.recipeId }.distinct().size >= 2 }.keys
        val sharedStepIds = sharedSigs.flatMap { byKey.getValue(it) }.map { it.step.id }.toSet()

        val shared = sharedSigs.map { sig ->
            val occ = byKey.getValue(sig)
            val first = occ.first().step
            val recipeIds = occ.map { it.recipeId }.distinct()
            // Sum the per-recipe dry amounts so the single shared batch covers everyone.
            val summed = first.ingredients.map { ing ->
                val totals = occ.mapNotNull { o ->
                    o.step.ingredients.firstOrNull { it.ingredientId == ing.ingredientId }?.quantity
                }
                val total = totals.takeIf { it.isNotEmpty() }?.sum()
                ing.copy(
                    quantity = total,
                    quantityDisplay = total?.tidy() ?: ing.quantityDisplay,
                    recipeIds = recipeIds,
                )
            }
            SharedCookStep(
                id = "shared-cook-" + sig.joinToString("|") { it.first + ":" + (it.second ?: "") },
                title = first.label ?: first.steps.firstOrNull().orEmpty().take(48),
                recipeIds = recipeIds,
                steps = first.steps,
                ingredients = summed,
                timer = first.timer,
                runtime = first.timer?.durationDescription,
            )
        }

        val perRecipe = recipes.map { r ->
            RecipeCookGuide(r, r.cookPhase.steps.filter { it.id !in sharedStepIds })
        }
        return perRecipe to shared
    }
}
