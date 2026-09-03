package com.feels.core.data.mapper

import com.feels.core.data.local.entity.CheckInEntity
import com.feels.core.data.local.entity.EmotionEntity
import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.model.Emotion
import com.feels.core.domain.model.EmotionTier

fun EmotionEntity.toDomain(): Emotion = Emotion(
    id = id,
    label = label,
    tier = EmotionTier.entries.first { it.level == tier },
    parentId = parentId,
    colorHex = colorHex,
    distressLevel = distressLevel,
    sortOrder = sortOrder,
)

fun CheckInEntity.toDomain(): CheckIn = CheckIn(
    id = id,
    emotionId = emotionId,
    intensity = intensity,
    note = note,
    timestampMillis = timestampMillis,
)

fun CheckIn.toEntity(): CheckInEntity = CheckInEntity(
    id = id,
    emotionId = emotionId,
    intensity = intensity,
    note = note,
    timestampMillis = timestampMillis,
)
