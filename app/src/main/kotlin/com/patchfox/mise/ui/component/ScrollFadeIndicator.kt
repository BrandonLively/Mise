package com.patchfox.mise.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * Bottom gradient-fade overlay + a gently bouncing chevron — the "there's more below,
 * scroll down" cue. Shared across lane step cards and any overflowing scroll region.
 * Place inside a Box, aligned to BottomCenter, gated by the caller's "can scroll" state.
 */
@Composable
fun ScrollFadeIndicator(
    modifier: Modifier = Modifier,
    background: Color = MiseTokens.colors.surface,
) {
    val infinite = rememberInfiniteTransition(label = "chevron-bounce")
    val translate by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(1800), RepeatMode.Reverse),
        label = "chevron-y",
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.6f to background.copy(alpha = 0.6f),
                    1.0f to background,
                ),
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 12.dp)
                .offset { IntOffset(0, translate.toInt()) }
                .size(26.dp)
                .background(MiseTokens.colors.ink.copy(alpha = 0.06f), RoundedCornerShape(999.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MiseTokens.colors.ink3,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
