package com.feels.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.feels.core.data.local.dao.CheckInDao
import com.feels.core.data.local.dao.EmotionDao
import com.feels.core.data.local.entity.CheckInEntity
import com.feels.core.data.local.entity.EmotionEntity

@Database(
    entities = [EmotionEntity::class, CheckInEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class FeelSDatabase : RoomDatabase() {
    abstract fun emotionDao(): EmotionDao
    abstract fun checkInDao(): CheckInDao
}
