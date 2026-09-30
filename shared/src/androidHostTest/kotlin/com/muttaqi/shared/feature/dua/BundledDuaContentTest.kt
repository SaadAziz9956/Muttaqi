package com.muttaqi.shared.feature.dua

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaOfTheDay
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Decodes the real bundled files, so a change to their shape is caught here rather than in the apps */
class BundledDuaContentTest {
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    @Test
    fun theBundledDuasDecode() = runTest {
        val repository = BundledDuaRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val categories = repository.categories(Language.Urdu)
        assertEquals("rabbana", categories.first().id)
        assertTrue(categories.sumOf { it.entryCount } > 250)
        assertTrue(repository.quranicDuas(Language.English).isNotEmpty())
    }

    @Test
    fun theDuaOfTheDayIsTheOneTheIosAppShowedOnTheSameDays() = runTest {
        val pick = GetDuaOfTheDay(BundledDuaRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler))))
        // Swift picked duas[ordinality of the day in the era % 38]; 30 September 2026 showed 26:83 on iOS
        assertEquals("26:83", pick(LocalDate(2026, 9, 30), Language.English)?.reference)
        assertEquals("26:83", pick(LocalDate(2026, 9, 30), Language.Urdu)?.reference)
        assertEquals("27:19", pick(LocalDate(2026, 10, 1), Language.English)?.reference)
        assertEquals("3:8", pick(LocalDate(2027, 3, 15), Language.English)?.reference)
    }
}
