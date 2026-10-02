package com.muttaqi.shared.feature.journal

import com.muttaqi.shared.feature.journal.domain.model.JournalEntry
import com.muttaqi.shared.feature.journal.domain.usecase.ImportJournalEntries
import com.muttaqi.shared.feature.journal.presentation.entry.JournalEntryViewModel
import com.muttaqi.shared.feature.journal.presentation.list.JournalListViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970
import kotlin.math.roundToLong
import kotlin.time.Instant

object JournalViewModels : KoinComponent {
    fun list(): JournalListViewModel = get()

    fun entry(id: String?): JournalEntryViewModel = get { parametersOf(id) }
}

object JournalSwiftDataImport : KoinComponent {
    val isNeeded: Boolean get() = get<ImportJournalEntries>().isNeeded

    fun entry(id: String, title: String, body: String, createdAt: NSDate, updatedAt: NSDate) = JournalEntry(
        id = id,
        title = title,
        body = body,
        createdAt = createdAt.toInstant(),
        updatedAt = updatedAt.toInstant(),
    )

    @Throws(Exception::class)
    suspend fun importEntries(entries: List<JournalEntry>) = get<ImportJournalEntries>()(entries)
}

val JournalEntry.createdDate: NSDate get() = createdAt.toNSDate()

private fun NSDate.toInstant(): Instant = Instant.fromEpochMilliseconds((timeIntervalSince1970 * 1000).roundToLong())

private fun Instant.toNSDate(): NSDate = NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / 1000.0)
