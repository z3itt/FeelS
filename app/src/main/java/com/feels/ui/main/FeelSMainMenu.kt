package com.feels.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.feels.R
import com.feels.ui.components.WheelSearchField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeelSMainTopBar(
    onClearHistoryClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            MainMenuButton(
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onClearHistoryClick = onClearHistoryClick,
                onPrivacyClick = onPrivacyClick,
                onTermsClick = onTermsClick,
                onAboutClick = onAboutClick,
                onSettingsClick = onSettingsClick,
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = onDarkThemeChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeelSWheelTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClear: () -> Unit,
    onClearHistoryClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WheelSearchField(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                onClear = onSearchClear,
                modifier = Modifier.weight(1f),
            )
            MainMenuButton(
                menuExpanded = menuExpanded,
                onMenuExpandedChange = { menuExpanded = it },
                onClearHistoryClick = onClearHistoryClick,
                onPrivacyClick = onPrivacyClick,
                onTermsClick = onTermsClick,
                onAboutClick = onAboutClick,
                onSettingsClick = onSettingsClick,
                isDarkTheme = isDarkTheme,
                onDarkThemeChange = onDarkThemeChange,
            )
        }
    }
}

@Composable
private fun MainMenuButton(
    menuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onClearHistoryClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
) {
    Box {
        IconButton(onClick = { onMenuExpandedChange(true) }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.menu_more_options),
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { onMenuExpandedChange(false) },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
        ) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .fillMaxWidth(),
            ) {
                ThemeModeToggle(
                    isDarkTheme = isDarkTheme,
                    onDarkThemeChange = onDarkThemeChange,
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_settings)) },
                onClick = {
                    onMenuExpandedChange(false)
                    onSettingsClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_clear_history)) },
                onClick = {
                    onMenuExpandedChange(false)
                    onClearHistoryClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_privacy_policy)) },
                onClick = {
                    onMenuExpandedChange(false)
                    onPrivacyClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_terms_of_service)) },
                onClick = {
                    onMenuExpandedChange(false)
                    onTermsClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.menu_about)) },
                onClick = {
                    onMenuExpandedChange(false)
                    onAboutClick()
                },
            )
        }
    }
}

@Composable
private fun ThemeModeToggle(
    isDarkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lightLabel = stringResource(R.string.menu_light_mode)
    val darkLabel = stringResource(R.string.menu_dark_mode)
    val trackShape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(trackShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
            .padding(3.dp),
    ) {
        ThemeModeToggleOption(
            selected = !isDarkTheme,
            icon = Icons.Outlined.LightMode,
            label = lightLabel,
            onClick = { onDarkThemeChange(false) },
            modifier = Modifier.weight(1f),
        )
        ThemeModeToggleOption(
            selected = isDarkTheme,
            icon = Icons.Outlined.DarkMode,
            label = darkLabel,
            onClick = { onDarkThemeChange(true) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ThemeModeToggleOption(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val optionShape = RoundedCornerShape(18.dp)
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .height(34.dp)
            .clip(optionShape)
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .semantics { contentDescription = label },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
fun ClearHistoryConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.clear_history_title)) },
        text = { Text(stringResource(R.string.clear_history_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.clear_history_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit,
) {
    LegalDocumentDialog(
        title = stringResource(R.string.privacy_policy_title),
        body = stringResource(R.string.privacy_policy_body),
        onDismiss = onDismiss,
    )
}

@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit,
) {
    LegalDocumentDialog(
        title = stringResource(R.string.terms_of_service_title),
        body = stringResource(R.string.terms_of_service_body),
        onDismiss = onDismiss,
    )
}

@Composable
private fun LegalDocumentDialog(
    title: String,
    body: String,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_got_it))
            }
        },
    )
}
