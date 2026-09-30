package com.muttaqi.shared.feature.home.presentation

import com.muttaqi.shared.core.mvi.Reducer
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess

internal object HomeReducer : Reducer<HomeState, HomeMutation> {
    override fun reduce(state: HomeState, mutation: HomeMutation): HomeState = when (mutation) {
        is HomeMutation.ClockTicked -> state.copy(now = mutation.now, today = mutation.today, hijriDate = mutation.hijriDate)
        is HomeMutation.ContentLoaded -> with(mutation.content) {
            state.copy(
                greeting = greeting,
                ayahOfTheDay = ayah,
                hadithOfTheDay = hadith,
                duaOfTheDay = dua,
                nameOfTheDay = name,
                topicOfTheDay = topic,
            )
        }
        is HomeMutation.DhikrCounted -> state.copy(dhikrToday = mutation.total)
        is HomeMutation.JournalLoaded -> state.copy(journalToday = mutation.entry)
        is HomeMutation.ShortcutsLoaded -> state.copy(lastReading = mutation.shortcuts.lastReading, kahf = mutation.shortcuts.kahf)
        is HomeMutation.Located -> state.copy(schedule = mutation.schedule, qibla = mutation.qibla)
        is HomeMutation.LocationChecked -> state.copy(
            location = when {
                state.schedule != null -> HomeLocation.Available
                mutation.access == LocationAccess.Granted -> HomeLocation.Unknown
                mutation.access == LocationAccess.Denied -> HomeLocation.Denied
                else -> HomeLocation.NeedsPermission
            },
        )
    }
}
