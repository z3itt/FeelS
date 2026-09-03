package com.feels.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.feels.ui.components.defaultBreathingPattern

class FeelSBreathingWidget : FeelSResizableGlanceWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = WidgetDataLoader.isDarkTheme(context)
        provideContent { BreathingWidgetContent(isDark) }
    }
}

class FeelSBreathingWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FeelSBreathingWidget()
}

@Composable
private fun BreathingWidgetContent(isDark: Boolean) {
    val palette = widgetPalette(isDark)
    val context = LocalContext.current
    val pattern = defaultBreathingPattern()
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.background))
            .clickable(actionStartActivity(WidgetIntents.openBreathing(context)))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .background(ColorProvider(palette.accentSurface))
                .cornerRadius(18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "◎",
                style = TextStyle(fontSize = 16.sp, color = ColorProvider(palette.accentOnSurface)),
            )
        }
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(start = 10.dp),
        ) {
            Text(
                text = "Breathing",
                style = TextStyle(fontSize = 13.sp, color = ColorProvider(palette.onBackground)),
            )
            Text(
                text = pattern.subtitle,
                style = TextStyle(fontSize = 10.sp, color = ColorProvider(palette.onSurfaceVariant)),
            )
        }
    }
}
