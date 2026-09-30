package com.muttaqi.shared.feature.names

import com.muttaqi.shared.feature.names.presentation.NamesViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/** The 99 Names view model for Swift, from Koin: `NamesViewModels.shared.names()`, shared by the page and its search */
object NamesViewModels : KoinComponent {
    fun names(): NamesViewModel = get()
}
