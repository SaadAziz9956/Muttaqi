package com.muttaqi.shared.feature.dua.presentation.chapter

import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dua.domain.model.DuaEntry
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaChapter

internal object DuaChapterReducer : Reducer<DuaChapterState, DuaChapterMutation> {
    override fun reduce(state: DuaChapterState, mutation: DuaChapterMutation) = when (mutation) {
        is DuaChapterMutation.Loaded -> state.copy(
            isLoading = false,
            chapter = mutation.chapter,
            translationCredits = mutation.chapter?.entries?.map { it.translationCredit }?.distinct().orEmpty(),
        )
    }
}

class DuaChapterViewModel(
    chapterId: String,
    getChapter: GetDuaChapter,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<DuaChapterState, DuaChapterIntent, DuaChapterMutation, DuaChapterEffect>(
    DuaChapterState(),
    DuaChapterReducer,
) {
    init {
        launchNow {
            selectedLanguage.changes.collect { language -> mutate(DuaChapterMutation.Loaded(getChapter(chapterId, language))) }
        }
    }

    override fun handle(intent: DuaChapterIntent) {
        when (intent) {
            is DuaChapterIntent.ShareTapped -> entry(intent.entryId)?.let { emit(DuaChapterEffect.OpenShare(it.toSharePassage())) }
            is DuaChapterIntent.CopyTapped -> entry(intent.entryId)?.let { emit(DuaChapterEffect.Copy(it.copyText())) }
        }
    }

    private fun entry(id: String): DuaEntry? = state.value.chapter?.entries?.firstOrNull { it.id == id }
}

fun DuaEntry.toSharePassage() = SharePassage(arabic, transliteration.ifBlank { null }, translation, sourceAndGrade)

fun DuaEntry.copyText(): String = listOf(arabic, transliteration, translation, sourceAndGrade).filter { it.isNotBlank() }.joinToString("\n\n")
