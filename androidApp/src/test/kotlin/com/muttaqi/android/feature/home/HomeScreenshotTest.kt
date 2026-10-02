package com.muttaqi.android.feature.home

import android.app.Application
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createComposeRule
import com.muttaqi.android.feature.prayer.PRO_MAX
import com.muttaqi.android.testing.captureLightAndDark
import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.domain.DispatcherProvider
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.repository.CurrentDay
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaEntriesById
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.feature.home.data.IcuHijriCalendar
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.home.domain.usecase.GetDailyContent
import com.muttaqi.shared.feature.home.domain.usecase.GetHijriDate
import com.muttaqi.shared.feature.home.domain.usecase.GetQuranShortcuts
import com.muttaqi.shared.feature.home.domain.usecase.LocatePrayerTimes
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.repository.JournalReadRepository
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.feature.prayer.data.qibla.AdhanQiblaRepository
import com.muttaqi.shared.feature.prayer.data.times.AdhanPrayerTimesRepository
import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import com.muttaqi.shared.feature.prayer.domain.usecase.FollowHeading
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.GetPrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.usecase.GetQiblaDirection
import com.muttaqi.shared.feature.prayer.domain.usecase.LocateReader
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.ReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyahOfTheDay
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.topics.data.repository.BundledExploreRepository
import com.muttaqi.shared.feature.topics.domain.usecase.GetHadithOfTheDay
import com.muttaqi.shared.feature.topics.domain.usecase.GetTopicOfTheDay
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.ZoneId
import kotlin.time.Clock
import kotlin.time.Instant

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(application = Application::class, sdk = [35], qualifiers = PRO_MAX)
class HomeScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val karachi = TimeZone.of("Asia/Karachi")
    private val wednesdayEvening = LocalDateTime(2026, 9, 30, 19, 0).toInstant(karachi)
    private val fridayMorning = LocalDateTime(2026, 10, 2, 10, 0).toInstant(karachi)

    private val content = BundledContentSource { File("../content/data/$it").readText() }

    private val dispatchers = object : DispatcherProvider {
        override val main: CoroutineDispatcher = Dispatchers.Unconfined
        override val io: CoroutineDispatcher = Dispatchers.Unconfined
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    }
    private val duas = BundledDuaRepository(content, dispatchers)
    private val explore = BundledExploreRepository(content, dispatchers, GetDuaEntriesById(duas))
    private val names = BundledNamesRepository(content, dispatchers)
    private val dhikr = BundledDhikrRepository(content, dispatchers)

    private data class Saved(
        val dhikrSaid: Int = 10,
        val journal: String? = "Grateful for family",
        val reading: Pair<Int, Int>? = 16 to 127,
        val location: Coordinates? = Coordinates(24.8607, 67.0011),
    )

    private fun viewModel(now: Instant, language: Language = Language.English, saved: Saved = Saved()): HomeViewModel {
        val clock = object : ReaderClock {
            override val timeZone = karachi
            override fun now() = now
            override fun minutes(): Flow<Instant> = flowOf(now)
        }
        val today = now.toLocalDateTime(karachi).date
        val quran = object : SurahRepository, AyahRepository, ReadingProgressRepository {
            override suspend fun surahs() = HomeScreenshotQuran.surahs
            override suspend fun surah(number: Int) = HomeScreenshotQuran.surahs.firstOrNull { it.number == number }
            override suspend fun ayahs(surahNumber: Int, language: Language) = emptyList<Nothing>()
            override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language) =
                HomeScreenshotQuran.ayah(surahNumber, numberInSurah, language)
            override suspend fun progress(surahNumber: Int) = lastRead()?.takeIf { it.surahNumber == surahNumber }
            override suspend fun lastRead() = saved.reading?.let { (surah, ayah) ->
                SurahProgress(surah, ayah, (1..ayah).toSet(), ayah, 128, now)
            }
            override suspend fun all() = listOfNotNull(lastRead())
            override suspend fun record(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int, at: Instant) = Unit
            override suspend fun merge(records: List<SurahProgress>) = Unit
        }
        val firstDhikr = runBlocking { dhikr.sections(Language.English).first().dhikr.first().id }
        val progress = object : DhikrProgressRepository {
            override fun saved(dhikrId: String) = if (dhikrId == firstDhikr) DhikrProgress(saved.dhikrSaid, 0, today) else null
            override fun save(dhikrId: String, progress: DhikrProgress) = Unit
        }
        val journal = object : JournalReadRepository {
            private val entries = listOfNotNull(saved.journal?.let { JournalEntry("today", it, "", now, now) })
            override fun entries() = flowOf(entries)
            override suspend fun entry(id: String) = entries.firstOrNull { it.id == id }
        }
        val location = object : LocationRepository {
            override val access = if (saved.location != null) LocationAccess.Granted else LocationAccess.NotDetermined
            override suspend fun requestAccess() = access
            override fun lastKnownCoordinates() = saved.location
            override suspend fun refreshCoordinates(): Coordinates? = null
        }
        val noCompass = object : Compass {
            override val isAvailable = false
            override fun headings(): Flow<CompassHeading> = emptyFlow()
        }
        val getAyah = GetAyah(quran, quran)
        return HomeViewModel(
            GetDailyContent(
                getAyah,
                GetAyahOfTheDay(getAyah),
                GetHadithOfTheDay(explore),
                GetDuaOfTheDay(duas),
                GetNameOfTheDay(names),
                GetTopicOfTheDay(explore),
            ),
            GetQuranShortcuts(GetSurahs(quran), GetLastReading(quran, quran)),
            GetDhikrSaidToday(dhikr, GetTodaysDhikrProgress(progress, CurrentDay { today })),
            ObserveTodaysJournalEntry(ObserveJournalEntries(journal), object : Clock { override fun now() = now }) { karachi },
            LocatePrayerTimes(LocateReader(location), GetPrayerSchedule(AdhanPrayerTimesRepository()), GetQiblaDirection(AdhanQiblaRepository()), clock),
            GetLocationAccess(location),
            RequestLocationAccess(location),
            FollowHeading(noCompass),
            GetHijriDate(IcuHijriCalendar()),
            clock,
            object : SelectedLanguage {
                override val current = language
                override val changes = MutableStateFlow(language)
            },
        )
    }

    private fun capture(name: String, viewModel: HomeViewModel) = compose.captureLightAndDark(name) {
        val state by viewModel.state.collectAsState()
        HomeScreen(state, qiblaArrow = { null }, onIntent = {}, zone = ZoneId.of("Asia/Karachi"))
    }

    @Test
    fun home() = capture("home", viewModel(wednesdayEvening))

    @Test
    @Config(qualifiers = TALL)
    fun homePage() = capture("home_page", viewModel(wednesdayEvening))

    @Test
    fun urdu() = capture("home_urdu", viewModel(wednesdayEvening, Language.Urdu))

    @Test
    @Config(qualifiers = TALL)
    fun urduPage() = capture("home_urdu_page", viewModel(wednesdayEvening, Language.Urdu))

    @Test
    fun withoutALocation() = capture("home_no_location", viewModel(wednesdayEvening, saved = Saved(location = null)))

    @Test
    @Config(qualifiers = TALL)
    fun onAFridayAlKahfIsATapAway() = capture("home_friday_page", viewModel(fridayMorning))

    @Test
    @Config(qualifiers = TALL)
    fun beforeAnythingIsSaved() =
        capture("home_first_day_page", viewModel(wednesdayEvening, saved = Saved(dhikrSaid = 0, journal = null, reading = null)))
}

private const val TALL = "w440dp-h2700dp-xxhdpi"
