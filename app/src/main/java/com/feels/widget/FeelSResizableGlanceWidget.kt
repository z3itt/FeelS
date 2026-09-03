package com.feels.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode

/**
 * Glance defaults to [SizeMode.Single], which locks the widget to its minimum size and
 * ignores launcher resize events. Exact mode recomposes when the host reports new bounds.
 */
abstract class FeelSResizableGlanceWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
}
