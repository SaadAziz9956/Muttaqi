package com.muttaqi.shared.feature.prayer.domain.usecase

import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import com.muttaqi.shared.feature.prayer.domain.repository.PrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.repository.QiblaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/** Today's and tomorrow's prayer times where the reader is, the days counted in their time zone */
class GetPrayerSchedule(private val repository: PrayerTimesRepository) {
    operator fun invoke(coordinates: Coordinates, now: Instant, timeZone: TimeZone): PrayerSchedule? {
        val today = now.toLocalDateTime(timeZone).date
        return PrayerSchedule(
            today = repository.prayerTimes(today, coordinates) ?: return null,
            tomorrow = repository.prayerTimes(today.plus(1, DateTimeUnit.DAY), coordinates) ?: return null,
        )
    }
}

class GetQiblaDirection(private val repository: QiblaRepository) {
    operator fun invoke(coordinates: Coordinates): QiblaDirection = repository.qiblaDirection(coordinates)
}

/**
 * Where the reader is: the saved coordinates straight away, which work offline, then a fresh fix when there's access,
 * which corrects them if the reader has moved
 */
class LocateReader(private val location: LocationRepository) {
    operator fun invoke(): Flow<Coordinates> = flow {
        location.lastKnownCoordinates()?.let { emit(it) }
        if (location.access == LocationAccess.Granted) {
            location.refreshCoordinates()?.let { emit(it) }
        }
    }
}

class GetLocationAccess(private val location: LocationRepository) {
    operator fun invoke(): LocationAccess = location.access
}

/** Shows the system's request for location access while it's undecided, then gives the access there is */
class RequestLocationAccess(private val location: LocationRepository) {
    suspend operator fun invoke(): LocationAccess = location.requestAccess()
}
