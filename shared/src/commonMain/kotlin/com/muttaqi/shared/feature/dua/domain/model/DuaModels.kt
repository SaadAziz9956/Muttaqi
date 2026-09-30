package com.muttaqi.shared.feature.dua.domain.model

/** One dua or dhikr, with where it comes from, in the reader's language */
data class DuaEntry(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    /** How many times to say it; 1 when the source gives no count */
    val repeatCount: Int,
    /** Short English attribution, e.g. "Bukhari · Muslim" */
    val source: String,
    /** Full reference as the book gives it, e.g. «البخاري مع الفتح 11/113 ومسلم 4/2083»; empty when there's none */
    val reference: String,
    /** Whose translation is shown, e.g. "Saheeh International" */
    val translationCredit: String,
)

/** A chapter of related duas, e.g. "What to say before sleeping" */
data class DuaChapter(
    val id: String,
    val title: String,
    val titleArabic: String?,
    val entries: List<DuaEntry>,
)

/** A group of chapters shown as one tile on the Dua tab, e.g. "Sleep & Waking" */
data class DuaCategory(
    val id: String,
    val title: String,
    val chapters: List<DuaChapter>,
) {
    val entryCount: Int get() = chapters.sumOf { it.entries.size }
}

/** A Quranic supplication: the dua portion of an ayah, e.g. for the Dua of the Day */
data class QuranicDua(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
) {
    val reference: String get() = "$surahNumber:$ayahNumber"
}
