package com.muttaqi.shared.feature.prayer.domain.platform

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess

interface LocationProvider {
    val access: LocationAccess

    fun requestAccess(onResult: (LocationAccess) -> Unit)

    fun currentLocation(onResult: (Coordinates?) -> Unit): LocationRequest
}

interface LocationRequest {
    fun cancel()
}

interface HeadingProvider {
    val isAvailable: Boolean

    fun startUpdates(onHeading: (CompassHeading) -> Unit): HeadingUpdates
}

interface HeadingUpdates {
    fun stop()
}
