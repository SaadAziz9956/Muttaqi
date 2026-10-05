package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.dhikr.DhikrTestData.today
import com.muttaqi.shared.feature.dhikr.data.progress.SettingsDhikrProgressRepository
import com.muttaqi.shared.feature.dhikr.data.repository.BundledDhikrRepository
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.usecase.CountDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikr
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSections
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetTodaysDhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.usecase.ResetDhikrProgress
import com.muttaqi.shared.testing.FakeContentSource
import com.muttaqi.shared.testing.TestDispatchers
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DhikrUseCasesTest {
    private val day = FakeCurrentDay()
    private val progress = SettingsDhikrProgressRepository(MapSettings(), LegacyDataSource { null }, timeZone = { TimeZone.UTC })
    private val getProgress = GetTodaysDhikrProgress(progress, day)

    private fun TestScope.repository() =
        BundledDhikrRepository(FakeContentSource(DhikrTestData.files), TestDispatchers(StandardTestDispatcher(testScheduler)))

    @Test
    fun sectionsComeInOrderWithUrduWhereItsPublishedAndEnglishOtherwise() = runTest {
        val sections = GetDhikrSections(repository())(Language.Urdu)
        assertEquals(listOf("tasbih", "after-prayer"), sections.map { it.id })
        val subhanallah = sections[0].dhikr[0]
        assertEquals("اللہ پاک ہے", subhanallah.translation)
        assertEquals("Whoever says it…", subhanallah.hadith)
        val steps = sections[1].dhikr.single().steps
        assertEquals(listOf("اللہ پاک ہے", "praise be to Allah", "Allah is the Most Great"), steps.map { it.translation })
    }

    @Test
    fun eachTextIsCreditedToWhoeverTranslatedTheLanguageItIsShownIn() = runTest {
        val urdu = GetDhikrSections(repository())(Language.Urdu)
        assertEquals("Urdu translator, English hadith translator", urdu[0].dhikr[0].credit)
        assertEquals("Urdu translator, English translator", urdu[1].dhikr.single().credit)
        val english = GetDhikrSections(repository())(Language.English)
        assertEquals("English translator, English hadith translator", english[0].dhikr[0].credit)
        assertEquals("English translator", english[1].dhikr.single().credit)
    }

    @Test
    fun aStepWithNoPublishedTransliterationHasNone() = runTest {
        val steps = GetDhikrSections(repository())(Language.English)[1].dhikr.single().steps
        assertEquals(listOf("SubhanAllah", "Alhamdulillah", null), steps.map { it.transliteration })
    }

    @Test
    fun whatAnEntryLeavesOutIsNull() = runTest {
        val openEnded = GetDhikr(repository())("open-ended", Language.English)!!
        assertNull(openEnded.translation)
        assertNull(openEnded.hadith)
        assertNull(openEnded.credit)
        assertNull(openEnded.target)
        assertNull(GetDhikr(repository())("missing", Language.English))
    }

    @Test
    fun todaysProgressStartsFromZeroEachDay() {
        CountDhikr(progress, day)(DhikrTestData.single, getProgress("subhanallah"))
        assertEquals(DhikrProgress(1, 0, today), getProgress("subhanallah"))
        day.day = LocalDate(2026, 10, 1)
        assertEquals(DhikrProgress.empty(LocalDate(2026, 10, 1)), getProgress("subhanallah"))
    }

    @Test
    fun countingSavesAndResetStartsAgain() {
        val count = CountDhikr(progress, day)
        repeat(4) { count(DhikrTestData.single, getProgress("subhanallah")) }
        assertEquals(DhikrProgress(1, 1, today), getProgress("subhanallah"))
        assertEquals(DhikrProgress.empty(today), ResetDhikrProgress(progress, day)("subhanallah"))
        assertEquals(DhikrProgress.empty(today), getProgress("subhanallah"))
    }

    @Test
    fun saidTodayAddsUpEveryCounterAndSkipsOtherDays() = runTest {
        progress.save("subhanallah", DhikrProgress(3, 1, today))
        progress.save("open-ended", DhikrProgress(10, 0, today))
        progress.save("set", DhikrProgress(2, 2, LocalDate(2026, 9, 29)))
        assertEquals(13, GetDhikrSaidToday(repository(), getProgress)())
    }
}
