package com.muttaqi.shared.feature.topics.presentation.explore

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.topics.domain.usecase.BuildExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.ExploreSearchIndex
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreGroups
import com.muttaqi.shared.feature.topics.domain.usecase.GetExploreHeader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val getHeader: GetExploreHeader,
    private val getGroups: GetExploreGroups,
    private val buildSearchIndex: BuildExploreSearchIndex,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<ExploreState, ExploreIntent, ExploreMutation, ExploreEffect>(ExploreState(), ExploreReducer) {

    private var searchIndex: ExploreSearchIndex? = null

    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language ->
                try {
                    val groups = getGroups(language)
                    val index = buildSearchIndex(groups).also { searchIndex = it }
                    mutate(ExploreMutation.Loaded(getHeader().displayed(language), groups, index.search(state.value.query)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(ExploreMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: ExploreIntent) {
        when (intent) {
            is ExploreIntent.QueryChanged ->
                mutate(ExploreMutation.SearchUpdated(intent.query, searchIndex?.search(intent.query).orEmpty()))
            ExploreIntent.ClearQuery -> mutate(ExploreMutation.SearchUpdated("", emptyList()))
            is ExploreIntent.TopicTapped -> emit(ExploreEffect.OpenTopic(intent.topicId))
        }
    }
}
