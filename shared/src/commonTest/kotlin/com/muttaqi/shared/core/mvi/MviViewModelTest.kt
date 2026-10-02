package com.muttaqi.shared.core.mvi

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MviViewModelTest {

    private data class CounterState(val count: Int = 0) : UiState
    private sealed interface CounterIntent : UiIntent {
        data object Tap : CounterIntent
        data object Reset : CounterIntent
    }
    private sealed interface CounterMutation : UiMutation {
        data object Increment : CounterMutation
        data object Clear : CounterMutation
    }
    private sealed interface CounterEffect : UiEffect {
        data object RoundComplete : CounterEffect
    }

    private val reducer = Reducer<CounterState, CounterMutation> { state, mutation ->
        when (mutation) {
            CounterMutation.Increment -> state.copy(count = state.count + 1)
            CounterMutation.Clear -> state.copy(count = 0)
        }
    }

    private inner class CounterViewModel :
        MviViewModel<CounterState, CounterIntent, CounterMutation, CounterEffect>(CounterState(), reducer) {
        override fun handle(intent: CounterIntent) {
            when (intent) {
                CounterIntent.Tap -> {
                    mutate(CounterMutation.Increment)
                    if (state.value.count == 3) emit(CounterEffect.RoundComplete)
                }
                CounterIntent.Reset -> mutate(CounterMutation.Clear)
            }
        }
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun reducerIsPure() {
        val state = CounterState(count = 2)
        assertEquals(CounterState(3), reducer.reduce(state, CounterMutation.Increment))
        assertEquals(CounterState(2), state)
    }

    @Test
    fun intentsChangeStateOnlyThroughTheReducer() = runTest {
        val viewModel = CounterViewModel()
        viewModel.state.test {
            assertEquals(0, awaitItem().count)
            viewModel.dispatch(CounterIntent.Tap)
            assertEquals(1, awaitItem().count)
            viewModel.dispatch(CounterIntent.Reset)
            assertEquals(0, awaitItem().count)
        }
    }

    @Test
    fun effectsArriveOnce() = runTest {
        val viewModel = CounterViewModel()
        viewModel.effects.test {
            repeat(3) { viewModel.dispatch(CounterIntent.Tap) }
            assertEquals(CounterEffect.RoundComplete, awaitItem())
            expectNoEvents()
        }
    }
}
