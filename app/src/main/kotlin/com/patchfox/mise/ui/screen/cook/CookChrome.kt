package com.patchfox.mise.ui.screen.cook

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.state.CookStage
import com.patchfox.mise.ui.theme.MiseTokens

/** The Mise | Cook two-segment stage toggle that replaces the v2 3-phase tabs. */
@Composable
fun StageToggle(
    stage: CookStage,
    onSelect: (CookStage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MiseTokens.colors.surface2)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        StageSegment("Mise", stage == CookStage.MISE) { onSelect(CookStage.MISE) }
        StageSegment("Cook", stage == CookStage.COOK) { onSelect(CookStage.COOK) }
    }
}

@Composable
private fun StageSegment(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MiseTokens.colors.surface else Color.Transparent,
        label = "stage-seg-bg",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            label,
            style = MiseTokens.text.bodyEmphasis,
            color = if (selected) MiseTokens.colors.ink else MiseTokens.colors.ink3,
        )
    }
}

/** Per-lane progress dots: filled up to and including [currentIndex]. */
@Composable
fun ProgressDots(
    total: Int,
    currentIndex: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val filled = i <= currentIndex
            Box(
                modifier = Modifier
                    .size(if (i == currentIndex) 7.dp else 5.dp)
                    .clip(CircleShape)
                    .background(if (filled) color else MiseTokens.colors.line),
            )
        }
    }
}

/** Lane header: recipe identity + a bold status label + (cook only) progress dots. */
@Composable
fun LaneHeader(
    emoji: String?,
    title: String,
    accent: Color,
    statusLabel: String,
    total: Int = 0,
    currentIndex: Int = 0,
    showDots: Boolean = false,
    letter: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (letter != null) {
                Box(
                    modifier = Modifier.size(22.dp).clip(CircleShape).background(accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(letter, style = MiseTokens.text.small, color = MiseTokens.colors.surface)
                }
                Spacer(Modifier.width(8.dp))
            }
            if (!emoji.isNullOrBlank()) {
                Text(emoji, style = MiseTokens.text.body, color = MiseTokens.colors.ink)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                title,
                style = MiseTokens.text.h4,
                color = MiseTokens.colors.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(statusLabel, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink2)
        if (showDots && total > 0) {
            Spacer(Modifier.height(8.dp))
            ProgressDots(total = total, currentIndex = currentIndex, color = accent)
        }
    }
}
