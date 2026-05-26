package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe

@Composable
fun StepThumbStrip(
    steps: List<CookStep>,
    currentIndex: Int,
    onJumpTo: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    LaunchedEffect(currentIndex) {
        if (currentIndex in steps.indices) {
            state.animateScrollToItem(currentIndex.coerceAtMost(steps.lastIndex))
        }
    }
    LazyRow(
        state = state,
        modifier = modifier.height(90.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(steps) { idx, step ->
            val isActive = idx == currentIndex
            val color = step.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink3
            val thumbShape = RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier
                    .width(86.dp)
                    .clip(thumbShape)
                    .background(MiseTokens.colors.surface)
                    .then(
                        if (isActive) Modifier.border(2.dp, color, thumbShape)
                        else Modifier.border(1.dp, MiseTokens.colors.line, thumbShape),
                    )
                    .clickable { onJumpTo(idx) }
                    .padding(8.dp),
            ) {
                Column {
                    Text(step.clockTime, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.height(2.dp))
                    Row {
                        if (!step.recipeEmoji.isNullOrBlank()) {
                            Text(step.recipeEmoji!!, style = MiseTokens.text.small)
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("#${step.stepNumber}", style = MiseTokens.text.micro, color = color)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        step.task.firstOrNull()?.prefix.orEmpty(),
                        style = MiseTokens.text.small.copy(fontSize = MiseTokens.text.small.fontSize),
                        color = MiseTokens.colors.ink2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
