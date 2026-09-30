package com.muttaqi.shared.feature.journal.presentation.list

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry

/** The journal: the verse under the title, every entry newest first, and search across them */
data class JournalListState(
    val isLoading: Boolean = true,
    val header: DisplayedQuote? = null,
    val entries: List<JournalEntry> = emptyList(),
    val query: String = "",
    val results: List<JournalEntry> = emptyList(),
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()
    val hasEntries: Boolean get() = entries.isNotEmpty()

    /** What the list shows: every entry, or while searching, the ones that match */
    val shownEntries: List<JournalEntry> get() = if (isSearching) results else entries
}

sealed interface JournalListIntent : UiIntent {
    data class QueryChanged(val query: String) : JournalListIntent
    data class EntryTapped(val entryId: String) : JournalListIntent
    data object NewEntryTapped : JournalListIntent
    data class DeleteTapped(val entryId: String) : JournalListIntent
}

sealed interface JournalListMutation : UiMutation {
    data class HeaderLoaded(val header: DisplayedQuote) : JournalListMutation
    data class EntriesLoaded(val entries: List<JournalEntry>, val results: List<JournalEntry>) : JournalListMutation
    data class SearchUpdated(val query: String, val results: List<JournalEntry>) : JournalListMutation
    /** Taken out of the list straight away, before the database confirms, so a swiped row doesn't spring back */
    data class EntryRemoved(val entryId: String) : JournalListMutation
}

sealed interface JournalListEffect : UiEffect {
    /** Opens the editor on an entry, or on a new, blank one when [entryId] is null */
    data class OpenEntry(val entryId: String?) : JournalListEffect
}
