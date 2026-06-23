package com.patchfox.mise.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.combinedMacros
import com.patchfox.mise.domain.model.totalActiveMinutes
import com.patchfox.mise.ui.component.DailyMacrosCard
import com.patchfox.mise.ui.component.HeroCookDayCard
import com.patchfox.mise.ui.component.HeroCookDayStats
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.PrepWorkList
import com.patchfox.mise.ui.component.RecipeColorBand
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import kotlin.math.roundToInt

@Composable
fun HomePhone(
    state: HomeUiState,
    onTogglePrep: (PrepTaskId, Boolean) -> Unit,
    onWalkPlan: () -> Unit,
    onOpenStage: (CookStage) -> Unit,
    onOpenRecipe: (RecipeId) -> Unit,
) {
    if (state.loading && state.card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val card = state.card ?: return ErrorState(state.error.orEmpty())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 22.dp, end = 22.dp, top = 16.dp, bottom = 28.dp,
        ),
    ) {
        item {
            Text("mise", style = MiseTokens.text.h2, color = MiseTokens.colors.accent)
            Spacer(Modifier.height(18.dp))
        }
        item {
            HeroCookDayCard(
                eyebrow = "NEXT COOK SESSION",
                dayLabel = card.cookDate.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() } + " /",
                italicTail = "tomorrow.",
                stats = HeroCookDayStats(
                    activeMinutes = card.totalActiveMinutes(),
                    stepCount = card.recipes.sumOf { it.cookPhase.steps.size },
                    recipeCount = card.recipes.size,
                    bowlCount = card.recipes.sumOf { it.misePhase.bowls.size },
                ),
                onCta = onWalkPlan,
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            DailyMacrosCard(card.combinedMacros())
            Spacer(Modifier.height(22.dp))
        }
        item {
            PrepWorkList(
                tasks = state.prepTasks,
                checked = state.checkedPrep,
                recipesById = card.recipes.associateBy { it.id },
                onToggle = onTogglePrep,
            )
            Spacer(Modifier.height(22.dp))
        }
        item {
            Text("RECIPES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(8.dp))
        }
        item {
            RecipeGrid(card.recipes, onOpenRecipe)
            Spacer(Modifier.height(22.dp))
        }
        item {
            Text("COOK STAGES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(8.dp))
        }
        item {
            CookStageRows(card, onOpenStage)
        }
    }
}

@Composable
private fun RecipeGrid(recipes: List<Recipe>, onOpenRecipe: (RecipeId) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        recipes.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { recipe ->
                    Box(modifier = Modifier.weight(1f)) {
                        RecipeCard(recipe, onOpenRecipe)
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RecipeCard(recipe: Recipe, onOpenRecipe: (RecipeId) -> Unit) {
    MiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpenRecipe(recipe.id) },
    ) {
        Column {
            RecipeColorBand(color = recipe.color, thickness = 3.dp)
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        recipe.type.name,
                        style = MiseTokens.text.micro,
                        color = MiseTokens.colors.recipe(recipe.color),
                    )
                    Spacer(Modifier.weight(1f))
                    Text(recipe.emoji, style = MiseTokens.text.h2)
                }
                Spacer(Modifier.height(14.dp))
                Text(recipe.name, style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                Spacer(Modifier.height(10.dp))
                val perServing = recipe.perServing
                Text(
                    "${perServing.calories.toInt()} kcal · ${perServing.proteinGrams.toInt()}g P" +
                        (recipe.yield.servings?.let { " · $it servings" } ?: ""),
                    style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize),
                    color = MiseTokens.colors.ink3,
                )
            }
        }
    }
}

@Composable
private fun CookStageRows(card: CookCard, onOpenStage: (CookStage) -> Unit) {
    val rows = cookStageRows(card)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            MiseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenStage(row.stage) },
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(row.label, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
                        Spacer(Modifier.height(2.dp))
                        Text(row.hint, style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, null, tint = MiseTokens.colors.ink3)
                }
            }
        }
    }
}

internal data class CookStageRow(val label: String, val hint: String, val stage: CookStage)

/**
 * Two aggregate stage rows derived from every recipe's mise/cook phase.
 * Phase 0 is gone in v3 — its long-running kickoffs are folded into Cook.
 */
internal fun cookStageRows(card: CookCard): List<CookStageRow> {
    val bowlCount = card.recipes.sumOf { it.misePhase.bowls.size }
    val miseMinutes = card.recipes.sumOf { it.misePhase.durationMinutes ?: 0.0 }.roundToInt()
    val stepCount = card.recipes.sumOf { it.cookPhase.steps.size }
    val cookMinutes = card.recipes.sumOf { it.cookPhase.durationMinutes ?: 0.0 }.roundToInt()
    return listOf(
        CookStageRow("Mise en place", "$bowlCount bowls · $miseMinutes min", CookStage.MISE),
        CookStageRow("Cook", "$stepCount steps · $cookMinutes min", CookStage.COOK),
    )
}

@Composable
private fun ErrorState(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(
            if (message.isBlank()) "No cook card yet. Push one to Firestore to get started." else message,
            style = MiseTokens.text.body,
            color = MiseTokens.colors.ink2,
        )
    }
}
