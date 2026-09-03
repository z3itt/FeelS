package com.feels.wheel.geometry

import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.model.WheelFocusLevel

/**
 * Builds a full-wheel layout for the current navigation depth.
 * Each drill-down redistributes children evenly across 360° so labels stay readable.
 */
object FocusedWheelGeometryBuilder {

    fun build(
        allSegments: List<WheelSegment>,
        focusLevel: WheelFocusLevel,
        focusedPrimaryId: String?,
        focusedSecondaryId: String?,
        anchorMidAngleDeg: Float? = null,
    ): List<WheelSegment> {
        val filtered = when (focusLevel) {
            WheelFocusLevel.PRIMARY -> allSegments.filter { it.emotion.tier == EmotionTier.PRIMARY }
            WheelFocusLevel.SECONDARY -> allSegments.filter {
                it.emotion.tier == EmotionTier.SECONDARY &&
                    it.emotion.parentId == focusedPrimaryId
            }
            WheelFocusLevel.TERTIARY -> allSegments.filter {
                it.emotion.tier == EmotionTier.TERTIARY &&
                    it.emotion.parentId == focusedSecondaryId
            }
        }.sortedBy { it.emotion.sortOrder }

        if (filtered.isEmpty()) return emptyList()

        val tier = when (focusLevel) {
            WheelFocusLevel.PRIMARY -> EmotionTier.PRIMARY
            WheelFocusLevel.SECONDARY -> EmotionTier.SECONDARY
            WheelFocusLevel.TERTIARY -> EmotionTier.TERTIARY
        }
        val (inner, outer) = WheelDisplayMapper.displayRadii(tier)
        val sweep = 360f / filtered.size
        val startBase = if (anchorMidAngleDeg != null) {
            anchorMidAngleDeg - (filtered.size * sweep) / 2f
        } else {
            WheelLayout.START_ANGLE_OFFSET
        }

        return filtered.mapIndexed { index, segment ->
            segment.copy(
                startAngleDeg = startBase + index * sweep,
                sweepAngleDeg = sweep,
                innerRadiusFraction = inner,
                outerRadiusFraction = outer,
            )
        }
    }
}
