package com.feels.ui.components

import com.feels.core.domain.model.BreathingPatternIds

data class BreathingPatternSpec(
    val id: String,
    val name: String,
    val shortLabel: String,
    val subtitle: String,
    val inhaleMs: Long,
    val holdMs: Long,
    val exhaleMs: Long,
    val holdAfterExhaleMs: Long = 0,
)

val breathingPatterns = listOf(
    BreathingPatternSpec(
        id = BreathingPatternIds.BOX,
        name = "Box breathing",
        shortLabel = "Box",
        subtitle = "4 in · 4 hold · 4 out · 4 hold",
        inhaleMs = 4_000,
        holdMs = 4_000,
        exhaleMs = 4_000,
        holdAfterExhaleMs = 4_000,
    ),
    BreathingPatternSpec(
        id = BreathingPatternIds.RELAX,
        name = "Relaxing exhale",
        shortLabel = "Relax",
        subtitle = "4 in · 2 hold · 6 out",
        inhaleMs = 4_000,
        holdMs = 2_000,
        exhaleMs = 6_000,
    ),
    BreathingPatternSpec(
        id = BreathingPatternIds.CALM,
        name = "Calm hold",
        shortLabel = "Calm",
        subtitle = "4 in · 6 hold · 4 out",
        inhaleMs = 4_000,
        holdMs = 6_000,
        exhaleMs = 4_000,
    ),
    BreathingPatternSpec(
        id = BreathingPatternIds.QUICK,
        name = "Quick reset",
        shortLabel = "Quick",
        subtitle = "3 in · 3 hold · 3 out",
        inhaleMs = 3_000,
        holdMs = 3_000,
        exhaleMs = 3_000,
    ),
)

fun breathingPattern(id: String): BreathingPatternSpec =
    breathingPatterns.firstOrNull { it.id == id } ?: breathingPatterns.first()

fun defaultBreathingPattern(): BreathingPatternSpec = breathingPattern(BreathingPatternIds.QUICK)
