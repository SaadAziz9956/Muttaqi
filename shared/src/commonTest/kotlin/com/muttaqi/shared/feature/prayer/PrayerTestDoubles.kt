package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.prayer.data.location.SavedCoordinates
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingUpdates
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.muttaqi.shared.feature.prayer.domain.platform.LocationRequest
import com.russhwolf.settings.MapSettings

object PrayerTestData {
    val karachi = Coordinates(24.8607, 67.0011)
    val lahore = Coordinates(31.5204, 74.3587)
}

/** The device's location services, answering at once unless [holdFix] */
class FakeLocationProvider(
    override var access: LocationAccess = LocationAccess.NotDetermined,
    /** What the system prompt answers */
    var answer: LocationAccess = LocationAccess.Granted,
    var fix: Coordinates? = PrayerTestData.karachi,
) : LocationProvider {
    var accessRequests = 0
    var fixRequests = 0
    var cancelledRequests = 0
    /** Never reports a fix, like a phone indoors */
    var holdFix = false

    override fun requestAccess(onResult: (LocationAccess) -> Unit) {
        accessRequests++
        if (access == LocationAccess.NotDetermined) access = answer
        onResult(access)
    }

    override fun currentLocation(onResult: (Coordinates?) -> Unit): LocationRequest {
        fixRequests++
        if (!holdFix) onResult(if (access == LocationAccess.Granted) fix else null)
        return object : LocationRequest {
            override fun cancel() {
                cancelledRequests++
            }
        }
    }
}

/** A compass the test turns by hand with [turnTo] */
class FakeHeadingProvider(override val isAvailable: Boolean = true) : HeadingProvider {
    private val listeners = mutableListOf<(CompassHeading) -> Unit>()
    val isRunning: Boolean get() = listeners.isNotEmpty()

    override fun startUpdates(onHeading: (CompassHeading) -> Unit): HeadingUpdates {
        listeners += onHeading
        return object : HeadingUpdates {
            override fun stop() {
                listeners -= onHeading
            }
        }
    }

    fun turnTo(degrees: Double, accuracy: Double = 5.0) = listeners.toList().forEach { it(CompassHeading(degrees, accuracy)) }
}

fun savedCoordinates(settings: MapSettings = MapSettings(), legacy: Map<String, String> = emptyMap()) =
    SavedCoordinates(settings, LegacyDataSource { legacy[it] })
