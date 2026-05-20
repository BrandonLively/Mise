package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.Phase
import com.patchfox.mise.domain.model.phase1
import com.patchfox.mise.domain.model.phase2
import com.patchfox.mise.ui.component.BigStepCard
import com.patchfox.mise.ui.component.MiseChecklistRow
import com.patchfox.mise.ui.component.PendingTimerCardLarge
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.component.PhaseTabs
import com.patchfox.mise.ui.component.TimerCardLarge
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun CookTablet(state: CookUiState, actions: CookActions) {
    if (state.card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.loading) CircularProgressIndicator()
        }
        return
    }
    val card = state.card
    Column(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Cook flow", style = MiseTokens.text.h2, color = MiseTokens.colors.ink)
                Text(
                    card.cookDate.dayOfWeek.name + " · " + card.theme,
                    style = MiseTokens.text.micro,
                    color = MiseTokens.colors.ink3,
                )
            }
            PhaseTabs(
                selected = state.selectedPhase,
                onSelect = actions.setPhase,
                modifier = Modifier.width(280.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
        when (state.selectedPhase) {
            PhaseTab.Phase0 -> Phase0Tablet(card.cookPlan.phases.filterIsInstance<Phase.Phase0>().firstOrNull())
            PhaseTab.Phase1 -> Phase1Tablet(card.phase1(), state, actions, card.recipes.associateBy { it.id })
            PhaseTab.Phase2 -> Phase2Tablet(card.phase2(), state, actions)
        }
    }
}

@Composable
private fun Phase0Tablet(phase: Phase.Phase0?) {
    if (phase == null) return
    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        itemsIndexed(phase.steps) { _, step ->
            com.patchfox.mise.ui.component.MiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("STEP ${step.stepNumber}", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.height(6.dp))
                    Text(step.task.firstOrNull().orEmpty(), style = MiseTokens.text.h3, color = MiseTokens.colors.ink)
                    Spacer(Modifier.height(8.dp))
                    Text(step.description, style = MiseTokens.text.body, color = MiseTokens.colors.ink2)
                }
            }
        }
    }
}

@Composable
private fun Phase1Tablet(
    phase: Phase.Phase1?,
    state: CookUiState,
    actions: CookActions,
    recipesById: Map<com.patchfox.mise.domain.model.RecipeId, com.patchfox.mise.domain.model.Recipe>,
) {
    if (phase == null) return
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = 12.dp,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(phase.bowls) { bowl ->
            val primary = bowl.forRecipeIds.firstOrNull()?.let(recipesById::get)
            MiseChecklistRow(
                bowl = bowl,
                primaryRecipe = primary,
                checked = bowl.id in state.miseChecks,
                onToggle = { c -> actions.toggleMiseCheck(bowl.id, c) },
            )
        }
    }
}

@Composable
private fun Phase2Tablet(phase: Phase.Phase2?, state: CookUiState, actions: CookActions) {
    if (phase == null || phase.steps.isEmpty()) return
    val steps = phase.steps
    val current = steps.getOrNull(state.currentStepIndex) ?: return
    val density = LocalDensity.current
    val railState = rememberLazyListState()

    LaunchedEffect(state.currentStepIndex) {
        railState.animateScrollToItem(state.currentStepIndex.coerceAtMost(steps.lastIndex))
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // Left rail
        LazyColumn(
            state = railState,
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            itemsIndexed(steps) { idx, step ->
                val isActive = idx == state.currentStepIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isActive) MiseTokens.colors.surface else MiseTokens.colors.bg,
                            RoundedCornerShape(8.dp),
                        )
                        .clickable { actions.jumpToStep(idx) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(36.dp)
                            .background(
                                step.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink3,
                                RoundedCornerShape(2.dp),
                            ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(step.clockTime, style = MiseTokens.text.clock, color = MiseTokens.colors.ink2)
                        Row {
                            if (!step.recipeEmoji.isNullOrBlank()) Text(step.recipeEmoji!!, style = MiseTokens.text.small)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                step.task.firstOrNull().orEmpty(),
                                style = MiseTokens.text.small,
                                color = MiseTokens.colors.ink2,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }

        // Center BigStepCard
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
                .pointerInput(state.currentStepIndex, steps.size) {
                    var totalDx = 0f
                    detectHorizontalDragGestures(
                        onDragStart = { totalDx = 0f },
                        onDragEnd = {
                            val threshold = with(density) { 60.dp.toPx() }
                            if (totalDx <= -threshold) actions.advanceStep(1)
                            else if (totalDx >= threshold) actions.advanceStep(-1)
                        },
                        onDragCancel = { totalDx = 0f },
                        onHorizontalDrag = { _, delta -> totalDx += delta },
                    )
                },
        ) {
            BigStepCard(
                step = current,
                stepIndex = state.currentStepIndex,
                totalSteps = steps.size,
                showHandsOff = true,
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Right timers pane
        Column(
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
                .background(MiseTokens.colors.surface)
                .padding(20.dp),
        ) {
            Text(
                "ACTIVE TIMERS · ${state.activeTimers.size} RUNNING",
                style = MiseTokens.text.micro,
                color = MiseTokens.colors.ink3,
            )
            Spacer(Modifier.height(12.dp))
            state.activeTimers.forEach { t ->
                TimerCardLarge(
                    title = t.title,
                    remainingSeconds = t.remainingSeconds,
                    recipeEmoji = t.recipeEmoji,
                    recipeColor = t.recipeColor,
                    onDismiss = { actions.dismissTimer(t.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
            }
            val pending = current.timer
            if (pending != null && current.id !in state.startedTimerStepIds) {
                PendingTimerCardLarge(
                    title = pending.title,
                    durationSeconds = pending.durationSeconds,
                    recipeEmoji = current.recipeEmoji,
                    recipeColor = current.recipeColor,
                    onStart = { actions.startTimerForCurrentStep() },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
