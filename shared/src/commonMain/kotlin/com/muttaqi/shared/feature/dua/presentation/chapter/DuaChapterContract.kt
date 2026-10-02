package com.muttaqi.shared.feature.dua.presentation.chapter

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dua.domain.model.DuaChapter

data class DuaChapterState(
    val isLoading: Boolean = true,
    val chapter: DuaChapter? = null,
    val translationCredits: List<String> = emptyList(),
) : UiState

sealed interface DuaChapterIntent : UiIntent {
    data class ShareTapped(val entryId: String) : DuaChapterIntent
    data class CopyTapped(val entryId: String) : DuaChapterIntent
}

sealed interface DuaChapterMutation : UiMutation {
    data class Loaded(val chapter: DuaChapter?) : DuaChapterMutation
}

sealed interface DuaChapterEffect : UiEffect {
    data class OpenShare(val passage: SharePassage) : DuaChapterEffect
    data class Copy(val text: String) : DuaChapterEffect
}
