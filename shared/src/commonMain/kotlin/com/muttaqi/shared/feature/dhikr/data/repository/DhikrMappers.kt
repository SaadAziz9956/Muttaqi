package com.muttaqi.shared.feature.dhikr.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.data.dto.DhikrBookDto
import com.muttaqi.shared.feature.dhikr.data.dto.Translations
import com.muttaqi.shared.feature.dhikr.domain.model.Dhikr
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrStep

private fun Translations.pick(language: Language): String? = this[language.code] ?: this[Language.English.code]

private fun Translations.shownLanguage(language: Language): String? =
    if (containsKey(language.code)) language.code else Language.English.code.takeIf { containsKey(it) }

private fun DhikrBookDto.Entry.creditIn(language: Language): String? {
    val credit = credit ?: return null
    val translationLanguages = (listOfNotNull(translation) + steps.orEmpty().map { it.translation }).mapNotNull { it.shownLanguage(language) }
    val names = translationLanguages.distinct().flatMap { credit.translation?.get(it).orEmpty() } +
        listOfNotNull(hadith?.shownLanguage(language)).flatMap { credit.hadith?.get(it).orEmpty() }
    return names.distinct().joinToString(", ").ifEmpty { null }
}

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
    credit = creditIn(language),
)
