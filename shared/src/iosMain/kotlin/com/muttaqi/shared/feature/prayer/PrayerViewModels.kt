package com.muttaqi.shared.feature.prayer

import com.muttaqi.shared.feature.prayer.presentation.qibla.QiblaViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object PrayerViewModels : KoinComponent {
    fun qibla(): QiblaViewModel = get()
}
