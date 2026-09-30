package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.local.SurahEntity
import com.muttaqi.shared.feature.quran.data.remote.TafsirApi
import com.muttaqi.shared.feature.quran.data.repository.RoomTafsirRepository
import com.muttaqi.shared.feature.quran.data.repository.strippingHtml
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TafsirRepositoryTest {
    private val tafsirDao = FakeTafsirDao()
    private val textDao = FakeQuranTextDao().apply {
        surahRows[2] = SurahEntity(2, "سورة البقرة", "Al-Baqara", "The Cow", "Medinan", 7)
    }

    // As quran.com sends it: commentary on a group of ayahs sits on the first, and the rest are empty
    private val response = """
        {"tafsirs":[
          {"id":1,"resource_id":169,"verse_key":"2:1","text":"<h2>Alif Lam Mim</h2><p>The letters &amp; their meaning</p>"},
          {"id":2,"resource_id":169,"verse_key":"2:2","text":""},
          {"id":3,"resource_id":169,"verse_key":"2:3","text":"<p>Those who believe</p><p>in the Unseen</p>"},
          {"id":4,"resource_id":169,"verse_key":"2:4","text":""}
        ],"pagination":{"per_page":300,"current_page":1,"total_pages":1}}
    """.trimIndent()

    private val api = MockHttp { respondJson(response) }

    private fun TestScope.repository() =
        RoomTafsirRepository(tafsirDao, textDao, TafsirApi(api.client), TestDispatchers(StandardTestDispatcher(testScheduler)))

    @Test
    fun askedForAWholeSurahInOnePageFromIbnKathir() = runTest {
        repository().tafsir(2, Language.English)
        repository().tafsir(2, Language.Urdu)
        assertEquals(
            listOf(
                "https://api.quran.com/api/v4/tafsirs/169/by_chapter/2?per_page=300",
                "https://api.quran.com/api/v4/tafsirs/160/by_chapter/2?per_page=300",
            ),
            api.requests.map { it.url.toString() },
        )
    }

    @Test
    fun hindiReadersGetTheEnglishTafsir() = runTest {
        repository().tafsir(2, Language.Hindi)
        assertEquals("/api/v4/tafsirs/169/by_chapter/2", api.requests.single().url.encodedPath)
        assertEquals("hi", tafsirDao.rows.first().language)
        assertEquals("Ibn Kathir (Abridged)", tafsirDao.rows.first().tafsirSource)
    }

    @Test
    fun eachPassageCoversTheAyahsUpToTheNextAndTheLastRunsToTheEndOfTheSurah() = runTest {
        val entries = assertIs<Outcome.Success<List<com.muttaqi.shared.feature.quran.domain.model.TafsirEntry>>>(
            repository().tafsir(2, Language.English),
        ).value
        assertEquals(listOf(1 to 2, 3 to 7), entries.map { it.ayahNumber to it.lastAyahNumber })
        assertEquals("Alif Lam Mim\n\nThe letters & their meaning", entries.first().text)
        assertEquals(listOf("Those who believe", "in the Unseen"), entries.last().paragraphs)
        assertEquals("2:3", entries.last().verseKey)
    }

    @Test
    fun aSurahIsDownloadedOnceThenReadFromTheDatabase() = runTest {
        repository().tafsir(2, Language.English)
        repository().tafsir(2, Language.English)
        assertEquals(1, api.requests.size)
    }

    @Test
    fun htmlBecomesParagraphs() {
        assertEquals(
            "Heading\n\nFirst line\nsecond line\n\n\"Quoted\" & 'kept'",
            "<h1 style=\"x\">Heading</h1>\n<p>First line<br/>second line</p>  <p>&quot;Quoted&quot; &amp; &#39;kept&#39;</p>".strippingHtml(),
        )
        assertEquals("a b", "a&nbsp;b".strippingHtml())
    }
}
