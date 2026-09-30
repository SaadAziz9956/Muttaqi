package com.muttaqi.shared.feature.journal.presentation.entry

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.DeleteJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.GetJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.NewJournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.SaveJournalEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

internal object JournalEntryReducer : Reducer<JournalEntryState, JournalEntryMutation> {
    override fun reduce(state: JournalEntryState, mutation: JournalEntryMutation) = when (mutation) {
        is JournalEntryMutation.Loaded -> state.copy(entry = mutation.entry, startedEmpty = mutation.startedEmpty)
        is JournalEntryMutation.TitleEdited -> state.copy(entry = state.entry?.copy(title = mutation.title, updatedAt = mutation.at))
        is JournalEntryMutation.BodyEdited -> state.copy(entry = state.entry?.copy(body = mutation.body, updatedAt = mutation.at))
        is JournalEntryMutation.ConfirmingDelete -> state.copy(isConfirmingDelete = mutation.isConfirming)
    }
}

/**
 * Edits one entry, or a new one when [entryId] is null. It saves on its own [AUTOSAVE_DELAY] after typing stops, and
 * straight away when asked (as the reader leaves); an entry that's empty then is deleted rather than kept.
 */
class JournalEntryViewModel(
    entryId: String?,
    getEntry: GetJournalEntry,
    newEntry: NewJournalEntry,
    private val saveEntry: SaveJournalEntry,
    private val deleteEntry: DeleteJournalEntry,
    private val clock: Clock,
) : MviViewModel<JournalEntryState, JournalEntryIntent, JournalEntryMutation, JournalEntryEffect>(
    JournalEntryState(),
    JournalEntryReducer,
) {
    /** What the database has, as far as this screen knows; null while nothing of this entry is stored */
    private var stored: JournalEntry? = null
    private var pendingSave: Job? = null
    private var isDeleted = false

    init {
        if (entryId == null) {
            // Made here rather than loaded, so a new entry is ready on the first frame, with the keyboard
            mutate(JournalEntryMutation.Loaded(newEntry(), startedEmpty = true))
        } else {
            viewModelScope.launch {
                val entry = getEntry(entryId)
                stored = entry
                // An entry deleted meanwhile opens blank, under the same id
                val shown = entry ?: newEntry().copy(id = entryId)
                mutate(JournalEntryMutation.Loaded(shown, startedEmpty = shown.isEmpty))
            }
        }
    }

    override fun handle(intent: JournalEntryIntent) {
        when (intent) {
            is JournalEntryIntent.TitleChanged -> {
                // A title is one line: Return moves on to the body instead
                val title = intent.title.withoutLineBreaks()
                if (title != state.value.title) edited(JournalEntryMutation.TitleEdited(title, clock.now()))
            }
            is JournalEntryIntent.BodyChanged ->
                if (intent.body != state.value.body) edited(JournalEntryMutation.BodyEdited(intent.body, clock.now()))
            JournalEntryIntent.SaveNow -> saveNow()
            JournalEntryIntent.DeleteTapped ->
                // Nothing written yet, so there's nothing to lose and nothing to confirm
                if (state.value.isEmpty) delete() else mutate(JournalEntryMutation.ConfirmingDelete(true))
            JournalEntryIntent.DeleteConfirmed -> {
                mutate(JournalEntryMutation.ConfirmingDelete(false))
                delete()
            }
            JournalEntryIntent.DeleteCancelled -> mutate(JournalEntryMutation.ConfirmingDelete(false))
        }
    }

    private fun edited(mutation: JournalEntryMutation) {
        if (state.value.entry == null || isDeleted) return
        mutate(mutation)
        pendingSave?.cancel()
        pendingSave = viewModelScope.launch {
            delay(AUTOSAVE_DELAY)
            saveNow()
        }
    }

    private fun saveNow() {
        pendingSave?.cancel()
        pendingSave = null
        val entry = state.value.entry ?: return
        if (isDeleted || entry == stored) return
        // Never stored and still empty: there's nothing to write or delete
        if (stored == null && entry.isEmpty) return
        stored = entry.takeUnless { it.isEmpty }
        viewModelScope.launch { saveEntry(entry) }
    }

    private fun delete() {
        pendingSave?.cancel()
        isDeleted = true
        val entry = state.value.entry
        if (entry != null && stored != null) viewModelScope.launch { deleteEntry(entry.id) }
        emit(JournalEntryEffect.Close)
    }

    companion object {
        val AUTOSAVE_DELAY = 500.milliseconds
    }
}

private fun String.withoutLineBreaks(): String = filterNot { it == '\n' || it == '\r' || it == ' ' || it == ' ' || it == '\u0085' }
