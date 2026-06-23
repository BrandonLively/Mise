@file:Suppress("unused")

package com.patchfox.mise.data.dto

/*
 * REFERENCE ONLY — not wired into the build. Drop the real versions under
 * app/src/main/kotlin/.../data/dto/ and reconcile with your existing parser
 * (CookCardParser: ignoreUnknownKeys = true, isLenient = true, coerceInputValues = true).
 *
 * These DTOs mirror cook-card.v3.schema.json (see ../cook-card.v3.schema.json).
 * Top level is meta + recipes[]; each recipe is self-contained (misePhase + cookPhase).
 * There is NO cookPlan, NO phase-0/1/2, NO schedule/macroReconciliation/household —
 * those were required in the v2 DTOs and their absence is exactly why a v3 card fails
 * to parse against the old CookCardDto. This is a hard cutover.
 */

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

@Serializable
data class CookCardV3Dto(
    val schemaVersion: String,
    val meta: MetaV3Dto,
    val recipes: List<RecipeV3Dto>,
    // uploadedAt is a Firestore server-timestamp; not present in the JSON itself.
)

@Serializable
data class MetaV3Dto(
    val id: String,
    val theme: String,
    val cookDate: String,
    val servingsPerMain: Int,
    val servingsPerDessert: Int? = null,
    val dailyTarget: DailyTargetV3Dto,
    val generation: JsonElement? = null,
)

@Serializable
data class DailyTargetV3Dto(
    val calories: Double,
    val proteinFloorGrams: Double,
    val leanPreference: String? = null,
    val tolerancePercent: Double? = null,
)

@Serializable
data class RecipeV3Dto(
    val id: String,
    val name: String,
    val type: String,                         // "main" | "dessert"
    val emoji: String? = null,
    val color: String? = null,                // BLUE|GREEN|PURPLE|YELLOW|RED
    val description: String? = null,
    val source: SourceV3Dto? = null,
    val yield: YieldV3Dto? = null,
    val perServing: MacroV3Dto,               // DERIVED = Σ macroBreakdown rows
    val timing: TimingV3Dto? = null,
    val equipment: List<String> = emptyList(),
    val assembleAndStore: List<String> = emptyList(),
    val prepSchedule: List<PrepStepV3Dto> = emptyList(),
    val misePhase: MisePhaseV3Dto? = null,
    val cookPhase: CookPhaseV3Dto,
    val macroBreakdown: MacroBreakdownV3Dto? = null,
    val reheat: ReheatV3Dto,
    val deviationsFromPlan: List<String> = emptyList(),
)

@Serializable data class SourceV3Dto(val author: String? = null, val url: String? = null)

@Serializable
data class YieldV3Dto(
    val servings: Int? = null,
    val containerSize: String? = null,
    val perServingContainerNote: String? = null,
)

@Serializable
data class MacroV3Dto(
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
)

@Serializable
data class TimingV3Dto(
    val activeMinutes: Double? = null,
    val totalMinutes: JsonElement? = null,    // number OR string
    val keeps: String? = null,
)

@Serializable
data class PrepStepV3Dto(
    val offset: String,                       // "night-before" | "T-36h" | ...
    val label: String,
    val tasks: List<String> = emptyList(),
    val combinable: Boolean = true,
    val rationale: String? = null,
)

// ---- Phases (named objects, NOT an array) ----

@Serializable
data class MisePhaseV3Dto(
    val durationMinutes: Double? = null,
    val description: String? = null,
    val bowls: List<MiseBowlV3Dto> = emptyList(),
    val standalones: List<MiseStandaloneV3Dto> = emptyList(),
)

@Serializable
data class CookPhaseV3Dto(
    val durationMinutes: Double? = null,
    val description: String? = null,
    val steps: List<CookStepV3Dto>,           // ≥1; the leading backgroundable steps are the old "Phase 0"
)

@Serializable
data class MiseBowlV3Dto(
    val id: String,
    val name: String,
    val ingredients: List<IngredientV3Dto> = emptyList(),
    val steps: List<String> = emptyList(),    // prose prep (renamed from v2 `instruction`)
)

@Serializable
data class MiseStandaloneV3Dto(
    val name: String,
    val ingredientId: String? = null,
    val prep: List<String> = emptyList(),
)

@Serializable
data class IngredientV3Dto(
    val ingredientId: String,                 // canonical slug — the merge key
    val name: String,
    val quantity: JsonElement? = null,        // number | string | null  → parse with quantityAsDouble()
    val unit: String? = null,
    val preparation: String? = null,
    val combinable: Boolean = true,           // false = never pooled across recipes
)

@Serializable
data class CookStepV3Dto(
    val id: String,
    val backgroundable: Boolean = false,      // true = long-running hands-off; leads the cookPhase (the old Phase 0)
    val label: String? = null,
    val clockTime: String? = null,
    val steps: List<String>,                  // prose lines (renamed from v2 `task`; no InstructionStep wrapper)
    val ingredients: List<IngredientV3Dto> = emptyList(),
    val timer: TimerV3Dto? = null,
    val handsOff: List<String> = emptyList(), // "Meanwhile" block
    val isFinalStep: Boolean = false,
)

@Serializable
data class TimerV3Dto(
    val label: String? = null,                // v3 uses `label` (v2 doc called it `title`)
    val durationSeconds: Double? = null,
    val maxDurationSeconds: Double? = null,
    val durationDescription: String? = null,
)

@Serializable
data class MacroBreakdownV3Dto(val ingredients: List<MacroIngredientV3Dto> = emptyList())

@Serializable
data class MacroIngredientV3Dto(
    val name: String,
    val ingredientId: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val displayHint: String? = null,
    val source: String? = null,
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val carbsGrams: Double = 0.0,
    val fatGrams: Double = 0.0,
)

@Serializable
data class ReheatV3Dto(
    val method: String,
    val timeSeconds: Double? = null,
    val extraSteps: List<String> = emptyList(),
)

/** Ingredient.quantity is number|string|null in the schema. Pull a Double when it's
 * numeric (or a clean numeric string); null otherwise (e.g. "to taste"). */
fun IngredientV3Dto.quantityAsDouble(): Double? = when (val q = quantity) {
    is JsonPrimitive -> q.doubleOrNull ?: q.content.trim().toDoubleOrNull()
    else -> null
}
