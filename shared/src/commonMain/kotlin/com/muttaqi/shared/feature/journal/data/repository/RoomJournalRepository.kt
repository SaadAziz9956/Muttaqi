package com.muttaqi.shared.feature.journal.data.repository

import com.muttaqi.shared.feature.journal.data.local.JournalDao
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.repository.JournalReadRepository
import com.muttaqi.shared.feature.journal.domain.repository.JournalWriteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The journal in its Room database. Writes run one at a time, in the order they were asked for, in [writeScope],
 * which lives as long as the app: a save asked for as the reader leaves an entry still lands after that screen's
 * view model is gone, and a delete asked for after a save can't overtake it.
 */
internal class RoomJournalRepository(
    private val dao: JournalDao,
    writeScope: CoroutineScope,
) : JournalReadRepository, JournalWriteRepository {

    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        writeScope.launch {
            for (write in writes) write()
        }
    }

    override fun entries(): Flow<List<JournalEntry>> = dao.observeAll().map { entities -> entities.map { it.toEntry() } }

    override suspend fun entry(id: String): JournalEntry? = dao.find(id)?.toEntry()

    override suspend fun save(entry: JournalEntry) = write { dao.upsert(entry.toEntity()) }

    override suspend fun delete(id: String) = write { dao.delete(id) }

    override suspend fun addMissing(entries: List<JournalEntry>) = write { dao.insertMissing(entries.map { it.toEntity() }) }

    /** Queues the write and waits for it; if the caller stops waiting, the write still happens */
    private suspend fun write(block: suspend () -> Unit) {
        val done = CompletableDeferred<Unit>()
        writes.trySend {
            try {
                block()
                done.complete(Unit)
            } catch (cancelled: CancellationException) {
                done.cancel(cancelled)
                throw cancelled
            } catch (failure: Exception) {
                done.completeExceptionally(failure)
            }
        }
        done.await()
    }
}
