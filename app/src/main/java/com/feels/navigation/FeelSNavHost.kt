package com.feels.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.feels.ui.main.MainScreen
import com.feels.ui.onboarding.DisclaimerScreen
import com.feels.ui.splash.AppSplashScreen
import kotlinx.coroutines.delay

private const val BRAND_SPLASH_MS = 900L

@Composable
fun FeelSNavHost(
    hasAcceptedDisclaimer: Boolean?,
    onAcceptDisclaimer: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    var showBrandSplash by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(hasAcceptedDisclaimer) {
        if (hasAcceptedDisclaimer == null) return@LaunchedEffect
        delay(BRAND_SPLASH_MS)
        showBrandSplash = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when {
            hasAcceptedDisclaimer == null || showBrandSplash -> {
                AppSplashScreen(isDarkTheme = isDarkTheme)
            }
            !hasAcceptedDisclaimer -> {
                DisclaimerScreen(onAccepted = onAcceptDisclaimer)
            }
            else -> {
                MainScreen(
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                )
            }
        }
    }
}
