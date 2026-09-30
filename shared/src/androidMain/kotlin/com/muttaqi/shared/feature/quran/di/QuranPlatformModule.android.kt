package com.muttaqi.shared.feature.quran.di

import android.content.Context
import androidx.room.Room
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import com.muttaqi.shared.feature.quran.data.local.open
import org.koin.core.module.Module
import org.koin.dsl.module

/** The database in the app's own databases folder; the Context is the one the app gives Koin at launch */
internal actual val quranPlatformModule: Module = module {
    single<QuranDatabase> {
        val context = get<Context>()
        Room.databaseBuilder<QuranDatabase>(context, context.getDatabasePath(QuranDatabase.FILE_NAME).absolutePath).open()
    }
}
