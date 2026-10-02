package com.muttaqi.shared.feature.home.domain.model

import com.muttaqi.shared.feature.dua.domain.model.QuranicDua
import com.muttaqi.shared.feature.names.domain.model.AllahName
import com.muttaqi.shared.feature.prayer.domain.model.PrayerSchedule
import com.muttaqi.shared.feature.prayer.domain.model.QiblaDirection
import com.muttaqi.shared.feature.quran.domain.model.DailyAyah
import com.muttaqi.shared.feature.quran.domain.model.Surah
import com.muttaqi.shared.feature.topics.domain.model.ExploreTopic
import com.muttaqi.shared.feature.topics.domain.model.HadithPassage

data class DailyContent(
    val greeting: DailyAyah? = null,
    val ayah: DailyAyah? = null,
    val hadith: HadithPassage? = null,
    val dua: QuranicDua? = null,
    val name: AllahName? = null,
    val topic: ExploreTopic? = null,
)

data class LastReading(val surah: Surah, val ayahNumber: Int)

data class QuranShortcuts(val lastReading: LastReading? = null, val kahf: Surah? = null)

data class PrayerTimesHere(val schedule: PrayerSchedule?, val qibla: QiblaDirection)
