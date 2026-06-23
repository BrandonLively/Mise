package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.Ingredient
import com.patchfox.mise.ui.component.formatIngredient
import com.patchfox.mise.ui.theme.MiseTokens

/** A single ingredient line: bullet + "{qty}{unit} {name}, {prep}" (renders quantityDisplay). */
@Composable
fun IngredientRow(ingredient: Ingredient, modifier: Modifier = Modifier) {
    Row(modifier.padding(vertical = 2.dp)) {
        Text("·", style = MiseTokens.text.body, color = MiseTokens.colors.ink3)
        Spacer(Modifier.width(8.dp))
        Text(formatIngredient(ingredient), style = MiseTokens.text.body, color = MiseTokens.colors.ink2)
    }
}

/** Warm-theme square checkbox used by the mise lanes. */
@Composable
fun LaneCheckbox(
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = MiseTokens.colors.accent,
) {
    val shape = RoundedCornerShape(7.dp)
    Box(
        modifier = modifier
            .size(24.dp)
            .clip(shape)
            .background(if (checked) accent else MiseTokens.colors.surface)
            .border(1.5.dp, if (checked) accent else MiseTokens.colors.lineStrong, shape)
            .clickable { onToggle(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = MiseTokens.colors.surface, modifier = Modifier.size(16.dp))
        }
    }
}
