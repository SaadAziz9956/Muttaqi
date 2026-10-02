package com.muttaqi.shared.feature.journal.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.muttaqi.shared.feature.journal.data.local.JournalDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun Scope.journalDatabaseBuilder(): RoomDatabase.Builder<JournalDatabase> {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(directory?.path) { "No Application Support directory" } + "/" + JournalDatabase.FILE_NAME
    return Room.databaseBuilder<JournalDatabase>(name = path)
}
