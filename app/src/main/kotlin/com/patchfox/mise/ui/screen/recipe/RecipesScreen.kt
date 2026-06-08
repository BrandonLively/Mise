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
import com.patchfox.mise.domain.model.CookCard
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
    onOpenRecipe: (cookCardId: String?, RecipeId) -> Unit,
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
    // Everything other than the current card is "previous".
    val previous = state.history.filter { it.id != card.id }
    RecipesContent(current = card, previous = previous, onOpenRecipe = onOpenRecipe)
}

@Composable
fun RecipesContent(
    current: CookCard,
    previous: List<CookCard>,
    onOpenRecipe: (cookCardId: String?, RecipeId) -> Unit,
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
        items(current.recipes) { recipe ->
            // Current card → null cookCardId so detail reads the live current card.
            RecipeListRow(recipe) { id -> onOpenRecipe(null, id) }
        }

        if (previous.isNotEmpty()) {
            item {
                Spacer(Modifier.height(12.dp))
                Text("PREVIOUS RECIPES", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            }
            previous.forEach { pastCard ->
                item {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        pastCard.theme,
                        style = MiseTokens.text.h4,
                        color = MiseTokens.colors.ink2,
                    )
                    Text(
                        pastCard.cookDate.toString(),
                        style = MiseTokens.text.micro,
                        color = MiseTokens.colors.ink3,
                    )
                }
                items(pastCard.recipes) { recipe ->
                    val cardId = pastCard.id.value
                    RecipeListRow(recipe) { id -> onOpenRecipe(cardId, id) }
                }
            }
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
