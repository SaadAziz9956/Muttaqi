package com.muttaqi.shared.feature.quran.di

import androidx.room.Room
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import com.muttaqi.shared.feature.quran.data.local.open
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/** The database in Application Support, beside the SwiftData store it replaces */
internal actual val quranPlatformModule: Module = module {
    single<QuranDatabase> {
        Room.databaseBuilder<QuranDatabase>(name = "${applicationSupportPath()}/${QuranDatabase.FILE_NAME}").open()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun applicationSupportPath(): String {
    val url = NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )
    return requireNotNull(url?.path) { "No Application Support folder" }
}
