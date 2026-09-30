package com.muttaqi.shared.feature.dhikr.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import kotlinx.datetime.LocalDate

/** Every dhikr, grouped into the sections the Dikr page shows, in order */
fun interface DhikrRepository {
    suspend fun sections(language: Language): List<DhikrSection>
}

/** Each dhikr's count as last saved, so leaving the counter and coming back carries on where it stopped */
interface DhikrProgressRepository {
    /** The progress last saved for the dhikr, whatever day it was; null if it has never been said */
    fun saved(dhikrId: String): DhikrProgress?
    fun save(dhikrId: String, progress: DhikrProgress)
}

/** Today's date where the reader is, so counts start again at their midnight */
fun interface CurrentDay {
    fun today(): LocalDate
}
