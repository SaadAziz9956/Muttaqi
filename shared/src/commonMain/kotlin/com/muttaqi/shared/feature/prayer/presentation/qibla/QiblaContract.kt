package com.muttaqi.shared.feature.prayer.presentation.qibla

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import kotlin.math.abs

data class QiblaState(
    val phase: QiblaPhase = QiblaPhase.Locating,
    val isCompassAvailable: Boolean = true,
) : UiState {
    val qibla: QiblaDirection? get() = (phase as? QiblaPhase.Ready)?.qibla
}

sealed interface QiblaPhase {
    data object Locating : QiblaPhase
    data class NeedsLocation(val access: LocationAccess) : QiblaPhase
    data class Ready(val qibla: QiblaDirection) : QiblaPhase
}

sealed interface QiblaIntent : UiIntent {
    data object LocationButtonTapped : QiblaIntent
}

sealed interface QiblaMutation : UiMutation {
    data class Located(val qibla: QiblaDirection) : QiblaMutation
    data class LocationNeeded(val access: LocationAccess) : QiblaMutation
}

sealed interface QiblaEffect : UiEffect {
    data object OpenSettings : QiblaEffect
    data object FacingQibla : QiblaEffect
}

data class QiblaCompass(
    val heading: CompassHeading,
    val dialRotation: Double,
    val turnAngle: Double,
) {
    val isAligned: Boolean get() = abs(turnAngle) <= ALIGNED_WITHIN

    val needsCalibration: Boolean get() = heading.accuracy < 0 || heading.accuracy > CALIBRATED_WITHIN

    private companion object {
        const val ALIGNED_WITHIN = 3.0
        const val CALIBRATED_WITHIN = 25.0
    }
}
