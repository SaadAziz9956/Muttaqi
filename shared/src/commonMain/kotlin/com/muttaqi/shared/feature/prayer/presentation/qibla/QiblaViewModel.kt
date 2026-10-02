package com.muttaqi.shared.feature.prayer.presentation.qibla

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal object QiblaReducer : Reducer<QiblaState, QiblaMutation> {
    override fun reduce(state: QiblaState, mutation: QiblaMutation) = when (mutation) {
        is QiblaMutation.Located -> state.copy(phase = QiblaPhase.Ready(mutation.qibla))
        is QiblaMutation.LocationNeeded -> state.copy(phase = QiblaPhase.NeedsLocation(mutation.access))
    }
}

class QiblaViewModel(
    private val locateReader: LocateReader,
    private val getLocationAccess: GetLocationAccess,
    private val requestLocationAccess: RequestLocationAccess,
    private val getQiblaDirection: GetQiblaDirection,
    private val deviceCompass: Compass,
) : MviViewModel<QiblaState, QiblaIntent, QiblaMutation, QiblaEffect>(
    QiblaState(isCompassAvailable = deviceCompass.isAvailable),
    QiblaReducer,
) {
    private var wasAligned = false

    private val qiblaBearing = state.map { it.qibla?.bearing }.distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    val compass: StateFlow<QiblaCompass?> = qiblaBearing
        .map { it != null && deviceCompass.isAvailable }
        .distinctUntilChanged()
        .flatMapLatest { located -> if (located) dialReadings() else flowOf(null) }
        .combine(qiblaBearing) { reading, bearing -> if (reading != null && bearing != null) reading.toCompass(bearing) else null }
        .onEach { compass ->
            val aligned = compass?.isAligned == true
            if (aligned && !wasAligned) emit(QiblaEffect.FacingQibla)
            wasAligned = aligned
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        launchNow { locate() }
    }

    override fun handle(intent: QiblaIntent) {
        when (intent) {
            QiblaIntent.LocationButtonTapped -> {
                val phase = state.value.phase as? QiblaPhase.NeedsLocation ?: return
                if (phase.access == LocationAccess.Denied) {
                    emit(QiblaEffect.OpenSettings)
                } else {
                    viewModelScope.launch {
                        requestLocationAccess()
                        locate()
                    }
                }
            }
        }
    }

    private suspend fun locate() {
        var located = false
        locateReader().collect { coordinates ->
            located = true
            mutate(QiblaMutation.Located(getQiblaDirection(coordinates)))
        }
        if (!located) mutate(QiblaMutation.LocationNeeded(getLocationAccess()))
    }

    private fun dialReadings() = deviceCompass.headings()
        .runningFold<_, DialReading?>(null) { reading, heading -> reading?.next(heading) ?: DialReading.first(heading) }
}
