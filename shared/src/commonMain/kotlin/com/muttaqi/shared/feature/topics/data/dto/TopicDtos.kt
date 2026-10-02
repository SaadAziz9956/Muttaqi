package com.muttaqi.shared.feature.topics.data.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PageQuoteDto(val reference: String, val translation: Map<String, String>)

@Serializable
internal data class VerseDto(
    val reference: String,
    val arabic: String,
    val translation: Map<String, String>,
)

@Serializable
internal data class HadithDto(
    val arabic: String,
    val translation: Map<String, String>,
    val attribution: Map<String, String>,
    val grade: Map<String, String>,
    val source: String,
)

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
