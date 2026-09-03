package com.feels.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.feels.core.domain.repository.EmotionRepository
import com.feels.core.domain.usecase.QuickLogCheckInUseCase
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuickCheckInReceiver : BroadcastReceiver() {

    @Inject lateinit var quickLogCheckInUseCase: QuickLogCheckInUseCase
    @Inject lateinit var emotionRepository: EmotionRepository
    @Inject lateinit var notificationHelper: CheckInNotificationHelper
    @Inject lateinit var widgetRefreshCoordinator: WidgetRefreshCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != FeelSIntents.ACTION_QUICK_CHECK_IN) return
        val emotionId = intent.getStringExtra(FeelSIntents.EXTRA_QUICK_EMOTION_ID) ?: return
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loggedId = quickLogCheckInUseCase(emotionId)
                if (loggedId > 0L) {
                    val label = emotionRepository.getEmotionById(emotionId)?.label ?: "Feeling"
                    notificationHelper.showQuickCheckInSaved(label)
                    widgetRefreshCoordinator.refreshAll()
                }
            } catch (_: Exception) {
            } finally {
                pendingResult.finish()
            }
        }
    }
}
