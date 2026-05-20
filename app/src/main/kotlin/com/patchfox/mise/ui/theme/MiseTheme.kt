package com.patchfox.mise.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val MiseM3Colors = lightColorScheme(
    primary = LightMiseColors.accent,
    onPrimary = LightMiseColors.surface,
    primaryContainer = LightMiseColors.accentWash,
    onPrimaryContainer = LightMiseColors.ink,
    secondary = LightMiseColors.ink2,
    onSecondary = LightMiseColors.surface,
    background = LightMiseColors.bg,
    onBackground = LightMiseColors.ink,
    surface = LightMiseColors.surface,
    onSurface = LightMiseColors.ink,
    surfaceVariant = LightMiseColors.surface2,
    onSurfaceVariant = LightMiseColors.ink2,
    outline = LightMiseColors.lineStrong,
    outlineVariant = LightMiseColors.line,
    error = LightMiseColors.red,
    onError = LightMiseColors.surface,
)

@Composable
fun MiseTheme(
    colors: MiseColors = LightMiseColors,
    textStyles: MiseTextStyles = DefaultMiseTextStyles,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMiseColors provides colors,
        LocalMiseTextStyles provides textStyles,
    ) {
        MaterialTheme(
            colorScheme = MiseM3Colors,
            typography = buildMaterial3Typography(textStyles),
            content = content,
        )
    }
}

object MiseTokens {
    val colors: MiseColors
        @Composable
        get() = LocalMiseColors.current

    val text: MiseTextStyles
        @Composable
        get() = LocalMiseTextStyles.current

    val spacing: MiseSpacing
        @Composable
        get() = LocalMiseSpacing.current

    val shapes: MiseShapes
        @Composable
        get() = LocalMiseShapes.current
}
