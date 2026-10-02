package com.muttaqi.shared.feature.dhikr.presentation.list

import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSections
import kotlinx.coroutines.CancellationException

class DhikrListViewModel(
    private val getSections: GetDhikrSections,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<DhikrListState, DhikrListIntent, DhikrListMutation, DhikrListEffect>(DhikrListState(), DhikrListReducer) {

    init {
        launchNow {
            selectedLanguage.changes.collect { language ->
                try {
                    mutate(DhikrListMutation.Loaded(PageQuotes.rememberingAllah.displayed(language), getSections(language)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(DhikrListMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: DhikrListIntent) {
        when (intent) {
            is DhikrListIntent.SectionTapped -> mutate(DhikrListMutation.SectionSelected(intent.sectionId))
            is DhikrListIntent.DhikrTapped -> emit(DhikrListEffect.OpenCounter(intent.dhikrId))
        }
    }
}
