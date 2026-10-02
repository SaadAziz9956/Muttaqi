package com.muttaqi.shared.feature.journal.domain.repository

import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalReadRepository {
    fun entries(): Flow<List<JournalEntry>>

    suspend fun entry(id: String): JournalEntry?
}

interface JournalWriteRepository {
    suspend fun save(entry: JournalEntry)

    suspend fun delete(id: String)

    suspend fun addMissing(entries: List<JournalEntry>)
}

interface JournalImportStatus {
    val isImported: Boolean

    fun markImported()
}
