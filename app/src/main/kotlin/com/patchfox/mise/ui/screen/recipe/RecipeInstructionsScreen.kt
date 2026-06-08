package com.patchfox.mise.ui.screen.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.instructionsForRecipe
import com.patchfox.mise.ui.component.InstructionStepBlock
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.RecipeColorBand
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun RecipeInstructionsScreen(
    recipeId: String,
    windowSize: WindowSize,
    onBack: () -> Unit,
    viewModel: RecipeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val card = state.card
    if (card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.loading) CircularProgressIndicator()
        }
        return
    }
    val recipe = card.recipes.firstOrNull { it.id.value == recipeId }
    val steps = card.instructionsForRecipe(RecipeId(recipeId))
    RecipeInstructionsContent(recipe = recipe, steps = steps, onBack = onBack)
}

@Composable
private fun RecipeInstructionsContent(
    recipe: Recipe?,
    steps: List<CookStep>,
    onBack: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MiseTokens.colors.ink2)
                Spacer(Modifier.width(6.dp))
                Text("Back", style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink2)
            }
        }
        item {
            Text("INSTRUCTIONS", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (recipe != null && recipe.emoji.isNotBlank()) {
                    Text(recipe.emoji, style = MiseTokens.text.h2)
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    recipe?.name ?: "Recipe",
                    style = MiseTokens.text.h2,
                    color = MiseTokens.colors.ink,
                )
            }
        }

        if (steps.isEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "No step-by-step instructions are recorded for this recipe.",
                    style = MiseTokens.text.body,
                    color = MiseTokens.colors.ink3,
                )
            }
        } else {
            itemsIndexed(steps) { index, step ->
                InstructionStepCard(step = step, position = index + 1, total = steps.size)
            }
        }
    }
}

@Composable
private fun InstructionStepCard(step: CookStep, position: Int, total: Int) {
    MiseCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            RecipeColorBand(color = step.recipeColor, thickness = 3.dp)
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("STEP $position / $total", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.weight(1f))
                    if (step.clockTime.isNotBlank()) {
                        Text(step.clockTime, style = MiseTokens.text.clock, color = MiseTokens.colors.ink3)
                    }
                }
                Spacer(Modifier.height(10.dp))

                InstructionStepBlock(
                    steps = step.task,
                    headerStyle = MiseTokens.text.h4,
                    ingredientStyle = MiseTokens.text.small,
                    stepSpacing = 12,
                )

                step.timer?.let { timer ->
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(MiseTokens.colors.accentWash, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        val label = buildString {
                            append(timer.title)
                            append(" · ")
                            append(formatDuration(timer.durationSeconds))
                        }
                        Text(label, style = MiseTokens.text.small, color = MiseTokens.colors.accent)
                    }
                }

                if (step.equipmentUsed.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("EQUIPMENT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        step.equipmentUsed.forEach { eq ->
                            Box(
                                modifier = Modifier
                                    .background(MiseTokens.colors.surface2, RoundedCornerShape(999.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            ) {
                                Text(eq, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDuration(seconds: Int): String =
    if (seconds >= 60) {
        val m = seconds / 60
        val s = seconds % 60
        if (s == 0) "${m}m" else "${m}m ${s}s"
    } else {
        "${seconds}s"
    }
