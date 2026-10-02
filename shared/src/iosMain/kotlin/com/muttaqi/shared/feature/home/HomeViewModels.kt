package com.muttaqi.shared.feature.home

import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object HomeViewModels : KoinComponent {
    fun home(): HomeViewModel = get()
}
