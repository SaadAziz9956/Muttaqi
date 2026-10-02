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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class RoomJournalRepository(
    private val dao: JournalDao,
    writeScope: CoroutineScope,
) : JournalReadRepository, JournalWriteRepository {

    private val writes = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    private val stored = MutableStateFlow<List<JournalEntry>?>(null)

    init {
        writes.trySend {
            stored.value = try {
                dao.observeAll().first().map { it.toEntry() }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                emptyList()
            }
        }
        writeScope.launch {
            for (write in writes) write()
        }
    }

    override fun entries(): Flow<List<JournalEntry>> = stored.filterNotNull()

    override suspend fun entry(id: String): JournalEntry? {
        stored.value?.let { entries -> return entries.firstOrNull { it.id == id } }
        return dao.find(id)?.toEntry()
    }

    override suspend fun save(entry: JournalEntry) = write {
        val entity = entry.toEntity()
        dao.upsert(entity)
        change { entries -> entries.filterNot { it.id == entry.id } + entity.toEntry() }
    }

    override suspend fun delete(id: String) = write {
        dao.delete(id)
        change { entries -> entries.filterNot { it.id == id } }
    }

    override suspend fun addMissing(entries: List<JournalEntry>) = write {
        val entities = entries.map { it.toEntity() }
        dao.insertMissing(entities)
        change { current ->
            val known = current.mapTo(mutableSetOf()) { it.id }
            current + entities.filter { known.add(it.id) }.map { it.toEntry() }
        }
    }

    private fun change(transform: (List<JournalEntry>) -> List<JournalEntry>) {
        stored.update { entries -> entries?.let(transform)?.sortedByDescending { it.createdAt } }
    }

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
