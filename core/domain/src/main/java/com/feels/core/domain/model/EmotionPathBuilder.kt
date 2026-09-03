package com.feels.core.domain.model

object EmotionPathBuilder {
    fun build(emotionId: String, emotions: List<Emotion>): EmotionPath {
        val byId = emotions.associateBy { it.id }
        val emotion = byId[emotionId] ?: return EmotionPath()
        return when (emotion.tier) {
            EmotionTier.PRIMARY -> EmotionPath(primary = emotion)
            EmotionTier.SECONDARY -> {
                val primary = emotion.parentId?.let(byId::get)
                EmotionPath(primary = primary, secondary = emotion)
            }
            EmotionTier.TERTIARY -> {
                val secondary = emotion.parentId?.let(byId::get)
                val primary = secondary?.parentId?.let(byId::get)
                EmotionPath(primary = primary, secondary = secondary, tertiary = emotion)
            }
        }
    }
}
