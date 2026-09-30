package com.muttaqi.shared.feature.dhikr

import com.muttaqi.shared.feature.dhikr.domain.usecase.GetDhikrSaidToday
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Home's Dikr tile for Swift, while Home is still in Swift: every dhikr said today, all counters together, e.g.
 * `try await DhikrSaidToday.shared.count()`
 */
object DhikrSaidToday : KoinComponent {
    suspend fun count(): Int = get<GetDhikrSaidToday>()()
}
