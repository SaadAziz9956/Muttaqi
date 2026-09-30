package com.muttaqi.shared.feature.prayer.presentation.qibla

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import kotlin.math.abs

/**
 * The Qibla: where the Kaaba is from here, found from the reader's location. The compass reading changes many times a
 * second, so it has its own flow ([QiblaViewModel.compass]) rather than living here
 */
data class QiblaState(
    val phase: QiblaPhase = QiblaPhase.Locating,
    val isCompassAvailable: Boolean = true,
) : UiState {
    val qibla: QiblaDirection? get() = (phase as? QiblaPhase.Ready)?.qibla
}

sealed interface QiblaPhase {
    data object Locating : QiblaPhase
    /** No location yet; with [LocationAccess.Denied] only the system settings can give one */
    data class NeedsLocation(val access: LocationAccess) : QiblaPhase
    data class Ready(val qibla: QiblaDirection) : QiblaPhase
}

sealed interface QiblaIntent : UiIntent {
    /** The button under "Location needed": asks for access, or opens the settings once it's been refused */
    data object LocationButtonTapped : QiblaIntent
}

sealed interface QiblaMutation : UiMutation {
    data class Located(val qibla: QiblaDirection) : QiblaMutation
    data class LocationNeeded(val access: LocationAccess) : QiblaMutation
}

sealed interface QiblaEffect : UiEffect {
    data object OpenSettings : QiblaEffect
    /** The phone has just turned to face the Qibla; each app plays its success haptic */
    data object FacingQibla : QiblaEffect
}

/** The compass as the Qibla dial draws it, from one heading */
data class QiblaCompass(
    val heading: CompassHeading,
    /** The heading as a continuous angle (it can pass 360), so turning past north animates the short way round */
    val dialRotation: Double,
    /** Degrees to turn to face the Kaaba: positive means turn right */
    val turnAngle: Double,
) {
    val isAligned: Boolean get() = abs(turnAngle) <= ALIGNED_WITHIN

    val needsCalibration: Boolean get() = heading.accuracy < 0 || heading.accuracy > CALIBRATED_WITHIN

    private companion object {
        const val ALIGNED_WITHIN = 3.0
        const val CALIBRATED_WITHIN = 25.0
    }
}
