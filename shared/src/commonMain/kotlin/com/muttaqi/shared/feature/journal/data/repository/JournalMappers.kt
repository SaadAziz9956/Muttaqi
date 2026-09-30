package com.muttaqi.shared.feature.journal.data.repository

import com.muttaqi.shared.feature.journal.data.local.JournalEntryEntity
import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import kotlin.time.Instant

internal fun JournalEntryEntity.toEntry() = JournalEntry(
    id = id,
    title = title,
    body = body,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    updatedAt = Instant.fromEpochMilliseconds(updatedAt),
)

internal fun JournalEntry.toEntity() = JournalEntryEntity(
    id = id,
    title = title,
    body = body,
    createdAt = createdAt.toEpochMilliseconds(),
    updatedAt = updatedAt.toEpochMilliseconds(),
)
