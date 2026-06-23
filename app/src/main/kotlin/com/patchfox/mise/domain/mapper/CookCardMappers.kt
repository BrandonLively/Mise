package com.patchfox.mise.domain.mapper

import com.patchfox.mise.data.dto.CookCardV3Dto
import com.patchfox.mise.data.dto.CookStepV3Dto
import com.patchfox.mise.data.dto.IngredientV3Dto
import com.patchfox.mise.data.dto.MacroBreakdownV3Dto
import com.patchfox.mise.data.dto.MiseBowlV3Dto
import com.patchfox.mise.data.dto.MiseStandaloneV3Dto
import com.patchfox.mise.data.dto.PrepStepV3Dto
import com.patchfox.mise.data.dto.RecipeV3Dto
import com.patchfox.mise.data.dto.ReheatV3Dto
import com.patchfox.mise.data.dto.TimerV3Dto
import com.patchfox.mise.data.dto.numericAsDisplay
import com.patchfox.mise.data.dto.quantityAsDisplay
import com.patchfox.mise.data.dto.quantityAsDouble
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookCardId
import com.patchfox.mise.domain.model.CookPhase
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.DailyTarget
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.Macro
import com.patchfox.mise.domain.model.MacroIngredient
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.MisePhase
import com.patchfox.mise.domain.model.MiseStandalone
import com.patchfox.mise.domain.model.PrepStep
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.RecipeSource
import com.patchfox.mise.domain.model.RecipeTiming
import com.patchfox.mise.domain.model.RecipeType
import com.patchfox.mise.domain.model.RecipeYield
import com.patchfox.mise.domain.model.ReheatSpec
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.StepTimer
import com.patchfox.mise.ui.theme.RecipeColor
import kotlinx.datetime.LocalDate

fun CookCardV3Dto.toDomain(): CookCard = CookCard(
    id = CookCardId(meta.id),
    theme = meta.theme,
    cookDate = LocalDate.parse(meta.cookDate),
    servingsPerMain = meta.servingsPerMain,
    servingsPerDessert = meta.servingsPerDessert,
    dailyTarget = DailyTarget(
        calories = meta.dailyTarget.calories,
        proteinFloorGrams = meta.dailyTarget.proteinFloorGrams,
        leanPreference = meta.dailyTarget.leanPreference,
        tolerancePercent = meta.dailyTarget.tolerancePercent,
    ),
    recipes = recipes.map { it.toDomain() },
)

private fun RecipeV3Dto.toDomain(): Recipe = Recipe(
    id = RecipeId(id),
    name = name,
    type = parseRecipeType(type),
    emoji = emoji ?: "🍽️",
    color = parseRecipeColor(color),
    description = description.orEmpty(),
    source = source?.let { RecipeSource(it.author, it.url) },
    yield = yield?.let {
        RecipeYield(it.servings, it.containerSize, it.perServingContainerNote)
    } ?: RecipeYield(null, null, null),
    perServing = Macro(
        calories = perServing.calories,
        proteinGrams = perServing.proteinGrams,
        carbsGrams = perServing.carbsGrams,
        fatGrams = perServing.fatGrams,
    ),
    timing = timing?.let {
        RecipeTiming(
            activeMinutes = it.activeMinutes,
            totalMinutesText = it.totalMinutes.numericAsDisplay(),
            keeps = it.keeps,
        )
    },
    equipment = equipment,
    assembleAndStore = assembleAndStore,
    prepSchedule = prepSchedule.map { it.toDomain() },
    misePhase = misePhase?.let { mp ->
        MisePhase(
            durationMinutes = mp.durationMinutes,
            description = mp.description,
            bowls = mp.bowls.map { it.toDomain() },
            standalones = mp.standalones.map { it.toDomain() },
        )
    } ?: MisePhase(null, null, emptyList(), emptyList()),
    cookPhase = CookPhase(
        durationMinutes = cookPhase.durationMinutes,
        description = cookPhase.description,
        steps = cookPhase.steps.map { it.toDomain() },
    ),
    macroBreakdown = macroBreakdown.toDomain(),
    reheat = reheat?.toDomain(),
    deviationsFromPlan = deviationsFromPlan,
)

private fun PrepStepV3Dto.toDomain() = PrepStep(
    offset = offset,
    label = label,
    tasks = tasks,
    combinable = combinable,
    rationale = rationale,
)

private fun MiseBowlV3Dto.toDomain() = MiseBowl(
    id = BowlId(id),
    name = name,
    ingredients = ingredients.map { it.toDomain() },
    steps = steps,
)

private fun MiseStandaloneV3Dto.toDomain() = MiseStandalone(
    name = name,
    ingredientId = ingredientId,
    prep = prep,
)

private fun IngredientV3Dto.toDomain() = Ingredient(
    ingredientId = ingredientId,
    name = name,
    quantity = quantity.quantityAsDouble(),
    quantityDisplay = quantity.quantityAsDisplay(),
    unit = unit,
    preparation = preparation,
    combinable = combinable,
)

private fun CookStepV3Dto.toDomain() = CookStep(
    id = StepId(id),
    backgroundable = backgroundable,
    label = label,
    clockTime = clockTime,
    steps = steps,
    ingredients = ingredients.map { it.toDomain() },
    handsOff = handsOff,
    isFinalStep = isFinalStep,
    timer = timer?.toDomain(),
)

private fun TimerV3Dto.toDomain() = StepTimer(
    title = label ?: "Timer",
    durationSeconds = (durationSeconds ?: 0.0).toInt(),
    maxDurationSeconds = maxDurationSeconds?.toInt(),
    durationDescription = durationDescription,
)

private fun MacroBreakdownV3Dto?.toDomain(): List<MacroIngredient> =
    this?.ingredients.orEmpty().map { mi ->
        MacroIngredient(
            name = mi.name,
            ingredientId = mi.ingredientId,
            quantity = mi.quantity,
            unit = mi.unit,
            displayHint = mi.displayHint,
            source = mi.source,
            macro = Macro(mi.calories, mi.proteinGrams, mi.carbsGrams, mi.fatGrams),
        )
    }

private fun ReheatV3Dto.toDomain() = ReheatSpec(
    method = method,
    timeSeconds = timeSeconds?.toInt(),
    extraSteps = extraSteps,
)

private fun parseRecipeType(s: String): RecipeType = when (s.trim().lowercase()) {
    "main" -> RecipeType.MAIN
    "dessert" -> RecipeType.DESSERT
    "side" -> RecipeType.SIDE
    "snack" -> RecipeType.SNACK
    "breakfast" -> RecipeType.BREAKFAST
    else -> RecipeType.OTHER
}

/** Tolerant of casing/whitespace/missing; unknown colors fall back rather than crashing the map. */
private fun parseRecipeColor(s: String?): RecipeColor =
    runCatching { RecipeColor.valueOf(s.orEmpty().trim().uppercase()) }.getOrDefault(RecipeColor.BLUE)
