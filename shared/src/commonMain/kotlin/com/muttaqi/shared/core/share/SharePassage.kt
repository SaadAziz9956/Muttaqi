package com.muttaqi.shared.core.share

/** A verse, hadith, dua, dhikr or Name of Allah to share as a card, in the reader's language */
data class SharePassage(
    /** Empty when there is none */
    val arabic: String,
    val transliteration: String?,
    val translation: String,
    /** Where it's from, e.g. "Quran (2:255)" or "Narrated by Muslim · Authentic" */
    val reference: String,
)
