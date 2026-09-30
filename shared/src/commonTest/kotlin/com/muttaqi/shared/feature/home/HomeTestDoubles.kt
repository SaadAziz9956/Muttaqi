package com.muttaqi.shared.feature.home

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrRepository
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.dua.domain.repository.QuranicDuaRepository
import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.ReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.repository.ExploreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

/** Stand-ins for what Home reads from the other features. The texts are placeholders, marked with their language */
internal object HomeTestData {
    val karachi = TimeZone.of("Asia/Karachi")

    /** Wednesday 30 September 2026, 17:45 in Karachi, before Maghrib (18:20) */
    val wednesdayEvening: Instant = Instant.parse("2026-09-30T12:45:00Z")

    /** Friday 2 October 2026, 10:00 in Karachi */
    val fridayMorning: Instant = Instant.parse("2026-10-02T05:00:00Z")

    val surahs: List<Surah> = (Surah.FIRST..Surah.LAST).map { number ->
        val name = when (number) {
            2 -> "Al-Baqara"
            16 -> "An-Nahl"
            18 -> "Al-Kahf"
            else -> "Surah $number"
        }
        Surah(number, "سورة $number", name, "", "Meccan", 200)
    }

    fun surah(number: Int): Surah = surahs.first { it.number == number }

    /** 38 duas, as many as Duas.json has, so the day's index is the one the app picks */
    val duaCount = 38

    /** Five Explore topics; the hadith of the day picks from the four short ones in everyday groups */
    fun explore(language: Language) = listOf(
        ExploreGroup(
            "faith",
            "Faith",
            listOf(
                topic("tawhid", language, hadith("faith-1", language)),
                topic("patience", language, hadith("faith-2", language), hadith("too-long", language, length = 421)),
            ),
        ),
        ExploreGroup("worship", "Worship", listOf(topic("prayer", language, hadith("worship-1", language)))),
        ExploreGroup("daily-life", "Daily Life", listOf(topic("food", language, hadith("daily-1", language), hadith("daily-2", language)))),
        ExploreGroup("sins", "Sins to Avoid", listOf(topic("lying", language, hadith("sins-1", language)))),
    )

    private fun topic(id: String, language: Language, vararg hadith: HadithPassage) =
        ExploreTopic(id, "${language.code} $id", "drop-linear", emptyList(), emptyList(), hadith.toList(), emptyList())

    private fun hadith(id: String, language: Language, length: Int = 0) = HadithPassage(
        arabic = "حديث $id",
        translation = "${language.code} hadith $id".padEnd(length, '.'),
        attribution = "Narrated by Muslim",
        grade = "Authentic",
        credit = "HadeethEnc.com",
    )

    val subhanAllah = Dhikr("subhanallah", null, "سُبْحَانَ اللَّهِ", "SubhanAllah", "en glory", emptyList(), 33, null, "Muslim", "Sahih", null)
    val istighfar = subhanAllah.copy(id = "istighfar", count = null)
}

/** The Quran as stored, or not yet downloaded; each ayah's text names its place and language */
internal class FakeQuran(var stored: Boolean = true) : SurahRepository, AyahRepository, ReadingProgressRepository {
    var last: SurahProgress? = null

    override suspend fun surahs() = if (stored) HomeTestData.surahs else emptyList()
    override suspend fun surah(number: Int) = surahs().firstOrNull { it.number == number }
    override suspend fun ayahs(surahNumber: Int, language: Language) = error("Home reads one ayah at a time")
    override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language) = if (stored) {
        Ayah(
            number = surahNumber * 1000 + numberInSurah,
            numberInSurah = numberInSurah,
            surahNumber = surahNumber,
            arabicText = "آية $surahNumber:$numberInSurah ۝",
            transliteration = null,
            translation = "${language.code} $surahNumber:$numberInSurah",
            juz = 1,
            page = 1,
            hizbQuarter = 1,
        )
    } else {
        null
    }

    override suspend fun progress(surahNumber: Int) = last?.takeIf { it.surahNumber == surahNumber }
    override suspend fun lastRead() = last
    override suspend fun all() = listOfNotNull(last)
    override suspend fun record(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int, at: Instant) =
        error("Home only reads progress")
    override suspend fun merge(records: List<SurahProgress>) = error("Home only reads progress")

    fun readUpTo(surahNumber: Int, ayahNumber: Int) {
        last = SurahProgress(surahNumber, ayahNumber, (1..ayahNumber).toSet(), ayahNumber, 200, Instant.parse("2026-09-29T08:00:00Z"))
    }
}

internal val fakeDuas = QuranicDuaRepository { language ->
    List(HomeTestData.duaCount) { index ->
        QuranicDua(index + 1, index + 1, "دعاء ${index + 1}", "Dua ${index + 1}", "${language.code} dua ${index + 1}")
    }
}

internal val fakeNames = NamesRepository { language ->
    List(99) { AllahName(it + 1, "اسم ${it + 1}", "Name ${it + 1}", "${language.code} meaning ${it + 1}") }
}

internal val fakeExplore = object : ExploreRepository {
    override suspend fun header() = PublishedQuote(emptyMap(), "")
    override suspend fun groups(language: Language) = HomeTestData.explore(language)
}

internal val fakeDhikr = DhikrRepository { listOf(DhikrSection("tasbih", "Tasbih", "", listOf(HomeTestData.subhanAllah, HomeTestData.istighfar))) }

internal class FakeDhikrProgress : DhikrProgressRepository {
    val saved = mutableMapOf<String, DhikrProgress>()
    override fun saved(dhikrId: String) = saved[dhikrId]
    override fun save(dhikrId: String, progress: DhikrProgress) {
        saved[dhikrId] = progress
    }
}

/** A clock the test moves by hand; each move is a new minute for Home */
internal class FakeReaderClock(now: Instant, override val timeZone: TimeZone = HomeTestData.karachi) : ReaderClock {
    private val ticks = MutableStateFlow(now)

    var now: Instant
        get() = ticks.value
        set(value) {
            ticks.value = value
        }

    override fun now(): Instant = ticks.value
    override fun minutes(): Flow<Instant> = ticks

    /** The same time, for the features that take the standard clock */
    val asClock: Clock = object : Clock {
        override fun now(): Instant = ticks.value
    }
}

/** Names the civil day it's given, so tests see which day the Hijri date is for */
internal val fakeHijri = HijriCalendar { "Hijri of $it" }
