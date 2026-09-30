package com.muttaqi.shared.feature.journal

import com.muttaqi.shared.feature.journal.JournalTestData.parents
import com.muttaqi.shared.feature.journal.JournalTestData.walk
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.journal.presentation.today.JournalTodayIntent
import com.muttaqi.shared.feature.journal.presentation.today.JournalTodayViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class JournalTodayViewModelTest {
    private val repository = FakeJournalRepository(parents)
    private val clock = FakeClock(Instant.parse("2026-09-29T21:00:00Z"))
    private val viewModel by lazy { JournalTodayViewModel(ObserveTodaysJournalEntry(ObserveJournalEntries(repository), clock) { TimeZone.UTC }) }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun showsTodaysEntryAndFollowsNewOnes() = runTest {
        assertEquals("parents", viewModel.state.value.entry?.id)
        clock.now = JournalTestData.now
        repository.save(walk)
        assertEquals("walk", viewModel.state.value.entry?.id)
    }

    @Test
    fun refreshingAfterMidnightMovesOnToTheNewDay() = runTest {
        assertEquals("parents", viewModel.state.value.entry?.id)
        clock.now = Instant.parse("2026-09-30T00:30:00Z")
        viewModel.dispatch(JournalTodayIntent.Refresh)
        assertNull(viewModel.state.value.entry)
    }
}
