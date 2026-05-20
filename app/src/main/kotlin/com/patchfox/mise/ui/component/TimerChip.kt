package com.patchfox.mise.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.RecipeColor
import com.patchfox.mise.ui.theme.recipe

private const val PULSE_THRESHOLD_SECONDS = 30

fun formatMmSs(totalSeconds: Int): String {
    val s = totalSeconds.coerceAtLeast(0)
    val mm = s / 60
    val ss = s % 60
    return "%d:%02d".format(mm, ss)
}

@Composable
fun TimerChip(
    title: String,
    remainingSeconds: Int,
    recipeEmoji: String?,
    recipeColor: RecipeColor?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val done = remainingSeconds <= 0
    val pulsing = !done && remainingSeconds < PULSE_THRESHOLD_SECONDS

    val pulseAlpha = if (pulsing) {
        val infinite = rememberInfiniteTransition(label = "timer-pulse")
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 0.4f,
            animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
            label = "alpha",
        ).value
    } else 1f

    val accent = recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.accent
    val bg = if (done) MiseTokens.colors.ink else MiseTokens.colors.surface
    val fg = if (done) MiseTokens.colors.accentSoft else MiseTokens.colors.ink

    val chipShape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .alpha(pulseAlpha)
            .widthIn(min = 118.dp)
            .height(46.dp)
            .clip(chipShape)
            .background(bg)
            .border(BorderStroke(3.dp, SolidColor(accent)), chipShape)
            .clickable { onDismiss() }
            .padding(start = 14.dp, end = 14.dp),
    ) {
        if (!recipeEmoji.isNullOrBlank()) {
            Text(recipeEmoji, style = MiseTokens.text.body)
            Box(Modifier.width(6.dp))
        }
        Column(verticalArrangement = Arrangement.Center) {
            Text(
                if (done) "done" else formatMmSs(remainingSeconds),
                style = MiseTokens.text.clock,
                color = fg,
            )
            Text(
                title,
                style = MiseTokens.text.micro,
                color = fg.copy(alpha = 0.7f),
                maxLines = 1,
            )
        }
    }
}

@Composable
fun PendingTimerChip(
    title: String,
    durationSeconds: Int,
    recipeColor: RecipeColor?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.accent
    val pendingShape = RoundedCornerShape(999.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .widthIn(min = 118.dp)
            .height(46.dp)
            .clip(pendingShape)
            .background(MiseTokens.colors.surface)
            .border(BorderStroke(1.5.dp, SolidColor(accent)), pendingShape)
            .clickable { onStart() }
            .padding(start = 10.dp, end = 14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(MiseTokens.colors.ink, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.PlayArrow,
                contentDescription = "Start timer: $title",
                tint = MiseTokens.colors.surface,
                modifier = Modifier.size(14.dp),
            )
        }
        Box(Modifier.width(8.dp))
        Column {
            Text(formatMmSs(durationSeconds), style = MiseTokens.text.clock, color = MiseTokens.colors.ink)
            Text(title, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3, maxLines = 1)
        }
    }
}

@Composable
fun PendingTimerCardLarge(
    title: String,
    durationSeconds: Int,
    recipeEmoji: String?,
    recipeColor: RecipeColor?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.accent
    MiseCard(
        modifier = modifier,
        radius = MiseTokens.shapes.card,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MiseTokens.colors.ink, CircleShape)
                    .border(2.dp, accent, CircleShape)
                    .clickable { onStart() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    contentDescription = "Start timer: $title",
                    tint = MiseTokens.colors.surface,
                )
            }
            Box(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(formatMmSs(durationSeconds), style = MiseTokens.text.clockLarge)
                    Box(Modifier.width(8.dp))
                    Text("TAP TO START", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!recipeEmoji.isNullOrBlank()) {
                        Text(recipeEmoji, style = MiseTokens.text.body)
                        Box(Modifier.width(6.dp))
                    }
                    Text(title, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink2)
                }
            }
        }
    }
}

@Composable
fun TimerCardLarge(
    title: String,
    remainingSeconds: Int,
    recipeEmoji: String?,
    recipeColor: RecipeColor?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val done = remainingSeconds <= 0
    val accent = recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.accent
    val pulsing = !done && remainingSeconds < PULSE_THRESHOLD_SECONDS
    val pulseAlpha = if (pulsing) {
        val infinite = rememberInfiniteTransition(label = "timer-card-pulse")
        infinite.animateFloat(
            initialValue = 1f,
            targetValue = 0.5f,
            animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
            label = "alpha",
        ).value
    } else 1f

    MiseCard(
        modifier = modifier.alpha(pulseAlpha),
        radius = MiseTokens.shapes.card,
        background = if (done) MiseTokens.colors.ink else MiseTokens.colors.surface,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(enabled = done) { onDismiss() }
                .padding(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .height(40.dp)
                    .background(accent, RoundedCornerShape(2.dp)),
            )
            Box(Modifier.width(12.dp))
            Column {
                Text(
                    if (done) "DONE — TAP TO DISMISS" else formatMmSs(remainingSeconds),
                    style = MiseTokens.text.clockLarge,
                    color = if (done) MiseTokens.colors.accentSoft else MiseTokens.colors.ink,
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!recipeEmoji.isNullOrBlank()) {
                        Text(recipeEmoji, style = MiseTokens.text.body)
                        Box(Modifier.width(6.dp))
                    }
                    Text(
                        title,
                        style = MiseTokens.text.bodyEmphasis,
                        color = if (done) MiseTokens.colors.surface else MiseTokens.colors.ink2,
                    )
                }
            }
        }
    }
}
