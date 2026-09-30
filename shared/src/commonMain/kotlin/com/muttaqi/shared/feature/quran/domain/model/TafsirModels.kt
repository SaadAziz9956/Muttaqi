package com.muttaqi.shared.feature.quran.domain.model

/** One passage of Tafsir Ibn Kathir, which often explains a group of ayahs together */
data class TafsirEntry(
    val surahNumber: Int,
    /** The first ayah it explains */
    val ayahNumber: Int,
    /** The last ayah it explains; the same as [ayahNumber] for a single ayah */
    val lastAyahNumber: Int,
    /** Paragraphs are separated by blank lines */
    val text: String,
) {
    val verseKey: String get() = "$surahNumber:$ayahNumber"

    fun covers(ayah: Int): Boolean = ayah in ayahNumber..lastAyahNumber

    /** One entry can run past 40,000 characters, so screens lay it out a paragraph at a time */
    val paragraphs: List<String> get() = text.split("\n\n")
}
