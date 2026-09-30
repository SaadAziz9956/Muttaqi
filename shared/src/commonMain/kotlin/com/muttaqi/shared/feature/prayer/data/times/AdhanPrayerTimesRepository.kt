package com.muttaqi.shared.feature.prayer.data.times

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.DailyPrayerTimes
import com.muttaqi.shared.feature.prayer.domain.repository.PrayerTimesRepository
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import com.batoulapps.adhan2.Coordinates as AdhanCoordinates

/**
 * Prayer times calculated on the device with Adhan, so they work offline. The parameters are the ones the Swift app
 * has always used, so the times don't move by a minute: the University of Islamic Sciences, Karachi method (18° for
 * Fajr and Isha, Dhuhr a minute after noon) with Hanafi Asr, rounded to the nearest minute, and the high-latitude rule
 * Adhan recommends for the place (a seventh of the night above 48°, the middle of the night below)
 */
class AdhanPrayerTimesRepository(
    private val parameters: CalculationParameters = KarachiHanafi,
) : PrayerTimesRepository {

    override fun prayerTimes(date: LocalDate, coordinates: Coordinates): DailyPrayerTimes? {
        val times = try {
            PrayerTimes(
                AdhanCoordinates(coordinates.latitude, coordinates.longitude),
                DateComponents(date.year, date.month.number, date.day),
                parameters,
            )
        } catch (_: IllegalStateException) {
            // Adhan throws where the Swift version gave nil, e.g. where the sun doesn't rise or set that day
            return null
        }
        return DailyPrayerTimes(
            fajr = times.fajr,
            sunrise = times.sunrise,
            dhuhr = times.dhuhr,
            asr = times.asr,
            maghrib = times.maghrib,
            isha = times.isha,
        )
    }

    companion object {
        val KarachiHanafi: CalculationParameters = CalculationMethod.KARACHI.parameters.copy(madhab = Madhab.HANAFI)
    }
}
