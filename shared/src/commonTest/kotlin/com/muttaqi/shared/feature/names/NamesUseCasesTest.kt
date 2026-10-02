package com.muttaqi.shared.feature.names

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode.ByName
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode.ByNumber
import com.muttaqi.shared.feature.names.domain.repository.NamesRepository
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.feature.names.domain.usecase.GetAllahNames
import com.muttaqi.shared.feature.names.domain.usecase.GetNameOfTheDay
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NamesUseCasesTest {
    private fun TestScope.repository() =
        BundledNamesRepository(FakeContentSource(NamesTestData.files), TestDispatchers(StandardTestDispatcher(testScheduler)))

    private suspend fun TestScope.index() = BuildNamesSearchIndex(SearchTextFolder)(GetAllahNames(repository())(Language.English))

    @Test
    fun meaningsAreInUrduWhereTheyrePublishedAndEnglishOtherwise() = runTest {
        val names = GetAllahNames(repository())(Language.Urdu)
        assertEquals(listOf(1, 2, 3, 4), names.map { it.number })
        assertEquals("بہت رحم کرنے والا", names[1].meaning)
        assertEquals("the Most Beneficent (to the believers)", names[2].meaning)
    }

    @Test
    fun byNumberFindsThatName() = runTest {
        val index = index()
        assertEquals(listOf(3), index.search(" 3 ", ByNumber).map { it.number })
        assertTrue(index.search("99", ByNumber).isEmpty())
        assertTrue(index.search("rahman", ByNumber).isEmpty())
    }

    @Test
    fun byNameIgnoresCaseAccentsHyphensAndDoubledVowels() = runTest {
        val index = index()
        assertEquals(listOf(2), index.search("rahman", ByName).map { it.number })
        assertEquals(listOf(2), index.search("AR RAHMAAN", ByName).map { it.number })
        assertEquals(listOf(3), index.search("raheem", ByName).map { it.number })
        assertEquals(listOf(4), index.search("ghafur", ByName).map { it.number })
        assertEquals(listOf(2, 3), index.search("rah", ByName).map { it.number })
    }

    @Test
    fun byNameAlsoSearchesTheMeaning() = runTest {
        assertEquals(listOf(4), index().search("forgiving", ByName).map { it.number })
    }

    @Test
    fun aQueryWithNoLettersOrOnlySpacesFindsNothing() = runTest {
        val index = index()
        assertTrue(index.search("   ", ByName).isEmpty())
        assertTrue(index.search("1", ByName).isEmpty())
        assertTrue(index.search("-", ByName).isEmpty())
    }

    @Test
    fun theNameOfTheDayIsTheOneTheSwiftAppShowed() = runTest {
        val ninetyNine = NamesRepository { List(99) { AllahName(it + 1, "", "", "") } }
        val pick = GetNameOfTheDay(ninetyNine)
        assertEquals(63, pick(LocalDate(2026, 9, 30), Language.English)?.number)
        assertEquals(64, pick(LocalDate(2026, 10, 1), Language.English)?.number)
        assertNull(GetNameOfTheDay { emptyList() }(LocalDate(2026, 9, 30), Language.English))
    }
}
