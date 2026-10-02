package com.muttaqi.shared.feature.prayer.domain.model

data class QiblaDirection(
    val bearing: Double,
    val distanceInKilometers: Double,
)

data class CompassHeading(
    val degrees: Double,
    val accuracy: Double,
)
