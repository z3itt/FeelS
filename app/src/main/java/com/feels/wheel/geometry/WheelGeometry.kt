package com.feels.wheel.geometry

import com.feels.core.domain.model.Emotion
import com.feels.core.domain.model.EmotionTier
import kotlin.math.PI

data class WheelSegment(
    val emotion: Emotion,
    val startAngleDeg: Float,
    val sweepAngleDeg: Float,
    val innerRadiusFraction: Float,
    val outerRadiusFraction: Float,
    val rootPrimaryId: String,
)

object WheelLayout {
    const val PRIMARY_INNER = 0f
    const val PRIMARY_OUTER = 0.32f
    const val SECONDARY_INNER = 0.32f
    const val SECONDARY_OUTER = 0.62f
    const val TERTIARY_INNER = 0.62f
    const val TERTIARY_OUTER = 0.96f
    const val START_ANGLE_OFFSET = -90f
}

class WheelGeometryBuilder {

    fun build(allEmotions: List<Emotion>): List<WheelSegment> {
        if (allEmotions.isEmpty()) return emptyList()

        val byParent = allEmotions.groupBy { it.parentId }
        val primaries = allEmotions
            .filter { it.tier == EmotionTier.PRIMARY }
            .sortedBy { it.sortOrder }

        val primarySweep = 360f / primaries.size
        val segments = mutableListOf<WheelSegment>()

        primaries.forEachIndexed { index, primary ->
            val primaryStart = WheelLayout.START_ANGLE_OFFSET + index * primarySweep
            segments += segment(
                emotion = primary,
                start = primaryStart,
                sweep = primarySweep,
                inner = WheelLayout.PRIMARY_INNER,
                outer = WheelLayout.PRIMARY_OUTER,
                rootPrimaryId = primary.id,
            )

            val secondaries = byParent[primary.id].orEmpty().sortedBy { it.sortOrder }
            if (secondaries.isEmpty()) return@forEachIndexed

            val secondarySweep = primarySweep / secondaries.size
            secondaries.forEachIndexed { sIndex, secondary ->
                val secondaryStart = primaryStart + sIndex * secondarySweep
                segments += segment(
                    emotion = secondary,
                    start = secondaryStart,
                    sweep = secondarySweep,
                    inner = WheelLayout.SECONDARY_INNER,
                    outer = WheelLayout.SECONDARY_OUTER,
                    rootPrimaryId = primary.id,
                )

                val tertiaries = byParent[secondary.id].orEmpty().sortedBy { it.sortOrder }
                if (tertiaries.isEmpty()) return@forEachIndexed

                val tertiarySweep = secondarySweep / tertiaries.size
                tertiaries.forEachIndexed { tIndex, tertiary ->
                    segments += segment(
                        emotion = tertiary,
                        start = secondaryStart + tIndex * tertiarySweep,
                        sweep = tertiarySweep,
                        inner = WheelLayout.TERTIARY_INNER,
                        outer = WheelLayout.TERTIARY_OUTER,
                        rootPrimaryId = primary.id,
                    )
                }
            }
        }

        return segments
    }

    private fun segment(
        emotion: Emotion,
        start: Float,
        sweep: Float,
        inner: Float,
        outer: Float,
        rootPrimaryId: String,
    ) = WheelSegment(
        emotion = emotion,
        startAngleDeg = start,
        sweepAngleDeg = sweep,
        innerRadiusFraction = inner,
        outerRadiusFraction = outer,
        rootPrimaryId = rootPrimaryId,
    )
}

object PolarMath {
    fun normalizeAngle(angleDeg: Float): Float {
        var a = angleDeg % 360f
        if (a < 0f) a += 360f
        return a
    }

    fun angleFromPoint(centerX: Float, centerY: Float, x: Float, y: Float): Float {
        val dx = x - centerX
        val dy = y - centerY
        val radians = kotlin.math.atan2(dy.toDouble(), dx.toDouble())
        return normalizeAngle(Math.toDegrees(radians).toFloat())
    }

    fun distanceFromCenter(centerX: Float, centerY: Float, x: Float, y: Float): Float {
        val dx = x - centerX
        val dy = y - centerY
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }

    fun isAngleInRange(angle: Float, startDeg: Float, sweepDeg: Float): Boolean {
        val normalized = normalizeAngle(angle)
        val start = normalizeAngle(startDeg)
        val end = normalizeAngle(startDeg + sweepDeg)
        return if (start <= end) {
            normalized in start..end
        } else {
            normalized >= start || normalized <= end
        }
    }

    fun hitTest(
        segments: List<WheelSegment>,
        centerX: Float,
        centerY: Float,
        maxRadius: Float,
        touchX: Float,
        touchY: Float,
        visibleTier: EmotionTier? = null,
        focusedPrimaryId: String? = null,
    ): WheelSegment? {
        val distance = distanceFromCenter(centerX, centerY, touchX, touchY)
        val angle = angleFromPoint(centerX, centerY, touchX, touchY)
        val radiusFraction = distance / maxRadius

        return segments
            .asSequence()
            .filter { segment ->
                radiusFraction in segment.innerRadiusFraction..segment.outerRadiusFraction &&
                    isAngleInRange(angle, segment.startAngleDeg, segment.sweepAngleDeg)
            }
            .maxByOrNull { it.emotion.tier.level }
    }

    fun labelPosition(
        centerX: Float,
        centerY: Float,
        maxRadius: Float,
        segment: WheelSegment,
    ): Pair<Float, Float> {
        val midAngleRad = ((segment.startAngleDeg + segment.sweepAngleDeg / 2f) * PI / 180.0).toFloat()
        val midRadius = (segment.innerRadiusFraction + segment.outerRadiusFraction) / 2f * maxRadius
        val x = centerX + midRadius * kotlin.math.cos(midAngleRad)
        val y = centerY + midRadius * kotlin.math.sin(midAngleRad)
        return x to y
    }
}
