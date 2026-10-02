package com.muttaqi.shared.feature.quran.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert

@Dao
internal abstract class QuranTextDao {
    @Query("SELECT * FROM surahs ORDER BY number")
    abstract suspend fun surahs(): List<SurahEntity>

    @Query("SELECT * FROM surahs WHERE number = :number")
    abstract suspend fun surah(number: Int): SurahEntity?

    @Query("SELECT COUNT(*) FROM surahs")
    abstract suspend fun surahCount(): Int

    @Query("SELECT COUNT(*) FROM ayahs")
    abstract suspend fun ayahCount(): Int

    @Query(
        """
        SELECT a.*, t.text AS translation FROM ayahs a
        LEFT JOIN ayah_translations t ON t.ayahNumber = a.number AND t.language = :language
        WHERE a.surahNumber = :surahNumber ORDER BY a.numberInSurah
        """,
    )
    abstract suspend fun ayahs(surahNumber: Int, language: String): List<AyahRow>

    @Query(
        """
        SELECT a.*, t.text AS translation FROM ayahs a
        LEFT JOIN ayah_translations t ON t.ayahNumber = a.number AND t.language = :language
        WHERE a.surahNumber = :surahNumber AND a.numberInSurah = :numberInSurah LIMIT 1
        """,
    )
    abstract suspend fun ayah(surahNumber: Int, numberInSurah: Int, language: String): AyahRow?

    @Query("SELECT COUNT(*) FROM ayah_translations WHERE language = :language")
    abstract suspend fun translationCount(language: String): Int

    @Upsert
    protected abstract suspend fun upsertSurahs(surahs: List<SurahEntity>)

    @Upsert
    protected abstract suspend fun upsertAyahs(ayahs: List<AyahEntity>)

    @Query("DELETE FROM ayah_translations WHERE language = :language")
    protected abstract suspend fun deleteTranslations(language: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertTranslations(translations: List<AyahTranslationEntity>)

    @Transaction
    open suspend fun storeText(surahs: List<SurahEntity>, ayahs: List<AyahEntity>) {
        upsertSurahs(surahs)
        upsertAyahs(ayahs)
    }

    @Transaction
    open suspend fun replaceTranslation(language: String, translations: List<AyahTranslationEntity>) {
        deleteTranslations(language)
        insertTranslations(translations)
    }
}

@Dao
internal interface ReadingProgressDao {
    @Query("SELECT * FROM reading_progress WHERE surahNumber = :surahNumber")
    suspend fun progress(surahNumber: Int): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress ORDER BY lastReadAtEpochMillis DESC LIMIT 1")
    suspend fun lastRead(): ReadingProgressEntity?

    @Query("SELECT * FROM reading_progress ORDER BY surahNumber")
    suspend fun all(): List<ReadingProgressEntity>

    @Upsert
    suspend fun upsert(progress: List<ReadingProgressEntity>)
}

@Dao
internal interface TafsirDao {
    @Query("SELECT * FROM tafsir WHERE surahNumber = :surahNumber AND language = :language ORDER BY ayahNumber")
    suspend fun tafsir(surahNumber: Int, language: String): List<TafsirEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entries: List<TafsirEntity>)
}
