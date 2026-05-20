package com.patchfox.mise.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

data class HeroCookDayStats(
    val activeMinutes: Int,
    val stepCount: Int,
    val recipeCount: Int,
    val bowlCount: Int,
)

@Composable
fun HeroCookDayCard(
    eyebrow: String,
    dayLabel: String,
    italicTail: String?,
    stats: HeroCookDayStats,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    ctaLabel: String = "Walk the plan →",
) {
    MiseCard(
        modifier = modifier.fillMaxWidth(),
        radius = MiseTokens.shapes.cardLg,
        background = MiseTokens.colors.ink,
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Text(eyebrow, style = MiseTokens.text.micro, color = MiseTokens.colors.accentSoft)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(dayLabel, style = MiseTokens.text.h1, color = MiseTokens.colors.surface)
                if (!italicTail.isNullOrBlank()) {
                    Spacer(Modifier.padding(start = 6.dp))
                    Text(
                        italicTail,
                        style = MiseTokens.text.h1.copy(fontStyle = FontStyle.Italic),
                        color = MiseTokens.colors.accentSoft,
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Stat("ACTIVE", "${stats.activeMinutes / 60}h ${stats.activeMinutes % 60}m")
                Stat("STEPS", "${stats.stepCount}")
                Stat("RECIPES", "${stats.recipeCount}")
                Stat("BOWLS", "${stats.bowlCount}")
            }
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MiseTokens.colors.surface, RoundedCornerShape(999.dp))
                    .clickable { onCta() }
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(ctaLabel, style = MiseTokens.text.bodyEmphasis, color = MiseTokens.colors.ink)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(value, style = MiseTokens.text.clockLarge, color = MiseTokens.colors.surface)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
    }
}
