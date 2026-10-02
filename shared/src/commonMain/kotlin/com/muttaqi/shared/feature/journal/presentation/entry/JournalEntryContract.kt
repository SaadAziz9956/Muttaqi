package com.muttaqi.shared.feature.journal.presentation.entry

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import kotlin.time.Instant

data class JournalEntryState(
    val entry: JournalEntry? = null,
    val startedEmpty: Boolean = false,
    val isConfirmingDelete: Boolean = false,
) : UiState {
    val isLoading: Boolean get() = entry == null
    val title: String get() = entry?.title.orEmpty()
    val body: String get() = entry?.body.orEmpty()
    val createdAt: Instant? get() = entry?.createdAt
    val isEmpty: Boolean get() = entry?.isEmpty ?: true
}

sealed interface JournalEntryIntent : UiIntent {
    data class TitleChanged(val title: String) : JournalEntryIntent
    data class BodyChanged(val body: String) : JournalEntryIntent
    data object SaveNow : JournalEntryIntent
    data object DeleteTapped : JournalEntryIntent
    data object DeleteConfirmed : JournalEntryIntent
    data object DeleteCancelled : JournalEntryIntent
}

sealed interface JournalEntryMutation : UiMutation {
    data class Loaded(val entry: JournalEntry, val startedEmpty: Boolean) : JournalEntryMutation
    data class TitleEdited(val title: String, val at: Instant) : JournalEntryMutation
    data class BodyEdited(val body: String, val at: Instant) : JournalEntryMutation
    data class ConfirmingDelete(val isConfirming: Boolean) : JournalEntryMutation
}

sealed interface JournalEntryEffect : UiEffect {
    data object Close : JournalEntryEffect
}
