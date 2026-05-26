package com.patchfox.mise.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.InstructionStep
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * Renders a vertical list of [InstructionStep] entries. Each step is one
 * heading bullet (the action prefix) with its ingredients as indented
 * sub-bullets below. Action-only steps (`ingredients == null`) render as a
 * heading line with no sub-list.
 *
 * The header/body styles are tunable so the same block fits the large
 * Phase 2 step card and the more compact Phase 1 bowl row.
 */
@Composable
fun InstructionStepBlock(
    steps: List<InstructionStep>,
    modifier: Modifier = Modifier,
    headerStyle: TextStyle = MiseTokens.text.bodyEmphasis,
    ingredientStyle: TextStyle = MiseTokens.text.small,
    stepSpacing: Int = 10,
) {
    if (steps.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        steps.forEachIndexed { idx, step ->
            if (idx > 0) Spacer(Modifier.height(stepSpacing.dp))
            InstructionStepRow(step, headerStyle, ingredientStyle)
        }
    }
}

@Composable
private fun InstructionStepRow(
    step: InstructionStep,
    headerStyle: TextStyle,
    ingredientStyle: TextStyle,
) {
    Row {
        Text("·", style = headerStyle, color = MiseTokens.colors.ink3)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(step.prefix, style = headerStyle, color = MiseTokens.colors.ink)
            val ingredients = step.ingredients.orEmpty()
            if (ingredients.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                ingredients.forEach { ing ->
                    Row(modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp)) {
                        Text("·", style = ingredientStyle, color = MiseTokens.colors.ink3)
                        Spacer(Modifier.width(8.dp))
                        Text(formatIngredient(ing), style = ingredientStyle, color = MiseTokens.colors.ink2)
                    }
                }
            }
        }
    }
}
