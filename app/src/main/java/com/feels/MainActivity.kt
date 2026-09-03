package com.feels

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feels.core.domain.navigation.PendingWheelNavigation
import com.feels.core.ui.theme.FeelSTheme
import com.feels.navigation.FeelSNavHost
import com.feels.notifications.FeelSIntents
import com.feels.ui.onboarding.OnboardingViewModel
import com.feels.ui.theme.ThemeStartupStore
import com.feels.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicBoolean

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var pendingWheelNavigation: PendingWheelNavigation

    private val keepSplashScreen = AtomicBoolean(true)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepSplashScreen.get() }
        super.onCreate(savedInstanceState)
        val initialDarkTheme = ThemeStartupStore.read(this)
        requestNotificationPermissionIfNeeded()
        handleDeepLink(intent)
        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            val darkThemePreference by themeViewModel.isDarkThemeEnabled
                .collectAsStateWithLifecycle(initialValue = initialDarkTheme)
            val hasAcceptedDisclaimer by onboardingViewModel.hasAcceptedDisclaimer
                .collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                keepSplashScreen.set(false)
            }

            FeelSTheme(darkTheme = darkThemePreference) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    FeelSNavHost(
                        hasAcceptedDisclaimer = hasAcceptedDisclaimer,
                        onAcceptDisclaimer = onboardingViewModel::acceptDisclaimer,
                        isDarkTheme = darkThemePreference,
                        onDarkThemeChange = themeViewModel::setDarkThemeEnabled,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        when (intent?.action) {
            FeelSIntents.ACTION_OPEN_PRIMARY -> {
                val primaryId = intent.getStringExtra(FeelSIntents.EXTRA_PRIMARY_EMOTION_ID)
                pendingWheelNavigation.requestOpenWheel(primaryId)
            }
            FeelSIntents.ACTION_OPEN_WHEEL -> {
                pendingWheelNavigation.requestOpenWheel()
            }
            FeelSIntents.ACTION_OPEN_HISTORY -> {
                pendingWheelNavigation.requestOpenHistory()
            }
            FeelSIntents.ACTION_OPEN_BREATHING -> {
                pendingWheelNavigation.requestOpenBreathing()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (ThemeStartupStore.hasPromptedNotificationPermission(this)) return
        ThemeStartupStore.markNotificationPermissionPrompted(this)
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
