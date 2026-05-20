package com.patchfox.mise.domain.usecase

import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.model.RecipeId
import javax.inject.Inject

data class PrepTask(
    val id: PrepTaskId,
    val dayShortCaps: String,
    val text: String,
    val attributedRecipeId: RecipeId?,
)

/**
 * Filters schedule.prep tasks to food-prep items (marinade, thaw, ripen, fridge etc.)
 * and infers which recipe each one belongs to via keyword match.
 */
class ParsePrepTasksUseCase @Inject constructor() {

    private val foodPrepKeywords = listOf(
        "marinat", "thaw", "ripen", "fridge", "freezer", "move", "season", "brine",
        "soak", "refrigerate",
    )

    private val excludeKeywords = listOf("pickup", "walmart", "store", "booking")

    operator fun invoke(card: CookCard): List<PrepTask> {
        val result = mutableListOf<PrepTask>()
        val recipes = card.recipes
        for (entry in card.schedule.prep) {
            for ((idx, raw) in entry.tasks.withIndex()) {
                val lower = raw.lowercase()
                if (excludeKeywords.any { it in lower }) continue
                if (foodPrepKeywords.none { it in lower }) continue
                val recipeId = inferRecipe(lower, recipes.map { it.id to it.name.lowercase() })
                result += PrepTask(
                    id = PrepTaskId("${entry.date}-$idx"),
                    dayShortCaps = entry.dayOfWeek.name.take(3),
                    text = raw,
                    attributedRecipeId = recipeId,
                )
            }
        }
        return result
    }

    private fun inferRecipe(taskLower: String, recipes: List<Pair<RecipeId, String>>): RecipeId? {
        // Direct keyword hits to recipe names
        val keywordRecipe = listOf(
            "chicken" to "chicken-burrito-bowl",
            "shrimp" to "sheet-pan-shrimp-fajitas",
            "avocado" to "black-bean-enchilada-skillet",
            "enchilada" to "black-bean-enchilada-skillet",
            "chia" to "mexican-chocolate-chia-pudding",
            "pudding" to "mexican-chocolate-chia-pudding",
        ).firstOrNull { (kw, _) -> kw in taskLower }?.second

        if (keywordRecipe != null) {
            val match = recipes.firstOrNull { it.first.value == keywordRecipe }
            if (match != null) return match.first
        }

        // Fallback: any recipe name token appears in the task
        return recipes.firstOrNull { (_, name) ->
            name.split(Regex("[^a-z0-9]+"))
                .filter { it.length > 3 }
                .any { it in taskLower }
        }?.first
    }
}
