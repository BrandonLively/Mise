package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.ColorTag
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.RecipeColor
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.theme.recipeWash

private fun ColorTag.recipeColor(): RecipeColor = when (this) {
    ColorTag.NOTE -> RecipeColor.BLUE
    ColorTag.TIP -> RecipeColor.GREEN
    ColorTag.IMPORTANT -> RecipeColor.PURPLE
    ColorTag.WARNING -> RecipeColor.YELLOW
    ColorTag.CAUTION -> RecipeColor.RED
}

@Composable
fun ColorTagCallout(
    tag: ColorTag,
    description: String,
    modifier: Modifier = Modifier,
) {
    val color = tag.recipeColor()
    val border = MiseTokens.colors.recipe(color)
    val wash = MiseTokens.colors.recipeWash(color)
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .drawBehind {
                drawRect(
                    color = border,
                    topLeft = Offset(0f, 0f),
                    size = Size(3.dp.toPx(), size.height),
                )
            }
            .background(wash)
            .padding(start = 14.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
    ) {
        Text(tag.name, style = MiseTokens.text.micro, color = border)
        Spacer(Modifier.height(4.dp))
        Text(description, style = MiseTokens.text.body, color = MiseTokens.colors.ink2)
    }
}
