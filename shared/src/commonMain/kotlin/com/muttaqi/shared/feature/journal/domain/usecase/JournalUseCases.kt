package com.muttaqi.shared.feature.journal.domain.usecase

import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.repository.JournalImportStatus
import com.muttaqi.shared.feature.journal.domain.repository.JournalReadRepository
import com.muttaqi.shared.feature.journal.domain.repository.JournalWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Every entry, newest first, and again after each change */
class ObserveJournalEntries(private val repository: JournalReadRepository) {
    operator fun invoke(): Flow<List<JournalEntry>> = repository.entries().map { entries -> entries.sortedByDescending { it.createdAt } }
}

/** One entry by id, e.g. to open it for editing */
class GetJournalEntry(private val repository: JournalReadRepository) {
    suspend operator fun invoke(id: String): JournalEntry? = repository.entry(id)
}

/** A blank entry for now, with an id of its own; it's only saved once something is written in it */
class NewJournalEntry @OptIn(ExperimentalUuidApi::class) constructor(
    private val clock: Clock,
    private val newId: () -> String = { Uuid.random().toString() },
) {
    operator fun invoke(): JournalEntry {
        val now = clock.now()
        return JournalEntry(id = newId(), title = "", body = "", createdAt = now, updatedAt = now)
    }
}

/** Saves the entry, or deletes it once it's empty, so an untouched or cleared entry is never kept */
class SaveJournalEntry(private val repository: JournalWriteRepository) {
    suspend operator fun invoke(entry: JournalEntry) {
        if (entry.isEmpty) repository.delete(entry.id) else repository.save(entry)
    }
}

class DeleteJournalEntry(private val repository: JournalWriteRepository) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}

/** The newest entry written today, if there is one, e.g. for the Journal tile on Home */
class ObserveTodaysJournalEntry(
    private val observeEntries: ObserveJournalEntries,
    private val clock: Clock,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) {
    operator fun invoke(): Flow<JournalEntry?> = observeEntries().map { entries ->
        val zone = timeZone()
        val today = clock.now().toLocalDateTime(zone).date
        entries.firstOrNull { it.createdAt.toLocalDateTime(zone).date == today }
    }
}

/**
 * Brings over the entries written before the journal moved to shared code (on iOS, from SwiftData), once. It's marked
 * done only after every entry is in, so if it's interrupted it runs again at the next launch, and entries it already
 * brought over are left as they are
 */
class ImportJournalEntries(
    private val repository: JournalWriteRepository,
    private val status: JournalImportStatus,
) {
    val isNeeded: Boolean get() = !status.isImported

    suspend operator fun invoke(entries: List<JournalEntry>) {
        if (status.isImported) return
        repository.addMissing(entries)
        status.markImported()
    }
}
