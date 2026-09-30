package com.muttaqi.shared.feature.home.domain.usecase

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.feature.home.domain.model.DailyContent
import com.muttaqi.shared.feature.home.domain.model.LastReading
import com.muttaqi.shared.feature.home.domain.model.PrayerTimesHere
import com.muttaqi.shared.feature.home.domain.model.QuranShortcuts
import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyahOfTheDay
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * The day's texts from the other features: the verse under the greeting, and the ayah, hadith, dua, Name and Explore
 * topic of the day, each picked as the iOS app always has. One that can't be read is left out rather than keeping the
 * rest off Home
 */
class GetDailyContent(
    private val getAyah: GetAyah,
    private val getAyahOfTheDay: GetAyahOfTheDay,
    private val getHadithOfTheDay: GetHadithOfTheDay,
    private val getDuaOfTheDay: GetDuaOfTheDay,
    private val getNameOfTheDay: GetNameOfTheDay,
    private val getTopicOfTheDay: GetTopicOfTheDay,
) {
    suspend operator fun invoke(date: LocalDate, language: Language): DailyContent = coroutineScope {
        val greeting = async { orNull { getAyah(GREETING.first, GREETING.second, language) } }
        val ayah = async { orNull { getAyahOfTheDay(date, language) } }
        val hadith = async { orNull { getHadithOfTheDay(date, language) } }
        val dua = async { orNull { getDuaOfTheDay(date, language) } }
        val name = async { orNull { getNameOfTheDay(date, language) } }
        val topic = async { orNull { getTopicOfTheDay(date, language) } }
        DailyContent(greeting.await(), ayah.await(), hadith.await(), dua.await(), name.await(), topic.await())
    }

    private companion object {
        /** "So do not weaken and do not grieve…" */
        val GREETING = 3 to 139
    }
}

/** Where the reader left off in the Quran, and Surah al-Kahf, from the stored surahs; none before they're downloaded */
class GetQuranShortcuts(
    private val getSurahs: GetSurahs,
    private val getLastReading: GetLastReading,
) {
    suspend operator fun invoke(): QuranShortcuts {
        val surahs = orNull { getSurahs() }.orEmpty()
        val progress = orNull { getLastReading() }
        return QuranShortcuts(
            lastReading = progress?.let { last ->
                surahs.firstOrNull { it.number == last.surahNumber }?.let { LastReading(it, last.lastAyahNumber) }
            },
            kahf = surahs.firstOrNull { it.number == AL_KAHF },
        )
    }

    private companion object {
        const val AL_KAHF = 18
    }
}

/**
 * The prayer times and the Qibla where the reader is: from the saved location straight away, which works offline,
 * then again from a fresh fix when there's access, in case they've moved
 */
class LocatePrayerTimes(
    private val locateReader: LocateReader,
    private val getPrayerSchedule: GetPrayerSchedule,
    private val getQiblaDirection: GetQiblaDirection,
    private val clock: ReaderClock,
) {
    operator fun invoke(): Flow<PrayerTimesHere> = locateReader().map { coordinates ->
        PrayerTimesHere(getPrayerSchedule(coordinates, clock.now(), clock.timeZone), getQiblaDirection(coordinates))
    }
}

/** Today's Hijri date where the reader is. The Islamic day begins at Maghrib, so after sunset it's the next one's */
class GetHijriDate(private val calendar: HijriCalendar) {
    operator fun invoke(now: Instant, maghrib: Instant?, timeZone: TimeZone): String {
        val today = now.toLocalDateTime(timeZone).date
        val afterMaghrib = maghrib != null && maghrib.toLocalDateTime(timeZone).date == today && now >= maghrib
        return calendar.format(if (afterMaghrib) today.plus(1, DateTimeUnit.DAY) else today)
    }
}

private suspend fun <T> orNull(block: suspend () -> T?): T? = try {
    block()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    null
}
