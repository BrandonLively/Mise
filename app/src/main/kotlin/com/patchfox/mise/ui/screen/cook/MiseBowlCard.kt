package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.MiseBowl
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * A mise bowl in a checklist lane. Expanded (unchecked) shows ingredients + prose;
 * checking collapses it to a tight number + name row to reclaim vertical space.
 */
@Composable
fun MiseBowlCard(
    label: String,
    bowl: MiseBowl,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    MiseCard(
        modifier = modifier.fillMaxWidth(),
        radius = MiseTokens.shapes.cardSm,
        background = if (checked) MiseTokens.colors.surface2 else MiseTokens.colors.surface,
        onClick = { onToggle(!checked) },
    ) {
        if (checked) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().alpha(0.7f).padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(label, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                Spacer(Modifier.width(10.dp))
                Text(
                    bowl.name,
                    style = MiseTokens.text.bodyEmphasis,
                    color = MiseTokens.colors.ink2,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                LaneCheckbox(checked = true, onToggle = onToggle)
            }
        } else {
            Column(Modifier.fillMaxWidth().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.weight(1f))
                    LaneCheckbox(checked = false, onToggle = onToggle)
                }
                Spacer(Modifier.height(6.dp))
                Text(bowl.name, style = MiseTokens.text.h4, color = MiseTokens.colors.ink)
                Spacer(Modifier.height(8.dp))
                bowl.ingredients.forEach { IngredientRow(it) }
                if (bowl.steps.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    bowl.steps.take(3).forEach { line ->
                        Text(line, style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
                    }
                }
            }
        }
    }
}
