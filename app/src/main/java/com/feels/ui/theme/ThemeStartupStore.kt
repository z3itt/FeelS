package com.feels.ui.theme

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeStartupStore {
    private const val PREFS_NAME = "feels_theme_startup"
    private const val KEY_DARK = "is_dark"
    private const val KEY_NOTIFICATION_PROMPTED = "notification_prompted"

    fun read(context: Context): Boolean {
        val prefsContext = context.applicationContext ?: context
        return prefsContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK, false)
    }

    fun write(context: Context, isDarkTheme: Boolean) {
        val prefsContext = context.applicationContext ?: context
        prefsContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK, isDarkTheme)
            .apply()
    }

    fun hasPromptedNotificationPermission(context: Context): Boolean {
        val prefsContext = context.applicationContext ?: context
        return prefsContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_NOTIFICATION_PROMPTED, false)
    }

    fun markNotificationPermissionPrompted(context: Context) {
        val prefsContext = context.applicationContext ?: context
        prefsContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_NOTIFICATION_PROMPTED, true)
            .apply()
    }

    fun applyToDelegates(isDarkTheme: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkTheme) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
    }
}
