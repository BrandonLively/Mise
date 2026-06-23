package com.patchfox.mise.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun HomeScreen(
    windowSize: WindowSize,
    onWalkPlan: () -> Unit,
    onOpenStage: (CookStage) -> Unit,
    onOpenRecipe: (RecipeId) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    when (windowSize) {
        WindowSize.Compact -> HomePhone(
            state = state,
            onTogglePrep = viewModel::togglePrep,
            onWalkPlan = onWalkPlan,
            onOpenStage = onOpenStage,
            onOpenRecipe = onOpenRecipe,
        )
        else -> HomeTablet(
            state = state,
            onTogglePrep = viewModel::togglePrep,
            onWalkPlan = onWalkPlan,
            onOpenStage = onOpenStage,
            onOpenRecipe = onOpenRecipe,
        )
    }
}
