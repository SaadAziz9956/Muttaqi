package com.muttaqi.shared.feature.quran.presentation.list

import com.muttaqi.shared.core.mvi.UiEffect
import com.muttaqi.shared.core.mvi.UiIntent
import com.muttaqi.shared.core.mvi.UiMutation
import com.muttaqi.shared.core.mvi.UiState
import com.muttaqi.shared.core.quote.DisplayedQuote
import com.muttaqi.shared.feature.quran.domain.model.ReadingProgress
import com.muttaqi.shared.feature.quran.domain.model.Revelation
import com.muttaqi.shared.feature.quran.domain.model.Surah

/** Where a surah was revealed, to narrow the list */
enum class RevelationFilter(val label: String, val revelation: Revelation?) {
    All("All", null),
    Meccan("Meccan", Revelation.Meccan),
    Medinan("Medinan", Revelation.Medinan),
}

/** The Quran tab: the hadith under the title, where the reader left off, and the surahs to search and filter */
data class QuranListState(
    val isLoading: Boolean = true,
    /** Why the Quran couldn't be loaded, e.g. it couldn't be downloaded; null when it could */
    val error: String? = null,
    val header: DisplayedQuote? = null,
    val surahs: List<Surah> = emptyList(),
    val readingProgress: ReadingProgress? = null,
    val query: String = "",
    val filter: RevelationFilter = RevelationFilter.All,
    /** The surahs revealed where chosen that match the search */
    val visibleSurahs: List<Surah> = emptyList(),
) : UiState {
    val isSearching: Boolean get() = query.isNotBlank()
}

sealed interface QuranListIntent : UiIntent {
    /** The list is on screen again, e.g. back from a surah, so the reading position may have moved */
    data object Appeared : QuranListIntent
    data object Retry : QuranListIntent
    data class QueryChanged(val query: String) : QuranListIntent
    data object ClearQuery : QuranListIntent
    data class FilterSelected(val filter: RevelationFilter) : QuranListIntent
    data class SurahTapped(val surahNumber: Int) : QuranListIntent
    data object ContinueTapped : QuranListIntent
}

sealed interface QuranListMutation : UiMutation {
    data object Loading : QuranListMutation
    data class HeaderChanged(val header: DisplayedQuote) : QuranListMutation
    data class Loaded(
        val surahs: List<Surah>,
        val readingProgress: ReadingProgress?,
        val visibleSurahs: List<Surah>,
    ) : QuranListMutation
    data class LoadFailed(val message: String) : QuranListMutation
    data class ProgressChanged(val readingProgress: ReadingProgress?) : QuranListMutation
    data class Filtered(val query: String, val filter: RevelationFilter, val visibleSurahs: List<Surah>) : QuranListMutation
}

sealed interface QuranListEffect : UiEffect {
    data class OpenSurah(val surahNumber: Int) : QuranListEffect
    /** Opens the surah scrolled to where the reader left off */
    data class ContinueReading(val surahNumber: Int, val ayahNumber: Int) : QuranListEffect
}
