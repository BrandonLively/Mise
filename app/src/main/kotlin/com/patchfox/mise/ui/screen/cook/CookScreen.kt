package com.patchfox.mise.ui.screen.cook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.window.WindowSize

/**
 * Bundle of Cook-screen callbacks. Hoisting these into a value lets [CookPhone]
 * and [CookTablet] (and their inner composables) stay ViewModel-free, so they
 * render under Paparazzi / previews without a Hilt graph.
 */
data class CookActions(
    val setPhase: (PhaseTab) -> Unit,
    val jumpToStep: (Int) -> Unit,
    val advanceStep: (Int) -> Unit,
    val toggleMiseCheck: (BowlId, Boolean) -> Unit,
    val startTimerForCurrentStep: () -> Unit,
    val dismissTimer: (String) -> Unit,
    val onPhase1AllDoneChanged: (Boolean) -> Unit,
) {
    companion object {
        /** No-op actions, for previews and screenshot tests. */
        val NONE = CookActions(
            setPhase = {},
            jumpToStep = {},
            advanceStep = {},
            toggleMiseCheck = { _, _ -> },
            startTimerForCurrentStep = {},
            dismissTimer = {},
            onPhase1AllDoneChanged = {},
        )
    }
}

@Composable
fun CookScreen(
    windowSize: WindowSize,
    initialStepId: String?,
    initialPhase: PhaseTab?,
    onViewSummary: () -> Unit,
    viewModel: CookViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val actions = remember(viewModel) {
        CookActions(
            setPhase = viewModel::setPhase,
            jumpToStep = viewModel::jumpToStep,
            advanceStep = viewModel::advanceStep,
            toggleMiseCheck = viewModel::toggleMiseCheck,
            startTimerForCurrentStep = viewModel::startTimerForCurrentStep,
            dismissTimer = viewModel::dismissTimer,
            onPhase1AllDoneChanged = viewModel::onPhase1AllDoneChanged,
        )
    }

    // Tell the app-level session this screen is visible so the off-Phase-2 timer
    // alert stays suppressed while the user is on Cook (and Phase 2 specifically).
    DisposableEffect(Unit) {
        viewModel.setCookScreenVisible(true)
        onDispose { viewModel.setCookScreenVisible(false) }
    }

    LaunchedEffect(initialPhase) {
        if (initialPhase != null) viewModel.setPhase(initialPhase)
    }

    LaunchedEffect(state.card?.id, initialStepId) {
        if (initialStepId != null && state.card != null) {
            val idx = state.card!!.let { card ->
                val steps = card.cookPlan.phases
                    .filterIsInstance<com.patchfox.mise.domain.model.Phase.Phase2>()
                    .firstOrNull()?.steps.orEmpty()
                steps.indexOfFirst { it.id.value == initialStepId }
            }
            if (idx >= 0) viewModel.jumpToStep(idx)
        }
    }

    when (windowSize) {
        WindowSize.Compact -> CookPhone(state, actions, onViewSummary)
        else -> CookTablet(state, actions)
    }
}
