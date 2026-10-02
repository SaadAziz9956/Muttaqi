package com.muttaqi.shared.feature.journal

import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.repository.JournalImportStatus
import com.muttaqi.shared.feature.journal.domain.repository.JournalReadRepository
import com.muttaqi.shared.feature.journal.domain.repository.JournalWriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Instant

internal object JournalTestData {
    val now: Instant = Instant.parse("2026-09-30T10:00:00Z")

    val walk = entry("walk", "Morning light", "Grateful for the quiet walk before Fajr", "2026-09-30T05:10:00Z")
    val parents = entry("parents", "", "Alhamdulillah for my parents' health\nAnd for the rain", "2026-09-29T20:00:00Z")
    val cafe = entry("cafe", "Café with Ahmed", "Old friends", "2025-12-01T12:00:00Z")

    fun entry(id: String, title: String, body: String, createdAt: String) =
        Instant.parse(createdAt).let { JournalEntry(id, title, body, it, it) }
}

internal class FakeJournalRepository(vararg initial: JournalEntry) : JournalReadRepository, JournalWriteRepository {
    val stored = MutableStateFlow(initial.toList())
    val writes = mutableListOf<String>()

    override fun entries(): Flow<List<JournalEntry>> = stored

    override suspend fun entry(id: String): JournalEntry? = stored.value.firstOrNull { it.id == id }

    override suspend fun save(entry: JournalEntry) {
        writes += "save ${entry.id}: ${entry.title}|${entry.body}"
        stored.update { entries -> entries.filterNot { it.id == entry.id } + entry }
    }

    override suspend fun delete(id: String) {
        writes += "delete $id"
        stored.update { entries -> entries.filterNot { it.id == id } }
    }

    override suspend fun addMissing(entries: List<JournalEntry>) {
        writes += "add ${entries.map { it.id }}"
        stored.update { current -> current + entries.filter { new -> current.none { it.id == new.id } } }
    }
}

internal class FakeImportStatus(override var isImported: Boolean = false) : JournalImportStatus {
    override fun markImported() {
        isImported = true
    }
}

internal class FakeClock(var now: Instant = JournalTestData.now) : Clock {
    override fun now(): Instant = now
}
