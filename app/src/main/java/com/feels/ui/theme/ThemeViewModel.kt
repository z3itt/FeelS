package com.feels.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.repository.UserPreferencesRepository
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ThemeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val widgetRefreshCoordinator: WidgetRefreshCoordinator,
) : ViewModel() {

    val isDarkThemeEnabled: StateFlow<Boolean> =
        userPreferencesRepository.isDarkThemeEnabled
            .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeStartupStore.read(context))

    private var themeApplyJob: Job? = null

    fun setDarkThemeEnabled(enabled: Boolean) {
        ThemeStartupStore.write(context, enabled)
        themeApplyJob?.cancel()
        themeApplyJob = viewModelScope.launch {
            userPreferencesRepository.setDarkThemeEnabled(enabled)
            widgetRefreshCoordinator.refreshAll()
            delay(THEME_CROSSFADE_MS)
            applyThemeMode(context, isDarkThemeEnabled.value)
        }
    }

    companion object {
        private const val THEME_CROSSFADE_MS = 280L
    }
}
