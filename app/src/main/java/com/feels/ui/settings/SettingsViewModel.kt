package com.feels.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.repository.BackupRestoreResult
import com.feels.core.domain.repository.UserPreferencesRepository
import com.feels.core.domain.usecase.ExportCheckInsUseCase
import com.feels.core.domain.usecase.ImportCheckInsUseCase
import com.feels.notifications.ReminderScheduler
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val morningHour: Int = 9,
    val morningMinute: Int = 0,
    val eveningHour: Int = 18,
    val eveningMinute: Int = 0,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val exportCheckInsUseCase: ExportCheckInsUseCase,
    private val importCheckInsUseCase: ImportCheckInsUseCase,
    private val widgetRefreshCoordinator: WidgetRefreshCoordinator,
) : AndroidViewModel(application) {

    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferencesRepository.remindersEnabled,
        userPreferencesRepository.morningReminderHour,
        userPreferencesRepository.morningReminderMinute,
        userPreferencesRepository.eveningReminderHour,
        userPreferencesRepository.eveningReminderMinute,
    ) { enabled, morningHour, morningMinute, eveningHour, eveningMinute ->
        SettingsUiState(
            remindersEnabled = enabled,
            morningHour = morningHour,
            morningMinute = morningMinute,
            eveningHour = eveningHour,
            eveningMinute = eveningMinute,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setRemindersEnabled(enabled)
            reschedule()
        }
    }

    fun setMorningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setMorningReminderTime(hour, minute)
            reschedule()
        }
    }

    fun setEveningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setEveningReminderTime(hour, minute)
            reschedule()
        }
    }

    suspend fun createExportJson(): String = exportCheckInsUseCase()

    fun importBackup(json: String) {
        viewModelScope.launch {
            runCatching { importCheckInsUseCase(json) }
                .onSuccess { result ->
                    widgetRefreshCoordinator.refreshAll()
                    _messages.tryEmit(restoreMessage(result))
                }
                .onFailure {
                    _messages.tryEmit("That backup file could not be read.")
                }
        }
    }

    private suspend fun reschedule() {
        val context: Context = getApplication()
        ReminderScheduler.scheduleAll(context, userPreferencesRepository)
    }

    private fun restoreMessage(result: BackupRestoreResult): String {
        return when {
            result.importedCount == 0 && result.skippedCount == 0 -> "No check-ins found in that file."
            result.skippedCount == 0 -> "Imported ${result.importedCount} check-ins."
            else -> "Imported ${result.importedCount} check-ins. Skipped ${result.skippedCount}."
        }
    }
}
