package com.muttaqi.shared.feature.journal.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry

class JournalSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Pair<JournalEntry, String>>,
) {
    fun search(query: String): List<JournalEntry> {
        val words = folder.fold(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        return entries.filter { (_, text) -> words.all { it in text } }.map { it.first }
    }
}

class BuildJournalSearchIndex(private val folder: TextFolder) {
    operator fun invoke(entries: List<JournalEntry>): JournalSearchIndex =
        JournalSearchIndex(folder, entries.map { it to folder.fold(it.title + " " + it.body) })
}
