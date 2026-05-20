package com.patchfox.mise.domain.mapper

import com.patchfox.mise.data.cookcard.CookCardParser
import com.patchfox.mise.data.dto.CookCardDto
import com.patchfox.mise.data.dto.MiseBowlDto
import com.patchfox.mise.data.dto.Phase0Dto
import com.patchfox.mise.data.dto.Phase1Dto
import com.patchfox.mise.data.dto.Phase2Dto
import com.patchfox.mise.data.dto.Phase2StepDto
import com.patchfox.mise.data.dto.RecipeDto
import com.patchfox.mise.data.dto.ReheatEntryDto
import com.patchfox.mise.data.dto.ReheatSpecDto
import com.patchfox.mise.data.dto.TimerDto
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.BowlIngredient
import com.patchfox.mise.domain.model.ColorTag
import com.patchfox.mise.domain.model.ComponentYield
import com.patchfox.mise.domain.model.ComponentYieldGroup
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookCardId
import com.patchfox.mise.domain.model.CookPlan
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.DailyActual
import com.patchfox.mise.domain.model.DailyTarget
import com.patchfox.mise.domain.model.DayOfWeek
import com.patchfox.mise.domain.model.EmojiLegendEntry
import com.patchfox.mise.domain.model.Household
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.domain.model.Macro
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.MiseReference
import com.patchfox.mise.domain.model.MiseStandalone
import com.patchfox.mise.domain.model.Phase
import com.patchfox.mise.domain.model.Phase0Step
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.RecipeSource
import com.patchfox.mise.domain.model.RecipeTiming
import com.patchfox.mise.domain.model.RecipeType
import com.patchfox.mise.domain.model.RecipeYield
import com.patchfox.mise.domain.model.ReheatEntry
import com.patchfox.mise.domain.model.ReheatSpec
import com.patchfox.mise.domain.model.Schedule
import com.patchfox.mise.domain.model.ScheduleEntry
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.model.StepTimer
import com.patchfox.mise.ui.theme.RecipeColor
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun CookCardDto.toDomain(): CookCard {
    val recipes = recipes.map { it.toDomain() }
    val recipeById = recipes.associateBy { it.id.value }

    return CookCard(
        id = CookCardId(meta.id),
        theme = meta.theme,
        cookDate = LocalDate.parse(meta.cookDate),
        firstMealDate = meta.firstMealDate?.let(LocalDate::parse),
        pickupDate = meta.pickupDate?.let(LocalDate::parse),
        household = Household(meta.household.people, meta.household.daysCovered),
        servingsPerMain = meta.servingsPerMain,
        servingsPerDessert = meta.servingsPerDessert,
        dailyTarget = DailyTarget(
            calories = meta.dailyTarget.calories,
            proteinFloorGrams = meta.dailyTarget.proteinFloorGrams,
            leanPreference = meta.dailyTarget.leanPreference,
            tolerancePercent = meta.dailyTarget.tolerancePercent,
        ),
        dailyActual = DailyActual(
            calories = meta.dailyActual.calories,
            proteinGrams = meta.dailyActual.proteinGrams,
            carbsGrams = meta.dailyActual.carbsGrams,
            fatGrams = meta.dailyActual.fatGrams,
            withinTarget = meta.dailyActual.withinTarget,
            deltaPercent = meta.dailyActual.deltaPercent,
            statusNote = meta.dailyActual.statusNote,
        ),
        schedule = Schedule(
            prep = schedule.prep.map { it.toDomain() },
            cook = schedule.cook.map { it.toDomain() },
            firstMeal = schedule.firstMeal.map { it.toDomain() },
        ),
        recipes = recipes,
        cookPlan = cookPlan.toDomain(recipeById),
        reheatReference = reheatReference.map { it.toDomain() },
        componentYields = componentYields.map { entry ->
            ComponentYieldGroup(
                recipeId = RecipeId(entry.recipeId),
                components = entry.components.map { c ->
                    ComponentYield(
                        label = c.label,
                        yieldDescription = c.yieldDescription,
                        yieldGrams = c.yieldGrams,
                        totalCalories = c.totalCalories,
                        perServingCalories = c.perServingCalories,
                        addedFreshPerServing = c.addedFreshPerServing ?: false,
                    )
                },
            )
        },
    )
}

private fun com.patchfox.mise.data.dto.ScheduleEntryDto.toDomain() = ScheduleEntry(
    date = LocalDate.parse(date),
    dayOfWeek = dayOfWeek.uppercase().let { day ->
        runCatching { DayOfWeek.valueOf(day) }.getOrDefault(DayOfWeek.SUNDAY)
    },
    label = label,
    tasks = tasks,
    rationale = rationale,
)

private fun RecipeDto.toDomain(): Recipe = Recipe(
    id = RecipeId(id),
    name = name,
    type = parseRecipeType(type),
    emoji = emoji ?: "🍽️",
    color = parseRecipeColor(color),
    colorTag = colorTag?.let { runCatching { ColorTag.valueOf(it.trim().uppercase()) }.getOrNull() },
    colorTagDescription = colorTagDescription,
    description = description,
    source = source?.let { RecipeSource(it.author, it.url) },
    yield = RecipeYield(
        servings = yield.servings,
        containerSize = yield.containerSize,
        perServingContainerNote = yield.perServingContainerNote,
    ),
    perServing = Macro(
        calories = perServing.calories,
        proteinGrams = perServing.proteinGrams,
        carbsGrams = perServing.carbsGrams,
        fatGrams = perServing.fatGrams,
    ),
    timing = timing?.let { t ->
        RecipeTiming(
            activeMinutes = t.activeMinutes,
            totalMinutesText = t.totalMinutes?.let { tm ->
                (tm as? JsonPrimitive)?.let { p ->
                    if (p.isString) p.content else p.content
                } ?: tm.toString()
            },
            keeps = t.keeps,
        )
    },
    ingredients = ingredients.parseIngredientList(),
    equipment = equipment.parseStringList(),
    assembleAndStore = assembleAndStore?.parseStringList().orEmpty(),
    reheat = reheat.toDomain(),
)

private fun JsonElement.parseIngredientList(): List<Ingredient> {
    if (this !is JsonArray) return emptyList()
    return mapNotNull { el ->
        val obj = el as? JsonObject ?: return@mapNotNull null
        val name = obj["name"]?.jsonPrimitive?.contentOrNull() ?: return@mapNotNull null
        val batchQuantity = obj["batchQuantity"]?.jsonPrimitive?.contentOrNull() ?: ""
        Ingredient(
            name = name,
            batchQuantity = batchQuantity,
            batchGrams = obj["batchGrams"]?.doubleOrNull(),
            batchVolumeMl = obj["batchVolumeMl"]?.doubleOrNull(),
            batchCount = obj["batchCount"]?.doubleOrNull(),
            inMise = obj["inMise"]?.jsonPrimitive?.contentOrNull(),
            miseBowlId = obj["miseBowlId"]?.jsonPrimitive?.contentOrNull()?.let(::BowlId),
            miseStandalone = obj["miseStandalone"]?.jsonPrimitive?.booleanOrNull() ?: false,
            substitutionNote = obj["substitutionNote"]?.jsonPrimitive?.contentOrNull(),
        )
    }
}

private fun JsonElement.parseStringList(): List<String> = when (this) {
    is JsonArray -> mapNotNull { (it as? JsonPrimitive)?.contentOrNull() }
    is JsonPrimitive -> listOf(contentOrNull() ?: "").filter { it.isNotBlank() }
    else -> emptyList()
}

private fun ReheatSpecDto.toDomain() = ReheatSpec(
    method = method,
    timeSeconds = timeSeconds?.toInt(),
    additionalTimeSeconds = additionalTimeSeconds?.toInt(),
    additionalTimeNote = additionalTimeNote,
    extraSteps = extraSteps,
)

private fun ReheatEntryDto.toDomain() = ReheatEntry(
    recipeId = RecipeId(recipeId),
    recipeName = recipeName,
    method = method,
    timeSeconds = timeSeconds?.toInt(),
    additionalTimeSeconds = additionalTimeSeconds?.toInt(),
    additionalTimeNote = additionalTimeNote,
    extraSteps = extraSteps,
)

private fun parseRecipeType(s: String): RecipeType = when (s.lowercase()) {
    "main" -> RecipeType.MAIN
    "dessert" -> RecipeType.DESSERT
    "side" -> RecipeType.SIDE
    "snack" -> RecipeType.SNACK
    "breakfast" -> RecipeType.BREAKFAST
    else -> RecipeType.OTHER
}

/** Tolerant of casing/whitespace; unknown colors fall back rather than crashing the map. */
private fun parseRecipeColor(s: String): RecipeColor =
    runCatching { RecipeColor.valueOf(s.trim().uppercase()) }.getOrDefault(RecipeColor.BLUE)

private fun com.patchfox.mise.data.dto.CookPlanDto.toDomain(
    recipeById: Map<String, Recipe>,
): CookPlan {
    val parsedPhases = phases.mapNotNull { it.toPhase(recipeById) }
    return CookPlan(
        totalActiveMinutes = totalActiveMinutes?.toInt() ?: 0,
        phaseDurationMinutes = phaseDurationMinutes.mapValues { it.value.toInt() },
        emojiLegend = emojiLegend.map { e ->
            EmojiLegendEntry(RecipeId(e.recipeId), e.recipeName, e.emoji)
        },
        phases = parsedPhases,
    )
}

private fun JsonElement.toPhase(recipeById: Map<String, Recipe>): Phase? {
    val obj = this as? JsonObject ?: return null
    val id = obj["id"]?.jsonPrimitive?.contentOrNull() ?: return null
    return when (id) {
        "phase-0" -> CookCardParser.json.decodeFromJsonElement(Phase0Dto.serializer(), this).toDomain()
        "phase-1" -> CookCardParser.json.decodeFromJsonElement(Phase1Dto.serializer(), this).toDomain()
        "phase-2" -> CookCardParser.json.decodeFromJsonElement(Phase2Dto.serializer(), this).toDomain(recipeById)
        else -> null
    }
}

private fun Phase0Dto.toDomain() = Phase.Phase0(
    id = id,
    label = label,
    estimatedMinutes = estimatedMinutes?.toInt() ?: 0,
    purpose = purpose,
    steps = steps.map { s ->
        Phase0Step(
            id = StepId(s.id),
            stepNumber = s.stepNumber,
            task = s.task,
            description = s.description,
            recipeIds = s.recipeIds.map(::RecipeId),
            estimatedActiveMinutes = s.estimatedActiveMinutes?.toInt(),
            estimatedRuntime = s.estimatedRuntime,
            timer = s.timer?.toDomain(),
        )
    },
)

private fun Phase1Dto.toDomain() = Phase.Phase1(
    id = id,
    label = label,
    estimatedMinutes = estimatedMinutes?.toInt() ?: 0,
    purpose = purpose,
    smellHierarchyNote = smellHierarchyNote,
    bowls = bowls.map { it.toDomain() },
    standalone = standalone.map { MiseStandalone(it.label, it.prep?.parseStringList().orEmpty()) },
)

private fun MiseBowlDto.toDomain(): MiseBowl {
    val forRecipeIds = when (val fr = forRecipeId) {
        is JsonArray -> fr.mapNotNull { (it as? JsonPrimitive)?.contentOrNull() }.map(::RecipeId)
        is JsonPrimitive -> listOf(RecipeId(fr.content))
        else -> emptyList()
    }
    return MiseBowl(
        id = BowlId(id),
        bowlNumber = bowlNumber,
        name = name,
        forRecipeIds = forRecipeIds,
        ingredients = ingredients.map {
            BowlIngredient(
                name = it.name,
                qty = it.qty,
                grams = it.grams,
                ml = it.ml,
                count = it.count,
            )
        },
        instruction = instruction?.parseStringList().orEmpty(),
        notes = notes,
    )
}

private fun Phase2Dto.toDomain(recipeById: Map<String, Recipe>) = Phase.Phase2(
    id = id,
    label = label,
    estimatedMinutes = estimatedMinutes?.toInt() ?: 0,
    purpose = purpose,
    steps = steps.map { s ->
        val recipe = s.recipeId?.let { recipeById[it] }
        CookStep(
            id = StepId(s.id),
            stepNumber = s.stepNumber,
            clockTime = s.clockTime,
            clockMinutes = s.clockMinutes,
            recipeId = s.recipeId?.let(::RecipeId),
            recipeName = s.recipeName,
            recipeEmoji = s.recipeEmoji,
            recipeColor = recipe?.color,
            task = parseTaskField(s),
            miseReferences = s.miseReferences.map { r ->
                MiseReference(
                    bowlId = r.bowlId?.let(::BowlId),
                    bowlNumber = r.bowlNumber,
                    volumeHint = r.volumeHint,
                    label = r.label,
                    isStandalone = r.isStandalone ?: false,
                )
            },
            equipmentUsed = s.equipmentUsed,
            handsOff = s.handsOff,
            isFinalStep = s.isFinalStep,
            timer = s.timer?.toDomain(),
        )
    },
)

private fun parseTaskField(step: Phase2StepDto): List<String> = when (val t = step.task) {
    is JsonArray -> t.mapNotNull { (it as? JsonPrimitive)?.contentOrNull() }
    is JsonPrimitive -> {
        val raw = t.contentOrNull().orEmpty()
        raw.split(';', '\n').map { it.trim() }.filter { it.isNotBlank() }
    }
    else -> emptyList()
}

private fun TimerDto.toDomain() = StepTimer(
    title = label,
    durationSeconds = durationSeconds,
    maxDurationSeconds = maxDurationSeconds,
    durationDescription = durationDescription,
)

private fun JsonPrimitive.contentOrNull(): String? = if (isString || content != "null") content else null
private fun JsonPrimitive.booleanOrNull(): Boolean? = runCatching { content.toBooleanStrict() }.getOrNull()
private fun JsonElement.doubleOrNull(): Double? = (this as? JsonPrimitive)?.content?.toDoubleOrNull()
