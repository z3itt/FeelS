package com.feels.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val FeelSLightColorScheme = lightColorScheme(
    primary = FeelSPrimary,
    onPrimary = FeelSOnPrimary,
    primaryContainer = FeelSPrimaryContainer,
    onPrimaryContainer = FeelSOnPrimaryContainer,
    secondary = FeelSSecondary,
    onSecondary = FeelSOnPrimary,
    secondaryContainer = FeelSSecondaryContainer,
    onSecondaryContainer = FeelSOnSecondaryContainer,
    tertiary = FeelSTertiary,
    onTertiary = FeelSOnPrimary,
    tertiaryContainer = FeelSTertiaryContainer,
    onTertiaryContainer = FeelSOnTertiaryContainer,
    background = FeelSBackground,
    onBackground = FeelSOnBackground,
    surface = FeelSSurface,
    onSurface = FeelSOnBackground,
    onSurfaceVariant = FeelSOnSurfaceVariant,
    outline = FeelSOutline,
    surfaceVariant = Color(0xFFF3F2EE),
)

private val FeelSDarkColorScheme = darkColorScheme(
    primary = FeelSDarkPrimary,
    onPrimary = FeelSDarkOnPrimary,
    primaryContainer = FeelSDarkPrimaryContainer,
    onPrimaryContainer = FeelSDarkOnPrimaryContainer,
    secondary = FeelSDarkSecondary,
    onSecondary = FeelSDarkOnPrimary,
    secondaryContainer = FeelSDarkSecondaryContainer,
    onSecondaryContainer = FeelSDarkOnSecondaryContainer,
    tertiary = FeelSDarkTertiary,
    onTertiary = FeelSDarkOnPrimary,
    tertiaryContainer = FeelSDarkTertiaryContainer,
    onTertiaryContainer = FeelSDarkOnTertiaryContainer,
    background = FeelSDarkBackground,
    onBackground = FeelSDarkOnBackground,
    surface = FeelSDarkSurface,
    onSurface = FeelSDarkOnBackground,
    onSurfaceVariant = FeelSDarkOnSurfaceVariant,
    outline = FeelSDarkOutline,
    surfaceVariant = Color(0xFF2A2A28),
)

@Composable
fun FeelSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) FeelSDarkColorScheme else FeelSLightColorScheme,
        typography = FeelSTypography,
        content = content,
    )
}

private val FeelSTypography = androidx.compose.material3.Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)
