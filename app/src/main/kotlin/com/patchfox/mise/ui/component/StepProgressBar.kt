package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun StepProgressBar(
    steps: List<CookStep>,
    currentIndex: Int,
    onJumpTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (steps.isEmpty()) return
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .padding(horizontal = 2.dp),
    ) {
        steps.forEachIndexed { idx, step ->
            val past = idx < currentIndex
            val current = idx == currentIndex
            val color = when {
                current -> MiseTokens.colors.ink
                past -> step.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink2
                else -> MiseTokens.colors.surface2
            }
            val height = if (past || current) 5.dp else 3.dp
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 1.dp)
                    .clickable { onJumpTo(idx) },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height)
                        .background(color, RoundedCornerShape(2.dp)),
                )
            }
        }
    }
}
