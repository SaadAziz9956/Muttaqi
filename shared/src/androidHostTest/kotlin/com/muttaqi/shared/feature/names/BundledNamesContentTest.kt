package com.muttaqi.shared.feature.names

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.names.data.repository.BundledNamesRepository
import com.muttaqi.shared.feature.names.domain.model.NameSearchMode
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Decodes the real bundled file, so a change to its shape is caught here rather than in the apps */
class BundledNamesContentTest {
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    @Test
    fun theNinetyNineNamesDecodeInOrderWithBothMeanings() = runTest {
        val repository = BundledNamesRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val english = repository.names(Language.English)
        val urdu = repository.names(Language.Urdu)
        assertEquals((1..99).toList(), english.map { it.number })
        assertTrue(english.all { it.arabic.isNotBlank() && it.transliteration.isNotBlank() && it.meaning.isNotBlank() })
        // Urdu where al-Faryiwa'i's is published, otherwise the English, never blank
        assertTrue(urdu.all { it.meaning.isNotBlank() })
        assertTrue(urdu.count { name -> name.meaning.any { it.code in 0x0600..0x06FF } } > 90)
    }

    @Test
    fun searchFindsTheRealNamesHoweverTheyreSpelt() = runTest {
        val names = BundledNamesRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler))).names(Language.English)
        val index = BuildNamesSearchIndex(SearchTextFolder)(names)
        assertTrue(2 in index.search("rahmaan", NameSearchMode.ByName).map { it.number })
        assertTrue(3 in index.search("raheem", NameSearchMode.ByName).map { it.number })
        assertEquals(listOf(63), index.search("63", NameSearchMode.ByNumber).map { it.number })
        // Today's Name on Home before the move (see NamesUseCasesTest)
        assertEquals("al-hayyu", SearchTextFolder.fold(names[62].transliteration))
    }
}
