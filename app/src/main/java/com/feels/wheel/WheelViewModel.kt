package com.feels.wheel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.model.BreathingPatternIds
import com.feels.core.domain.model.CheckInIntervention
import com.feels.core.domain.model.CheckInInterventionType
import com.feels.core.domain.model.Emotion
import com.feels.core.domain.model.EmotionPath
import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.model.WheelFocusLevel
import com.feels.core.domain.navigation.MainNavigationRequest
import com.feels.core.domain.navigation.PendingWheelNavigation
import com.feels.core.domain.repository.EmotionRepository
import com.feels.core.domain.usecase.BuildEmotionPathUseCase
import com.feels.core.domain.usecase.EvaluateCheckInInterventionUseCase
import com.feels.core.domain.usecase.GetChildEmotionsUseCase
import com.feels.core.domain.usecase.LogCheckInUseCase
import com.feels.core.domain.usecase.SearchEmotionsUseCase
import com.feels.wheel.geometry.FocusedWheelGeometryBuilder
import com.feels.wheel.geometry.WheelGeometryBuilder
import com.feels.wheel.geometry.WheelSegment
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WheelUiState(
    val isLoading: Boolean = true,
    val loadError: Boolean = false,
    val checkInSavedPulse: Int = 0,
    val segments: List<WheelSegment> = emptyList(),
    val focusLevel: WheelFocusLevel = WheelFocusLevel.PRIMARY,
    val focusedPrimaryId: String? = null,
    val focusedSecondaryId: String? = null,
    val selectedPath: EmotionPath = EmotionPath(),
    val selectedEmotionId: String? = null,
    val showCheckInSheet: Boolean = false,
    val showPostCheckInAction: Boolean = false,
    val showGroundingExercise: Boolean = false,
    val checkInIntensity: Int = 3,
    val checkInNote: String = "",
    val checkInIntervention: CheckInIntervention? = null,
    val postCheckInIntervention: CheckInIntervention? = null,
    val activeBreathingPatternId: String = BreathingPatternIds.BOX,
    val breadcrumb: String = "",
    val hint: String = "Tap a core feeling to begin",
    val searchQuery: String = "",
    val searchResults: List<Emotion> = emptyList(),
    val isSearching: Boolean = false,
    val searchPending: Boolean = false,
    val wheelRotationDeg: Float = 0f,
) {
    val displaySegments: List<WheelSegment>
        get() = FocusedWheelGeometryBuilder.build(
            allSegments = segments,
            focusLevel = focusLevel,
            focusedPrimaryId = focusedPrimaryId,
            focusedSecondaryId = focusedSecondaryId,
            anchorMidAngleDeg = anchorMidAngleDeg,
        )

    private val anchorMidAngleDeg: Float?
        get() = when (focusLevel) {
            WheelFocusLevel.SECONDARY -> segments
                .find { it.emotion.id == focusedPrimaryId }
                ?.let { it.startAngleDeg + it.sweepAngleDeg / 2f }
            WheelFocusLevel.TERTIARY -> segments
                .find { it.emotion.id == focusedSecondaryId }
                ?.let { it.startAngleDeg + it.sweepAngleDeg / 2f }
            WheelFocusLevel.PRIMARY -> null
        }
}

@HiltViewModel
class WheelViewModel @Inject constructor(
    private val emotionRepository: EmotionRepository,
    private val getChildEmotionsUseCase: GetChildEmotionsUseCase,
    private val buildEmotionPathUseCase: BuildEmotionPathUseCase,
    private val logCheckInUseCase: LogCheckInUseCase,
    private val evaluateCheckInInterventionUseCase: EvaluateCheckInInterventionUseCase,
    private val searchEmotionsUseCase: SearchEmotionsUseCase,
    private val pendingWheelNavigation: PendingWheelNavigation,
    private val widgetRefreshCoordinator: WidgetRefreshCoordinator,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WheelUiState())
    val uiState: StateFlow<WheelUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            pendingWheelNavigation.requests.collect { request ->
                when (request) {
                    is MainNavigationRequest.OpenWheel -> {
                        if (!_uiState.value.isLoading) {
                            consumePendingNavigation()
                        }
                    }
                    MainNavigationRequest.OpenBreathing -> {
                        if (!_uiState.value.isLoading) {
                            onOpenBreathingExercise()
                        }
                    }
                    MainNavigationRequest.OpenHistory -> Unit
                }
            }
        }
        viewModelScope.launch {
            emotionRepository.observeAllEmotions()
                .catch {
                    _uiState.update { state ->
                        state.copy(isLoading = false, loadError = true)
                    }
                }
                .collect { emotions ->
                val segments = WheelGeometryBuilder().build(emotions)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = false,
                        segments = segments,
                    )
                }
                consumePendingNavigation()
                if (pendingWheelNavigation.consumeOpenBreathing()) {
                    onOpenBreathingExercise()
                }
            }
        }
    }

    fun retryLoad() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = false) }
            try {
                val emotions = emotionRepository.observeAllEmotions().first()
                val segments = WheelGeometryBuilder().build(emotions)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = false,
                        segments = segments,
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false, loadError = true) }
            }
        }
    }

    fun onUnsureSelected() {
        viewModelScope.launch {
            val emotion = emotionRepository.getEmotionById(UNSURE_EMOTION_ID) ?: return@launch
            onPrimarySelected(emotion)
        }
    }

    fun consumePendingNavigation() {
        viewModelScope.launch {
            val primaryId = pendingWheelNavigation.consumePrimaryEmotionId() ?: return@launch
            val emotion = emotionRepository.getEmotionById(primaryId) ?: return@launch
            if (emotion.tier != EmotionTier.PRIMARY) return@launch
            onPrimarySelected(emotion)
        }
    }

    fun onWheelRotationChanged(rotationDeg: Float) {
        _uiState.update { it.copy(wheelRotationDeg = rotationDeg) }
    }

    fun onSegmentTapped(segment: WheelSegment) {
        viewModelScope.launch {
            when (segment.emotion.tier) {
                EmotionTier.PRIMARY -> onPrimarySelected(segment.emotion)
                EmotionTier.SECONDARY -> onSecondarySelected(segment.emotion)
                EmotionTier.TERTIARY -> onTertiarySelected(segment.emotion)
            }
        }
    }

    private suspend fun onPrimarySelected(emotion: Emotion) {
        val children = getChildEmotionsUseCase(emotion.id)
        val path = buildEmotionPathUseCase(emotion.id)
        if (children.isEmpty()) {
            openCheckIn(path, emotion.id)
            return
        }
        _uiState.update {
            it.copy(
                focusLevel = WheelFocusLevel.SECONDARY,
                focusedPrimaryId = emotion.id,
                focusedSecondaryId = null,
                selectedPath = path,
                selectedEmotionId = emotion.id,
                breadcrumb = path.breadcrumb,
                hint = "What kind of ${emotion.label.lowercase()}?",
                showPostCheckInAction = false,
            )
        }
    }

    private suspend fun onSecondarySelected(emotion: Emotion) {
        val children = getChildEmotionsUseCase(emotion.id)
        val path = buildEmotionPathUseCase(emotion.id)
        if (children.isEmpty()) {
            openCheckIn(path, emotion.id)
            return
        }
        _uiState.update {
            it.copy(
                focusLevel = WheelFocusLevel.TERTIARY,
                focusedSecondaryId = emotion.id,
                selectedPath = path,
                selectedEmotionId = emotion.id,
                breadcrumb = path.breadcrumb,
                hint = "Can you be more specific?",
                showPostCheckInAction = false,
            )
        }
    }

    private suspend fun onTertiarySelected(emotion: Emotion) {
        val path = buildEmotionPathUseCase(emotion.id)
        openCheckIn(path, emotion.id)
    }

    private suspend fun openCheckIn(path: EmotionPath, emotionId: String) {
        val intervention = evaluateCheckInInterventionUseCase(emotionId, 3)
        _uiState.update {
            it.copy(
                selectedPath = path,
                selectedEmotionId = emotionId,
                breadcrumb = if (emotionId == UNSURE_EMOTION_ID) "" else path.breadcrumb,
                showCheckInSheet = true,
                showPostCheckInAction = false,
                postCheckInIntervention = null,
                checkInIntensity = 3,
                checkInNote = "",
                checkInIntervention = intervention,
            )
        }
    }

    fun onBack() {
        _uiState.update { state ->
            when (state.focusLevel) {
                WheelFocusLevel.TERTIARY -> state.copy(
                    focusLevel = WheelFocusLevel.SECONDARY,
                    focusedSecondaryId = null,
                    selectedEmotionId = null,
                    breadcrumb = state.selectedPath.primary?.label.orEmpty(),
                    hint = state.selectedPath.primary?.let { "What kind of ${it.label.lowercase()}?" }
                        ?: state.hint,
                    showCheckInSheet = false,
                    showPostCheckInAction = false,
                )
                WheelFocusLevel.SECONDARY -> state.copy(
                    focusLevel = WheelFocusLevel.PRIMARY,
                    focusedPrimaryId = null,
                    focusedSecondaryId = null,
                    selectedEmotionId = null,
                    selectedPath = EmotionPath(),
                    breadcrumb = "",
                    hint = "Tap a core feeling to begin",
                    showCheckInSheet = false,
                    showPostCheckInAction = false,
                )
                WheelFocusLevel.PRIMARY -> state
            }
        }
    }

    fun onDismissCheckIn() {
        _uiState.update {
            it.copy(
                showCheckInSheet = false,
            )
        }
    }

    fun onIntensityChanged(value: Int) {
        viewModelScope.launch {
            val intensity = value.coerceIn(1, 5)
            val emotionId = _uiState.value.selectedEmotionId
            val intervention = emotionId?.let {
                evaluateCheckInInterventionUseCase(it, intensity)
            }
            _uiState.update {
                it.copy(
                    checkInIntensity = intensity,
                    checkInIntervention = intervention,
                )
            }
        }
    }

    fun onNoteChanged(value: String) {
        _uiState.update { it.copy(checkInNote = value) }
    }

    fun onSaveCheckIn() {
        viewModelScope.launch {
            saveCheckInAndShowFollowUp(startGrounding = false)
        }
    }

    fun onSaveAndStartGrounding() {
        viewModelScope.launch {
            saveCheckInAndShowFollowUp(startGrounding = true)
        }
    }

    private suspend fun saveCheckInAndShowFollowUp(startGrounding: Boolean) {
        val emotionId = _uiState.value.selectedEmotionId ?: return
        val intensity = _uiState.value.checkInIntensity
        logCheckInUseCase(
            emotionId = emotionId,
            intensity = intensity,
            note = _uiState.value.checkInNote,
        )
        val intervention = evaluateCheckInInterventionUseCase(emotionId, intensity)
        _uiState.update {
            it.copy(
                showCheckInSheet = false,
                checkInSavedPulse = it.checkInSavedPulse + 1,
                showPostCheckInAction = !startGrounding && intervention.type != CheckInInterventionType.NONE,
                postCheckInIntervention = intervention,
                activeBreathingPatternId = intervention.suggestedBreathingPatternId,
                showGroundingExercise = startGrounding &&
                    intervention.type == CheckInInterventionType.HIGH_DISTRESS_GROUNDING,
                hint = when {
                    startGrounding -> "Ground with your breath for one minute."
                    intervention.type != CheckInInterventionType.NONE -> "Saved. One small next step below."
                    else -> "Saved. Tap the wheel to check in again."
                },
            )
        }
        widgetRefreshCoordinator.refreshAll()
    }

    fun onStartGroundingFromPostAction() {
        val patternId = _uiState.value.postCheckInIntervention?.suggestedBreathingPatternId
            ?: BreathingPatternIds.BOX
        _uiState.update {
            it.copy(
                showPostCheckInAction = false,
                showGroundingExercise = true,
                activeBreathingPatternId = patternId,
            )
        }
    }

    fun onBreathingPatternSelected(patternId: String) {
        _uiState.update { it.copy(activeBreathingPatternId = patternId) }
    }

    fun onDismissPostCheckInAction() {
        _uiState.update {
            it.copy(
                showPostCheckInAction = false,
                postCheckInIntervention = null,
            )
        }
    }

    fun onDismissGroundingExercise() {
        _uiState.update { it.copy(showGroundingExercise = false) }
    }

    fun onOpenBreathingExercise() {
        _uiState.update {
            it.copy(
                showGroundingExercise = true,
                activeBreathingPatternId = BreathingPatternIds.QUICK,
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        val trimmed = query.trim()
        _uiState.update {
            it.copy(
                searchQuery = query,
                isSearching = query.isNotBlank(),
                searchPending = trimmed.length >= 2,
            )
        }
        if (trimmed.length < 2) {
            _uiState.update { it.copy(searchResults = emptyList(), searchPending = false) }
            return
        }
        viewModelScope.launch {
            val results = searchEmotionsUseCase(query)
            _uiState.update { it.copy(searchResults = results, searchPending = false) }
        }
    }

    fun onSearchResultSelected(emotion: Emotion) {
        viewModelScope.launch {
            val path = buildEmotionPathUseCase(emotion.id)
            _uiState.update {
                it.copy(
                    searchQuery = "",
                    searchResults = emptyList(),
                    isSearching = false,
                )
            }
            openCheckIn(path, emotion.id)
        }
    }

    fun onClearSearch() {
        _uiState.update {
            it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false)
        }
    }

    companion object {
        const val UNSURE_EMOTION_ID = "unsure"
    }
}
