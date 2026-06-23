package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.component.TimerChip
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.theme.recipeWash

/**
 * The command bar conducts the session: a row of recipe chips (toggle/scroll lanes
 * + show status) on the left, running timers on the right. Lane visibility is
 * independent per stage, so the chips reflect [CookUiState.stage].
 */
@Composable
fun CommandBar(
    state: CookUiState,
    onToggleLane: (LaneId) -> Unit,
    onReopenRecipe: (RecipeId) -> Unit,
    onDismissTimer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val recipes = state.card?.recipes.orEmpty()
    // Mise is per-recipe (no shared lane); only the Cook stage has a shared lane.
    val hasShared = state.stage == CookStage.COOK && state.sharedCookSteps.isNotEmpty()
    Row(modifier = modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
        // Recipes group
        Column(Modifier.weight(1f)) {
            Text("TAP TO SHOW / HIDE LANE", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp),
            ) {
                if (hasShared) {
                    item { SharedCommandChip(state, onClick = { onToggleLane(LaneId.SHARED) }) }
                }
                items(recipes, key = { it.id.value }) { recipe ->
                    RecipeCommandChip(
                        recipe = recipe,
                        state = state,
                        onClick = {
                            if (state.stage == CookStage.COOK && recipe.id in state.completedRecipes) {
                                onReopenRecipe(recipe.id)
                            } else {
                                onToggleLane(LaneId(recipe.id.value))
                            }
                        },
                    )
                }
            }
        }

        Box(Modifier.padding(horizontal = 12.dp).width(1.dp).fillMaxHeight(0.7f).background(MiseTokens.colors.line))

        // Timers group
        Column {
            val running = state.activeTimers.size
            Text("TIMERS · $running RUNNING", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(6.dp))
            if (state.activeTimers.isEmpty()) {
                Text("None running", style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.activeTimers, key = { it.id }) { t ->
                        TimerChip(
                            title = t.title,
                            remainingSeconds = t.remainingSeconds,
                            recipeEmoji = t.recipeEmoji,
                            recipeColor = t.recipeColor,
                            onDismiss = { onDismissTimer(t.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCommandChip(recipe: Recipe, state: CookUiState, onClick: () -> Unit) {
    val accent = MiseTokens.colors.recipe(recipe.color)
    val wash = MiseTokens.colors.recipeWash(recipe.color)
    val laneId = LaneId(recipe.id.value)
    val completed = state.stage == CookStage.COOK && recipe.id in state.completedRecipes
    val visible = !state.isHidden(state.stage, laneId) && !completed

    val status: String = when (state.stage) {
        CookStage.COOK -> {
            if (completed) {
                "done"
            } else {
                val total = state.cookGuides.firstOrNull { it.recipe.id == recipe.id }?.steps?.size ?: 0
                if (total == 0) "—" else "${(state.recipeStepIndex(recipe.id) + 1).coerceAtMost(total)}/$total"
            }
        }
        CookStage.MISE -> {
            val bowls = state.miseGuides.firstOrNull { it.recipe.id == recipe.id }?.bowls.orEmpty()
            val checked = bowls.count { it.id in state.checkedBowls }
            "$checked/${bowls.size} bowls"
        }
    }

    Chip(
        emoji = recipe.emoji,
        name = recipe.name.split(Regex("\\s+")).take(2).joinToString(" "),
        status = status,
        accent = accent,
        fill = if (visible) wash else MiseTokens.colors.surface,
        ringed = visible,
        dim = completed,
        showCheck = completed,
        onClick = onClick,
        letter = state.recipeLetter(recipe.id),
    )
}

@Composable
private fun SharedCommandChip(state: CookUiState, onClick: () -> Unit) {
    val visible = !state.isHidden(state.stage, LaneId.SHARED)
    // The shared chip only appears on the Cook stage (mise is per-recipe).
    val total = state.sharedCookSteps.size
    val status = if (total == 0) "—" else "${(state.sharedStepIndex + 1).coerceAtMost(total)}/$total"
    Chip(
        emoji = "🥣",
        name = "Shared",
        status = status,
        accent = MiseTokens.colors.accentSoft,
        fill = if (visible) MiseTokens.colors.accentWash else MiseTokens.colors.surface,
        ringed = visible,
        dim = false,
        showCheck = false,
        onClick = onClick,
    )
}

@Composable
private fun Chip(
    emoji: String,
    name: String,
    status: String,
    accent: Color,
    fill: Color,
    ringed: Boolean,
    dim: Boolean,
    showCheck: Boolean,
    onClick: () -> Unit,
    letter: String? = null,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (dim) 0.55f else 1f)
            .height(44.dp)
            .clip(shape)
            .background(fill)
            .then(if (ringed) Modifier.border(1.5.dp, accent, shape) else Modifier.border(1.dp, MiseTokens.colors.line, shape))
            .clickable { onClick() }
            .padding(start = 8.dp, end = 14.dp),
    ) {
        Box(
            modifier = Modifier.size(26.dp).clip(CircleShape).background(MiseTokens.colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            if (showCheck) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MiseTokens.colors.recipe(com.patchfox.mise.ui.theme.RecipeColor.GREEN), modifier = Modifier.size(16.dp))
            } else {
                Text(emoji, style = MiseTokens.text.small)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (letter != null) {
                    Text(letter, style = MiseTokens.text.bodyEmphasis, color = accent)
                    Spacer(Modifier.width(4.dp))
                }
                Text(name, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(5.dp))
                Text(status, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3, maxLines = 1)
            }
        }
    }
}
