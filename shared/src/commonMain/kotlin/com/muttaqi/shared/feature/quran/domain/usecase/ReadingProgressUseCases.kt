package com.muttaqi.shared.feature.quran.domain.usecase

import com.muttaqi.shared.feature.quran.domain.model.QuranCompletion
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.StoredReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.feature.quran.domain.repository.ReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.repository.StoredProgressImportMarker
import com.muttaqi.shared.feature.quran.domain.repository.SurahRepository
import kotlin.time.Clock
import kotlin.time.Instant

class RecordReading(
    private val repository: ReadingProgressRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int) =
        repository.record(surahNumber, lastAyahNumber, readAyahs, totalAyahs, clock.now())
}

class GetLastReading(
    private val progress: ReadingProgressRepository,
    private val surahs: SurahRepository,
) {
    suspend operator fun invoke(): ReadingProgress? {
        val last = progress.lastRead() ?: return null
        val surah = surahs.surah(last.surahNumber) ?: return null
        return ReadingProgress(
            surahNumber = last.surahNumber,
            surahName = surah.name,
            surahEnglishName = surah.englishName,
            lastAyahNumber = last.lastAyahNumber,
            lastReadAt = last.lastReadAt,
        )
    }
}

class GetQuranCompletion(
    private val progress: ReadingProgressRepository,
    private val surahs: SurahRepository,
) {
    suspend operator fun invoke(): QuranCompletion = QuranCompletion(
        ayahsRead = progress.all().sumOf { it.readAyahs.size },
        totalAyahs = surahs.surahs().sumOf { it.numberOfAyahs },
    )
}

class ImportStoredReadingProgress(
    private val repository: ReadingProgressRepository,
    private val marker: StoredProgressImportMarker,
) {
    val isNeeded: Boolean get() = !marker.isImported

    suspend operator fun invoke(records: List<StoredReadingProgress>) {
        if (marker.isImported) return
        repository.merge(
            records.map {
                SurahProgress(
                    surahNumber = it.surahNumber,
                    lastAyahNumber = it.lastAyahNumber,
                    readAyahs = it.readAyahs.toSet(),
                    completedAyahs = it.completedAyahs,
                    totalAyahs = it.totalAyahs,
                    lastReadAt = Instant.fromEpochMilliseconds(it.lastReadAtEpochMillis),
                )
            },
        )
        marker.markImported()
    }
}
