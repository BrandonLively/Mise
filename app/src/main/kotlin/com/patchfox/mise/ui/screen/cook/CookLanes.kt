package com.patchfox.mise.ui.screen.cook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.StepId
import com.patchfox.mise.domain.usecase.RecipeCookGuide
import com.patchfox.mise.domain.usecase.RecipeMiseGuide
import com.patchfox.mise.domain.usecase.SharedCookStep
import com.patchfox.mise.ui.component.InstructionStepBlock
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.ScrollFadeIndicator
import com.patchfox.mise.ui.component.formatMmSs
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.RecipeColor
import com.patchfox.mise.ui.theme.recipe

/** Lane-scoped callbacks, bundled so the lane composables stay ViewModel-free. */
data class CookLaneCallbacks(
    val advanceRecipe: (RecipeId, Int) -> Unit,
    val advanceShared: (Int) -> Unit,
    val toggleBowl: (BowlId, Boolean) -> Unit,
    val startStepTimer: (CookStep, RecipeColor?, String?) -> Unit,
    val startSharedTimer: (SharedCookStep) -> Unit,
) {
    companion object {
        val NONE = CookLaneCallbacks({ _, _ -> }, {}, { _, _ -> }, { _, _, _ -> }, {})
    }
}

/**
 * The horizontally snap-scrolling board of lanes (3 fit a tablet viewport). Builds the
 * ordered visible-lane list for the current stage, then renders each as a fixed-width column.
 */
@Composable
fun LaneScroller(
    state: CookUiState,
    callbacks: CookLaneCallbacks,
    modifier: Modifier = Modifier,
) {
    val card = state.card ?: return
    BoxWithConstraints(modifier.fillMaxSize()) {
        val laneWidth = ((maxWidth - 16.dp * 2 - 12.dp * 2) / 3).coerceAtLeast(280.dp)
        val lanes = remember(state.stage, state.hiddenLanesByStage, state.completedRecipes, card.id) {
            buildVisibleLanes(state)
        }
        val listState = rememberLazyListState()
        Box(Modifier.fillMaxSize()) {
            LazyRow(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                flingBehavior = rememberSnapFlingBehavior(listState),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(lanes, key = { it.value }) { laneId ->
                    Box(Modifier.width(laneWidth).fillMaxHeight()) {
                        RenderLane(laneId, state, callbacks)
                    }
                }
            }
            // Right-edge overflow hint
            androidx.compose.animation.AnimatedVisibility(
                visible = listState.canScrollForward,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MiseTokens.colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "More lanes",
                        tint = MiseTokens.colors.ink3,
                    )
                }
            }
        }
    }
}

private fun buildVisibleLanes(state: CookUiState): List<LaneId> {
    val card = state.card ?: return emptyList()
    val out = mutableListOf<LaneId>()
    // Mise is per-recipe (no shared lane); only the Cook stage has a shared lane.
    val sharedExists = state.stage == CookStage.COOK && state.sharedCookSteps.isNotEmpty()
    if (sharedExists && !state.isHidden(state.stage, LaneId.SHARED)) out += LaneId.SHARED
    for (r in card.recipes) {
        val id = LaneId(r.id.value)
        val hidden = state.isHidden(state.stage, id) ||
            (state.stage == CookStage.COOK && r.id in state.completedRecipes)
        if (!hidden) out += id
    }
    return out
}

@Composable
private fun RenderLane(laneId: LaneId, state: CookUiState, callbacks: CookLaneCallbacks) {
    if (laneId == LaneId.SHARED) {
        // Shared lane exists for COOK only — mise is intentionally per-recipe.
        if (state.stage == CookStage.COOK) SharedCookLane(state, callbacks)
        return
    }
    val recipeId = RecipeId(laneId.value)
    when (state.stage) {
        CookStage.COOK -> {
            val guide = state.cookGuides.firstOrNull { it.recipe.id == recipeId } ?: return
            CookLane(guide, state, callbacks)
        }
        CookStage.MISE -> {
            val guide = state.miseGuides.firstOrNull { it.recipe.id == recipeId } ?: return
            MiseLane(guide, state, callbacks)
        }
    }
}

// --- lane shell + body + footer -------------------------------------------

@Composable
private fun LaneShell(accent: Color, content: @Composable ColumnScope.() -> Unit) {
    MiseCard(modifier = Modifier.fillMaxSize(), radius = MiseTokens.shapes.cardLg) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(3.dp).background(accent))
            content()
        }
    }
}

@Composable
private fun LaneScrollBody(modifier: Modifier, content: @Composable () -> Unit) {
    val scroll = rememberScrollState()
    val showFade by remember(scroll) {
        androidx.compose.runtime.derivedStateOf { scroll.maxValue > 0 && scroll.value < scroll.maxValue - 24 }
    }
    Box(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxSize().verticalScroll(scroll)) { content() }
        AnimatedVisibility(visible = showFade, modifier = Modifier.align(Alignment.BottomCenter)) {
            ScrollFadeIndicator()
        }
    }
}

@Composable
private fun LaneFooter(
    isLast: Boolean,
    enabled: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    isFinishable: Boolean = true,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(MiseTokens.colors.surface2)
                .clickable(enabled = enabled) { onBack() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = MiseTokens.colors.ink2)
        }
        val primaryBg = if (isLast && isFinishable) MiseTokens.colors.recipe(RecipeColor.GREEN) else MiseTokens.colors.ink
        Row(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (enabled) primaryBg else MiseTokens.colors.surface2)
                .clickable(enabled = enabled) { onNext() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                if (isLast && isFinishable) "Done · finish" else "Next step",
                style = MiseTokens.text.bodyEmphasis,
                color = if (enabled) MiseTokens.colors.surface else MiseTokens.colors.ink3,
            )
        }
    }
}

// --- per-recipe lanes ------------------------------------------------------

@Composable
private fun CookLane(guide: RecipeCookGuide, state: CookUiState, callbacks: CookLaneCallbacks) {
    val recipe = guide.recipe
    val accent = MiseTokens.colors.recipe(recipe.color)
    val steps = guide.steps
    val index = state.recipeStepIndex(recipe.id).coerceIn(0, maxOf(0, steps.lastIndex))
    LaneShell(accent) {
        LaneHeader(
            letter = state.recipeLetter(recipe.id),
            emoji = recipe.emoji,
            title = recipe.name,
            accent = accent,
            statusLabel = if (steps.isEmpty()) "All steps shared" else "Step ${index + 1} of ${steps.size}",
            total = steps.size,
            currentIndex = index,
            showDots = true,
        )
        LaneScrollBody(Modifier.weight(1f)) {
            if (steps.isEmpty()) {
                Text(
                    "All of this recipe's cook steps are shared with other recipes.",
                    style = MiseTokens.text.small,
                    color = MiseTokens.colors.ink3,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                val step = steps[index]
                StepCard(
                    step = step,
                    started = StepId(step.id.value) in state.startedTimerStepIds,
                    onStartTimer = { callbacks.startStepTimer(step, recipe.color, recipe.emoji) },
                    onClick = { callbacks.advanceRecipe(recipe.id, 1) },
                )
            }
        }
        if (steps.isNotEmpty()) {
            LaneFooter(
                isLast = index == steps.lastIndex,
                enabled = true,
                onBack = { callbacks.advanceRecipe(recipe.id, -1) },
                onNext = { callbacks.advanceRecipe(recipe.id, 1) },
            )
        }
    }
}

@Composable
private fun MiseLane(guide: RecipeMiseGuide, state: CookUiState, callbacks: CookLaneCallbacks) {
    val recipe = guide.recipe
    val accent = MiseTokens.colors.recipe(recipe.color)
    val letter = state.recipeLetter(recipe.id)
    val bowls = guide.bowls
    val checked = bowls.count { it.id in state.checkedBowls }
    LaneShell(accent) {
        LaneHeader(
            letter = letter,
            emoji = recipe.emoji,
            title = recipe.name,
            accent = accent,
            statusLabel = "$checked/${bowls.size} bowls",
            showDots = false,
        )
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(bowls, key = { it.id.value }) { bowl ->
                val idx = bowls.indexOf(bowl) + 1
                MiseBowlCard(
                    label = "$letter$idx",
                    bowl = bowl,
                    checked = bowl.id in state.checkedBowls,
                    onToggle = { isChecked -> callbacks.toggleBowl(bowl.id, isChecked) },
                )
            }
        }
    }
}

// --- shared lanes ----------------------------------------------------------

@Composable
private fun SharedCookLane(state: CookUiState, callbacks: CookLaneCallbacks) {
    val accent = MiseTokens.colors.accentSoft
    val steps = state.sharedCookSteps
    if (steps.isEmpty()) return
    val index = state.sharedStepIndex.coerceIn(0, steps.lastIndex)
    val step = steps[index]
    LaneShell(accent) {
        LaneHeader(
            emoji = "🥣",
            title = "Shared steps",
            accent = accent,
            statusLabel = "Step ${index + 1} of ${steps.size}",
            total = steps.size,
            currentIndex = index,
            showDots = true,
        )
        LaneScrollBody(Modifier.weight(1f)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { callbacks.advanceShared(1) }
                    .padding(16.dp),
            ) {
                Text("SHARED STEP", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                Spacer(Modifier.height(6.dp))
                Text(step.title, style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                Spacer(Modifier.height(10.dp))
                InstructionStepBlock(lines = step.steps, style = MiseTokens.text.body, stepSpacing = 10)
                if (step.ingredients.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    step.ingredients.forEach { IngredientRow(it) }
                }
                step.timer?.let { timer ->
                    Spacer(Modifier.height(14.dp))
                    val started = StepId(step.id) in state.startedTimerStepIds
                    if (!started) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(MiseTokens.colors.ink)
                                .clickable { callbacks.startSharedTimer(step) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Text(
                                "Start timer · ${timer.title} · ${formatMmSs(timer.durationSeconds)}",
                                style = MiseTokens.text.bodyEmphasis,
                                color = MiseTokens.colors.surface,
                            )
                        }
                    } else {
                        Text("${timer.title} running · see top bar", style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
                    }
                }
            }
        }
        LaneFooter(
            isLast = index == steps.lastIndex,
            enabled = true,
            onBack = { callbacks.advanceShared(-1) },
            onNext = { callbacks.advanceShared(1) },
            isFinishable = false,
        )
    }
}
