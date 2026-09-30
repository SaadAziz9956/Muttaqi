package com.muttaqi.shared.feature.prayer.domain.model

data class QiblaDirection(
    /** Bearing to the Kaaba in degrees clockwise from true north */
    val bearing: Double,
    val distanceInKilometers: Double,
)

data class CompassHeading(
    /** Degrees clockwise from true north (magnetic north when true north isn't available) */
    val degrees: Double,
    /** Error estimate in degrees; negative when the compass needs calibrating */
    val accuracy: Double,
)
