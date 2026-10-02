package com.muttaqi.shared.feature.dua.presentation.category

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.feature.dua.domain.model.DuaCategory

data class DuaCategoryState(
    val isLoading: Boolean = true,
    val category: DuaCategory? = null,
) : UiState

sealed interface DuaCategoryIntent : UiIntent {
    data class ChapterTapped(val chapterId: String) : DuaCategoryIntent
}

sealed interface DuaCategoryMutation : UiMutation {
    data class Loaded(val category: DuaCategory?) : DuaCategoryMutation
}

sealed interface DuaCategoryEffect : UiEffect {
    data class OpenChapter(val chapterId: String) : DuaCategoryEffect
}
