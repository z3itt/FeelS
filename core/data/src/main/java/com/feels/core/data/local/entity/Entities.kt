package com.feels.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emotions")
data class EmotionEntity(
    @PrimaryKey val id: String,
    val label: String,
    val tier: Int,
    val parentId: String?,
    val colorHex: String,
    val distressLevel: Int,
    val sortOrder: Int,
)

@Entity(tableName = "check_ins")
data class CheckInEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emotionId: String,
    val intensity: Int,
    val note: String?,
    val timestampMillis: Long,
)
