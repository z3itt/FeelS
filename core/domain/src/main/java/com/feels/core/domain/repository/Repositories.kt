package com.feels.core.domain.repository

import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.model.Emotion
import kotlinx.coroutines.flow.Flow

interface EmotionRepository {
    fun observeAllEmotions(): Flow<List<Emotion>>
    suspend fun getEmotionById(id: String): Emotion?
    suspend fun getChildren(parentId: String): List<Emotion>
    suspend fun getPrimaryEmotions(): List<Emotion>
}

interface CheckInRepository {
    fun observeRecentCheckIns(limit: Int = 20): Flow<List<CheckIn>>
    fun observeCheckInsSince(sinceMillis: Long): Flow<List<CheckIn>>
    suspend fun getCheckInById(id: Long): CheckIn?
    suspend fun getAllCheckIns(): List<CheckIn>
    suspend fun logCheckIn(checkIn: CheckIn): Long
    suspend fun updateCheckIn(checkIn: CheckIn)
    suspend fun deleteCheckIn(id: Long)
    suspend fun clearAllCheckIns()
}

interface UserPreferencesRepository {
    val hasAcceptedDisclaimer: Flow<Boolean>
    suspend fun setDisclaimerAccepted(accepted: Boolean)
    val isDarkThemeEnabled: Flow<Boolean>
    suspend fun setDarkThemeEnabled(enabled: Boolean)
    val remindersEnabled: Flow<Boolean>
    suspend fun setRemindersEnabled(enabled: Boolean)
    val morningReminderHour: Flow<Int>
    val morningReminderMinute: Flow<Int>
    val eveningReminderHour: Flow<Int>
    val eveningReminderMinute: Flow<Int>
    suspend fun setMorningReminderTime(hour: Int, minute: Int)
    suspend fun setEveningReminderTime(hour: Int, minute: Int)
}

data class BackupRestoreResult(
    val importedCount: Int,
    val skippedCount: Int,
)

interface BackupRepository {
    suspend fun exportJson(): String
    suspend fun importJson(json: String): BackupRestoreResult
}
