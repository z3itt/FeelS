package com.feels

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.feels.core.data.local.seed.DatabaseSeeder
import com.feels.core.domain.repository.UserPreferencesRepository
import com.feels.notifications.CheckInNotificationHelper
import com.feels.notifications.ReminderScheduler
import com.feels.ui.theme.ThemeStartupStore
import com.feels.ui.theme.applyThemeMode
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class FeelSApplication : Application(), Configuration.Provider {

    @Inject lateinit var databaseSeeder: DatabaseSeeder
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: CheckInNotificationHelper
    @Inject lateinit var widgetRefreshCoordinator: WidgetRefreshCoordinator
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        val isDarkTheme = ThemeStartupStore.read(this)
        applyThemeMode(this, isDarkTheme)
        applicationScope.launch {
            databaseSeeder.seedIfEmpty()
            widgetRefreshCoordinator.refreshAll()
            ReminderScheduler.scheduleAll(this@FeelSApplication, userPreferencesRepository)
        }
        notificationHelper.ensureChannel()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
