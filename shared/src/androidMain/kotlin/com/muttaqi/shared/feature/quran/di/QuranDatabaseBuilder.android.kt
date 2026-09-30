package com.muttaqi.shared.feature.quran.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.muttaqi.shared.feature.quran.data.local.QuranDatabase
import org.koin.core.scope.Scope

/** In the app's databases folder; the Context is the one the app gives Koin with `androidContext` */
internal actual fun Scope.quranDatabaseBuilder(): RoomDatabase.Builder<QuranDatabase> {
    val context = get<Context>().applicationContext
    return Room.databaseBuilder<QuranDatabase>(context, context.getDatabasePath(QuranDatabase.FILE_NAME).absolutePath)
}
