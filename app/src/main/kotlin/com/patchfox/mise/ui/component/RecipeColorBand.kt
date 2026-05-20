package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.RecipeColor
import com.patchfox.mise.ui.theme.recipe

/**
 * Thin recipe-color accent band. Intended to sit at the top edge of a clipped
 * card surface (see [MiseCard]) — the parent's clip handles rounded corners,
 * so this band stays a plain rectangle.
 */
@Composable
fun RecipeColorBand(
    color: RecipeColor?,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
) {
    if (color == null) return
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(MiseTokens.colors.recipe(color)),
    )
}
