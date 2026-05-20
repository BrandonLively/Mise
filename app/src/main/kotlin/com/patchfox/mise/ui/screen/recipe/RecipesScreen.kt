package com.patchfox.mise.ui.screen.recipe

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.RecipeColorBand
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun RecipesScreen(
    windowSize: WindowSize,
    onOpenRecipe: (RecipeId) -> Unit,
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
    RecipesContent(recipes = card.recipes, onOpenRecipe = onOpenRecipe)
}

@Composable
fun RecipesContent(
    recipes: List<Recipe>,
    onOpenRecipe: (RecipeId) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("RECIPES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(6.dp))
            Text("This week's cook card", style = MiseTokens.text.h2, color = MiseTokens.colors.ink)
        }
        items(recipes) { recipe ->
            RecipeListRow(recipe, onOpenRecipe)
        }
    }
}

@Composable
private fun RecipeListRow(recipe: Recipe, onOpen: (RecipeId) -> Unit) {
    MiseCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onOpen(recipe.id) },
    ) {
        Column {
            RecipeColorBand(color = recipe.color, thickness = 3.dp)
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                Text(recipe.emoji, style = MiseTokens.text.h1)
                Spacer(Modifier.padding(start = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(recipe.type.name, style = MiseTokens.text.micro, color = MiseTokens.colors.recipe(recipe.color))
                    Text(recipe.name, style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                    Spacer(Modifier.height(4.dp))
                    Text(recipe.description, style = MiseTokens.text.small, color = MiseTokens.colors.ink2, maxLines = 2)
                }
            }
        }
    }
}
