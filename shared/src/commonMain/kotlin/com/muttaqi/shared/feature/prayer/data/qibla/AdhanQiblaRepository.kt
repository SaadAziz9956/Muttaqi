package com.muttaqi.shared.feature.prayer.data.qibla

import com.batoulapps.adhan2.Qibla
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.domain.repository.QiblaRepository
import com.batoulapps.adhan2.Coordinates as AdhanCoordinates

/** The Qibla by Adhan's great-circle bearing, and the distance to the Kaaba as Core Location measured it */
class AdhanQiblaRepository : QiblaRepository {
    override fun qiblaDirection(from: Coordinates) = QiblaDirection(
        bearing = Qibla(AdhanCoordinates(from.latitude, from.longitude)).direction,
        distanceInKilometers = Wgs84Distance.meters(from, Kaaba) / 1000,
    )

    private companion object {
        /** Where the Swift app measured the distance to, which differs slightly from Adhan's own point in Makkah */
        val Kaaba = Coordinates(latitude = 21.4225, longitude = 39.8262)
    }
}
