package com.muttaqi.shared.feature.home.domain.model

import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage

/**
 * The day's texts on Home, in the reader's language. Each is null when it can't be had, e.g. the verses before the
 * Quran has been downloaded
 */
data class DailyContent(
    /** Quran 3:139, under the greeting */
    val greeting: DailyAyah? = null,
    val ayah: DailyAyah? = null,
    /** A short authentic hadith from Explore */
    val hadith: HadithPassage? = null,
    val dua: QuranicDua? = null,
    val name: AllahName? = null,
    val topic: ExploreTopic? = null,
)

/** Where the reader left off in the Quran, with that surah, to open it there again */
data class LastReading(val surah: Surah, val ayahNumber: Int)

/** Home's ways into the Quran: where the reader left off, and Surah al-Kahf for Fridays */
data class QuranShortcuts(val lastReading: LastReading? = null, val kahf: Surah? = null)

/** The prayer times and the Qibla where the reader is; no times where they can't be worked out, e.g. near the poles */
data class PrayerTimesHere(val schedule: PrayerSchedule?, val qibla: QiblaDirection)
