package com.muttaqi.shared.feature.share.di

import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.share.presentation.ShareViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val shareModule = module {
    viewModel { (passage: SharePassage) -> ShareViewModel(passage, get()) }
}
