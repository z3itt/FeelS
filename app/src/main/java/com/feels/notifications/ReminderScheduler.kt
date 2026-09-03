package com.feels.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.WorkManager
import com.feels.core.domain.repository.UserPreferencesRepository
import java.util.Calendar
import kotlinx.coroutines.flow.first

object ReminderScheduler {
    private const val LEGACY_PERIODIC_WORK_NAME = "feels_daily_reminder"
    private const val LEGACY_MORNING_WORK_NAME = "feels_morning_reminder"
    private const val LEGACY_EVENING_WORK_NAME = "feels_evening_reminder"

    const val EXTRA_SLOT = "reminder_slot"
    const val SLOT_MORNING = "morning"
    const val SLOT_EVENING = "evening"

    private const val REQUEST_MORNING = 40_001
    private const val REQUEST_EVENING = 40_002

    suspend fun scheduleAll(
        context: Context,
        userPreferencesRepository: UserPreferencesRepository,
    ) {
        cancelLegacyWork(context)
        val enabled = userPreferencesRepository.remindersEnabled.first()
        if (!enabled) {
            cancelAllAlarms(context)
            return
        }
        scheduleSlot(
            context = context,
            requestCode = REQUEST_MORNING,
            slot = SLOT_MORNING,
            hour = userPreferencesRepository.morningReminderHour.first(),
            minute = userPreferencesRepository.morningReminderMinute.first(),
        )
        scheduleSlot(
            context = context,
            requestCode = REQUEST_EVENING,
            slot = SLOT_EVENING,
            hour = userPreferencesRepository.eveningReminderHour.first(),
            minute = userPreferencesRepository.eveningReminderMinute.first(),
        )
    }

    private fun scheduleSlot(
        context: Context,
        requestCode: Int,
        slot: String,
        hour: Int,
        minute: Int,
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAtMillis = nextTriggerMillis(hour, minute)
        val pendingIntent = alarmPendingIntent(context, requestCode, slot)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        alarmManager.cancel(alarmPendingIntent(context, REQUEST_MORNING, SLOT_MORNING))
        alarmManager.cancel(alarmPendingIntent(context, REQUEST_EVENING, SLOT_EVENING))
    }

    private fun cancelLegacyWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(LEGACY_PERIODIC_WORK_NAME)
        workManager.cancelUniqueWork(LEGACY_MORNING_WORK_NAME)
        workManager.cancelUniqueWork(LEGACY_EVENING_WORK_NAME)
    }

    private fun alarmPendingIntent(
        context: Context,
        requestCode: Int,
        slot: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT, slot)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance()
        next.set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
        next.set(Calendar.MINUTE, minute.coerceIn(0, 59))
        next.set(Calendar.SECOND, 0)
        next.set(Calendar.MILLISECOND, 0)
        if (!next.after(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
