package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * The Cook board, used for both phone and tablet. The [LaneScroller] adapts to width
 * (≈1 lane on a phone, 3 on a tablet), so a single adaptive layout covers both.
 * Kept ViewModel-free (callbacks hoisted) so it renders under screenshot tests.
 */
@Composable
fun CookContent(
    state: CookUiState,
    onSetStage: (CookStage) -> Unit,
    onToggleLane: (LaneId) -> Unit,
    onReopenRecipe: (RecipeId) -> Unit,
    onDismissTimer: (String) -> Unit,
    laneCallbacks: CookLaneCallbacks,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(MiseTokens.colors.bg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.stage == CookStage.MISE) "Mise en place" else "Cooking lanes",
                    style = MiseTokens.text.h2,
                    color = MiseTokens.colors.ink,
                )
                state.card?.let {
                    Text(
                        "${it.theme.uppercase()} · COOK SESSION",
                        style = MiseTokens.text.micro,
                        color = MiseTokens.colors.ink3,
                    )
                }
            }
            Spacer(Modifier.height(0.dp))
            StageToggle(state.stage, onSetStage)
        }

        when {
            state.card == null && state.loading ->
                CenterMessage("Loading the cook card…")
            state.card == null ->
                CenterMessage("No cook card yet. Pull to sync once one is uploaded.")
            else -> {
                CommandBar(
                    state = state,
                    onToggleLane = onToggleLane,
                    onReopenRecipe = onReopenRecipe,
                    onDismissTimer = onDismissTimer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                LaneScroller(state, laneCallbacks, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.CenterMessage(text: String) {
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(text, style = MiseTokens.text.body, color = MiseTokens.colors.ink3)
    }
}
