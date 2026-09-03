package com.feels.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.feels.core.domain.model.EmotionTier
import com.feels.wheel.geometry.WheelSegment

@Composable
fun WheelAccessibilityList(
    segments: List<WheelSegment>,
    onSegmentSelect: (WheelSegment) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (segments.isEmpty()) return

    val orderedSegments = segments.sortedBy { it.emotion.sortOrder }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                collectionInfo = CollectionInfo(rowCount = 1, columnCount = orderedSegments.size)
            }
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Accessible list",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { contentDescription = "Accessible emotion list for screen readers" },
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(orderedSegments, key = { _, segment -> segment.emotion.id }) { index, segment ->
                FilterChip(
                    selected = false,
                    onClick = { onSegmentSelect(segment) },
                    label = { Text(segment.emotion.label) },
                    modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "${segment.emotion.label}. ${tierA11yLabel(segment.emotion.tier)}"
                        collectionItemInfo = CollectionItemInfo(
                            rowIndex = 0,
                            rowSpan = 1,
                            columnIndex = index,
                            columnSpan = 1,
                        )
                    },
                )
            }
        }
    }
}

private fun tierA11yLabel(tier: EmotionTier): String = when (tier) {
    EmotionTier.PRIMARY -> "Core feeling"
    EmotionTier.SECONDARY -> "Secondary feeling"
    EmotionTier.TERTIARY -> "Specific feeling"
}
