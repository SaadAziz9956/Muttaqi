package com.muttaqi.shared.feature.dhikr.domain.model

import kotlinx.datetime.LocalDate

data class Dhikr(
    val id: String,
    val title: String?,
    val arabic: String,
    val transliteration: String,
    val translation: String?,
    val steps: List<DhikrStep>,
    val count: Int?,
    val hadith: String?,
    val reference: String,
    val grade: String,
    val credit: String?,
) {
    val target: Int? get() = if (steps.isEmpty()) count else steps.sumOf { it.count }
}

data class DhikrStep(
    val arabic: String,
    val transliteration: String,
    val translation: String?,
    val count: Int,
)

data class DhikrSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val dhikr: List<Dhikr>,
)

data class DhikrProgress(
    val count: Int,
    val rounds: Int,
    val day: LocalDate,
) {
    companion object {
        fun empty(day: LocalDate) = DhikrProgress(count = 0, rounds = 0, day = day)
    }
}

data class DhikrStepPosition(val index: Int, val said: Int)

enum class DhikrMilestone { Repetition, PhraseFinished, RoundFinished }
