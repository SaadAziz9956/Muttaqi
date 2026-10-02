package com.muttaqi.shared.feature.topics.domain.usecase

import com.muttaqi.shared.core.text.TextFolder
import com.muttaqi.shared.feature.topics.domain.model.ExploreGroup
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic

data class ExploreSearchResult(val group: ExploreGroup, val topic: ExploreTopic)

class ExploreSearchIndex internal constructor(
    private val folder: TextFolder,
    private val entries: List<Entry>,
) {
    internal class Entry(
        val result: ExploreSearchResult,
        val names: String,
        val text: String,
    )

    fun search(query: String): List<ExploreSearchResult> {
        val words = folder.fold(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        val (byName, rest) = entries.partition { entry -> words.all { it in entry.names } }
        val byText = rest.filter { entry -> words.all { it in entry.names || it in entry.text } }
        return (byName + byText).map { it.result }
    }
}

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
