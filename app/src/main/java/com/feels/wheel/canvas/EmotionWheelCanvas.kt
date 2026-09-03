package com.feels.wheel.canvas

import android.graphics.Paint
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.model.WheelFocusLevel
import com.feels.wheel.geometry.PolarMath
import com.feels.wheel.geometry.WheelDisplayMapper
import com.feels.wheel.geometry.WheelSegment
import com.feels.wheel.gesture.WheelTransformStateHolder
import com.feels.wheel.gesture.mapTouchInverseRotation
import com.feels.wheel.gesture.rememberWheelRotationState
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

private const val MIN_SCALE = 0.85f
private const val MAX_SCALE = 3.5f
private const val PRIMARY_ROTATION_MS = 90_000
private const val DRILL_MORPH_MS = 520

@Composable
fun EmotionWheelCanvas(
    displaySegments: List<WheelSegment>,
    focusLevel: WheelFocusLevel,
    selectedEmotionId: String?,
    onSegmentTap: (WheelSegment) -> Unit,
    onRotationChanged: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val fontScale = LocalConfiguration.current.fontScale.coerceIn(0.85f, 1.5f)
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val wheelBackground = MaterialTheme.colorScheme.surfaceVariant
    val hubColor = MaterialTheme.colorScheme.background
    val borderColor = MaterialTheme.colorScheme.onBackground
    val labelColor = MaterialTheme.colorScheme.onBackground
    val transform = remember { WheelTransformStateHolder() }
    val rotationState = rememberWheelRotationState()
    val morphProgress = remember { Animatable(1f) }
    val isPrimary = focusLevel == WheelFocusLevel.PRIMARY
    val currentRotation = rotationState.rotation.value

    LaunchedEffect(currentRotation) {
        onRotationChanged(currentRotation)
    }

    val visibleTier = when (focusLevel) {
        WheelFocusLevel.PRIMARY -> EmotionTier.PRIMARY
        WheelFocusLevel.SECONDARY -> EmotionTier.SECONDARY
        WheelFocusLevel.TERTIARY -> EmotionTier.TERTIARY
    }

    LaunchedEffect(focusLevel, displaySegments.map { it.emotion.id }) {
        morphProgress.snapTo(0f)
        morphProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(DRILL_MORPH_MS, easing = FastOutSlowInEasing),
        )
    }

    LaunchedEffect(isPrimary) {
        if (isPrimary) {
            rotationState.snapToFrozen()
            while (isActive) {
                val target = rotationState.rotation.value + 360f
                rotationState.rotation.animateTo(
                    targetValue = target,
                    animationSpec = tween(durationMillis = PRIMARY_ROTATION_MS, easing = LinearEasing),
                )
            }
        } else {
            rotationState.freezeAtCurrent()
        }
    }

    val wheelScale = 0.88f + 0.12f * morphProgress.value

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = buildWheelDescription(visibleTier, displaySegments.size)
            }
            .graphicsLayer {
                scaleX = transform.scale.value
                scaleY = transform.scale.value
                translationX = transform.offsetX.value
                translationY = transform.offsetY.value
            }
            .pointerInput(displaySegments, focusLevel) {
                coroutineScope {
                    launch {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scope.launch {
                                val newScale = (transform.scale.value * zoom)
                                    .coerceIn(MIN_SCALE, MAX_SCALE)
                                transform.scale.snapTo(newScale)
                                transform.offsetX.snapTo(transform.offsetX.value + pan.x)
                                transform.offsetY.snapTo(transform.offsetY.value + pan.y)
                            }
                        }
                    }
                    detectTapGestures(
                        onDoubleTap = { scope.launch { transform.resetTransform() } },
                        onTap = { offset ->
                            val layout = wheelLayout(size.width.toFloat(), size.height.toFloat())
                            val scaled = transform.mapTouchToCanvas(offset, layout.center)
                            val rotationDeg = rotationState.rotation.value
                            val wheelScale = 0.88f + 0.12f * morphProgress.value
                            val unrotated = mapTouchInverseRotation(scaled, layout.center, rotationDeg)
                            val hit = PolarMath.hitTest(
                                segments = displaySegments,
                                centerX = layout.center.x,
                                centerY = layout.center.y,
                                maxRadius = layout.maxRadius * wheelScale,
                                touchX = unrotated.x,
                                touchY = unrotated.y,
                            )
                            hit?.let {
                                if (it.emotion.tier == EmotionTier.PRIMARY) {
                                    scope.launch { rotationState.freezeAtCurrent() }
                                }
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSegmentTap(it)
                            }
                        },
                    )
                }
            },
    ) {
        val layout = wheelLayout(size.width, size.height)
        val (defaultInner, defaultOuter) = WheelDisplayMapper.displayRadii(visibleTier)
        val innerRadius = defaultInner * layout.maxRadius
        val outerRadius = defaultOuter * layout.maxRadius
        val rotationDeg = currentRotation
        val morph = morphProgress.value

        rotate(rotationDeg, layout.center) {
            drawCircle(
                color = wheelBackground,
                radius = (outerRadius + 6f) * wheelScale,
                center = layout.center,
                alpha = morph,
            )
            drawCircle(
                color = hubColor,
                radius = (innerRadius - 2f) * wheelScale,
                center = layout.center,
                alpha = morph,
            )
            drawCircle(
                color = borderColor.copy(alpha = 0.08f),
                radius = outerRadius * wheelScale,
                center = layout.center,
                style = Stroke(width = 2f),
                alpha = morph,
            )

            displaySegments.forEach { segment ->
                val isSelected = segment.emotion.id == selectedEmotionId
                val baseColor = parseHexColor(segment.emotion.colorHex)
                val fillColor = if (isSelected) {
                    baseColor.copy(alpha = morph)
                } else {
                    baseColor.copy(alpha = 0.92f * morph)
                }

                val segInner = segment.innerRadiusFraction * layout.maxRadius * wheelScale
                val segOuter = segment.outerRadiusFraction * layout.maxRadius * wheelScale

                drawRingSegment(
                    center = layout.center,
                    innerRadius = segInner,
                    outerRadius = segOuter,
                    startAngle = segment.startAngleDeg,
                    sweepAngle = segment.sweepAngleDeg,
                    fillColor = fillColor,
                    borderColor = if (isSelected) {
                        borderColor.copy(alpha = 0.55f * morph)
                    } else {
                        borderColor.copy(alpha = 0.22f * morph)
                    },
                    borderWidth = if (isSelected) 2.5f else 1.2f,
                )

                drawSegmentLabel(
                    center = layout.center,
                    maxRadius = layout.maxRadius * wheelScale,
                    segment = segment,
                    density = density.density,
                    fontScale = fontScale,
                    wheelRotationDeg = rotationDeg,
                    alpha = morph,
                    labelColor = labelColor,
                )
            }
        }
    }
}

private data class WheelLayoutMetrics(
    val center: Offset,
    val maxRadius: Float,
)

private fun wheelLayout(width: Float, height: Float): WheelLayoutMetrics {
    val sizeMin = min(width, height)
    val maxRadius = sizeMin * 0.48f
    return WheelLayoutMetrics(
        center = Offset(width / 2f, height / 2f),
        maxRadius = maxRadius,
    )
}

private fun DrawScope.drawRingSegment(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    sweepAngle: Float,
    fillColor: Color,
    borderColor: Color,
    borderWidth: Float,
) {
    if (sweepAngle <= 0f || outerRadius <= innerRadius) return

    val path = Path().apply {
        addDonutSegment(
            centerX = center.x,
            centerY = center.y,
            innerRadius = innerRadius,
            outerRadius = outerRadius,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
        )
    }
    drawPath(path = path, color = fillColor)
    drawPath(path = path, color = borderColor, style = Stroke(width = borderWidth))
}

private fun Path.addDonutSegment(
    centerX: Float,
    centerY: Float,
    innerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    sweepAngle: Float,
) {
    val startRad = Math.toRadians(startAngle.toDouble())
    val startOuterX = centerX + outerRadius * cos(startRad).toFloat()
    val startOuterY = centerY + outerRadius * sin(startRad).toFloat()

    moveTo(startOuterX, startOuterY)
    arcTo(
        rect = Rect(
            centerX - outerRadius,
            centerY - outerRadius,
            centerX + outerRadius,
            centerY + outerRadius,
        ),
        startAngleDegrees = startAngle,
        sweepAngleDegrees = sweepAngle,
        forceMoveTo = false,
    )
    arcTo(
        rect = Rect(
            centerX - innerRadius,
            centerY - innerRadius,
            centerX + innerRadius,
            centerY + innerRadius,
        ),
        startAngleDegrees = startAngle + sweepAngle,
        sweepAngleDegrees = -sweepAngle,
        forceMoveTo = false,
    )
    close()
}

private const val UNSURE_EMOTION_ID = "unsure"

private fun DrawScope.drawSegmentLabel(
    center: Offset,
    maxRadius: Float,
    segment: WheelSegment,
    density: Float,
    fontScale: Float,
    wheelRotationDeg: Float,
    alpha: Float,
    labelColor: Color,
) {
    if (alpha < 0.05f) return

    val sweep = segment.sweepAngleDeg
    if (sweep < 2f) return

    val midAngleDeg = segment.startAngleDeg + sweep / 2f
    val midAngleRad = Math.toRadians(midAngleDeg.toDouble())
    val innerRadius = segment.innerRadiusFraction * maxRadius
    val outerRadius = segment.outerRadiusFraction * maxRadius
    val ringWidth = outerRadius - innerRadius
    val labelRadiusFraction = when (segment.emotion.tier) {
        EmotionTier.PRIMARY -> 0.52f
        EmotionTier.SECONDARY -> 0.68f
        EmotionTier.TERTIARY -> 0.62f
    }
    val labelRadius = innerRadius + ringWidth * labelRadiusFraction
    val sweepRad = Math.toRadians(sweep.toDouble()).toFloat()
    val halfSweepRad = sweepRad / 2f
    val arcLength = labelRadius * sweepRad
    val chordWidth = 2f * labelRadius * sin(halfSweepRad)
    val label = segment.emotion.label

    val labelX = center.x + labelRadius * cos(midAngleRad).toFloat()
    val labelY = center.y + labelRadius * sin(midAngleRad).toFloat()

    // Flip when the label crosses the horizontal midline on screen (not in local wheel space).
    val screenAngleRad = Math.toRadians((midAngleDeg + wheelRotationDeg).toDouble())
    val isBelowHorizontalMidline = sin(screenAngleRad) > 0f

    if (segment.emotion.id == UNSURE_EMOTION_ID) {
        drawStackedSegmentLabel(
            labelX = labelX,
            labelY = labelY,
            midAngleDeg = midAngleDeg,
            isBelowHorizontalMidline = isBelowHorizontalMidline,
            density = density,
            fontScale = fontScale,
            ringWidth = ringWidth,
            alpha = alpha,
            labelColor = labelColor,
        )
        return
    }

    val fontScaleFactor = 1f / fontScale
    val baseFontPx = when (segment.emotion.tier) {
        EmotionTier.PRIMARY -> 15f * density * fontScaleFactor
        EmotionTier.SECONDARY -> 14f * density * fontScaleFactor
        EmotionTier.TERTIARY -> 12f * density * fontScaleFactor
    }
    val minFontPx = when (segment.emotion.tier) {
        EmotionTier.PRIMARY -> 9f * density * fontScaleFactor
        EmotionTier.SECONDARY -> 8f * density * fontScaleFactor
        EmotionTier.TERTIARY -> 8f * density * fontScaleFactor
    }

    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(
                (255 * alpha).toInt().coerceIn(0, 255),
                (labelColor.red * 255).toInt(),
                (labelColor.green * 255).toInt(),
                (labelColor.blue * 255).toInt(),
            )
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isFakeBoldText = segment.emotion.tier == EmotionTier.PRIMARY
        }

        val maxTextWidth = when (segment.emotion.tier) {
            EmotionTier.PRIMARY -> maxOf(chordWidth * 0.9f, arcLength * 0.88f)
            EmotionTier.SECONDARY,
            EmotionTier.TERTIARY,
            -> minOf(chordWidth * 0.86f, arcLength * 0.84f)
        }

        var fontSize = baseFontPx.coerceAtMost(ringWidth * 0.58f)
        paint.textSize = fontSize
        while (paint.measureText(label) > maxTextWidth && fontSize > minFontPx) {
            fontSize -= 0.5f
            paint.textSize = fontSize
        }

        val displayLabel = ellipsizeLabel(label, paint, maxTextWidth)

        var textRotation = midAngleDeg + 90f
        if (isBelowHorizontalMidline) {
            textRotation += 180f
        }

        save()
        translate(labelX, labelY)
        rotate(textRotation)
        drawText(displayLabel, 0f, fontSize * 0.32f, paint)
        restore()
    }
}

private fun DrawScope.drawStackedSegmentLabel(
    labelX: Float,
    labelY: Float,
    midAngleDeg: Float,
    isBelowHorizontalMidline: Boolean,
    density: Float,
    fontScale: Float,
    ringWidth: Float,
    alpha: Float,
    labelColor: Color,
) {
    val lines = listOf("Mixed", "/", "Unsure")
    val fontScaleFactor = 1f / fontScale
    val lineFontPx = (9.5f * density * fontScaleFactor).coerceAtMost(ringWidth * 0.34f)
    val lineGap = lineFontPx * 0.18f

    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(
                (255 * alpha).toInt().coerceIn(0, 255),
                (labelColor.red * 255).toInt(),
                (labelColor.green * 255).toInt(),
                (labelColor.blue * 255).toInt(),
            )
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isFakeBoldText = true
            textSize = lineFontPx
        }

        var textRotation = midAngleDeg + 90f
        if (isBelowHorizontalMidline) {
            textRotation += 180f
        }

        val totalHeight = lines.size * lineFontPx + (lines.size - 1) * lineGap
        val startY = -totalHeight / 2f + lineFontPx * 0.32f

        save()
        translate(labelX, labelY)
        rotate(textRotation)
        lines.forEachIndexed { index, line ->
            val y = startY + index * (lineFontPx + lineGap)
            paint.textSize = if (line == "/") lineFontPx * 0.82f else lineFontPx
            drawText(line, 0f, y, paint)
        }
        restore()
    }
}

private fun ellipsizeLabel(text: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text
    var trimmed = text
    while (trimmed.length > 1 && paint.measureText("$trimmed…") > maxWidth) {
        trimmed = trimmed.dropLast(1)
    }
    return if (trimmed.length < text.length) "$trimmed…" else trimmed
}

private fun buildWheelDescription(tier: EmotionTier, visibleCount: Int): String {
    val level = when (tier) {
        EmotionTier.PRIMARY -> "core feelings"
        EmotionTier.SECONDARY -> "secondary feelings"
        EmotionTier.TERTIARY -> "specific feelings"
    }
    return "Emotion wheel showing $visibleCount $level. Double tap to reset zoom."
}

private fun parseHexColor(hex: String): Color {
    val cleaned = hex.removePrefix("#")
    return Color(0xFF000000L or cleaned.toLong(16))
}
