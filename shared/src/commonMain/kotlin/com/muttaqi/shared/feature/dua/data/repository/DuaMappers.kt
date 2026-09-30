package com.muttaqi.shared.feature.dua.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.model.inLanguage
import com.muttaqi.shared.feature.dua.data.dto.HisnBookDto
import com.muttaqi.shared.feature.dua.data.dto.QuranicDuaDto
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory
import com.muttaqi.shared.feature.dua.domain.model.DuaChapter
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua

internal const val HISN_CREDIT = "Hisn al-Muslim (hisnmuslim.com)"

internal fun QuranicDuaDto.toQuranicDua(language: Language) = QuranicDua(
    surahNumber = surah,
    ayahNumber = ayah,
    arabic = arabic,
    transliteration = transliteration,
    translation = translations.inLanguage(language),
)

/** The Quranic duas as the first category, "Rabbana Duas", in one chapter */
internal fun List<QuranicDuaDto>.toRabbanaCategory(language: Language): DuaCategory {
    val entries = map { dua ->
        DuaEntry(
            id = "quran-${dua.surah}:${dua.ayah}",
            arabic = dua.arabic,
            transliteration = dua.transliteration,
            translation = dua.translations.inLanguage(language),
            repeatCount = 1,
            source = "Quran ${dua.surah}:${dua.ayah}",
            reference = "",
            translationCredit = if (language == Language.Urdu && dua.translations.containsKey(Language.Urdu.code)) {
                "Fateh Muhammad Jalandhry"
            } else {
                "Saheeh International"
            },
        )
    }
    return DuaCategory(
        id = "rabbana",
        title = "Rabbana Duas",
        chapters = listOf(DuaChapter(id = "rabbana", title = "Rabbana Duas", titleArabic = "أدعية من القرآن", entries = entries)),
    )
}

/** Hisn al-Muslim's categories; Urdu where a published translation exists, otherwise the English */
internal fun HisnBookDto.toCategories(language: Language): List<DuaCategory> = categories.map { category ->
    DuaCategory(
        id = category.id,
        title = category.title,
        chapters = category.chapters.map { chapter ->
            DuaChapter(
                id = "hisn-${chapter.id}",
                title = chapter.title,
                titleArabic = chapter.titleArabic,
                entries = chapter.duas.map { it.toEntry(language) },
            )
        },
    )
}

private fun HisnBookDto.Entry.toEntry(language: Language): DuaEntry {
    val urdu = translationUrdu?.takeIf { language == Language.Urdu }
    return DuaEntry(
        id = "hisn-$id",
        arabic = arabic,
        transliteration = transliteration,
        translation = urdu ?: translation,
        repeatCount = repeatCount.coerceAtLeast(1),
        source = source,
        reference = reference,
        translationCredit = urdu?.let { translationUrduCredit } ?: HISN_CREDIT,
    )
}
