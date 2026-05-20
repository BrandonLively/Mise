package com.patchfox.mise.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MiseColors(
    val bg: Color,
    val surface: Color,
    val surface2: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val lineStrong: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentWash: Color,
    val blue: Color,
    val green: Color,
    val purple: Color,
    val yellow: Color,
    val red: Color,
    val blueWash: Color,
    val greenWash: Color,
    val purpleWash: Color,
    val yellowWash: Color,
    val redWash: Color,
)

val LightMiseColors = MiseColors(
    bg = Color(0xFFF3ECE1),
    surface = Color(0xFFFAF5EC),
    surface2 = Color(0xFFEDE4D3),
    ink = Color(0xFF2A1F17),
    ink2 = Color(0xFF5A4838),
    ink3 = Color(0xFF8B7A68),
    line = Color(0x1A2A1F17),
    lineStrong = Color(0x2E2A1F17),
    accent = Color(0xFFB8533A),
    accentSoft = Color(0xFFE3A685),
    accentWash = Color(0xFFF5E3D6),
    blue = Color(0xFF4D6E8C),
    green = Color(0xFF6B8E5A),
    purple = Color(0xFF8A6DA3),
    yellow = Color(0xFFC69A2E),
    red = Color(0xFFB8533A),
    blueWash = Color(0xFFDBE6EF),
    greenWash = Color(0xFFE1EAD7),
    purpleWash = Color(0xFFE6DCEC),
    yellowWash = Color(0xFFF1E6C3),
    redWash = Color(0xFFF1D8C8),
)

val LocalMiseColors = staticCompositionLocalOf { LightMiseColors }

enum class RecipeColor { BLUE, GREEN, PURPLE, YELLOW, RED }

fun MiseColors.recipe(c: RecipeColor): Color = when (c) {
    RecipeColor.BLUE -> blue
    RecipeColor.GREEN -> green
    RecipeColor.PURPLE -> purple
    RecipeColor.YELLOW -> yellow
    RecipeColor.RED -> red
}

fun MiseColors.recipeWash(c: RecipeColor): Color = when (c) {
    RecipeColor.BLUE -> blueWash
    RecipeColor.GREEN -> greenWash
    RecipeColor.PURPLE -> purpleWash
    RecipeColor.YELLOW -> yellowWash
    RecipeColor.RED -> redWash
}
