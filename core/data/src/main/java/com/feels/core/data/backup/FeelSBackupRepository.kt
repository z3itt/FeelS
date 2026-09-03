package com.feels.core.data.backup

import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.repository.BackupRepository
import com.feels.core.domain.repository.BackupRestoreResult
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

@Singleton
class FeelSBackupRepository @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val emotionRepository: EmotionRepository,
) : BackupRepository {

    override suspend fun exportJson(): String {
        val checkIns = checkInRepository.getAllCheckIns().map { checkIn ->
            FeelSBackupCheckIn(
                emotionId = checkIn.emotionId,
                intensity = checkIn.intensity,
                note = checkIn.note,
                timestampMillis = checkIn.timestampMillis,
            )
        }
        return FeelSBackupJson.encode(
            FeelSBackupFile(
                exportedAtMillis = System.currentTimeMillis(),
                checkIns = checkIns,
            ),
        )
    }

    override suspend fun importJson(json: String): BackupRestoreResult {
        val file = FeelSBackupJson.decode(json)
        val knownIds = emotionRepository.observeAllEmotions().first().map { it.id }.toSet()
        var imported = 0
        var skipped = 0
        file.checkIns.forEach { entry ->
            if (entry.emotionId !in knownIds) {
                skipped++
                return@forEach
            }
            checkInRepository.logCheckIn(
                CheckIn(
                    emotionId = entry.emotionId,
                    intensity = entry.intensity.coerceIn(1, 5),
                    note = entry.note?.trim()?.takeIf { it.isNotEmpty() },
                    timestampMillis = entry.timestampMillis,
                ),
            )
            imported++
        }
        return BackupRestoreResult(importedCount = imported, skippedCount = skipped)
    }
}
