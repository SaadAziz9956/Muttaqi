package com.muttaqi.shared.feature.journal.presentation.today

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry

/** Today's entry for the Journal tile on Home, if one has been written */
data class JournalTodayState(val entry: JournalEntry? = null) : UiState

sealed interface JournalTodayIntent : UiIntent {
    /** Checks the day again, e.g. when the app comes back after midnight */
    data object Refresh : JournalTodayIntent
}

sealed interface JournalTodayMutation : UiMutation {
    data class Loaded(val entry: JournalEntry?) : JournalTodayMutation
}

sealed interface JournalTodayEffect : UiEffect
