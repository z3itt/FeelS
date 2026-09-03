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
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.feels.core.domain.model.HeatmapDay

private const val HEATMAP_DAY_COUNT = 30
private const val HEATMAP_COLUMNS = 10
private const val HEATMAP_ROWS = HEATMAP_DAY_COUNT / HEATMAP_COLUMNS

class FeelSHeatmapWidget : FeelSResizableGlanceWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = WidgetDataLoader.isDarkTheme(context)
        val days = runCatching { WidgetDataLoader.loadHeatmap(context) }
            .getOrDefault(emptyList())
            .take(HEATMAP_DAY_COUNT)
        provideContent { HeatmapWidgetContent(days = days, isDark = isDark) }
    }
}

class FeelSHeatmapWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FeelSHeatmapWidget()
}

@Composable
private fun HeatmapWidgetContent(
    days: List<HeatmapDay>,
    isDark: Boolean,
) {
    val palette = widgetPalette(isDark)
    val context = LocalContext.current
    val size = LocalSize.current
    val compact = size.height < 70.dp
    val outerPadding = if (compact) 6.dp else 12.dp
    val titleReserve = if (compact) 0.dp else 22.dp
    val gridTopPadding = if (compact) 0.dp else 10.dp
    val rowSpacing = if (compact) 1.dp else 2.dp
    val availableGridHeight = size.height - outerPadding * 2 - titleReserve - gridTopPadding
    val cellHeight = (
        (availableGridHeight - rowSpacing * (HEATMAP_ROWS - 1)) / HEATMAP_ROWS
        ).coerceIn(5.dp, if (compact) 16.dp else 20.dp)
    val cellHorizontalPadding = if (compact) 1.dp else 2.dp

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.background))
            .clickable(actionStartActivity(WidgetIntents.openHistory(context)))
            .padding(outerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!compact) {
            Text(
                text = "30-day mood heatmap",
                style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.onSurfaceVariant)),
            )
        }
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight()
                .padding(top = gridTopPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.chunked(HEATMAP_COLUMNS).forEachIndexed { rowIndex, rowDays ->
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(top = if (rowIndex > 0) rowSpacing else 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowDays.forEach { day ->
                        HeatmapCell(
                            day = day,
                            cellHeight = cellHeight,
                            horizontalPadding = cellHorizontalPadding,
                            emptyColor = palette.emptyCell,
                            isDark = isDark,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.HeatmapCell(
    day: HeatmapDay,
    cellHeight: Dp,
    horizontalPadding: Dp,
    emptyColor: Color,
    isDark: Boolean,
) {
    Box(
        modifier = GlanceModifier
            .defaultWeight()
            .padding(horizontal = horizontalPadding)
            .height(cellHeight),
        contentAlignment = Alignment.Center,
    ) {
        WidgetGradientFill(
            gradientColorHexes = day.gradientColorHexes,
            displayColorHex = day.displayColorHex,
            hasCheckIn = day.hasCheckIn,
            isDark = isDark,
            modifier = GlanceModifier.fillMaxSize(),
            cornerRadius = if (cellHeight <= 8.dp) 2.dp else 4.dp,
            bitmapWidthPx = 64,
            bitmapHeightPx = cellHeight.toPx(LocalContext.current).toInt().coerceAtLeast(12),
            emptyColor = emptyColor,
        )
    }
}

private fun Dp.toPx(context: Context): Float = value * context.resources.displayMetrics.density
