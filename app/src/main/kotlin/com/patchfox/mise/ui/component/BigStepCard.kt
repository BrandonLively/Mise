package com.patchfox.mise.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.ui.theme.MiseTokens
import com.patchfox.mise.ui.theme.recipe
import com.patchfox.mise.ui.theme.recipeWash

@Composable
fun BigStepCard(
    step: CookStep,
    stepIndex: Int,
    totalSteps: Int,
    showHandsOff: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val showFade by androidx.compose.runtime.remember(scrollState) {
        androidx.compose.runtime.derivedStateOf {
            scrollState.maxValue > 0 && scrollState.value < scrollState.maxValue - 24
        }
    }

    MiseCard(
        modifier = modifier,
        radius = MiseTokens.shapes.cardLg,
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                RecipeColorBand(color = step.recipeColor, thickness = 4.dp)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(20.dp),
                ) {
                    // Header row: clock + step counter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(step.clockTime, style = MiseTokens.text.clockLarge, color = MiseTokens.colors.ink)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "STEP ${stepIndex + 1} / $totalSteps",
                            style = MiseTokens.text.micro,
                            color = MiseTokens.colors.ink3,
                        )
                    }
                    Spacer(Modifier.height(10.dp))

                    // Recipe label row
                    if (step.recipeName != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (!step.recipeEmoji.isNullOrBlank()) {
                                Text(step.recipeEmoji!!, style = MiseTokens.text.body)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(
                                step.recipeName!!.uppercase(),
                                style = MiseTokens.text.micro,
                                color = step.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink2,
                            )
                        }
                        Spacer(Modifier.height(14.dp))
                    }

                    // Task bullets
                    step.task.forEach { line ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("·", style = MiseTokens.text.h3, color = MiseTokens.colors.ink3)
                            Spacer(Modifier.width(10.dp))
                            Text(line, style = MiseTokens.text.h3, color = MiseTokens.colors.ink)
                        }
                    }

                    if (step.miseReferences.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        Text("FROM MISE", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            step.miseReferences.forEach { ref ->
                                val text = buildString {
                                    if (ref.bowlNumber != null) append("Bowl ${ref.bowlNumber}")
                                    else if (!ref.label.isNullOrBlank()) append(ref.label)
                                    if (!ref.volumeHint.isNullOrBlank()) {
                                        if (isNotEmpty()) append(" · ")
                                        append(ref.volumeHint)
                                    }
                                }
                                if (text.isNotBlank()) {
                                    val washColor = step.recipeColor?.let { MiseTokens.colors.recipeWash(it) } ?: MiseTokens.colors.surface2
                                    Box(
                                        modifier = Modifier
                                            .background(washColor, RoundedCornerShape(999.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                    ) {
                                        Text(
                                            text,
                                            style = MiseTokens.text.small,
                                            color = step.recipeColor?.let { MiseTokens.colors.recipe(it) } ?: MiseTokens.colors.ink2,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (step.equipmentUsed.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        Text("EQUIPMENT", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            step.equipmentUsed.forEach { eq ->
                                Box(
                                    modifier = Modifier
                                        .background(MiseTokens.colors.surface2, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                ) {
                                    Text(eq, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                                }
                            }
                        }
                    }

                    if (showHandsOff && step.handsOff.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        MiseCard(
                            modifier = Modifier.fillMaxWidth(),
                            radius = MiseTokens.shapes.cardSm,
                            background = MiseTokens.colors.surface2,
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("BACKGROUND OPS", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
                                Spacer(Modifier.height(6.dp))
                                step.handsOff.forEach { op ->
                                    Row {
                                        Text("·", style = MiseTokens.text.body, color = MiseTokens.colors.ink3)
                                        Spacer(Modifier.width(6.dp))
                                        Text(op, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }

            // Fade indicator overlay
            AnimatedVisibility(
                visible = showFade,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250)),
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                ScrollFadeIndicator()
            }
        }
    }
}

@Composable
private fun ScrollFadeIndicator(modifier: Modifier = Modifier) {
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
                    0.6f to MiseTokens.colors.surface.copy(alpha = 0.6f),
                    1.0f to MiseTokens.colors.surface,
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

