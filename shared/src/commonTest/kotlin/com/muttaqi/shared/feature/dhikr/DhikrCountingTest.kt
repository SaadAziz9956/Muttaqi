package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.feature.dhikr.DhikrTestData.openEnded
import com.muttaqi.shared.feature.dhikr.DhikrTestData.set
import com.muttaqi.shared.feature.dhikr.DhikrTestData.single
import com.muttaqi.shared.feature.dhikr.DhikrTestData.today
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrMilestone
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStepPosition
import com.muttaqi.shared.feature.dhikr.domain.model.counted
import com.muttaqi.shared.feature.dhikr.domain.model.isRoundComplete
import com.muttaqi.shared.feature.dhikr.domain.model.milestoneAt
import com.muttaqi.shared.feature.dhikr.domain.model.repetitionsSaid
import com.muttaqi.shared.feature.dhikr.domain.model.roundProgress
import com.muttaqi.shared.feature.dhikr.domain.model.stepPosition
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DhikrCountingTest {
    private fun DhikrProgress.countedTimes(times: Int, dhikr: Dhikr) =
        (1..times).fold(this) { progress, _ -> progress.counted(dhikr, today) }

    @Test
    fun aSetsTargetIsItsStepsAddedUp() {
        assertEquals(5, set.target)
        assertEquals(3, single.target)
        assertNull(openEnded.target)
    }

    @Test
    fun theRoundCompletesAtTheTargetAndTheNextTapStartsAnother() {
        val complete = DhikrProgress.empty(today).countedTimes(3, single)
        assertEquals(DhikrProgress(3, 1, today), complete)
        assertTrue(single.isRoundComplete(complete.count))
        assertEquals(1.0, single.roundProgress(complete.count))
        assertEquals(DhikrProgress(1, 1, today), complete.counted(single, today))
    }

    @Test
    fun openEndedRemembranceJustCountsUp() {
        val progress = DhikrProgress.empty(today).countedTimes(40, openEnded)
        assertEquals(DhikrProgress(40, 0, today), progress)
        assertFalse(openEnded.isRoundComplete(progress.count))
        assertEquals(0.0, openEnded.roundProgress(progress.count))
    }

    @Test
    fun aCountFromAnEarlierDayStartsAgain() {
        val yesterday = DhikrProgress(2, 4, LocalDate(2026, 9, 29))
        assertEquals(DhikrProgress(1, 0, today), yesterday.counted(single, today))
    }

    @Test
    fun theCurrentStepMovesThroughTheSet() {
        assertEquals(DhikrStepPosition(0, 0), set.stepPosition(0))
        assertEquals(DhikrStepPosition(0, 1), set.stepPosition(1))
        assertEquals(DhikrStepPosition(1, 0), set.stepPosition(2))
        assertEquals(DhikrStepPosition(2, 0), set.stepPosition(4))
        assertEquals(DhikrStepPosition(2, 1), set.stepPosition(5))
        assertNull(single.stepPosition(1))
    }

    @Test
    fun milestonesMarkTheEndOfEachPhraseAndOfTheRound() {
        assertEquals(
            listOf(
                DhikrMilestone.Repetition,
                DhikrMilestone.PhraseFinished,
                DhikrMilestone.Repetition,
                DhikrMilestone.PhraseFinished,
                DhikrMilestone.RoundFinished,
            ),
            (1..5).map { set.milestoneAt(it) },
        )
        assertEquals(DhikrMilestone.RoundFinished, single.milestoneAt(3))
        assertEquals(DhikrMilestone.Repetition, openEnded.milestoneAt(33))
    }

    @Test
    fun repetitionsSaidCountAJustFinishedRoundOnce() {
        assertEquals(2, DhikrProgress(2, 0, today).repetitionsSaid(single))
        assertEquals(3, DhikrProgress(3, 1, today).repetitionsSaid(single))
        assertEquals(4, DhikrProgress(1, 1, today).repetitionsSaid(single))
        assertEquals(6, DhikrProgress(3, 2, today).repetitionsSaid(single))
        assertEquals(40, DhikrProgress(40, 0, today).repetitionsSaid(openEnded))
    }
}
