package com.muttaqi.shared.feature.prayer.domain.repository

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

fun interface PrayerTimesRepository {
    fun prayerTimes(date: LocalDate, coordinates: Coordinates): DailyPrayerTimes?
}

fun interface QiblaRepository {
    fun qiblaDirection(from: Coordinates): QiblaDirection
}

interface LocationRepository {
    val access: LocationAccess

    suspend fun requestAccess(): LocationAccess

    fun lastKnownCoordinates(): Coordinates?

    suspend fun refreshCoordinates(): Coordinates?
}

interface Compass {
    val isAvailable: Boolean

    fun headings(): Flow<CompassHeading>
}
