package com.feels.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.navigation.MainNavigationRequest
import com.feels.core.domain.navigation.PendingWheelNavigation
import com.feels.core.domain.usecase.ClearCheckInHistoryUseCase
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    pendingWheelNavigation: PendingWheelNavigation,
    private val clearCheckInHistoryUseCase: ClearCheckInHistoryUseCase,
    private val widgetRefreshCoordinator: WidgetRefreshCoordinator,
) : ViewModel() {

    val navigationRequests: SharedFlow<MainNavigationRequest> = pendingWheelNavigation.requests

    private val _historyCleared = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val historyCleared: SharedFlow<Unit> = _historyCleared.asSharedFlow()

    fun clearHistory() {
        viewModelScope.launch {
            clearCheckInHistoryUseCase()
            widgetRefreshCoordinator.refreshAll()
            _historyCleared.tryEmit(Unit)
        }
    }
}
