package com.muttaqi.shared.feature.dhikr.presentation.list

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection

/** The Dikr page: the hadith under the title, a tab for each section, and the picked section's dhikr */
data class DhikrListState(
    val isLoading: Boolean = true,
    val header: DisplayedQuote? = null,
    val sections: List<DhikrSection> = emptyList(),
    val selectedSectionId: String? = null,
    val failed: Boolean = false,
) : UiState {
    /** The tab being shown: the one picked, or the first */
    val selectedSection: DhikrSection? get() = sections.firstOrNull { it.id == selectedSectionId } ?: sections.firstOrNull()
}

sealed interface DhikrListIntent : UiIntent {
    data class SectionTapped(val sectionId: String) : DhikrListIntent
    data class DhikrTapped(val dhikrId: String) : DhikrListIntent
}

sealed interface DhikrListMutation : UiMutation {
    data class Loaded(val header: DisplayedQuote, val sections: List<DhikrSection>) : DhikrListMutation
    data class SectionSelected(val sectionId: String) : DhikrListMutation
    data object LoadFailed : DhikrListMutation
}

sealed interface DhikrListEffect : UiEffect {
    data class OpenCounter(val dhikrId: String) : DhikrListEffect
}
