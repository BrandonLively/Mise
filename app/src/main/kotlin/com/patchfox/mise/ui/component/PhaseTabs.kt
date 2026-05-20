package com.patchfox.mise.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

enum class PhaseTab(val label: String, val subLabel: String) {
    Phase0("Phase 0", "Kickoff"),
    Phase1("Phase 1", "Mise"),
    Phase2("Phase 2", "Cook"),
}

@Composable
fun PhaseTabs(
    selected: PhaseTab,
    onSelect: (PhaseTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(MiseTokens.colors.surface2, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PhaseTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val bg by animateColorAsState(
                if (isSelected) MiseTokens.colors.surface else MiseTokens.colors.surface2,
                label = "tab-bg",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .then(
                        if (isSelected) {
                            Modifier.shadow(1.dp, RoundedCornerShape(8.dp), clip = false)
                        } else Modifier,
                    )
                    .background(bg, RoundedCornerShape(8.dp))
                    .clickable { onSelect(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        tab.label,
                        style = MiseTokens.text.bodyEmphasis,
                        color = if (isSelected) MiseTokens.colors.ink else MiseTokens.colors.ink3,
                    )
                    Text(
                        tab.subLabel,
                        style = MiseTokens.text.micro,
                        color = if (isSelected) MiseTokens.colors.ink2 else MiseTokens.colors.ink3,
                    )
                }
            }
        }
    }
}
