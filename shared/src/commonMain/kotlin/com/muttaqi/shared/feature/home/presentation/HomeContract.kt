package com.muttaqi.shared.feature.home.presentation

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.home.domain.model.DailyContent
import com.muttaqi.shared.feature.home.domain.model.LastReading
import com.muttaqi.shared.feature.home.domain.model.QuranShortcuts
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.model.Prayer
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.prayer.domain.model.UpcomingPrayer
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

/** Whether Home has prayer times, and if not, why */
enum class HomeLocation {
    /** Allowed, but no fix has come yet */
    Unknown,

    /** Not asked yet, so the pill asks */
    NeedsPermission,

    /** Refused; only the system settings can change it */
    Denied,
    Available,
}

/** The daily cards, which can each be shared as a card or copied */
enum class DailyCard { Ayah, Hadith, Dua }

/**
 * Home: the Hijri date and prayer times, the greeting's verse, the bento tiles that each show something live, and the
 * day's ayah, hadith and dua. What depends on the time is worked out from [now], which moves on each minute; the
 * compass has its own flow ([HomeViewModel.qiblaArrow]), as it changes many times a second
 */
data class HomeState(
    val now: Instant = Instant.DISTANT_PAST,
    /** The reader's date at [now] */
    val today: LocalDate? = null,
    /** e.g. "Rabiʻ II 19, 1448 AH"; after Maghrib, the next day's */
    val hijriDate: String = "",
    val schedule: PrayerSchedule? = null,
    val location: HomeLocation = HomeLocation.Unknown,
    val qibla: QiblaDirection? = null,
    val isCompassAvailable: Boolean = true,
    /** Quran 3:139, under the greeting */
    val greeting: DailyAyah? = null,
    val ayahOfTheDay: DailyAyah? = null,
    val hadithOfTheDay: HadithPassage? = null,
    val duaOfTheDay: QuranicDua? = null,
    val nameOfTheDay: AllahName? = null,
    val topicOfTheDay: ExploreTopic? = null,
    /** Every dhikr said today, all counters together */
    val dhikrToday: Int = 0,
    /** Today's journal entry, if one has been written */
    val journalToday: JournalEntry? = null,
    val lastReading: LastReading? = null,
    val kahf: Surah? = null,
) : UiState {
    /** The next prayer, which after Isha is tomorrow's Fajr */
    val nextPrayer: UpcomingPrayer? get() = schedule?.nextPrayer(now)

    /** The next prayer when it's one of today's, picked out in the strip; after Isha none of today's is */
    val nextPrayerToday: Prayer?
        get() = nextPrayer?.takeIf { schedule?.today?.time(it.prayer) == it.time }?.prayer

    /** Without a location there are no times, so the pill becomes the way to set one */
    val asksForLocation: Boolean
        get() = nextPrayer == null && (location == HomeLocation.NeedsPermission || location == HomeLocation.Denied)

    /** Reading al-Kahf on Friday is a sunnah, so on Fridays it's a tap away, unless it's the surah being continued */
    val fridayKahf: Surah?
        get() = kahf?.takeIf { today?.dayOfWeek == DayOfWeek.FRIDAY && lastReading?.surah?.number != it.number }
}

sealed interface HomeIntent : UiIntent {
    /** Home came into view, or the app came back to the foreground: the day's content, counts and times again */
    data object Refresh : HomeIntent

    /** The pill without prayer times: asks for location, or opens the settings once it's been refused */
    data object SetLocationTapped : HomeIntent
    data object QiblaTapped : HomeIntent
    data object DhikrTapped : HomeIntent
    data object NameTapped : HomeIntent

    /** The Journal tile: every entry */
    data object JournalTapped : HomeIntent

    /** The Journal tile's round button: today's entry, or a new one */
    data object TodaysEntryTapped : HomeIntent
    data object EmotionsTapped : HomeIntent
    data object TopicTapped : HomeIntent
    data object ContinueReadingTapped : HomeIntent
    data object KahfTapped : HomeIntent
    data object AyahOfTheDayTapped : HomeIntent
    data class ShareTapped(val card: DailyCard) : HomeIntent
    data class CopyTapped(val card: DailyCard) : HomeIntent
}

sealed interface HomeMutation : UiMutation {
    data class ClockTicked(val now: Instant, val today: LocalDate, val hijriDate: String) : HomeMutation
    data class ContentLoaded(val content: DailyContent) : HomeMutation
    data class DhikrCounted(val total: Int) : HomeMutation
    data class JournalLoaded(val entry: JournalEntry?) : HomeMutation
    data class ShortcutsLoaded(val shortcuts: QuranShortcuts) : HomeMutation
    data class Located(val schedule: PrayerSchedule?, val qibla: QiblaDirection) : HomeMutation

    /** Every place there is has been tried; [access] says why there are no times, if there are none */
    data class LocationChecked(val access: LocationAccess) : HomeMutation
}

sealed interface HomeEffect : UiEffect {
    data object OpenQibla : HomeEffect
    data object OpenDhikr : HomeEffect
    data object OpenNames : HomeEffect
    data object OpenJournal : HomeEffect

    /** Today's entry to read or edit, or a new one when [entryId] is null */
    data class OpenJournalEntry(val entryId: String?) : HomeEffect
    data object OpenEmotions : HomeEffect

    /** The Explore topic, in the Explore tab */
    data class OpenTopic(val topicId: String) : HomeEffect

    /** The surah at the ayah (number within the surah), in the Quran tab */
    data class OpenSurah(val surah: Surah, val ayahNumber: Int) : HomeEffect
    data class OpenShare(val passage: SharePassage) : HomeEffect

    /** The platform puts the text on the clipboard */
    data class Copy(val text: String) : HomeEffect
    data object OpenSettings : HomeEffect
}
