package com.muttaqi.shared.feature.dua

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dua.data.repository.BundledDuaRepository
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.TestDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BundledDuaRepositoryTest {
    private fun repository(dispatcher: kotlinx.coroutines.CoroutineDispatcher) =
        BundledDuaRepository(FakeContentSource(DuaTestData.files), TestDispatchers(dispatcher))

    @Test
    fun quranicDuasComeFirstAsRabbana() = runTest {
        val categories = repository(StandardTestDispatcher(testScheduler)).categories(Language.English)
        assertEquals(listOf("rabbana", "sleep", "travel"), categories.map { it.id })
        assertEquals("quran-2:201", categories.first().chapters.single().entries.first().id)
    }

    @Test
    fun urduIsUsedWhereItsPublishedAndEnglishOtherwise() = runTest {
        val categories = repository(StandardTestDispatcher(testScheduler)).categories(Language.Urdu)
        val waking = categories[1].chapters[0].entries.single()
        assertEquals("سب تعریف اللہ کے لیے", waking.translation)
        assertEquals("Za'i", waking.translationCredit)
        val sleeping = categories[1].chapters[1].entries.single()
        assertEquals("In Your name, O Allah", sleeping.translation)
        assertEquals("Hisn al-Muslim (hisnmuslim.com)", sleeping.translationCredit)
    }

    @Test
    fun quranicCreditFollowsTheTranslationShown() = runTest {
        val rabbana = repository(StandardTestDispatcher(testScheduler)).categories(Language.Urdu).first().chapters.single().entries
        assertEquals("Fateh Muhammad Jalandhry", rabbana[0].translationCredit)
        assertEquals("Our Lord, let not our hearts deviate", rabbana[1].translation)
        assertEquals("Saheeh International", rabbana[1].translationCredit)
    }

    @Test
    fun repeatCountIsAtLeastOne() = runTest {
        val entries = repository(StandardTestDispatcher(testScheduler)).categories(Language.English)[1].chapters.flatMap { it.entries }
        assertEquals(listOf(1, 3), entries.map { it.repeatCount })
    }
}
