package com.patchfox.mise.ui.screen.cook

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.ui.component.InstructionStepBlock
import com.patchfox.mise.ui.component.MiseCard
import com.patchfox.mise.ui.component.formatMmSs
import com.patchfox.mise.ui.theme.MiseTokens

/**
 * One cook step, clock-less (the lane header carries the step counter). Prose lines
 * as serif bullets, the structured ingredients it acts on, a "Meanwhile" block for
 * concurrent hands-off ops, and the in-card Start-timer affordance.
 */
@Composable
fun StepCard(
    step: CookStep,
    started: Boolean,
    onStartTimer: () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(16.dp),
    ) {
        InstructionStepBlock(lines = step.steps, style = MiseTokens.text.h4, stepSpacing = 12)

        if (step.ingredients.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            step.ingredients.forEach { IngredientRow(it) }
        }

        if (step.handsOff.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            MeanwhileBlock(step.handsOff)
        }

        step.timer?.let { timer ->
            Spacer(Modifier.height(16.dp))
            if (!started) {
                StartTimerButton(label = timer.title, seconds = timer.durationSeconds, onClick = onStartTimer)
            } else {
                Text(
                    "${timer.title} running · see top bar",
                    style = MiseTokens.text.small,
                    color = MiseTokens.colors.ink3,
                )
            }
        }
    }
}

@Composable
private fun MeanwhileBlock(ops: List<String>) {
    MiseCard(
        modifier = Modifier.fillMaxWidth(),
        radius = MiseTokens.shapes.cardSm,
        background = MiseTokens.colors.surface2,
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("MEANWHILE", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(6.dp))
            ops.forEach { op ->
                Row {
                    Text("·", style = MiseTokens.text.small, color = MiseTokens.colors.ink3)
                    Spacer(Modifier.width(6.dp))
                    Text(op, style = MiseTokens.text.small, color = MiseTokens.colors.ink2)
                }
            }
        }
    }
}

@Composable
private fun StartTimerButton(label: String, seconds: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MiseTokens.colors.ink)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = MiseTokens.colors.surface, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Start timer · $label · ${formatMmSs(seconds)}",
            style = MiseTokens.text.bodyEmphasis,
            color = MiseTokens.colors.surface,
        )
    }
}
