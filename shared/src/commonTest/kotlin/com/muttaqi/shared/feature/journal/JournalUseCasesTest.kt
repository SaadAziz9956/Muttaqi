package com.muttaqi.shared.feature.journal

import app.cash.turbine.test
import com.muttaqi.shared.core.text.SearchTextFolder
import com.muttaqi.shared.feature.journal.JournalTestData.cafe
import com.muttaqi.shared.feature.journal.JournalTestData.parents
import com.muttaqi.shared.feature.journal.JournalTestData.walk
import com.muttaqi.shared.feature.journal.data.repository.SettingsJournalImportStatus
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.repository.JournalWriteRepository
import com.muttaqi.shared.feature.journal.domain.usecase.BuildJournalSearchIndex
import com.muttaqi.shared.feature.journal.domain.usecase.ImportJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.NewJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.SaveJournalEntry
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class JournalUseCasesTest {
    @Test
    fun entriesComeNewestFirst() = runTest {
        val repository = FakeJournalRepository(cafe, walk, parents)
        assertEquals(listOf("walk", "parents", "cafe"), ObserveJournalEntries(repository)().first().map { it.id })
    }

    @Test
    fun anEmptyEntryIsDeletedRatherThanSaved() = runTest {
        val repository = FakeJournalRepository(walk)
        val save = SaveJournalEntry(repository)
        save(walk.copy(title = "  ", body = "\n"))
        save(parents)
        assertEquals(listOf("delete walk", "save parents: |${parents.body}"), repository.writes)
    }

    @Test
    fun theListShowsTheTitleOrElseTheBodysFirstLine() {
        assertEquals("Morning light", walk.preview)
        assertEquals("Alhamdulillah for my parents' health", parents.preview)
        assertEquals("Second", walk.copy(title = " ", body = "\n  \n  Second  \nThird").preview)
        assertTrue(walk.copy(title = " \n", body = "\t").isEmpty)
    }

    @Test
    fun aNewEntryIsBlankAndDatedNow() {
        val entry = NewJournalEntry(FakeClock(), newId = { "new" })()
        assertEquals("new", entry.id)
        assertTrue(entry.isEmpty)
        assertEquals(JournalTestData.now, entry.createdAt)
        val make = NewJournalEntry(FakeClock())
        assertTrue(make().id != make().id)
    }

    @Test
    fun searchMatchesEveryWordWhateverTheCaseOrAccents() {
        val index = BuildJournalSearchIndex(SearchTextFolder)(listOf(walk, parents, cafe))
        assertEquals(listOf("parents"), index.search("RAIN health").map { it.id })
        assertEquals(listOf("cafe"), index.search("cafe").map { it.id })
        assertEquals(listOf("walk"), index.search("fajr  light").map { it.id })
        assertTrue(index.search("rain walk").isEmpty())
        assertTrue(index.search("   ").isEmpty())
    }

    @Test
    fun todaysEntryIsTheNewestWrittenOnTheReadersDay() = runTest {
        val repository = FakeJournalRepository(parents, walk)
        val clock = FakeClock()
        fun today(zone: String) = ObserveTodaysJournalEntry(ObserveJournalEntries(repository), clock) { TimeZone.of(zone) }()
        assertEquals("walk", today("Asia/Karachi").first()?.id)
        clock.now = Instant.parse("2026-09-30T02:00:00Z")
        assertEquals("parents", today("America/New_York").first()?.id)
        clock.now = Instant.parse("2026-10-02T10:00:00Z")
        assertNull(today("UTC").first())
    }

    @Test
    fun todaysEntryFollowsWrites() = runTest {
        val repository = FakeJournalRepository()
        ObserveTodaysJournalEntry(ObserveJournalEntries(repository), FakeClock()) { TimeZone.UTC }().test {
            assertNull(awaitItem())
            repository.save(walk)
            assertEquals("walk", awaitItem()?.id)
        }
    }

    @Test
    fun importRunsOnceAndKeepsEntriesAlreadyThere() = runTest {
        val edited = walk.copy(title = "Edited since")
        val repository = FakeJournalRepository(edited)
        val status = FakeImportStatus()
        val import = ImportJournalEntries(repository, status)
        assertTrue(import.isNeeded)
        import(listOf(walk, parents))
        assertFalse(import.isNeeded)
        assertEquals(setOf("Edited since", ""), repository.stored.value.map { it.title }.toSet())
        repository.delete("parents")
        import(listOf(walk, parents))
        assertEquals(listOf("walk"), repository.stored.value.map { it.id })
    }

    @Test
    fun anImportThatFailsIsTriedAgain() = runTest {
        val status = FakeImportStatus()
        val failing = object : JournalWriteRepository by FakeJournalRepository() {
            override suspend fun addMissing(entries: List<JournalEntry>) = error("Disk full")
        }
        runCatching { ImportJournalEntries(failing, status)(listOf(walk)) }
        assertFalse(status.isImported)
    }

    @Test
    fun theImportIsRememberedInSettings() {
        val settings = MapSettings()
        SettingsJournalImportStatus(settings).markImported()
        assertTrue(SettingsJournalImportStatus(settings).isImported)
        assertEquals(true, settings.getBooleanOrNull("journal_imported_from_swiftdata"))
    }
}
