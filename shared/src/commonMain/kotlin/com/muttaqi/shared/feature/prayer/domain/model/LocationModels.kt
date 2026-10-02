package com.muttaqi.shared.feature.prayer.domain.model

data class Coordinates(val latitude: Double, val longitude: Double)

enum class LocationAccess {
    NotDetermined,
    Denied,
    Granted,
}
