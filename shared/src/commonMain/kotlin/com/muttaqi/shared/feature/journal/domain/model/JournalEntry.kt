package com.muttaqi.shared.feature.journal.domain.model

import kotlin.time.Instant

data class JournalEntry(
    val id: String,
    val title: String,
    val body: String,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isEmpty: Boolean get() = title.isBlank() && body.isBlank()

    val preview: String
        get() {
            val title = title.trim()
            if (title.isNotEmpty()) return title
            return body.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
        }
}
