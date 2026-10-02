package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.core.content.BundledContentSource
import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BundledDhikrContentTest {
    private val content = BundledContentSource { File("../content/data/$it").readText() }

    @Test
    fun theBundledDhikrDecode() = runTest {
        val repository = BundledDhikrRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val sections = repository.sections(Language.Urdu)
        assertEquals("tasbih", sections.first().id)
        assertEquals(9, sections.size)
        val all = sections.flatMap { it.dhikr }
        assertTrue(all.size > 50)
        assertEquals(all.size, all.map { it.id }.toSet().size, "every dhikr has its own id, which its progress is saved under")
        assertTrue(all.all { it.arabic.isNotBlank() && it.transliteration.isNotBlank() && it.reference.isNotBlank() })
        assertTrue(all.flatMap { it.steps }.all { it.translation != null && it.count > 0 })
    }

    @Test
    fun theCountedSetsAddUp() = runTest {
        val repository = BundledDhikrRepository(content, TestDispatchers(StandardTestDispatcher(testScheduler)))
        val sets = repository.sections(Language.English).flatMap { it.dhikr }.associateBy { it.id }
        assertEquals(100, sets.getValue("after-every-prayer-33-33-and").target)
        assertEquals(listOf(33, 33, 34), sets.getValue("after-every-prayer-33-33-and").steps.map { it.count })
        assertEquals(100, sets.getValue("subhanallahi-wa-bihamdihi").target)
    }
}
