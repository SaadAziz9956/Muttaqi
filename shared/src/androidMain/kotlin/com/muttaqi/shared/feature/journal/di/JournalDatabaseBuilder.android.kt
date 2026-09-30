package com.muttaqi.shared.feature.journal.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.muttaqi.shared.feature.journal.data.local.JournalDatabase
import org.koin.core.scope.Scope

/** In the app's databases folder; the Context is the one the app gives Koin with `androidContext` */
internal actual fun Scope.journalDatabaseBuilder(): RoomDatabase.Builder<JournalDatabase> {
    val context = get<Context>().applicationContext
    return Room.databaseBuilder<JournalDatabase>(context, context.getDatabasePath(JournalDatabase.FILE_NAME).absolutePath)
}
