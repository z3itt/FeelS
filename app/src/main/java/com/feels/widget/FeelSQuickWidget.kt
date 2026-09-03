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
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.feels.notifications.FeelSPrimaryIds

class FeelSQuickWidget : FeelSResizableGlanceWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val isDark = WidgetDataLoader.isDarkTheme(context)
        provideContent { QuickWidgetContent(isDark) }
    }
}

class FeelSQuickWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FeelSQuickWidget()
}

@Composable
private fun QuickWidgetContent(isDark: Boolean) {
    val palette = widgetPalette(isDark)
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(palette.background))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "FeelS quick check-in",
            style = TextStyle(fontSize = 12.sp, color = ColorProvider(palette.onSurfaceVariant)),
        )
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            quickEmotions.forEachIndexed { index, emotion ->
                if (index > 0) {
                    Spacer(GlanceModifier.width(8.dp))
                }
                QuickEmotionChip(emotion = emotion, onBackground = palette.onBackground)
            }
        }
    }
}

@Composable
private fun RowScope.QuickEmotionChip(
    emotion: QuickEmotion,
    onBackground: Color,
) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .defaultWeight()
            .height(40.dp)
            .background(ColorProvider(emotion.color))
            .cornerRadius(10.dp)
            .clickable(
                actionStartActivity(WidgetIntents.openPrimary(context, emotion.id)),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emotion.label,
            style = TextStyle(
                fontSize = 10.sp,
                color = ColorProvider(onBackground),
            ),
        )
    }
}

private data class QuickEmotion(
    val id: String,
    val label: String,
    val color: Color,
)

private val quickEmotions = listOf(
    QuickEmotion(FeelSPrimaryIds.HAPPY, "Happy", Color(0xFFF5D547)),
    QuickEmotion(FeelSPrimaryIds.SAD, "Sad", Color(0xFF7EB8D4)),
    QuickEmotion(FeelSPrimaryIds.BAD, "Bad", Color(0xFF7CB89A)),
    QuickEmotion(FeelSPrimaryIds.ANGRY, "Angry", Color(0xFFE8929C)),
)
