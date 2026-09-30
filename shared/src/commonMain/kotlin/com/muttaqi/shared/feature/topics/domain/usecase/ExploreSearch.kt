package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic

/** A topic that matches a search, with the group it sits in */
data class ExploreSearchResult(val group: ExploreGroup, val topic: ExploreTopic)

/** The topics folded for search once, so each keystroke only compares */
class ExploreSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Entry>,
) {
    internal class Entry(
        val result: ExploreSearchResult,
        /** The topic's title, its group's title and its keywords */
        val names: String,
        /** Its verses', hadith's and duas' translations, and the duas' transliterations */
        val text: String,
    )

    /**
     * Topics containing every word of the query: first those whose title, keywords or group match, e.g. "zakah" for
     * Charity & Zakat, then those whose verses, hadith or duas mention it
     */
    fun search(query: String): List<ExploreSearchResult> {
        val words = folder.fold(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        val (byName, rest) = entries.partition { entry -> words.all { it in entry.names } }
        val byText = rest.filter { entry -> words.all { it in entry.names || it in entry.text } }
        return (byName + byText).map { it.result }
    }
}

/** Builds the search index for Explore's groups, in the language they were loaded in */
class BuildExploreSearchIndex(private val folder: TextFolder) {
    operator fun invoke(groups: List<ExploreGroup>): ExploreSearchIndex = ExploreSearchIndex(
        folder,
        groups.flatMap { group ->
            group.topics.map { topic ->
                val names = listOf(topic.title, group.title) + topic.keywords
                val text = topic.verses.map { it.translation } + topic.hadith.map { it.translation } +
                    topic.duas.flatMap { listOf(it.translation, it.transliteration) }
                ExploreSearchIndex.Entry(
                    ExploreSearchResult(group, topic),
                    folder.fold(names.joinToString(" ")),
                    folder.fold(text.joinToString(" ")),
                )
            }
        },
    )
}
