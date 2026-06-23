package com.patchfox.mise.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patchfox.mise.domain.model.Macro
import com.patchfox.mise.ui.theme.MiseTokens

/** "Today's plate" — the combined per-serving macros across the plan's recipes. */
@Composable
fun DailyMacrosCard(macros: Macro, modifier: Modifier = Modifier) {
    MiseCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("TODAY'S PLATE", style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
            Spacer(Modifier.height(10.dp))
            Row {
                MacroCol("KCAL", macros.calories.toInt().toString())
                MacroCol("PROTEIN", "${macros.proteinGrams.toInt()}g")
                MacroCol("CARBS", "${macros.carbsGrams.toInt()}g")
                MacroCol("FAT", "${macros.fatGrams.toInt()}g")
            }
        }
    }
}

@Composable
private fun RowScope.MacroCol(label: String, value: String) {
    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MiseTokens.text.clockLarge, color = MiseTokens.colors.ink)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MiseTokens.text.micro, color = MiseTokens.colors.ink3)
    }
}
