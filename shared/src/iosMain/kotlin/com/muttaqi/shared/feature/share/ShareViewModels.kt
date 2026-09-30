package com.muttaqi.shared.feature.share

import com.muttaqi.shared.core.share.SharePassage
import com.muttaqi.shared.feature.share.presentation.ShareViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

/** The Share page's view model for Swift, from Koin: `ShareViewModels.shared.share(passage: passage)` */
object ShareViewModels : KoinComponent {
    fun share(passage: SharePassage): ShareViewModel = get { parametersOf(passage) }
}
