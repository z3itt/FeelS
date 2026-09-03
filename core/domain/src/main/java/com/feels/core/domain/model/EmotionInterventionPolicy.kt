package com.feels.core.domain.model

object EmotionInterventionPolicy {
    const val HAPPY_PRIMARY_ID = "happy"

    fun isHappyBranch(primaryId: String?): Boolean = primaryId == HAPPY_PRIMARY_ID

    fun resolvePrimaryId(emotionId: String, emotions: List<Emotion>): String? {
        val byId = emotions.associateBy { it.id }
        var current = byId[emotionId] ?: return null
        while (current.parentId != null) {
            current = byId[current.parentId] ?: break
        }
        return current.id
    }

    fun isHappyBranchEmotion(emotionId: String, emotions: List<Emotion>): Boolean {
        val primaryId = resolvePrimaryId(emotionId, emotions)
        return isHappyBranch(primaryId)
    }
}
