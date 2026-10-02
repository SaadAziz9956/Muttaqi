package com.muttaqi.shared.feature.journal

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.muttaqi.shared.feature.journal.JournalTestData.cafe
import com.muttaqi.shared.feature.journal.JournalTestData.parents
import com.muttaqi.shared.feature.journal.JournalTestData.walk
import com.muttaqi.shared.feature.journal.data.local.JournalDatabase
import com.muttaqi.shared.feature.journal.data.repository.RoomJournalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class RoomJournalRepositoryTest {
    private val database = Room.inMemoryDatabaseBuilder<JournalDatabase>()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()

    private fun TestScope.repository() = RoomJournalRepository(database.journalDao(), backgroundScope)

    @AfterTest
    fun tearDown() = database.close()

    @Test
    fun savesUpdatesAndDeletes() = runTest {
        val repository = repository()
        repository.save(walk)
        repository.save(walk.copy(title = "Evening light"))
        assertEquals(listOf("Evening light"), repository.entries().first().map { it.title })
        assertEquals(walk.body, repository.entry("walk")?.body)
        repository.delete("walk")
        assertNull(repository.entry("walk"))
    }

    @Test
    fun entriesComeNewestFirstAndFollowWrites() = runTest {
        val repository = repository()
        repository.entries().test {
            assertEquals(emptyList(), awaitItem())
            repository.save(cafe)
            assertEquals(listOf("cafe"), awaitItem().map { it.id })
            repository.save(walk)
            assertEquals(listOf("walk", "cafe"), awaitItem().map { it.id })
            repository.save(parents)
            assertEquals(listOf("walk", "parents", "cafe"), awaitItem().map { it.id })
        }
    }

    @Test
    fun importedEntriesKeepTheirIdsAndDatesAndDontReplaceOnesAlreadyThere() = runTest {
        val repository = repository()
        repository.save(walk.copy(title = "Edited since"))
        val written = Instant.parse("2024-02-29T23:59:58.123Z")
        val old = JournalTestData.entry("6F9619FF-8B86-D011-B42D-00C04FC964FF", "From SwiftData", "Kept", "2024-02-29T23:59:58.123Z")
            .copy(updatedAt = written + 1.hours)
        repository.addMissing(listOf(old, walk))
        assertEquals("Edited since", repository.entry("walk")?.title)
        assertEquals(old, repository.entry(old.id))
    }

    @Test
    fun writesLandInTheOrderTheyWereAskedFor() = runTest {
        val repository = repository()
        launch { repository.save(walk) }
        launch { repository.save(walk.copy(body = "Second draft")) }
        launch { repository.delete("walk") }
        launch { repository.save(cafe) }
        launch { repository.save(walk.copy(body = "Written again")) }
        repository.entries().first { it.size == 2 }
        assertEquals("Written again", repository.entry("walk")?.body)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun aWriteFinishesEvenIfTheScreenThatAskedIsGone() = runTest {
        val repository = repository()
        val screen = launch { repository.save(walk) }
        runCurrent()
        screen.cancel()
        assertEquals(listOf("walk"), repository.entries().first { it.isNotEmpty() }.map { it.id })
    }
}

