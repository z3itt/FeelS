package com.feels.core.domain.model

enum class CheckInInterventionType {
    HIGH_DISTRESS_GROUNDING,
    REFLECTIVE_PROMPT,
    NONE,
}

data class CheckInIntervention(
    val type: CheckInInterventionType,
    val reflectiveQuestion: String? = null,
    val suggestedBreathingPatternId: String = BreathingPatternIds.BOX,
)

object BreathingPatternIds {
    const val BOX = "box"
    const val RELAX = "relax"
    const val CALM = "calm"
    const val QUICK = "quick"
}

data class HeatmapDay(
    val dayIndex: Int,
    val hasCheckIn: Boolean,
    val displayColorHex: String?,
    val gradientColorHexes: List<String> = emptyList(),
    val summaryLabel: String?,
    val entryCount: Int = 0,
)

data class WeeklyMoodDay(
    val dayLabel: String,
    val hasCheckIn: Boolean,
    val displayColorHex: String?,
    /** Intensity-adjusted colors for horizontal gradient (one per check-in). */
    val gradientColorHexes: List<String> = emptyList(),
    val summaryLabel: String?,
    val entryCount: Int = 0,
)

data class TriggerInsight(
    val emotionLabel: String,
    val emotionPath: String,
    val peakIntensity: Int,
    val occurrenceCount: Int,
    val topKeywords: List<String>,
)
