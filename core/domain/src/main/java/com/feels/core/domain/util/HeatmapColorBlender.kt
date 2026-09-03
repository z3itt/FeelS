package com.feels.core.domain.util

object HeatmapColorBlender {

    data class ColorSample(
        val colorHex: String,
        val intensity: Int,
    )

    /** One intensity-adjusted color per check-in, in chronological order. */
    fun gradientColors(samples: List<ColorSample>): List<String> =
        samples.mapNotNull { sample ->
            val (red, green, blue) = parseHex(sample.colorHex)
            val adjusted = adjustLightnessForIntensity(
                red = red,
                green = green,
                blue = blue,
                averageIntensity = sample.intensity.coerceIn(1, 5).toFloat(),
            )
            toHex(adjusted.first, adjusted.second, adjusted.third)
        }

    fun blend(samples: List<ColorSample>): String? {
        val gradient = gradientColors(samples)
        if (gradient.isEmpty()) return null
        if (gradient.size == 1) return gradient.first()

        var totalWeight = 0f
        var red = 0f
        var green = 0f
        var blue = 0f

        samples.forEach { sample ->
            val weight = sample.intensity.coerceIn(1, 5).toFloat()
            val (r, g, b) = parseHex(sample.colorHex)
            totalWeight += weight
            red += r * weight
            green += g * weight
            blue += b * weight
        }

        if (totalWeight <= 0f) return gradient.first()

        val averageIntensity = samples.sumOf { it.intensity.coerceIn(1, 5) }.toFloat() / samples.size
        val blended = adjustLightnessForIntensity(
            red = (red / totalWeight).toInt().coerceIn(0, 255),
            green = (green / totalWeight).toInt().coerceIn(0, 255),
            blue = (blue / totalWeight).toInt().coerceIn(0, 255),
            averageIntensity = averageIntensity,
        )
        return toHex(blended.first, blended.second, blended.third)
    }

    private fun adjustLightnessForIntensity(
        red: Int,
        green: Int,
        blue: Int,
        averageIntensity: Float,
    ): Triple<Int, Int, Int> {
        val (hue, saturation, lightness) = rgbToHsl(red, green, blue)
        val lightnessFactor = when {
            averageIntensity <= 1f -> 1.18f
            averageIntensity >= 5f -> 0.72f
            else -> 1.18f - ((averageIntensity - 1f) / 4f) * 0.46f
        }
        val adjustedLightness = (lightness * lightnessFactor).coerceIn(0.18f, 0.88f)
        return hslToRgb(hue, saturation, adjustedLightness)
    }

    private fun parseHex(hex: String): Triple<Int, Int, Int> {
        val cleaned = hex.removePrefix("#")
        val value = cleaned.toLong(16)
        val red = ((value shr 16) and 0xFF).toInt()
        val green = ((value shr 8) and 0xFF).toInt()
        val blue = (value and 0xFF).toInt()
        return Triple(red, green, blue)
    }

    private fun toHex(red: Int, green: Int, blue: Int): String {
        return String.format("#%02X%02X%02X", red, green, blue)
    }

    private fun rgbToHsl(red: Int, green: Int, blue: Int): Triple<Float, Float, Float> {
        val r = red / 255f
        val g = green / 255f
        val b = blue / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min

        val lightness = (max + min) / 2f
        if (delta == 0f) {
            return Triple(0f, 0f, lightness)
        }

        val saturation = if (lightness <= 0.5f) {
            delta / (max + min)
        } else {
            delta / (2f - max - min)
        }

        val hue = when (max) {
            r -> ((g - b) / delta + (if (g < b) 6f else 0f)) / 6f
            g -> ((b - r) / delta + 2f) / 6f
            else -> ((r - g) / delta + 4f) / 6f
        }
        return Triple(hue, saturation, lightness)
    }

    private fun hslToRgb(hue: Float, saturation: Float, lightness: Float): Triple<Int, Int, Int> {
        if (saturation == 0f) {
            val gray = (lightness * 255f).toInt().coerceIn(0, 255)
            return Triple(gray, gray, gray)
        }

        fun hueToRgb(p: Float, q: Float, tIn: Float): Float {
            var t = tIn
            if (t < 0f) t += 1f
            if (t > 1f) t -= 1f
            return when {
                t < 1f / 6f -> p + (q - p) * 6f * t
                t < 1f / 2f -> q
                t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
                else -> p
            }
        }

        val q = if (lightness < 0.5f) {
            lightness * (1f + saturation)
        } else {
            lightness + saturation - lightness * saturation
        }
        val p = 2f * lightness - q
        val red = (hueToRgb(p, q, hue + 1f / 3f) * 255f).toInt().coerceIn(0, 255)
        val green = (hueToRgb(p, q, hue) * 255f).toInt().coerceIn(0, 255)
        val blue = (hueToRgb(p, q, hue - 1f / 3f) * 255f).toInt().coerceIn(0, 255)
        return Triple(red, green, blue)
    }
}
