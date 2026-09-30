package com.muttaqi.shared.feature.prayer.domain.repository

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

/** The day's prayer times, calculated on the device so they work offline */
fun interface PrayerTimesRepository {
    /** Null where they can't be calculated, e.g. near the poles when the sun doesn't set */
    fun prayerTimes(date: LocalDate, coordinates: Coordinates): DailyPrayerTimes?
}

fun interface QiblaRepository {
    fun qiblaDirection(from: Coordinates): QiblaDirection
}

/** Where the reader is */
interface LocationRepository {
    val access: LocationAccess

    suspend fun requestAccess(): LocationAccess

    /** The last saved fix, available immediately and offline */
    fun lastKnownCoordinates(): Coordinates?

    /** Asks for a fresh fix and saves it; null when access is missing or no fix arrives in time */
    suspend fun refreshCoordinates(): Coordinates?
}

/** The phone's heading as it turns */
interface Compass {
    val isAvailable: Boolean

    /** Each change of heading while collected; the compass runs only while something collects it */
    fun headings(): Flow<CompassHeading>
}
