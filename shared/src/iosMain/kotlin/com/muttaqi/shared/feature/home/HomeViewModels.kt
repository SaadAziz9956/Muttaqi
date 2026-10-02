package com.muttaqi.shared.feature.home

import com.muttaqi.shared.feature.home.domain.usecase.PrepareContent
import com.muttaqi.shared.feature.home.presentation.HomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object HomeViewModels : KoinComponent {
    fun home(): HomeViewModel = get()
}

object ContentPreparation : KoinComponent {
    suspend fun prepare() = get<PrepareContent>()()
}
