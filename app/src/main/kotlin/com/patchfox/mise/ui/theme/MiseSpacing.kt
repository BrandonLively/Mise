package com.patchfox.mise.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MiseSpacing(
    val xs4: Dp = 4.dp,
    val xs6: Dp = 6.dp,
    val s8: Dp = 8.dp,
    val s10: Dp = 10.dp,
    val s12: Dp = 12.dp,
    val s14: Dp = 14.dp,
    val m16: Dp = 16.dp,
    val m18: Dp = 18.dp,
    val m22: Dp = 22.dp,
    val l28: Dp = 28.dp,
    val l32: Dp = 32.dp,
    val l36: Dp = 36.dp,
    val xl44: Dp = 44.dp,
)

@Immutable
data class MiseShapes(
    val chip: Dp = 999.dp,
    val input: Dp = 12.dp,
    val cardSm: Dp = 14.dp,
    val card: Dp = 18.dp,
    val cardLg: Dp = 22.dp,
)

val LocalMiseSpacing = staticCompositionLocalOf { MiseSpacing() }
val LocalMiseShapes = staticCompositionLocalOf { MiseShapes() }
