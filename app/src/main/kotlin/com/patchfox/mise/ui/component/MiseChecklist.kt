package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.domain.model.Recipe
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun MiseChecklistRow(
    bowl: MiseBowl,
    primaryRecipe: Recipe?,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = primaryRecipe?.color
    MiseCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (checked) 0.65f else 1f),
        background = if (checked) MiseTokens.colors.surface2 else MiseTokens.colors.surface,
        onClick = { onToggle(!checked) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Text("#${bowl.bowlNumber}", style = MiseTokens.text.clock, color = MiseTokens.colors.ink3)
            Spacer(Modifier.width(12.dp))
            if (color != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(MiseTokens.colors.recipe(color), CircleShape),
                )
                Spacer(Modifier.width(8.dp))
            }
            if (!primaryRecipe?.emoji.isNullOrBlank()) {
                Text(primaryRecipe!!.emoji, style = MiseTokens.text.body)
                Spacer(Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    bowl.name,
                    style = MiseTokens.text.h4.copy(
                        textDecoration = if (checked) TextDecoration.LineThrough else null,
                    ),
                    color = MiseTokens.colors.ink,
                )
                if (bowl.instruction.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    InstructionStepBlock(
                        steps = bowl.instruction,
                        headerStyle = MiseTokens.text.body,
                        ingredientStyle = MiseTokens.text.small,
                    )
                }
            }
        }
    }
}

@Composable
fun BowlIdChip(bowlId: BowlId, modifier: Modifier = Modifier) {
    // Helper for previews — unused at runtime
}
