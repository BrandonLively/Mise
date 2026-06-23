package com.patchfox.mise.domain.usecase

import com.patchfox.mise.data.cookcard.CookCardParser
import com.patchfox.mise.domain.mapper.toDomain
import com.patchfox.mise.domain.model.RecipeId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * v3 prep-attribution spec. [ParsePrepTasksUseCase] flattens each recipe's
 * relative-offset [com.patchfox.mise.domain.model.PrepStep]s into one attributed
 * checklist, where attribution is exact (the prep lives on the recipe). The caribbean
 * sample has NO prepSchedule, so this uses the MINIMAL fixture: the chicken carries a
 * "night-before" marinade and the salmon a "T-36h" thaw.
 */
class ParsePrepTasksUseCaseTest {

    private val useCase = ParsePrepTasksUseCase()

    private val card = CookCardParser.parse(
        this::class.java.getResource("/cook-card-v3-minimal.json")!!.readText(),
    ).toDomain()

    @Test
    fun `every prep task is attributed to a real recipe in the card`() {
        val tasks = useCase(card)
        assertTrue(tasks.isNotEmpty(), "minimal fixture has prepSchedule entries")
        val recipeIds = card.recipes.map { it.id }.toSet()
        tasks.forEach { task ->
            assertNotNull(task.attributedRecipeId, "task '${task.text}' should be attributed")
            assertTrue(
                task.attributedRecipeId in recipeIds,
                "task '${task.text}' attributed to an unknown recipe ${task.attributedRecipeId}",
            )
        }
    }

    @Test
    fun `chicken marinade task is attributed to the chicken recipe with an EVE day chip`() {
        val tasks = useCase(card)
        val marinade = tasks.first { it.text.contains("marinade", ignoreCase = true) }
        assertEquals(RecipeId("jerk-chicken-rice-peas"), marinade.attributedRecipeId)
        assertEquals("EVE", marinade.dayShortCaps)
    }

    @Test
    fun `salmon thaw task is attributed to the salmon recipe with a T-36h derived chip`() {
        val tasks = useCase(card)
        val thaw = tasks.first { it.text.contains("thaw", ignoreCase = true) }
        assertEquals(RecipeId("jerk-salmon-mango-salsa"), thaw.attributedRecipeId)
        // offset "T-36h" -> caps of the part after "T-"
        assertEquals("36H", thaw.dayShortCaps)
    }

    @Test
    fun `each recipe's prep schedule contributes its tasks`() {
        val tasks = useCase(card)
        val byRecipe = tasks.groupBy { it.attributedRecipeId }
        assertTrue(
            byRecipe[RecipeId("jerk-chicken-rice-peas")]?.isNotEmpty() == true,
            "chicken's night-before marinade should produce a task",
        )
        assertTrue(
            byRecipe[RecipeId("jerk-salmon-mango-salsa")]?.isNotEmpty() == true,
            "salmon's T-36h thaw should produce a task",
        )
    }
}
