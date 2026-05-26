package com.patchfox.mise.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CookCardDto(
    val schemaVersion: String,
    val meta: MetaDto,
    val schedule: ScheduleDto,
    val recipes: List<RecipeDto>,
    val cookPlan: CookPlanDto,
    val reheatReference: List<ReheatEntryDto> = emptyList(),
    val containerMath: List<ContainerMathEntryDto> = emptyList(),
    val macroReconciliation: JsonElement? = null,
    val componentYields: List<ComponentYieldEntryDto> = emptyList(),
    val substitutions: List<JsonElement> = emptyList(),
    val leftovers: List<JsonElement> = emptyList(),
)

@Serializable
data class MetaDto(
    val id: String,
    val theme: String,
    val cookDate: String,
    val firstMealDate: String? = null,
    val pickupDate: String? = null,
    val household: HouseholdDto,
    val servingsPerMain: Int,
    val servingsPerDessert: Int? = null,
    val dailyTarget: DailyTargetDto,
    val dailyActual: DailyActualDto,
    val generation: JsonElement? = null,
)

@Serializable
data class HouseholdDto(
    val people: Int,
    val daysCovered: Int,
)

@Serializable
data class DailyTargetDto(
    val calories: Double,
    val proteinFloorGrams: Double,
    val leanPreference: String? = null,
    val tolerancePercent: Double? = null,
)

@Serializable
data class DailyActualDto(
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val withinTarget: Boolean? = null,
    val deltaPercent: Double? = null,
    val statusNote: String? = null,
)

@Serializable
data class ScheduleDto(
    val prep: List<ScheduleEntryDto> = emptyList(),
    val cook: List<ScheduleEntryDto> = emptyList(),
    val firstMeal: List<ScheduleEntryDto> = emptyList(),
)

@Serializable
data class ScheduleEntryDto(
    val date: String,
    val dayOfWeek: String,
    val label: String,
    val tasks: List<String> = emptyList(),
    val rationale: String? = null,
)

@Serializable
data class RecipeDto(
    val id: String,
    val name: String,
    val type: String,
    val emoji: String? = null,
    val color: String,
    val colorTag: String? = null,
    val colorTagDescription: String? = null,
    val description: String,
    val source: SourceDto? = null,
    val yield: YieldDto,
    val perServing: MacroDto,
    val timing: TimingDto? = null,
    val ingredients: JsonElement,
    val equipment: JsonElement,
    val assembleAndStore: JsonElement? = null,
    val reheat: ReheatSpecDto,
)

@Serializable
data class SourceDto(
    val author: String? = null,
    val url: String? = null,
)

@Serializable
data class YieldDto(
    val servings: Int,
    val containerSize: String? = null,
    val perServingContainerNote: String? = null,
)

@Serializable
data class MacroDto(
    val calories: Double,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
)

@Serializable
data class TimingDto(
    val activeMinutes: Double? = null,
    val totalMinutes: JsonElement? = null,
    val keeps: String? = null,
)

@Serializable
data class IngredientDto(
    val name: String,
    val batchQuantity: String,
    val batchGrams: Double? = null,
    val batchVolumeMl: Double? = null,
    val batchCount: Double? = null,
    val inMise: String? = null,
    val miseBowlId: String? = null,
    val miseStandalone: Boolean? = null,
    val substitutionNote: String? = null,
)

@Serializable
data class ReheatSpecDto(
    val method: String,
    val timeSeconds: Double? = null,
    val additionalTimeSeconds: Double? = null,
    val additionalTimeNote: String? = null,
    val extraSteps: List<String> = emptyList(),
)

@Serializable
data class CookPlanDto(
    val totalActiveMinutes: Double? = null,
    val phaseDurationMinutes: Map<String, Double> = emptyMap(),
    val emojiLegend: List<EmojiLegendEntryDto> = emptyList(),
    val phases: List<JsonElement>,
)

@Serializable
data class EmojiLegendEntryDto(
    val recipeId: String,
    val recipeName: String,
    val emoji: String,
)

@Serializable
data class Phase0Dto(
    val id: String,
    val label: String,
    val estimatedMinutes: Double? = null,
    val purpose: String? = null,
    val steps: List<Phase0StepDto> = emptyList(),
)

@Serializable
data class Phase0StepDto(
    val id: String,
    val stepNumber: Int,
    /**
     * v2 schema: array of `InstructionStep` objects. Parsed flexibly in the
     * mapper via [JsonElement] so unexpected shapes are skipped rather than
     * crashing the whole card.
     */
    val task: JsonElement,
    val description: String,
    val recipeIds: List<String> = emptyList(),
    val estimatedActiveMinutes: Double? = null,
    val estimatedRuntime: String? = null,
    val timer: TimerDto? = null,
)

@Serializable
data class Phase1Dto(
    val id: String,
    val label: String,
    val estimatedMinutes: Double? = null,
    val purpose: String? = null,
    val smellHierarchyNote: String? = null,
    val bowls: List<MiseBowlDto> = emptyList(),
    val standalone: List<MiseStandaloneDto> = emptyList(),
)

@Serializable
data class MiseBowlDto(
    val id: String,
    val bowlNumber: Int,
    val name: String,
    val forRecipeId: JsonElement,
    val ingredients: List<BowlIngredientDto> = emptyList(),
    /** v2 schema: array of `InstructionStep` objects. */
    val instruction: JsonElement? = null,
    val notes: List<String> = emptyList(),
)

@Serializable
data class BowlIngredientDto(
    val name: String,
    val qty: String,
    val grams: Double? = null,
    val ml: Double? = null,
    val count: Double? = null,
)

@Serializable
data class MiseStandaloneDto(
    val label: String,
    /** v2 schema: array of `InstructionStep` objects. */
    val prep: JsonElement? = null,
)

@Serializable
data class InstructionStepDto(
    val instructionPrefix: String,
    val ingredients: List<InstructionIngredientDto>? = null,
)

@Serializable
data class InstructionIngredientDto(
    val name: String,
    val quantity: String? = null,
    val unit: String? = null,
    val preparation: String? = null,
)

@Serializable
data class Phase2Dto(
    val id: String,
    val label: String,
    val estimatedMinutes: Double? = null,
    val purpose: String? = null,
    val stepCount: Int? = null,
    val steps: List<Phase2StepDto> = emptyList(),
)

@Serializable
data class Phase2StepDto(
    val id: String,
    val stepNumber: Int,
    val clockTime: String,
    val clockMinutes: Int,
    val recipeId: String? = null,
    val recipeName: String? = null,
    val recipeEmoji: String? = null,
    val task: JsonElement,
    val miseReferences: List<MiseReferenceDto> = emptyList(),
    val equipmentUsed: List<String> = emptyList(),
    val handsOff: List<String> = emptyList(),
    val timer: TimerDto? = null,
    val isFinalStep: Boolean = false,
)

@Serializable
data class MiseReferenceDto(
    val bowlId: String? = null,
    val bowlNumber: Int? = null,
    val volumeHint: String? = null,
    val label: String? = null,
    val isStandalone: Boolean? = null,
)

@Serializable
data class TimerDto(
    val label: String,
    val durationSeconds: Int,
    val maxDurationSeconds: Int? = null,
    val durationDescription: String? = null,
)

@Serializable
data class ReheatEntryDto(
    val recipeId: String,
    val recipeName: String,
    val method: String,
    val timeSeconds: Double? = null,
    val additionalTimeSeconds: Double? = null,
    val additionalTimeNote: String? = null,
    val extraSteps: List<String> = emptyList(),
)

@Serializable
data class ContainerMathEntryDto(
    val recipeId: String,
    val containers: List<ContainerDto> = emptyList(),
)

@Serializable
data class ContainerDto(
    val count: Int,
    val size: String,
    val purpose: String,
)

@Serializable
data class ComponentYieldEntryDto(
    val recipeId: String,
    val components: List<ComponentYieldDto> = emptyList(),
    val perRecipeTotal: JsonElement? = null,
    val perRecipeTotalNote: String? = null,
)

@Serializable
data class ComponentYieldDto(
    val label: String,
    val yieldDescription: String? = null,
    val yieldGrams: Double,
    val totalCalories: Double,
    val perServingCalories: Double,
    val addedFreshPerServing: Boolean? = null,
)
