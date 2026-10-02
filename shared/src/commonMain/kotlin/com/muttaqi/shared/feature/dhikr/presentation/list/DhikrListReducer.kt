package com.muttaqi.shared.feature.dhikr.presentation.list

import com.muttaqi.shared.core.mvi.Reducer

internal object DhikrListReducer : Reducer<DhikrListState, DhikrListMutation> {
    override fun reduce(state: DhikrListState, mutation: DhikrListMutation): DhikrListState = when (mutation) {
        is DhikrListMutation.Loaded -> state.copy(isLoading = false, header = mutation.header, sections = mutation.sections, failed = false)
        is DhikrListMutation.SectionSelected -> state.copy(selectedSectionId = mutation.sectionId)
        DhikrListMutation.LoadFailed -> state.copy(isLoading = false, failed = true)
    }
}
