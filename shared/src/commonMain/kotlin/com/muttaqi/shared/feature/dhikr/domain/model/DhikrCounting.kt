package com.muttaqi.shared.feature.dhikr.domain.model

import kotlinx.datetime.LocalDate

fun Dhikr.isRoundComplete(count: Int): Boolean = target?.let { count >= it } ?: false

fun Dhikr.roundProgress(count: Int): Double {
    val target = target ?: return 0.0
    if (target <= 0) return 0.0
    return (count.toDouble() / target).coerceAtMost(1.0)
}

fun Dhikr.stepPosition(count: Int): DhikrStepPosition? {
    if (steps.isEmpty()) return null
    var start = 0
    steps.forEachIndexed { index, step ->
        if (count < start + step.count) return DhikrStepPosition(index, count - start)
        start += step.count
    }
    return DhikrStepPosition(steps.lastIndex, steps.last().count)
}

fun Dhikr.milestoneAt(count: Int): DhikrMilestone {
    if (target == count) return DhikrMilestone.RoundFinished
    var phraseEnd = 0
    for (step in steps.dropLast(1)) {
        phraseEnd += step.count
        if (count == phraseEnd) return DhikrMilestone.PhraseFinished
    }
    return DhikrMilestone.Repetition
}

fun DhikrProgress.counted(dhikr: Dhikr, today: LocalDate): DhikrProgress {
    val current = if (day == today) this else DhikrProgress.empty(today)
    val count = (if (dhikr.isRoundComplete(current.count)) 0 else current.count) + 1
    val rounds = if (dhikr.isRoundComplete(count)) current.rounds + 1 else current.rounds
    return current.copy(count = count, rounds = rounds)
}

fun DhikrProgress.repetitionsSaid(dhikr: Dhikr): Int {
    val target = dhikr.target ?: return count
    val finishedBefore = if (dhikr.isRoundComplete(count)) (rounds - 1).coerceAtLeast(0) else rounds
    return finishedBefore * target + count
}
