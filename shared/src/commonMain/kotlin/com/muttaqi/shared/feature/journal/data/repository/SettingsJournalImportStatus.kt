package com.muttaqi.shared.feature.journal.data.repository

import com.muttaqi.shared.feature.journal.domain.repository.JournalImportStatus
import com.russhwolf.settings.Settings

/** Kept in the app's settings (the standard user defaults on iOS), so the import runs once per install */
internal class SettingsJournalImportStatus(private val settings: Settings) : JournalImportStatus {
    override val isImported: Boolean get() = settings.getBoolean(KEY, false)

    override fun markImported() = settings.putBoolean(KEY, true)

    private companion object {
        const val KEY = "journal_imported_from_swiftdata"
    }
}
