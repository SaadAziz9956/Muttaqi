package com.muttaqi.shared.feature.topics.presentation.page

import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.topics.domain.model.PassageTopic

internal object TopicPageReducer : Reducer<TopicPageState, TopicPageMutation> {
    override fun reduce(state: TopicPageState, mutation: TopicPageMutation): TopicPageState = when (mutation) {
        is TopicPageMutation.Loaded -> state.copy(isLoading = false, topics = mutation.topics).showing(state.selectedId, state.section)
        is TopicPageMutation.TopicSelected -> state.showing(mutation.topicId, state.section)
        is TopicPageMutation.SectionSelected -> state.showing(state.selectedId, mutation.section)
        TopicPageMutation.LoadFailed -> state.copy(isLoading = false)
    }

    private fun TopicPageState.showing(topicId: String, section: TopicSection): TopicPageState {
        val topic = topics.firstOrNull { it.id == topicId } ?: topics.firstOrNull()
            ?: return copy(sections = emptyList(), passages = emptyList(), translationCredits = emptyList())
        val sections = TopicSection.entries.filter { topic.passages(it).isNotEmpty() }
        val shown = if (section in sections) section else sections.firstOrNull() ?: section
        return copy(
            selectedId = topic.id,
            sections = sections,
            section = shown,
            passages = topic.passages(shown),
            translationCredits = topic.credits(shown),
        )
    }
}

internal fun PassageTopic.passages(section: TopicSection): List<TopicPassage> = when (section) {
    TopicSection.Quran -> verses.map { TopicPassage("quran-${it.reference}", it.arabic, it.translation, it.source, null) }
    TopicSection.Hadith -> hadith.mapIndexed { index, hadith ->
        TopicPassage("hadith-$index", hadith.arabic, hadith.translation, hadith.source, null)
    }
    TopicSection.Dua -> duas.map { TopicPassage(it.id, it.arabic, it.translation, it.source, it.transliteration.ifBlank { null }) }
}

private fun PassageTopic.credits(section: TopicSection): List<String> = when (section) {
    TopicSection.Quran -> verses.map { it.credit }
    TopicSection.Hadith -> hadith.map { it.credit }
    TopicSection.Dua -> duas.map { it.translationCredit }
}.distinct()
