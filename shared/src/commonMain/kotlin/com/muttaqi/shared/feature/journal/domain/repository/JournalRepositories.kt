package com.muttaqi.shared.feature.journal.domain.repository

import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

/** Reading the journal */
interface JournalReadRepository {
    /** Every entry, then the entries again whenever one is written or deleted */
    fun entries(): Flow<List<JournalEntry>>

    suspend fun entry(id: String): JournalEntry?
}

/**
 * Writing the journal. Writes land in the order they were asked for, and finish even if the screen that asked has
 * gone, e.g. the save when the reader leaves an entry
 */
interface JournalWriteRepository {
    /** Inserts the entry, or updates the one with the same id */
    suspend fun save(entry: JournalEntry)

    suspend fun delete(id: String)

    /** Adds entries kept elsewhere before, with their own ids and dates; an entry already here is left as it is */
    suspend fun addMissing(entries: List<JournalEntry>)
}

/** Whether the entries written before the journal moved to shared code have been brought over */
interface JournalImportStatus {
    val isImported: Boolean

    fun markImported()
}
