package com.feels.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun HeatmapDayTile(
    displayColorHex: String?,
    gradientColorHexes: List<String>,
    hasCheckIn: Boolean,
    entryCount: Int = 0,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp),
    emptyColor: Color = MaterialTheme.colorScheme.outline,
) {
    val gradientColors = when {
        gradientColorHexes.size >= 2 -> gradientColorHexes.map(::parseHeatmapColor)
        displayColorHex != null -> listOf(parseHeatmapColor(displayColorHex))
        else -> emptyList()
    }
    val filled = hasCheckIn && gradientColors.isNotEmpty()
    val borderColor = if (filled) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(width = if (filled) 1.5.dp else 1.dp, color = borderColor, shape = shape)
            .background(
                if (gradientColors.isEmpty()) {
                    emptyColor.copy(alpha = if (filled) 1f else 0.25f)
                } else if (gradientColors.size == 1) {
                    gradientColors.first()
                } else {
                    Color.Transparent
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (gradientColors.size >= 2) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.horizontalGradient(gradientColors)),
            )
        }
        if (entryCount > 1) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)),
            )
        }
    }
}

fun parseHeatmapColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return Color(0xFF000000L or cleaned.toLong(16))
}
