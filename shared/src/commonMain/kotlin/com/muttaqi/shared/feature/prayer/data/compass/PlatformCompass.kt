package com.muttaqi.shared.feature.prayer.data.compass

import com.muttaqi.shared.feature.prayer.domain.model.CompassHeading
import com.muttaqi.shared.feature.prayer.domain.platform.HeadingProvider
import com.muttaqi.shared.feature.prayer.domain.repository.Compass
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

class PlatformCompass(private val provider: HeadingProvider) : Compass {
    override val isAvailable: Boolean get() = provider.isAvailable

    override fun headings(): Flow<CompassHeading> = callbackFlow {
        val updates = provider.startUpdates { heading -> trySend(heading) }
        awaitClose { updates.stop() }
    }.conflate()
}
