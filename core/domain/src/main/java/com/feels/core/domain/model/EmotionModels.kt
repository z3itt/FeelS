package com.feels.core.domain.model

enum class EmotionTier(val level: Int) {
    PRIMARY(1),
    SECONDARY(2),
    TERTIARY(3),
}

data class Emotion(
    val id: String,
    val label: String,
    val tier: EmotionTier,
    val parentId: String?,
    val colorHex: String,
    val distressLevel: Int,
    val sortOrder: Int,
)

data class EmotionPath(
    val primary: Emotion? = null,
    val secondary: Emotion? = null,
    val tertiary: Emotion? = null,
) {
    val breadcrumb: String
        get() = listOfNotNull(primary, secondary, tertiary)
            .joinToString(" → ") { it.label }

    val deepest: Emotion?
        get() = tertiary ?: secondary ?: primary
}

data class CheckIn(
    val id: Long = 0,
    val emotionId: String,
    val intensity: Int,
    val note: String?,
    val timestampMillis: Long,
)

enum class WheelFocusLevel {
    PRIMARY,
    SECONDARY,
    TERTIARY,
}
