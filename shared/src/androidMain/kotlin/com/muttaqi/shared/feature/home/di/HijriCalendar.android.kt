package com.muttaqi.shared.feature.home.di

import com.muttaqi.shared.feature.home.data.IcuHijriCalendar
import com.muttaqi.shared.feature.home.domain.platform.HijriCalendar

internal actual fun platformHijriCalendar(): HijriCalendar = IcuHijriCalendar()
