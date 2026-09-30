package com.muttaqi.shared.feature.names.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.model.inLanguage
import com.muttaqi.shared.feature.names.data.dto.NamesBookDto
import com.muttaqi.shared.feature.names.domain.model.AllahName

/** The names with their meanings in the language, or the English where there's none in it */
internal fun NamesBookDto.toNames(language: Language): List<AllahName> = names.map { entry ->
    AllahName(
        number = entry.number,
        arabic = entry.arabic,
        transliteration = entry.transliteration,
        meaning = entry.meaning.inLanguage(language),
    )
}
