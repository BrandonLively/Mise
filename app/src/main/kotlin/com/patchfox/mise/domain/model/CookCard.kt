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

data class CookCard(
    val id: CookCardId,
    val theme: String,
    val cookDate: LocalDate,
    val firstMealDate: LocalDate?,
    val pickupDate: LocalDate?,
    val household: Household,
    val servingsPerMain: Int,
    val servingsPerDessert: Int?,
    val dailyTarget: DailyTarget,
    val dailyActual: DailyActual,
    val schedule: Schedule,
    val recipes: List<Recipe>,
    val cookPlan: CookPlan,
    val reheatReference: List<ReheatEntry>,
    val componentYields: List<ComponentYieldGroup>,
)

data class Household(val people: Int, val daysCovered: Int)

data class DailyTarget(
    val calories: Double,
    val proteinFloorGrams: Double,
    val leanPreference: String?,
    val tolerancePercent: Double?,
)

data class DailyActual(
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val withinTarget: Boolean?,
    val deltaPercent: Double?,
    val statusNote: String?,
)

data class Schedule(
    val prep: List<ScheduleEntry>,
    val cook: List<ScheduleEntry>,
    val firstMeal: List<ScheduleEntry>,
)

data class ScheduleEntry(
    val date: LocalDate,
    val dayOfWeek: DayOfWeek,
    val label: String,
    val tasks: List<String>,
    val rationale: String?,
)

enum class DayOfWeek { SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY }

enum class RecipeType { MAIN, DESSERT, SIDE, SNACK, BREAKFAST, OTHER }

enum class ColorTag { NOTE, TIP, IMPORTANT, WARNING, CAUTION }

data class Recipe(
    val id: RecipeId,
    val name: String,
    val type: RecipeType,
    val emoji: String,
    val color: RecipeColor,
    val colorTag: ColorTag?,
    val colorTagDescription: String?,
    val description: String,
    val source: RecipeSource?,
    val yield: RecipeYield,
    val perServing: Macro,
    val timing: RecipeTiming?,
    val ingredients: List<Ingredient>,
    val equipment: List<String>,
    val assembleAndStore: List<String>,
    val reheat: ReheatSpec,
)

data class RecipeSource(val author: String?, val url: String?)

data class RecipeYield(
    val servings: Int,
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

data class Ingredient(
    val name: String,
    val batchQuantity: String,
    val batchGrams: Double?,
    val batchVolumeMl: Double?,
    val batchCount: Double?,
    val inMise: String?,
    val miseBowlId: BowlId?,
    val miseStandalone: Boolean,
    val substitutionNote: String?,
)

data class ReheatSpec(
    val method: String,
    val timeSeconds: Int?,
    val additionalTimeSeconds: Int?,
    val additionalTimeNote: String?,
    val extraSteps: List<String>,
)

data class ReheatEntry(
    val recipeId: RecipeId,
    val recipeName: String,
    val method: String,
    val timeSeconds: Int?,
    val additionalTimeSeconds: Int?,
    val additionalTimeNote: String?,
    val extraSteps: List<String>,
)

data class CookPlan(
    val totalActiveMinutes: Int,
    val phaseDurationMinutes: Map<String, Int>,
    val emojiLegend: List<EmojiLegendEntry>,
    val phases: List<Phase>,
)

data class EmojiLegendEntry(
    val recipeId: RecipeId,
    val recipeName: String,
    val emoji: String,
)

sealed class Phase {
    abstract val id: String
    abstract val label: String
    abstract val estimatedMinutes: Int
    abstract val purpose: String?

    data class Phase0(
        override val id: String,
        override val label: String,
        override val estimatedMinutes: Int,
        override val purpose: String?,
        val steps: List<Phase0Step>,
    ) : Phase()

    data class Phase1(
        override val id: String,
        override val label: String,
        override val estimatedMinutes: Int,
        override val purpose: String?,
        val smellHierarchyNote: String?,
        val bowls: List<MiseBowl>,
        val standalone: List<MiseStandalone>,
    ) : Phase()

    data class Phase2(
        override val id: String,
        override val label: String,
        override val estimatedMinutes: Int,
        override val purpose: String?,
        val steps: List<CookStep>,
    ) : Phase()
}

data class Phase0Step(
    val id: StepId,
    val stepNumber: Int,
    val task: List<InstructionStep>,
    val description: String,
    val recipeIds: List<RecipeId>,
    val estimatedActiveMinutes: Int?,
    val estimatedRuntime: String?,
    val timer: StepTimer?,
)

data class MiseBowl(
    val id: BowlId,
    val bowlNumber: Int,
    val name: String,
    val forRecipeIds: List<RecipeId>,
    val ingredients: List<BowlIngredient>,
    val instruction: List<InstructionStep>,
    val notes: List<String>,
)

data class BowlIngredient(
    val name: String,
    val qty: String,
    val grams: Double?,
    val ml: Double?,
    val count: Double?,
)

data class MiseStandalone(val label: String, val prep: List<InstructionStep>)

data class CookStep(
    val id: StepId,
    val stepNumber: Int,
    val clockTime: String,
    val clockMinutes: Int,
    val recipeId: RecipeId?,
    val recipeName: String?,
    val recipeEmoji: String?,
    val recipeColor: RecipeColor?,
    val task: List<InstructionStep>,
    val miseReferences: List<MiseReference>,
    val equipmentUsed: List<String>,
    val handsOff: List<String>,
    val isFinalStep: Boolean,
    val timer: StepTimer?,
)

/**
 * One bullet inside a multi-step instruction: an action prefix plus the
 * ingredients introduced at that step. [ingredients] is null when the step
 * is action-only (e.g. "Whisk well until smooth") and references items
 * already prepped in a prior step.
 */
data class InstructionStep(
    val prefix: String,
    val ingredients: List<InstructionIngredient>?,
)

data class InstructionIngredient(
    val name: String,
    val quantity: String?,
    val unit: String?,
    val preparation: String?,
)

data class MiseReference(
    val bowlId: BowlId?,
    val bowlNumber: Int?,
    val volumeHint: String?,
    val label: String?,
    val isStandalone: Boolean,
)

data class StepTimer(
    val title: String,
    val durationSeconds: Int,
    val maxDurationSeconds: Int?,
    val durationDescription: String?,
)

data class ComponentYieldGroup(
    val recipeId: RecipeId,
    val components: List<ComponentYield>,
)

data class ComponentYield(
    val label: String,
    val yieldDescription: String?,
    val yieldGrams: Double,
    val totalCalories: Double,
    val perServingCalories: Double,
    val addedFreshPerServing: Boolean,
)

/** Convenience accessor for the cook screen. */
fun CookCard.phase2(): Phase.Phase2? =
    cookPlan.phases.filterIsInstance<Phase.Phase2>().firstOrNull()

fun CookCard.phase1(): Phase.Phase1? =
    cookPlan.phases.filterIsInstance<Phase.Phase1>().firstOrNull()

fun CookCard.phase0(): Phase.Phase0? =
    cookPlan.phases.filterIsInstance<Phase.Phase0>().firstOrNull()

fun CookCard.recipeById(id: RecipeId): Recipe? =
    recipes.firstOrNull { it.id == id }
