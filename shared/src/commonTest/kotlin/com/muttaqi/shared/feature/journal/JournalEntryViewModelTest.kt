package com.muttaqi.shared.feature.journal

import app.cash.turbine.test
import com.muttaqi.shared.feature.journal.JournalTestData.walk
import com.muttaqi.shared.feature.journal.domain.usecase.DeleteJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.GetJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.NewJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.SaveJournalEntry
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryEffect
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryIntent
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class JournalEntryViewModelTest {
    private val repository = FakeJournalRepository(walk)
    private val clock = FakeClock()

    private fun TestScope.viewModel(entryId: String?): JournalEntryViewModel = JournalEntryViewModel(
        entryId,
        GetJournalEntry(repository),
        NewJournalEntry(clock, newId = { "new" }),
        SaveJournalEntry(repository),
        DeleteJournalEntry(repository),
        clock,
    ).also { runCurrent() }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun aNewEntryIsReadyAtOnceAndAsksForTheKeyboard() = runTest {
        val viewModel = JournalEntryViewModel(null, GetJournalEntry(repository), NewJournalEntry(clock, newId = { "new" }), SaveJournalEntry(repository), DeleteJournalEntry(repository), clock)
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.startedEmpty)
        assertEquals(JournalTestData.now, state.createdAt)
    }

    @Test
    fun anEntryOpensAsItWasWritten() = runTest {
        val state = viewModel("walk").state.value
        assertEquals("Morning light", state.title)
        assertEquals(walk.body, state.body)
        assertFalse(state.startedEmpty)
    }

    @Test
    fun savesHalfASecondAfterTypingStops() = runTest {
        val viewModel = viewModel("walk")
        viewModel.dispatch(JournalEntryIntent.TitleChanged("Morning"))
        advanceTimeBy(300.milliseconds)
        viewModel.dispatch(JournalEntryIntent.BodyChanged("Quiet"))
        advanceTimeBy(499.milliseconds)
        assertTrue(repository.writes.isEmpty())
        advanceTimeBy(2.milliseconds)
        assertEquals(listOf("save walk: Morning|Quiet"), repository.writes)
    }

    @Test
    fun anEditRecordsWhenItWasMade() = runTest {
        val viewModel = viewModel("walk")
        clock.now = JournalTestData.now + 5.minutes
        viewModel.dispatch(JournalEntryIntent.BodyChanged("Later"))
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        val saved = repository.stored.value.single()
        assertEquals(walk.createdAt, saved.createdAt)
        assertEquals(clock.now, saved.updatedAt)
    }

    @Test
    fun leavingSavesAtOnceAndOnlyOnce() = runTest {
        val viewModel = viewModel("walk")
        viewModel.dispatch(JournalEntryIntent.BodyChanged("Written"))
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        assertEquals(listOf("save walk: Morning light|Written"), repository.writes)
        advanceTimeBy(1_000.milliseconds)
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        assertEquals(1, repository.writes.size)
    }

    @Test
    fun aNewEntryLeftEmptyIsNeverWritten() = runTest {
        val viewModel = viewModel(null)
        viewModel.dispatch(JournalEntryIntent.TitleChanged("  "))
        advanceTimeBy(1_000.milliseconds)
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun anEntryClearedOfEverythingIsDeleted() = runTest {
        val viewModel = viewModel("walk")
        viewModel.dispatch(JournalEntryIntent.TitleChanged(""))
        viewModel.dispatch(JournalEntryIntent.BodyChanged(""))
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        assertEquals(listOf("delete walk"), repository.writes)
        viewModel.dispatch(JournalEntryIntent.BodyChanged("Back"))
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        runCurrent()
        assertEquals("save walk: |Back", repository.writes.last())
    }

    @Test
    fun aTitleIsOneLine() = runTest {
        val viewModel = viewModel(null)
        viewModel.dispatch(JournalEntryIntent.TitleChanged("Morning\n"))
        assertEquals("Morning", viewModel.state.value.title)
        viewModel.dispatch(JournalEntryIntent.BodyChanged("One\nTwo"))
        assertEquals("One\nTwo", viewModel.state.value.body)
    }

    @Test
    fun deletingAnEmptyEntryClosesWithoutAsking() = runTest {
        val viewModel = viewModel(null)
        viewModel.effects.test {
            viewModel.dispatch(JournalEntryIntent.DeleteTapped)
            assertEquals(JournalEntryEffect.Close, awaitItem())
        }
        assertFalse(viewModel.state.value.isConfirmingDelete)
        assertTrue(repository.writes.isEmpty())
    }

    @Test
    fun deletingAWrittenEntryAsksFirst() = runTest {
        val viewModel = viewModel("walk")
        viewModel.dispatch(JournalEntryIntent.DeleteTapped)
        assertTrue(viewModel.state.value.isConfirmingDelete)
        viewModel.dispatch(JournalEntryIntent.DeleteCancelled)
        assertFalse(viewModel.state.value.isConfirmingDelete)
        assertTrue(repository.writes.isEmpty())

        viewModel.effects.test {
            viewModel.dispatch(JournalEntryIntent.DeleteTapped)
            viewModel.dispatch(JournalEntryIntent.DeleteConfirmed)
            assertEquals(JournalEntryEffect.Close, awaitItem())
        }
        runCurrent()
        assertEquals(listOf("delete walk"), repository.writes)
        assertTrue(repository.stored.value.isEmpty())
    }

    @Test
    fun nothingIsSavedAfterADelete() = runTest {
        val viewModel = viewModel("walk")
        viewModel.dispatch(JournalEntryIntent.BodyChanged("Typed, then deleted"))
        viewModel.dispatch(JournalEntryIntent.DeleteTapped)
        viewModel.dispatch(JournalEntryIntent.DeleteConfirmed)
        viewModel.dispatch(JournalEntryIntent.SaveNow)
        advanceTimeBy(1_000.milliseconds)
        assertEquals(listOf("delete walk"), repository.writes)
    }
}
