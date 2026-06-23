package com.patchfox.mise.ui.screen.cook

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.window.WindowSize

/**
 * Cook route host. Collects state, keeps the app-session "Cook visible" signal in
 * sync, handles the stage / step deep-links, and renders the adaptive [CookContent]
 * board (which sizes itself to phone vs tablet).
 */
@Composable
fun CookScreen(
    @Suppress("UNUSED_PARAMETER") windowSize: WindowSize,
    initialStepId: String?,
    initialStage: CookStage?,
    viewModel: CookViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        viewModel.setCookScreenVisible(true)
        onDispose { viewModel.setCookScreenVisible(false) }
    }

    LaunchedEffect(initialStage) {
        if (initialStage != null) viewModel.setStage(initialStage)
    }

    LaunchedEffect(state.card?.id, initialStepId) {
        if (initialStepId != null && state.card != null) viewModel.jumpToStep(initialStepId)
    }

    val laneCallbacks = remember(viewModel) {
        CookLaneCallbacks(
            advanceRecipe = viewModel::advanceRecipe,
            advanceShared = viewModel::advanceShared,
            toggleBowl = viewModel::toggleBowl,
            startStepTimer = viewModel::startStepTimer,
            startSharedTimer = viewModel::startSharedTimer,
        )
    }

    CookContent(
        state = state,
        onSetStage = viewModel::setStage,
        onToggleLane = { lane -> viewModel.toggleLane(state.stage, lane) },
        onReopenRecipe = viewModel::reopenRecipe,
        onDismissTimer = viewModel::dismissTimer,
        laneCallbacks = laneCallbacks,
    )
}
