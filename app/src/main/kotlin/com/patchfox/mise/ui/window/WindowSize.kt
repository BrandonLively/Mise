package com.patchfox.mise.ui.window

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

enum class WindowSize { Compact, Medium, Expanded }

val WindowSizeClass.miseSize: WindowSize
    get() = when (widthSizeClass) {
        WindowWidthSizeClass.Compact -> WindowSize.Compact
        WindowWidthSizeClass.Medium -> WindowSize.Medium
        else -> WindowSize.Expanded
    }
