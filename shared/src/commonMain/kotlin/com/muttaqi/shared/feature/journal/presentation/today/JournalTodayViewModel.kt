package com.muttaqi.shared.feature.journal.presentation.today

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal object JournalTodayReducer : Reducer<JournalTodayState, JournalTodayMutation> {
    override fun reduce(state: JournalTodayState, mutation: JournalTodayMutation) = when (mutation) {
        is JournalTodayMutation.Loaded -> state.copy(entry = mutation.entry)
    }
}

class JournalTodayViewModel(
    private val observeToday: ObserveTodaysJournalEntry,
) : MviViewModel<JournalTodayState, JournalTodayIntent, JournalTodayMutation, JournalTodayEffect>(JournalTodayState(), JournalTodayReducer) {

    private var observation: Job? = null

    init {
        observe()
    }

    override fun handle(intent: JournalTodayIntent) {
        when (intent) {
            JournalTodayIntent.Refresh -> observe()
        }
    }

    private fun observe() {
        observation?.cancel()
        observation = viewModelScope.launch {
            observeToday().catch { emit(null) }.collect { mutate(JournalTodayMutation.Loaded(it)) }
        }
    }
}
