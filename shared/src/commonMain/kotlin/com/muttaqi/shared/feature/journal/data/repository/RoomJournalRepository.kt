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
