package com.muttaqi.shared.feature.topics.data.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.core.model.inLanguage
import com.muttaqi.shared.core.quote.PublishedQuote
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.topics.data.dto.EmotionsBookDto
import com.muttaqi.shared.feature.topics.data.dto.ExploreBookDto
import com.muttaqi.shared.feature.topics.data.dto.HadithDto
import com.muttaqi.shared.feature.topics.data.dto.PageQuoteDto
import com.muttaqi.shared.feature.topics.data.dto.VerseDto
import com.muttaqi.shared.feature.topics.domain.model.Emotion
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import com.muttaqi.shared.feature.topics.domain.model.QuranPassage

internal fun PageQuoteDto.toPublishedQuote() = PublishedQuote(translation, "Quran ($reference)")

/** Jalandhry is credited only where his Urdu is shown; a verse without it shows (and credits) Saheeh International */
internal fun VerseDto.toPassage(language: Language) = QuranPassage(
    reference = reference,
    arabic = arabic,
    translation = translation.inLanguage(language),
    credit = if (language == Language.Urdu && translation.containsKey(Language.Urdu.code)) {
        "Fateh Muhammad Jalandhry"
    } else {
        "Saheeh International"
    },
)

internal fun HadithDto.toPassage(language: Language) = HadithPassage(
    arabic = arabic,
    translation = translation.inLanguage(language),
    attribution = attribution.inLanguage(language),
    grade = grade.inLanguage(language),
    credit = source,
)

/** Duas listed by their number in Hisn al-Muslim, e.g. 176, as the Dua feature's entries ("hisn-176") */
private fun List<Int>.toDuas(entries: Map<String, DuaEntry>): List<DuaEntry> = mapNotNull { entries["hisn-$it"] }

internal fun EmotionsBookDto.Entry.toEmotion(language: Language, duas: Map<String, DuaEntry>) = Emotion(
    id = id,
    title = title,
    verses = verses.map { it.toPassage(language) },
    hadith = hadith.map { it.toPassage(language) },
    duas = this.duas.toDuas(duas),
)

internal fun ExploreBookDto.Group.toGroup(language: Language, duas: Map<String, DuaEntry>) = ExploreGroup(
    id = id,
    title = title,
    topics = topics.map { topic ->
        ExploreTopic(
            id = topic.id,
            title = topic.title,
            icon = topic.icon,
            keywords = topic.keywords,
            verses = topic.verses.map { it.toPassage(language) },
            hadith = topic.hadith.map { it.toPassage(language) },
            duas = topic.duas.toDuas(duas),
        )
    },
)
