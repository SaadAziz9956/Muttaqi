package com.muttaqi.shared.feature.home.presentation

import androidx.lifecycle.viewModelScope
import com.muttaqi.shared.core.mvi.MviViewModel
import com.muttaqi.shared.core.preferences.SelectedLanguage
import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.home.domain.platform.ReaderClock
import com.muttaqi.shared.feature.home.domain.platform.dateAt
import com.muttaqi.shared.feature.home.domain.platform.today
import com.muttaqi.shared.feature.home.domain.usecase.GetDailyContent
import com.muttaqi.shared.feature.home.domain.usecase.GetHijriDate
import com.muttaqi.shared.feature.home.domain.usecase.GetQuranShortcuts
import com.muttaqi.shared.feature.home.domain.usecase.LocatePrayerTimes
import com.muttaqi.shared.feature.journal.domain.usecase.ObserveTodaysJournalEntry
import com.muttaqi.shared.feature.prayer.domain.usecase.FollowHeading
import com.muttaqi.shared.feature.prayer.domain.usecase.GetLocationAccess
import com.muttaqi.shared.feature.prayer.domain.usecase.RequestLocationAccess
import com.muttaqi.shared.feature.prayer.presentation.qibla.shortestTurn
import com.muttaqi.shared.feature.quran.presentation.copyText
import com.muttaqi.shared.feature.quran.presentation.toSharePassage
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Instant

class HomeViewModel(
    private val getDailyContent: GetDailyContent,
    private val getQuranShortcuts: GetQuranShortcuts,
    private val getDhikrSaidToday: GetDhikrSaidToday,
    private val observeTodaysJournalEntry: ObserveTodaysJournalEntry,
    private val locatePrayerTimes: LocatePrayerTimes,
    private val getLocationAccess: GetLocationAccess,
    private val requestLocationAccess: RequestLocationAccess,
    private val followHeading: FollowHeading,
    private val getHijriDate: GetHijriDate,
    private val clock: ReaderClock,
    selectedLanguage: SelectedLanguage,
) : MviViewModel<HomeState, HomeIntent, HomeMutation, HomeEffect>(
    HomeState(isCompassAvailable = followHeading.isAvailable),
    HomeReducer,
) {
    private val refreshes = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val qiblaArrow: StateFlow<Double?> = state.map { it.qibla != null }
        .distinctUntilChanged()
        .flatMapLatest { located -> if (located) arrowRotations() else flowOf(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        tick(clock.now())
        viewModelScope.launch { clock.minutes().collect(::tick) }
        viewModelScope.launch {
            combine(selectedLanguage.changes, refreshes) { language, _ -> language }.collectLatest { language ->
                mutate(HomeMutation.ContentLoaded(getDailyContent(clock.today(), language)))
            }
        }
        viewModelScope.launch {
            refreshes.collectLatest {
                mutate(HomeMutation.DhikrCounted(orZero { getDhikrSaidToday() }))
                mutate(HomeMutation.ShortcutsLoaded(getQuranShortcuts()))
            }
        }
        viewModelScope.launch {
            refreshes.collectLatest {
                observeTodaysJournalEntry().catch { emit(null) }.collect { mutate(HomeMutation.JournalLoaded(it)) }
            }
        }
        viewModelScope.launch { refreshes.collectLatest { locate() } }
    }

    override fun handle(intent: HomeIntent) {
        val current = state.value
        when (intent) {
            HomeIntent.Refresh -> refreshes.update { it + 1 }
            HomeIntent.SetLocationTapped -> if (current.location == HomeLocation.Denied) {
                emit(HomeEffect.OpenSettings)
            } else {
                viewModelScope.launch {
                    requestLocationAccess()
                    locate()
                }
            }
            HomeIntent.QiblaTapped -> emit(HomeEffect.OpenQibla)
            HomeIntent.DhikrTapped -> emit(HomeEffect.OpenDhikr)
            HomeIntent.NameTapped -> emit(HomeEffect.OpenNames)
            HomeIntent.JournalTapped -> emit(HomeEffect.OpenJournal)
            HomeIntent.TodaysEntryTapped -> emit(HomeEffect.OpenJournalEntry(current.journalToday?.id))
            HomeIntent.EmotionsTapped -> emit(HomeEffect.OpenEmotions)
            HomeIntent.TopicTapped -> current.topicOfTheDay?.let { emit(HomeEffect.OpenTopic(it.id)) }
            HomeIntent.ContinueReadingTapped -> current.lastReading?.let { emit(HomeEffect.OpenSurah(it.surah, it.ayahNumber)) }
            HomeIntent.KahfTapped -> current.kahf?.let { emit(HomeEffect.OpenSurah(it, 1)) }
            HomeIntent.AyahOfTheDayTapped -> current.ayahOfTheDay?.let { emit(HomeEffect.OpenSurah(it.surah, it.ayah.numberInSurah)) }
            is HomeIntent.ShareTapped -> current.sharePassage(intent.card)?.let { emit(HomeEffect.OpenShare(it)) }
            is HomeIntent.CopyTapped -> current.copyText(intent.card)?.let { emit(HomeEffect.Copy(it)) }
        }
    }

    private fun tick(now: Instant) {
        val maghrib = state.value.schedule?.today?.maghrib
        mutate(HomeMutation.ClockTicked(now, clock.dateAt(now), getHijriDate(now, maghrib, clock.timeZone)))
    }

    private suspend fun locate() {
        locatePrayerTimes().collect { here ->
            mutate(HomeMutation.Located(here.schedule, here.qibla))
            tick(clock.now())
        }
        mutate(HomeMutation.LocationChecked(getLocationAccess()))
    }

    private fun arrowRotations(): Flow<Double?> = followHeading()
        .mapNotNull { heading -> state.value.qibla?.let { it.bearing - heading.degrees } }
        .runningFold<Double, ArrowTurn?>(null) { turn, target -> turn?.next(target) ?: ArrowTurn(target, target) }
        .map { it?.rotation }
}

private data class ArrowTurn(val target: Double, val rotation: Double) {
    fun next(target: Double) = ArrowTurn(target, rotation + shortestTurn(this.target, target))
}

private fun HomeState.sharePassage(card: DailyCard): SharePassage? = when (card) {
    DailyCard.Ayah -> ayahOfTheDay?.ayah?.toSharePassage()
    DailyCard.Hadith -> hadithOfTheDay?.toSharePassage()
    DailyCard.Dua -> duaOfTheDay?.toSharePassage()
}

private fun HomeState.copyText(card: DailyCard): String? = when (card) {
    DailyCard.Ayah -> ayahOfTheDay?.ayah?.copyText()
    DailyCard.Hadith -> hadithOfTheDay?.let { listOf(it.arabic, it.translation, it.source).joinToString("\n\n") }
    DailyCard.Dua -> duaOfTheDay?.let { listOf(it.arabic, it.transliteration, it.translation, it.source).joinToString("\n\n") }
}

private fun HadithPassage.toSharePassage() = SharePassage(arabic, null, translation, source)

private fun QuranicDua.toSharePassage() = SharePassage(arabic, transliteration, translation, source)

private val QuranicDua.source: String get() = "Quran ($reference)"

private suspend fun orZero(count: suspend () -> Int): Int = try {
    count()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    0
}
