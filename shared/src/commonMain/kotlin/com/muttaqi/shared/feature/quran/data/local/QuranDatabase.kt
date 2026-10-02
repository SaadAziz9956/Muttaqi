package com.muttaqi.shared.feature.quran.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
    entities = [
        SurahEntity::class,
        AyahEntity::class,
        AyahTranslationEntity::class,
        ReadingProgressEntity::class,
        TafsirEntity::class,
    ],
    version = 1,
)
@ConstructedBy(QuranDatabaseConstructor::class)
internal abstract class QuranDatabase : RoomDatabase() {
    abstract fun textDao(): QuranTextDao
    abstract fun progressDao(): ReadingProgressDao
    abstract fun tafsirDao(): TafsirDao

    companion object {
        const val FILE_NAME = "quran.db"
    }
}

@Suppress("KotlinNoActualForExpect")
internal expect object QuranDatabaseConstructor : RoomDatabaseConstructor<QuranDatabase> {
    override fun initialize(): QuranDatabase
}
