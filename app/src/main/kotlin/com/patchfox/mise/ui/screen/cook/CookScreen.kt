package com.patchfox.mise.ui.screen.cook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.ui.component.PhaseTab
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun CookScreen(
    windowSize: WindowSize,
    initialStepId: String?,
    initialPhase: PhaseTab?,
    onViewSummary: () -> Unit,
    viewModel: CookViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

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
        WindowSize.Compact -> CookPhone(state, viewModel, onViewSummary)
        else -> CookTablet(state, viewModel)
    }
}
