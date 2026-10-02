package com.muttaqi.shared.feature.prayer.data.location

import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class SavedCoordinates(private val settings: Settings, private val legacy: LegacyDataSource) {

    fun load(): Coordinates? {
        val json = settings.getStringOrNull(KEY) ?: legacy.text(KEY) ?: return null
        val dto = try {
            CoordinatesJson.decodeFromString<CoordinatesDto>(json)
        } catch (_: IllegalArgumentException) {
            return null
        }
        return Coordinates(dto.latitude, dto.longitude)
    }

    fun save(coordinates: Coordinates) {
        settings.putString(KEY, CoordinatesJson.encodeToString(CoordinatesDto(coordinates.latitude, coordinates.longitude)))
    }

    private companion object {
        const val KEY = "last_known_coordinates"
        val CoordinatesJson = Json { ignoreUnknownKeys = true }
    }
}

@Serializable
internal data class CoordinatesDto(val latitude: Double, val longitude: Double)
