package com.muttaqi.shared.feature.quran

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import com.muttaqi.shared.feature.quran.data.local.TafsirEntity
import com.muttaqi.shared.feature.quran.data.remote.QuranApi
import com.muttaqi.shared.feature.quran.data.repository.RoomQuranRepository
import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * The Quran's Room database itself, in memory, with the SQLite driver the apps use, so its queries are checked as
 * written. It runs in the iOS simulator tests: the Android host tests' JVM has no build of the bundled SQLite for the
 * computer it runs on
 */
class RoomQuranDatabaseTest {
    private val database = Room.inMemoryDatabaseBuilder<QuranDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private val api = MockHttp { request -> respondJson(fullQuranJson(request.url.encodedPath.substringAfterLast('/'))) }

    private fun TestScope.quran() =
        RoomQuranRepository(database.textDao(), QuranApi(api.client), TestDispatchers(StandardTestDispatcher(testScheduler)))

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun ayahsComeInOrderWithTheLanguagesTranslation() = runTest {
        val quran = quran()
        quran.downloadText()
        quran.downloadTranslation(Language.Urdu)
        val baqara = quran.ayahs(2, Language.Urdu)
        assertEquals((1..8).toList(), baqara.map { it.numberInSurah })
        assertEquals(QuranTestData.ayahs.filter { it.surah == 2 }.map { it.urdu }, baqara.map { it.translation })
        // Another language's translation isn't stored, so the ayahs come without one rather than not at all
        assertEquals(List(8) { null }, quran.ayahs(2, Language.English).map { it.translation })
        assertEquals(QuranTestData.ayahs.first { it.surah == 18 }.transliteration, quran.ayah(18, 1, Language.Urdu)?.transliteration)
        assertNull(quran.ayah(18, 2, Language.Urdu))
    }

    @Test
    fun downloadingAgainReplacesRatherThanDuplicates() = runTest {
        val quran = quran()
        repeat(2) {
            quran.downloadText()
            quran.downloadTranslation(Language.English)
        }
        assertEquals(QuranTestData.surahs.size, database.textDao().surahCount())
        assertEquals(QuranTestData.ayahs.size, database.textDao().ayahCount())
        assertEquals(QuranTestData.ayahs.size, database.textDao().translationCount("en"))
        assertEquals(testSurahs, quran.surahs())
    }

    @Test
    fun readingProgressIsKeptPerSurahWithTheLatestFirst() = runTest {
        val progress = RoomReadingProgressRepository(database.progressDao())
        val start = Instant.fromEpochMilliseconds(1_790_000_000_000)
        progress.record(2, 3, setOf(1, 2, 3), 286, start)
        progress.record(2, 5, setOf(4, 5), 286, start + 2.minutes)
        progress.merge(listOf(SurahProgress(1, 7, (1..7).toSet(), 7, 7, start + 1.minutes)))
        assertEquals(2, progress.lastRead()?.surahNumber)
        assertEquals((1..5).toSet(), progress.progress(2)?.readAyahs)
        assertEquals(listOf(1, 2), progress.all().map { it.surahNumber })
    }

    @Test
    fun tafsirIsCachedPerSurahAndLanguage() = runTest {
        val dao = database.tafsirDao()
        dao.insert(listOf(TafsirEntity(2, 3, "en", "Ibn Kathir (Abridged)", "Three"), TafsirEntity(2, 1, "en", "Ibn Kathir (Abridged)", "One")))
        dao.insert(listOf(TafsirEntity(2, 1, "en", "Ibn Kathir (Abridged)", "One, again")))
        assertEquals(listOf("One, again", "Three"), dao.tafsir(2, "en").map { it.text })
        assertEquals(emptyList(), dao.tafsir(2, "ur"))
    }
}
