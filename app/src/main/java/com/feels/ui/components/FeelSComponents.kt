package com.feels.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.feels.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.feels.core.domain.model.BreathingPatternIds
import com.feels.core.domain.model.CheckInIntervention
import com.feels.core.domain.model.CheckInInterventionType
import com.feels.core.domain.model.EmotionPath
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    emotionPath: EmotionPath,
    intensity: Int,
    note: String,
    intervention: CheckInIntervention?,
    onIntensityChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onStartGrounding: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = emotionPath.deepest?.label ?: "Check in",
                    style = MaterialTheme.typography.titleLarge,
                )
                if (emotionPath.breadcrumb.isNotBlank()) {
                    Text(
                        text = emotionPath.breadcrumb,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Text(
                    text = "How intense does this feel?",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Slider(
                    value = intensity.toFloat(),
                    onValueChange = { onIntensityChange(it.toInt()) },
                    valueRange = 1f..5f,
                    steps = 3,
                )
                Text(
                    text = intensityLabel(intensity),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val reflectiveQuestion = intervention?.reflectiveQuestion
                if (intervention?.type == CheckInInterventionType.REFLECTIVE_PROMPT &&
                    !reflectiveQuestion.isNullOrBlank()
                ) {
                    Text(
                        text = reflectiveQuestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                TextField(
                    value = note,
                    onValueChange = onNoteChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.check_in_note_hint)) },
                    minLines = 1,
                    maxLines = 2,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (intervention?.type == CheckInInterventionType.HIGH_DISTRESS_GROUNDING) {
                    OutlinedButton(
                        onClick = {
                            focusManager.clearFocus()
                            onStartGrounding()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        Icon(Icons.Default.SelfImprovement, contentDescription = null)
                        Text(
                            text = "1-Min Grounding",
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSave()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text("Save check-in")
                }
            }
        }
    }
}

@Composable
fun PostCheckInActionCard(
    intervention: CheckInIntervention,
    onStartGrounding: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Check-in saved",
            style = MaterialTheme.typography.titleMedium,
        )

        when (intervention.type) {
            CheckInInterventionType.HIGH_DISTRESS_GROUNDING -> {
                Text(
                    text = "That sounds like a lot. A short grounding exercise may help your body settle.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Text("Not now")
                    }
                    Button(
                        onClick = onStartGrounding,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("1-Min Grounding")
                    }
                }
            }
            CheckInInterventionType.REFLECTIVE_PROMPT -> {
                Text(
                    text = intervention.reflectiveQuestion.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "Take a quiet moment with that question. No need to write anything down.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Done")
                }
            }
            CheckInInterventionType.NONE -> {
                Text(
                    text = "Your feeling is logged. Come back whenever you're ready.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
fun GentleGroundingOffer(
    emotionLabel: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PostCheckInActionCard(
        intervention = CheckInIntervention(type = CheckInInterventionType.HIGH_DISTRESS_GROUNDING),
        onStartGrounding = onAccept,
        onDismiss = onDecline,
        modifier = modifier,
    )
}

@Composable
fun HapticGroundingExercise(
    initialPatternId: String = BreathingPatternIds.BOX,
    onPatternSelected: (String) -> Unit = {},
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemVibrator() }
    var selectedPatternId by remember(initialPatternId) { mutableStateOf(initialPatternId) }
    val pattern = remember(selectedPatternId) { breathingPattern(selectedPatternId) }
    var phaseLabel by remember { mutableStateOf("Breathe in") }
    var secondsRemaining by remember { mutableIntStateOf(60) }
    val sessionProgress = (60 - secondsRemaining) / 60f

    val phaseDurationMs = when (phaseLabel) {
        "Breathe in" -> pattern.inhaleMs
        "Hold" -> pattern.holdMs
        "Breathe out" -> pattern.exhaleMs
        else -> pattern.holdAfterExhaleMs
    }.coerceAtLeast(1_000)

    val transition = rememberInfiniteTransition(label = "breath")
    val scale by transition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = phaseDurationMs.toInt()),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )
    val ringAlpha by transition.animateFloat(
        initialValue = 0.18f,
        targetValue = 0.42f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = phaseDurationMs.toInt()),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "ringAlpha",
    )

    LaunchedEffect(selectedPatternId) {
        phaseLabel = "Breathe in"
        secondsRemaining = 60
        val startedAt = System.currentTimeMillis()
        while (System.currentTimeMillis() - startedAt < 60_000L) {
            phaseLabel = "Breathe in"
            vibrator?.runBreathPulse(durationMs = pattern.inhaleMs, strong = true)
            delay(pattern.inhaleMs)
            if (pattern.holdMs > 0) {
                phaseLabel = "Hold"
                vibrator?.cancel()
                delay(pattern.holdMs)
            }
            phaseLabel = "Breathe out"
            vibrator?.runBreathPulse(durationMs = pattern.exhaleMs, strong = false)
            delay(pattern.exhaleMs)
            if (pattern.holdAfterExhaleMs > 0) {
                phaseLabel = "Hold"
                vibrator?.cancel()
                delay(pattern.holdAfterExhaleMs)
            }
        }
    }

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining -= 1
        }
    }

    DisposableEffect(Unit) {
        onDispose { vibrator?.cancel() }
    }

    val calmGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.background,
        ),
    )
    val orbColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
    val ringColor = MaterialTheme.colorScheme.primary.copy(alpha = ringAlpha)

    Dialog(
        onDismissRequest = onDone,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(calmGradient),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 32.dp)
                    .widthIn(max = 420.dp),
            ) {
                Text(
                    text = "Breathing space",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = pattern.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = pattern.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                ) {
                    breathingPatterns.forEach { option ->
                        FilterChip(
                            selected = selectedPatternId == option.id,
                            onClick = {
                                selectedPatternId = option.id
                                onPatternSelected(option.id)
                            },
                            label = { Text(option.shortLabel) },
                        )
                    }
                }

                LinearProgressIndicator(
                    progress = { sessionProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Text(
                    text = "${secondsRemaining}s remaining",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(220.dp)
                        .padding(vertical = 8.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val baseRadius = size.minDimension * 0.34f * scale
                        drawCircle(
                            color = ringColor,
                            radius = baseRadius * 1.18f,
                            center = center,
                            style = Stroke(width = 3.dp.toPx()),
                        )
                        drawCircle(
                            color = ringColor.copy(alpha = ringAlpha * 0.7f),
                            radius = baseRadius * 0.92f,
                            center = center,
                            style = Stroke(width = 2.dp.toPx()),
                        )
                        drawCircle(
                            color = orbColor,
                            radius = if (phaseLabel == "Hold") baseRadius else baseRadius * 0.88f,
                            center = center,
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = phaseLabel,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Follow the rhythm",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                Text(
                    text = "Let your shoulders drop. Stop anytime.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                OutlinedButton(
                    onClick = onDone,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(0.6f),
                ) {
                    Text("Stop")
                }
            }
        }
    }
}

@Composable
fun GentleGroundingExercise(
    initialPatternId: String = BreathingPatternIds.BOX,
    onPatternSelected: (String) -> Unit = {},
    onDone: () -> Unit,
) {
    HapticGroundingExercise(
        initialPatternId = initialPatternId,
        onPatternSelected = onPatternSelected,
        onDone = onDone,
    )
}

private fun intensityLabel(value: Int): String = when (value) {
    1 -> "Very mild"
    2 -> "Mild"
    3 -> "Moderate"
    4 -> "Strong"
    5 -> "Very strong"
    else -> "Moderate"
}

private fun Context.getSystemVibrator(): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

private fun Vibrator.runBreathPulse(durationMs: Long, strong: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (strong) {
            VibrationEffect.createWaveform(
                longArrayOf(0, 400, 200, 400, 200, 400, 200, 400),
                intArrayOf(0, 180, 0, 180, 0, 180, 0, 180),
                -1,
            )
        } else {
            VibrationEffect.createWaveform(
                longArrayOf(0, 250, 350, 250, 350, 250),
                intArrayOf(0, 120, 0, 90, 0, 60),
                -1,
            )
        }
        vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        vibrate(durationMs)
    }
}
