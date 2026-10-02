package com.muttaqi.shared.feature.share.presentation

import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.core.share.SharePassage

class ShareViewModel(
    passage: SharePassage,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<ShareState, ShareIntent, ShareMutation, ShareEffect>(
    ShareState(passage, PageQuotes.inviteWithWisdom.displayed(selectedLanguage.current)),
    ShareReducer,
) {
    init {
        launchNow {
            selectedLanguage.changes.collect { language ->
                mutate(ShareMutation.VerseChanged(PageQuotes.inviteWithWisdom.displayed(language)))
            }
        }
    }

    override fun handle(intent: ShareIntent) {
        val passage = state.value.passage
        when (intent) {
            ShareIntent.ShareTapped -> emit(ShareEffect.ShareImage(title = passage.reference, fileName = passage.imageFileName()))
            ShareIntent.SaveTapped -> emit(ShareEffect.SaveImage(fileName = passage.imageFileName()))
        }
    }
}

internal fun SharePassage.imageFileName(): String =
    reference.map { if (it in "\\/:*?\"<>|" || it.isISOControl()) '-' else it }.joinToString("").trim().ifEmpty { "Muttaqi" }
