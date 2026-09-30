package com.muttaqi.shared.feature.prayer.domain.platform

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess

// The device services prayer times and the Qibla need, implemented natively by each app: Core Location in Swift on
// iOS, LocationManager and the rotation-vector sensor on Android. They report through callbacks rather than suspend
// functions and flows, so Swift classes can implement them; the data layer turns them into coroutines.

/** The device's location services */
interface LocationProvider {
    val access: LocationAccess

    /** Asks for access while it's undecided, then reports the access there is, once */
    fun requestAccess(onResult: (LocationAccess) -> Unit)

    /** Looks for one current fix and reports it once, or null without access; [LocationRequest.cancel] stops looking */
    fun currentLocation(onResult: (Coordinates?) -> Unit): LocationRequest
}

/** A search for the device's location that's still running */
interface LocationRequest {
    fun cancel()
}

/** The compass */
interface HeadingProvider {
    val isAvailable: Boolean

    /** Reports the heading each time it changes, until [HeadingUpdates.stop] */
    fun startUpdates(onHeading: (CompassHeading) -> Unit): HeadingUpdates
}

/** Compass updates that are running */
interface HeadingUpdates {
    fun stop()
}
