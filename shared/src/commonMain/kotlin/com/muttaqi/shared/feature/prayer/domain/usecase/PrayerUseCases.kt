package com.muttaqi.shared.feature.prayer.domain.usecase

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import com.muttaqi.shared.feature.prayer.domain.repository.PrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.repository.QiblaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

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

class RequestLocationAccess(private val location: LocationRepository) {
    suspend operator fun invoke(): LocationAccess = location.requestAccess()
}

class FollowHeading(private val compass: Compass) {
    val isAvailable: Boolean get() = compass.isAvailable

    operator fun invoke(): Flow<CompassHeading> = if (compass.isAvailable) compass.headings() else emptyFlow()
}
