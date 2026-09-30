package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.TimeZone
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock

/**
 * Home's prayer times, Qibla and location for Swift, while Home is still in Swift, e.g.
 * `HomePrayerTimes.shared.schedule(coordinates: saved)`
 */
object HomePrayerTimes : KoinComponent {
    private val location: LocationRepository get() = get()
    private val compass: Compass get() = get()

    val access: LocationAccess get() = location.access

    suspend fun requestAccess(): LocationAccess = location.requestAccess()

    fun lastKnownCoordinates(): Coordinates? = location.lastKnownCoordinates()

    suspend fun refreshCoordinates(): Coordinates? = location.refreshCoordinates()

    /** Today's and tomorrow's times where the reader is, in the device's time zone */
    fun schedule(coordinates: Coordinates): PrayerSchedule? =
        get<GetPrayerSchedule>()(coordinates, Clock.System.now(), TimeZone.currentSystemDefault())

    fun qibla(coordinates: Coordinates): QiblaDirection = get<GetQiblaDirection>()(coordinates)

    val isCompassAvailable: Boolean get() = compass.isAvailable

    fun headings(): Flow<CompassHeading> = compass.headings()
}
