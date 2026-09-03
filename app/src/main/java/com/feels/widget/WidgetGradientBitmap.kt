package com.feels.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Shader
import android.util.LruCache
import androidx.compose.ui.unit.Dp

internal object WidgetGradientBitmap {

    private const val CACHE_MAX_ENTRIES = 64
    private val cache = LruCache<String, Bitmap>(CACHE_MAX_ENTRIES)

    fun horizontalGradient(
        hexColors: List<String>,
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        strokeColorArgb: Int? = null,
        strokeWidthPx: Float = 0f,
    ): Bitmap {
        require(hexColors.isNotEmpty()) { "At least one color is required" }

        if (hexColors.size == 1) {
            return solid(
                hex = hexColors.first(),
                widthPx = widthPx,
                heightPx = heightPx,
                cornerRadiusPx = cornerRadiusPx,
                strokeColorArgb = strokeColorArgb,
                strokeWidthPx = strokeWidthPx,
            )
        }

        val key = buildString {
            append(hexColors.joinToString("|"))
            append('@')
            append(widthPx)
            append('x')
            append(heightPx)
            append("r")
            append(cornerRadiusPx)
            append("s")
            append(strokeColorArgb ?: 0)
            append("w")
            append(strokeWidthPx)
        }
        cache.get(key)?.let { return it }

        val colors = hexColors.map { parseAndroidColor(it) }.toIntArray()
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f,
                0f,
                widthPx.toFloat(),
                0f,
                colors,
                null,
                Shader.TileMode.CLAMP,
            )
        }
        canvas.drawRoundRect(
            0f,
            0f,
            widthPx.toFloat(),
            heightPx.toFloat(),
            cornerRadiusPx,
            cornerRadiusPx,
            paint,
        )
        drawStrokeIfNeeded(canvas, widthPx, heightPx, cornerRadiusPx, strokeColorArgb, strokeWidthPx)
        cache.put(key, bitmap)
        return bitmap
    }

    private fun solid(
        hex: String,
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        strokeColorArgb: Int? = null,
        strokeWidthPx: Float = 0f,
    ): Bitmap {
        val key = "solid:$hex@$widthPx x$heightPx r$cornerRadiusPx s${strokeColorArgb ?: 0} w$strokeWidthPx"
        cache.get(key)?.let { return it }

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = parseAndroidColor(hex)
        }
        canvas.drawRoundRect(
            0f,
            0f,
            widthPx.toFloat(),
            heightPx.toFloat(),
            cornerRadiusPx,
            cornerRadiusPx,
            paint,
        )
        drawStrokeIfNeeded(canvas, widthPx, heightPx, cornerRadiusPx, strokeColorArgb, strokeWidthPx)
        cache.put(key, bitmap)
        return bitmap
    }

    private fun drawStrokeIfNeeded(
        canvas: Canvas,
        widthPx: Int,
        heightPx: Int,
        cornerRadiusPx: Float,
        strokeColorArgb: Int?,
        strokeWidthPx: Float,
    ) {
        if (strokeColorArgb == null || strokeWidthPx <= 0f) return
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = strokeColorArgb
            style = Style.STROKE
            strokeWidth = strokeWidthPx
        }
        val inset = strokeWidthPx / 2f
        canvas.drawRoundRect(
            inset,
            inset,
            widthPx.toFloat() - inset,
            heightPx.toFloat() - inset,
            cornerRadiusPx,
            cornerRadiusPx,
            strokePaint,
        )
    }

    private fun parseAndroidColor(hex: String): Int {
        val normalized = if (hex.startsWith("#")) hex else "#$hex"
        return android.graphics.Color.parseColor(normalized)
    }
}

internal fun resolveGradientHexes(
    gradientColorHexes: List<String>,
    displayColorHex: String?,
): List<String> = when {
    gradientColorHexes.size >= 2 -> gradientColorHexes
    displayColorHex != null -> listOf(displayColorHex)
    else -> emptyList()
}

internal fun Dp.toPx(density: Float): Float = value * density
