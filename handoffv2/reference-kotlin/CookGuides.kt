@file:Suppress("unused")

package com.patchfox.mise.domain.cook

/*
 * REFERENCE ONLY — the NEW logic the Android session can't copy from existing code:
 * the app-side derivation of the Shared lanes from self-contained v3 recipes.
 *
 *   • Mise: merge bowl ingredients across the selected recipes by
 *     (ingredientId, unit, preparation); combinable=false never pools.
 *   • Cook: group `backgroundable` steps that cook the SAME thing — keyed by the
 *     step's full ingredient signature (sorted (ingredientId, preparation) pairs),
 *     so plain rice merges with plain rice, but coconut "rice & peas" does NOT merge
 *     with plain rice.
 *
 * Pure Kotlin, no Android deps — unit-testable as-is (see CookGuidesTest.kt). Reconcile
 * the domain types below with whatever your existing Recipe/drill-down model already has;
 * only the cook/mise shapes are new in v3.
 */

import com.patchfox.mise.data.dto.*

// ---------------------------------------------------------------------------
// Domain models (v3 cook/mise shapes)
// ---------------------------------------------------------------------------

typealias RecipeId = String

data class Recipe(
    val id: RecipeId,
    val name: String,
    val emoji: String?,
    val type: String,                 // "main" | "dessert"
    val misePhase: MisePhase,
    val cookPhase: CookPhase,
)

data class MisePhase(val bowls: List<MiseBowl>, val standalones: List<MiseStandalone>)
data class CookPhase(val steps: List<CookStep>)

data class MiseBowl(
    val id: String,
    val name: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    /** Populated only on app-derived SHARED bowls — which recipes contributed. */
    val sharedRecipeIds: List<RecipeId> = emptyList(),
)

data class MiseStandalone(val name: String, val ingredientId: String?, val prep: List<String>)

data class Ingredient(
    val ingredientId: String,
    val name: String,
    val quantity: Double?,
    val unit: String?,
    val preparation: String?,
    val combinable: Boolean = true,
    /** Populated only on merged shared lines — which recipes this line covers (for emojis). */
    val recipeIds: List<RecipeId> = emptyList(),
)

data class CookStep(
    val id: String,
    val backgroundable: Boolean,
    val steps: List<String>,
    val ingredients: List<Ingredient>,
    val timer: Timer?,
    val handsOff: List<String>,
    val isFinalStep: Boolean,
)

data class Timer(val label: String?, val durationSeconds: Double?, val durationDescription: String?)

// ---------------------------------------------------------------------------
// Use-case outputs
// ---------------------------------------------------------------------------

data class RecipeMiseGuide(val recipe: Recipe, val bowls: List<MiseBowl>)
data class RecipeCookGuide(val recipe: Recipe, val steps: List<CookStep>)

data class SharedStep(
    val id: String,
    val title: String,
    val recipeIds: List<RecipeId>,
    val task: List<String>,           // prose lines
    val ingredients: List<Ingredient>,
    val timer: Timer?,
    val runtime: String?,             // e.g. "~25 min"
)

// ---------------------------------------------------------------------------
// MISE: per-recipe lanes + an app-derived shared lane
// ---------------------------------------------------------------------------

class BuildMiseGuidesUseCase {

    private data class MiseKey(val ingredientId: String, val unit: String?, val preparation: String?)

    /** @return (per-recipe guides with shared ingredients removed, shared bowls). */
    operator fun invoke(recipes: List<Recipe>): Pair<List<RecipeMiseGuide>, List<MiseBowl>> {
        // 1. index every COMBINABLE mise ingredient occurrence by its merge key
        data class Occ(val recipeId: RecipeId, val ing: Ingredient)
        val byKey = LinkedHashMap<MiseKey, MutableList<Occ>>()
        for (r in recipes) for (b in r.misePhase.bowls) for (ing in b.ingredients) {
            if (!ing.combinable) continue
            byKey.getOrPut(MiseKey(ing.ingredientId, ing.unit, ing.preparation)) { mutableListOf() }
                .add(Occ(r.id, ing))
        }

        // 2. a key is SHARED iff it occurs in >= 2 distinct recipes
        val sharedKeys = byKey.filterValues { occ -> occ.map { it.recipeId }.distinct().size >= 2 }.keys

        // 3. one merged line per shared key (sum quantities; carry contributing recipes)
        val sharedLines = sharedKeys.map { key ->
            val occ = byKey.getValue(key)
            val qty = occ.map { it.ing.quantity }
            val summed = if (qty.all { it != null }) qty.filterNotNull().sum() else null
            Ingredient(
                ingredientId = key.ingredientId,
                name = occ.first().ing.name,
                quantity = summed,
                unit = key.unit,
                preparation = key.preparation,
                combinable = true,
                recipeIds = occ.map { it.recipeId }.distinct(),
            )
        }
        val sharedBowls = if (sharedLines.isEmpty()) emptyList()
        else listOf(
            MiseBowl(
                id = "shared-mise",
                name = "Shared prep",
                ingredients = sharedLines,
                steps = emptyList(),
                sharedRecipeIds = sharedLines.flatMap { it.recipeIds }.distinct(),
            )
        )

        // 4. per-recipe lanes: drop the shared ingredients (done once in the shared lane);
        //    keep combinable=false and single-recipe ingredients; drop now-empty bowls.
        val perRecipe = recipes.map { r ->
            val bowls = r.misePhase.bowls.mapNotNull { b ->
                val kept = b.ingredients.filter { ing ->
                    !ing.combinable || MiseKey(ing.ingredientId, ing.unit, ing.preparation) !in sharedKeys
                }
                if (kept.isEmpty()) null else b.copy(ingredients = kept)
            }
            RecipeMiseGuide(r, bowls)
        }
        return perRecipe to sharedBowls
    }
}

// ---------------------------------------------------------------------------
// COOK: per-recipe lanes + an app-derived shared cook lane (backgroundable hoist)
// ---------------------------------------------------------------------------

class BuildCookGuidesUseCase {

    /** A backgroundable step's "what am I cooking" signature: the sorted set of its
     * ingredients' (ingredientId, preparation). Plain white-rice ≠ rice+beans. */
    private fun signature(step: CookStep): List<Pair<String, String?>> =
        step.ingredients.map { it.ingredientId to it.preparation }.sortedBy { it.first + (it.second ?: "") }

    /** @return (per-recipe cook guides with shared steps removed, shared steps). */
    operator fun invoke(recipes: List<Recipe>): Pair<List<RecipeCookGuide>, List<SharedStep>> {
        data class Occ(val recipeId: RecipeId, val step: CookStep)
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
            // Merge note: the dry quantities differ per recipe — sum them per ingredientId so the
            // single shared batch covers everyone. Prose here uses the first recipe's lines as a
            // template; refine the wording to the summed amount in the UI/use-case as you prefer.
            val summed = first.ingredients.map { ing ->
                val total = occ.mapNotNull { o -> o.step.ingredients.firstOrNull { it.ingredientId == ing.ingredientId }?.quantity }
                ing.copy(quantity = total.takeIf { it.isNotEmpty() }?.sum(), recipeIds = occ.map { it.recipeId }.distinct())
            }
            SharedStep(
                id = "shared-cook-" + sig.joinToString("-") { it.first },
                title = first.label ?: first.steps.firstOrNull().orEmpty().take(48),
                recipeIds = occ.map { it.recipeId }.distinct(),
                task = first.steps,
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

// ---------------------------------------------------------------------------
// DTO → domain mapping (mechanical — fold into your existing CookCardMappers)
// ---------------------------------------------------------------------------

fun CookCardV3Dto.toRecipes(): List<Recipe> = recipes.map { it.toDomain() }

fun RecipeV3Dto.toDomain() = Recipe(
    id = id, name = name, emoji = emoji, type = type,
    misePhase = MisePhase(
        bowls = (misePhase?.bowls ?: emptyList()).map { b ->
            MiseBowl(b.id, b.name, b.ingredients.map { it.toDomain() }, b.steps)
        },
        standalones = (misePhase?.standalones ?: emptyList()).map { MiseStandalone(it.name, it.ingredientId, it.prep) },
    ),
    cookPhase = CookPhase(cookPhase.steps.map { s ->
        CookStep(
            id = s.id, backgroundable = s.backgroundable, steps = s.steps,
            ingredients = s.ingredients.map { it.toDomain() },
            timer = s.timer?.let { Timer(it.label, it.durationSeconds, it.durationDescription) },
            handsOff = s.handsOff, isFinalStep = s.isFinalStep,
        )
    }),
)

private fun IngredientV3Dto.toDomain() =
    Ingredient(ingredientId, name, quantityAsDouble(), unit, preparation, combinable)
