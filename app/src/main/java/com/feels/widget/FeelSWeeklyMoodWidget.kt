package com.feels.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.feels.core.domain.model.WeeklyMoodDay

private const val WEEKLY_DAY_COUNT = 7

class FeelSWeeklyMoodWidget : FeelSResizableGlanceWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = WidgetDataLoader.isDarkTheme(context)
        val days = runCatching { WidgetDataLoader.loadWeeklyMood(context) }
            .getOrDefault(emptyList())
        provideContent { WeeklyMoodWidgetContent(days = days, isDark = isDark) }
    }
}

class FeelSWeeklyMoodWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FeelSWeeklyMoodWidget()
}

@Composable
private fun WeeklyMoodWidgetContent(
    days: List<WeeklyMoodDay>,
    isDark: Boolean,
) {
    val palette = widgetPalette(isDark)
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < 70.dp
    val outerPadding = if (compact) 6.dp else 10.dp
    val chartTopPadding = if (compact) 0.dp else 8.dp
    val labelHeight = if (compact) 0.dp else 14.dp
    val titleHeight = if (compact) 0.dp else 18.dp

    val availableChartHeight = size.height - outerPadding * 2 - titleHeight - chartTopPadding - labelHeight
    val barHeight = if (compact) {
        availableChartHeight.coerceIn(18.dp, 48.dp)
    } else {
        (availableChartHeight.value * 0.88f)
            .dp
            .coerceIn(32.dp, 140.dp)
    }

    val displayDays = normalizeWeeklyDays(days)
    val innerWidth = (size.width.value - outerPadding.value * 2).coerceAtLeast(WEEKLY_DAY_COUNT * 16f)
    val slotWidth = (innerWidth / WEEKLY_DAY_COUNT).dp
    val barWidth = (slotWidth.value - 3f).coerceAtLeast(8f).dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.background))
            .clickable(actionStartActivity(WidgetIntents.openHistory(context)))
            .padding(outerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Top,
    ) {
        if (!compact) {
            Text(
                text = "Weekly mood",
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.onSurfaceVariant)),
            )
        }
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = chartTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Bottom,
        ) {
            displayDays.forEach { day ->
                WeeklyDaySlot(
                    day = day,
                    slotWidth = slotWidth,
                    barWidth = barWidth,
                    barHeight = barHeight,
                    showLabel = !compact,
                    emptyColor = palette.emptyCell,
                    labelColor = palette.onSurfaceVariant,
                    isDark = isDark,
                )
            }
        }
    }
}

@Composable
private fun WeeklyDaySlot(
    day: WeeklyMoodDay,
    slotWidth: Dp,
    barWidth: Dp,
    barHeight: Dp,
    showLabel: Boolean,
    emptyColor: Color,
    labelColor: Color,
    isDark: Boolean,
) {
    val context = LocalContext.current
    val barCornerRadius = if (barHeight <= 24.dp) 6.dp else 8.dp
    val density = context.resources.displayMetrics.density
    val bitmapWidthPx = (barWidth.value * density).toInt().coerceIn(8, 48)

    Column(
        modifier = GlanceModifier.width(slotWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.Bottom,
    ) {
        WidgetGradientFill(
            gradientColorHexes = day.gradientColorHexes,
            displayColorHex = day.displayColorHex,
            hasCheckIn = day.hasCheckIn,
            isDark = isDark,
            modifier = GlanceModifier
                .width(barWidth)
                .height(barHeight),
            cornerRadius = barCornerRadius,
            bitmapWidthPx = bitmapWidthPx,
            bitmapHeightPx = barHeight.toPx(context).toInt().coerceAtLeast(16),
            emptyColor = emptyColor,
        )
        if (showLabel) {
            Text(
                text = day.dayLabel.take(3),
                modifier = GlanceModifier.width(slotWidth),
                style = TextStyle(
                    fontSize = 8.sp,
                    color = ColorProvider(labelColor),
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

private fun normalizeWeeklyDays(days: List<WeeklyMoodDay>): List<WeeklyMoodDay> {
    val source = days.takeLast(WEEKLY_DAY_COUNT)
    return List(WEEKLY_DAY_COUNT) { index ->
        val day = source.getOrNull(index)
        WeeklyMoodDay(
            dayLabel = day?.dayLabel?.takeIf { it.isNotBlank() } ?: fallbackDayLabel(index),
            hasCheckIn = day?.hasCheckIn == true,
            displayColorHex = day?.displayColorHex,
            gradientColorHexes = day?.gradientColorHexes.orEmpty(),
            summaryLabel = day?.summaryLabel,
            entryCount = day?.entryCount ?: 0,
        )
    }
}

private fun fallbackDayLabel(index: Int): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.DAY_OF_YEAR, -(WEEKLY_DAY_COUNT - 1 - index))
    return java.text.SimpleDateFormat("EEE", java.util.Locale.ENGLISH).format(calendar.time)
}

private fun Dp.toPx(context: Context): Float = value * context.resources.displayMetrics.density
