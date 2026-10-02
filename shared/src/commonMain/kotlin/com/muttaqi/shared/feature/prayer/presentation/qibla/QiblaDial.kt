package com.muttaqi.shared.feature.prayer.presentation.qibla

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading

internal fun shortestTurn(from: Double, to: Double): Double {
    val delta = (to - from).rem(360.0)
    return when {
        delta > 180 -> delta - 360
        delta < -180 -> delta + 360
        else -> delta
    }
}

internal data class DialReading(val heading: CompassHeading, val rotation: Double) {
    fun next(heading: CompassHeading) = DialReading(heading, rotation + shortestTurn(this.heading.degrees, heading.degrees))

    fun toCompass(qiblaBearing: Double) = QiblaCompass(heading, rotation, shortestTurn(heading.degrees, qiblaBearing))

    companion object {
        fun first(heading: CompassHeading) = DialReading(heading, heading.degrees)
    }
}
