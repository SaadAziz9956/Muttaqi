package com.muttaqi.shared.feature.names.presentation

import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.quote.PageQuotes
import com.muttaqi.shared.core.quote.displayed
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.names.domain.usecase.BuildNamesSearchIndex
import com.muttaqi.shared.feature.names.domain.usecase.GetAllahNames
import com.muttaqi.shared.feature.names.domain.usecase.NamesSearchIndex
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NamesViewModel(
    private val getNames: GetAllahNames,
    private val buildSearchIndex: BuildNamesSearchIndex,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<NamesState, NamesIntent, NamesMutation, NamesEffect>(NamesState(), NamesReducer) {

    private var searchIndex: NamesSearchIndex? = null

    private val mutablePosition = MutableStateFlow(1)
    val position: StateFlow<Int> = mutablePosition.asStateFlow()

    init {
        launchNow {
            selectedLanguage.changes.collect { language ->
                try {
                    val names = getNames(language)
                    val index = buildSearchIndex(names).also { searchIndex = it }
                    val current = state.value
                    mutate(NamesMutation.Loaded(names, PageQuotes.ninetyNineNames.displayed(language), index.search(current.query, current.searchMode)))
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    mutate(NamesMutation.LoadFailed)
                }
            }
        }
    }

    override fun handle(intent: NamesIntent) {
        when (intent) {
            is NamesIntent.NameShown -> mutablePosition.value = intent.number
            NamesIntent.ShareTapped -> name(position.value)?.let { emit(NamesEffect.OpenShare(it.toSharePassage())) }
            is NamesIntent.QueryChanged -> mutate(
                NamesMutation.SearchUpdated(intent.query, searchIndex?.search(intent.query, state.value.searchMode).orEmpty()),
            )
            is NamesIntent.SearchModeChanged -> if (intent.mode != state.value.searchMode) mutate(NamesMutation.SearchModeChanged(intent.mode))
            NamesIntent.ClearQuery -> mutate(NamesMutation.SearchUpdated("", emptyList()))
            is NamesIntent.ResultTapped -> {
                mutablePosition.value = intent.number
                emit(NamesEffect.ShowName(intent.number))
            }
        }
    }

    private fun name(number: Int): AllahName? = state.value.names.firstOrNull { it.number == number }
}

fun AllahName.toSharePassage() = SharePassage(
    arabic = arabic,
    transliteration = transliteration,
    translation = meaning,
    reference = "The Names of Allah ($number of 99)",
)
