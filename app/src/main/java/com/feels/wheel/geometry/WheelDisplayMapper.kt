package com.feels.wheel.geometry

import com.feels.core.domain.model.EmotionTier

/**
 * Maps logical segment geometry to on-screen donut radii for the hybrid navigation model.
 * Each visible tier expands to fill most of the canvas instead of occupying a tiny inner slice.
 */
object WheelDisplayMapper {

    fun displayRadii(tier: EmotionTier): Pair<Float, Float> = when (tier) {
        EmotionTier.PRIMARY -> 0.14f to 0.98f
        EmotionTier.SECONDARY -> 0.11f to 0.98f
        EmotionTier.TERTIARY -> 0.08f to 0.98f
    }

    fun forDisplay(segment: WheelSegment, visibleTier: EmotionTier): WheelSegment {
        if (segment.emotion.tier != visibleTier) return segment
        val (inner, outer) = displayRadii(visibleTier)
        return segment.copy(
            innerRadiusFraction = inner,
            outerRadiusFraction = outer,
        )
    }

    fun mapVisible(
        segments: List<WheelSegment>,
        visibleTier: EmotionTier,
        focusedPrimaryId: String?,
        dimUnfocused: Boolean,
    ): List<WheelSegment> = segments
        .asSequence()
        .filter { it.emotion.tier == visibleTier }
        .filter { !dimUnfocused || focusedPrimaryId == null || it.rootPrimaryId == focusedPrimaryId }
        .map { forDisplay(it, visibleTier) }
        .toList()
}
