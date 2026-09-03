package com.feels.core.domain.usecase

import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.model.Emotion
import com.feels.core.domain.model.EmotionInterventionPolicy
import com.feels.core.domain.model.EmotionPath
import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.repository.BackupRepository
import com.feels.core.domain.repository.BackupRestoreResult
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class GetPrimaryEmotionsUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(): List<Emotion> = emotionRepository.getPrimaryEmotions()
}

class GetChildEmotionsUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(parentId: String): List<Emotion> =
        emotionRepository.getChildren(parentId)
}

class BuildEmotionPathUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(emotionId: String): EmotionPath {
        val emotion = emotionRepository.getEmotionById(emotionId) ?: return EmotionPath()
        return when (emotion.tier) {
            com.feels.core.domain.model.EmotionTier.PRIMARY -> EmotionPath(primary = emotion)
            com.feels.core.domain.model.EmotionTier.SECONDARY -> {
                val primary = emotion.parentId?.let { emotionRepository.getEmotionById(it) }
                EmotionPath(primary = primary, secondary = emotion)
            }
            com.feels.core.domain.model.EmotionTier.TERTIARY -> {
                val secondary = emotion.parentId?.let { emotionRepository.getEmotionById(it) }
                val primary = secondary?.parentId?.let { emotionRepository.getEmotionById(it) }
                EmotionPath(primary = primary, secondary = secondary, tertiary = emotion)
            }
        }
    }
}

class LogCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(emotionId: String, intensity: Int, note: String?): Long {
        return checkInRepository.logCheckIn(
            CheckIn(
                emotionId = emotionId,
                intensity = intensity.coerceIn(1, 5),
                note = note?.trim()?.takeIf { it.isNotEmpty() },
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }
}

class ShouldSuggestGroundingUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(emotionId: String, intensity: Int = 3): Boolean {
        val emotion = emotionRepository.getEmotionById(emotionId) ?: return false
        var current = emotion
        while (current.parentId != null) {
            current = emotionRepository.getEmotionById(current.parentId) ?: break
        }
        if (EmotionInterventionPolicy.isHappyBranch(current.id)) return false
        return intensity >= 4 && emotion.distressLevel >= 2
    }
}

class SearchEmotionsUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(query: String): List<Emotion> {
        val normalized = query.trim().lowercase()
        if (normalized.length < 2) return emptyList()
        return emotionRepository.observeAllEmotions()
            .first()
            .filter { it.label.lowercase().contains(normalized) }
            .sortedWith(compareByDescending<Emotion> { it.tier.level }.thenBy { it.label })
            .take(8)
    }
}

class ClearCheckInHistoryUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke() {
        checkInRepository.clearAllCheckIns()
    }
}

class UpdateCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(id: Long, intensity: Int, note: String?) {
        val existing = checkInRepository.getCheckInById(id) ?: return
        checkInRepository.updateCheckIn(
            existing.copy(
                intensity = intensity.coerceIn(1, 5),
                note = note?.trim()?.takeIf { it.isNotEmpty() },
            ),
        )
    }
}

class DeleteCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
) {
    suspend operator fun invoke(id: Long) {
        checkInRepository.deleteCheckIn(id)
    }
}

class ExportCheckInsUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(): String = backupRepository.exportJson()
}

class ImportCheckInsUseCase @Inject constructor(
    private val backupRepository: BackupRepository,
) {
    suspend operator fun invoke(json: String): BackupRestoreResult = backupRepository.importJson(json)
}
