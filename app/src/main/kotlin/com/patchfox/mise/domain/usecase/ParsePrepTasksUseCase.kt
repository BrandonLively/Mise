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
 * Flattens each recipe's [com.patchfox.mise.domain.model.PrepStep] (v3 per-recipe
 * relative-offset prep — marinade, thaw, etc.) into a single attributed prep checklist.
 * Attribution is exact: the prep lives on the recipe, so [PrepTask.attributedRecipeId]
 * is the owning recipe (no keyword guessing).
 */
class ParsePrepTasksUseCase @Inject constructor() {

    operator fun invoke(card: CookCard): List<PrepTask> {
        val result = mutableListOf<PrepTask>()
        for (recipe in card.recipes) {
            recipe.prepSchedule.forEachIndexed { stepIdx, step ->
                step.tasks.forEachIndexed { taskIdx, raw ->
                    result += PrepTask(
                        id = PrepTaskId("${recipe.id.value}-$stepIdx-$taskIdx"),
                        dayShortCaps = offsetCaps(step.offset),
                        text = raw,
                        attributedRecipeId = recipe.id,
                    )
                }
            }
        }
        return result
    }

    /** Short caps label for the prep chip, derived from the relative offset. */
    private fun offsetCaps(offset: String): String {
        val o = offset.trim()
        val lower = o.lowercase()
        return when {
            lower.startsWith("night") -> "EVE"
            lower.startsWith("morning") -> "AM"
            lower.startsWith("same") -> "DAY"
            lower.startsWith("t-") -> o.substring(2).uppercase()
            else -> o.take(4).uppercase()
        }
    }
}
