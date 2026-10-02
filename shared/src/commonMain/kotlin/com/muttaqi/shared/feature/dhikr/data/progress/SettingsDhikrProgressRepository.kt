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

class SettingsDhikrProgressRepository(
    private val settings: Settings,
    private val legacy: LegacyDataSource,
    private val timeZone: () -> TimeZone = { TimeZone.currentSystemDefault() },
) : DhikrProgressRepository {

    override fun saved(dhikrId: String): DhikrProgress? {
        val key = key(dhikrId)
        val json = settings.getStringOrNull(key) ?: legacy.text(key) ?: return null
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
        val start = progress.day.atStartOfDayIn(timeZone()).epochSeconds - SECONDS_FROM_1970_TO_2001
        settings.putString(key(dhikrId), ProgressJson.encodeToString(DhikrProgressDto(progress.count, progress.rounds, start.toDouble())))
    }

    private companion object {
        const val SECONDS_FROM_1970_TO_2001 = 978_307_200L

        val ProgressJson = Json { ignoreUnknownKeys = true }

        fun key(dhikrId: String) = "dhikr_progress.$dhikrId"
    }
}
