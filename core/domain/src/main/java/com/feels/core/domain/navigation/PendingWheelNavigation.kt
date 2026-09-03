package com.feels.core.domain.navigation

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed class MainNavigationRequest {
    data class OpenWheel(val primaryEmotionId: String? = null) : MainNavigationRequest()
    data object OpenHistory : MainNavigationRequest()
    data object OpenBreathing : MainNavigationRequest()
}

@Singleton
class PendingWheelNavigation @Inject constructor() {
    private val _requests = MutableSharedFlow<MainNavigationRequest>(extraBufferCapacity = 1)
    val requests: SharedFlow<MainNavigationRequest> = _requests.asSharedFlow()

    var primaryEmotionId: String? = null
    private var pendingBreathing: Boolean = false

    fun requestOpenWheel(primaryEmotionId: String? = null) {
        if (!primaryEmotionId.isNullOrBlank()) {
            this.primaryEmotionId = primaryEmotionId
        }
        _requests.tryEmit(MainNavigationRequest.OpenWheel(primaryEmotionId))
    }

    fun requestOpenHistory() {
        _requests.tryEmit(MainNavigationRequest.OpenHistory)
    }

    fun requestOpenBreathing() {
        pendingBreathing = true
        _requests.tryEmit(MainNavigationRequest.OpenBreathing)
    }

    fun consumeOpenBreathing(): Boolean {
        val pending = pendingBreathing
        pendingBreathing = false
        return pending
    }

    fun consumePrimaryEmotionId(): String? {
        val id = primaryEmotionId
        primaryEmotionId = null
        return id
    }
}
