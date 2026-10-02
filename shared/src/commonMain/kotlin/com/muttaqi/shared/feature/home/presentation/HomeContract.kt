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

enum class HomeLocation {
    Unknown,

    NeedsPermission,

    Denied,
    Available,
}

enum class DailyCard { Ayah, Hadith, Dua }

data class HomeState(
    val now: Instant = Instant.DISTANT_PAST,
    val today: LocalDate? = null,
    val hijriDate: String = "",
    val schedule: PrayerSchedule? = null,
    val location: HomeLocation = HomeLocation.Unknown,
    val qibla: QiblaDirection? = null,
    val isCompassAvailable: Boolean = true,
    val greeting: DailyAyah? = null,
    val ayahOfTheDay: DailyAyah? = null,
    val hadithOfTheDay: HadithPassage? = null,
    val duaOfTheDay: QuranicDua? = null,
    val nameOfTheDay: AllahName? = null,
    val topicOfTheDay: ExploreTopic? = null,
    val dhikrToday: Int = 0,
    val journalToday: JournalEntry? = null,
    val lastReading: LastReading? = null,
    val kahf: Surah? = null,
) : UiState {
    val nextPrayer: UpcomingPrayer? get() = schedule?.nextPrayer(now)

    val nextPrayerToday: Prayer?
        get() = nextPrayer?.takeIf { schedule?.today?.time(it.prayer) == it.time }?.prayer

    val asksForLocation: Boolean
        get() = nextPrayer == null && (location == HomeLocation.NeedsPermission || location == HomeLocation.Denied)

    val fridayKahf: Surah?
        get() = kahf?.takeIf { today?.dayOfWeek == DayOfWeek.FRIDAY && lastReading?.surah?.number != it.number }
}

sealed interface HomeIntent : UiIntent {
    data object Refresh : HomeIntent

    data object SetLocationTapped : HomeIntent
    data object QiblaTapped : HomeIntent
    data object DhikrTapped : HomeIntent
    data object NameTapped : HomeIntent

    data object JournalTapped : HomeIntent

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

    data class LocationChecked(val access: LocationAccess) : HomeMutation
}

sealed interface HomeEffect : UiEffect {
    data object OpenQibla : HomeEffect
    data object OpenDhikr : HomeEffect
    data object OpenNames : HomeEffect
    data object OpenJournal : HomeEffect

    data class OpenJournalEntry(val entryId: String?) : HomeEffect
    data object OpenEmotions : HomeEffect

    data class OpenTopic(val topicId: String) : HomeEffect

    data class OpenSurah(val surah: Surah, val ayahNumber: Int) : HomeEffect
    data class OpenShare(val passage: SharePassage) : HomeEffect

    data class Copy(val text: String) : HomeEffect
    data object OpenSettings : HomeEffect
}
