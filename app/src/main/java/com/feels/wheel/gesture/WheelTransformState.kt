package com.feels.wheel.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import kotlin.math.cos
import kotlin.math.sin

@Stable
class WheelTransformStateHolder {
    val scale = Animatable(1f)
    val offsetX = Animatable(0f)
    val offsetY = Animatable(0f)

    suspend fun animateToFocus(targetScale: Float, targetOffsetX: Float, targetOffsetY: Float) {
        scale.animateTo(targetScale, spring(stiffness = Spring.StiffnessMediumLow))
        offsetX.animateTo(targetOffsetX, spring(stiffness = Spring.StiffnessMediumLow))
        offsetY.animateTo(targetOffsetY, spring(stiffness = Spring.StiffnessMediumLow))
    }

    suspend fun resetTransform() {
        animateToFocus(1f, 0f, 0f)
    }

    fun mapTouchToCanvas(touch: Offset, canvasCenter: Offset): Offset {
        val s = scale.value
        val x = (touch.x - canvasCenter.x - offsetX.value) / s + canvasCenter.x
        val y = (touch.y - canvasCenter.y - offsetY.value) / s + canvasCenter.y
        return Offset(x, y)
    }
}

@Stable
class WheelRotationStateHolder {
    val rotation = Animatable(0f)
    var frozenRotation: Float = 0f

    val effectiveRotation: Float
        get() = rotation.value

    suspend fun freezeAtCurrent() {
        frozenRotation = rotation.value
    }

    suspend fun snapToFrozen() {
        rotation.snapTo(frozenRotation)
    }
}

fun mapTouchInverseRotation(touch: Offset, center: Offset, rotationDeg: Float): Offset {
    val rad = Math.toRadians(-rotationDeg.toDouble())
    val dx = touch.x - center.x
    val dy = touch.y - center.y
    val cosR = cos(rad).toFloat()
    val sinR = sin(rad).toFloat()
    return Offset(
        center.x + dx * cosR - dy * sinR,
        center.y + dx * sinR + dy * cosR,
    )
}

@Composable
fun rememberWheelTransformState(): WheelTransformStateHolder = remember { WheelTransformStateHolder() }

@Composable
fun rememberWheelRotationState(): WheelRotationStateHolder = remember { WheelRotationStateHolder() }
