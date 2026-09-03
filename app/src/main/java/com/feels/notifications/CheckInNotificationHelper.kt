package com.feels.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.feels.MainActivity
import com.feels.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckInNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "FeelS check-in reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Gentle reminders to log how you feel"
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    fun showCheckInReminder() {
        if (!canPostNotifications()) return
        ensureChannel()
        val openIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            pendingIntentFlags(),
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("How are you feeling?")
            .setContentText("Tap a quick option or open FeelS to explore the wheel.")
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(buildQuickAction(FeelSPrimaryIds.SAD, "Sad"))
            .addAction(buildQuickAction(FeelSPrimaryIds.BAD, "Bad"))
            .addAction(buildQuickAction(FeelSPrimaryIds.HAPPY, "Happy"))
            .build()

        NotificationManagerCompat.from(context).notify(REMINDER_NOTIFICATION_ID, notification)
    }

    fun showQuickCheckInSaved(emotionLabel: String) {
        if (!canPostNotifications()) return
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_notification)
            .setContentTitle("Check-in saved")
            .setContentText("$emotionLabel logged quietly in the background.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(QUICK_LOG_NOTIFICATION_ID, notification)
    }

    private fun buildQuickAction(emotionId: String, label: String): NotificationCompat.Action {
        val intent = Intent(context, QuickCheckInReceiver::class.java).apply {
            action = FeelSIntents.ACTION_QUICK_CHECK_IN
            putExtra(FeelSIntents.EXTRA_QUICK_EMOTION_ID, emotionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            emotionId.hashCode(),
            intent,
            pendingIntentFlags(),
        )
        return NotificationCompat.Action.Builder(0, label, pendingIntent).build()
    }

    private fun pendingIntentFlags(): Int =
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

    private fun canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val CHANNEL_ID = "feels_check_in"
        const val REMINDER_NOTIFICATION_ID = 1001
        const val QUICK_LOG_NOTIFICATION_ID = 1002
    }
}
