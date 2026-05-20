package com.patchfox.mise.ui.screen.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patchfox.mise.data.state.SummaryInputRepository
import com.patchfox.mise.domain.model.ComponentYield
import com.patchfox.mise.domain.model.ComponentYieldGroup
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.window.WindowSize

@Composable
fun SummaryScreen(
    windowSize: WindowSize,
    viewModel: SummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    if (state.card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (state.loading) CircularProgressIndicator()
        }
        return
    }
    when (windowSize) {
        WindowSize.Compact -> SummaryPhone(state, viewModel)
        else -> SummaryTablet(state, viewModel)
    }
}

@Composable
private fun SummaryPhone(state: SummaryUiState, viewModel: SummaryViewModel) {
    val card = state.card ?: return
    val groups = weighableGroups(card)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .imePadding()
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("COOK COMPLETE · WEIGH THE RESULT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(8.dp))
            Text("What's in the containers?", style = MiseTokens.text.h2, color = MiseTokens.colors.ink)
            Spacer(Modifier.height(14.dp))
        }
        item {
            DivideByTile(days = state.days, onChange = viewModel::setDays)
        }
        items(groups) { (recipe, group) ->
            RecipeWeighCard(
                recipe = recipe,
                group = group,
                days = state.days,
                inputs = state.inputs,
                onChange = viewModel::setWeight,
            )
        }
    }
}

@Composable
private fun SummaryTablet(state: SummaryUiState, viewModel: SummaryViewModel) {
    val card = state.card ?: return
    val groups = weighableGroups(card)
    Row(modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .imePadding()
    ) {
        Column(
            modifier = Modifier
                .width(380.dp)
                .fillMaxHeight()
                .background(MiseTokens.colors.surface)
                .padding(28.dp),
        ) {
            Text("COOK COMPLETE · WEIGH THE RESULT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(14.dp))
            Text("What's in the", style = MiseTokens.text.h1, color = MiseTokens.colors.ink)
            Text(
                "containers?",
                style = MiseTokens.text.h1.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = MiseTokens.colors.accent,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Type batch weights to learn what really lands in each container. The per-day split refreshes live.",
                style = MiseTokens.text.body,
                color = MiseTokens.colors.ink2,
            )
            Spacer(Modifier.height(24.dp))
            DivideByTile(days = state.days, onChange = viewModel::setDays, large = true)
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            items(groups) { (recipe, group) ->
                RecipeWeighCard(
                    recipe = recipe,
                    group = group,
                    days = state.days,
                    inputs = state.inputs,
                    onChange = viewModel::setWeight,
                )
            }
        }
    }
}

@Composable
private fun DivideByTile(days: Int, onChange: (Int) -> Unit, large: Boolean = false) {
    MiseCard(modifier = Modifier.fillMaxWidth(), background = MiseTokens.colors.surface2) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("DIVIDE BY", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Stepper("–") { onChange((days - 1).coerceAtLeast(1)) }
                Text(
                    days.toString(),
                    style = if (large) MiseTokens.text.h1 else MiseTokens.text.clockLarge,
                    color = MiseTokens.colors.ink,
                    modifier = Modifier.padding(horizontal = 18.dp),
                )
                Stepper("+") { onChange((days + 1).coerceAtMost(14)) }
            }
        }
    }
}

@Composable
private fun Stepper(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(MiseTokens.colors.surface)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
    }
}

private data class WeighableGroup(val recipe: Recipe, val group: ComponentYieldGroup)

private fun weighableGroups(card: CookCard): List<Pair<Recipe, ComponentYieldGroup>> {
    val recipesById = card.recipes.associateBy { it.id }
    val groups = if (card.componentYields.isNotEmpty()) card.componentYields else card.recipes.map { r ->
        ComponentYieldGroup(
            recipeId = r.id,
            components = listOf(
                ComponentYield(
                    label = "Total",
                    yieldDescription = null,
                    yieldGrams = 0.0,
                    totalCalories = 0.0,
                    perServingCalories = 0.0,
                    addedFreshPerServing = false,
                ),
            ),
        )
    }
    return groups.mapNotNull { g -> recipesById[g.recipeId]?.let { it to g } }
}

@Composable
private fun RecipeWeighCard(
    recipe: Recipe,
    group: ComponentYieldGroup,
    days: Int,
    inputs: Map<SummaryInputRepository.Key, String>,
    onChange: (RecipeId, String, String) -> Unit,
) {
    MiseCard(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MiseTokens.colors.recipe(recipe.color).copy(alpha = 0.08f),
                    )
                    .padding(16.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(recipe.emoji, style = MiseTokens.text.h2)
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(recipe.name, style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                        Text(
                            "${group.components.size} components",
                            style = MiseTokens.text.micro,
                            color = MiseTokens.colors.ink3,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                group.components.forEach { component ->
                    val key = SummaryInputRepository.Key(recipe.id, component.label)
                    val raw = inputs[key] ?: ""
                    val parsed = raw.toDoubleOrNull() ?: 0.0
                    val perDay = if (parsed > 0 && days > 0) (parsed / days).toInt() else 0

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(component.label, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink, modifier = Modifier.weight(1f))
                            if (component.yieldGrams > 0) {
                                Text(
                                    "plan: ${component.yieldGrams.toInt()}g",
                                    style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize),
                                    color = MiseTokens.colors.ink3,
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        WeightInputField(
                            raw = raw,
                            onChange = { onChange(recipe.id, component.label, it) },
                        )
                        if (perDay > 0) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "→ ${perDay}g / day",
                                style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize),
                                color = MiseTokens.colors.recipe(recipe.color),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightInputField(raw: String, onChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    // Local TextFieldValue keeps the cursor pinned to the end on every keystroke.
    // The String state from the ViewModel updates on each onChange; without this,
    // recomposition with the String-only OutlinedTextField API resets the cursor
    // to position 0 and new characters land at the start of the field.
    var fieldValue by remember { mutableStateOf(TextFieldValue(raw, TextRange(raw.length))) }
    androidx.compose.runtime.LaunchedEffect(raw) {
        if (raw != fieldValue.text) {
            fieldValue = TextFieldValue(raw, TextRange(raw.length))
        }
    }
    OutlinedTextField(
        value = fieldValue,
        onValueChange = { newValue ->
            val sanitized = TextFieldValue(newValue.text, TextRange(newValue.text.length))
            fieldValue = sanitized
            onChange(sanitized.text)
        },
        suffix = { Text("g", style = MiseTokens.text.clock, color = MiseTokens.colors.ink3) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
        ),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.End),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MiseTokens.colors.surface,
            unfocusedContainerColor = MiseTokens.colors.surface,
        ),
    )
}
