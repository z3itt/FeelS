package com.feels.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.feels.R

private val LightSplashBackground = Color(0xFFFAFAF8)
private val DarkSplashBackground = Color(0xFF121212)
private val LightSplashPlate = Color.White
private val DarkSplashPlate = Color(0xFF1A1A1A)

@Composable
fun AppSplashScreen(isDarkTheme: Boolean) {
    val wordmark = if (isDarkTheme) {
        R.drawable.ic_splash_wordmark_dark
    } else {
        R.drawable.ic_splash_wordmark_light
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDarkTheme) DarkSplashBackground else LightSplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.46f)
                .clip(RoundedCornerShape(percent = 32))
                .background(if (isDarkTheme) DarkSplashPlate else LightSplashPlate)
                .padding(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(wordmark),
                contentDescription = "FeelS",
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
