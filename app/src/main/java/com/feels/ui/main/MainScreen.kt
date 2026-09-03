package com.feels.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.feels.R
import com.feels.core.domain.navigation.MainNavigationRequest
import com.feels.ui.settings.SettingsDialog
import com.feels.ui.history.HistoryScreen
import com.feels.ui.wheel.WheelScreen

private sealed class MainTab(val route: String, val label: String, val icon: ImageVector) {
    data object Wheel : MainTab("wheel_tab", "Wheel", Icons.Default.PanoramaFishEye)
    data object History : MainTab("history_tab", "History", Icons.Default.History)
}

private val tabTransitionMillis = 280
private val floatingTabBarClearance = 88.dp

private fun tabIndex(route: String): Int = when (route) {
    MainTab.Wheel.route -> 0
    MainTab.History.route -> 1
    else -> 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    viewModel: MainViewModel = hiltViewModel(),
) {
    val tabs = listOf(MainTab.Wheel, MainTab.History)
    var selectedTabRoute by rememberSaveable { mutableStateOf(MainTab.Wheel.route) }
    val snackbarHostState = remember { SnackbarHostState() }
    var showClearHistoryDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showTermsDialog by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val historyClearedMessage = stringResource(R.string.clear_history_success)

    LaunchedEffect(viewModel) {
        viewModel.navigationRequests.collect { request ->
            selectedTabRoute = when (request) {
                is MainNavigationRequest.OpenWheel,
                MainNavigationRequest.OpenBreathing,
                -> MainTab.Wheel.route
                MainNavigationRequest.OpenHistory -> MainTab.History.route
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.historyCleared.collect {
            snackbarHostState.showSnackbar(historyClearedMessage)
        }
    }

    if (showClearHistoryDialog) {
        ClearHistoryConfirmDialog(
            onConfirm = {
                showClearHistoryDialog = false
                viewModel.clearHistory()
            },
            onDismiss = { showClearHistoryDialog = false },
        )
    }

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }

    if (showTermsDialog) {
        TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            AnimatedContent(
                targetState = selectedTabRoute,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = floatingTabBarClearance),
                transitionSpec = {
                    val forward = tabIndex(targetState) > tabIndex(initialState)
                    val easing = FastOutSlowInEasing
                    val enterSlide = slideInHorizontally(
                        animationSpec = tween(tabTransitionMillis, easing = easing),
                        initialOffsetX = { fullWidth ->
                            if (forward) fullWidth / 5 else -fullWidth / 5
                        },
                    )
                    val exitSlide = slideOutHorizontally(
                        animationSpec = tween(tabTransitionMillis, easing = easing),
                        targetOffsetX = { fullWidth ->
                            if (forward) -fullWidth / 5 else fullWidth / 5
                        },
                    )
                    (enterSlide + fadeIn(tween(tabTransitionMillis, easing = easing))) togetherWith
                        (exitSlide + fadeOut(tween(tabTransitionMillis, easing = easing)))
                },
                label = "main_tab_transition",
            ) { route ->
                Box(modifier = Modifier.fillMaxSize()) {
                    when (route) {
                        MainTab.Wheel.route -> {
                            WheelScreen(
                                onClearHistoryClick = { showClearHistoryDialog = true },
                                onPrivacyClick = { showPrivacyDialog = true },
                                onTermsClick = { showTermsDialog = true },
                                onAboutClick = { showAboutDialog = true },
                                onSettingsClick = { showSettingsDialog = true },
                                isDarkTheme = isDarkTheme,
                                onDarkThemeChange = onDarkThemeChange,
                            )
                        }
                        MainTab.History.route -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                FeelSMainTopBar(
                                    title = MainTab.History.label,
                                    onClearHistoryClick = { showClearHistoryDialog = true },
                                    onPrivacyClick = { showPrivacyDialog = true },
                                    onTermsClick = { showTermsDialog = true },
                                    onAboutClick = { showAboutDialog = true },
                                    onSettingsClick = { showSettingsDialog = true },
                                    isDarkTheme = isDarkTheme,
                                    onDarkThemeChange = onDarkThemeChange,
                                )
                                HistoryScreen(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            FloatingPillTabBar(
                tabs = tabs,
                selectedRoute = selectedTabRoute,
                onTabSelected = { selectedTabRoute = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            )
        }
    }
}

@Composable
private fun FloatingPillTabBar(
    tabs: List<MainTab>,
    selectedRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.heightIn(min = 52.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.34f),
        shadowElevation = 10.dp,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                FloatingPillTabItem(
                    tab = tab,
                    selected = selectedRoute == tab.route,
                    onClick = { onTabSelected(tab.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingPillTabItem(
    tab: MainTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            Color.Transparent
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = tab.icon,
                contentDescription = tab.label,
                tint = contentColor,
                modifier = Modifier.size(20.dp),
            )
            if (selected) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                )
            }
        }
    }
}
