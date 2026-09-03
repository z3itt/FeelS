package com.feels.core.domain.usecase

import com.feels.core.domain.model.BreathingPatternIds
import com.feels.core.domain.model.CheckInIntervention
import com.feels.core.domain.model.CheckInInterventionType
import com.feels.core.domain.model.EmotionInterventionPolicy
import com.feels.core.domain.model.EmotionPathBuilder
import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.model.HeatmapDay
import com.feels.core.domain.model.TriggerInsight
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import com.feels.core.domain.util.HeatmapColorBlender
import com.feels.core.domain.util.LocalDayClock
import com.feels.core.domain.util.NoteKeywordAnalyzer
import com.feels.core.domain.model.WeeklyMoodDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class EvaluateCheckInInterventionUseCase @Inject constructor(
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(emotionId: String, intensity: Int): CheckInIntervention {
        val emotion = emotionRepository.getEmotionById(emotionId) ?: return CheckInIntervention(
            type = CheckInInterventionType.NONE,
        )

        val primaryId = walkToPrimaryId(emotion.id)
        if (EmotionInterventionPolicy.isHappyBranch(primaryId)) {
            return CheckInIntervention(type = CheckInInterventionType.NONE)
        }

        val isHighDistress = intensity >= 4 &&
            emotion.tier == EmotionTier.TERTIARY &&
            emotion.distressLevel >= 1

        if (isHighDistress) {
            return CheckInIntervention(
                type = CheckInInterventionType.HIGH_DISTRESS_GROUNDING,
                suggestedBreathingPatternId = suggestedPatternForIntensity(intensity),
            )
        }

        if (intensity <= 3) {
            return CheckInIntervention(
                type = CheckInInterventionType.REFLECTIVE_PROMPT,
                reflectiveQuestion = "What is one small thing you can control right now?",
            )
        }

        return CheckInIntervention(type = CheckInInterventionType.NONE)
    }

    private suspend fun walkToPrimaryId(emotionId: String): String? {
        var current = emotionRepository.getEmotionById(emotionId) ?: return null
        while (current.parentId != null) {
            current = emotionRepository.getEmotionById(current.parentId) ?: break
        }
        return current.id
    }

    private fun suggestedPatternForIntensity(intensity: Int): String = when (intensity) {
        5 -> BreathingPatternIds.BOX
        4 -> BreathingPatternIds.RELAX
        else -> BreathingPatternIds.BOX
    }
}

class GetEmotionalHeatmapUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val emotionRepository: EmotionRepository,
) {
    operator fun invoke(days: Int = 30): Flow<List<HeatmapDay>> {
        val sinceMillis = LocalDayClock.startOfDayDaysAgo((days - 1).coerceAtLeast(0))
        return combine(
            checkInRepository.observeCheckInsSince(sinceMillis),
            emotionRepository.observeAllEmotions(),
        ) { checkIns, emotions ->
            buildHeatmap(checkIns, emotions, days)
        }
    }

    private fun buildHeatmap(
        checkIns: List<com.feels.core.domain.model.CheckIn>,
        emotions: List<com.feels.core.domain.model.Emotion>,
        days: Int,
    ): List<HeatmapDay> {
        val todayStart = LocalDayClock.startOfDay()
        val maxOffset = (days - 1).coerceAtLeast(0)

        val checkInsByDay = checkIns.groupBy { checkIn ->
            val dayOffset = LocalDayClock.calendarDaysBetween(
                LocalDayClock.startOfDay(checkIn.timestampMillis),
                todayStart,
            )
            dayOffset.coerceIn(0, maxOffset)
        }.filterKeys { it in 0..maxOffset }

        return (days - 1 downTo 0).map { dayIndex ->
            val dayCheckIns = checkInsByDay[dayIndex].orEmpty()
            if (dayCheckIns.isEmpty()) {
                HeatmapDay(
                    dayIndex = dayIndex,
                    hasCheckIn = false,
                    displayColorHex = null,
                    gradientColorHexes = emptyList(),
                    summaryLabel = null,
                    entryCount = 0,
                )
            } else {
                val samples = dayCheckIns.mapNotNull { checkIn ->
                    val path = EmotionPathBuilder.build(checkIn.emotionId, emotions)
                    val primary = path.primary ?: return@mapNotNull null
                    HeatmapColorBlender.ColorSample(
                        colorHex = primary.colorHex,
                        intensity = checkIn.intensity,
                    )
                }
                val dominant = dayCheckIns.maxWithOrNull(
                    compareBy<com.feels.core.domain.model.CheckIn> { it.intensity }
                        .thenBy { it.timestampMillis },
                )
                val dominantPath = EmotionPathBuilder.build(dominant!!.emotionId, emotions)
                val summaryLabel = if (dayCheckIns.size == 1) {
                    dominantPath.primary?.label
                } else {
                    "${dayCheckIns.size} check-ins"
                }
                HeatmapDay(
                    dayIndex = dayIndex,
                    hasCheckIn = true,
                    displayColorHex = HeatmapColorBlender.blend(samples),
                    gradientColorHexes = HeatmapColorBlender.gradientColors(samples),
                    summaryLabel = summaryLabel,
                    entryCount = dayCheckIns.size,
                )
            }
        }
    }
}

class GetTriggerInsightsUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val emotionRepository: EmotionRepository,
) {
    operator fun invoke(): Flow<TriggerInsight?> {
        val weekStart = LocalDayClock.startOfDayDaysAgo(6)
        return combine(
            checkInRepository.observeCheckInsSince(weekStart),
            emotionRepository.observeAllEmotions(),
        ) { checkIns, emotions ->
            buildInsight(checkIns, emotions)
        }
    }

    private fun buildInsight(
        checkIns: List<com.feels.core.domain.model.CheckIn>,
        emotions: List<com.feels.core.domain.model.Emotion>,
    ): TriggerInsight? {
        if (checkIns.isEmpty()) return null

        val grouped = checkIns.groupBy { it.emotionId }
        val topEntry = grouped.maxByOrNull { (_, entries) ->
            val highIntensityCount = entries.count { it.intensity >= 4 }
            highIntensityCount * 100 + entries.size
        } ?: return null

        val emotionId = topEntry.key
        val entries = topEntry.value
        val path = EmotionPathBuilder.build(emotionId, emotions)
        val deepest = path.deepest ?: return null
        val peakIntensity = entries.maxOf { it.intensity }
        val notes = entries.mapNotNull { it.note?.trim() }.filter { it.isNotEmpty() }

        return TriggerInsight(
            emotionLabel = deepest.label,
            emotionPath = path.breadcrumb,
            peakIntensity = peakIntensity,
            occurrenceCount = entries.size,
            topKeywords = NoteKeywordAnalyzer.topKeywords(notes),
        )
    }
}

class GetWeeklyMoodSummaryUseCase @Inject constructor(
    private val getEmotionalHeatmapUseCase: GetEmotionalHeatmapUseCase,
) {
    operator fun invoke(): Flow<List<WeeklyMoodDay>> =
        getEmotionalHeatmapUseCase(days = WEEK_LENGTH).map { days ->
            val heatmap = days.takeLast(WEEK_LENGTH)
            List(WEEK_LENGTH) { index ->
                val day = heatmap.getOrNull(index)
                WeeklyMoodDay(
                    dayLabel = weekdayLabel(index),
                    hasCheckIn = day?.hasCheckIn == true,
                    displayColorHex = day?.displayColorHex,
                    gradientColorHexes = day?.gradientColorHexes.orEmpty(),
                    summaryLabel = day?.summaryLabel,
                    entryCount = day?.entryCount ?: 0,
                )
            }
        }

    private fun weekdayLabel(index: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -(WEEK_LENGTH - 1 - index))
        return SimpleDateFormat("EEE", Locale.ENGLISH).format(calendar.time)
    }

    private companion object {
        const val WEEK_LENGTH = 7
    }
}

class QuickLogCheckInUseCase @Inject constructor(
    private val checkInRepository: CheckInRepository,
    private val emotionRepository: EmotionRepository,
) {
    suspend operator fun invoke(primaryEmotionId: String): Long {
        val emotion = emotionRepository.getEmotionById(primaryEmotionId) ?: return -1L
        return checkInRepository.logCheckIn(
            com.feels.core.domain.model.CheckIn(
                emotionId = emotion.id,
                intensity = 3,
                note = null,
                timestampMillis = System.currentTimeMillis(),
            ),
        )
    }
}
