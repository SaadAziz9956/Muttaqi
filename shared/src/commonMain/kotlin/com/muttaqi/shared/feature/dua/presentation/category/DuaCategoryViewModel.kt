package com.muttaqi.shared.feature.dua.presentation.category

import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategory

internal object DuaCategoryReducer : Reducer<DuaCategoryState, DuaCategoryMutation> {
    override fun reduce(state: DuaCategoryState, mutation: DuaCategoryMutation) = when (mutation) {
        is DuaCategoryMutation.Loaded -> state.copy(isLoading = false, category = mutation.category)
    }
}

class DuaCategoryViewModel(
    categoryId: String,
    getCategory: GetDuaCategory,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<DuaCategoryState, DuaCategoryIntent, DuaCategoryMutation, DuaCategoryEffect>(
    DuaCategoryState(),
    DuaCategoryReducer,
) {
    init {
        launchNow {
            selectedLanguage.changes.collect { language -> mutate(DuaCategoryMutation.Loaded(getCategory(categoryId, language))) }
        }
    }

    override fun handle(intent: DuaCategoryIntent) {
        when (intent) {
            is DuaCategoryIntent.ChapterTapped -> emit(DuaCategoryEffect.OpenChapter(intent.chapterId))
        }
    }
}
