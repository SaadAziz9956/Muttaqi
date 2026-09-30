package com.muttaqi.shared.feature.dhikr.data.progress

import com.muttaqi.shared.core.preferences.LegacyDataSource
import com.muttaqi.shared.feature.dhikr.data.dto.DhikrProgressDto
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.repository.DhikrProgressRepository
import com.russhwolf.settings.Settings
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong
import kotlin.time.Instant

/**
 * Each dhikr's progress under `dhikr_progress.<id>`, in the JSON the Swift app has always written, so a count made
 * before the move to shared code carries on. The Swift app stored that JSON as data, which settings can't read, so
 * [legacy] reads those values until the next count rewrites them as text.
 */
class SettingsDhikrProgressRepository(
    private val settings: Settings,
    private val legacy: LegacyDataSource,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DhikrProgressRepository {

    override fun saved(dhikrId: String): DhikrProgress? {
        val key = key(dhikrId)
        val json = settings.getStringOrNull(key) ?: legacy.text(key) ?: return null
        // Unreadable progress counts as none, as it did in the Swift app; it only ever holds one day's count
        val dto = try {
            ProgressJson.decodeFromString<DhikrProgressDto>(json)
        } catch (_: IllegalArgumentException) {
            return null
        }
        val day = Instant.fromEpochMilliseconds(((dto.day + SECONDS_FROM_1970_TO_2001) * 1000).roundToLong())
            .toLocalDateTime(timeZone()).date
        return DhikrProgress(count = dto.count, rounds = dto.rounds, day = day)
    }

    override fun save(dhikrId: String, progress: DhikrProgress) {
        // The start of the day where the reader is, as Swift's Calendar.startOfDay gave it
        val start = progress.day.atStartOfDayIn(timeZone()).epochSeconds - SECONDS_FROM_1970_TO_2001
        settings.putString(key(dhikrId), ProgressJson.encodeToString(DhikrProgressDto(progress.count, progress.rounds, start.toDouble())))
    }

    private companion object {
        /** Foundation's reference date, 2001-01-01 UTC, from which JSONEncoder counts a Date's seconds */
        const val SECONDS_FROM_1970_TO_2001 = 978_307_200L

        val ProgressJson = Json { ignoreUnknownKeys = true }

        fun key(dhikrId: String) = "dhikr_progress.$dhikrId"
    }
}
