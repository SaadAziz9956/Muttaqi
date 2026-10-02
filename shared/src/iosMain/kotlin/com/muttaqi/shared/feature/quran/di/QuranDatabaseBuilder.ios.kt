package com.muttaqi.shared.feature.quran.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.scope.Scope
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
internal actual fun Scope.quranDatabaseBuilder(): RoomDatabase.Builder<QuranDatabase> {
    val directory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    val path = requireNotNull(directory?.path) { "No Application Support directory" } + "/" + QuranDatabase.FILE_NAME
    return Room.databaseBuilder<QuranDatabase>(name = path)
}
