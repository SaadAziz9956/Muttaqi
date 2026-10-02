package com.muttaqi.shared.feature.quran.domain.model

data class TafsirEntry(
    val surahNumber: Int,
    val ayahNumber: Int,
    val lastAyahNumber: Int,
    val text: String,
) {
    val verseKey: String get() = "$surahNumber:$ayahNumber"

    fun covers(ayah: Int): Boolean = ayah in ayahNumber..lastAyahNumber

    val paragraphs: List<String> get() = text.split("\n\n")
}
