package com.muttaqi.shared.feature.quran.presentation.list

import com.muttaqi.shared.core.domain.Outcome
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.feature.quran.domain.usecase.FilterSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.GetLastReading
import com.muttaqi.shared.feature.quran.domain.usecase.GetSurahs
import com.muttaqi.shared.feature.quran.domain.usecase.SyncQuran
import com.muttaqi.shared.feature.quran.presentation.QuranMessages
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

class QuranListViewModel(
    private val syncQuran: SyncQuran,
    private val getSurahs: GetSurahs,
    private val getLastReading: GetLastReading,
    private val filterSurahs: FilterSurahs,
    private val selectedLanguage: SelectedLanguage,
) : MviViewModel<QuranListState, QuranListIntent, QuranListMutation, QuranListEffect>(
    QuranListState(header = PageQuotes.learnAndTeachQuran.displayed(selectedLanguage.current)),
    QuranListReducer,
) {

    private var loading: Job? = null

    init {
        launchNow {
            selectedLanguage.changes.collect { language ->
                mutate(QuranListMutation.HeaderChanged(PageQuotes.learnAndTeachQuran.displayed(language)))
            }
        }
        load()
    }

    override fun handle(intent: QuranListIntent) {
        when (intent) {
            QuranListIntent.Appeared -> if (!state.value.isLoading && state.value.error == null) refreshProgress()
            QuranListIntent.Retry -> load()
            is QuranListIntent.QueryChanged -> filter(intent.query, state.value.filter)
            QuranListIntent.ClearQuery -> filter("", state.value.filter)
            is QuranListIntent.FilterSelected -> filter(state.value.query, intent.filter)
            is QuranListIntent.SurahTapped -> emit(QuranListEffect.OpenSurah(intent.surahNumber))
            QuranListIntent.ContinueTapped -> {
                val progress = state.value.readingProgress ?: return
                if (state.value.surahs.none { it.number == progress.surahNumber }) return
                emit(QuranListEffect.ContinueReading(progress.surahNumber, progress.lastAyahNumber))
            }
        }
    }

    private fun load() {
        if (loading?.isActive == true) return
        mutate(QuranListMutation.Loading)
        loading = launchNow {
            val language = selectedLanguage.current
            try {
                if (syncQuran(language) is Outcome.Failure) {
                    mutate(QuranListMutation.LoadFailed(QuranMessages.downloadFailed(language)))
                    return@launchNow
                }
                val surahs = getSurahs()
                val visible = filterSurahs(surahs, state.value.query, state.value.filter.revelation)
                mutate(QuranListMutation.Loaded(surahs, getLastReading(), visible))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutate(QuranListMutation.LoadFailed(error.message ?: QuranMessages.downloadFailed(language)))
            }
        }
    }

    private fun refreshProgress() {
        launchNow {
            val progress = getLastReading()
            if (progress != state.value.readingProgress) mutate(QuranListMutation.ProgressChanged(progress))
        }
    }

    private fun filter(query: String, filter: RevelationFilter) {
        mutate(QuranListMutation.Filtered(query, filter, filterSurahs(state.value.surahs, query, filter.revelation)))
    }
}
