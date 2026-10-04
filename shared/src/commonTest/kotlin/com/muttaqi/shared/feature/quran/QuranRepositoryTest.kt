package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.DomainError
import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.remote.QuranApi
import com.muttaqi.shared.feature.quran.data.repository.MushafAyah
import com.muttaqi.shared.feature.quran.data.repository.MushafSource
import com.muttaqi.shared.feature.quran.data.repository.RoomQuranRepository
import com.muttaqi.shared.testing.TestDispatchers
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuranRepositoryTest {
    private val dao = FakeQuranTextDao()

    private var mushaf: MushafSource = QuranTestData.mushaf

    private val api = MockHttp { request -> respondJson(fullQuranJson(request.url.encodedPath.substringAfterLast('/'))) }

    private fun TestScope.repository(http: MockHttp = api) =
        RoomQuranRepository(dao, QuranApi(http.client), TestDispatchers(StandardTestDispatcher(testScheduler)), mushaf)

    @Test
    fun syncsFromTheSameApiAndEditionsAsTheIosApp() = runTest {
        val repository = repository()
        repository.downloadText()
        repository.downloadTranslation(Language.Urdu)
        val urls = api.requests.map { it.url.toString() }.sorted()
        assertEquals(
            listOf(
                "https://api.qurani.ai/gw/qh/v1/quran/en.transliteration",
                "https://api.qurani.ai/gw/qh/v1/quran/quran-uthmani",
                "https://api.qurani.ai/gw/qh/v1/quran/ur.jalandhry",
            ),
            urls,
        )
    }

    @Test
    fun theTextIsStoredWithEachAyahsTransliteration() = runTest {
        val repository = repository()
        assertIs<Outcome.Success<Unit>>(repository.downloadText())
        assertEquals(testSurahs, repository.surahs())
        val baqara = repository.ayahs(2, Language.English)
        assertEquals(8, baqara.size)
        val first = QuranTestData.ayahs.first { it.surah == 2 }
        assertEquals(first.arabic, baqara.first().arabicText)
        assertEquals(first.transliteration, baqara.first().transliteration)
        assertEquals(first.page, baqara.first().page)
        assertNull(baqara.first().translation)
    }

    @Test
    fun theWordsPageAndJuzAlwaysComeFromTheMushafNotTheApi() = runTest {
        mushaf = MushafSource { surah, ayah -> MushafAyah("mushaf $surah:$ayah", "\uFC00", page = 600, juz = 30) }
        val repository = repository()
        repository.downloadText()
        val first = repository.ayahs(2, Language.English).first()
        assertEquals("mushaf 2:1", first.arabicText)
        assertEquals("\uFC00", first.ayahMark)
        assertEquals(600, first.page)
        assertEquals(30, first.juz)
        assertEquals("mushaf 2:1", repository.ayah(2, 1, Language.English)?.arabicText)
    }

    @Test
    fun aTranslationIsKeptPerLanguageAndReplacedWhenDownloadedAgain() = runTest {
        val repository = repository()
        repository.downloadText()
        assertFalse(repository.hasTranslation(Language.Urdu))
        repository.downloadTranslation(Language.Urdu)
        repository.downloadTranslation(Language.Urdu)
        assertTrue(repository.hasTranslation(Language.Urdu))
        assertFalse(repository.hasTranslation(Language.English))
        assertEquals(QuranTestData.ayahs.size, dao.translationRows.size)
        assertEquals("ur.jalandhry", dao.translationRows.values.first().editionIdentifier)
        val ayah = repository.ayah(1, 2, Language.Urdu)!!
        assertEquals(QuranTestData.ayahs[1].urdu, ayah.translation)
        assertNull(repository.ayah(1, 2, Language.English)!!.translation)
    }

    @Test
    fun theTextCountsAsStoredOnlyWithEverySurah() = runTest {
        val repository = repository()
        repository.downloadText()
        assertFalse(repository.hasText())
    }

    @Test
    fun noConnectionIsReportedAsSuch() = runTest {
        val offline = MockHttp { throw IOException("The Internet connection appears to be offline.") }
        assertEquals(Outcome.Failure(DomainError.NoConnection), repository(offline).downloadText())
        assertTrue(dao.surahRows.isEmpty())
    }

    @Test
    fun anErrorStatusIsAFailureRatherThanDecoded() = runTest {
        val broken = MockHttp { respondJson("error code: 502", HttpStatusCode.BadGateway) }
        assertIs<Outcome.Failure>(repository(broken).downloadTranslation(Language.English))
    }

    @Test
    fun aMissingAyahCountFallsBackToTheAyahsGiven() = runTest {
        val withoutCounts = MockHttp { request ->
            respondJson(fullQuranJson(request.url.encodedPath.substringAfterLast('/')).replace(Regex("\"numberOfAyahs\":\\d+,"), ""))
        }
        repository(withoutCounts).downloadText()
        assertEquals(7, dao.surahRows.getValue(1).numberOfAyahs)
        assertEquals(8, dao.surahRows.getValue(2).numberOfAyahs)
    }
}
