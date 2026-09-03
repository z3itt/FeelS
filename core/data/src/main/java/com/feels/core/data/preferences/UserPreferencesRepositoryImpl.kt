package com.feels.core.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.feels.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "feels_prefs")

@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : UserPreferencesRepository {

    private val disclaimerKey = booleanPreferencesKey("disclaimer_accepted")
    private val darkThemeKey = booleanPreferencesKey("dark_theme_enabled")
    private val remindersEnabledKey = booleanPreferencesKey("reminders_enabled")
    private val morningReminderKey = intPreferencesKey("morning_reminder_hour")
    private val morningReminderMinuteKey = intPreferencesKey("morning_reminder_minute")
    private val eveningReminderKey = intPreferencesKey("evening_reminder_hour")
    private val eveningReminderMinuteKey = intPreferencesKey("evening_reminder_minute")

    override val hasAcceptedDisclaimer: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[disclaimerKey] ?: false }

    override val isDarkThemeEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[darkThemeKey] ?: false }

    override val remindersEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[remindersEnabledKey] ?: true }

    override val morningReminderHour: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[morningReminderKey] ?: 9 }

    override val morningReminderMinute: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[morningReminderMinuteKey] ?: 0 }

    override val eveningReminderHour: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[eveningReminderKey] ?: 18 }

    override val eveningReminderMinute: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[eveningReminderMinuteKey] ?: 0 }

    override suspend fun setDisclaimerAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs -> prefs[disclaimerKey] = accepted }
    }

    override suspend fun setDarkThemeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[darkThemeKey] = enabled }
    }

    override suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[remindersEnabledKey] = enabled }
    }

    override suspend fun setMorningReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[morningReminderKey] = hour.coerceIn(0, 23)
            prefs[morningReminderMinuteKey] = minute.coerceIn(0, 59)
        }
    }

    override suspend fun setEveningReminderTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[eveningReminderKey] = hour.coerceIn(0, 23)
            prefs[eveningReminderMinuteKey] = minute.coerceIn(0, 59)
        }
    }
}
