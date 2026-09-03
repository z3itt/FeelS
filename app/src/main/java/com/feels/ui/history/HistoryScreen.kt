package com.feels.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.feels.R
import com.feels.core.domain.model.HeatmapDay
import com.feels.ui.components.HeatmapDayTile

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val trends by viewModel.primaryTrends.collectAsStateWithLifecycle()
    val heatmap by viewModel.heatmap.collectAsStateWithLifecycle()
    val triggerInsight by viewModel.triggerInsight.collectAsStateWithLifecycle()
    val loadError by viewModel.loadError.collectAsStateWithLifecycle()
    var editingItem by remember { mutableStateOf<HistoryItemUi?>(null) }
    var deletingItem by remember { mutableStateOf<HistoryItemUi?>(null) }

    if (loadError) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                Text(
                    text = stringResource(R.string.error_load_failed),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = viewModel::retryLoad) {
                    Text(stringResource(R.string.action_retry))
                }
            }
        }
        return
    }

    if (items.isEmpty() && trends.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                Text(
                    text = "No check-ins yet",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Your saved feelings will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "heatmap") {
            EmotionalHeatmapCard(days = heatmap)
        }

        triggerInsight?.let { insight ->
            item(key = "trigger_insight") {
                TriggerInsightCard(insight = insight)
            }
        }

        if (trends.isNotEmpty()) {
            item(key = "trends_header") {
                Text(
                    text = "Core feeling trends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(trends, key = { it.primaryId }) { trend ->
                PrimaryTrendRow(trend = trend)
            }
        }

        item(key = "history_header") {
            Text(
                text = "Recent check-ins",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )
        }

        items(items, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.emotionLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { editingItem = item },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.history_edit_check_in),
                            )
                        }
                        IconButton(
                            onClick = { deletingItem = item },
                            modifier = Modifier.size(48.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.history_delete_check_in),
                            )
                        }
                    }
                    if (item.emotionPath.isNotBlank()) {
                        Text(
                            text = item.emotionPath,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "Intensity ${item.intensity}/5 · ${item.timeLabel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    item.note?.let { note ->
                        Text(
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }

    editingItem?.let { item ->
        EditCheckInDialog(
            item = item,
            onDismiss = { editingItem = null },
            onSave = { intensity, note ->
                viewModel.updateCheckIn(item.id, intensity, note)
                editingItem = null
            },
        )
    }

    deletingItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deletingItem = null },
            title = { Text(stringResource(R.string.history_delete_title)) },
            text = { Text(stringResource(R.string.history_delete_message, item.emotionLabel)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCheckIn(item.id)
                    deletingItem = null
                }) {
                    Text(stringResource(R.string.history_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingItem = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmotionalHeatmapCard(days: List<HeatmapDay>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "30-day emotional heatmap",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Each square blends that day's feelings. Multiple entries mix as a gradient; intensity shifts the shade.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                maxItemsInEachRow = 10,
                modifier = Modifier.semantics {
                    contentDescription = "30 day emotional heatmap"
                },
            ) {
                days.forEach { day ->
                    HeatmapDayTile(
                        displayColorHex = day.displayColorHex,
                        gradientColorHexes = day.gradientColorHexes,
                        hasCheckIn = day.hasCheckIn,
                        entryCount = day.entryCount,
                        modifier = Modifier
                            .size(24.dp)
                            .semantics {
                                contentDescription = if (day.hasCheckIn) {
                                    if (day.entryCount > 1) {
                                        "${day.entryCount} check-ins blended"
                                    } else {
                                        "${day.summaryLabel.orEmpty()} check-in"
                                    }
                                } else {
                                    "No check-in"
                                }
                            },
                        emptyColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TriggerInsightCard(insight: TriggerInsightUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Most triggered sub-feeling",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "This week you logged ${insight.emotionLabel} (${insight.peakIntensity}/5) ${insight.occurrenceCount} times.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = insight.emotionPath,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (insight.topKeywords.isNotEmpty()) {
                Text(
                    text = "Common note words: ${insight.topKeywords.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun PrimaryTrendRow(trend: PrimaryTrendUi) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = trend.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${trend.count}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LinearProgressIndicator(
            progress = { trend.fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = parseTrendColor(trend.colorHex),
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        )
    }
}

@Composable
private fun EditCheckInDialog(
    item: HistoryItemUi,
    onDismiss: () -> Unit,
    onSave: (Int, String?) -> Unit,
) {
    var intensity by remember(item.id) { mutableIntStateOf(item.intensity) }
    var note by remember(item.id) { mutableStateOf(item.note.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.history_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = item.emotionLabel,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(R.string.history_edit_intensity, intensity),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = intensity.toFloat(),
                    onValueChange = { intensity = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                )
                TextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.check_in_note_hint)) },
                    minLines = 1,
                    maxLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(intensity, note) }) {
                Text(stringResource(R.string.history_edit_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

private fun parseTrendColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return Color(0xFF000000L or cleaned.toLong(16))
}
