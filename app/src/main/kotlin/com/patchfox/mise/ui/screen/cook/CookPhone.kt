package com.patchfox.mise.ui.screen.cook

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookStep
import kotlin.math.abs
import kotlinx.coroutines.launch
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.Phase
import com.patchfox.mise.domain.model.phase0
import com.patchfox.mise.domain.model.phase1
import com.patchfox.mise.domain.model.phase2
import com.patchfox.mise.ui.component.BigStepCard
import com.patchfox.mise.ui.component.ColorTagCallout
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.MiseChecklistRow
import com.patchfox.mise.ui.component.PendingTimerChip
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.component.PhaseTabs
import com.patchfox.mise.ui.component.StepProgressBar
import com.patchfox.mise.ui.component.StepThumbStrip
import com.patchfox.mise.ui.component.TimerChip
import com.patchfox.mise.ui.component.VoiceMicButton
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.voice.VoiceUi

@Composable
fun CookPhone(
    state: CookUiState,
    actions: CookActions,
    onViewSummary: () -> Unit,
    voiceUi: VoiceUi = VoiceUi.OFF,
) {
    if (state.card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.loading) CircularProgressIndicator()
        }
        return
    }
    val card = state.card

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Header row
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "COOK · " + card.cookDate.dayOfWeek.name + " " +
                        card.cookDate.monthNumber.toString().padStart(2, '0') + "/" +
                        card.cookDate.dayOfMonth.toString().padStart(2, '0'),
                    style = MiseTokens.text.micro,
                    color = MiseTokens.colors.ink3,
                )
                Text(card.theme, style = MiseTokens.text.h3, color = MiseTokens.colors.ink)
            }
            val phase2 = card.phase2()
            if (phase2 != null) {
                Text(
                    "${state.currentStepIndex + 1} / ${phase2.steps.size}",
                    style = MiseTokens.text.clockLarge,
                    color = MiseTokens.colors.ink2,
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        PhaseTabs(
            selected = state.selectedPhase,
            onSelect = actions.setPhase,
        )
        Spacer(Modifier.height(14.dp))

        when (state.selectedPhase) {
            PhaseTab.Phase0 -> Phase0View(card.phase0())
            PhaseTab.Phase1 -> Phase1View(card.phase1(), card.recipes.associateBy { it.id }, state, actions)
            PhaseTab.Phase2 -> Phase2View(
                card.phase2(), state, actions,
                showHandsOff = true,
                onViewSummary = onViewSummary,
                voiceUi = voiceUi,
            )
        }
    }
}

@Composable
private fun Phase0View(phase: Phase.Phase0?) {
    if (phase == null) return
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            if (!phase.purpose.isNullOrBlank()) {
                ColorTagCallout(
                    tag = com.patchfox.mise.domain.model.ColorTag.NOTE,
                    description = phase.purpose,
                )
            }
        }
        items(phase.steps) { step ->
            MiseCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("STEP ${step.stepNumber}", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                        Spacer(Modifier.weight(1f))
                        Text(step.estimatedRuntime ?: "", style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize), color = MiseTokens.colors.ink3)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(step.task.firstOrNull().orEmpty(), style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                    Spacer(Modifier.height(6.dp))
                    Text(step.description, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                }
            }
        }
    }
}

@Composable
private fun Phase1View(
    phase: Phase.Phase1?,
    recipesById: Map<com.patchfox.mise.domain.model.RecipeId, com.patchfox.mise.domain.model.Recipe>,
    state: CookUiState,
    actions: CookActions,
) {
    if (phase == null) return
    val checkedCount = phase.bowls.count { it.id in state.miseChecks }
    val allDone = phase.bowls.isNotEmpty() && checkedCount == phase.bowls.size

    // ViewModel owns the "has-fired" flag so it survives tab switches. Without
    // this, returning to Phase 1 after auto-advance would re-trigger and bounce
    // the user back to Phase 2.
    LaunchedEffect(allDone) {
        actions.onPhase1AllDoneChanged(allDone)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            ColorTagCallout(
                tag = com.patchfox.mise.domain.model.ColorTag.TIP,
                description = "$checkedCount of ${phase.bowls.size} bowls done. Ordered least-to-most odoriferous.",
            )
        }
        items(phase.bowls) { bowl ->
            val primaryRecipe = bowl.forRecipeIds.firstOrNull()?.let(recipesById::get)
            MiseChecklistRow(
                bowl = bowl,
                primaryRecipe = primaryRecipe,
                checked = bowl.id in state.miseChecks,
                onToggle = { checked -> actions.toggleMiseCheck(bowl.id, checked) },
            )
        }
    }
}

@Composable
private fun Phase2View(
    phase: Phase.Phase2?,
    state: CookUiState,
    actions: CookActions,
    showHandsOff: Boolean,
    onViewSummary: () -> Unit,
    voiceUi: VoiceUi,
) {
    if (phase == null || phase.steps.isEmpty()) return
    val steps = phase.steps
    val isCongrats = state.currentStepIndex >= steps.size
    val current = if (isCongrats) null else steps.getOrNull(state.currentStepIndex) ?: return
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var containerWidth by remember { mutableStateOf(0) }

    // Reset offset whenever the displayed card changes underneath us.
    LaunchedEffect(state.currentStepIndex) { offsetX.snapTo(0f) }

    Column(modifier = Modifier.fillMaxSize()) {
        StepProgressBar(
            steps = steps,
            currentIndex = state.currentStepIndex.coerceAtMost(steps.lastIndex),
            onJumpTo = actions.jumpToStep,
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (isCongrats) "DONE" else "${state.currentStepIndex + 1} / ${steps.size}",
                style = MiseTokens.text.clock,
                color = MiseTokens.colors.ink2,
            )
            Spacer(Modifier.padding(start = 10.dp))
            Text(
                if (isCongrats) "MEAL PREP COMPLETE"
                else (current?.recipeName ?: "").uppercase(),
                style = MiseTokens.text.micro,
                color = current?.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink3,
            )
            Spacer(Modifier.weight(1f))
            VoiceMicButton(voiceUi)
        }
        Spacer(Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { containerWidth = it.width }
                .pointerInput(state.currentStepIndex, steps.size) {
                    val touchSlop = viewConfiguration.touchSlop
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val startX = down.position.x
                        var totalDx = 0f
                        var isDrag = false

                        // Track the gesture until all pointers lift. If horizontal
                        // movement crosses touchSlop we flip into drag mode and
                        // start updating the card offset; otherwise we treat it as
                        // a tap on release and check the left/right hit zones.
                        var change: PointerInputChange? = down
                        while (change != null && change.pressed) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            change = event.changes.firstOrNull { it.id == down.id }
                                ?: event.changes.firstOrNull()
                            val dx = change?.positionChange()?.x ?: 0f
                            if (!isDrag) {
                                val totalSinceDown = (change?.position?.x ?: startX) - startX
                                if (abs(totalSinceDown) > touchSlop) {
                                    isDrag = true
                                    totalDx = totalSinceDown
                                    change?.consume()
                                }
                            } else {
                                totalDx += dx
                                coroutineScope.launch { offsetX.snapTo(offsetX.value + dx) }
                                change?.consume()
                            }
                        }

                        if (isDrag) {
                            val width = containerWidth.toFloat().coerceAtLeast(1f)
                            val commitThreshold = (width * 0.25f)
                                .coerceAtLeast(60.dp.toPx())
                            val currentOffset = offsetX.value
                            coroutineScope.launch {
                                when {
                                    currentOffset <= -commitThreshold && state.currentStepIndex < steps.size -> {
                                        offsetX.animateTo(-width, tween(180))
                                        actions.advanceStep(1)
                                    }
                                    currentOffset >= commitThreshold && state.currentStepIndex > 0 -> {
                                        offsetX.animateTo(width, tween(180))
                                        actions.advanceStep(-1)
                                    }
                                    else -> offsetX.animateTo(0f, spring())
                                }
                            }
                        } else {
                            // Tap. Hit-test the 10% edge zones on each side.
                            val width = containerWidth.toFloat().coerceAtLeast(1f)
                            val edge = width * 0.1f
                            val canNext = state.currentStepIndex < steps.size
                            val canPrev = state.currentStepIndex > 0
                            when {
                                startX < edge && canPrev -> coroutineScope.launch {
                                    offsetX.animateTo(width, tween(180))
                                    actions.advanceStep(-1)
                                }
                                startX > width - edge && canNext -> coroutineScope.launch {
                                    offsetX.animateTo(-width, tween(180))
                                    actions.advanceStep(1)
                                }
                            }
                        }
                    }
                },
        ) {
            val cardModifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.toInt(), 0) }
            if (isCongrats) {
                CongratsCard(onViewSummary = onViewSummary, modifier = cardModifier)
            } else {
                BigStepCard(
                    step = current!!,
                    stepIndex = state.currentStepIndex,
                    totalSteps = steps.size,
                    showHandsOff = showHandsOff,
                    modifier = cardModifier,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        StepThumbStrip(
            steps = steps,
            currentIndex = state.currentStepIndex.coerceAtMost(steps.lastIndex),
            onJumpTo = actions.jumpToStep,
        )
        Spacer(Modifier.height(8.dp))
        ActiveTimersStrip(current ?: steps.last(), state, actions)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CongratsCard(onViewSummary: () -> Unit, modifier: Modifier = Modifier) {
    MiseCard(modifier = modifier, radius = MiseTokens.shapes.cardLg) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🎉", style = MiseTokens.text.h1)
            Spacer(Modifier.height(14.dp))
            Text(
                "Meal prep complete",
                style = MiseTokens.text.h2,
                color = MiseTokens.colors.ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "nice work.",
                style = MiseTokens.text.h2.copy(fontStyle = FontStyle.Italic),
                color = MiseTokens.colors.accent,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Weigh the containers next so the per-day split refreshes.",
                style = MiseTokens.text.body,
                color = MiseTokens.colors.ink2,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onViewSummary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiseTokens.colors.ink,
                    contentColor = MiseTokens.colors.surface,
                ),
            ) {
                Text("View summary", style = MiseTokens.text.bodyEmphasis)
            }
        }
    }
}

@Composable
private fun ActiveTimersStrip(
    current: CookStep,
    state: CookUiState,
    actions: CookActions,
) {
    val anyPendingForCurrent = current.timer != null && current.id !in state.startedTimerStepIds
    val sortedActive = state.activeTimers.sortedBy { it.remainingSeconds }
    val hasAnything = sortedActive.isNotEmpty() || anyPendingForCurrent
    var pendingStopId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MiseTokens.colors.surface)
            .padding(14.dp),
    ) {
        Text(
            "ACTIVE TIMERS · ${sortedActive.size}",
            style = MiseTokens.text.micro,
            color = MiseTokens.colors.ink3,
        )
        Spacer(Modifier.height(8.dp))
        if (!hasAnything) {
            Text("Nothing running.", style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
        } else {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (anyPendingForCurrent) {
                    val timer = current.timer!!
                    PendingTimerChip(
                        title = timer.title,
                        durationSeconds = timer.durationSeconds,
                        recipeColor = current.recipeColor,
                        onStart = { actions.startTimerForCurrentStep() },
                    )
                }
                sortedActive.forEach { t ->
                    TimerChip(
                        title = t.title,
                        remainingSeconds = t.remainingSeconds,
                        recipeEmoji = t.recipeEmoji,
                        recipeColor = t.recipeColor,
                        // Running → confirm stop. Done → dismiss directly. The
                        // +1 minute extension is available from the off-Phase-2
                        // alert dialog when the user is away from this strip.
                        onDismiss = {
                            if (t.remainingSeconds <= 0) actions.dismissTimer(t.id)
                            else pendingStopId = t.id
                        },
                    )
                }
            }
        }
    }

    val stopId = pendingStopId
    if (stopId != null) {
        AlertDialog(
            onDismissRequest = { pendingStopId = null },
            title = { Text("Stop this timer?") },
            text = { Text("It hasn't finished yet.") },
            confirmButton = {
                TextButton(onClick = {
                    actions.dismissTimer(stopId)
                    pendingStopId = null
                }) { Text("Stop") }
            },
            dismissButton = {
                TextButton(onClick = { pendingStopId = null }) { Text("Keep running") }
            },
        )
    }
}
