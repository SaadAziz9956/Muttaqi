package com.muttaqi.shared.feature.dua.presentation.list

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.dua.domain.usecase.BuildDuaSearchIndex
import com.muttaqi.shared.feature.dua.domain.usecase.DuaSearchIndex
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategories
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class DuaListViewModel(
    private val getCategories: GetDuaCategories,
    private val buildSearchIndex: BuildDuaSearchIndex,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<DuaListState, DuaListIntent, DuaListMutation, DuaListEffect>(DuaListState(), DuaListReducer) {

    private var searchIndex: DuaSearchIndex? = null

    init {
        viewModelScope.launch {
            selectedLanguage.changes.collect { language ->
                try {
                    val categories = getCategories(language)
                    val index = buildSearchIndex(categories).also { searchIndex = it }
                    mutate(DuaListMutation.Loaded(PageQuotes.callUponMe.displayed(language), categories, index.search(state.value.query)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(DuaListMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: DuaListIntent) {
        when (intent) {
            is DuaListIntent.QueryChanged ->
                mutate(DuaListMutation.SearchUpdated(intent.query, searchIndex?.search(intent.query).orEmpty()))
            DuaListIntent.ClearQuery -> mutate(DuaListMutation.SearchUpdated("", emptyList()))
            is DuaListIntent.CategoryTapped -> {
                val category = state.value.categories.firstOrNull { it.id == intent.categoryId } ?: return
                val only = category.chapters.singleOrNull()
                emit(if (only != null) DuaListEffect.OpenChapter(only.id) else DuaListEffect.OpenCategory(category.id))
            }
            is DuaListIntent.SearchResultTapped -> emit(DuaListEffect.OpenChapter(intent.chapterId))
        }
    }
}
