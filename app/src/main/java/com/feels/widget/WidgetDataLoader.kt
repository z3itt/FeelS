package com.feels.widget

import android.content.Context
import android.content.Intent
import com.feels.MainActivity
import com.feels.core.domain.repository.UserPreferencesRepository
import com.feels.core.domain.usecase.GetEmotionalHeatmapUseCase
import com.feels.core.domain.usecase.GetWeeklyMoodSummaryUseCase
import com.feels.notifications.FeelSIntents
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun getEmotionalHeatmapUseCase(): GetEmotionalHeatmapUseCase
    fun getWeeklyMoodSummaryUseCase(): GetWeeklyMoodSummaryUseCase
    fun getUserPreferencesRepository(): UserPreferencesRepository
}

object WidgetDataLoader {
    private fun entryPoint(context: Context): WidgetEntryPoint =
        EntryPointAccessors.fromApplication(context.applicationContext, WidgetEntryPoint::class.java)

    suspend fun loadHeatmap(context: Context) =
        entryPoint(context).getEmotionalHeatmapUseCase().invoke(days = 30).first()

    suspend fun loadWeeklyMood(context: Context) =
        entryPoint(context).getWeeklyMoodSummaryUseCase().invoke().first()

    suspend fun isDarkTheme(context: Context): Boolean =
        entryPoint(context).getUserPreferencesRepository().isDarkThemeEnabled.first()
}

object WidgetIntents {
    fun openPrimary(context: Context, primaryEmotionId: String): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = FeelSIntents.ACTION_OPEN_PRIMARY
            putExtra(FeelSIntents.EXTRA_PRIMARY_EMOTION_ID, primaryEmotionId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun openHistory(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = FeelSIntents.ACTION_OPEN_HISTORY
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun openWheel(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = FeelSIntents.ACTION_OPEN_WHEEL
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    fun openBreathing(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            action = FeelSIntents.ACTION_OPEN_BREATHING
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
}
