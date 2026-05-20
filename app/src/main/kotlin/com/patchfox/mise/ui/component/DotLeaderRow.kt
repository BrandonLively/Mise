package com.patchfox.mise.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.patchfox.mise.ui.theme.MiseTokens

@Composable
fun DotLeaderRow(
    name: String,
    quantity: String,
    modifier: Modifier = Modifier,
) {
    // The name column is weighted so a long ingredient name wraps instead of
    // overrunning into the quantity. A fixed buffer of 20% of the screen width
    // sits between the two so the wrapped text never touches the amount.
    val bufferWidth = (LocalConfiguration.current.screenWidthDp * 0.2f).dp
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            name,
            style = MiseTokens.text.small,
            color = MiseTokens.colors.ink,
            modifier = Modifier.weight(1f),
        )
        Canvas(
            modifier = Modifier
                .width(bufferWidth)
                .height(18.dp)
                .padding(horizontal = 6.dp),
        ) {
            val radius = 1.dp.toPx()
            val gap = 6.dp.toPx()
            val y = size.height - radius * 2
            var x = 0f
            while (x < size.width) {
                drawCircle(
                    color = androidx.compose.ui.graphics.Color(0xFF8B7A68),
                    radius = radius,
                    center = Offset(x, y),
                )
                x += gap
            }
        }
        Text(
            quantity,
            style = MiseTokens.text.clock.copy(fontSize = MiseTokens.text.small.fontSize),
            color = MiseTokens.colors.ink2,
        )
    }
}
