package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * Standard mise card surface — 1dp inset border, 1dp shadow, rounded corners,
 * clipped contents. The shadow is drawn outside the clip; everything else
 * (background, border, child content, click ripple) is clipped to [shape] so
 * accent bands and ripples respect the rounded corners.
 */
@Composable
fun MiseCard(
    modifier: Modifier = Modifier,
    radius: Dp = MiseTokens.shapes.card,
    background: Color = MiseTokens.colors.surface,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    Box(
        modifier = modifier
            .shadow(1.dp, shape, clip = false, spotColor = MiseTokens.colors.ink.copy(alpha = 0.04f))
            .clip(shape)
            .background(background)
            .border(1.dp, MiseTokens.colors.line, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        content = content,
    )
}
