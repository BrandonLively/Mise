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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.ui.component.ColorTagCallout
import com.patchfox.mise.ui.component.DotLeaderRow
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.theme.recipeWash
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    windowSize: WindowSize,
    onBack: () -> Unit,
    onOpenInstructions: (recipeId: String) -> Unit,
    viewModel: RecipeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val card = state.card
    if (card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val initial = card.recipes.firstOrNull { it.id.value == recipeId } ?: card.recipes.first()

    when (windowSize) {
        WindowSize.Compact -> RecipeDetailPhone(initial, onBack, onOpenInstructions)
        else -> RecipeDetailTablet(card.recipes, initial, onBack, onOpenInstructions)
    }
}

@Composable
internal fun RecipeDetailPhone(
    recipe: Recipe,
    onBack: () -> Unit,
    onOpenInstructions: (recipeId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(modifier = Modifier
                .clickable { onBack() }
                .padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MiseTokens.colors.ink2)
                Spacer(Modifier.width(6.dp))
                Text("Back", style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink2)
            }
        }
        item { RecipeDetailContent(recipe, onOpenInstructions = { onOpenInstructions(recipe.id.value) }) }
    }
}

@Composable
internal fun RecipeDetailTablet(
    recipes: List<Recipe>,
    initial: Recipe,
    onBack: () -> Unit,
    onOpenInstructions: (recipeId: String) -> Unit,
) {
    var selected by remember { mutableStateOf(initial) }
    Row(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MiseTokens.colors.surface)
                .padding(20.dp)
                .width(360.dp),
        ) {
            Row(modifier = Modifier.clickable { onBack() }, verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = MiseTokens.colors.ink2)
                Spacer(Modifier.width(6.dp))
                Text("Back", style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink2)
            }
            Spacer(Modifier.height(20.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recipes) { r ->
                    val active = r.id == selected.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (active) MiseTokens.colors.surface2 else MiseTokens.colors.surface, RoundedCornerShape(12.dp))
                            .clickable { selected = r }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MiseTokens.colors.recipeWash(r.color), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(r.emoji, style = MiseTokens.text.h4)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(r.name, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
                            Text(r.description, style = MiseTokens.text.small, color = MiseTokens.colors.ink3, maxLines = 2)
                        }
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { RecipeDetailContent(selected, onOpenInstructions = { onOpenInstructions(selected.id.value) }) }
        }
    }
}

@Composable
private fun RecipeDetailContent(recipe: Recipe, onOpenInstructions: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(MiseTokens.colors.recipeWash(recipe.color), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    recipe.type.name + " · SERVES " + recipe.yield.servings,
                    style = MiseTokens.text.micro,
                    color = MiseTokens.colors.recipe(recipe.color),
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(recipe.name, style = MiseTokens.text.h2, color = MiseTokens.colors.ink, modifier = Modifier.weight(1f))
            Text(recipe.emoji, style = MiseTokens.text.display.copy(fontSize = MiseTokens.text.h1.fontSize))
        }
        Text(recipe.description, style = MiseTokens.text.body, color = MiseTokens.colors.ink2)

        // Primary action — jump to the step-by-step instructions for this recipe.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MiseTokens.colors.accent)
                .clickable { onOpenInstructions() }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "Instructions",
                style = MiseTokens.text.bodyEmphasis,
                color = MiseTokens.colors.surface,
            )
        }

        // Macros band
        MiseCard(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.padding(16.dp)) {
                MacroCol("KCAL", recipe.perServing.calories.toInt().toString())
                MacroCol("PROTEIN", recipe.perServing.proteinGrams.toInt().toString() + "g")
                MacroCol("CARBS", recipe.perServing.carbsGrams.toInt().toString() + "g")
                MacroCol("FAT", recipe.perServing.fatGrams.toInt().toString() + "g")
            }
        }

        if (recipe.colorTag != null && !recipe.colorTagDescription.isNullOrBlank()) {
            ColorTagCallout(tag = recipe.colorTag, description = recipe.colorTagDescription!!)
        }

        // Ingredients
        Text("INGREDIENTS", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
        Column {
            recipe.ingredients.forEach { ing ->
                DotLeaderRow(name = ing.name, quantity = ing.batchQuantity)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MiseTokens.colors.line),
                )
            }
        }

        // Equipment
        if (recipe.equipment.isNotEmpty()) {
            Text("EQUIPMENT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                recipe.equipment.forEach { eq ->
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

        // Reheat
        Text("REHEAT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
        MiseCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MiseTokens.colors.recipe(recipe.color), RoundedCornerShape(999.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(recipe.reheat.method, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink, modifier = Modifier.weight(1f))
                    if (recipe.reheat.timeSeconds != null) {
                        Text(
                            if (recipe.reheat.timeSeconds!! >= 60) "${recipe.reheat.timeSeconds!! / 60}m ${recipe.reheat.timeSeconds!! % 60}s"
                            else "${recipe.reheat.timeSeconds}s",
                            style = MiseTokens.text.clock,
                            color = MiseTokens.colors.ink2,
                        )
                    }
                }
                if (recipe.reheat.extraSteps.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    recipe.reheat.extraSteps.forEach { step ->
                        Row {
                            Text("·", style = MiseTokens.text.body, color = MiseTokens.colors.ink3)
                            Spacer(Modifier.width(8.dp))
                            Text(step, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MacroCol(label: String, value: String) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MiseTokens.text.clockLarge, color = MiseTokens.colors.ink)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
    }
}
