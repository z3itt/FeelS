package com.feels.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _hasAcceptedDisclaimer = MutableStateFlow<Boolean?>(null)
    val hasAcceptedDisclaimer: StateFlow<Boolean?> = _hasAcceptedDisclaimer.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferencesRepository.hasAcceptedDisclaimer.collect { accepted ->
                _hasAcceptedDisclaimer.value = accepted
            }
        }
    }

    fun acceptDisclaimer() {
        viewModelScope.launch {
            userPreferencesRepository.setDisclaimerAccepted(true)
        }
    }
}
