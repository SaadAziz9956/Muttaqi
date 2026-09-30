package com.muttaqi.shared.feature.prayer.data.qibla

import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * The distance along the Earth's surface in metres, on the WGS-84 ellipsoid by Vincenty's inverse formula. It's what
 * Core Location's `CLLocation.distance(from:)` gives, to the millimetre, so the distance to Makkah reads as it did in
 * the Swift app
 */
internal object Wgs84Distance {
    private const val A = 6_378_137.0
    private const val F = 1 / 298.257223563
    private const val B = A * (1 - F)

    fun meters(from: Coordinates, to: Coordinates): Double {
        val l = (to.longitude - from.longitude).radians
        val u1 = atan((1 - F) * tan(from.latitude.radians))
        val u2 = atan((1 - F) * tan(to.latitude.radians))
        val sinU1 = sin(u1)
        val cosU1 = cos(u1)
        val sinU2 = sin(u2)
        val cosU2 = cos(u2)

        var lambda = l
        var sinSigma: Double
        var cosSigma: Double
        var sigma: Double
        var cos2Alpha: Double
        var cos2SigmaM: Double
        var iterations = 0
        do {
            val sinLambda = sin(lambda)
            val cosLambda = cos(lambda)
            sinSigma = sqrt((cosU2 * sinLambda).squared + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda).squared)
            if (sinSigma == 0.0) return 0.0 // the same point
            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda
            sigma = atan2(sinSigma, cosSigma)
            val sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma
            cos2Alpha = 1 - sinAlpha * sinAlpha
            // On the equator there's no latitude to reduce
            cos2SigmaM = if (cos2Alpha != 0.0) cosSigma - 2 * sinU1 * sinU2 / cos2Alpha else 0.0
            val c = F / 16 * cos2Alpha * (4 + F * (4 - 3 * cos2Alpha))
            val previous = lambda
            lambda = l + (1 - c) * F * sinAlpha *
                (sigma + c * sinSigma * (cos2SigmaM + c * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)))
        } while (abs(lambda - previous) > 1e-12 && ++iterations < 200)

        val u2Squared = cos2Alpha * (A * A - B * B) / (B * B)
        val bigA = 1 + u2Squared / 16384 * (4096 + u2Squared * (-768 + u2Squared * (320 - 175 * u2Squared)))
        val bigB = u2Squared / 1024 * (256 + u2Squared * (-128 + u2Squared * (74 - 47 * u2Squared)))
        val deltaSigma = bigB * sinSigma * (
            cos2SigmaM + bigB / 4 * (
                cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) -
                    bigB / 6 * cos2SigmaM * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)
                )
            )
        return B * bigA * (sigma - deltaSigma)
    }

    private val Double.radians get() = this * PI / 180
    private val Double.squared get() = this * this
}
