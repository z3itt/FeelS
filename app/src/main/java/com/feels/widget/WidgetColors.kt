package com.feels.widget

import androidx.compose.ui.graphics.Color

internal data class WidgetPalette(
    val background: Color,
    val onBackground: Color,
    val onSurfaceVariant: Color,
    val emptyCell: Color,
    val accentSurface: Color,
    val accentOnSurface: Color,
)

internal fun widgetPalette(isDark: Boolean): WidgetPalette = if (isDark) {
    WidgetPalette(
        background = Color(0xFF121212),
        onBackground = Color(0xFFE8E8E4),
        onSurfaceVariant = Color(0xFF9E9E9A),
        emptyCell = Color(0xFF3A3A38),
        accentSurface = Color(0xFF7CB89A).copy(alpha = 0.35f),
        accentOnSurface = Color(0xFF9FD4B8),
    )
} else {
    WidgetPalette(
        background = Color(0xFFFAFAF8),
        onBackground = Color(0xFF2C2C2A),
        onSurfaceVariant = Color(0xFF6B6B68),
        emptyCell = Color(0xFFE8E8E4),
        accentSurface = Color(0xFF7CB89A).copy(alpha = 0.35f),
        accentOnSurface = Color(0xFF2C5E4A),
    )
}
