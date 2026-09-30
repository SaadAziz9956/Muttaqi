package com.muttaqi.shared.feature.prayer.data.location

import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.prayer.domain.model.Coordinates
import com.russhwolf.settings.Settings
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The last fix under `last_known_coordinates`, as the JSON the Swift app has always written, so prayer times show
 * straight away after the update. The Swift app stored that JSON as data, which settings can't read, so [legacy]
 * reads it until the next fix is saved as text
 */
class SavedCoordinates(private val settings: Settings, private val legacy: LegacyDataSource) {

    fun load(): Coordinates? {
        val json = settings.getStringOrNull(KEY) ?: legacy.text(KEY) ?: return null
        // Unreadable coordinates count as none, as they did in the Swift app; the next fix replaces them
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

/** The Swift app's `Coordinates` as its JSONEncoder wrote them */
@Serializable
internal data class CoordinatesDto(val latitude: Double, val longitude: Double)
