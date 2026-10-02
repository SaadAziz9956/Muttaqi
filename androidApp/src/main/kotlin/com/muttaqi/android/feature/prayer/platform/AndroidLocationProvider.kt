package com.muttaqi.android.feature.prayer.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.muttaqi.android.platform.ActivityPermissions
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.muttaqi.shared.feature.prayer.domain.model.LocationAccess
import com.muttaqi.shared.feature.prayer.domain.platform.LocationProvider
import com.muttaqi.shared.feature.prayer.domain.platform.LocationRequest

class AndroidLocationProvider(private val context: Context, private val permissions: ActivityPermissions) : LocationProvider {
    private val manager = context.getSystemService(LocationManager::class.java)

    override val access: LocationAccess
        get() = when {
            permissions.isGranted(FINE) || permissions.isGranted(COARSE) -> LocationAccess.Granted
            permissions.isPermanentlyDenied(COARSE) -> LocationAccess.Denied
            else -> LocationAccess.NotDetermined
        }

    override fun requestAccess(onResult: (LocationAccess) -> Unit) {
        if (access != LocationAccess.NotDetermined) return onResult(access)
        permissions.request(arrayOf(FINE, COARSE)) { onResult(access) }
    }

    @SuppressLint("MissingPermission")
    override fun currentLocation(onResult: (Coordinates?) -> Unit): LocationRequest {
        val provider = provider()
        if (access != LocationAccess.Granted || provider == null) {
            onResult(null)
            return NoRequest
        }
        val cancellation = CancellationSignal()
        LocationManagerCompat.getCurrentLocation(manager, provider, cancellation, ContextCompat.getMainExecutor(context)) { location ->
            onResult(location?.let { Coordinates(it.latitude, it.longitude) })
        }
        return object : LocationRequest {
            override fun cancel() = cancellation.cancel()
        }
    }

    private fun provider(): String? = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && manager.hasProvider(LocationManager.FUSED_PROVIDER) -> LocationManager.FUSED_PROVIDER
        manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
        else -> null
    }

    private object NoRequest : LocationRequest {
        override fun cancel() = Unit
    }

    private companion object {
        const val FINE = Manifest.permission.ACCESS_FINE_LOCATION
        const val COARSE = Manifest.permission.ACCESS_COARSE_LOCATION
    }
}
