package com.patchfox.mise.ui.screen.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.phase0
import com.patchfox.mise.domain.model.phase1
import com.patchfox.mise.domain.model.phase2
import com.patchfox.mise.ui.component.HeroCookDayCard
import com.patchfox.mise.ui.component.HeroCookDayStats
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.component.PrepWorkList
import com.patchfox.mise.ui.component.RecipeColorBand
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun HomeTablet(
    state: HomeUiState,
    onTogglePrep: (PrepTaskId, Boolean) -> Unit,
    onWalkPlan: () -> Unit,
    onOpenPhase: (PhaseTab) -> Unit,
    onOpenRecipe: (RecipeId) -> Unit,
) {
    if (state.card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val card = state.card

    Row(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Left pane (60%)
        LazyColumn(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(start = 32.dp, end = 16.dp, top = 24.dp, bottom = 24.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "2026 · WEEK 21 · COOK CARD",
                        style = MiseTokens.text.micro,
                        color = MiseTokens.colors.ink3,
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(MiseTokens.colors.accentWash, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(card.cookDate.dayOfWeek.name.take(3) + " 10:00",
                            style = MiseTokens.text.micro,
                            color = MiseTokens.colors.accent)
                    }
                }
                Spacer(Modifier.height(20.dp))
                HeroCookDayCard(
                    eyebrow = "NEXT COOK SESSION",
                    dayLabel = card.cookDate.dayOfWeek.name.lowercase().replaceFirstChar { it.titlecase() } + " /",
                    italicTail = "tomorrow.",
                    stats = HeroCookDayStats(
                        activeMinutes = card.cookPlan.totalActiveMinutes,
                        stepCount = card.phase2()?.steps?.size ?: 0,
                        recipeCount = card.recipes.size,
                        bowlCount = card.phase1()?.bowls?.size ?: 0,
                    ),
                    onCta = onWalkPlan,
                )
                Spacer(Modifier.height(28.dp))
                PrepWorkList(
                    tasks = state.prepTasks,
                    checked = state.checkedPrep,
                    recipesById = card.recipes.associateBy { it.id },
                    onToggle = onTogglePrep,
                )
                Spacer(Modifier.height(28.dp))
                Text("RECIPES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                Spacer(Modifier.height(8.dp))
                RecipeGridTablet(card.recipes, onOpenRecipe)
            }
        }

        // Right pane (40%) — surface background with left border
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
                .background(MiseTokens.colors.surface)
                .drawBehind {
                    drawLine(
                        color = androidx.compose.ui.graphics.Color(0x1A2A1F17),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        strokeWidth = 1f,
                    )
                }
                .padding(24.dp),
        ) {
            CountdownTile()
            Spacer(Modifier.height(20.dp))
            Text("COOK PHASES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(8.dp))
            CookPhasesList(card, onOpenPhase)
            Spacer(Modifier.height(28.dp))
            Text("SCHEDULE PREVIEW", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(8.dp))
            SchedulePreview(card)
        }
    }
}

@Composable
private fun CountdownTile() {
    MiseCard(
        modifier = Modifier.fillMaxWidth(),
        background = MiseTokens.colors.accentWash,
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("COUNTDOWN", style = MiseTokens.text.micro, color = MiseTokens.colors.accent)
            Spacer(Modifier.height(8.dp))
            Text("22h to cook", style = MiseTokens.text.h2.copy(fontSize = MiseTokens.text.clockLarge.fontSize), color = MiseTokens.colors.ink)
            Spacer(Modifier.height(6.dp))
            Text("Chicken should already be marinating from Saturday night.", style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
        }
    }
}

@Composable
private fun CookPhasesList(
    card: com.patchfox.mise.domain.model.CookCard,
    onOpenPhase: (PhaseTab) -> Unit,
) {
    data class PhaseRow(val label: String, val hint: String, val tab: PhaseTab)
    val rows = listOfNotNull(
        card.phase0()?.let { PhaseRow(it.label, "${it.steps.size} steps · ${it.estimatedMinutes} min", PhaseTab.Phase0) },
        card.phase1()?.let { PhaseRow(it.label, "${it.bowls.size} bowls · ${it.estimatedMinutes} min", PhaseTab.Phase1) },
        card.phase2()?.let { PhaseRow(it.label, "${it.steps.size} steps · ${it.estimatedMinutes} min", PhaseTab.Phase2) },
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            val (label, hint) = row.label to row.hint
            MiseCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onOpenPhase(row.tab) },
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
                        Text(hint, style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
                    }
                    Icon(Icons.Filled.KeyboardArrowRight, null, tint = MiseTokens.colors.ink3)
                }
            }
        }
    }
}

@Composable
private fun SchedulePreview(card: com.patchfox.mise.domain.model.CookCard) {
    val rows = buildList {
        card.schedule.prep.forEach { add(it.dayOfWeek.name.take(3) + " · " + it.label) }
        card.schedule.cook.forEach { add(it.dayOfWeek.name.take(3) + " · " + it.label) }
        card.schedule.firstMeal.forEach { add(it.dayOfWeek.name.take(3) + " · " + it.label) }
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        rows.forEach { row ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier
                    .background(MiseTokens.colors.ink2, RoundedCornerShape(999.dp))
                    .height(6.dp)
                    .width(6.dp))
                Spacer(Modifier.width(10.dp))
                Text(row, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
            }
        }
    }
}

@Composable
private fun RecipeGridTablet(recipes: List<Recipe>, onOpenRecipe: (RecipeId) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        recipes.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                pair.forEach { r ->
                    MiseCard(
                        modifier = Modifier.weight(1f),
                        onClick = { onOpenRecipe(r.id) },
                    ) {
                        Column {
                            RecipeColorBand(color = r.color, thickness = 3.dp)
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row {
                                    Text(r.type.name, style = MiseTokens.text.micro, color = MiseTokens.colors.recipe(r.color))
                                    Spacer(Modifier.weight(1f))
                                    Text(r.emoji, style = MiseTokens.text.h1)
                                }
                                Spacer(Modifier.height(14.dp))
                                Text(r.name, style = MiseTokens.text.h2, color = MiseTokens.colors.ink)
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    "${r.perServing.calories.toInt()} kcal · ${r.perServing.proteinGrams.toInt()}g P · ${r.yield.servings} servings",
                                    style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize),
                                    color = MiseTokens.colors.ink3,
                                )
                            }
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
