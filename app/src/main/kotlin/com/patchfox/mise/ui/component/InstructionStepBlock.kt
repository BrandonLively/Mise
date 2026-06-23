package com.patchfox.mise.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * Renders a vertical list of prose lines as bullet rows. In cook-card v3 a step's
 * prose is a plain `List<String>` (no InstructionStep prefix/ingredient wrapper);
 * structured ingredients, when shown, are rendered separately (see [IngredientLines]).
 */
@Composable
fun InstructionStepBlock(
    lines: List<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MiseTokens.text.bodyEmphasis,
    stepSpacing: Int = 10,
) {
    if (lines.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth()) {
        lines.forEachIndexed { idx, line ->
            if (idx > 0) Spacer(Modifier.height(stepSpacing.dp))
            Row {
                Text("·", style = style, color = MiseTokens.colors.ink3)
                Spacer(Modifier.width(8.dp))
                Text(line, style = style, color = MiseTokens.colors.ink)
            }
        }
    }
}
