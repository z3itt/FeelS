package com.feels.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.unit.ColorProvider

@Composable
internal fun WidgetGradientFill(
    gradientColorHexes: List<String>,
    displayColorHex: String?,
    hasCheckIn: Boolean = false,
    isDark: Boolean = false,
    modifier: GlanceModifier,
    cornerRadius: Dp,
    bitmapWidthPx: Int,
    bitmapHeightPx: Int,
    emptyColor: androidx.compose.ui.graphics.Color = widgetPalette(isDark).emptyCell,
) {
    val colors = resolveGradientHexes(gradientColorHexes, displayColorHex)
    val palette = widgetPalette(isDark)
    val density = LocalContext.current.resources.displayMetrics.density
    val cornerRadiusPx = cornerRadius.toPx(density).coerceAtLeast(1f)
    val strokeWidthPx = if (hasCheckIn && colors.isNotEmpty()) 2f * density else 0f
    val strokeColorArgb = if (hasCheckIn && colors.isNotEmpty()) {
        android.graphics.Color.argb(
            190,
            (palette.onBackground.red * 255).toInt(),
            (palette.onBackground.green * 255).toInt(),
            (palette.onBackground.blue * 255).toInt(),
        )
    } else {
        null
    }

    when {
        colors.isEmpty() -> {
            Box(
                modifier = modifier
                    .cornerRadius(cornerRadius)
                    .background(ColorProvider(emptyColor.copy(alpha = if (hasCheckIn) 1f else 0.25f))),
            ) {}
        }
        else -> {
            val bitmap = WidgetGradientBitmap.horizontalGradient(
                hexColors = colors,
                widthPx = bitmapWidthPx,
                heightPx = bitmapHeightPx,
                cornerRadiusPx = cornerRadiusPx,
                strokeColorArgb = strokeColorArgb,
                strokeWidthPx = strokeWidthPx,
            )
            Box(
                modifier = modifier
                    .cornerRadius(cornerRadius)
                    .background(
                        imageProvider = ImageProvider(bitmap),
                        contentScale = ContentScale.FillBounds,
                    ),
            ) {}
        }
    }
}
