package com.muttaqi.shared.feature.journal.presentation.entry

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import kotlin.time.Instant

/** One entry being written or read. There's no Save button: it saves a moment after typing stops, like Notes */
data class JournalEntryState(
    /** The entry as edited so far; null until it's loaded */
    val entry: JournalEntry? = null,
    /** Opened blank, e.g. from the new-entry button, so the title gets the keyboard straight away */
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
    /** Writes any pending change now, e.g. when the reader leaves the screen or the app, or puts the keyboard away */
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
    /** The entry is gone, so the screen closes */
    data object Close : JournalEntryEffect
}
