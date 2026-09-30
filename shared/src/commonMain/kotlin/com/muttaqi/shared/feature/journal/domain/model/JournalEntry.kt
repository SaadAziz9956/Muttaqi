package com.muttaqi.shared.feature.journal.domain.model

import kotlin.time.Instant

/** One entry in the gratitude journal */
data class JournalEntry(
    val id: String,
    val title: String,
    val body: String,
    /** The day the entry was written, shown in the list and on the entry */
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /** Nothing worth keeping, so an untouched or cleared entry is never saved */
    val isEmpty: Boolean get() = title.isBlank() && body.isBlank()

    /** The line shown in the list: the title, or the body's first line when there's no title */
    val preview: String
        get() {
            val title = title.trim()
            if (title.isNotEmpty()) return title
            return body.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() }.orEmpty()
        }
}
