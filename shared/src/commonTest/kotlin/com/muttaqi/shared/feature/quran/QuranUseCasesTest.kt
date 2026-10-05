package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.model.Ayah
import com.muttaqi.shared.feature.quran.domain.model.Revelation
import com.muttaqi.shared.feature.quran.domain.model.StoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.SurahReading
import com.muttaqi.shared.feature.quran.domain.repository.AyahRepository
import com.muttaqi.shared.feature.quran.domain.repository.StoredProgressImportMarker
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import com.muttaqi.shared.feature.quran.domain.usecase.ChangeTranslation
import com.muttaqi.shared.feature.quran.domain.usecase.FilterSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyah
import com.muttaqi.shared.feature.quran.domain.usecase.GetAyahOfTheDay
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetQuranCompletion
import com.muttaqi.shared.feature.quran.domain.usecase.ImportStoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.usecase.ReadSurah
import com.muttaqi.shared.feature.quran.domain.usecase.RecordReading
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.testing.FakeSelectedLanguage
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuranUseCasesTest {
    private val library = FakeQuranLibrary()
    private val progress = RoomReadingProgressRepository(FakeReadingProgressDao())
    private val language = FakeSelectedLanguage()
    private val record = FakeDownloadRecord()
    private val filter = FilterSurahs(SearchTextFolder)

    @Test
    fun searchFindsSurahsByNumberNameMeaningOrArabic() {
        fun search(query: String, revelation: Revelation? = null) = filter(testSurahs, query, revelation).map { it.number }
        assertEquals(listOf(1, 2, 9, 18, 114), search("  "))
        assertEquals(listOf(1, 18, 114), search("1"))
        assertEquals(listOf(18), search("alkahf"))
        assertEquals(listOf(18), search("Al Kahf"))
        assertEquals(listOf(2), search("cow"))
        assertEquals(listOf(2), search("البقرة"))
        assertEquals(listOf(1, 18, 114), search("", Revelation.Meccan))
        assertEquals(listOf(18), search("1", Revelation.Meccan).filter { it == 18 })
        assertEquals(emptyList(), search("kahf", Revelation.Medinan))
    }

    @Test
    fun syncDownloadsOnlyWhatIsMissingAndReadsInTheLanguage() = runTest {
        val empty = FakeQuranLibrary(textStored = false, translations = emptySet())
        val sync = SyncQuran(empty, record, language.selector())
        assertEquals(Outcome.Success(Unit), sync(Language.Urdu))
        assertEquals(listOf("text", "ur"), empty.downloads)
        assertEquals(Language.Urdu, language.current)
        assertTrue(record.textDownloaded)
        assertEquals(listOf(Language.Urdu), record.languages)

        sync(Language.Urdu)
        assertEquals(listOf("text", "ur"), empty.downloads)
    }

    @Test
    fun aFailedSyncKeepsTheLanguage() = runTest {
        val offline = FakeQuranLibrary(textStored = false, translations = emptySet()).apply { failDownloads = true }
        assertIs<Outcome.Failure>(SyncQuran(offline, record, language.selector())(Language.Urdu))
        assertEquals(Language.English, language.current)
        assertFalse(record.textDownloaded)
    }

    @Test
    fun aStoredTranslationIsChosenWithoutDownloading() = runTest {
        val change = ChangeTranslation(library, SyncQuran(library, record, language.selector()), language.selector())
        change(Language.Urdu)
        assertEquals(Language.Urdu, language.current)
        assertTrue(library.downloads.isEmpty())
        change(Language.Hindi)
        assertEquals(listOf("hi"), library.downloads)
        assertEquals(Language.Hindi, language.current)
    }

    @Test
    fun aSurahComesWithItsNeighbours() = runTest {
        val reading = ReadSurah(library, library)(2, Language.English)!!
        assertEquals(1, reading.previousSurah?.number)
        assertNull(reading.nextSurah)
        assertNull(ReadSurah(library, library)(3, Language.English))
    }

    @Test
    fun alFatihasBismillahIsItsNumberedFirstAyahAndAtTawbahHasNone() {
        val fatiha = SurahReading(testSurah(1), testAyahs(1), null, testSurah(2), testAyahs(1).first())
        assertFalse(fatiha.showsBismillah)
        assertEquals((1..7).toList(), fatiha.ayahs.map { it.numberInSurah })
        assertEquals(listOf(1), fatiha.pages.map { it.number })
        assertEquals(1, fatiha.pages.single().ayahs.first().numberInSurah)

        val tawbah = SurahReading(testSurah(9), testAyahs(9), null, null, testAyahs(1).first())
        assertFalse(tawbah.showsBismillah)
        assertEquals(2, tawbah.ayahs.size)
    }

    @Test
    fun everyOtherSurahsBismillahIsAlFatihasFirstAyahInTheReadersTranslation() = runTest {
        val urdu = ReadSurah(library, library)(2, Language.Urdu)!!
        assertEquals(QuranTestData.ayahs.first().arabic, urdu.bismillahText)
        assertEquals(QuranTestData.ayahs.first().urdu, urdu.bismillahTranslation)

        val english = ReadSurah(library, library)(2, Language.English)!!
        assertEquals(QuranTestData.ayahs.first().english, english.bismillahTranslation)
    }

    @Test
    fun theBismillahHasNoTranslationUntilTheReadersTranslationIsStored() = runTest {
        val hindi = ReadSurah(library, library)(2, Language.Hindi)!!
        assertEquals(QuranTestData.ayahs.first().arabic, hindi.bismillahText)
        assertNull(hindi.bismillahTranslation)
    }

    @Test
    fun ayahsAreGroupedIntoTheirMushafPages() {
        val pages = SurahReading(testSurah(2), testAyahs(2), null, null, testAyahs(1).first()).pages
        assertEquals(listOf(2, 3), pages.map { it.number })
        assertEquals(listOf(8, 13), pages.map { it.id })
        assertEquals(listOf(5, 3), pages.map { it.ayahs.size })
    }

    @Test
    fun oneAyahComesWithItsSurahAndReference() = runTest {
        val ayah = GetAyah(library, library)(2, 3, Language.Urdu)!!
        assertEquals("2:3", ayah.reference)
        assertEquals("Al-Baqara", ayah.surah.englishName)
        assertEquals(QuranTestData.ayahs.first { it.surah == 2 && it.numberInSurah == 3 }.urdu, ayah.ayah.translation)
        assertNull(GetAyah(library, library)(2, 200, Language.Urdu))
        assertNull(GetAyah(FakeQuranLibrary(textStored = false), FakeQuranLibrary(textStored = false))(2, 3, Language.Urdu))
    }

    @Test
    fun theAyahOfTheDayIsTheOneTheSwiftAppPicked() = runTest {
        val asked = mutableListOf<String>()
        val anySurah = object : SurahRepository {
            override suspend fun surahs() = testSurahs
            override suspend fun surah(number: Int) = testSurah(2).copy(number = number)
        }
        val recording = object : AyahRepository {
            override suspend fun ayahs(surahNumber: Int, language: Language) = emptyList<Ayah>()
            override suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: Language): Ayah? {
                asked += "$surahNumber:$numberInSurah"
                return testAyahs(2).first().copy(surahNumber = surahNumber, numberInSurah = numberInSurah)
            }
        }
        val pick = GetAyahOfTheDay(GetAyah(anySurah, recording))
        assertEquals("16:128", pick(LocalDate(2026, 9, 30), Language.English)?.reference)
        assertEquals("16:128", pick(LocalDate(2026, 9, 30), Language.Urdu)?.reference)
        assertEquals("21:35", pick(LocalDate(2026, 10, 1), Language.English)?.reference)
        assertEquals("16:128", pick(LocalDate(2026, 11, 8), Language.English)?.reference)
        assertEquals(listOf("16:128", "16:128", "21:35", "16:128"), asked)
        assertNull(GetAyahOfTheDay(GetAyah(FakeQuranLibrary(textStored = false), recording))(LocalDate(2026, 9, 30), Language.English))
    }

    @Test
    fun theLastReadingNamesItsSurah() = runTest {
        assertNull(GetLastReading(progress, library)())
        RecordReading(progress, FixedClock())(2, 3, setOf(1, 2, 3), 286)
        val last = GetLastReading(progress, library)()!!
        assertEquals("Al-Baqara", last.surahEnglishName)
        assertEquals("سورة البقرة", last.surahName)
        assertEquals(3, last.lastAyahNumber)
        assertEquals(FixedClock().now, last.lastReadAt)
    }

    @Test
    fun completionCountsDistinctAyahsAcrossTheQuran() = runTest {
        RecordReading(progress, FixedClock())(1, 7, (1..7).toSet(), 7)
        RecordReading(progress, FixedClock())(2, 3, setOf(1, 2, 3), 286)
        RecordReading(progress, FixedClock())(2, 3, setOf(2, 3), 286)
        val completion = GetQuranCompletion(progress, library)()
        assertEquals(10, completion.ayahsRead)
        assertEquals(testSurahs.sumOf { it.numberOfAyahs }, completion.totalAyahs)
        assertEquals(completion.totalAyahs - 10, completion.ayahsLeft)
    }

    @Test
    fun storedProgressIsImportedOnce() = runTest {
        val marker = object : StoredProgressImportMarker {
            override var isImported = false
            override fun markImported() { isImported = true }
        }
        val import = ImportStoredReadingProgress(progress, marker)
        assertTrue(import.isNeeded)
        import(listOf(StoredReadingProgress(2, 3, listOf(1, 2, 3), 3, 286, 1_790_000_000_000)))
        assertFalse(import.isNeeded)
        assertEquals(3, progress.progress(2)!!.lastAyahNumber)

        import(listOf(StoredReadingProgress(2, 99, listOf(99), 1, 286, 1_800_000_000_000)))
        assertEquals(3, progress.progress(2)!!.lastAyahNumber)
    }
}
