package com.muttaqi.shared.feature.home

import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** Home's view model for Swift, from Koin: `HomeViewModels.shared.home()` */
object HomeViewModels : KoinComponent {
    fun home(): HomeViewModel = get()
}
