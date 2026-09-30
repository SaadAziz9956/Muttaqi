package com.muttaqi.shared.feature.prayer.presentation.qibla

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading

/** The shortest turn from one angle to another, in degrees from -180 to 180: positive is clockwise */
internal fun shortestTurn(from: Double, to: Double): Double {
    val delta = (to - from).rem(360.0)
    return when {
        delta > 180 -> delta - 360
        delta < -180 -> delta + 360
        else -> delta
    }
}

/**
 * The dial's rotation as the compass moves: it starts at the first heading and then turns by the shortest step to each
 * next one, so it can pass 360 and the dial never spins the long way round
 */
internal data class DialReading(val heading: CompassHeading, val rotation: Double) {
    fun next(heading: CompassHeading) = DialReading(heading, rotation + shortestTurn(this.heading.degrees, heading.degrees))

    fun toCompass(qiblaBearing: Double) = QiblaCompass(heading, rotation, shortestTurn(heading.degrees, qiblaBearing))

    companion object {
        fun first(heading: CompassHeading) = DialReading(heading, heading.degrees)
    }
}
