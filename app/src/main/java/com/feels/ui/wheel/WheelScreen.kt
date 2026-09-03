package com.feels.ui.wheel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feels.R
import com.feels.core.domain.model.CheckInIntervention
import com.feels.core.domain.model.WheelFocusLevel
import com.feels.ui.components.BreathingExerciseShortcut
import com.feels.ui.components.CheckInBottomSheet
import com.feels.ui.components.GentleGroundingExercise
import com.feels.ui.components.PostCheckInActionCard
import com.feels.ui.components.WheelAccessibilityList
import com.feels.ui.components.WheelSearchEmptyState
import com.feels.ui.components.WheelSearchResults
import com.feels.ui.main.FeelSWheelTopBar
import com.feels.ui.util.performLightConfirmHaptic
import com.feels.wheel.WheelViewModel
import com.feels.wheel.canvas.EmotionWheelCanvas
import androidx.compose.foundation.Image

@Composable
fun WheelScreen(
    onClearHistoryClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    viewModel: WheelViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    val savedMessage = stringResource(R.string.check_in_saved)
    var showSavedToast by remember { mutableStateOf(false) }
    var lastHandledPulse by remember { mutableStateOf(0) }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            viewModel.consumePendingNavigation()
        }
    }

    LaunchedEffect(uiState.checkInSavedPulse) {
        if (uiState.checkInSavedPulse > lastHandledPulse) {
            lastHandledPulse = uiState.checkInSavedPulse
            view.performLightConfirmHaptic()
            showSavedToast = true
            kotlinx.coroutines.delay(900)
            showSavedToast = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        FeelSWheelTopBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChanged,
            onSearchClear = viewModel::onClearSearch,
            onClearHistoryClick = onClearHistoryClick,
            onPrivacyClick = onPrivacyClick,
            onTermsClick = onTermsClick,
            onAboutClick = onAboutClick,
            onSettingsClick = onSettingsClick,
            isDarkTheme = isDarkTheme,
            onDarkThemeChange = onDarkThemeChange,
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.loadError -> {
                    LoadErrorState(
                        onRetry = viewModel::retryLoad,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        if (uiState.isSearching) {
                            if (uiState.searchResults.isNotEmpty()) {
                                WheelSearchResults(
                                    results = uiState.searchResults,
                                    onResultClick = viewModel::onSearchResultSelected,
                                )
                            } else if (
                                uiState.searchQuery.trim().length >= 2 &&
                                !uiState.searchPending
                            ) {
                                WheelSearchEmptyState()
                            }
                        }

                        WheelHeader(
                            focusLevel = uiState.focusLevel,
                            breadcrumb = uiState.breadcrumb,
                            hint = uiState.hint,
                            isDarkTheme = isDarkTheme,
                            onBack = viewModel::onBack,
                            onBreathingClick = viewModel::onOpenBreathingExercise,
                        )

                        if (!uiState.isSearching) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .aspectRatio(1f)
                                    .heightIn(max = 320.dp)
                                    .align(Alignment.CenterHorizontally),
                                contentAlignment = Alignment.Center,
                            ) {
                                EmotionWheelCanvas(
                                    displaySegments = uiState.displaySegments,
                                    focusLevel = uiState.focusLevel,
                                    selectedEmotionId = uiState.selectedEmotionId,
                                    onSegmentTap = viewModel::onSegmentTapped,
                                    onRotationChanged = viewModel::onWheelRotationChanged,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                            WheelAccessibilityList(
                                segments = uiState.displaySegments,
                                onSegmentSelect = viewModel::onSegmentTapped,
                            )
                        }

                        if (
                            !uiState.isSearching &&
                            uiState.focusLevel == WheelFocusLevel.PRIMARY
                        ) {
                            Surface(
                                onClick = viewModel::onUnsureSelected,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 4.dp),
                                shape = RoundedCornerShape(percent = 32),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = stringResource(R.string.wheel_unsure_mixed),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                    Text(
                                        text = stringResource(R.string.wheel_unsure_slash),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    )
                                    Text(
                                        text = stringResource(R.string.wheel_unsure_unsure),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            WheelPostCheckInOverlay(
                visible = uiState.showPostCheckInAction,
                intervention = uiState.postCheckInIntervention,
                onStartGrounding = viewModel::onStartGroundingFromPostAction,
                onDismiss = viewModel::onDismissPostCheckInAction,
                modifier = Modifier.align(Alignment.BottomCenter),
            )

            CheckInSavedToast(
                visible = showSavedToast,
                message = savedMessage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp),
            )
        }
    }

    if (uiState.showCheckInSheet) {
        CheckInBottomSheet(
            emotionPath = uiState.selectedPath,
            intensity = uiState.checkInIntensity,
            note = uiState.checkInNote,
            intervention = uiState.checkInIntervention,
            onIntensityChange = viewModel::onIntensityChanged,
            onNoteChange = viewModel::onNoteChanged,
            onSave = viewModel::onSaveCheckIn,
            onStartGrounding = viewModel::onSaveAndStartGrounding,
            onDismiss = viewModel::onDismissCheckIn,
        )
    }

    if (uiState.showGroundingExercise) {
        GentleGroundingExercise(
            initialPatternId = uiState.activeBreathingPatternId,
            onPatternSelected = viewModel::onBreathingPatternSelected,
            onDone = viewModel::onDismissGroundingExercise,
        )
    }
}

@Composable
private fun LoadErrorState(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.error_load_failed),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.action_retry))
        }
    }
}

@Composable
private fun CheckInSavedToast(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(420)),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.inverseSurface,
            tonalElevation = 2.dp,
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.inverseOnSurface,
            )
        }
    }
}

@Composable
private fun WheelPostCheckInOverlay(
    visible: Boolean,
    intervention: CheckInIntervention?,
    onStartGrounding: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && intervention != null,
        modifier = modifier,
        enter = fadeIn(tween(280)),
        exit = fadeOut(tween(280)),
    ) {
        if (intervention != null) {
            PostCheckInActionCard(
                intervention = intervention,
                onStartGrounding = onStartGrounding,
                onDismiss = onDismiss,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun WheelHeader(
    focusLevel: WheelFocusLevel,
    breadcrumb: String,
    hint: String,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onBreathingClick: () -> Unit,
) {
    val textIndent = if (focusLevel != WheelFocusLevel.PRIMARY) 56.dp else 52.dp
    val wheelMarkRes = if (isDarkTheme) {
        R.drawable.ic_wheel_mark_dark
    } else {
        R.drawable.ic_wheel_mark_light
    }
    val wheelMarkShape = RoundedCornerShape(percent = 36)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        if (focusLevel != WheelFocusLevel.PRIMARY) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(wheelMarkShape)
                        .then(
                            if (isDarkTheme) {
                                Modifier
                            } else {
                                Modifier.background(Color.White)
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(wheelMarkRes),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "FeelS",
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            if (breadcrumb.isNotBlank()) {
                Text(
                    text = breadcrumb,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = textIndent, top = 2.dp),
                )
            }
            Text(
                text = hint,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = textIndent, top = 2.dp),
            )
        }

        BreathingExerciseShortcut(onClick = onBreathingClick)
    }
}
