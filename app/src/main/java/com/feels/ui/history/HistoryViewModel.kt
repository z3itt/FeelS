package com.feels.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feels.core.domain.model.CheckIn
import com.feels.core.domain.model.Emotion
import com.feels.core.domain.model.EmotionPathBuilder
import com.feels.core.domain.model.EmotionTier
import com.feels.core.domain.model.TriggerInsight
import com.feels.core.domain.repository.CheckInRepository
import com.feels.core.domain.repository.EmotionRepository
import com.feels.core.domain.usecase.DeleteCheckInUseCase
import com.feels.core.domain.usecase.GetEmotionalHeatmapUseCase
import com.feels.core.domain.usecase.GetTriggerInsightsUseCase
import com.feels.core.domain.usecase.UpdateCheckInUseCase
import com.feels.core.domain.util.LocalDayClock
import com.feels.widget.WidgetRefreshCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class HistoryItemUi(
    val id: Long,
    val emotionLabel: String,
    val emotionPath: String,
    val intensity: Int,
    val note: String?,
    val timeLabel: String,
)

data class PrimaryTrendUi(
    val primaryId: String,
    val label: String,
    val colorHex: String,
    val count: Int,
    val fraction: Float,
)

data class TriggerInsightUi(
    val emotionLabel: String,
    val emotionPath: String,
    val peakIntensity: Int,
    val occurrenceCount: Int,
    val topKeywords: List<String>,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    checkInRepository: CheckInRepository,
    private val emotionRepository: EmotionRepository,
    getEmotionalHeatmapUseCase: GetEmotionalHeatmapUseCase,
    getTriggerInsightsUseCase: GetTriggerInsightsUseCase,
    private val updateCheckInUseCase: UpdateCheckInUseCase,
    private val deleteCheckInUseCase: DeleteCheckInUseCase,
    private val widgetRefreshCoordinator: WidgetRefreshCoordinator,
) : ViewModel() {

    private val formatter = SimpleDateFormat("MMM d · h:mm a", Locale.ENGLISH)
    private val _loadError = MutableStateFlow(false)
    val loadError: StateFlow<Boolean> = _loadError.asStateFlow()

    init {
        viewModelScope.launch {
            emotionRepository.observeAllEmotions()
                .catch { _loadError.value = true }
                .collect { _loadError.value = false }
        }
    }

    fun retryLoad() {
        viewModelScope.launch {
            try {
                emotionRepository.observeAllEmotions().first()
                _loadError.value = false
            } catch (_: Exception) {
                _loadError.value = true
            }
        }
    }

    private val combined: Flow<Pair<List<CheckIn>, List<Emotion>>> = combine(
        checkInRepository.observeRecentCheckIns(50),
        emotionRepository.observeAllEmotions(),
    ) { checkIns, emotions -> checkIns to emotions }

    private val trendWindow: Flow<Pair<List<CheckIn>, List<Emotion>>> = combine(
        checkInRepository.observeCheckInsSince(LocalDayClock.startOfDayDaysAgo(29)),
        emotionRepository.observeAllEmotions(),
    ) { checkIns, emotions -> checkIns to emotions }

    val items: StateFlow<List<HistoryItemUi>> = combined
        .map { (checkIns, emotions) ->
            checkIns.map { checkIn ->
                val path = EmotionPathBuilder.build(checkIn.emotionId, emotions)
                HistoryItemUi(
                    id = checkIn.id,
                    emotionLabel = path.deepest?.label ?: checkIn.emotionId,
                    emotionPath = path.breadcrumb,
                    intensity = checkIn.intensity,
                    note = checkIn.note,
                    timeLabel = formatter.format(Date(checkIn.timestampMillis)),
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val primaryTrends: StateFlow<List<PrimaryTrendUi>> = trendWindow
        .map { (checkIns, emotions) ->
            if (checkIns.isEmpty()) return@map emptyList()

            val primaries = emotions.filter { it.tier == EmotionTier.PRIMARY }.associateBy { it.id }
            val counts = mutableMapOf<String, Int>()

            checkIns.forEach { checkIn ->
                val path = EmotionPathBuilder.build(checkIn.emotionId, emotions)
                val primaryId = path.primary?.id ?: return@forEach
                counts[primaryId] = (counts[primaryId] ?: 0) + 1
            }

            val maxCount = counts.values.maxOrNull()?.coerceAtLeast(1) ?: 1
            counts.entries
                .sortedByDescending { it.value }
                .mapNotNull { (primaryId, count) ->
                    val primary = primaries[primaryId] ?: return@mapNotNull null
                    PrimaryTrendUi(
                        primaryId = primaryId,
                        label = primary.label,
                        colorHex = primary.colorHex,
                        count = count,
                        fraction = count.toFloat() / maxCount,
                    )
                }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val heatmap = getEmotionalHeatmapUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val triggerInsight: StateFlow<TriggerInsightUi?> = getTriggerInsightsUseCase()
        .map { insight -> insight?.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateCheckIn(id: Long, intensity: Int, note: String?) {
        viewModelScope.launch {
            updateCheckInUseCase(id, intensity, note)
            widgetRefreshCoordinator.refreshAll()
        }
    }

    fun deleteCheckIn(id: Long) {
        viewModelScope.launch {
            deleteCheckInUseCase(id)
            widgetRefreshCoordinator.refreshAll()
        }
    }
}

private fun TriggerInsight.toUi() = TriggerInsightUi(
    emotionLabel = emotionLabel,
    emotionPath = emotionPath,
    peakIntensity = peakIntensity,
    occurrenceCount = occurrenceCount,
    topKeywords = topKeywords,
)
