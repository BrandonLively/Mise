package com.patchfox.mise.domain.model

import com.patchfox.mise.ui.theme.RecipeColor
import kotlinx.datetime.LocalDate

@JvmInline
value class RecipeId(val value: String)

@JvmInline
value class BowlId(val value: String)

@JvmInline
value class StepId(val value: String)

@JvmInline
value class PrepTaskId(val value: String)

@JvmInline
value class CookCardId(val value: String)

/**
 * cook-card v3 domain model. The card is just [meta-ish fields] + a list of
 * fully self-contained [Recipe]s. There is no top-level cookPlan, schedule,
 * household, or dailyActual: daily totals are DERIVED by the consumer
 * (Σ recipes' [Recipe.perServing]); the shared COOK lane is DERIVED app-side
 * (see BuildCookGuidesUseCase). Mise is strictly per-recipe (no shared lane).
 */
data class CookCard(
    val id: CookCardId,
    val theme: String,
    val cookDate: LocalDate,
    val servingsPerMain: Int,
    val servingsPerDessert: Int?,
    val dailyTarget: DailyTarget,
    val recipes: List<Recipe>,
)

data class DailyTarget(
    val calories: Double,
    val proteinFloorGrams: Double,
    val leanPreference: String?,
    val tolerancePercent: Double?,
)

enum class RecipeType { MAIN, DESSERT, SIDE, SNACK, BREAKFAST, OTHER }

/** Callout styling tag — kept for in-app recipe-note callouts (no longer authored on the card). */
enum class ColorTag { NOTE, TIP, IMPORTANT, WARNING, CAUTION }

data class Recipe(
    val id: RecipeId,
    val name: String,
    val type: RecipeType,
    val emoji: String,
    val color: RecipeColor,
    val description: String,
    val source: RecipeSource?,
    val yield: RecipeYield,
    val perServing: Macro,
    val timing: RecipeTiming?,
    val equipment: List<String>,
    val assembleAndStore: List<String>,
    val prepSchedule: List<PrepStep>,
    val misePhase: MisePhase,
    val cookPhase: CookPhase,
    val macroBreakdown: List<MacroIngredient>,
    val reheat: ReheatSpec?,
    val deviationsFromPlan: List<String>,
)

data class RecipeSource(val author: String?, val url: String?)

data class RecipeYield(
    val servings: Int?,
    val containerSize: String?,
    val perServingContainerNote: String?,
)

data class Macro(
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)

data class RecipeTiming(
    val activeMinutes: Double?,
    val totalMinutesText: String?,
    val keeps: String?,
)

/**
 * Per-recipe prep in RELATIVE time ([offset], e.g. "night-before", "T-36h").
 * The client may merge identical [combinable] prep across a multi-recipe selection.
 */
data class PrepStep(
    val offset: String,
    val label: String,
    val tasks: List<String>,
    val combinable: Boolean,
    val rationale: String?,
)

data class MisePhase(
    val durationMinutes: Double?,
    val description: String?,
    val bowls: List<MiseBowl>,
    val standalones: List<MiseStandalone>,
)

data class CookPhase(
    val durationMinutes: Double?,
    val description: String?,
    val steps: List<CookStep>,
)

data class MiseBowl(
    val id: BowlId,
    val name: String,
    val ingredients: List<Ingredient>,
    val steps: List<String>,
    /** Populated only on app-derived SHARED bowls — which recipes contributed. */
    val sharedRecipeIds: List<RecipeId> = emptyList(),
)

/**
 * The one ingredient shape, used by both mise bowls and cook steps.
 * [quantity] is the parsed numeric amount (for cross-recipe merge summing) or null
 * when the authored value isn't cleanly numeric; [quantityDisplay] is always the
 * human form ("1 can", "½ tsp", "~250g", "2").
 */
data class Ingredient(
    val ingredientId: String,
    val name: String,
    val quantity: Double?,
    val quantityDisplay: String?,
    val unit: String?,
    val preparation: String?,
    val combinable: Boolean = true,
    /** Populated only on merged shared lines — which recipes this line covers (for emojis). */
    val recipeIds: List<RecipeId> = emptyList(),
)

data class MiseStandalone(
    val name: String,
    val ingredientId: String?,
    val prep: List<String>,
)

data class CookStep(
    val id: StepId,
    val backgroundable: Boolean,
    val label: String?,
    val clockTime: String?,
    val steps: List<String>,
    val ingredients: List<Ingredient>,
    val handsOff: List<String>,
    val isFinalStep: Boolean,
    val timer: StepTimer?,
)

data class StepTimer(
    val title: String,
    val durationSeconds: Int,
    val maxDurationSeconds: Int?,
    val durationDescription: String?,
)

data class MacroIngredient(
    val name: String,
    val ingredientId: String?,
    val quantity: Double?,
    val unit: String?,
    val displayHint: String?,
    val source: String?,
    val macro: Macro,
)

data class ReheatSpec(
    val method: String,
    val timeSeconds: Int?,
    val extraSteps: List<String>,
)

// ---------------------------------------------------------------------------
// Convenience accessors / derivations
// ---------------------------------------------------------------------------

fun CookCard.recipeById(id: RecipeId): Recipe? = recipes.firstOrNull { it.id == id }

/** Daily macro total = Σ each recipe's per-serving row (v3 derives this consumer-side). */
fun CookCard.dailyTotal(): Macro = recipes.fold(Macro(0.0, 0.0, 0.0, 0.0)) { acc, r ->
    Macro(
        calories = acc.calories + r.perServing.calories,
        proteinGrams = acc.proteinGrams + r.perServing.proteinGrams,
        carbsGrams = acc.carbsGrams + r.perServing.carbsGrams,
        fatGrams = acc.fatGrams + r.perServing.fatGrams,
    )
}

/** Active minutes for the card = Σ recipes' mise + cook phase durations (rounded). */
fun CookCard.totalActiveMinutes(): Int = recipes.sumOf { r ->
    ((r.misePhase.durationMinutes ?: 0.0) + (r.cookPhase.durationMinutes ?: 0.0))
}.toInt()

operator fun Macro.plus(other: Macro): Macro = Macro(
    calories = calories + other.calories,
    proteinGrams = proteinGrams + other.proteinGrams,
    carbsGrams = carbsGrams + other.carbsGrams,
    fatGrams = fatGrams + other.fatGrams,
)

/**
 * One serving of this recipe, summed from its per-ingredient [Recipe.macroBreakdown] rows
 * (falls back to the derived [Recipe.perServing] when the breakdown is absent).
 */
fun Recipe.macrosFromBreakdown(): Macro =
    if (macroBreakdown.isEmpty()) perServing
    else macroBreakdown.fold(Macro(0.0, 0.0, 0.0, 0.0)) { acc, mi -> acc + mi.macro }

/**
 * Combined macros for one serving of every recipe in the plan — derived per-recipe
 * (Σ each recipe's [macrosFromBreakdown]), so it stays correct for any chosen subset.
 */
fun CookCard.combinedMacros(): Macro =
    recipes.fold(Macro(0.0, 0.0, 0.0, 0.0)) { acc, r -> acc + r.macrosFromBreakdown() }
