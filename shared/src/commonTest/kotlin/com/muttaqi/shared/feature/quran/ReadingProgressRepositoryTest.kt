package com.muttaqi.shared.feature.quran

import com.muttaqi.shared.feature.quran.data.repository.RoomReadingProgressRepository
import com.muttaqi.shared.feature.quran.domain.model.SurahProgress
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ReadingProgressRepositoryTest {
    private val dao = FakeReadingProgressDao()
    private val repository = RoomReadingProgressRepository(dao)
    private val start = Instant.fromEpochMilliseconds(1_790_000_000_000)

    @Test
    fun readAyahsAddUpAndThePositionMoves() = runTest {
        repository.record(2, lastAyahNumber = 1, readAyahs = setOf(1, 2, 3), totalAyahs = 286, at = start)
        repository.record(2, lastAyahNumber = 2, readAyahs = setOf(2, 3, 4), totalAyahs = 286, at = start + 1.minutes)
        val progress = repository.progress(2)!!
        assertEquals(2, progress.lastAyahNumber)
        assertEquals(setOf(1, 2, 3, 4), progress.readAyahs)
        assertEquals(4, progress.completedAyahs)
        assertEquals("1,2,3,4", dao.rows.getValue(2).readAyahs)
    }

    @Test
    fun theLastReadIsTheMostRecent() = runTest {
        repository.record(18, 5, setOf(5), 110, start + 1.minutes)
        repository.record(2, 3, setOf(3), 286, start)
        assertEquals(18, repository.lastRead()!!.surahNumber)
        assertEquals(listOf(2, 18), repository.all().map { it.surahNumber })
    }

    @Test
    fun mergingKeepsTheMoreRecentPositionAndEveryAyahRead() = runTest {
        repository.record(2, 10, setOf(9, 10), 286, start + 5.minutes)
        repository.merge(
            listOf(
                SurahProgress(2, 3, setOf(1, 2, 3), completedAyahs = 3, totalAyahs = 286, lastReadAt = start),
                SurahProgress(1, 2, (1..7).toSet(), completedAyahs = 7, totalAyahs = 7, lastReadAt = start),
            ),
        )
        val baqara = repository.progress(2)!!
        assertEquals(10, baqara.lastAyahNumber)
        assertEquals(setOf(1, 2, 3, 9, 10), baqara.readAyahs)
        assertEquals(start + 5.minutes, baqara.lastReadAt)
        assertEquals(7, repository.progress(1)!!.completedAyahs)
    }

    @Test
    fun aCountFromBeforeReadAyahsWereKeptIsNotLost() = runTest {
        repository.merge(listOf(SurahProgress(3, 40, emptySet(), completedAyahs = 40, totalAyahs = 200, lastReadAt = start)))
        assertEquals(40, repository.progress(3)!!.completedAyahs)
    }
}
