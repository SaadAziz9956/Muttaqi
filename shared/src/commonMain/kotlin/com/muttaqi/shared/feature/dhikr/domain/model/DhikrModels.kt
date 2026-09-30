package com.muttaqi.shared.feature.dhikr.domain.model

import kotlinx.datetime.LocalDate

/** A phrase of remembrance, how many times the hadith says to repeat it, and why, in the reader's language */
data class Dhikr(
    val id: String,
    /** Names a set of phrases, e.g. "After every prayer · 33, 33 and 34"; null for a single phrase */
    val title: String?,
    val arabic: String,
    val transliteration: String,
    /** Published translation of the words; null when there's none, e.g. where a translator left them in Arabic */
    val translation: String?,
    /** Empty unless the dhikr is a set said in parts */
    val steps: List<DhikrStep>,
    /** Times to say a single phrase; null when the hadith gives no number */
    val count: Int?,
    /** The hadith giving its virtue, in a published translation; null when there's none, e.g. for some verses */
    val hadith: String?,
    val reference: String,
    val grade: String,
    /** Whose translations are shown, e.g. "HadeethEnc.com" */
    val credit: String?,
) {
    /** Times to say it in one go, the steps' counts added up for a set; null for open-ended remembrance */
    val target: Int? get() = if (steps.isEmpty()) count else steps.sumOf { it.count }
}

/** One phrase of a set said in parts, e.g. SubhanAllah 33 times before moving on to Alhamdulillah */
data class DhikrStep(
    val arabic: String,
    val transliteration: String,
    /** Published translation; null when there's none */
    val translation: String?,
    val count: Int,
)

/** A tab on the Dikr page, e.g. "Tasbih · Glorifying Allah" */
data class DhikrSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val dhikr: List<Dhikr>,
)

/** How far the reader has got with one dhikr today */
data class DhikrProgress(
    /** Repetitions in the current round */
    val count: Int,
    /** Rounds finished today; only counted for a dhikr with a target */
    val rounds: Int,
    /** The day this progress belongs to; any other day starts again from zero */
    val day: LocalDate,
) {
    companion object {
        fun empty(day: LocalDate) = DhikrProgress(count = 0, rounds = 0, day = day)
    }
}

/** For a set said in parts: the phrase to say now, and how many of it have been said */
data class DhikrStepPosition(val index: Int, val said: Int)

/** What a repetition completed, so each app can give a stronger haptic at the end of a phrase or a round */
enum class DhikrMilestone { Repetition, PhraseFinished, RoundFinished }
