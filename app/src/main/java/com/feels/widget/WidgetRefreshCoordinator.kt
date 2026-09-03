package com.feels.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRefreshCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun refreshAll() {
        FeelSQuickWidget().updateAll(context)
        FeelSHeatmapWidget().updateAll(context)
        FeelSWeeklyMoodWidget().updateAll(context)
        FeelSBreathingWidget().updateAll(context)
    }
}
