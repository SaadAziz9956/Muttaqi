package com.muttaqi.shared.feature.dhikr.domain.repository

import com.muttaqi.shared.core.model.Language
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrProgress
import com.muttaqi.shared.feature.dhikr.domain.model.DhikrSection
import kotlinx.datetime.LocalDate

fun interface DhikrRepository {
    suspend fun sections(language: Language): List<DhikrSection>
}

interface DhikrProgressRepository {
    fun saved(dhikrId: String): DhikrProgress?
    fun save(dhikrId: String, progress: DhikrProgress)
}

fun interface CurrentDay {
    fun today(): LocalDate
}
