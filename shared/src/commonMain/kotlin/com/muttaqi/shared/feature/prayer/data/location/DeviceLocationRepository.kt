package com.muttaqi.shared.feature.prayer.data.location

import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.muttaqi.shared.feature.prayer.domain.repository.LocationRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DeviceLocationRepository(
    private val provider: LocationProvider,
    private val saved: SavedCoordinates,
    private val fixTimeout: Duration = 10.seconds,
) : LocationRepository {

    override val access: LocationAccess get() = provider.access

    override suspend fun requestAccess(): LocationAccess = suspendCancellableCoroutine { continuation ->
        provider.requestAccess { access -> if (continuation.isActive) continuation.resume(access) }
    }

    override fun lastKnownCoordinates(): Coordinates? = saved.load()

    override suspend fun refreshCoordinates(): Coordinates? {
        if (access != LocationAccess.Granted) return null
        val fix = withTimeoutOrNull(fixTimeout) {
            suspendCancellableCoroutine<Coordinates?> { continuation ->
                val request = provider.currentLocation { fix -> if (continuation.isActive) continuation.resume(fix) }
                continuation.invokeOnCancellation { request.cancel() }
            }
        } ?: return null
        saved.save(fix)
        return fix
    }
}
