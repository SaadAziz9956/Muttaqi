package com.muttaqi.shared.feature.dua.domain.model

data class DuaEntry(
    val id: String,
    val arabic: String,
    val transliteration: String,
    val translation: String,
    val repeatCount: Int,
    val source: String,
    val reference: String,
    val translationCredit: String,
    val grade: String? = null,
) {
    val sourceAndGrade: String get() = listOfNotNull(source, grade).joinToString(" · ")
}

data class DuaChapter(
    val id: String,
    val title: String,
    val titleArabic: String?,
    val entries: List<DuaEntry>,
)

data class DuaCategory(
    val id: String,
    val title: String,
    val chapters: List<DuaChapter>,
) {
    val entryCount: Int get() = chapters.sumOf { it.entries.size }
}

data class QuranicDua(
    val surahNumber: Int,
    val ayahNumber: Int,
    val arabic: String,
    val transliteration: String,
    val translation: String,
) {
    val reference: String get() = "$surahNumber:$ayahNumber"
}
