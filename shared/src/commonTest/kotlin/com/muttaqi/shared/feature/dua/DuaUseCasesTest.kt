package com.muttaqi.shared.feature.dua

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.BuildDuaSearchIndex
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DuaUseCasesTest {
    @Test
    fun searchMatchesArabicWithoutHarakatAndEveryWord() = runTest {
        val repository = BundledDuaRepository(FakeContentSource(DuaTestData.files), TestDispatchers(StandardTestDispatcher(testScheduler)))
        val index = BuildDuaSearchIndex(SearchTextFolder)(repository.categories(Language.English))
        assertEquals(listOf("hisn-2"), index.search("باسمك").map { it.chapter.id })
        assertEquals(listOf("hisn-1"), index.search("waking praise").map { it.chapter.id })
        assertTrue(index.search("   ").isEmpty())
    }

    @Test
    fun duaOfTheDayStaysAllDayAndMovesAtMidnight() = runTest {
        val repository = BundledDuaRepository(FakeContentSource(DuaTestData.files), TestDispatchers(StandardTestDispatcher(testScheduler)))
        val pick = GetDuaOfTheDay(repository)
        val today = pick(LocalDate(2026, 9, 30), Language.English)
        val tomorrow = pick(LocalDate(2026, 10, 1), Language.English)
        assertEquals(today, pick(LocalDate(2026, 9, 30), Language.English))
        assertTrue(today != tomorrow)
    }
}
