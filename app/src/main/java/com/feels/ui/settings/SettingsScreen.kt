package com.feels.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.feels.R
import java.text.DateFormat
import java.util.Calendar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var notificationsAllowed by remember { mutableStateOf(areNotificationsAllowed(context)) }
    var timePicker by remember { mutableStateOf<TimePickerTarget?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        notificationsAllowed = areNotificationsAllowed(context)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                val json = viewModel.createExportJson()
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                }
            }.onSuccess {
                statusMessage = context.getString(R.string.settings_export_success)
            }.onFailure {
                statusMessage = context.getString(R.string.settings_export_failed)
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.readBytes().toString(Charsets.UTF_8)
            }
        }.onSuccess { json ->
            if (json.isNullOrBlank()) {
                statusMessage = context.getString(R.string.settings_import_failed)
            } else {
                viewModel.importBackup(json)
            }
        }.onFailure {
            statusMessage = context.getString(R.string.settings_import_failed)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = areNotificationsAllowed(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            statusMessage = message
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications_section),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = if (notificationsAllowed) {
                        stringResource(R.string.settings_notifications_on)
                    } else {
                        stringResource(R.string.settings_notifications_off)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!notificationsAllowed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(onClick = {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) {
                        Text(stringResource(R.string.settings_allow_notifications))
                    }
                }
                OutlinedButton(onClick = { openNotificationSettings(context) }) {
                    Text(stringResource(R.string.settings_open_system_settings))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.settings_reminders_toggle),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Switch(
                        checked = uiState.remindersEnabled,
                        onCheckedChange = viewModel::setRemindersEnabled,
                    )
                }
                TextButton(
                    onClick = { timePicker = TimePickerTarget.Morning },
                    enabled = uiState.remindersEnabled,
                ) {
                    Text(
                        stringResource(
                            R.string.settings_morning_time,
                            formatTime(uiState.morningHour, uiState.morningMinute),
                        ),
                    )
                }
                TextButton(
                    onClick = { timePicker = TimePickerTarget.Evening },
                    enabled = uiState.remindersEnabled,
                ) {
                    Text(
                        stringResource(
                            R.string.settings_evening_time,
                            formatTime(uiState.eveningHour, uiState.eveningMinute),
                        ),
                    )
                }

                Text(
                    text = stringResource(R.string.settings_backup_section),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = stringResource(R.string.settings_backup_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = { exportLauncher.launch("feels-backup.json") }) {
                    Text(stringResource(R.string.settings_export))
                }
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/plain")) }) {
                    Text(stringResource(R.string.settings_import))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_got_it))
            }
        },
    )

    timePicker?.let { target ->
        val initialHour = if (target == TimePickerTarget.Morning) uiState.morningHour else uiState.eveningHour
        val initialMinute = if (target == TimePickerTarget.Morning) uiState.morningMinute else uiState.eveningMinute
        val pickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { timePicker = null },
            title = {
                Text(
                    if (target == TimePickerTarget.Morning) {
                        stringResource(R.string.settings_morning_picker)
                    } else {
                        stringResource(R.string.settings_evening_picker)
                    },
                )
            },
            text = { TimePicker(state = pickerState) },
            confirmButton = {
                TextButton(onClick = {
                    if (target == TimePickerTarget.Morning) {
                        viewModel.setMorningTime(pickerState.hour, pickerState.minute)
                    } else {
                        viewModel.setEveningTime(pickerState.hour, pickerState.minute)
                    }
                    timePicker = null
                }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
            dismissButton = {
                TextButton(onClick = { timePicker = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    statusMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { statusMessage = null },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { statusMessage = null }) {
                    Text(stringResource(R.string.action_got_it))
                }
            },
        )
    }
}

private enum class TimePickerTarget { Morning, Evening }

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
    calendar.set(Calendar.MINUTE, minute.coerceIn(0, 59))
    return DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time)
}

private fun areNotificationsAllowed(context: android.content.Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
}

private fun openNotificationSettings(context: android.content.Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
