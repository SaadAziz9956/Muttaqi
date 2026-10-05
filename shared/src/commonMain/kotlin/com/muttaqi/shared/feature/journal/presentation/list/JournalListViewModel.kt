package com.muttaqi.shared.feature.journal.presentation.list

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.journal.domain.usecase.BuildJournalSearchIndex
import com.muttaqi.shared.feature.journal.domain.usecase.DeleteJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.JournalSearchIndex
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class JournalListViewModel(
    observeEntries: ObserveJournalEntries,
    private val buildSearchIndex: BuildJournalSearchIndex,
    private val deleteEntry: DeleteJournalEntry,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<JournalListState, JournalListIntent, JournalListMutation, JournalListEffect>(JournalListState(), JournalListReducer) {

    private var searchIndex: JournalSearchIndex? = null

    private val removing = mutableSetOf<String>()

    init {
        launchNow {
            selectedLanguage.changes.collect { language ->
                mutate(JournalListMutation.HeaderLoaded(PageQuotes.byThePen.displayed(language)))
            }
        }
        launchNow {
            observeEntries()
                .catch { emit(emptyList()) }
                .collect { stored ->
                    removing.retainAll { id -> stored.any { it.id == id } }
                    val entries = stored.filterNot { it.id in removing }
                    val index = buildSearchIndex(entries).also { searchIndex = it }
                    mutate(JournalListMutation.EntriesLoaded(entries, index.search(state.value.query)))
                }
        }
    }

    override fun handle(intent: JournalListIntent) {
        when (intent) {
            is JournalListIntent.QueryChanged ->
                mutate(JournalListMutation.SearchUpdated(intent.query, searchIndex?.search(intent.query).orEmpty()))
            is JournalListIntent.EntryTapped -> emit(JournalListEffect.OpenEntry(intent.entryId))
            JournalListIntent.NewEntryTapped -> emit(JournalListEffect.OpenEntry(null))
            is JournalListIntent.DeleteTapped -> {
                removing += intent.entryId
                mutate(JournalListMutation.EntryRemoved(intent.entryId))
                viewModelScope.launch { deleteEntry(intent.entryId) }
            }
        }
    }
}
