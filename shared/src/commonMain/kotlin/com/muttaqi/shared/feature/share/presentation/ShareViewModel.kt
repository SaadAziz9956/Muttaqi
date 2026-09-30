package com.muttaqi.shared.feature.share.presentation

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.core.share.SharePassage
import kotlinx.coroutines.launch

class ShareViewModel(
    passage: SharePassage,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<ShareState, ShareIntent, ShareMutation, ShareEffect>(
    // The verse is there from the first frame, so the page doesn't change height as it opens
    ShareState(passage, PageQuotes.inviteWithWisdom.displayed(selectedLanguage.current)),
    ShareReducer,
) {
    init {
        viewModelScope.launch {
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

/**
 * The name of the card's image file, without its extension: where the passage is from, e.g. "Quran (2-255)", with
 * the characters file systems reject replaced by a dash
 */
internal fun SharePassage.imageFileName(): String =
    reference.map { if (it in "\\/:*?\"<>|" || it.isISOControl()) '-' else it }.joinToString("").trim().ifEmpty { "Muttaqi" }
