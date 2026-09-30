package com.muttaqi.shared.feature.dhikr.domain.model

import kotlinx.datetime.LocalDate

// How a counter moves through a dhikr: pure rules, shared by the counter screen and the day's total on Home

/** Whether [count] repetitions finish a round; never for open-ended remembrance */
fun Dhikr.isRoundComplete(count: Int): Boolean = target?.let { count >= it } ?: false

/** Share of the current round said, from 0 to 1; always 0 for open-ended remembrance */
fun Dhikr.roundProgress(count: Int): Double {
    val target = target ?: return 0.0
    if (target <= 0) return 0.0
    return (count.toDouble() / target).coerceAtMost(1.0)
}

/** For a set said in parts: the phrase to say at [count], and how many of it have been said; null for one phrase */
fun Dhikr.stepPosition(count: Int): DhikrStepPosition? {
    if (steps.isEmpty()) return null
    var start = 0
    steps.forEachIndexed { index, step ->
        if (count < start + step.count) return DhikrStepPosition(index, count - start)
        start += step.count
    }
    return DhikrStepPosition(steps.lastIndex, steps.last().count)
}

/** What reaching [count] completed: the round, a phrase of a set, or just one more repetition */
fun Dhikr.milestoneAt(count: Int): DhikrMilestone {
    if (target == count) return DhikrMilestone.RoundFinished
    var phraseEnd = 0
    for (step in steps.dropLast(1)) {
        phraseEnd += step.count
        if (count == phraseEnd) return DhikrMilestone.PhraseFinished
    }
    return DhikrMilestone.Repetition
}

/** The progress after one more repetition on [today] */
fun DhikrProgress.counted(dhikr: Dhikr, today: LocalDate): DhikrProgress {
    // A count left from an earlier day, e.g. with the screen open past midnight, starts again
    val current = if (day == today) this else DhikrProgress.empty(today)
    // The tap after a finished round starts the next one
    val count = (if (dhikr.isRoundComplete(current.count)) 0 else current.count) + 1
    val rounds = if (dhikr.isRoundComplete(count)) current.rounds + 1 else current.rounds
    return current.copy(count = count, rounds = rounds)
}

/**
 * Repetitions said so far: the finished rounds and the current one. A round that has just finished is already among
 * [DhikrProgress.rounds] while its count still shows, so it's counted once
 */
fun DhikrProgress.repetitionsSaid(dhikr: Dhikr): Int {
    val target = dhikr.target ?: return count
    val finishedBefore = if (dhikr.isRoundComplete(count)) (rounds - 1).coerceAtLeast(0) else rounds
    return finishedBefore * target + count
}
