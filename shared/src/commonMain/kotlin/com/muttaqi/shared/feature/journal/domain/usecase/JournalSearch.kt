package com.muttaqi.shared.feature.journal.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry

/** The entries folded for search once, so each keystroke only compares */
class JournalSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Pair<JournalEntry, String>>,
) {
    /** Entries whose title or body contains every word of the query, whatever their case or accents */
    fun search(query: String): List<JournalEntry> {
        val words = folder.fold(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        return entries.filter { (_, text) -> words.all { it in text } }.map { it.first }
    }
}

/** Builds the search index for the journal's entries, keeping their order */
class BuildJournalSearchIndex(private val folder: TextFolder) {
    operator fun invoke(entries: List<JournalEntry>): JournalSearchIndex =
        JournalSearchIndex(folder, entries.map { it to folder.fold(it.title + " " + it.body) })
}
