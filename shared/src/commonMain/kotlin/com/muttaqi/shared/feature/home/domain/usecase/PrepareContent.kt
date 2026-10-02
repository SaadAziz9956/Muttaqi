package com.muttaqi.shared.feature.home.domain.usecase

import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dua.domain.usecase.GetDuaCategories
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.home.domain.platform.today
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveJournalEntries
import com.muttaqi.shared.feature.topics.domain.usecase.GetEmotions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class PrepareContent(
    private val getDailyContent: GetDailyContent,
    private val getQuranShortcuts: GetQuranShortcuts,
    private val getDhikrSaidToday: GetDhikrSaidToday,
    private val getDuaCategories: GetDuaCategories,
    private val getEmotions: GetEmotions,
    private val observeJournalEntries: ObserveJournalEntries,
    private val clock: ReaderClock,
    private val selectedLanguage: SelectedLanguage,
) {
    suspend operator fun invoke(budget: Duration = BUDGET) {
        val language = selectedLanguage.current
        withTimeoutOrNull(budget) {
            coroutineScope {
                launch { quietly { getDailyContent(clock.today(), language) } }
                launch { quietly { getQuranShortcuts() } }
                launch { quietly { getDhikrSaidToday() } }
                launch { quietly { getDuaCategories(language) } }
                launch { quietly { getEmotions(language) } }
                launch { quietly { observeJournalEntries().first() } }
            }
        }
    }

    private companion object {
        val BUDGET = 1500.milliseconds
    }
}

private suspend fun quietly(block: suspend () -> Any?) {
    try {
        block()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
    }
}
