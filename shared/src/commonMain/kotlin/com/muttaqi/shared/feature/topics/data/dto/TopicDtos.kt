package com.muttaqi.shared.feature.topics.data.dto

import kotlinx.serialization.Serializable

// The shape Emotions.json and Explore.json share. Translations are keyed by language code, e.g. {"en": …, "ur": …}

/** The verse under a page's title */
@Serializable
internal data class PageQuoteDto(val reference: String, val translation: Map<String, String>)

/** Uthmani Arabic, Saheeh International and Fateh Muhammad Jalandhry; part-verses cut to the same words in all three */
@Serializable
internal data class VerseDto(
    val reference: String,
    val arabic: String,
    val translation: Map<String, String>,
)

/** A hadith graded sahih or hasan, with HadeethEnc's published Arabic, English and Urdu, in full */
@Serializable
internal data class HadithDto(
    val arabic: String,
    val translation: Map<String, String>,
    val attribution: Map<String, String>,
    val grade: Map<String, String>,
    val source: String,
)

/** Emotions.json; each emotion lists its duas by their number in Hisn al-Muslim */
@Serializable
internal data class EmotionsBookDto(val header: PageQuoteDto, val emotions: List<Entry>) {
    @Serializable
    data class Entry(
        val id: String,
        val title: String,
        val verses: List<VerseDto>,
        val hadith: List<HadithDto>,
        val duas: List<Int>,
    )
}

/** Explore.json: topics in groups; each topic lists its duas by their number in Hisn al-Muslim */
@Serializable
internal data class ExploreBookDto(val header: PageQuoteDto, val groups: List<Group>) {
    @Serializable
    data class Group(val id: String, val title: String, val topics: List<Topic>)

    @Serializable
    data class Topic(
        val id: String,
        val title: String,
        val icon: String,
        val keywords: List<String>,
        val verses: List<VerseDto>,
        val hadith: List<HadithDto>,
        val duas: List<Int>,
    )
}
