package com.muttaqi.shared.feature.prayer.domain.model

/** A place on Earth in degrees, as the device's location services report it */
data class Coordinates(val latitude: Double, val longitude: Double)

/** Whether the app may use the device's location */
enum class LocationAccess {
    /** Not asked yet, so the app can ask */
    NotDetermined,
    /** Refused, or restricted on the device; only the system settings can change it */
    Denied,
    Granted,
}
