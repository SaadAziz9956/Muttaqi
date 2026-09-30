package com.muttaqi.shared.feature.dhikr.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.data.dto.DhikrBookDto
import com.muttaqi.shared.feature.dhikr.data.dto.Translations
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStep

/** The published translation in the language, or the English where there's none in it; null when there's neither */
private fun Translations.pick(language: Language): String? = this[language.code] ?: this[Language.English.code]

internal fun DhikrBookDto.toSections(language: Language): List<DhikrSection> = sections.map { section ->
    DhikrSection(
        id = section.id,
        title = section.title,
        subtitle = section.subtitle,
        dhikr = section.dhikr.map { it.toDhikr(language) },
    )
}

private fun DhikrBookDto.Entry.toDhikr(language: Language) = Dhikr(
    id = id,
    title = title,
    arabic = arabic,
    transliteration = transliteration,
    translation = translation?.pick(language),
    steps = steps.orEmpty().map { step ->
        DhikrStep(
            arabic = step.arabic,
            transliteration = step.transliteration,
            translation = step.translation.pick(language),
            count = step.count,
        )
    },
    count = count,
    hadith = hadith?.pick(language),
    reference = reference,
    grade = grade,
    credit = credit?.pick(language),
)
