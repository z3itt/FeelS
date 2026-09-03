package com.feels.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.feels.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository
    @Inject lateinit var notificationHelper: CheckInNotificationHelper

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED,
                    Intent.ACTION_LOCALE_CHANGED -> {
                        ReminderScheduler.scheduleAll(context, userPreferencesRepository)
                    }
                    else -> {
                        val enabled = userPreferencesRepository.remindersEnabled.first()
                        if (enabled) {
                            notificationHelper.showCheckInReminder()
                        }
                        ReminderScheduler.scheduleAll(context, userPreferencesRepository)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
