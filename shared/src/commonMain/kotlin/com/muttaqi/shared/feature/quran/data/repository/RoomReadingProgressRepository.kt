package com.muttaqi.shared.feature.quran.data.repository

import com.muttaqi.shared.feature.quran.data.local.ReadingProgressDao
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import com.muttaqi.shared.feature.quran.domain.repository.ReadingProgressRepository
import kotlin.concurrent.Volatile
import kotlin.time.Instant

internal class RoomReadingProgressRepository(private val dao: ReadingProgressDao) : ReadingProgressRepository {

    override suspend fun progress(surahNumber: Int): SurahProgress? = dao.progress(surahNumber)?.toDomain()

    @Volatile
    private var latest: Latest? = null

    override suspend fun lastRead(): SurahProgress? {
        latest?.let { return it.progress }
        return dao.lastRead()?.toDomain().also { if (latest == null) latest = Latest(it) }
    }

    override suspend fun all(): List<SurahProgress> = dao.all().map { it.toDomain() }

    override suspend fun record(surahNumber: Int, lastAyahNumber: Int, readAyahs: Set<Int>, totalAyahs: Int, at: Instant) {
        val read = dao.progress(surahNumber)?.toDomain()?.readAyahs.orEmpty() + readAyahs
        val progress = SurahProgress(
            surahNumber = surahNumber,
            lastAyahNumber = lastAyahNumber,
            readAyahs = read,
            completedAyahs = read.size,
            totalAyahs = totalAyahs,
            lastReadAt = at,
        )
        dao.upsert(listOf(progress.toEntity()))
        val cached = latest?.progress
        latest = if (latest != null && (cached == null || cached.lastReadAt <= at)) Latest(progress) else null
    }

    override suspend fun merge(records: List<SurahProgress>) {
        val merged = records.map { record ->
            val existing = dao.progress(record.surahNumber)?.toDomain() ?: return@map record
            val latest = if (existing.lastReadAt >= record.lastReadAt) existing else record
            val read = existing.readAyahs + record.readAyahs
            latest.copy(
                readAyahs = read,
                completedAyahs = maxOf(read.size, existing.completedAyahs, record.completedAyahs),
                totalAyahs = maxOf(existing.totalAyahs, record.totalAyahs),
            )
        }
        dao.upsert(merged.map { it.toEntity() })
        latest = null
    }
}

private class Latest(val progress: SurahProgress?)
